import { afterEach, describe, expect, it, vi } from 'vitest'
import { saveToken } from './authStorage'
import { apiFetch } from './api'

describe('apiFetch', () => {
  afterEach(() => {
    sessionStorage.clear()
    vi.unstubAllGlobals()
  })

  it('envia o token JWT no cabeçalho Authorization', async () => {
    saveToken('jwt-token')
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
    saveToken('jwt-token')
    const unauthorized = vi.fn()
    window.addEventListener('ihub:unauthorized', unauthorized)
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ status: 401 }))

    await apiFetch('/api/integrations')

    expect(sessionStorage.getItem('ihub_token')).toBeNull()
    expect(unauthorized).toHaveBeenCalledOnce()
    window.removeEventListener('ihub:unauthorized', unauthorized)
  })
})
