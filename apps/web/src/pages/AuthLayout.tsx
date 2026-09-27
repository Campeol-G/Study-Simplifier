import type { ReactNode } from "react";

/** páginas de auth: só centraliza o terminal na tela */
export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="mx-auto w-full max-w-xl px-4 pb-16 pt-10 sm:pt-16">
      {children}
    </div>
  );
}
