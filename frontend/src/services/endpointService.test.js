import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  createEndpoint,
  deleteEndpoint,
  executeEndpoint,
  getEndpointsByIntegration,
  updateEndpoint,
} from './endpointService'

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

  it('lista, atualiza e exclui endpoints da integração', async () => {
    const fetch = vi.fn()
      .mockResolvedValueOnce(jsonResponse([{ id: 3 }]))
      .mockResolvedValueOnce(jsonResponse({ id: 3 }))
      .mockResolvedValueOnce(jsonResponse(null, { status: 204 }))
    vi.stubGlobal('fetch', fetch)

    await expect(getEndpointsByIntegration(8)).resolves.toEqual([{ id: 3 }])
    await updateEndpoint(3, { name: 'Buscar pedido' })
    await deleteEndpoint(3)

    expect(fetch).toHaveBeenNthCalledWith(
      1,
      '/api/endpoints/integration/8',
      expect.objectContaining({ headers: {} }),
    )
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      '/api/endpoints/3',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ name: 'Buscar pedido' }),
      }),
    )
    expect(fetch).toHaveBeenNthCalledWith(
      3,
      '/api/endpoints/3',
      expect.objectContaining({ method: 'DELETE' }),
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

