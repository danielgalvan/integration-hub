import { afterEach, describe, expect, it, vi } from 'vitest'
import { login } from './authService'

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
})
