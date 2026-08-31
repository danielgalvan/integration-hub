import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import LoginPage from './LoginPage'

const environmentService = vi.hoisted(() => ({
  getEnvironments: vi.fn(),
}))

vi.mock('../services/environmentService', () => environmentService)

const environments = [
  { id: 'development', name: 'Desenvolvimento Local' },
  { id: 'cloud', name: 'Oracle Cloud' },
]

async function renderLoadedLogin(onLogin = vi.fn()) {
  render(<LoginPage onLogin={onLogin} />)

  await screen.findByRole('option', {
    name: 'Desenvolvimento Local',
  })

  return onLogin
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    environmentService.getEnvironments.mockResolvedValue(environments)
  })

  it('carrega os ambientes e seleciona o primeiro automaticamente', async () => {
    await renderLoadedLogin()

    expect(screen.getByLabelText('Ambiente')).toHaveValue(
      'development',
    )
    expect(screen.getByRole('option', {
      name: 'Oracle Cloud',
    })).toBeInTheDocument()
  })

  it('mantém o formulário desabilitado enquanto carrega os ambientes', () => {
    environmentService.getEnvironments.mockReturnValue(
      new Promise(() => {}),
    )

    render(<LoginPage onLogin={vi.fn()} />)

    expect(screen.getByLabelText('Usuário')).toBeDisabled()
    expect(screen.getByRole('button', {
      name: 'Entrar',
    })).toBeDisabled()
  })

  it('valida usuário e senha antes de enviar', async () => {
    const onLogin = await renderLoadedLogin()

    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Informe usuário e senha.',
    )
    expect(onLogin).not.toHaveBeenCalled()
  })

  it('envia as credenciais com o primeiro ambiente por padrão', async () => {
    const onLogin = vi.fn().mockResolvedValue()
    await renderLoadedLogin(onLogin)

    fireEvent.change(screen.getByLabelText('Usuário'), {
      target: { value: ' admin ' },
    })
    fireEvent.change(screen.getByLabelText('Senha'), {
      target: { value: 'senha' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() => expect(onLogin).toHaveBeenCalledWith({
      username: 'admin',
      password: 'senha',
      environment: 'development',
    }))
  })

  it('envia o ambiente escolhido pelo usuário', async () => {
    const onLogin = vi.fn().mockResolvedValue()
    await renderLoadedLogin(onLogin)

    fireEvent.change(screen.getByLabelText('Usuário'), {
      target: { value: 'admin' },
    })
    fireEvent.change(screen.getByLabelText('Senha'), {
      target: { value: 'senha' },
    })
    fireEvent.change(screen.getByLabelText('Ambiente'), {
      target: { value: 'cloud' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() => expect(onLogin).toHaveBeenCalledWith({
      username: 'admin',
      password: 'senha',
      environment: 'cloud',
    }))
  })

  it('exibe o erro retornado pelo login', async () => {
    const onLogin = vi.fn().mockRejectedValue(
      new Error('Usuário ou senha inválidos.'),
    )
    await renderLoadedLogin(onLogin)

    fireEvent.change(screen.getByLabelText('Usuário'), {
      target: { value: 'admin' },
    })
    fireEvent.change(screen.getByLabelText('Senha'), {
      target: { value: 'incorreta' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByText(
      'Usuário ou senha inválidos.',
    )).toBeInTheDocument()
  })

  it('informa erro e impede o login quando não carrega ambientes', async () => {
    environmentService.getEnvironments.mockRejectedValue(
      new Error('Não foi possível carregar os ambientes.'),
    )

    render(<LoginPage onLogin={vi.fn()} />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Não foi possível carregar os ambientes.',
    )
    expect(screen.getByRole('button', {
      name: 'Entrar',
    })).toBeDisabled()
  })
})
