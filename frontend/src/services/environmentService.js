const API_URL = 'http://localhost:8081'

export async function getEnvironments() {
  const response = await fetch(`${API_URL}/api/environments`)

  if (!response.ok) {
    throw new Error('Não foi possível carregar os ambientes.')
  }

  return response.json()
}
