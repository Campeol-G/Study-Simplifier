import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  mockCurrentUser,
  mockLogin,
  mockLogout,
  mockRegister,
  mockRequestReset,
  mockResetPassword,
  type MockUser,
} from "./mockStore";

interface AuthContextValue {
  user: MockUser | null;
  ready: boolean;
  login: (email: string, password: string) => Promise<MockUser>;
  register: (name: string, email: string, password: string) => Promise<MockUser>;
  logout: () => Promise<void>;
  requestReset: (email: string) => Promise<string>;
  resetPassword: (token: string, password: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<MockUser | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    setUser(mockCurrentUser());
    setReady(true);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const u = await mockLogin({ email, password });
    setUser(u);
    return u;
  }, []);

  const register = useCallback(
    async (name: string, email: string, password: string) => {
      const u = await mockRegister({ name, email, password });
      setUser(u);
      return u;
    },
    []
  );

  const logout = useCallback(async () => {
    await mockLogout();
    setUser(null);
  }, []);

  const requestReset = useCallback(async (email: string) => {
    return mockRequestReset(email);
  }, []);

  const resetPassword = useCallback(async (token: string, password: string) => {
    await mockResetPassword({ token, password });
  }, []);

  const value = useMemo(
    () => ({ user, ready, login, register, logout, requestReset, resetPassword }),
    [user, ready, login, register, logout, requestReset, resetPassword]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth deve ser usado dentro de <AuthProvider>");
  return ctx;
}
