import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import ChangePasswordPage from './ChangePasswordPage'

describe('ChangePasswordPage', () => {
  it('valida confirmação e tamanho mínimo da senha', () => {
    render(<ChangePasswordPage onChangePassword={vi.fn()} onLogout={vi.fn()} />)

    fireEvent.change(screen.getByLabelText('Nova senha'), { target: { value: '123' } })
    fireEvent.change(screen.getByLabelText('Confirmar senha'), { target: { value: '123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Alterar senha' }))

    expect(screen.getByRole('alert')).toHaveTextContent('no mínimo 6 caracteres')
  })

  it('envia a nova senha confirmada', async () => {
    const onChangePassword = vi.fn().mockResolvedValue()
    render(<ChangePasswordPage onChangePassword={onChangePassword} onLogout={vi.fn()} />)

    fireEvent.change(screen.getByLabelText('Nova senha'), { target: { value: 'nova-senha' } })
    fireEvent.change(screen.getByLabelText('Confirmar senha'), { target: { value: 'nova-senha' } })
    fireEvent.click(screen.getByRole('button', { name: 'Alterar senha' }))

    await waitFor(() => expect(onChangePassword).toHaveBeenCalledWith('nova-senha'))
  })
})

