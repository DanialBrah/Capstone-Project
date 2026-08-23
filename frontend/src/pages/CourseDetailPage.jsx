import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import ErrorMessage from '../components/ErrorMessage.jsx';
import LoadingMessage from '../components/LoadingMessage.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { activateCourse, deactivateCourse, deleteCourse, fetchCourseById } from '../services/courseApi.js';
import { enrolInCourse } from '../services/enrolmentApi.js';

export default function CourseDetailPage() {
  const { id } = useParams();
  const { token, user } = useAuth();
  const navigate = useNavigate();

  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [enrolling, setEnrolling] = useState(false);
  const [enrolError, setEnrolError] = useState('');
  const [enrolled, setEnrolled] = useState(false);

  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    let ignore = false;

    async function loadCourse() {
      try {
        setLoading(true);
        setError('');
        const data = await fetchCourseById(id, token);

        if (!ignore) {
          setCourse(data);
        }
      } catch (err) {
        if (!ignore) {
          setError(err.message || 'Could not load this course.');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    loadCourse();

    return () => {
      ignore = true;
    };
  }, [id, token]);

  async function handleEnrol() {
    setEnrolling(true);
    setEnrolError('');

    try {
      await enrolInCourse(token, id);
      setEnrolled(true);
    } catch (err) {
      setEnrolError(err.message || 'Could not enrol in this course.');
    } finally {
      setEnrolling(false);
    }
  }

  async function handleToggleActive() {
    try {
      const updated = course.active ? await deactivateCourse(id, token) : await activateCourse(id, token);
      setCourse(updated);
    } catch (err) {
      setError(err.message || 'Could not update this course.');
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Permanently delete "${course.title}"? This cannot be undone.`)) {
      return;
    }

    setDeleting(true);
    setError('');

    try {
      await deleteCourse(id, token);
      navigate('/admin/courses', { replace: true });
    } catch (err) {
      setError(err.message || 'Could not delete this course.');
      setDeleting(false);
    }
  }

  if (loading) {
    return <LoadingMessage message="Loading course..." />;
  }

  if (error) {
    return <ErrorMessage message={error} />;
  }

  if (!course) {
    return null;
  }

  const isFull = course.enrolledCount >= course.capacity;

  return (
    <>
      <div className="page-heading">
        <h1>{course.title}</h1>
        <StatusBadge status={course.active ? 'ACTIVE' : 'INACTIVE'} />
      </div>

      {course.imageBase64 && <img className="course-image-banner" src={course.imageBase64} alt="" />}

      <p>{course.description}</p>

      <div className="detail-grid">
        <div>
          <span>Category</span>
          <strong>{course.category}</strong>
        </div>
        <div>
          <span>Level</span>
          <strong>{course.level}</strong>
        </div>
        <div>
          <span>Instructor</span>
          <strong>{course.instructor}</strong>
        </div>
        <div>
          <span>Seats</span>
          <strong>
            {course.enrolledCount} / {course.capacity} {isFull && <StatusBadge status="FULL" />}
          </strong>
        </div>
      </div>

      {user?.role === 'STUDENT' && (
        <section className="card">
          {enrolled ? (
            <p>
              You're enrolled in this course. View it on your{' '}
              <Link to="/my-enrolments">My Enrolments</Link> page.
            </p>
          ) : (
            <>
              <button
                type="button"
                className="button-primary"
                onClick={handleEnrol}
                disabled={enrolling || !course.active || isFull}
              >
                {enrolling ? 'Enrolling...' : 'Enrol in this course'}
              </button>
              {!course.active && <p className="muted">This course is not currently active.</p>}
              {course.active && isFull && <p className="muted">This course is full.</p>}
              {enrolError && <ErrorMessage message={enrolError} />}
            </>
          )}
        </section>
      )}

      {user?.role === 'ADMIN' && (
        <section className="form-actions">
          <Link className="button-link" to={`/admin/courses/${course.id}/edit`}>
            Edit course
          </Link>
          <button type="button" onClick={handleToggleActive}>
            {course.active ? 'Deactivate' : 'Activate'}
          </button>
          <button type="button" className="button-danger" onClick={handleDelete} disabled={deleting}>
            {deleting ? 'Deleting...' : 'Delete'}
          </button>
          <button type="button" onClick={() => navigate('/admin/courses')}>
            Back to course management
          </button>
        </section>
      )}
    </>
  );
}
