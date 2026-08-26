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
})
