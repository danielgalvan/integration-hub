import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import TemporaryPasswordDialog from './TemporaryPasswordDialog'

describe('TemporaryPasswordDialog', () => {
  afterEach(() => vi.restoreAllMocks())

  it('copia a senha temporária e informa a confirmação', async () => {
    const writeText = vi.fn().mockResolvedValue()
    Object.assign(navigator, { clipboard: { writeText } })

    render(<TemporaryPasswordDialog password="Senha123" onClose={vi.fn()} />)
    fireEvent.click(screen.getByRole('button', { name: 'Copiar' }))

    await waitFor(() => expect(writeText).toHaveBeenCalledWith('Senha123'))
    expect(screen.getByRole('button', { name: 'Copiado!' })).toBeInTheDocument()
  })

  it('fecha o diálogo ao confirmar leitura', () => {
    const onClose = vi.fn()
    render(<TemporaryPasswordDialog password="Senha123" onClose={onClose} />)

    fireEvent.click(screen.getByRole('button', { name: 'Entendi' }))
    expect(onClose).toHaveBeenCalledOnce()
  })
})

