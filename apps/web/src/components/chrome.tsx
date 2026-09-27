import { Link, useNavigate } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "../auth/AuthContext";

/** barra superior: uma linha de texto, sem pill, sem blur */
export function TopBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = async () => {
    await logout();
    navigate("/", { replace: true });
  };

  return (
    <header className="border-b border-zinc-800/80">
      <div className="mx-auto flex max-w-2xl items-center justify-between gap-4 px-4 py-3 text-[13px]">
        <Link to="/" className="text-zinc-200 hover:text-white">
          studys<span className="text-zinc-600">@web:~$</span>
          <span className="cli-cursor ml-2" />
        </Link>
        <nav className="flex items-center gap-4 text-zinc-500">
          {user ? (
            <>
              <span className="hidden max-w-40 truncate sm:inline">{user.email}</span>
              <button
                type="button"
                onClick={onLogout}
                className="hover:text-zinc-100"
              >
                [sair]
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="hover:text-zinc-100">[login]</Link>
              <Link to="/register" className="hover:text-zinc-100">[register]</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

/** a janela de terminal — é praticamente a UI inteira */
export function TermFrame({
  path = "~/studys",
  children,
}: {
  path?: string;
  children: ReactNode;
}) {
  return (
    <div className="border border-zinc-800 bg-[#0c0c0f]">
      <div className="flex items-center justify-between border-b border-zinc-800/70 px-4 py-2 text-xs text-zinc-600">
        <span className="truncate">┌─ {path}</span>
        <span className="shrink-0">utf-8</span>
      </div>
      <div className="px-4 py-5 sm:px-6 sm:py-6">{children}</div>
    </div>
  );
}

/** statusline estilo tmux — estática, só contexto */
export function StatusLine({ route }: { route: string }) {
  return (
    <footer className="border-t border-zinc-800/80">
      <div className="mx-auto flex max-w-2xl items-center justify-between gap-4 px-4 py-2.5 text-xs text-zinc-600">
        <span>
          <span className="text-zinc-400">[studys 0.1]</span> {route}
        </span>
        <span>feat/frontend* · mock-local</span>
      </div>
    </footer>
  );
}

/** divisória de texto, não decoração */
export function Rule() {
  return (
    <div aria-hidden className="select-none overflow-hidden whitespace-nowrap text-zinc-800">
      {"─".repeat(72)}
    </div>
  );
}
