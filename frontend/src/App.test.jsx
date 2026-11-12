
import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from '@testing-library/react'
import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'
import App from './App'

const authService = vi.hoisted(() => ({
  changePassword: vi.fn(),
  getAuthenticatedUser: vi.fn(),
  login: vi.fn(),
}))

const authStorage = vi.hoisted(() => ({
  clearAuth: vi.fn(),
  getEnvironment: vi.fn(),
  getEnvironmentName: vi.fn(),
  getRole: vi.fn(),
  getToken: vi.fn(),
  isPasswordChangeRequired: vi.fn(),
  saveAuth: vi.fn(),
  setPasswordChangeRequired: vi.fn(),
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
          environment: 'development',
          environmentName: 'Desenvolvimento Local',
        })
      }
    >
      Fazer login
    </button>
  ),
}))

vi.mock('./pages/ChangePasswordPage', () => ({
  default: ({ onChangePassword }) => (
    <button
      type="button"
      onClick={() => onChangePassword('nova-senha')}
    >
      Alterar senha obrigatória
    </button>
  ),
}))

vi.mock('./components/layout/Header', () => ({
  default: ({ title = 'Integrações', onLogout }) => (
    <header>
      <h1>{title}</h1>

      <button
        type="button"
        onClick={onLogout}
      >
        Sair
      </button>
    </header>
  ),
}))

vi.mock('./components/layout/Sidebar', () => ({
  default: ({
    role,
    environmentName,
    onOpenIntegrations,
    onOpenExecutions,
    onOpenUsers,
  }) => (
    <nav aria-label="Menu principal">
      <span>{environmentName}</span>

      <button
        type="button"
        onClick={onOpenIntegrations}
      >
        Integrações
      </button>

      {role === 'A' && (
        <>
          <button
            type="button"
            onClick={onOpenExecutions}
          >
            Histórico de execuções
          </button>

          <button
            type="button"
            onClick={onOpenUsers}
          >
            Usuários
          </button>
        </>
      )}
    </nav>
  ),
}))

vi.mock('./pages/IntegrationsPage', () => ({
  default: ({ onOpenEndpoints }) => (
    <div>
      <div>Integrações protegidas</div>

      <button
        type="button"
        onClick={() =>
          onOpenEndpoints({
            id: 1,
            name: 'Integração de teste',
          })
        }
      >
        Abrir endpoints
      </button>
    </div>
  ),
}))

vi.mock('./pages/EndpointsPage', () => ({
  default: ({ integration, onBack }) => (
    <div>
      <div>Endpoints protegidos</div>
      <span>{integration?.name}</span>

      <button
        type="button"
        onClick={onBack}
      >
        Voltar para integrações
      </button>
    </div>
  ),
}))

vi.mock('./pages/ExecutionsPage', () => ({
  default: ({ role }) => (
    <div>
      <div>Histórico protegido</div>
      <span>Perfil: {role}</span>
    </div>
  ),
}))

vi.mock('./pages/UsersPage', () => ({
  default: () => (
    <div>Usuários protegidos</div>
  ),
}))

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    authStorage.getToken.mockReturnValue(null)
    authStorage.getRole.mockReturnValue(null)
    authStorage.getEnvironment.mockReturnValue(null)
    authStorage.getEnvironmentName.mockReturnValue(null)
    authStorage.isPasswordChangeRequired.mockReturnValue(false)
    authStorage.saveAuth.mockImplementation(({ token }) => {
      authStorage.getToken.mockReturnValue(token)
    })
    authStorage.clearAuth.mockImplementation(() => {
      authStorage.getToken.mockReturnValue(null)
    })

    authService.getAuthenticatedUser.mockResolvedValue({
      name: 'Administrador',
      role: 'A',
    })
  })

  async function renderAuthenticatedApp(expectedText = 'Integrações protegidas') {
    render(<App />)
    await screen.findByText(expectedText)
  }

  it('armazena sessão e libera a área administrativa após login', async () => {
    authService.login.mockResolvedValue({
      token: 'header.eyJyb2xlIjoiQSJ9.signature',
      passwordChangeRequired: false,
    })

    render(<App />)

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Fazer login',
      }),
    )

    await waitFor(() =>
      expect(authStorage.saveAuth).toHaveBeenCalledWith({
        token: 'header.eyJyb2xlIjoiQSJ9.signature',
        role: 'A',
        environment: 'development',
        environmentName: 'Desenvolvimento Local',
        passwordChangeRequired: false,
      }),
    )

    expect(
      await screen.findByText('Integrações protegidas'),
    ).toBeInTheDocument()

    expect(
      screen.getByText('Desenvolvimento Local'),
    ).toBeInTheDocument()
  })

  it('restaura o nome do ambiente salvo na sessão', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    authStorage.getEnvironment.mockReturnValue('cloud')
    authStorage.getEnvironmentName.mockReturnValue('Oracle Cloud')

    await renderAuthenticatedApp()

    expect(
      screen.getByText('Oracle Cloud'),
    ).toBeInTheDocument()
  })

  it('exige a troca de senha quando indicada no login', async () => {
    authService.login.mockResolvedValue({
      token: 'header.eyJyb2xlIjoiQSJ9.signature',
      passwordChangeRequired: true,
    })

    render(<App />)

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Fazer login',
      }),
    )

    expect(
      await screen.findByRole('button', {
        name: 'Alterar senha obrigatória',
      }),
    ).toBeInTheDocument()
  })

  it('encerra a exigência de troca após alterar a senha', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    authStorage.isPasswordChangeRequired.mockReturnValue(true)
    authService.changePassword.mockResolvedValue()

    await renderAuthenticatedApp('Alterar senha obrigatória')

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Alterar senha obrigatória',
      }),
    )

    await waitFor(() =>
      expect(authService.changePassword).toHaveBeenCalledWith(
        'jwt-token',
        'nova-senha',
      ),
    )

    expect(
      authStorage.setPasswordChangeRequired,
    ).toHaveBeenCalledWith(false)

    expect(
      screen.getByText('Integrações protegidas'),
    ).toBeInTheDocument()
  })

  it('abre usuários somente para administrador', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    authStorage.getRole.mockReturnValue('A')

    await renderAuthenticatedApp()

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Usuários',
      }),
    )

    expect(
      screen.getByText('Usuários protegidos'),
    ).toBeInTheDocument()
  })

  it('remove a sessão ao receber evento de não autorizado', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')

    await renderAuthenticatedApp()

    window.dispatchEvent(
      new Event('ihub:unauthorized'),
    )

    expect(
      await screen.findByRole('button', {
        name: 'Fazer login',
      }),
    ).toBeInTheDocument()

    expect(
      authStorage.clearAuth,
    ).toHaveBeenCalledOnce()
  })

  it('carrega os dados do usuário autenticado após restaurar a sessão', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')

    authService.getAuthenticatedUser.mockResolvedValue({
      name: 'Daniel Galvan',
      role: 'A',
    })

    render(<App />)

    await waitFor(() =>
      expect(
        authService.getAuthenticatedUser,
      ).toHaveBeenCalledOnce(),
    )
  })

  it('mantém as telas protegidas bloqueadas até validar o token', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    let resolveValidation
    authService.getAuthenticatedUser.mockReturnValue(
      new Promise((resolve) => {
        resolveValidation = resolve
      }),
    )

    render(<App />)

    expect(screen.getByRole('status')).toHaveTextContent('Verificando sessão')
    expect(screen.queryByText('Integrações protegidas')).not.toBeInTheDocument()

    await act(async () => {
      resolveValidation({ name: 'Daniel Galvan', role: 'A' })
    })

    expect(await screen.findByText('Integrações protegidas')).toBeInTheDocument()
  })

  it('mantém a sessão bloqueada quando o servidor falha e permite tentar novamente', async () => {
    authStorage.getToken.mockReturnValue('jwt-token')
    authService.getAuthenticatedUser
      .mockRejectedValueOnce(new Error('Servidor indisponível'))
      .mockResolvedValueOnce({ name: 'Daniel Galvan', role: 'A' })

    render(<App />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Não foi possível validar sua sessão',
    )
    expect(screen.queryByText('Integrações protegidas')).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Tentar novamente' }))

    expect(await screen.findByText('Integrações protegidas')).toBeInTheDocument()
    expect(authService.getAuthenticatedUser).toHaveBeenCalledTimes(2)
  })

  describe('Histórico de execuções', () => {
    it('exibe o menu de histórico para administrador', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      expect(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      ).toBeInTheDocument()
    })

    it('abre a página de histórico ao clicar no menu', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )

      expect(
        screen.getByText('Histórico protegido'),
      ).toBeInTheDocument()

      expect(
        screen.getByText('Perfil: A'),
      ).toBeInTheDocument()

      expect(
        screen.queryByText('Integrações protegidas'),
      ).not.toBeInTheDocument()
    })

    it('retorna do histórico para integrações', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )

      expect(
        screen.getByText('Histórico protegido'),
      ).toBeInTheDocument()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Integrações',
        }),
      )

      expect(
        screen.getByText('Integrações protegidas'),
      ).toBeInTheDocument()

      expect(
        screen.queryByText('Histórico protegido'),
      ).not.toBeInTheDocument()
    })

    it('permite navegar do histórico para usuários', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Usuários',
        }),
      )

      expect(
        screen.getByText('Usuários protegidos'),
      ).toBeInTheDocument()

      expect(
        screen.queryByText('Histórico protegido'),
      ).not.toBeInTheDocument()
    })

    it('preserva a navegação de integrações para endpoints', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Abrir endpoints',
        }),
      )

      expect(
        screen.getByText('Endpoints protegidos'),
      ).toBeInTheDocument()

      expect(
        screen.getByText('Integração de teste'),
      ).toBeInTheDocument()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )

      expect(
        screen.getByText('Histórico protegido'),
      ).toBeInTheDocument()

      expect(
        screen.queryByText('Endpoints protegidos'),
      ).not.toBeInTheDocument()
    })

    it.each(['C', 'U', null])(
      'não permite acessar o histórico para o perfil %s',
      async (role) => {
        authStorage.getToken.mockReturnValue('jwt-token')
        authStorage.getRole.mockReturnValue(role)

        await renderAuthenticatedApp()

        expect(
          screen.queryByRole('button', {
            name: 'Histórico de execuções',
          }),
        ).not.toBeInTheDocument()

        expect(
          screen.queryByText('Histórico protegido'),
        ).not.toBeInTheDocument()

        expect(
          screen.getByText('Integrações protegidas'),
        ).toBeInTheDocument()
      },
    )

    it('não exibe o histórico após encerrar a sessão', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )

      expect(
        screen.getByText('Histórico protegido'),
      ).toBeInTheDocument()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Sair',
        }),
      )

      expect(
        screen.getByRole('button', {
          name: 'Fazer login',
        }),
      ).toBeInTheDocument()

      expect(
        screen.queryByText('Histórico protegido'),
      ).not.toBeInTheDocument()

      expect(
        authStorage.clearAuth,
      ).toHaveBeenCalledOnce()
    })
  })

  describe('Cabeçalho dinâmico', () => {
    it('exibe Integrações como título inicial', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Integrações',
        }),
      ).toBeInTheDocument()
    })

    it('altera o título ao abrir os endpoints', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Abrir endpoints',
        }),
      )

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Endpoints',
        }),
      ).toBeInTheDocument()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Voltar para integrações',
        }),
      )

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Integrações',
        }),
      ).toBeInTheDocument()
    })

    it('altera o título ao abrir o histórico de execuções', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Histórico de execuções',
        }),
      ).toBeInTheDocument()

      expect(
        screen.queryByRole('heading', {
          level: 1,
          name: 'Integrações',
        }),
      ).not.toBeInTheDocument()
    })

    it('altera o título ao abrir usuários', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Usuários',
        }),
      )

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Usuários',
        }),
      ).toBeInTheDocument()
    })

    it('atualiza o título ao navegar entre páginas', async () => {
      authStorage.getToken.mockReturnValue('jwt-token')
      authStorage.getRole.mockReturnValue('A')

      await renderAuthenticatedApp()

      const expectTitle = (title) => {
        expect(
          screen.getByRole('heading', {
            level: 1,
            name: title,
          }),
        ).toBeInTheDocument()
      }

      expectTitle('Integrações')

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Abrir endpoints',
        }),
      )
      expectTitle('Endpoints')

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )
      expectTitle('Histórico de execuções')

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Usuários',
        }),
      )
      expectTitle('Usuários')

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Integrações',
        }),
      )
      expectTitle('Integrações')
    })
  })
})
