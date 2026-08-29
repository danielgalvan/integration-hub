import {
  clearAuth,
  getToken,
} from './authStorage'

const API_URL = 'http://localhost:8081'

export async function apiFetch(path, options = {}) {
  const token = getToken()

  const headers = {
    ...(options.headers || {}),
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  })

  if (response.status === 401) {
    clearAuth()

    window.dispatchEvent(
      new Event('ihub:unauthorized'),
    )
  }

  return response
}
