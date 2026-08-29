import { apiFetch } from '../utils/api'

const API_URL = 'http://localhost:8081'

export async function login(
  username,
  password,
  environment,
) {
  const response = await fetch(
    `${API_URL}/api/auth/login`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username,
        password,
        environment,
      }),
    },
  )

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error(
        'Usuário ou senha inválidos.',
      )
    }

    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível realizar o login.',
    )
  }

  return response.json()
}

export async function getAuthenticatedUser() {
  const response = await apiFetch(
    '/api/auth/me',
  )

  if (!response.ok) {
    throw new Error(
      'Não foi possível carregar os dados do usuário.',
    )
  }

  return response.json()
}

export async function changePassword(
  token,
  newPassword,
) {
  const response = await fetch(
    `${API_URL}/api/auth/password`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        newPassword,
      }),
    },
  )

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error(
        'Sua sessão expirou. Faça login novamente.',
      )
    }

    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível alterar a senha.',
    )
  }
}
