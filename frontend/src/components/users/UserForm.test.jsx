import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import UserForm from './UserForm'

describe('UserForm', () => {
  it('valida campos obrigatórios antes de criar', () => {
    render(<UserForm saving={false} onSave={vi.fn()} onCancel={vi.fn()} />)
    fireEvent.click(screen.getByRole('button', { name: 'Criar usuário' }))
    expect(screen.getByRole('alert')).toHaveTextContent('Usuário é obrigatório')
  })

  it('envia usuário novo normalizado sem status', () => {
    const onSave = vi.fn()
    render(<UserForm saving={false} onSave={onSave} onCancel={vi.fn()} />)

    fireEvent.change(screen.getByLabelText('Usuário'), { target: { value: ' novo.usuario ' } })
    fireEvent.change(screen.getByLabelText('Nome'), { target: { value: ' Novo Usuário ' } })
    fireEvent.change(screen.getByLabelText('E-mail'), { target: { value: ' novo@example.com ' } })
    fireEvent.change(screen.getByLabelText('Perfil'), { target: { value: 'C' } })
    fireEvent.click(screen.getByRole('button', { name: 'Criar usuário' }))

    expect(onSave).toHaveBeenCalledWith({
      username: 'novo.usuario', name: 'Novo Usuário', email: 'novo@example.com', type: 'C',
    })
  })
})

