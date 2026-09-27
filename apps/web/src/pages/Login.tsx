import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { loginSchema, type LoginInput } from "../auth/validation";
import { Rule, TermFrame } from "../components/chrome";
import { Box, BracketInput, TermButton, TextLink } from "../components/controls";
import { AuthLayout } from "./AuthLayout";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as { state?: { from?: string } };
  const [serverError, setServerError] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginInput>({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (data: LoginInput) => {
    setServerError("");
    try {
      await login(data.email, data.password);
      navigate(location.state?.from ?? "/app", { replace: true });
    } catch (e) {
      setServerError(e instanceof Error ? e.message : "falha no login");
    }
  };

  return (
    <AuthLayout>
      <TermFrame path="~/studys/login">
        <p className="text-zinc-100">$ studys auth login</p>
        <p className="text-zinc-600">sessão local · sem backend por enquanto</p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-6 space-y-5">
          <Box label="email" error={errors.email?.message}>
            <BracketInput
              type="email"
              autoComplete="email"
              placeholder="voce@studys.dev"
              {...register("email")}
            />
          </Box>

          <Box label="senha" error={errors.password?.message}>
            <BracketInput
              type="password"
              autoComplete="current-password"
              placeholder="••••••••"
              {...register("password")}
            />
          </Box>

          {serverError && (
            <p className="border border-zinc-700 px-3 py-2 text-[13px] text-zinc-100">
              {"! "}
              {serverError}
            </p>
          )}

          <TermButton type="submit" loading={isSubmitting} loadingLabel="entrando…">
            entrar
          </TermButton>
        </form>

        <div className="mt-6">
          <Rule />
          <p className="mt-3 text-[13px] text-zinc-600">
            <TextLink to="/forgot">esqueci a senha</TextLink>
            {"  ·  "}
            <TextLink to="/register">criar conta</TextLink>
          </p>
        </div>
      </TermFrame>
    </AuthLayout>
  );
}
