import React, { createContext, useContext, useState, useCallback, useEffect, useRef } from "react";
import { User, AuthState, LoginData, RegisterData, UserRole } from "@/types";
import { fetchProfile } from "@/lib/api";

interface AuthContextType extends AuthState {
  login: (data: LoginData) => Promise<boolean>;
  register: (data: RegisterData) => Promise<boolean>;
  loginWithGoogle: (idToken: string) => Promise<boolean>;
  loginWithGoogleCode: (code: string) => Promise<boolean>;
  logout: () => void;
  updateUser: (updates: Partial<User>) => void;
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

  // Fetch full profile (including profilePictureUrl) whenever authenticated
  const profileFetched = useRef(false);
  useEffect(() => {
    if (authState.isAuthenticated && authState.token && !profileFetched.current) {
      profileFetched.current = true;
      fetchProfile()
        .then((p) => {
          setAuthState((prev) => {
            if (!prev.user) return prev;
            return {
              ...prev,
              user: {
                ...prev.user,
                firstName: p.firstName || prev.user.firstName,
                lastName: p.lastName || prev.user.lastName,
                phone: p.phone || undefined,
                address: p.address || undefined,
                nickname: p.nickname || undefined,
                profilePictureUrl: p.profilePictureUrl || undefined,
              },
            };
          });
        })
        .catch(() => { /* ignore - profile fetch failed */ });
    }
    if (!authState.isAuthenticated) {
      profileFetched.current = false;
    }
  }, [authState.isAuthenticated, authState.token]);

  const login = useCallback(async (data: LoginData): Promise<boolean> => {
    try {
      const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
      const response = await fetch(`${apiUrl}/api/auth/login`, {
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
      profileFetched.current = false; // trigger profile fetch
      return true;
    } catch {
      return false;
    }
  }, []);

  const register = useCallback(async (data: RegisterData): Promise<boolean> => {
    try {
      if (data.password !== data.confirmPassword) return false;
      if (data.password.length < 4) return false;

      const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
      const response = await fetch(`${apiUrl}/api/auth/register`, {
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

  const loginWithGoogle = useCallback(async (idToken: string): Promise<boolean> => {
    try {
      const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
      const response = await fetch(`${apiUrl}/api/auth/oauth2/google`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ idToken }),
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

  const loginWithGoogleCode = useCallback(async (code: string): Promise<boolean> => {
    try {
      const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
      const redirectUri = `${window.location.origin}/auth/google/callback`;
      const response = await fetch(`${apiUrl}/api/auth/oauth2/google`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code, redirectUri }),
      });

      if (!response.ok) return false;

      const result = await response.json();
      const data = result.data || result;
      const user: User = {
        id: data.id?.toString() || `user-${Date.now()}`,
        username: data.email.split("@")[0],
        email: data.email,
        firstName: data.firstName || "User",
        lastName: data.lastName || "",
        role: (data.role || "USER") as UserRole,
      };

      setAuthState({ user, token: data.token, isAuthenticated: true });
      return true;
    } catch {
      return false;
    }
  }, []);

  const logout = useCallback(() => {
    setAuthState({ user: null, token: null, isAuthenticated: false });
    localStorage.removeItem("ebike_auth");
  }, []);

  const updateUser = useCallback((updates: Partial<User>) => {
    setAuthState((prev) => {
      if (!prev.user) return prev;
      return { ...prev, user: { ...prev.user, ...updates } };
    });
  }, []);

  return (
    <AuthContext.Provider value={{ ...authState, login, register, loginWithGoogle, loginWithGoogleCode, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
