import { afterEach, describe, expect, it, vi } from 'vitest'
import { createIntegration, deleteIntegration } from './integrationService'

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
      'http://localhost:8081/api/integrations',
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
})
