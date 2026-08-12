import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import EmptyState from '../components/EmptyState.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import LoadingMessage from '../components/LoadingMessage.jsx';
import PaginationControls from '../components/PaginationControls.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { fetchCourses } from '../services/courseApi.js';

const PAGE_SIZE = 8;

export default function CoursesPage() {
  const { token } = useAuth();

  const [keywordInput, setKeywordInput] = useState('');
  const [categoryInput, setCategoryInput] = useState('');
  const [filters, setFilters] = useState({ keyword: '', category: '' });
  const [sortBy, setSortBy] = useState('title');
  const [direction, setDirection] = useState('asc');
  const [page, setPage] = useState(0);

  const [pageData, setPageData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;

    async function loadCourses() {
      try {
        setLoading(true);
        setError('');
        // Students only ever browse active courses - inactive ones are
        // managed separately by admins on the "Manage Courses" page.
        const data = await fetchCourses(token, {
          keyword: filters.keyword,
          category: filters.category,
          active: true,
          page,
          size: PAGE_SIZE,
          sortBy,
          direction
        });

        if (!ignore) {
          setPageData(data);
        }
      } catch (err) {
        if (!ignore) {
          setError(err.message || 'Could not load courses.');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    loadCourses();

    return () => {
      ignore = true;
    };
  }, [token, filters, sortBy, direction, page]);

  function handleSearchSubmit(event) {
    event.preventDefault();
    setPage(0);
    setFilters({ keyword: keywordInput.trim(), category: categoryInput.trim() });
  }

  return (
    <>
      <div className="page-heading">
        <h1>Courses</h1>
      </div>

      <form className="filter-bar" onSubmit={handleSearchSubmit}>
        <label>
          Search by title
          <input
            type="search"
            placeholder="e.g. Java"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
          />
        </label>

        <label>
          Category
          <input
            type="text"
            placeholder="e.g. Programming"
            value={categoryInput}
            onChange={(event) => setCategoryInput(event.target.value)}
          />
        </label>

        <label>
          Sort by
          <select
            value={sortBy}
            onChange={(event) => {
              setSortBy(event.target.value);
              setPage(0);
            }}
          >
            <option value="title">Title</option>
            <option value="category">Category</option>
            <option value="level">Level</option>
            <option value="capacity">Capacity</option>
            <option value="createdAt">Newest</option>
          </select>
        </label>

        <label>
          Direction
          <select
            value={direction}
            onChange={(event) => {
              setDirection(event.target.value);
              setPage(0);
            }}
          >
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
          </select>
        </label>

        <button type="submit" className="button-primary">
          Search
        </button>
      </form>

      {loading && <LoadingMessage message="Loading courses..." />}
      {error && <ErrorMessage message={error} />}

      {!loading && !error && pageData && (
        <>
          {pageData.content.length === 0 ? (
            <EmptyState message="No courses match your search." />
          ) : (
            <div className="data-table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Level</th>
                    <th>Instructor</th>
                    <th>Seats</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {pageData.content.map((course) => (
                    <tr key={course.id}>
                      <td>{course.title}</td>
                      <td>{course.category}</td>
                      <td>{course.level}</td>
                      <td>{course.instructor}</td>
                      <td>
                        {course.enrolledCount} / {course.capacity}
                        {course.enrolledCount >= course.capacity && (
                          <>
                            {' '}
                            <StatusBadge status="FULL" />
                          </>
                        )}
                      </td>
                      <td>
                        <Link className="button-link" to={`/courses/${course.id}`}>
                          View
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <PaginationControls pageInfo={pageData} loading={loading} onPageChange={setPage} />
        </>
      )}
    </>
  );
}
