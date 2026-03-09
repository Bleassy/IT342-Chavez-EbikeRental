import React, { createContext, useContext, useState, useCallback, useEffect } from "react";

// ─── Types ────────────────────────────────────────────────────────────────────

export type UserRole = "USER" | "ADMIN";

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  phone?: string;
  address?: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}

export interface LoginData {
  email: string;
  password: string;
}

export interface RegisterData {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  confirmPassword: string;
  phone?: string;
  address?: string;
}

interface AuthContextType extends AuthState {
  login: (data: LoginData) => Promise<boolean>;
  register: (data: RegisterData) => Promise<boolean>;
  logout: () => void;
}

// ─── Context ──────────────────────────────────────────────────────────────────

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [authState, setAuthState] = useState<AuthState>(() => {
    const stored = localStorage.getItem("ebike_auth");
    if (stored) {
      try {
        return JSON.parse(stored);
      } catch {
        return { user: null, token: null, isAuthenticated: false };
      }
    }
    return { user: null, token: null, isAuthenticated: false };
  });

  // Persist to localStorage whenever auth state changes
  useEffect(() => {
    localStorage.setItem("ebike_auth", JSON.stringify(authState));
  }, [authState]);

  // ── Login ──────────────────────────────────────────────────────────────────
  const login = useCallback(async (data: LoginData): Promise<boolean> => {
    try {
      const response = await fetch(`${API_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (!response.ok) return false;

      const result = await response.json();
      const authData = result.data || result;

      const user: User = {
        id: authData.id?.toString() || `user-${Date.now()}`,
        username: authData.email.split("@")[0],
        email: authData.email,
        firstName: authData.firstName || "User",
        lastName: authData.lastName || "",
        role: (authData.role || "USER") as UserRole,
      };

      setAuthState({ user, token: authData.token, isAuthenticated: true });
      return true;
    } catch {
      return false;
    }
  }, []);

  // ── Register ───────────────────────────────────────────────────────────────
  const register = useCallback(async (data: RegisterData): Promise<boolean> => {
    try {
      if (data.password !== data.confirmPassword) return false;
      if (data.password.length < 6) return false;

      const response = await fetch(`${API_URL}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: data.email,
          password: data.password,
          firstName: data.firstName,
          lastName: data.lastName,
          phone: data.phone || "",
          address: data.address || "",
        }),
      });

      if (!response.ok) return false;

      const result = await response.json();
      const authData = result.data || result;

      const user: User = {
        id: authData.id?.toString() || `user-${Date.now()}`,
        username: data.email.split("@")[0],
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        role: "USER" as UserRole,
      };

      setAuthState({ user, token: authData.token, isAuthenticated: true });
      return true;
    } catch {
      return false;
    }
  }, []);

  // ── Logout ─────────────────────────────────────────────────────────────────
  const logout = useCallback(() => {
    setAuthState({ user: null, token: null, isAuthenticated: false });
    localStorage.removeItem("ebike_auth");
  }, []);

  return (
    <AuthContext.Provider value={{ ...authState, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
