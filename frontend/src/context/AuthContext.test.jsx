import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from './AuthContext.jsx';
import { loginRequest, registerRequest } from '../services/authApi.js';

vi.mock('../services/authApi.js', () => ({
  loginRequest: vi.fn(),
  registerRequest: vi.fn()
}));

const STORAGE_KEY = 'courseEnrolmentAuth';

const SAMPLE_RESPONSE = {
  token: 'jwt-token',
  tokenType: 'Bearer',
  expiresInMinutes: 60,
  userId: 'user-1',
  name: 'Ada Lovelace',
  email: 'ada@example.com',
  role: 'STUDENT'
};

function TestConsumer() {
  const { user, isAuthenticated, login, register, logout } = useAuth();

  return (
    <div>
      <span data-testid="authenticated">{String(isAuthenticated)}</span>
      <span data-testid="user-name">{user?.name ?? 'none'}</span>
      <button onClick={() => login('ada@example.com', 'password123')}>Log in</button>
      <button onClick={() => register('Ada', 'ada@example.com', 'password123')}>Register</button>
      <button onClick={logout}>Log out</button>
    </div>
  );
}

function renderWithProvider() {
  return render(
    <AuthProvider>
      <TestConsumer />
    </AuthProvider>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('starts unauthenticated when localStorage has no stored auth', () => {
    renderWithProvider();

    expect(screen.getByTestId('authenticated').textContent).toBe('false');
    expect(screen.getByTestId('user-name').textContent).toBe('none');
  });

  it('initializes from a previously stored auth payload', () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ token: 'stored-token', user: { name: 'Stored User' } }));

    renderWithProvider();

    expect(screen.getByTestId('authenticated').textContent).toBe('true');
    expect(screen.getByTestId('user-name').textContent).toBe('Stored User');
  });

  it('login() updates state and persists to localStorage', async () => {
    loginRequest.mockResolvedValue(SAMPLE_RESPONSE);
    renderWithProvider();

    fireEvent.click(screen.getByText('Log in'));

    expect(await screen.findByTestId('user-name')).toHaveTextContent('Ada Lovelace');
    expect(screen.getByTestId('authenticated').textContent).toBe('true');
    expect(loginRequest).toHaveBeenCalledWith('ada@example.com', 'password123');

    const stored = JSON.parse(localStorage.getItem(STORAGE_KEY));
    expect(stored.token).toBe('jwt-token');
    expect(stored.user.role).toBe('STUDENT');
  });

  it('register() updates state and persists to localStorage', async () => {
    registerRequest.mockResolvedValue(SAMPLE_RESPONSE);
    renderWithProvider();

    fireEvent.click(screen.getByText('Register'));

    expect(await screen.findByTestId('user-name')).toHaveTextContent('Ada Lovelace');
    expect(registerRequest).toHaveBeenCalledWith('Ada', 'ada@example.com', 'password123');
  });

  it('logout() clears state and localStorage', async () => {
    loginRequest.mockResolvedValue(SAMPLE_RESPONSE);
    renderWithProvider();

    fireEvent.click(screen.getByText('Log in'));
    await screen.findByTestId('user-name');

    fireEvent.click(screen.getByText('Log out'));

    expect(screen.getByTestId('authenticated').textContent).toBe('false');
    expect(screen.getByTestId('user-name').textContent).toBe('none');
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });
});
