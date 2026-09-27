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
import { Box, BracketInput, TermButton, TextLink } from "../components/controls";
import { AuthLayout } from "./AuthLayout";

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
    <AuthLayout>
      <TermFrame path="~/studys/recover">
        {done ? (
          <div>
            <p className="text-zinc-100">$ studys auth reset --status</p>
            <p className="mt-3 bg-zinc-100 px-3 py-2 text-[13px] text-black">
              ok: senha redefinida. você já pode entrar.
            </p>
            <p className="mt-4 text-[13px]">
              <TextLink to="/login">$ studys auth login</TextLink>
            </p>
          </div>
        ) : (
          <div>
            <p className="text-zinc-100">$ studys auth reset --request</p>
            <p className="text-zinc-600">passo {issuedToken ? "2/2" : "1/2"}</p>

            <form
              onSubmit={forgotForm.handleSubmit(onRequest)}
              noValidate
              className="mt-6 space-y-5"
            >
              <Box label="email" error={forgotForm.formState.errors.email?.message}>
                <BracketInput
                  type="email"
                  placeholder="voce@studys.dev"
                  {...forgotForm.register("email")}
                />
              </Box>
              <TermButton
                type="submit"
                loading={forgotForm.formState.isSubmitting}
                loadingLabel="gerando…"
              >
                solicitar token
              </TermButton>
            </form>

            {issuedToken && (
              <div className="mt-6">
                <Rule />
                <p className="mt-4 text-[13px] text-zinc-400">
                  token (mock, sem email real):{" "}
                  <span className="text-zinc-100">{issuedToken}</span>
                </p>
                <form
                  onSubmit={resetForm.handleSubmit(onReset)}
                  noValidate
                  className="mt-4 space-y-5"
                >
                  <Box label="token" error={resetForm.formState.errors.token?.message}>
                    <BracketInput
                      autoComplete="off"
                      placeholder="A1B2C3D4"
                      {...resetForm.register("token")}
                    />
                  </Box>
                  <Box
                    label="nova senha"
                    error={resetForm.formState.errors.password?.message}
                  >
                    <BracketInput
                      type="password"
                      autoComplete="new-password"
                      placeholder="mínimo 6 caracteres"
                      {...resetForm.register("password")}
                    />
                  </Box>
                  <Box
                    label="confirmar senha"
                    error={resetForm.formState.errors.confirm?.message}
                  >
                    <BracketInput
                      type="password"
                      autoComplete="new-password"
                      placeholder="repita a senha"
                      {...resetForm.register("confirm")}
                    />
                  </Box>
                  <TermButton
                    type="submit"
                    loading={resetForm.formState.isSubmitting}
                    loadingLabel="redefinindo…"
                  >
                    redefinir senha
                  </TermButton>
                </form>
              </div>
            )}

            {serverError && (
              <p className="mt-4 border border-zinc-700 px-3 py-2 text-[13px] text-zinc-100">
                {"! "}
                {serverError}
              </p>
            )}

            <div className="mt-6">
              <Rule />
              <p className="mt-3 text-[13px] text-zinc-600">
                <TextLink to="/login">← voltar ao login</TextLink>
              </p>
            </div>
          </div>
        )}
      </TermFrame>
    </AuthLayout>
  );
}
