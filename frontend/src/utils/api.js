import {
  clearAuth,
  getToken,
} from './authStorage'

const API_URL = ''

export async function apiFetch(path, options = {}) {
  const {
    skipUnauthorizedRedirect = false,
    ...fetchOptions
  } = options
  const token = getToken()

  const headers = {
    ...(fetchOptions.headers || {}),
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...fetchOptions,
    headers,
  })

  if (response.status === 401 && !skipUnauthorizedRedirect) {
    clearAuth()

    window.dispatchEvent(
      new Event('ihub:unauthorized'),
    )
  }

  return response
}
