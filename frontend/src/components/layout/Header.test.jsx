
import {
  fireEvent,
  render,
  screen,
} from '@testing-library/react'
import {
  describe,
  expect,
  it,
  vi,
} from 'vitest'
import Header from './Header'

describe('Header', () => {
  it('exibe nome, iniciais e perfil do usuário autenticado', () => {
    render(
      <Header
        user={{
          name: 'Daniel Galvan',
          role: 'A',
        }}
        onLogout={vi.fn()}
      />,
    )

    expect(
      screen.getByText('Daniel Galvan'),
    ).toBeInTheDocument()

    expect(
      screen.getByText('DG'),
    ).toBeInTheDocument()

    expect(
      screen.getByText('Administrador'),
    ).toBeInTheDocument()
  })

  it('usa o username e permite encerrar a sessão', () => {
    const onLogout = vi.fn()

    render(
      <Header
        user={{
          username: 'criador',
          role: 'C',
        }}
        onLogout={onLogout}
      />,
    )

    expect(
      screen.getByText('criador'),
    ).toBeInTheDocument()

    expect(
      screen.getByText('CR'),
    ).toBeInTheDocument()

    expect(
      screen.getByText('Criador'),
    ).toBeInTheDocument()

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Sair',
      }),
    )

    expect(
      onLogout,
    ).toHaveBeenCalledOnce()
  })

  describe('Título dinâmico', () => {
    it('exibe Integrações como título padrão', () => {
      render(
        <Header
          user={{
            name: 'Administrador',
            role: 'A',
          }}
          onLogout={vi.fn()}
        />,
      )

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Integrações',
        }),
      ).toBeInTheDocument()
    })

    it('exibe o título personalizado recebido', () => {
      render(
        <Header
          user={{
            name: 'Administrador',
            role: 'A',
          }}
          title="Histórico de execuções"
          onLogout={vi.fn()}
        />,
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

    it('atualiza o título quando a propriedade muda', () => {
      const user = {
        name: 'Administrador',
        role: 'A',
      }

      const onLogout = vi.fn()

      const { rerender } = render(
        <Header
          user={user}
          title="Integrações"
          onLogout={onLogout}
        />,
      )

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Integrações',
        }),
      ).toBeInTheDocument()

      rerender(
        <Header
          user={user}
          title="Histórico de execuções"
          onLogout={onLogout}
        />,
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

    it('preserva usuário, perfil e logout com título personalizado', () => {
      const onLogout = vi.fn()

      render(
        <Header
          user={{
            name: 'Daniel Galvan',
            role: 'A',
          }}
          title="Usuários"
          onLogout={onLogout}
        />,
      )

      expect(
        screen.getByRole('heading', {
          level: 1,
          name: 'Usuários',
        }),
      ).toBeInTheDocument()

      expect(
        screen.getByText('Integration Hub'),
      ).toBeInTheDocument()

      expect(
        screen.getByText('Daniel Galvan'),
      ).toBeInTheDocument()

      expect(
        screen.getByText('DG'),
      ).toBeInTheDocument()

      expect(
        screen.getByText('Administrador'),
      ).toBeInTheDocument()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Sair',
        }),
      )

      expect(
        onLogout,
      ).toHaveBeenCalledOnce()
    })
  })
})
