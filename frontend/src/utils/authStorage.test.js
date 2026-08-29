import { afterEach, describe, expect, it } from 'vitest'
import {
  clearAuth,
  getEnvironment,
  getRole,
  getToken,
  isAuthenticated,
  isPasswordChangeRequired,
  saveAuth,
  setPasswordChangeRequired,
} from './authStorage'

describe('authStorage', () => {
  afterEach(() => sessionStorage.clear())

  it('salva e recupera todos os dados da sessão', () => {
    saveAuth({
      token: 'jwt-token', role: 'A', environment: 'HOMOLOGATION', passwordChangeRequired: true,
    })

    expect(getToken()).toBe('jwt-token')
    expect(getRole()).toBe('A')
    expect(getEnvironment()).toBe('HOMOLOGATION')
    expect(isPasswordChangeRequired()).toBe(true)
    expect(isAuthenticated()).toBe(true)
  })

  it('altera e limpa o estado de troca obrigatória junto com a sessão', () => {
    saveAuth({ token: 'jwt-token', role: 'U', environment: 'DEVELOPMENT', passwordChangeRequired: true })
    setPasswordChangeRequired(false)
    expect(isPasswordChangeRequired()).toBe(false)

    clearAuth()
    expect(isAuthenticated()).toBe(false)
    expect(getRole()).toBeNull()
    expect(getEnvironment()).toBeNull()
  })
})
