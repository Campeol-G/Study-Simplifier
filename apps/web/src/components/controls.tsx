import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode } from "react";
import { Link } from "react-router-dom";

export function Box({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-[13px] text-zinc-500">{label}</span>
      {children}
      {error && (
        <span className="mt-1.5 block text-[13px] text-zinc-200">
          {"! err: "}
          {error}
        </span>
      )}
    </label>
  );
}

/** caixa de preencher do terminal: [ .... ] */
export function BracketInput(props: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <span className="flex items-center border border-zinc-700 bg-black/40 transition-colors focus-within:border-zinc-400">
      <span className="select-none pl-2.5 text-zinc-600">[</span>
      <input
        {...props}
        className="w-full bg-transparent px-2 py-2 text-[14px] text-zinc-100 placeholder:text-zinc-700 focus:outline-none"
      />
      <span className="select-none pr-2.5 text-zinc-600">]</span>
    </span>
  );
}

interface TermButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  loading?: boolean;
  loadingLabel?: string;
}

/** botão como em dialog/whiptail: [ entrar ] — inverte no hover */
export function TermButton({
  children,
  loading = false,
  loadingLabel = "aguarde…",
  ...props
}: TermButtonProps) {
  return (
    <button
      {...props}
      disabled={loading || props.disabled}
      className="w-full border border-zinc-400 bg-transparent px-4 py-2 text-center text-[14px] text-zinc-100 transition-colors hover:bg-zinc-100 hover:text-black active:bg-zinc-300 disabled:cursor-wait disabled:opacity-60 disabled:hover:bg-transparent disabled:hover:text-zinc-100"
    >
      {loading ? `[ ${loadingLabel} ]` : `[ ${children} ]`}
    </button>
  );
}

export function TextLink({ to, children }: { to: string; children: ReactNode }) {
  return (
    <Link to={to} className="text-zinc-400 underline decoration-zinc-700 underline-offset-4 hover:text-zinc-100">
      {children}
    </Link>
  );
}
