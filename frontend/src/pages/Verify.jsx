import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import api from "../api";

export default function Verify() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [status, setStatus] = useState("loading"); // loading | success | error
  const [message, setMessage] = useState("");
  const called = useRef(false);

  useEffect(() => {
    if (called.current) return; // token ek-ch vela vaparta yeto
    called.current = true;

    const token = params.get("token");
    if (!token) {
      setStatus("error");
      setMessage("This verification link is incomplete.");
      return;
    }

    api
      .get("/auth/verify", { params: { token } })
      .then(() => setStatus("success"))
      .catch((err) => {
        setStatus("error");
        setMessage(err.response?.data?.error || "We could not verify your email.");
      });
  }, []);

  const circle = (bg, symbol) => (
    <div
      style={{
        width: 72,
        height: 72,
        borderRadius: "50%",
        background: bg,
        color: "#fff",
        fontSize: 38,
        fontWeight: 700,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        margin: "0 auto 20px",
      }}
    >
      {symbol}
    </div>
  );

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 20,
        background: "linear-gradient(160deg, #d5e9ea 0%, #f3f7f8 55%, #ffffff 100%)",
      }}
    >
      <div
        className="center-box"
        style={{ textAlign: "center", margin: 0, width: "100%", maxWidth: 440, padding: 36 }}
      >
        <div
          style={{
            fontFamily: '"Bricolage Grotesque", Arial, sans-serif',
            fontWeight: 700,
            fontSize: "1.3rem",
            marginBottom: 24,
            color: "#0f2a35",
          }}
        >
          PhysioBook
        </div>

        {status === "loading" && (
          <>
            {circle("#8aa0a7", "…")}
            <h2>Verifying your email</h2>
            <p>Please wait a moment.</p>
          </>
        )}

        {status === "success" && (
          <>
            {circle("#0e7c86", "✓")}
            <h2>Email verified</h2>
            <p style={{ color: "#3d5762", marginBottom: 24 }}>
              Your account is ready. You can sign in now.
            </p>
            <button style={{ width: "100%", padding: 14 }} onClick={() => navigate("/login")}>
              Sign In
            </button>
          </>
        )}

        {status === "error" && (
          <>
            {circle("#d32f2f", "!")}
            <h2>Verification failed</h2>
            <p style={{ color: "#3d5762", marginBottom: 24 }}>{message}</p>
            <div style={{ display: "flex", gap: 12, justifyContent: "center" }}>
              <button onClick={() => navigate("/signup")}>Sign Up again</button>
              <button className="secondary" onClick={() => navigate("/login")}>
                Sign In
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}