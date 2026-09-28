import type { ReactNode } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useRoutePath } from "../lib/route";

/** abas como pastas: ~/home  ~/about  ~/updates */
const TABS = [
  { to: "/", path: "~/home", end: true },
  { to: "/about", path: "~/about", end: false },
  { to: "/updates", path: "~/updates", end: false },
];

export function TopBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = async () => {
    await logout();
    navigate("/", { replace: true });
  };

  return (
    <header className="border-b border-zinc-800/70">
      <div className="mx-auto flex max-w-3xl items-center justify-between gap-4 px-4 py-3 text-[13px]">
        <Link to="/" className="shrink-0 text-zinc-200 transition-colors hover:text-white">
          studys
        </Link>

        <nav className="flex min-w-0 items-center gap-4">
          {TABS.map((t) => (
            <NavLink
              key={t.to}
              to={t.to}
              end={t.end}
              className={({ isActive }) =>
                `transition-colors duration-200 ${
                  isActive ? "text-zinc-100" : "text-zinc-600 hover:text-zinc-300"
                }`
              }
            >
              {t.path}
            </NavLink>
          ))}
        </nav>

        <div className="flex shrink-0 items-center gap-4">
          {user ? (
            <>
              <span className="hidden max-w-32 truncate text-zinc-600 md:inline">
                {user.email}
              </span>
              <button
                type="button"
                onClick={onLogout}
                className="text-zinc-500 transition-colors hover:text-zinc-200"
              >
                ~/sair
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-zinc-500 transition-colors hover:text-zinc-200">
                ~/login
              </Link>
              <Link
                to="/register"
                className="text-zinc-500 transition-colors hover:text-zinc-200"
              >
                ~/register
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

/** divisória fina */
export function Rule() {
  return <div aria-hidden className="h-px bg-zinc-800/70" />;
}

/** moldura de terminal: usada nas telas de auth */
export function TermFrame({
  path,
  children,
}: {
  path: string;
  children: ReactNode;
}) {
  return (
    <div className="border border-zinc-800/80 bg-[#0c0c0f]">
      <div className="border-b border-zinc-800/70 px-5 py-2 text-xs text-zinc-600">
        {path}
      </div>
      <div className="px-5 py-6">{children}</div>
    </div>
  );
}

export function StatusLine() {
  const route = useRoutePath();
  return (
    <footer className="border-t border-zinc-800/70">
      <div className="mx-auto flex max-w-3xl items-center justify-between gap-4 px-4 py-2.5 text-xs text-zinc-600">
        <span>
          <span className="text-zinc-500">studys 0.1</span> {route}
        </span>
        <span>mock-local</span>
      </div>
    </footer>
  );
}
