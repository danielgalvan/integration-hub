import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'

const authService = vi.hoisted(() => ({
  changePassword: vi.fn(), login: vi.fn(),
}))
const authStorage = vi.hoisted(() => ({
  clearAuth: vi.fn(), getEnvironment: vi.fn(), getRole: vi.fn(), getToken: vi.fn(),
  isPasswordChangeRequired: vi.fn(), saveAuth: vi.fn(), setPasswordChangeRequired: vi.fn(),
}))

vi.mock('./services/authService', () => authService)
vi.mock('./utils/authStorage', () => authStorage)
vi.mock('./pages/LoginPage', () => ({
  default: ({ onLogin }) => <button type="button" onClick={() => onLogin({ username: 'admin', password: 'senha', environment: 'DEVELOPMENT' })}>Fazer login</button>,
}))
vi.mock('./pages/ChangePasswordPage', () => ({
  default: ({ onChangePassword }) => <button type="button" onClick={() => onChangePassword('nova-senha')}>Alterar senha obrigatória</button>,
}))
vi.mock('./components/layout/Header', () => ({
  default: ({ onLogout }) => <button type="button" onClick={onLogout}>Sair</button>,
}))
vi.mock('./components/layout/Sidebar', () => ({
  default: ({ onOpenUsers }) => <button type="button" onClick={onOpenUsers}>Usuários</button>,
}))
vi.mock('./pages/IntegrationsPage', () => ({ default: () => <div>Integrações protegidas</div> }))
vi.mock('./pages/EndpointsPage', () => ({ default: () => <div>Endpoints protegidos</div> }))
vi.mock('./pages/UsersPage', () => ({ default: () => <div>Usuários protegidos</div> }))

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    authStorage.getToken.mockReturnValue(null)
    authStorage.getRole.mockReturnValue(null)
    authStorage.getEnvironment.mockReturnValue(null)
    authStorage.isPasswordChangeRequired.mockReturnValue(false)
  })

  it('armazena sessão e libera a área administrativa após login', async () => {
    authService.login.mockResolvedValue({ token: 'header.eyJyb2xlIjoiQSJ9.signature', passwordChangeRequired: false })
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Fazer login' }))

    await waitFor(() => expect(authStorage.saveAuth).toHaveBeenCalledWith({
      token: 'header.eyJyb2xlIjoiQSJ9.signature', role: 'A', environment: 'DEVELOPMENT', passwordChangeRequired: false,
    }))
    expect(await screen.findByText('Integrações protegidas')).toBeInTheDocument()
  })

  it('exige a troca de senha quando indicada no login', async () => {
    authService.login.mockResolvedValue({ token: 'header.eyJyb2xlIjoiQSJ9.signature', passwordChangeRequired: true })
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Fazer login' }))
    expect(await screen.findByRole('button', { name: 'Alterar senha obrigatória' })).toBeInTheDocument()
  })

  it('encerra a exigência de troca após alterar a senha', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    authStorage.isPasswordChangeRequired.mockReturnValue(true)
    authService.changePassword.mockResolvedValue()
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Alterar senha obrigatória' }))

    await waitFor(() => expect(authService.changePassword).toHaveBeenCalledWith('jwt-token', 'nova-senha'))
    expect(authStorage.setPasswordChangeRequired).toHaveBeenCalledWith(false)
    expect(screen.getByText('Integrações protegidas')).toBeInTheDocument()
  })

  it('abre usuários somente para administrador', () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    authStorage.getRole.mockReturnValue('A')
    render(<App />)
    fireEvent.click(screen.getByRole('button', { name: 'Usuários' }))
    expect(screen.getByText('Usuários protegidos')).toBeInTheDocument()
  })

  it('remove a sessão ao receber evento de não autorizado', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    render(<App />)
    window.dispatchEvent(new Event('ihub:unauthorized'))
    expect(await screen.findByRole('button', { name: 'Fazer login' })).toBeInTheDocument()
    expect(authStorage.clearAuth).toHaveBeenCalledOnce()
  })
})
