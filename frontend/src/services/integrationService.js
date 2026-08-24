const API_URL = 'http://localhost:8081'

export async function getIntegrations() {
  const response = await fetch(`${API_URL}/api/integrations`)

  if (!response.ok) {
    throw new Error('Não foi possível carregar as integrações.')
  }

  return response.json()
}

export async function createIntegration(integration) {
  const response = await fetch(`${API_URL}/api/integrations`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(integration),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message || 'Não foi possível cadastrar a integração.',
    )
  }

  return response.json()
}