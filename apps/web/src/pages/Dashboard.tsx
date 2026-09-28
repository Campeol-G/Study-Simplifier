import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { Rule, TermFrame } from "../components/chrome";
import { TermButton } from "../components/controls";

const FAKE_PLANS = [
  { id: "calculo-I", nodes: "04/12", status: "active" },
  { id: "fisica-II", nodes: "00/09", status: "queued" },
  { id: "ingles-b2", nodes: "11/15", status: "review" },
];

export function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = async () => {
    await logout();
    navigate("/", { replace: true });
  };

  return (
    <div className="mx-auto w-full max-w-xl px-5 pb-20 pt-14">
      <TermFrame path="~/app">
        <div className="space-y-1 text-[14.5px]">
          <p>
            <span className="text-zinc-600">user&nbsp;&nbsp;</span>
            <span className="text-zinc-200">{user?.name ?? "guest"}</span>
          </p>
          <p>
            <span className="text-zinc-600">email </span>
            <span className="text-zinc-200">{user?.email ?? "—"}</span>
          </p>
        </div>

        <div className="mt-8">
          <Rule />
          <p className="mt-4 text-[13px] text-zinc-600">planos — 03, mockados</p>
          <div className="mt-2 space-y-1 text-[14px]">
            {FAKE_PLANS.map((p, i) => (
              <p key={p.id} className="flex justify-between gap-4">
                <span className="truncate text-zinc-400">
                  <span className="text-zinc-600">[0{i + 1}] </span>
                  {p.id}
                </span>
                <span className="shrink-0 text-zinc-600">
                  {p.nodes} · {p.status}
                </span>
              </p>
            ))}
          </div>
        </div>

        <div className="mt-8">
          <TermButton type="button" onClick={onLogout}>
            sair
          </TermButton>
        </div>
      </TermFrame>
    </div>
  );
}
