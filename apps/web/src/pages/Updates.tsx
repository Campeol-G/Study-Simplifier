import { Rule } from "../components/chrome";

const ENTRIES = [
  {
    date: "2026-09-28",
    what: "fluxo de auth completo no front (login, register, recover)",
    note: "mock local em localStorage, sem backend conectado",
  },
  {
    date: "2026-09-26",
    what: "schema inicial com 5 entidades e migrations V1/V2",
    note: "users, plans, plan_nodes, content_cache, resources",
  },
  {
    date: "2026-09-25",
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
