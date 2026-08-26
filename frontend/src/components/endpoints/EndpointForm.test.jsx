import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import EndpointForm from './EndpointForm'

function fillRequiredFields() {
  fireEvent.change(screen.getByLabelText('Nome'), {
    target: { value: 'Buscar cliente' },
  })
  fireEvent.change(screen.getByLabelText('Path'), {
    target: { value: '/buscar' },
  })
}

describe('EndpointForm', () => {
  it('gera parâmetros únicos a partir do SQL', () => {
    render(<EndpointForm integrationId={1} onCancel={vi.fn()} onSubmit={vi.fn()} />)

    fireEvent.change(screen.getByLabelText('SQL'), {
      target: { value: 'select * from cliente where id = :id or pai_id = :id and status = :status' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Gerar parâmetros' }))

    expect(screen.getByDisplayValue('id')).toBeInTheDocument()
    expect(screen.getByDisplayValue('status')).toBeInTheDocument()
  })

  it('impede o envio quando os parâmetros não correspondem ao SQL', () => {
    const onValidationError = vi.fn()
    const onSubmit = vi.fn()

    render(
      <EndpointForm
        integrationId={1}
        onCancel={vi.fn()}
        onSubmit={onSubmit}
        onValidationError={onValidationError}
      />,
    )
    fillRequiredFields()
    fireEvent.change(screen.getByLabelText('SQL'), {
      target: { value: 'select * from cliente where id = :id' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar endpoint' }))

    expect(onSubmit).not.toHaveBeenCalled()
    expect(onValidationError).toHaveBeenCalledWith(
      'O SQL foi alterado. Gere novamente os parâmetros antes de salvar.',
    )
  })

  it('envia os parâmetros configurados ao salvar', () => {
    const onSubmit = vi.fn()

    render(<EndpointForm integrationId={7} onCancel={vi.fn()} onSubmit={onSubmit} />)
    fillRequiredFields()
    fireEvent.change(screen.getByLabelText('SQL'), {
      target: { value: 'select * from cliente where id = :id' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Gerar parâmetros' }))
    fireEvent.change(screen.getByLabelText('Tipo'), {
      target: { value: 'NUMBER' },
    })
    fireEvent.click(screen.getByLabelText('Obrigatório'))
    fireEvent.click(screen.getByRole('button', { name: 'Salvar endpoint' }))

    expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
      integrationId: 7,
      name: 'Buscar cliente',
      path: '/buscar',
      parameters: [{ name: 'id', type: 'NUMBER', required: true }],
    }))
  })
})
