import { apiRequest, buildQueryString } from './httpClient.js';

export async function fetchCourses(token, params = {}) {
  const queryString = buildQueryString({
    keyword: params.keyword,
    category: params.category,
    level: params.level,
    active: params.active,
    page: params.page ?? 0,
    size: params.size ?? 10,
    sortBy: params.sortBy ?? 'title',
    direction: params.direction ?? 'asc'
  });

  return apiRequest(`/api/courses?${queryString}`, { token });
}

export async function fetchCourseById(id, token) {
  return apiRequest(`/api/courses/${id}`, { token });
}

export async function createCourse(token, payload) {
  return apiRequest('/api/courses', {
    method: 'POST',
    token,
    body: payload
  });
}

export async function updateCourse(id, token, payload) {
  return apiRequest(`/api/courses/${id}`, {
    method: 'PUT',
    token,
    body: payload
  });
}

export async function activateCourse(id, token) {
  return apiRequest(`/api/courses/${id}/activate`, { method: 'PATCH', token });
}

export async function deactivateCourse(id, token) {
  return apiRequest(`/api/courses/${id}/deactivate`, { method: 'PATCH', token });
}
