import React, { createContext, useContext, useState, useEffect } from 'react';
import { AuthResponse, Role } from '../types';
import api, { setAccessToken } from '../api/client';

interface AuthContextType {
  user: {
    userId: number;
    email: string;
    fullName: string;
    role: Role;
    customerId?: number;
    technicianId?: number;
  } | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (authData: AuthResponse) => void;
  logout: () => void;
  updateUser: (updatedFields: Partial<NonNullable<AuthContextType['user']>>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthContextType['user']>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Clear legacy localStorage token artifacts for backward compatibility & migration
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('authUser');

    // Perform silent refresh using HttpOnly cookie to rehydrate session on page load/reopen
    api
      .post('/auth/refresh', {})
      .then((res) => {
        if (res.data?.success && res.data?.data) {
          const authData: AuthResponse = res.data.data;
          setAccessToken(authData.accessToken);
          setUser({
            userId: authData.userId,
            email: authData.email,
            fullName: authData.fullName,
            role: authData.role,
            customerId: authData.customerId,
            technicianId: authData.technicianId,
          });
        } else {
          setAccessToken(null);
          setUser(null);
        }
      })
      .catch(() => {
        setAccessToken(null);
        setUser(null);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  const login = (authData: AuthResponse) => {
    const userData = {
      userId: authData.userId,
      email: authData.email,
      fullName: authData.fullName,
      role: authData.role,
      customerId: authData.customerId,
      technicianId: authData.technicianId,
    };
    setAccessToken(authData.accessToken);
    setUser(userData);
  };

  const updateUser = (updatedFields: Partial<NonNullable<AuthContextType['user']>>) => {
    setUser((prev) => {
      if (!prev) return null;
      return { ...prev, ...updatedFields };
    });
  };

  const logout = () => {
    api.post('/auth/logout').catch(() => {});
    setAccessToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
