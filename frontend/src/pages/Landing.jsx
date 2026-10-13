import { useNavigate } from "react-router-dom";

const slots = [
  { time: "10:00", state: "booked" },
  { time: "10:30", state: "booked" },
  { time: "11:00", state: "open" },
  { time: "11:30", state: "selected" },
  { time: "12:00", state: "open" },
  { time: "12:30", state: "open" },
];

export default function Landing() {
  const navigate = useNavigate();

  return (
    <div className="landing">
      <header className="landing-nav">
        <div className="landing-brand">PhysioBook</div>
        <button className="nav-link" onClick={() => navigate("/login")}>
          Sign In
        </button>
      </header>

      <main className="landing-main">
        <section>
          <h1>See your physiotherapist this week, not next month.</h1>
          <p className="lead">
            Pick a physiotherapist, choose a free time slot, pay, and get your
            confirmation by email. Physiotherapists manage their own
            schedule and fees.
          </p>
          <div className="landing-actions">
            <button className="btn-primary" onClick={() => navigate("/signup")}>
              Sign Up
            </button>
            <button className="btn-outline" onClick={() => navigate("/login")}>
              Sign In
            </button>
          </div>
        </section>

        <section className="schedule" aria-label="Example schedule">
          <div className="schedule-head">
            <div className="schedule-avatar">MR</div>
            <div>
              <div className="schedule-name">Dr. Meera Rao</div>
              <div className="schedule-sub">Sports injury, Hyderabad</div>
            </div>
            <div className="schedule-fee">
              ₹500
              <div className="schedule-sub">per visit</div>
            </div>
          </div>

          <div className="schedule-label">Free slots today</div>
          <div className="slot-grid">
            {slots.map((s) => (
              <div key={s.time} className={`slot ${s.state === "open" ? "" : s.state}`}>
                {s.time}
              </div>
            ))}
          </div>

          <div className="schedule-foot">
            Example schedule. Booked slots are crossed out, so nobody gets
            double booked.
          </div>
        </section>
      </main>
    </div>
  );
}