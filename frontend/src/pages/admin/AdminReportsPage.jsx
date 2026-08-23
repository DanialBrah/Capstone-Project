import { useEffect, useState } from 'react';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import LoadingMessage from '../../components/LoadingMessage.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { fetchEnrolmentReports } from '../../services/reportApi.js';

function ReportCard({ title, description, items }) {
  const maxCount = Math.max(1, ...items.map((item) => item.count));

  return (
    <section className="card report-card">
      <h2>{title}</h2>
      <p className="muted">{description}</p>

      {items.length === 0 ? (
        <p className="muted">No data yet.</p>
      ) : (
        <div>
          {items.map((item) => (
            <div className="report-row" key={item.label}>
              <span>{item.label}</span>
              <div className="report-bar-track">
                <div className="report-bar-fill" style={{ width: `${(item.count / maxCount) * 100}%` }} />
              </div>
              <strong>{item.count}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

export default function AdminReportsPage() {
  const { token } = useAuth();
  const [reports, setReports] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;

    async function loadReports() {
      try {
        setLoading(true);
        setError('');
        const data = await fetchEnrolmentReports(token);

        if (!ignore) {
          setReports(data);
        }
      } catch (err) {
        if (!ignore) {
          setError(err.message || 'Could not load reports.');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    loadReports();

    return () => {
      ignore = true;
    };
  }, [token]);

  return (
    <>
      <div className="page-heading">
        <h1>Reports</h1>
      </div>

      {loading && <LoadingMessage message="Loading reports..." />}
      {error && <ErrorMessage message={error} />}

      {!loading && reports && (
        <section className="report-grid">
          <ReportCard
            title="Enrolments by Course"
            description="Active enrolments per course - also shows which courses are most popular."
            items={reports.byCourse}
          />
          <ReportCard
            title="Enrolments by Category"
            description="Active enrolments grouped by course category."
            items={reports.byCategory}
          />
          <ReportCard
            title="Monthly Enrolment Totals"
            description="Active enrolments grouped by the month they happened."
            items={reports.byMonth}
          />
        </section>
      )}
    </>
  );
}
