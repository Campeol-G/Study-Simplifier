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
    <div className="mx-auto w-full max-w-2xl px-4 pb-16 pt-10 sm:pt-16">
      <TermFrame path="~/studys/app">
        <p className="text-zinc-100">$ studys session --info</p>
        <p className="text-zinc-600">backend: disconnected · dados mockados</p>

        <div className="mt-5">
          <Rule />
          <div className="mt-4 space-y-1 text-[13.5px]">
            <p>
              <span className="text-zinc-600">user  </span>
              <span className="text-zinc-200">{user?.name ?? "guest"}</span>
            </p>
            <p>
              <span className="text-zinc-600">email </span>
              <span className="text-zinc-200">{user?.email ?? "—"}</span>
            </p>
          </div>
        </div>

        <div className="mt-5">
          <p className="text-zinc-100">$ studys plan --list</p>
          <div className="mt-2 space-y-1 text-[13.5px]">
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

        <div className="mt-6">
          <TermButton
            type="button"
            onClick={onLogout}
          >
            sair
          </TermButton>
        </div>
      </TermFrame>
      <p className="mt-4 text-xs leading-relaxed text-zinc-600">
        {"// próximo passo real: trocar o provider mock por POST /auth/* da API Spring."}
      </p>
    </div>
  );
}
