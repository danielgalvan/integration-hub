import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'

const authService = vi.hoisted(() => ({
  login: vi.fn(),
}))

const authStorage = vi.hoisted(() => ({
  getToken: vi.fn(),
  removeToken: vi.fn(),
  saveToken: vi.fn(),
}))

vi.mock('./services/authService', () => authService)

vi.mock('./utils/authStorage', () => authStorage)

vi.mock('./pages/LoginPage', () => ({
  default: ({ onLogin }) => (
    <button
      type="button"
      onClick={() =>
        onLogin({
          username: 'admin',
          password: 'senha',
          environment: 'DEVELOPMENT',
        })
      }
    >
      Fazer login
    </button>
  ),
}))

vi.mock('./components/layout/Header', () => ({
  default: ({ onLogout }) => (
    <button
      type="button"
      onClick={onLogout}
    >
      Sair
    </button>
  ),
}))

vi.mock('./components/layout/Sidebar', () => ({
  default: () => <div>Menu</div>,
}))

vi.mock('./pages/IntegrationsPage', () => ({
  default: () => <div>Integrações protegidas</div>,
}))

vi.mock('./pages/EndpointsPage', () => ({
  default: () => <div>Endpoints protegidos</div>,
}))

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    authStorage.getToken.mockReturnValue(null)
  })

  it('armazena o token e libera a área administrativa após login', async () => {
    authService.login.mockResolvedValue({
      token: 'jwt-token',
    })

    render(<App />)

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Fazer login',
      }),
    )

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith(
        'admin',
        'senha',
        'DEVELOPMENT',
      )

      expect(authStorage.saveToken)
        .toHaveBeenCalledWith('jwt-token')
    })

    expect(
      await screen.findByText('Integrações protegidas'),
    ).toBeInTheDocument()
  })

  it('retorna ao login ao receber o evento de não autorizado', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')

    render(<App />)

    expect(
      screen.getByText('Integrações protegidas'),
    ).toBeInTheDocument()

    window.dispatchEvent(
      new Event('ihub:unauthorized'),
    )

    expect(
      await screen.findByRole('button', {
        name: 'Fazer login',
      }),
    ).toBeInTheDocument()

    expect(
      authStorage.removeToken,
    ).toHaveBeenCalledOnce()
  })

  it('remove o token ao sair', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')

    render(<App />)

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Sair',
      }),
    )

    expect(
      await screen.findByRole('button', {
        name: 'Fazer login',
      }),
    ).toBeInTheDocument()

    expect(
      authStorage.removeToken,
    ).toHaveBeenCalledOnce()
  })
})
