import { Link } from "react-router-dom";
import { Rule } from "../components/chrome";

const INDEX = [
  { to: "/about", path: "~/about", desc: "o projeto, a stack e o estado atual" },
  { to: "/updates", path: "~/updates", desc: "o que mudou por último" },
];

const STATUS = [
  ["backend", "disconnected"],
  ["branch", "feat/frontend"],
];

export function LandingPage() {
  return (
    <div className="mx-auto w-full max-w-3xl px-5 pb-20 pt-14 sm:pt-20">
      <p className="text-[13px] text-zinc-600">~/home</p>

      {/* titulo — respiro grande, hierarquia por tamanho */}
      <h1 className="mt-8 text-3xl leading-tight text-zinc-100 sm:text-4xl">
        simplifique seus estudos
      </h1>

      <div className="mt-8 max-w-2xl space-y-4 text-[15px] leading-relaxed text-zinc-400">
        <p>
          Você aponta um assunto e o tempo que tem por dia. O StudyS devolve uma trilha:
          nós encadeados, cada um com seu conteúdo e seus recursos.
        </p>
        <p>
          Conforme você acerta ou erra, o plano se ajusta. Nada de lista solta — cada
          passo sabe onde vem e o que destrava.
        </p>
      </div>

      <div className="mt-12">
        <Rule />
        <p className="mt-6 text-[13px] text-zinc-600">índice</p>
        <ul className="mt-3 space-y-0.5">
          {INDEX.map((i) => (
            <li key={i.to}>
              <Link
                to={i.to}
                className="group flex flex-col gap-0.5 py-2 transition-colors duration-200 sm:flex-row sm:items-baseline sm:gap-4"
              >
                <span className="shrink-0 text-zinc-500 transition-colors duration-200 group-hover:text-zinc-200">
                  {i.path}
                </span>
                <span className="text-zinc-600 transition-colors duration-200 group-hover:text-zinc-400">
                  {i.desc}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </div>

      <div className="mt-12">
        <Rule />
        <dl className="mt-6 grid grid-cols-1 gap-x-8 gap-y-1 text-[13px] sm:grid-cols-[8rem_1fr]">
          {STATUS.map(([k, v]) => (
            <div key={k} className="contents">
              <dt className="text-zinc-600">{k}</dt>
              <dd className="text-zinc-500">{v}</dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  );
}
