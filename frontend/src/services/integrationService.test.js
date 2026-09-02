import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  createIntegration,
  deleteIntegration,
  generateIntegrationApiKey,
} from './integrationService'

function response(body, options = {}) {
  return {
    ok: options.ok ?? true,
    json: vi.fn().mockResolvedValue(body),
  }
}

describe('integrationService', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('envia o payload ao criar uma integração', async () => {
    const fetch = vi.fn().mockResolvedValue(response({ id: 1 }))
    vi.stubGlobal('fetch', fetch)
    const integration = { name: 'Clientes', basePath: '/api/clientes', active: 'S' }

    await createIntegration(integration)

    expect(fetch).toHaveBeenCalledWith(
      '/api/integrations',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(integration) }),
    )
  })

  it('usa a mensagem retornada pela API quando a exclusão falha', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response(
      { message: 'A integração possui endpoints vinculados' },
      { ok: false },
    )))

    await expect(deleteIntegration(8)).rejects.toThrow(
      'A integração possui endpoints vinculados',
    )
  })

  it('gera API Key para a integração informada', async () => {
    const fetch = vi.fn().mockResolvedValue(
      response({ apiKey: 'ihub_chave_teste' }),
    )
    vi.stubGlobal('fetch', fetch)

    await expect(generateIntegrationApiKey(8)).resolves.toEqual({
      apiKey: 'ihub_chave_teste',
    })

    expect(fetch).toHaveBeenCalledWith(
      '/api/integrations/8/api-key',
      expect.objectContaining({ method: 'POST' }),
    )
  })

  it('usa a mensagem retornada pela API quando a geração falha', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response(
      { message: 'A integração não está configurada para utilizar API Key' },
      { ok: false },
    )))

    await expect(generateIntegrationApiKey(8)).rejects.toThrow(
      'A integração não está configurada para utilizar API Key',
    )
  })
})

