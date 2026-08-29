import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import ApiKeyDialog from './ApiKeyDialog'

describe('ApiKeyDialog', () => {
  afterEach(() => vi.restoreAllMocks())

  it('exibe e copia a API Key com o header de consumo', async () => {
    const writeText = vi.fn().mockResolvedValue()
    Object.assign(navigator, { clipboard: { writeText } })

    render(
      <ApiKeyDialog
        apiKey="ihub_chave_teste"
        onClose={vi.fn()}
      />,
    )

    expect(screen.getByText('X-API-Key: ihub_chave_teste'))
      .toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Copiar' }))

    await waitFor(() => expect(writeText)
      .toHaveBeenCalledWith('ihub_chave_teste'))
    expect(screen.getByRole('button', { name: 'Copiado!' }))
      .toBeInTheDocument()
  })

  it('fecha pelo botão principal ou pelo ícone de fechar', () => {
    const onClose = vi.fn()

    render(
      <ApiKeyDialog
        apiKey="ihub_chave_teste"
        onClose={onClose}
      />,
    )

    const closeButtons = screen.getAllByRole('button', {
      name: 'Fechar',
    })

    fireEvent.click(closeButtons[0])
    fireEvent.click(closeButtons[1])

    expect(onClose).toHaveBeenCalledTimes(2)
  })
})
