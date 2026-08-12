import { NavLink, useNavigate } from 'react-router';
import { useAuth } from '../context/AuthContext.jsx';

export default function AppHeader() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="app-header">
      <div className="app-header-brand">
        <strong>Course Enrolment</strong>
      </div>

      {isAuthenticated && (
        <>
          <nav className="app-nav">
            <NavLink to="/courses">Courses</NavLink>
            {user?.role === 'STUDENT' && <NavLink to="/my-enrolments">My Enrolments</NavLink>}
            {user?.role === 'ADMIN' && (
              <>
                <NavLink to="/admin/courses">Manage Courses</NavLink>
                <NavLink to="/admin/enrolments">All Enrolments</NavLink>
                <NavLink to="/admin/reports">Reports</NavLink>
              </>
            )}
          </nav>

          <div className="app-header-user">
            <span>
              {user?.name} <span className="muted">({user?.role})</span>
            </span>
            <button type="button" onClick={handleLogout}>
              Log out
            </button>
          </div>
        </>
      )}
    </header>
  );
}
