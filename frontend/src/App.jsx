import { BrowserRouter, Routes, Route } from "react-router-dom";
import Landing from "./pages/Landing";
import Signup from "./pages/Signup";
import Login from "./pages/Login";
import Verify from "./pages/Verify";
import UserDashboard from "./pages/UserDashboard";
import PhysioDashboard from "./pages/PhysioDashboard";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/login" element={<Login />} />
        <Route path="/verify" element={<Verify />} />
        <Route path="/user" element={<UserDashboard />} />
        <Route path="/physio" element={<PhysioDashboard />} />
      </Routes>
    </BrowserRouter>
  );
}