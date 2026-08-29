import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
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

    expect(screen.getByText('Daniel Galvan')).toBeInTheDocument()
    expect(screen.getByText('DG')).toBeInTheDocument()
    expect(screen.getByText('Administrador')).toBeInTheDocument()
  })

  it('usa o username e permite encerrar a sessão', () => {
    const onLogout = vi.fn()

    render(
      <Header
        user={{ username: 'criador', role: 'C' }}
        onLogout={onLogout}
      />,
    )

    expect(screen.getByText('criador')).toBeInTheDocument()
    expect(screen.getByText('CR')).toBeInTheDocument()
    expect(screen.getByText('Criador')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Sair' }))
    expect(onLogout).toHaveBeenCalledOnce()
  })
})
