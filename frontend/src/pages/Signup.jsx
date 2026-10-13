import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

export default function Signup() {
  const navigate = useNavigate();
  const [role, setRole] = useState("USER");
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    contactNumber: "",
    age: "",
    gender: "Male",
    qualification: "",
    specialization: "",
    clinicAddress: "",
    fees: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async () => {
    setError("");

    const payload = {
      role,
      name: form.name,
      email: form.email,
      password: form.password,
      contactNumber: form.contactNumber,
    };

    if (role === "USER") {
      payload.age = form.age ? Number(form.age) : null;
      payload.gender = form.gender;
    } else {
      payload.qualification = form.qualification;
      payload.specialization = form.specialization;
      payload.clinicAddress = form.clinicAddress;
      payload.fees = form.fees ? Number(form.fees) : null;
    }

    try {
      await api.post("/auth/signup", payload);
      setSuccess(true);
    } catch (err) {
      setError(
        err.response?.data?.error ||
          "Please fill all fields correctly (valid email, password min 6 characters)."
      );
    }
  };

  if (success) {
    return (
      <div className="center-box" style={{ textAlign: "center" }}>
        <h2>Signup Successful</h2>
        <p className="success">
          We have sent a verification link to <b>{form.email}</b>.
        </p>
        <p>Please verify your email, then sign in.</p>
        <button onClick={() => navigate("/login")}>Go to Sign In</button>
      </div>
    );
  }

  return (
    <div className="center-box">
      <h2>Sign Up</h2>

      <label>I am a</label>
      <select value={role} onChange={(e) => setRole(e.target.value)}>
        <option value="USER">User (Patient)</option>
        <option value="PHYSIO">Physiotherapist</option>
      </select>

      <label>Name</label>
      <input name="name" value={form.name} onChange={change} />

      <label>Email</label>
      <input name="email" type="email" value={form.email} onChange={change} />

      <label>Password</label>
      <input name="password" type="password" value={form.password} onChange={change} />

      <label>Contact Number</label>
      <input name="contactNumber" value={form.contactNumber} onChange={change} />

      {role === "USER" && (
        <>
          <label>Age</label>
          <input name="age" type="number" value={form.age} onChange={change} />

          <label>Gender</label>
          <select name="gender" value={form.gender} onChange={change}>
            <option>Male</option>
            <option>Female</option>
            <option>Other</option>
          </select>
        </>
      )}

      {role === "PHYSIO" && (
        <>
          <label>Qualification</label>
          <input name="qualification" value={form.qualification} onChange={change} />

          <label>Specialization</label>
          <input name="specialization" value={form.specialization} onChange={change} />

          <label>Clinic Address</label>
          <input name="clinicAddress" value={form.clinicAddress} onChange={change} />

          <label>Fees per Appointment</label>
          <input name="fees" type="number" value={form.fees} onChange={change} />
        </>
      )}

      {error && <div className="error">{error}</div>}

      <div style={{ display: "flex", gap: 12 }}>
        <button onClick={submit}>Sign Up</button>
        <button className="secondary" onClick={() => navigate("/")}>
          Back
        </button>
      </div>
    </div>
  );
}