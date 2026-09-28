import {
  BrowserRouter,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { StatusLine, TopBar } from "./components/chrome";
import { AboutPage } from "./pages/About";
import { DashboardPage } from "./pages/Dashboard";
import { ForgotPage } from "./pages/Forgot";
import { LandingPage } from "./pages/Landing";
import { LoginPage } from "./pages/Login";
import { RegisterPage } from "./pages/Register";
import { UpdatesPage } from "./pages/Updates";

function ProtectedRoute() {
  const { user, ready } = useAuth();
  const location = useLocation();
  if (!ready) {
    return (
      <div className="mx-auto w-full max-w-xl px-5 pt-14 text-[13px] text-zinc-600">
        verificando sessão…
      </div>
    );
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <Outlet />;
}

function PublicOnly() {
  const { user, ready } = useAuth();
  if (!ready) return null;
  if (user) return <Navigate to="/app" replace />;
  return <Outlet />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="flex min-h-[100dvh] flex-col bg-[#09090b] font-mono text-zinc-300">
          <TopBar />
          <main className="flex-1">
            <Routes>
              <Route path="/" element={<LandingPage />} />
              <Route path="/about" element={<AboutPage />} />
              <Route path="/updates" element={<UpdatesPage />} />
              <Route element={<PublicOnly />}>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/forgot" element={<ForgotPage />} />
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/app" element={<DashboardPage />} />
              </Route>
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
          <StatusLine />
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
