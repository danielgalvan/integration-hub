import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import MessageDialog from './MessageDialog'

describe('MessageDialog', () => {
  it('não renderiza enquanto estiver fechado', () => {
    render(<MessageDialog open={false} onClose={vi.fn()} />)

    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
  })

  it('exibe a mensagem e fecha pelo botão OK', () => {
    const onClose = vi.fn()

    render(
      <MessageDialog
        open
        title="Falha ao salvar"
        message="Tente novamente."
        onClose={onClose}
      />,
    )

    expect(screen.getByRole('alertdialog')).toHaveTextContent(
      'Falha ao salvar',
    )
    fireEvent.click(screen.getByRole('button', { name: 'OK' }))

    expect(onClose).toHaveBeenCalledOnce()
  })
})
