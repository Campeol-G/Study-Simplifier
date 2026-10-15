import { useLocation } from "react-router-dom";

/** path legível da rota atual, no vocabulário de pastas */
export function useRoutePath(): string {
  const { pathname } = useLocation();
  if (pathname === "/") return "~/home";
  if (pathname === "/app") return "~/app";
  return `~${pathname}`;
}
