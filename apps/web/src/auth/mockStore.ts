// Mock auth store — 100% front-end, sem backend.
// Trocar por chamadas HTTP quando a API Spring implementar /auth/*.
// Senha é apenas ofuscada (NÃO é segurança real).

export interface MockUser {
  id: string;
  name: string;
  email: string;
  passHash: string;
}

const USERS_KEY = "studys_users";
const SESSION_KEY = "studys_session";
const RESET_KEY = "studys_reset_tokens";
const LATENCY_MS = 550;

const wait = (ms = LATENCY_MS) =>
  new Promise((r) => setTimeout(r, ms));

function read<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

function write(key: string, value: unknown) {
  localStorage.setItem(key, JSON.stringify(value));
}

// ofuscação reversível apenas para o mock funcionar visualmente
function fakeHash(pw: string): string {
  return btoa(`studys$${pw}`).split("").reverse().join("");
}

function fakeCompare(pw: string, hash: string): boolean {
  return fakeHash(pw) === hash;
}

function uid(): string {
  return (
    globalThis.crypto?.randomUUID?.() ??
    `id-${Date.now()}-${Math.floor(Math.random() * 1e6)}`
  );
}

export function mockListUsers(): MockUser[] {
  return read<MockUser[]>(USERS_KEY, []);
}

export function mockCurrentUser(): MockUser | null {
  const session = read<{ userId: string } | null>(SESSION_KEY, null);
  if (!session) return null;
  return mockListUsers().find((u) => u.id === session.userId) ?? null;
}

export async function mockRegister(input: {
  name: string;
  email: string;
  password: string;
}): Promise<MockUser> {
  await wait();
  const email = input.email.trim().toLowerCase();
  const users = mockListUsers();
  if (users.some((u) => u.email === email)) {
    throw new Error("ERR_USER_EXISTS :: este email já está registrado");
  }
  const user: MockUser = {
    id: uid(),
    name: input.name.trim(),
    email,
    passHash: fakeHash(input.password),
  };
  users.push(user);
  write(USERS_KEY, users);
  write(SESSION_KEY, { userId: user.id });
  return user;
}

export async function mockLogin(input: {
  email: string;
  password: string;
}): Promise<MockUser> {
  await wait();
  const email = input.email.trim().toLowerCase();
  const users = mockListUsers();
  const user = users.find((u) => u.email === email);
  if (!user || !fakeCompare(input.password, user.passHash)) {
    throw new Error("ERR_BAD_CREDENTIALS :: email ou senha inválidos");
  }
  write(SESSION_KEY, { userId: user.id });
  return user;
}

export async function mockLogout(): Promise<void> {
  await wait(200);
  localStorage.removeItem(SESSION_KEY);
}

export async function mockRequestReset(email: string): Promise<string> {
  await wait();
  const normalized = email.trim().toLowerCase();
  const users = mockListUsers();
  // resposta genérica por segurança, mas no mock retornamos o token
  // para o fluxo funcionar sem email real
  const user = users.find((u) => u.email === normalized);
  const tokens = read<Record<string, string>>(RESET_KEY, {});
  const token = Math.random().toString(36).slice(2, 10).toUpperCase();
  if (user) {
    tokens[user.email] = token;
    write(RESET_KEY, tokens);
    return token;
  }
  // ainda simula sucesso para não vazar existência
  return token;
}

export async function mockResetPassword(input: {
  token: string;
  password: string;
}): Promise<void> {
  await wait();
  const tokens = read<Record<string, string>>(RESET_KEY, {});
  const entry = Object.entries(tokens).find(
    ([, t]) => t.toLowerCase() === input.token.trim().toLowerCase()
  );
  if (!entry) throw new Error("ERR_BAD_TOKEN :: token inválido ou expirado");
  const [email] = entry;
  const users = mockListUsers().map((u) =>
    u.email === email ? { ...u, passHash: fakeHash(input.password) } : u
  );
  write(USERS_KEY, users);
  delete tokens[email];
  write(RESET_KEY, tokens);
}
