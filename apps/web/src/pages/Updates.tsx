import { Rule } from "../components/chrome";

const ENTRIES = [
  {
    date: "2026-10-15",
    what: "contrato v2, nível por plano e contador de tempo por sessão",
    note: "SQLite em ~/.studys, migrations V1 a V3, e a autenticação removida do projeto inteiro",
  },
  {
    date: "2026-10-09",
    what: "contrato de dados compartilhado entre desktop e web",
    note: "schema do plano, prompts de IA e fixtures — congelado a partir daqui",
  },
  {
    date: "2026-09-26",
    what: "esqueleto multi-módulo",
    note: "apps, services, shared e infra",
  },
];

export function UpdatesPage() {
  return (
    <div className="mx-auto w-full max-w-2xl px-5 pb-20 pt-14">
      <p className="text-[13px] text-zinc-600">~/updates</p>

      <h1 className="mt-8 text-2xl leading-tight text-zinc-100">mudanças</h1>

      <div className="mt-10 space-y-8">
        {ENTRIES.map((e) => (
          <div key={e.date}>
            <Rule />
            <p className="mt-4 text-[13px] text-zinc-600">{e.date}</p>
            <p className="mt-1 text-[15px] leading-relaxed text-zinc-300">{e.what}</p>
            <p className="mt-1 text-[13.5px] leading-relaxed text-zinc-500">{e.note}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
