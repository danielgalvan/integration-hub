import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import LoginPage from './LoginPage'

describe('LoginPage', () => {
  it('valida usuário e senha antes de enviar', () => {
    const onLogin = vi.fn()
    render(<LoginPage onLogin={onLogin} />)

    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Informe usuário e senha.',
    )
    expect(onLogin).not.toHaveBeenCalled()
  })

  it('envia as credenciais com ambiente de desenvolvimento por padrão', async () => {
    const onLogin = vi.fn().mockResolvedValue()
    render(<LoginPage onLogin={onLogin} />)

    fireEvent.change(screen.getByLabelText('Usuário'), {
      target: { value: ' admin ' },
    })

    fireEvent.change(screen.getByLabelText('Senha'), {
      target: { value: 'senha' },
    })

    fireEvent.click(
      screen.getByRole('button', { name: 'Entrar' }),
    )

    await waitFor(() => {
      expect(onLogin).toHaveBeenCalledWith({
        username: 'admin',
        password: 'senha',
        environment: 'DEVELOPMENT',
      })
    })
  })

  it('envia o ambiente de homologação quando selecionado', async () => {
    const onLogin = vi.fn().mockResolvedValue()
    render(<LoginPage onLogin={onLogin} />)

    fireEvent.change(screen.getByLabelText('Usuário'), {
      target: { value: 'admin' },
    })

    fireEvent.change(screen.getByLabelText('Senha'), {
      target: { value: 'senha' },
    })

    fireEvent.change(screen.getByLabelText('Ambiente'), {
      target: { value: 'HOMOLOGATION' },
    })

    fireEvent.click(
      screen.getByRole('button', { name: 'Entrar' }),
    )

    await waitFor(() => {
      expect(onLogin).toHaveBeenCalledWith({
        username: 'admin',
        password: 'senha',
        environment: 'HOMOLOGATION',
      })
    })
  })

  it('exibe o erro retornado pelo login', async () => {
    const onLogin = vi.fn().mockRejectedValue(
      new Error('Usuário ou senha inválidos.'),
    )

    render(<LoginPage onLogin={onLogin} />)

    fireEvent.change(screen.getByLabelText('Usuário'), {
      target: { value: 'admin' },
    })

    fireEvent.change(screen.getByLabelText('Senha'), {
      target: { value: 'incorreta' },
    })

    fireEvent.click(
      screen.getByRole('button', { name: 'Entrar' }),
    )

    expect(
      await screen.findByText('Usuário ou senha inválidos.'),
    ).toBeInTheDocument()
  })
})
