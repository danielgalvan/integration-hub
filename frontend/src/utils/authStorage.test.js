import { afterEach, describe, expect, it } from 'vitest'
import { getToken, removeToken, saveToken } from './authStorage'

describe('authStorage', () => {
  afterEach(() => sessionStorage.clear())

  it('salva, recupera e remove o token da sessão', () => {
    saveToken('jwt-token')
    expect(getToken()).toBe('jwt-token')

    removeToken()
    expect(getToken()).toBeNull()
  })
})
