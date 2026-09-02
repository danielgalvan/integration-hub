import { afterEach, describe, expect, it, vi } from 'vitest'
import { getEnvironments } from './environmentService'

function response(body, ok = true) {
  return {
    ok,
    json: vi.fn().mockResolvedValue(body),
  }
}

describe('environmentService', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('consulta os ambientes públicos disponíveis', async () => {
    const fetch = vi.fn().mockResolvedValue(response([
      { id: 'development', name: 'Desenvolvimento Local' },
    ]))
    vi.stubGlobal('fetch', fetch)

    await expect(getEnvironments()).resolves.toEqual([
      { id: 'development', name: 'Desenvolvimento Local' },
    ])
    expect(fetch).toHaveBeenCalledWith(
      '/api/environments',
    )
  })

  it('informa erro quando a API não retorna os ambientes', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response(null, false)))

    await expect(getEnvironments()).rejects.toThrow(
      'Não foi possível carregar os ambientes.',
    )
  })
})

