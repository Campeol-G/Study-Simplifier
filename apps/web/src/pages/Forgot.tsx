import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuth } from "../auth/AuthContext";
import {
  forgotSchema,
  resetSchema,
  type ForgotInput,
  type ResetInput,
} from "../auth/validation";
import { Rule, TermFrame } from "../components/chrome";
import { PromptField, TermButton, TextLink } from "../components/controls";

export function ForgotPage() {
  const { requestReset, resetPassword } = useAuth();
  const [issuedToken, setIssuedToken] = useState<string | null>(null);
  const [done, setDone] = useState(false);
  const [serverError, setServerError] = useState("");

  const forgotForm = useForm<ForgotInput>({ resolver: zodResolver(forgotSchema) });
  const resetForm = useForm<ResetInput>({ resolver: zodResolver(resetSchema) });

  const onRequest = async (data: ForgotInput) => {
    setServerError("");
    try {
      const token = await requestReset(data.email);
      setIssuedToken(token);
      resetForm.setValue("token", token);
    } catch (e) {
      setServerError(e instanceof Error ? e.message : "falha ao solicitar reset");
    }
  };

  const onReset = async (data: ResetInput) => {
    setServerError("");
    try {
      await resetPassword(data.token, data.password);
      setDone(true);
    } catch (e) {
      setServerError(e instanceof Error ? e.message : "falha ao redefinir");
    }
  };

  return (
    <div className="mx-auto w-full max-w-xl px-5 pb-20 pt-14">
      <TermFrame path="~/recover">
        {done ? (
          <div>
            <p className="text-[14.5px] text-zinc-200">
              ok: senha redefinida. você já pode entrar.
            </p>
            <p className="mt-4 text-[13px]">
              <TextLink to="/login">~/login</TextLink>
            </p>
          </div>
        ) : (
          <div>
            <form
              onSubmit={forgotForm.handleSubmit(onRequest)}
              noValidate
              className="space-y-4"
            >
              <PromptField
                id="rec-email"
                label="email"
                type="email"
                placeholder="voce@studys.dev"
                error={forgotForm.formState.errors.email?.message}
                {...forgotForm.register("email")}
              />
              <div className="pt-2">
                <TermButton
                  type="submit"
                  loading={forgotForm.formState.isSubmitting}
                  loadingLabel="gerando…"
                >
                  solicitar token
                </TermButton>
              </div>
            </form>

            {issuedToken && (
              <div className="mt-8">
                <Rule />
                <p className="mt-4 text-[13px] text-zinc-500">
                  token mock, sem email real:{" "}
                  <span className="text-zinc-200">{issuedToken}</span>
                </p>
                <form
                  onSubmit={resetForm.handleSubmit(onReset)}
                  noValidate
                  className="mt-4 space-y-4"
                >
                  <PromptField
                    id="res-token"
                    label="token"
                    autoComplete="off"
                    placeholder="A1B2C3D4"
                    error={resetForm.formState.errors.token?.message}
                    {...resetForm.register("token")}
                  />
                  <PromptField
                    id="res-senha"
                    label="nova senha"
                    type="password"
                    autoComplete="new-password"
                    placeholder="mínimo 6 caracteres"
                    error={resetForm.formState.errors.password?.message}
                    {...resetForm.register("password")}
                  />
                  <PromptField
                    id="res-conf"
                    label="confirmar"
                    type="password"
                    autoComplete="new-password"
                    placeholder="repita a senha"
                    error={resetForm.formState.errors.confirm?.message}
                    {...resetForm.register("confirm")}
                  />
                  {serverError && (
                    <p className="text-[13px] text-zinc-200">
                      {"! "}
                      {serverError}
                    </p>
                  )}
                  <div className="pt-2">
                    <TermButton
                      type="submit"
                      loading={resetForm.formState.isSubmitting}
                      loadingLabel="redefinindo…"
                    >
                      redefinir senha
                    </TermButton>
                  </div>
                </form>
              </div>
            )}

            {serverError && !issuedToken && (
              <p className="mt-4 text-[13px] text-zinc-200">
                {"! "}
                {serverError}
              </p>
            )}

            <div className="mt-8">
              <Rule />
              <p className="mt-4 text-[13px]">
                <TextLink to="/login">← ~/login</TextLink>
              </p>
            </div>
          </div>
        )}
      </TermFrame>
    </div>
  );
}
