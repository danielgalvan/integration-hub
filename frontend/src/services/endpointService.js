const API_URL = 'http://localhost:8081'

export async function getEndpoints() {
  const response = await fetch(`${API_URL}/api/endpoints`)

  if (!response.ok) {
    throw new Error('Não foi possível carregar os endpoints.')
  }

  return response.json()
}

export async function getEndpointById(id) {
  const response = await fetch(`${API_URL}/api/endpoints/${id}`)

  if (!response.ok) {
    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message || 'Não foi possível carregar o endpoint.',
    )
  }

  return response.json()
}

export async function getEndpointsByIntegration(integrationId) {
  const response = await fetch(
    `${API_URL}/api/endpoints/integration/${integrationId}`,
  )

  if (!response.ok) {
    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message
        || 'Não foi possível carregar os endpoints da integração.',
    )
  }

  return response.json()
}

export async function createEndpoint(endpoint) {
  const response = await fetch(`${API_URL}/api/endpoints`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(endpoint),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message || 'Não foi possível cadastrar o endpoint.',
    )
  }

  return response.json()
}