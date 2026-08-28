const API_URL = 'http://localhost:8081'

export async function login(username, password, environment) {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      username,
      password,
      environment,
    }),
  })

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Usuário ou senha inválidos.')
    }

    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message || 'Não foi possível realizar o login.',
    )
  }

  return response.json()
}
