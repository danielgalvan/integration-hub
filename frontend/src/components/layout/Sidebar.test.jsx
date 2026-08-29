import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import Sidebar from './Sidebar'

describe('Sidebar', () => {
  it('abre a tela de integrações ao clicar em Integrações', () => {
    const onOpenIntegrations = vi.fn()

    render(<Sidebar onOpenIntegrations={onOpenIntegrations} />)

    fireEvent.click(
      screen.getByRole('button', { name: /Integrações/ }),
    )

    expect(onOpenIntegrations).toHaveBeenCalledOnce()
  })

  it('não exibe um item de endpoints na navegação lateral', () => {
    render(<Sidebar onOpenIntegrations={vi.fn()} />)

    expect(
      screen.queryByRole('button', { name: 'Endpoints' }),
    ).not.toBeInTheDocument()
  })

  it('exibe a administração de usuários somente para administrador', () => {
    const onOpenUsers = vi.fn()

    render(
      <Sidebar
        role="A"
        environment="HOMOLOGATION"
        onOpenIntegrations={vi.fn()}
        onOpenUsers={onOpenUsers}
      />,
    )

    fireEvent.click(
      screen.getByRole('button', { name: /Users/ }),
    )

    expect(onOpenUsers).toHaveBeenCalledOnce()
    expect(screen.getByText('Homologação')).toBeInTheDocument()
  })

  it('oculta a administração de usuários para consumidor', () => {
    render(
      <Sidebar
        role="U"
        onOpenIntegrations={vi.fn()}
        onOpenUsers={vi.fn()}
      />,
    )

    expect(
      screen.queryByRole('button', { name: /Users/ }),
    ).not.toBeInTheDocument()
  })
})
