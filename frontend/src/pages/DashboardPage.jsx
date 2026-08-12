import { useNavigate } from 'react-router';
import { useAuth } from '../context/AuthContext.jsx';

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <main style={{ padding: 32, textAlign: 'left' }}>
      <h1>Welcome, {user?.name}</h1>
      <p>
        Logged in as {user?.email} ({user?.role})
      </p>
      <button type="button" onClick={handleLogout}>
        Log out
      </button>
    </main>
  );
}
