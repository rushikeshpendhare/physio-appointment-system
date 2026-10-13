import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

const today = () => new Date().toLocaleDateString("en-CA"); // YYYY-MM-DD

export default function PhysioDashboard() {
  const navigate = useNavigate();

  // Daily appointments
  const [viewDate, setViewDate] = useState(today());
  const [appointments, setAppointments] = useState([]);
  const [orderChanged, setOrderChanged] = useState(false);

  // Schedule setup
  const [slotDate, setSlotDate] = useState(today());
  const [startTime, setStartTime] = useState("10:00");
  const [endTime, setEndTime] = useState("13:00");
  const [duration, setDuration] = useState(30);

  // Day off + fees
  const [offDate, setOffDate] = useState(today());
  const [fees, setFees] = useState("");

  // Notification (screen var fixed disto)
  const [toast, setToast] = useState(null); // { type: "success" | "error", text }

  const notify = (text) => setToast({ type: "success", text });
  const fail = (err, fallback) =>
    setToast({
      type: "error",
      text:
        err.response?.data?.error ||
        (err.response?.status === 401
          ? "Session expired. Please sign in again."
          : err.response?.status === 403
          ? "You are not allowed to do this."
          : fallback),
    });

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 5000);
    return () => clearTimeout(t);
  }, [toast]);

  const loadAppointments = () => {
    api
      .get("/physio/appointments", { params: { date: viewDate } })
      .then((res) => {
        setAppointments(res.data);
        setOrderChanged(false);
      })
      .catch((err) => fail(err, "Could not load appointments."));
  };

  useEffect(() => {
    loadAppointments();
  }, [viewDate]);

  const move = (index, dir) => {
    const next = [...appointments];
    const target = index + dir;
    if (target < 0 || target >= next.length) return;
    [next[index], next[target]] = [next[target], next[index]];
    setAppointments(next);
    setOrderChanged(true);
  };

  const saveOrder = async () => {
    try {
      await api.put("/physio/appointments/reorder", {
        date: viewDate,
        appointmentIds: appointments.map((a) => a.id),
      });
      notify("Appointment order saved.");
      loadAppointments();
    } catch (err) {
      fail(err, "Could not save the order.");
    }
  };

  const saveSchedule = async () => {
    try {
      const res = await api.post("/physio/slots", {
        date: slotDate,
        startTime,
        endTime,
        durationMinutes: Number(duration),
      });
      notify(`Schedule saved: ${res.data.length} new slots on ${slotDate}. Patients can book them now.`);
    } catch (err) {
      fail(err, "Could not save the schedule.");
    }
  };

  const markDayOff = async () => {
    try {
      const res = await api.post("/physio/day-off", { date: offDate });
      notify(`${offDate} marked as day off. ${res.data.slotsClosed} open slots closed.`);
    } catch (err) {
      fail(err, "Could not mark the day off.");
    }
  };

  const updateFees = async () => {
    try {
      await api.put("/physio/fees", { fees: Number(fees) });
      notify(`Fees updated to ₹${fees} per appointment.`);
      setFees("");
    } catch (err) {
      fail(err, "Could not update fees.");
    }
  };

  const logout = () => {
    localStorage.clear();
    navigate("/");
  };

  return (
    <div className="container">
      {toast && (
        <div
          style={{
            position: "fixed",
            top: 16,
            left: "50%",
            transform: "translateX(-50%)",
            zIndex: 1000,
            maxWidth: 560,
            width: "calc(100% - 32px)",
            padding: "14px 18px",
            borderRadius: 10,
            color: "#fff",
            fontWeight: 600,
            background: toast.type === "success" ? "#2e7d32" : "#d32f2f",
            boxShadow: "0 4px 16px rgba(0,0,0,0.2)",
          }}
        >
          {toast.text}
        </div>
      )}

      <div className="topbar">
        <h2>Welcome, {localStorage.getItem("name")}</h2>
        <button className="secondary" onClick={logout}>Sign Out</button>
      </div>

      <div className="card">
        <h3>Appointments</h3>
        <label>Date</label>
        <input
          type="date"
          value={viewDate}
          onChange={(e) => setViewDate(e.target.value)}
          style={{ maxWidth: 220 }}
        />

        {appointments.length === 0 && <p>No appointments booked on this date.</p>}

        {appointments.map((a, i) => (
          <div
            key={a.id}
            style={{
              display: "flex",
              alignItems: "center",
              gap: 12,
              padding: "10px 0",
              borderTop: "1px solid #e1eaec",
            }}
          >
            <div style={{ fontWeight: 600, width: 28 }}>{i + 1}</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 600 }}>
                {a.startTime} - {a.endTime}, {a.patientName}
              </div>
              <div style={{ color: "#5b727b", fontSize: 14 }}>
                Contact: {a.patientContact} | {a.status}
              </div>
            </div>
            <button className="secondary" onClick={() => move(i, -1)} disabled={i === 0}>
              Move up
            </button>
            <button
              className="secondary"
              onClick={() => move(i, 1)}
              disabled={i === appointments.length - 1}
            >
              Move down
            </button>
          </div>
        ))}

        {orderChanged && (
          <div style={{ marginTop: 12 }}>
            <button onClick={saveOrder}>Save order</button>
          </div>
        )}
      </div>

      <div className="card">
        <h3>Set up daily schedule</h3>
        <div className="row" style={{ alignItems: "flex-end" }}>
          <div>
            <label>Date</label>
            <input type="date" value={slotDate} min={today()} onChange={(e) => setSlotDate(e.target.value)} />
          </div>
          <div>
            <label>Start time</label>
            <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
          </div>
          <div>
            <label>End time</label>
            <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} />
          </div>
          <div>
            <label>Minutes per slot</label>
            <input type="number" min="10" step="5" value={duration} onChange={(e) => setDuration(e.target.value)} />
          </div>
        </div>
        <p style={{ color: "#5b727b", fontSize: 14 }}>
          Saving again for the same date replaces open slots. Booked slots stay as they are.
        </p>
        <button onClick={saveSchedule}>Save schedule</button>
      </div>

      <div className="row">
        <div className="card">
          <h3>Mark a day off</h3>
          <label>Date</label>
          <input type="date" value={offDate} min={today()} onChange={(e) => setOffDate(e.target.value)} />
          <button className="danger" onClick={markDayOff}>Mark day off</button>
        </div>

        <div className="card">
          <h3>Update fees</h3>
          <label>Fees per appointment (₹)</label>
          <input type="number" min="1" value={fees} onChange={(e) => setFees(e.target.value)} />
          <button onClick={updateFees} disabled={!fees}>Update fees</button>
        </div>
      </div>
    </div>
  );
}