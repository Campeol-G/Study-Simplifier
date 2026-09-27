import { useRef, useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Rule, TermFrame } from "../components/chrome";

const HELP_LINES = [
  "  login      acessar sua conta",
  "  register   criar uma conta local",
  "  recover    redefinir sua senha",
  "  help       mostrar esta ajuda",
  "  clear      limpar a tela",
];

const INITIAL = [
  "STUDYS(1)                  StudyS Manual                  STUDYS(1)",
  "",
  "NAME",
  "    studys — simplifique seus estudos",
  "",
  "USAGE",
  "    studys <command>",
  "",
  "COMMANDS",
  ...HELP_LINES,
  "",
  "EXAMPLE",
  "    $ studys login",
  "",
  "digite um comando abaixo — ou use os atalhos.",
];

export function LandingPage() {
  const navigate = useNavigate();
  const [lines, setLines] = useState<string[]>(INITIAL);
  const [value, setValue] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const run = (raw: string) => {
    const cmd = raw.trim().toLowerCase();
    if (!cmd) return;
    if (cmd === "clear") {
      setLines([]);
      return;
    }
    if (cmd === "login") {
      navigate("/login");
      return;
    }
    if (cmd === "register") {
      navigate("/register");
      return;
    }
    if (cmd === "recover" || cmd === "forgot" || cmd === "reset") {
      navigate("/forgot");
      return;
    }
    if (cmd === "help" || cmd === "--help" || cmd === "-h") {
      setLines((h) => [...h, `$ ${raw.trim()}`, "", ...HELP_LINES, ""]);
      return;
    }
    setLines((h) => [...h, `$ ${raw.trim()}`, `command not found: ${cmd} — tente: help`, ""]);
  };

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    run(value);
    setValue("");
  };

  return (
    <div className="mx-auto w-full max-w-2xl px-4 pb-16 pt-10 sm:pt-16">
      <TermFrame path="~/studys">
        {/* saída — só texto, quebra preservada */}
        <div className="whitespace-pre-wrap break-words text-[13.5px] leading-relaxed">
          {lines.map((l, i) =>
            l.startsWith("$ ") ? (
              <p key={i} className="text-zinc-100">{l}</p>
            ) : l.startsWith("command not found") ? (
              <p key={i} className="text-zinc-200">{`! ${l}`}</p>
            ) : l === "NAME" || l === "USAGE" || l === "COMMANDS" || l === "EXAMPLE" ? (
              <p key={i} className="text-zinc-100">{l}</p>
            ) : (
              <p key={i} className="text-zinc-500">{l === "" ? " " : l}</p>
            )
          )}
        </div>

        <Rule />

        {/* atalhos clicáveis */}
        <p className="mb-4 text-[13px] text-zinc-500">
          atalhos:{" "}
          <Link to="/login" className="text-zinc-300 hover:text-white">[login]</Link>{" "}
          <Link to="/register" className="text-zinc-300 hover:text-white">[register]</Link>{" "}
          <Link to="/forgot" className="text-zinc-300 hover:text-white">[recover]</Link>
        </p>

        {/* prompt interativo */}
        <form onSubmit={onSubmit} onClick={() => inputRef.current?.focus()}>
          <div className="flex items-center gap-2 text-[14px]">
            <span className="shrink-0 text-zinc-500">~/studys $</span>
            <input
              ref={inputRef}
              value={value}
              onChange={(e) => setValue(e.target.value)}
              autoFocus
              autoComplete="off"
              spellCheck={false}
              aria-label="comando"
              placeholder="help"
              className="w-full bg-transparent text-zinc-100 placeholder:text-zinc-700 focus:outline-none"
            />
            {value === "" && <span className="cli-cursor shrink-0" />}
          </div>
        </form>
      </TermFrame>
      <p className="mt-4 text-xs leading-relaxed text-zinc-600">
        {"// auth 100% local por enquanto — nenhum dado sai do navegador."}
      </p>
    </div>
  );
}
