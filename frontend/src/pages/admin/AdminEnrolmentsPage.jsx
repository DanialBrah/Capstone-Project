import { useEffect, useState } from 'react';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import LoadingMessage from '../../components/LoadingMessage.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { cancelEnrolment, fetchAllEnrolments } from '../../services/enrolmentApi.js';

export default function AdminEnrolmentsPage() {
  const { token } = useAuth();

  const [enrolments, setEnrolments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancellingId, setCancellingId] = useState('');

  async function loadEnrolments() {
    try {
      setLoading(true);
      setError('');
      const data = await fetchAllEnrolments(token);
      setEnrolments(data);
    } catch (err) {
      setError(err.message || 'Could not load enrolments.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadEnrolments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  async function handleCancel(id) {
    setCancellingId(id);
    setError('');

    try {
      await cancelEnrolment(id, token);
      await loadEnrolments();
    } catch (err) {
      setError(err.message || 'Could not cancel this enrolment.');
    } finally {
      setCancellingId('');
    }
  }

  return (
    <>
      <div className="page-heading">
        <h1>All Enrolments</h1>
      </div>

      {loading && <LoadingMessage message="Loading enrolments..." />}
      {error && <ErrorMessage message={error} />}

      {!loading && enrolments.length === 0 && <EmptyState message="No enrolments yet." />}

      {!loading && enrolments.length > 0 && (
        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Email</th>
                <th>Course</th>
                <th>Category</th>
                <th>Enrolled at</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {enrolments.map((enrolment) => (
                <tr key={enrolment.id}>
                  <td>{enrolment.studentName}</td>
                  <td>{enrolment.studentEmail}</td>
                  <td>{enrolment.courseTitle}</td>
                  <td>{enrolment.courseCategory}</td>
                  <td>{new Date(enrolment.enrolledAt).toLocaleDateString()}</td>
                  <td>
                    <StatusBadge status={enrolment.status} />
                  </td>
                  <td>
                    {enrolment.status === 'ACTIVE' && (
                      <button
                        type="button"
                        className="button-danger"
                        onClick={() => handleCancel(enrolment.id)}
                        disabled={cancellingId === enrolment.id}
                      >
                        {cancellingId === enrolment.id ? 'Cancelling...' : 'Cancel'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
