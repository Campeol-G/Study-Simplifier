import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { registerSchema, type RegisterInput } from "../auth/validation";
import { Rule, TermFrame } from "../components/chrome";
import { PromptField, TermButton, TextLink } from "../components/controls";

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
    <div className="mx-auto w-full max-w-xl px-5 pb-20 pt-14">
      <TermFrame path="~/register">
        <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
          <PromptField
            id="reg-nome"
            label="nome"
            autoComplete="name"
            placeholder="Ada Lovelace"
            error={errors.name?.message}
            {...register("name")}
          />
          <PromptField
            id="reg-email"
            label="email"
            type="email"
            autoComplete="email"
            placeholder="voce@studys.dev"
            error={errors.email?.message}
            {...register("email")}
          />
          <PromptField
            id="reg-senha"
            label="senha"
            type="password"
            autoComplete="new-password"
            placeholder="mínimo 6 caracteres"
            error={errors.password?.message}
            {...register("password")}
          />
          <PromptField
            id="reg-conf"
            label="confirmar"
            type="password"
            autoComplete="new-password"
            placeholder="repita a senha"
            error={errors.confirm?.message}
            {...register("confirm")}
          />

          {serverError && (
            <p className="text-[13px] text-zinc-200">
              {"! "}
              {serverError}
            </p>
          )}

          <div className="pt-2">
            <TermButton type="submit" loading={isSubmitting} loadingLabel="criando…">
              criar conta
            </TermButton>
          </div>
        </form>

        <div className="mt-6">
          <Rule />
          <p className="mt-4 text-[13px] text-zinc-600">
            já tem conta? <TextLink to="/login">~/login</TextLink>
          </p>
        </div>
      </TermFrame>
    </div>
  );
}
