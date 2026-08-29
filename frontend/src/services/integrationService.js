import { apiFetch } from '../utils/api'

export async function getIntegrations() {
  const response = await apiFetch(
    '/api/integrations',
  )

  if (!response.ok) {
    throw new Error(
      'Não foi possível carregar as integrações.',
    )
  }

  return response.json()
}

export async function createIntegration(
  integration,
) {
  const response = await apiFetch(
    '/api/integrations',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(integration),
    },
  )

  if (!response.ok) {
    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível cadastrar a integração.',
    )
  }

  return response.json()
}

export async function updateIntegration(
  id,
  integration,
) {
  const response = await apiFetch(
    `/api/integrations/${id}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(integration),
    },
  )

  if (!response.ok) {
    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível atualizar a integração.',
    )
  }

  return response.json()
}

export async function generateIntegrationApiKey(
  id,
) {
  const response = await apiFetch(
    `/api/integrations/${id}/api-key`,
    {
      method: 'POST',
    },
  )

  if (!response.ok) {
    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível gerar a API Key.',
    )
  }

  return response.json()
}

export async function deleteIntegration(id) {
  const response = await apiFetch(
    `/api/integrations/${id}`,
    {
      method: 'DELETE',
    },
  )

  if (!response.ok) {
    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível excluir a integração.',
    )
  }
}
