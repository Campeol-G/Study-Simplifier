import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { registerSchema, type RegisterInput } from "../auth/validation";
import { Rule, TermFrame } from "../components/chrome";
import { Box, BracketInput, TermButton, TextLink } from "../components/controls";
import { AuthLayout } from "./AuthLayout";

export function RegisterPage() {
  const { register: signUp } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterInput>({ resolver: zodResolver(registerSchema) });

  const onSubmit = async (data: RegisterInput) => {
    setServerError("");
    try {
      await signUp(data.name, data.email, data.password);
      navigate("/app", { replace: true });
    } catch (e) {
      setServerError(e instanceof Error ? e.message : "falha no cadastro");
    }
  };

  return (
    <AuthLayout>
      <TermFrame path="~/studys/register">
        <p className="text-zinc-100">$ studys auth create</p>
        <p className="text-zinc-600">registro local · plano free incluso</p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-6 space-y-5">
          <Box label="nome" error={errors.name?.message}>
            <BracketInput
              autoComplete="name"
              placeholder="Ada Lovelace"
              {...register("name")}
            />
          </Box>

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
              autoComplete="new-password"
              placeholder="mínimo 6 caracteres"
              {...register("password")}
            />
          </Box>

          <Box label="confirmar senha" error={errors.confirm?.message}>
            <BracketInput
              type="password"
              autoComplete="new-password"
              placeholder="repita a senha"
              {...register("confirm")}
            />
          </Box>

          {serverError && (
            <p className="border border-zinc-700 px-3 py-2 text-[13px] text-zinc-100">
              {"! "}
              {serverError}
            </p>
          )}

          <TermButton type="submit" loading={isSubmitting} loadingLabel="criando…">
            criar conta
          </TermButton>
        </form>

        <div className="mt-6">
          <Rule />
          <p className="mt-3 text-[13px] text-zinc-600">
            já tem conta? <TextLink to="/login">entrar</TextLink>
          </p>
        </div>
      </TermFrame>
    </AuthLayout>
  );
}
