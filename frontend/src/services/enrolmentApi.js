import { apiRequest } from './httpClient.js';

export async function enrolInCourse(token, courseId) {
  return apiRequest('/api/enrolments', {
    method: 'POST',
    token,
    body: { courseId }
  });
}

export async function fetchMyEnrolments(token) {
  return apiRequest('/api/enrolments/my', { token });
}

export async function fetchAllEnrolments(token) {
  return apiRequest('/api/enrolments', { token });
}

export async function cancelEnrolment(id, token) {
  return apiRequest(`/api/enrolments/${id}`, { method: 'DELETE', token });
}
