import { apiFetch } from '../utils/api'

async function getErrorMessage(
  response,
  fallbackMessage,
) {
  const error = await response
    .json()
    .catch(() => null)

  return (
    error?.message ||
    fallbackMessage
  )
}

export async function getIntegrationCredentials(
  integrationId,
) {
  const response = await apiFetch(
    `/api/integrations/${integrationId}/credentials`,
  )

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        'Não foi possível carregar as credenciais.',
      ),
    )
  }

  return response.json()
}

export async function createIntegrationCredential(
  integrationId,
  name,
) {
  const response = await apiFetch(
    `/api/integrations/${integrationId}/credentials`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name,
      }),
    },
  )

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        'Não foi possível criar a credencial.',
      ),
    )
  }

  return response.json()
}

export async function updateIntegrationCredential(
  integrationId,
  credentialId,
  name,
) {
  const response = await apiFetch(
    `/api/integrations/${integrationId}/credentials/${credentialId}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name,
      }),
    },
  )

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        'Não foi possível atualizar a credencial.',
      ),
    )
  }

  return response.json()
}

export async function setIntegrationCredentialActive(
  integrationId,
  credentialId,
  active,
) {
  const response = await apiFetch(
    `/api/integrations/${integrationId}/credentials/${credentialId}/active`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        active,
      }),
    },
  )

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        active
          ? 'Não foi possível ativar a credencial.'
          : 'Não foi possível desativar a credencial.',
      ),
    )
  }

  return response.json()
}

export async function getIntegrationCredentialApiKey(
  integrationId,
  credentialId,
) {
  const response = await apiFetch(
    `/api/integrations/${integrationId}/credentials/${credentialId}/api-key`,
  )

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        'Não foi possível consultar a API Key.',
      ),
    )
  }

  return response.json()
}

export async function regenerateIntegrationCredentialApiKey(
  integrationId,
  credentialId,
) {
  const response = await apiFetch(
    `/api/integrations/${integrationId}/credentials/${credentialId}/api-key`,
    {
      method: 'POST',
    },
  )

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(
        response,
        'Não foi possível regenerar a API Key.',
      ),
    )
  }

  return response.json()
}
