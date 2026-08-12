import { Navigate } from 'react-router';
import { useAuth } from '../context/AuthContext.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';

// Wraps ProtectedRoute with a role check. A logged-in student hitting an
// admin URL is sent to /courses rather than /login - they don't need to
// log in again, they just don't have access to this page.
export default function AdminRoute({ children }) {
  const { user } = useAuth();

  return (
    <ProtectedRoute>
      {user?.role === 'ADMIN' ? children : <Navigate to="/courses" replace />}
    </ProtectedRoute>
  );
}
