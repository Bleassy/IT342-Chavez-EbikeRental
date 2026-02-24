import React, { createContext, useContext, useState, useCallback, useEffect } from "react";
import { User, AuthState, LoginData, RegisterData, UserRole } from "@/types";
import { mockUsers } from "@/data/mockData";

interface AuthContextType extends AuthState {
  login: (data: LoginData) => Promise<boolean>;
  register: (data: RegisterData) => Promise<boolean>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

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

  useEffect(() => {
    localStorage.setItem("ebike_auth", JSON.stringify(authState));
  }, [authState]);

  const login = useCallback(async (data: LoginData): Promise<boolean> => {
    // Simulate API call
    const found = mockUsers.find((u) => u.email === data.email);
    if (found && data.password.length >= 4) {
      setAuthState({ user: found, token: "mock-jwt-token", isAuthenticated: true });
      return true;
    }
    // Auto-create user for demo
    if (data.password.length >= 4) {
      const newUser: User = {
        id: `user-${Date.now()}`,
        username: data.email.split("@")[0],
        email: data.email,
        firstName: "Demo",
        lastName: "User",
        role: "USER" as UserRole,
      };
      setAuthState({ user: newUser, token: "mock-jwt-token", isAuthenticated: true });
      return true;
    }
    return false;
  }, []);

  const register = useCallback(async (data: RegisterData): Promise<boolean> => {
    if (data.password !== data.confirmPassword) return false;
    if (data.password.length < 4) return false;

    const newUser: User = {
      id: `user-${Date.now()}`,
      username: data.username,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      role: "USER" as UserRole,
    };
    setAuthState({ user: newUser, token: "mock-jwt-token", isAuthenticated: true });
    return true;
  }, []);

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
