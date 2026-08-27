import { apiFetch } from '../utils/api'

const API_URL = 'http://localhost:8081'

export async function getEndpoints() {
  const response = await apiFetch('/api/endpoints')

  if (!response.ok) {
    throw new Error('Não foi possível carregar os endpoints.')
  }

  return response.json()
}

export async function getEndpointById(id) {
  const response = await apiFetch(`/api/endpoints/${id}`)

  if (!response.ok) {
    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message || 'Não foi possível carregar o endpoint.',
    )
  }

  return response.json()
}

export async function getEndpointsByIntegration(integrationId) {
  const response = await apiFetch(
    `/api/endpoints/integration/${integrationId}`,
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
  const response = await apiFetch('/api/endpoints', {
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

export async function updateEndpoint(id, endpoint) {
  const response = await apiFetch(`/api/endpoints/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(endpoint),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message || 'Não foi possível atualizar o endpoint.',
    )
  }

  return response.json()
}

export async function deleteEndpoint(id) {
  const response = await apiFetch(`/api/endpoints/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    const error = await response.json().catch(() => null)

    throw new Error(
      error?.message || 'Não foi possível excluir o endpoint.',
    )
  }
}

export async function executeEndpoint(
  integration,
  endpoint,
  parameters = {},
) {
  const basePath = integration.basePath.replace(/\/$/, '')
  const endpointPath = endpoint.path.replace(/^\//, '')

  const url = new URL(
    `${API_URL}${basePath}/${endpointPath}`,
  )

  Object.entries(parameters).forEach(([name, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.append(name, value)
    }
  })

  const startedAt = performance.now()

  try {
    const response = await fetch(url)

    const duration = Math.round(
      performance.now() - startedAt,
    )

    const contentType = response.headers.get('content-type')

    let data

    if (contentType?.includes('application/json')) {
      data = await response.json()
    } else {
      data = await response.text()
    }

    return {
      success: response.ok,
      status: response.status,
      statusText: response.statusText,
      url: url.toString(),
      duration,
      data,
    }
  } catch {
    throw new Error(
      'Não foi possível executar o endpoint.',
    )
  }
}
