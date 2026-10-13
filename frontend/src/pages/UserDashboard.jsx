import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

const today = () => new Date().toLocaleDateString("en-CA"); // YYYY-MM-DD

export default function UserDashboard() {
  const navigate = useNavigate();
  const [physios, setPhysios] = useState([]);
  const [selected, setSelected] = useState(null);
  const [date, setDate] = useState(today());
  const [slots, setSlots] = useState([]);
  const [slotsLoaded, setSlotsLoaded] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [showPayment, setShowPayment] = useState(false);
  const [method, setMethod] = useState("UPI");
  const [paying, setPaying] = useState(false);
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const fetchSlots = () => {
    if (!selected) return;
    setSlotsLoaded(false);
    api
      .get(`/patient/physios/${selected.id}/slots`, { params: { date } })
      .then((res) => {
        setSlots(res.data);
        setSlotsLoaded(true);
      })
      .catch(() => setError("Could not load slots."));
  };

  const loadAppointments = () => {
    api
      .get("/patient/appointments")
      .then((res) => setAppointments(res.data.filter((a) => a.date >= today())))
      .catch(() => {});
  };

  useEffect(() => {
    api
      .get("/patient/physios")
      .then((res) => setPhysios(res.data))
      .catch(() => setError("Could not load physiotherapists. Please sign in again."));
    loadAppointments();
  }, []);

  useEffect(() => {
    setSelectedSlot(null);
    setShowPayment(false);
    fetchSlots();
  }, [selected, date]);

  const pay = async () => {
    setError("");
    setSuccess("");
    setPaying(true);
    await new Promise((r) => setTimeout(r, 1200)); // mock payment processing
    try {
      await api.post("/patient/book", { slotId: selectedSlot.id, paymentMethod: method });
      setSuccess(
        `Payment successful. Your appointment with ${selected.name} is confirmed for ${date} at ${selectedSlot.startTime}. A confirmation email has been sent.`
      );
      setSelectedSlot(null);
      setShowPayment(false);
      fetchSlots();
      loadAppointments();
    } catch (err) {
      setError(err.response?.data?.error || "Payment failed. Please try again.");
      fetchSlots();
    } finally {
      setPaying(false);
    }
  };

  const logout = () => {
    localStorage.clear();
    navigate("/");
  };

  return (
    <div className="container">
      <div className="topbar">
        <h2>Welcome, {localStorage.getItem("name")}</h2>
        <button className="secondary" onClick={logout}>Sign Out</button>
      </div>

      {success && <div className="success">{success}</div>}
      {error && <div className="error">{error}</div>}

      <h3>Choose a physiotherapist</h3>
      <div className="row">
        {physios.map((p) => (
          <div
            className="card"
            key={p.id}
            style={selected?.id === p.id ? { outline: "2px solid #0e7c86" } : {}}
          >
            <h3>{p.name}</h3>
            <div>{p.specialization}</div>
            <div style={{ color: "#5b727b", margin: "4px 0 12px" }}>{p.clinicAddress}</div>
            <div style={{ fontWeight: 600, marginBottom: 12 }}>₹{p.fees} per appointment</div>
            <button onClick={() => { setSuccess(""); setError(""); setSelected(p); }}>
              View slots
            </button>
          </div>
        ))}
      </div>
      {physios.length === 0 && !error && <p>No physiotherapists available yet.</p>}

      {selected && (
        <div className="card" style={{ marginTop: 20 }}>
          <h3>Available slots with {selected.name}</h3>

          <label>Date</label>
          <input
            type="date"
            value={date}
            min={today()}
            onChange={(e) => setDate(e.target.value)}
            style={{ maxWidth: 220 }}
          />

          {slotsLoaded && slots.length === 0 && (
            <p>No free slots on this date. Try another date.</p>
          )}

          <div className="slot-grid" style={{ maxWidth: 480 }}>
            {slots.map((s) => (
              <div
                key={s.id}
                className={`slot ${selectedSlot?.id === s.id ? "selected" : ""}`}
                style={{ cursor: "pointer" }}
                onClick={() => { setSelectedSlot(s); setShowPayment(false); }}
              >
                {s.startTime} - {s.endTime}
              </div>
            ))}
          </div>

          {selectedSlot && !showPayment && (
            <div style={{ marginTop: 16 }}>
              <p>
                Selected: <b>{selectedSlot.startTime} - {selectedSlot.endTime}</b> on {date}
              </p>
              <button onClick={() => setShowPayment(true)}>Continue to payment</button>
            </div>
          )}

          {selectedSlot && showPayment && (
            <div className="card" style={{ marginTop: 16, background: "#f3f7f8" }}>
              <h3>Payment</h3>
              <p>
                {selected.name}, {date}, {selectedSlot.startTime} - {selectedSlot.endTime}
              </p>
              <p style={{ fontWeight: 600 }}>Amount: ₹{selected.fees}</p>

              <label>Payment method</label>
              <select value={method} onChange={(e) => setMethod(e.target.value)} style={{ maxWidth: 220 }}>
                <option value="UPI">UPI</option>
                <option value="CARD">Card</option>
              </select>

              <p style={{ color: "#5b727b", fontSize: 14 }}>
                This is a demo payment. No money is charged.
              </p>

              <div style={{ display: "flex", gap: 12 }}>
                <button onClick={pay} disabled={paying}>
                  {paying ? "Processing payment..." : `Pay ₹${selected.fees}`}
                </button>
                <button className="secondary" onClick={() => setShowPayment(false)} disabled={paying}>
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      <h3 style={{ marginTop: 28 }}>Your upcoming appointments</h3>
      {appointments.length === 0 && <p>You have no upcoming appointments.</p>}
      {appointments.map((a) => (
        <div className="card" key={a.id}>
          <div style={{ fontWeight: 600 }}>
            {a.date}, {a.startTime} - {a.endTime}
          </div>
          <div>{a.physioName}</div>
          <div style={{ color: "#2e7d32" }}>
            {a.status} | Payment: {a.paymentStatus} (₹{a.amount})
          </div>
        </div>
      ))}
    </div>
  );
}