const API_URL = 'http://localhost:8081'

export async function getIntegrations() {
  const response = await fetch(`${API_URL}/api/integrations`)

  if (!response.ok) {
    throw new Error('Não foi possível carregar as integrações.')
  }

  return response.json()
}