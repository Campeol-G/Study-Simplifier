import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { StatusLine, TopBar } from "./components/chrome";
import { AboutPage } from "./pages/About";
import { DashboardPage } from "./pages/Dashboard";
import { LandingPage } from "./pages/Landing";
import { UpdatesPage } from "./pages/Updates";

export default function App() {
  return (
    <BrowserRouter>
      <div className="flex min-h-[100dvh] flex-col bg-[#09090b] font-mono text-zinc-300">
        <TopBar />
        <main className="flex-1">
          <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/updates" element={<UpdatesPage />} />
            <Route path="/app" element={<DashboardPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
        <StatusLine />
      </div>
    </BrowserRouter>
  );
}