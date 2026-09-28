import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { loginSchema, type LoginInput } from "../auth/validation";
import { Rule, TermFrame } from "../components/chrome";
import { PromptField, TermButton, TextLink } from "../components/controls";

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
    <div className="mx-auto w-full max-w-xl px-5 pb-20 pt-14">
      <TermFrame path="~/login">
        <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
          <PromptField
            id="login-email"
            label="email"
            type="email"
            autoComplete="email"
            placeholder="voce@studys.dev"
            error={errors.email?.message}
            {...register("email")}
          />
          <PromptField
            id="login-senha"
            label="senha"
            type="password"
            autoComplete="current-password"
            placeholder="••••••••"
            error={errors.password?.message}
            {...register("password")}
          />

          {serverError && (
            <p className="text-[13px] text-zinc-200">
              {"! "}
              {serverError}
            </p>
          )}

          <div className="pt-2">
            <TermButton type="submit" loading={isSubmitting} loadingLabel="entrando…">
              entrar
            </TermButton>
          </div>
        </form>

        <div className="mt-6">
          <Rule />
          <p className="mt-4 text-[13px]">
            <TextLink to="/forgot">~/recover</TextLink>
            {"  ·  "}
            <TextLink to="/register">~/register</TextLink>
          </p>
        </div>
      </TermFrame>
    </div>
  );
}
