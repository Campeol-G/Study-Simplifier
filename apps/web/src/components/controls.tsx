import type { InputHTMLAttributes, ReactNode } from "react";
import { Link } from "react-router-dom";

/**
 * Campo no estilo instalador: pergunta à esquerda, resposta na mesma linha.
 * A resposta aparece onde você digita (inclusive a senha, mascarada).
 */
export function PromptField({
  label,
  error,
  id,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string }) {
  return (
    <div className="grid grid-cols-[6.5rem_1.25rem_1fr] items-baseline gap-x-1">
      <label htmlFor={id} className="text-[13.5px] text-zinc-500">
        ? {label}
      </label>
      <span aria-hidden className="text-zinc-600">
        ▸
      </span>
      <input
        {...props}
        id={id}
        className="min-w-0 bg-transparent text-[14.5px] text-zinc-100 placeholder:text-zinc-700 focus:outline-none"
      />
      {error && (
        <p className="col-start-2 col-end-4 text-[13px] text-zinc-200">
          {"! "}
          {error}
        </p>
      )}
    </div>
  );
}

/** ação principal: borda fina, texto puro, sem colchetes */
export function TermButton({
  children,
  loading = false,
  loadingLabel = "aguarde…",
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & {
  loading?: boolean;
  loadingLabel?: string;
}) {
  return (
    <button
      {...props}
      disabled={loading || props.disabled}
      className="w-full border border-zinc-800 py-2.5 text-[14px] text-zinc-200 transition-colors duration-200 hover:border-zinc-500 hover:text-zinc-50 active:bg-zinc-900 disabled:cursor-wait disabled:opacity-50"
    >
      {loading ? loadingLabel : children}
    </button>
  );
}

export function TextLink({ to, children }: { to: string; children: ReactNode }) {
  return (
    <Link
      to={to}
      className="text-zinc-500 underline decoration-zinc-800 underline-offset-4 transition-colors duration-200 hover:text-zinc-200 hover:decoration-zinc-600"
    >
      {children}
    </Link>
  );
}
