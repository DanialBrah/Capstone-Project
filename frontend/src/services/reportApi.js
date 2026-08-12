import { apiRequest } from './httpClient.js';

export async function fetchEnrolmentReports(token) {
  const [byCourse, byCategory, byMonth] = await Promise.all([
    apiRequest('/api/reports/enrolments-by-course', { token }),
    apiRequest('/api/reports/enrolments-by-category', { token }),
    apiRequest('/api/reports/monthly-enrolments', { token })
  ]);

  return { byCourse, byCategory, byMonth };
}
