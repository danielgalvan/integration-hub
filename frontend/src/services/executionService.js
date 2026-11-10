
import { apiFetch } from '../utils/api'

const BASE_URL = '/api/admin/executions'

export async function getExecutions(filters = {}) {
  const {
    integrationId,
    endpointId,
    executionStatus,
    startedFrom,
    startedTo,
    page = 0,
    size = 20,
  } = filters

  const params = new URLSearchParams()

  if (integrationId !== undefined &&
      integrationId !== null &&
      integrationId !== '') {
    params.set('integrationId', integrationId)
  }

  if (endpointId !== undefined &&
      endpointId !== null &&
      endpointId !== '') {
    params.set('endpointId', endpointId)
  }

  if (executionStatus) {
    params.set('executionStatus', executionStatus)
  }

  if (startedFrom) {
    params.set('startedFrom', startedFrom)
  }

  if (startedTo) {
    params.set('startedTo', startedTo)
  }

  params.set('page', page)
  params.set('size', size)

  const response = await apiFetch(
    `${BASE_URL}?${params.toString()}`,
  )

  if (!response.ok) {
    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível carregar o histórico de execuções.',
    )
  }

  return response.json()
}

export async function getExecutionById(id) {
  if (id === undefined || id === null || id === '') {
    throw new Error('ID da execução não informado.')
  }

  const response = await apiFetch(
    `${BASE_URL}/${encodeURIComponent(id)}`,
  )

  if (!response.ok) {
    if (response.status === 404) {
      throw new Error('Execução não encontrada.')
    }

    const error = await response
      .json()
      .catch(() => null)

    throw new Error(
      error?.message ||
        'Não foi possível carregar os detalhes da execução.',
    )
  }

  return response.json()
}
