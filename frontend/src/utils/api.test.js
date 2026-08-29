import { afterEach, describe, expect, it, vi } from 'vitest'
import { saveAuth } from './authStorage'
import { apiFetch } from './api'

describe('apiFetch', () => {
  afterEach(() => {
    sessionStorage.clear()
    vi.unstubAllGlobals()
  })

  it('envia o token JWT no cabeçalho Authorization', async () => {
    saveAuth({
      token: 'jwt-token',
      role: 'A',
      environment: 'DEVELOPMENT',
      passwordChangeRequired: false,
    })
    const fetch = vi.fn().mockResolvedValue({ status: 200 })
    vi.stubGlobal('fetch', fetch)

    await apiFetch('/api/integrations', { method: 'GET' })

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8081/api/integrations',
      expect.objectContaining({
        headers: { Authorization: 'Bearer jwt-token' },
      }),
    )
  })

  it('remove o token e notifica a aplicação quando recebe 401', async () => {
    saveAuth({
      token: 'jwt-token',
      role: 'A',
      environment: 'DEVELOPMENT',
      passwordChangeRequired: true,
    })
    const unauthorized = vi.fn()
    window.addEventListener('ihub:unauthorized', unauthorized)
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ status: 401 }))

    await apiFetch('/api/integrations')

    expect(sessionStorage.getItem('ihub_token')).toBeNull()
    expect(sessionStorage.getItem('ihub_role')).toBeNull()
    expect(sessionStorage.getItem('ihub_environment')).toBeNull()
    expect(
      sessionStorage.getItem('ihub_password_change_required'),
    ).toBeNull()
    expect(unauthorized).toHaveBeenCalledOnce()
    window.removeEventListener('ihub:unauthorized', unauthorized)
  })
})
