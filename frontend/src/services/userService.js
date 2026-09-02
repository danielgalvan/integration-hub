import { getToken } from '../utils/authStorage'

const API_URL = ''

function getHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${getToken()}`,
  }
}

async function handleResponse(response) {
  if (response.status === 401) {
    window.dispatchEvent(
      new Event('ihub:unauthorized'),
    )

    throw new Error(
      'Sua sessão expirou. Faça login novamente.',
    )
  }

  if (response.status === 403) {
    throw new Error(
      'Você não possui permissão para realizar esta operação.',
    )
  }

  if (!response.ok) {
    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível concluir a operação.',
    )
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export async function getUsers() {
  const response = await fetch(
    `${API_URL}/api/users`,
    {
      method: 'GET',
      headers: getHeaders(),
    },
  )

  return handleResponse(response)
}

export async function createUser(user) {
  const response = await fetch(
    `${API_URL}/api/users`,
    {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(user),
    },
  )

  return handleResponse(response)
}

export async function updateUser(
  id,
  user,
) {
  const response = await fetch(
    `${API_URL}/api/users/${id}`,
    {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify(user),
    },
  )

  return handleResponse(response)
}

export async function deleteUser(id) {
  const response = await fetch(
    `${API_URL}/api/users/${id}`,
    {
      method: 'DELETE',
      headers: getHeaders(),
    },
  )

  return handleResponse(response)
}

export async function resetUserPassword(id) {
  const response = await fetch(
    `${API_URL}/api/users/${id}/reset-password`,
    {
      method: 'POST',
      headers: getHeaders(),
    },
  )

  return handleResponse(response)
}
