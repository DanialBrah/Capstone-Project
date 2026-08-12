import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import LoadingMessage from '../../components/LoadingMessage.jsx';
import PaginationControls from '../../components/PaginationControls.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { activateCourse, deactivateCourse, fetchCourses } from '../../services/courseApi.js';

const PAGE_SIZE = 10;

export default function AdminCoursesPage() {
  const { token } = useAuth();

  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [togglingId, setTogglingId] = useState('');

  async function loadCourses() {
    try {
      setLoading(true);
      setError('');
      // No "active" filter here - admins manage both active and
      // deactivated courses from this page.
      const data = await fetchCourses(token, { page, size: PAGE_SIZE, sortBy: 'title', direction: 'asc' });
      setPageData(data);
    } catch (err) {
      setError(err.message || 'Could not load courses.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCourses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, page]);

  async function handleToggleActive(course) {
    setTogglingId(course.id);
    setError('');

    try {
      const updated = course.active ? await deactivateCourse(course.id, token) : await activateCourse(course.id, token);
      setPageData((current) => ({
        ...current,
        content: current.content.map((item) => (item.id === updated.id ? updated : item))
      }));
    } catch (err) {
      setError(err.message || 'Could not update this course.');
    } finally {
      setTogglingId('');
    }
  }

  return (
    <>
      <div className="page-heading">
        <h1>Manage Courses</h1>
        <Link className="button-link button-primary" to="/admin/courses/new">
          + New course
        </Link>
      </div>

      {loading && <LoadingMessage message="Loading courses..." />}
      {error && <ErrorMessage message={error} />}

      {!loading && pageData && pageData.content.length === 0 && (
        <EmptyState message="No courses yet. Create the first one." />
      )}

      {!loading && pageData && pageData.content.length > 0 && (
        <>
          <div className="data-table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Level</th>
                  <th>Seats</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map((course) => (
                  <tr key={course.id}>
                    <td>{course.title}</td>
                    <td>{course.category}</td>
                    <td>{course.level}</td>
                    <td>
                      {course.enrolledCount} / {course.capacity}
                    </td>
                    <td>
                      <StatusBadge status={course.active ? 'ACTIVE' : 'INACTIVE'} />
                    </td>
                    <td className="actions-cell">
                      <Link className="button-link" to={`/admin/courses/${course.id}/edit`}>
                        Edit
                      </Link>
                      <button
                        type="button"
                        onClick={() => handleToggleActive(course)}
                        disabled={togglingId === course.id}
                      >
                        {course.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <PaginationControls pageInfo={pageData} loading={loading} onPageChange={setPage} />
        </>
      )}
    </>
  );
}
