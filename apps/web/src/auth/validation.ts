import { z } from "zod";

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, "informe o email")
    .email("email inválido — ex: voce@studys.dev"),
  password: z.string().min(6, "mínimo de 6 caracteres"),
});

export const registerSchema = z
  .object({
    name: z.string().min(2, "informe seu nome").max(120),
    email: z.string().min(1, "informe o email").email("email inválido"),
    password: z.string().min(6, "mínimo de 6 caracteres").max(72),
    confirm: z.string().min(1, "confirme a senha"),
  })
  .refine((d) => d.password === d.confirm, {
    message: "as senhas não conferem",
    path: ["confirm"],
  });

export const forgotSchema = z.object({
  email: z.string().min(1, "informe o email").email("email inválido"),
});

export const resetSchema = z
  .object({
    token: z.string().min(6, "token inválido"),
    password: z.string().min(6, "mínimo de 6 caracteres").max(72),
    confirm: z.string().min(1, "confirme a senha"),
  })
  .refine((d) => d.password === d.confirm, {
    message: "as senhas não conferem",
    path: ["confirm"],
  });

export type LoginInput = z.infer<typeof loginSchema>;
export type RegisterInput = z.infer<typeof registerSchema>;
export type ForgotInput = z.infer<typeof forgotSchema>;
export type ResetInput = z.infer<typeof resetSchema>;
