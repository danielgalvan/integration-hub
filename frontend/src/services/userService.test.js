import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  createUser,
  deleteUser,
  getUsers,
  resetUserPassword,
  updateUser,
} from './userService'

function response(body, options = {}) {
  return {
    ok: options.ok ?? true,
    status: options.status ?? 200,
    json: vi.fn().mockResolvedValue(body),
  }
}

describe('userService', () => {
  afterEach(() => {
    sessionStorage.clear()
    vi.unstubAllGlobals()
  })

  it('lista usuários com o token da sessão', async () => {
    sessionStorage.setItem('ihub_token', 'jwt-token')
    const fetch = vi.fn().mockResolvedValue(response([{ id: 1, username: 'admin' }]))
    vi.stubGlobal('fetch', fetch)

    await expect(getUsers()).resolves.toEqual([{ id: 1, username: 'admin' }])
    expect(fetch).toHaveBeenCalledWith(
      '/api/users',
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: 'Bearer jwt-token' }) }),
    )
  })

  it('envia dados ao criar e retorna a senha temporária', async () => {
    sessionStorage.setItem('ihub_token', 'jwt-token')
    const fetch = vi.fn().mockResolvedValue(response({ temporaryPassword: 'Senha123' }, { status: 201 }))
    vi.stubGlobal('fetch', fetch)
    const user = { username: 'novo', name: 'Novo Usuário', email: null, type: 'U' }

    await expect(createUser(user)).resolves.toEqual({ temporaryPassword: 'Senha123' })
    expect(fetch).toHaveBeenCalledWith(
      '/api/users',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(user) }),
    )
  })

  it('informa falta de permissão ao resetar senha', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response(null, { ok: false, status: 403 })))
    await expect(resetUserPassword(10)).rejects.toThrow('Você não possui permissão')
  })

  it('atualiza e exclui usuário pelo identificador', async () => {
    const fetch = vi.fn()
      .mockResolvedValueOnce(response({ id: 10 }))
      .mockResolvedValueOnce(response(null, { status: 204 }))
    vi.stubGlobal('fetch', fetch)

    await updateUser(10, { name: 'Novo nome' })
    await deleteUser(10)

    expect(fetch).toHaveBeenNthCalledWith(
      1,
      '/api/users/10',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ name: 'Novo nome' }),
      }),
    )
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      '/api/users/10',
      expect.objectContaining({ method: 'DELETE' }),
    )
  })
})

