import { afterEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({
  apiFetch: vi.fn(),
}))

vi.mock('../utils/api', () => api)

import {
  changePassword,
  getAuthenticatedUser,
  login,
} from './authService'

function response(body, options = {}) {
  return {
    ok: options.ok ?? true,
    status: options.status ?? 200,
    json: vi.fn().mockResolvedValue(body),
  }
}

describe('authService', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('envia as credenciais, o ambiente e retorna o token', async () => {
    const fetch = vi.fn().mockResolvedValue(
      response({ token: 'jwt' }),
    )

    vi.stubGlobal('fetch', fetch)

    await expect(
      login(
        'admin',
        'senha',
        'DEVELOPMENT',
      ),
    ).resolves.toEqual({
      token: 'jwt',
    })

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8081/api/auth/login',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          username: 'admin',
          password: 'senha',
          environment: 'DEVELOPMENT',
        }),
      }),
    )
  })

  it('envia homologação quando selecionado', async () => {
    const fetch = vi.fn().mockResolvedValue(
      response({ token: 'jwt' }),
    )

    vi.stubGlobal('fetch', fetch)

    await login(
      'admin',
      'senha',
      'HOMOLOGATION',
    )

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8081/api/auth/login',
      expect.objectContaining({
        body: JSON.stringify({
          username: 'admin',
          password: 'senha',
          environment: 'HOMOLOGATION',
        }),
      }),
    )
  })

  it('informa credenciais inválidas quando a API retorna 401', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        response(null, {
          ok: false,
          status: 401,
        }),
      ),
    )

    await expect(
      login(
        'admin',
        'errada',
        'DEVELOPMENT',
      ),
    ).rejects.toThrow(
      'Usuário ou senha inválidos.',
    )
  })

  it('envia a nova senha com o token da sessão', async () => {
    const fetch = vi.fn().mockResolvedValue(
      response(null, { status: 204 }),
    )
    vi.stubGlobal('fetch', fetch)

    await expect(
      changePassword('jwt-token', 'nova-senha'),
    ).resolves.toBeUndefined()

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8081/api/auth/password',
      expect.objectContaining({
        method: 'PUT',
        headers: expect.objectContaining({
          Authorization: 'Bearer jwt-token',
        }),
        body: JSON.stringify({ newPassword: 'nova-senha' }),
      }),
    )
  })

  it('informa expiração de sessão ao trocar senha sem autorização', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(
      response(null, { ok: false, status: 401 }),
    ))

    await expect(
      changePassword('jwt-token', 'nova-senha'),
    ).rejects.toThrow('Sua sessão expirou. Faça login novamente.')
  })

  it('consulta os dados do usuário autenticado', async () => {
    api.apiFetch.mockResolvedValue(
      response({ username: 'criador', role: 'C' }),
    )

    await expect(getAuthenticatedUser()).resolves.toEqual({
      username: 'criador',
      role: 'C',
    })

    expect(api.apiFetch).toHaveBeenCalledWith('/api/auth/me')
  })

  it('informa erro quando não consegue consultar o usuário autenticado', async () => {
    api.apiFetch.mockResolvedValue(response(null, { ok: false }))

    await expect(getAuthenticatedUser()).rejects.toThrow(
      'Não foi possível carregar os dados do usuário.',
    )
  })
})
