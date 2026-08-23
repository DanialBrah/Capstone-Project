import { createContext, useContext, useMemo, useState } from 'react';
import { loginRequest, registerRequest } from '../services/authApi.js';

const STORAGE_KEY = 'courseEnrolmentAuth';
const AuthContext = createContext(null);

function readStoredAuth() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  } catch {
    return null;
  }
}

function toAuthState(response) {
  return {
    token: response.token,
    tokenType: response.tokenType,
    expiresInMinutes: response.expiresInMinutes,
    user: {
      id: response.userId,
      name: response.name,
      email: response.email,
      role: response.role
    }
  };
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth);

  async function login(email, password) {
    const response = await loginRequest(email, password);
    const nextAuth = toAuthState(response);

    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextAuth));
    setAuth(nextAuth);
    return nextAuth;
  }

  async function register(name, email, password) {
    const response = await registerRequest(name, email, password);
    const nextAuth = toAuthState(response);

    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextAuth));
    setAuth(nextAuth);
    return nextAuth;
  }

  // JWTs are stateless - there's no server-side session to end, so
  // "logging out" just means forgetting the token on this device.
  function logout() {
    localStorage.removeItem(STORAGE_KEY);
    setAuth(null);
  }

  const value = useMemo(
    () => ({
      auth,
      token: auth?.token ?? '',
      user: auth?.user ?? null,
      isAuthenticated: Boolean(auth?.token),
      login,
      register,
      logout
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);

  if (!value) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return value;
}
