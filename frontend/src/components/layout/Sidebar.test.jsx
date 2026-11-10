
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import Sidebar from './Sidebar'

describe('Sidebar', () => {
  it('abre a tela de integrações ao clicar em Integrações', () => {
    const onOpenIntegrations = vi.fn()

    render(
      <Sidebar
        onOpenIntegrations={onOpenIntegrations}
      />,
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: /Integrações/,
      }),
    )

    expect(
      onOpenIntegrations,
    ).toHaveBeenCalledOnce()
  })

  it('não exibe um item de endpoints na navegação lateral', () => {
    render(
      <Sidebar
        onOpenIntegrations={vi.fn()}
      />,
    )

    expect(
      screen.queryByRole('button', {
        name: 'Endpoints',
      }),
    ).not.toBeInTheDocument()
  })

  it('exibe a administração de usuários somente para administrador', () => {
    const onOpenUsers = vi.fn()

    render(
      <Sidebar
        role="A"
        environmentName="Ambiente de Homologação"
        onOpenIntegrations={vi.fn()}
        onOpenExecutions={vi.fn()}
        onOpenUsers={onOpenUsers}
      />,
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: /Users/,
      }),
    )

    expect(
      onOpenUsers,
    ).toHaveBeenCalledOnce()

    expect(
      screen.getByText(
        'Ambiente de Homologação',
      ),
    ).toBeInTheDocument()
  })

  it('oculta a administração de usuários para consumidor', () => {
    render(
      <Sidebar
        role="U"
        onOpenIntegrations={vi.fn()}
        onOpenExecutions={vi.fn()}
        onOpenUsers={vi.fn()}
      />,
    )

    expect(
      screen.queryByRole('button', {
        name: /Users/,
      }),
    ).not.toBeInTheDocument()
  })

  it('exibe diretamente o nome do ambiente recebido', () => {
    render(
      <Sidebar
        environmentName="Oracle Cloud"
        onOpenIntegrations={vi.fn()}
      />,
    )

    expect(
      screen.getByText('Oracle Cloud'),
    ).toBeInTheDocument()
  })

  describe('Histórico de execuções', () => {
    it('exibe o histórico de execuções para administrador', () => {
      render(
        <Sidebar
          role="A"
          onOpenIntegrations={vi.fn()}
          onOpenExecutions={vi.fn()}
          onOpenUsers={vi.fn()}
        />,
      )

      expect(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      ).toBeInTheDocument()
    })

    it('abre o histórico de execuções ao clicar no item', () => {
      const onOpenExecutions = vi.fn()

      render(
        <Sidebar
          role="A"
          onOpenIntegrations={vi.fn()}
          onOpenExecutions={onOpenExecutions}
          onOpenUsers={vi.fn()}
        />,
      )

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Histórico de execuções',
        }),
      )

      expect(
        onOpenExecutions,
      ).toHaveBeenCalledOnce()
    })

    it.each(['C', 'U', null])(
      'oculta o histórico de execuções para o perfil %s',
      (role) => {
        const onOpenExecutions = vi.fn()

        render(
          <Sidebar
            role={role}
            onOpenIntegrations={vi.fn()}
            onOpenExecutions={onOpenExecutions}
            onOpenUsers={vi.fn()}
          />,
        )

        expect(
          screen.queryByRole('button', {
            name: 'Histórico de execuções',
          }),
        ).not.toBeInTheDocument()

        expect(
          onOpenExecutions,
        ).not.toHaveBeenCalled()
      },
    )
  })
})
