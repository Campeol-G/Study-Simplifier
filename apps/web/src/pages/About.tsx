import { Rule } from "../components/chrome";

const STACK = [
  ["frontend", "React 19 · TypeScript · Vite · Tailwind"],
  ["backend", "Spring Boot 4 · Java 27 · SQLite"],
  ["módulos", "plan-engine · content-generation · onboarding-chat"],
];

const STATE = [
  ["db", "migrations V1, V2 e V3 — em ~/.studys/studys.db, um arquivo só"],
  ["api", "/plans e /plans/{id}/nodes/{nodeId} — sem autenticação, usuário único"],
  ["contrato", "schema v2 congelado: o formato que o desktop e o web trocam"],
  ["ia", "ainda não — nenhum cliente de provedor existe no repositório"],
  ["front", "protótipo com dados mockados; não chama a API"],
];

export function AboutPage() {
  return (
    <div className="mx-auto w-full max-w-2xl px-5 pb-20 pt-14">
      <p className="text-[13px] text-zinc-600">~/about</p>

      <h1 className="mt-8 text-2xl leading-tight text-zinc-100">o projeto</h1>
      <div className="mt-6 space-y-4 text-[15px] leading-relaxed text-zinc-400">
        <p>
          O StudyS é um simplificador de estudos. Você conta o que quer aprender e em
          quanto tempo, e ele transforma isso em um plano navegável.
        </p>
        <p>
          A ideia é tratar estudo como um grafo, não como uma lista: cada nó depende do
          anterior, carrega conteúdo e recursos, e o sistema registra tentativas para
          saber onde você precisa de reforço.
        </p>
      </div>

      <div className="mt-12">
        <Rule />
        <p className="mt-6 text-[13px] text-zinc-600">stack</p>
        <dl className="mt-3 grid grid-cols-1 gap-x-8 gap-y-1 text-[13.5px] sm:grid-cols-[7rem_1fr]">
          {STACK.map(([k, v]) => (
            <div key={k} className="contents">
              <dt className="text-zinc-600">{k}</dt>
              <dd className="text-zinc-400">{v}</dd>
            </div>
          ))}
        </dl>
      </div>

      <div className="mt-10">
        <p className="text-[13px] text-zinc-600">estado</p>
        <dl className="mt-3 grid grid-cols-1 gap-x-8 gap-y-1 text-[13.5px] sm:grid-cols-[7rem_1fr]">
          {STATE.map(([k, v]) => (
            <div key={k} className="contents">
              <dt className="text-zinc-600">{k}</dt>
              <dd className="text-zinc-400">{v}</dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  );
}
