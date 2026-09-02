import { afterEach, describe, expect, it, vi } from 'vitest'
import { createEndpoint, executeEndpoint } from './endpointService'

function jsonResponse(body, options = {}) {
  return {
    ok: options.ok ?? true,
    status: options.status ?? 200,
    statusText: options.statusText ?? 'OK',
    headers: { get: () => 'application/json' },
    json: vi.fn().mockResolvedValue(body),
    text: vi.fn(),
  }
}

describe('endpointService', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('envia o payload ao criar um endpoint', async () => {
    const fetch = vi.fn().mockResolvedValue(jsonResponse({ id: 1 }))
    vi.stubGlobal('fetch', fetch)
    const endpoint = { integrationId: 1, name: 'Listar', path: '/listar' }

    await createEndpoint(endpoint)

    expect(fetch).toHaveBeenCalledWith(
      '/api/endpoints',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(endpoint),
      }),
    )
  })

  it('monta e executa a URL dinâmica codificando parâmetros', async () => {
    const fetch = vi.fn().mockResolvedValue(jsonResponse([{ id: 1 }]))
    vi.stubGlobal('fetch', fetch)

    const result = await executeEndpoint(
      { basePath: '/api/clientes/' },
      { path: '/buscar', method: 'GET' },
      { nome: 'Ana Maria', codigo: '10' },
    )

    expect(fetch).toHaveBeenCalledWith(
      '/api/clientes/buscar?nome=Ana+Maria&codigo=10',
      expect.objectContaining({ method: 'GET' }),
    )
    expect(result).toEqual(expect.objectContaining({
      success: true,
      status: 200,
      data: [{ id: 1 }],
    }))
  })

  it('retorna o corpo de erro da execução sem lançar exceção', async () => {
    const fetch = vi.fn().mockResolvedValue(jsonResponse(
      { message: 'Parâmetro obrigatório não informado: codigo' },
      { ok: false, status: 400, statusText: 'Bad Request' },
    ))
    vi.stubGlobal('fetch', fetch)

    const result = await executeEndpoint(
      { basePath: '/api/clientes' },
      { path: '/buscar' },
    )

    expect(result).toEqual(expect.objectContaining({
      success: false,
      status: 400,
      data: { message: 'Parâmetro obrigatório não informado: codigo' },
    }))
  })
})

