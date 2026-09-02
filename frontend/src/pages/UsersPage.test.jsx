import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UsersPage from './UsersPage'

const userService = vi.hoisted(() => ({
  createUser: vi.fn(), deleteUser: vi.fn(), getUsers: vi.fn(), resetUserPassword: vi.fn(), updateUser: vi.fn(),
}))

vi.mock('../services/userService', () => userService)
vi.mock('../components/users/UserForm', () => ({
  default: ({ onSave }) => <button type="button" onClick={() => onSave({ username: 'novo', name: 'Novo', email: null, type: 'U' })}>Salvar usuário</button>,
}))
vi.mock('../components/users/TemporaryPasswordDialog', () => ({
  default: ({ password }) => <div>Senha: {password}</div>,
}))

describe('UsersPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    userService.getUsers.mockResolvedValue([])
  })

  it('carrega e exibe usuários', async () => {
    userService.getUsers.mockResolvedValue([{ id: 1, username: 'admin', name: 'Admin', email: null, type: 'A', status: 'A', passwordChangeRequired: false }])
    render(<UsersPage />)

    expect(await screen.findByText('admin')).toBeInTheDocument()
    expect(screen.getByText('Administrador')).toBeInTheDocument()
  })

  it('cria usuário, recarrega a lista e mostra senha temporária', async () => {
    userService.createUser.mockResolvedValue({ temporaryPassword: 'Senha123' })
    render(<UsersPage />)
    await screen.findByText('Nenhum usuário cadastrado.')

    fireEvent.click(screen.getByRole('button', { name: '+ Novo usuário' }))
    fireEvent.click(screen.getByRole('button', { name: 'Salvar usuário' }))

    await waitFor(() => expect(userService.createUser).toHaveBeenCalled())
    expect(await screen.findByText('Senha: Senha123')).toBeInTheDocument()
    expect(userService.getUsers).toHaveBeenCalledTimes(2)
  })

  it('reseta a senha e apresenta a nova senha temporária', async () => {
    userService.getUsers.mockResolvedValue([
      {
        id: 1,
        username: 'admin',
        name: 'Admin',
        email: null,
        type: 'A',
        status: 'A',
        passwordChangeRequired: false,
      },
    ])
    userService.resetUserPassword.mockResolvedValue({
      temporaryPassword: 'NovaSenha123',
    })

    render(<UsersPage />)
    await screen.findByText('admin')
    fireEvent.click(
      screen.getByRole('button', { name: 'Resetar senha' }),
    )

    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveTextContent('resetada')
    fireEvent.click(
      within(dialog).getByRole('button', {
        name: 'Resetar senha',
      }),
    )

    await waitFor(() => expect(
      userService.resetUserPassword,
    ).toHaveBeenCalledWith(1))
    expect(
      await screen.findByText('Senha: NovaSenha123'),
    ).toBeInTheDocument()
  })

  it('exclui o usuário após confirmação', async () => {
    userService.getUsers.mockResolvedValue([
      {
        id: 1,
        username: 'admin',
        name: 'Admin',
        email: null,
        type: 'A',
        status: 'A',
        passwordChangeRequired: false,
      },
    ])
    userService.deleteUser.mockResolvedValue()

    render(<UsersPage />)
    await screen.findByText('admin')
    fireEvent.click(
      screen.getByRole('button', { name: 'Excluir' }),
    )

    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveTextContent('removido permanentemente')
    fireEvent.click(
      within(dialog).getByRole('button', {
        name: 'Excluir',
      }),
    )

    await waitFor(() => expect(
      userService.deleteUser,
    ).toHaveBeenCalledWith(1))
  })

  it('não executa a exclusão ao cancelar a confirmação', async () => {
    userService.getUsers.mockResolvedValue([
      {
        id: 1,
        username: 'admin',
        name: 'Admin',
        email: null,
        type: 'A',
        status: 'A',
        passwordChangeRequired: false,
      },
    ])

    render(<UsersPage />)
    await screen.findByText('admin')
    fireEvent.click(screen.getByRole('button', { name: 'Excluir' }))
    fireEvent.click(
      within(screen.getByRole('dialog')).getByRole('button', {
        name: 'Cancelar',
      }),
    )

    expect(userService.deleteUser).not.toHaveBeenCalled()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})

