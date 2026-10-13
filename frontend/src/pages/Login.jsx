import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const submit = async () => {
    setError("");
    try {
      const res = await api.post("/auth/login", { email, password });
      const { token, role, name, userId } = res.data;

      localStorage.setItem("token", token);
      localStorage.setItem("role", role);
      localStorage.setItem("name", name);
      localStorage.setItem("userId", userId);

      navigate(role === "PHYSIO" ? "/physio" : "/user");
    } catch (err) {
      setError(
        err.response?.data?.error || "Could not sign in. Check your email and password."
      );
    }
  };

  return (
    <div className="center-box">
      <h2>Sign In</h2>

      <label>Email</label>
      <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />

      <label>Password</label>
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && submit()}
      />

      {error && <div className="error">{error}</div>}

      <div style={{ display: "flex", gap: 12 }}>
        <button onClick={submit}>Sign In</button>
        <button className="secondary" onClick={() => navigate("/")}>
          Back
        </button>
      </div>

      <p style={{ marginTop: 18 }}>
        New here?{" "}
        <a href="/signup" style={{ color: "#0e7c86" }}>
          Create an account
        </a>
      </p>
    </div>
  );
}