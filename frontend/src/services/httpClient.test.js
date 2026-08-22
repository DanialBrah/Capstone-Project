import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiRequest, buildQueryString } from './httpClient.js';

describe('buildQueryString', () => {
  it('omits undefined, null, and empty-string values', () => {
    const result = buildQueryString({ keyword: undefined, category: null, level: '', page: 0 });

    expect(result).toBe('page=0');
  });

  it('includes provided values, including falsy-but-real ones like 0 and false', () => {
    const result = buildQueryString({ keyword: 'java', active: false, page: 0, size: 10 });

    const params = new URLSearchParams(result);
    expect(params.get('keyword')).toBe('java');
    expect(params.get('active')).toBe('false');
    expect(params.get('page')).toBe('0');
    expect(params.get('size')).toBe('10');
  });
});

describe('apiRequest', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  function mockFetchOnce({ ok, status = 200, body }) {
    const fetchMock = vi.fn().mockResolvedValue({
      ok,
      status,
      headers: { get: () => 'application/json' },
      json: () => Promise.resolve(body)
    });
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
  }

  it('attaches an Authorization header when a token is provided', async () => {
    const fetchMock = mockFetchOnce({ ok: true, body: { id: '1' } });

    await apiRequest('/api/courses', { token: 'abc123' });

    const [, options] = fetchMock.mock.calls[0];
    expect(options.headers.Authorization).toBe('Bearer abc123');
  });

  it('sends a JSON body and Content-Type header when a body is given', async () => {
    const fetchMock = mockFetchOnce({ ok: true, body: {} });

    await apiRequest('/api/courses', { method: 'POST', body: { title: 'Java' } });

    const [, options] = fetchMock.mock.calls[0];
    expect(options.headers['Content-Type']).toBe('application/json');
    expect(options.body).toBe(JSON.stringify({ title: 'Java' }));
  });

  it('throws with the server-provided message on a non-ok response', async () => {
    mockFetchOnce({ ok: false, status: 409, body: { message: 'Already enrolled in this course' } });

    await expect(apiRequest('/api/enrolments', { method: 'POST' })).rejects.toThrow(
      'Already enrolled in this course'
    );
  });

  it('falls back to a generic message when the error body has none', async () => {
    mockFetchOnce({ ok: false, status: 500, body: {} });

    await expect(apiRequest('/api/courses')).rejects.toThrow('Request failed with status 500');
  });
});
