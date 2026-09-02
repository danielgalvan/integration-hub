import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import IntegrationForm from './IntegrationForm'

describe('IntegrationForm', () => {
  it('envia integração inativa quando o status é desmarcado', () => {
    const onSubmit = vi.fn()

    render(<IntegrationForm onCancel={vi.fn()} onSubmit={onSubmit} />)

    fireEvent.change(screen.getByLabelText('Nome'), {
      target: { value: 'Clientes' },
    })
    fireEvent.change(screen.getByLabelText('Base Path'), {
      target: { value: '/api/clientes' },
    })
    fireEvent.click(screen.getByLabelText('Ativa'))
    fireEvent.click(screen.getByRole('button', {
      name: 'Salvar integração',
    }))

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Clientes',
      description: '',
      basePath: '/api/clientes',
      active: 'N',
      authType: 'NONE',
    })
  })

  it('desabilita o status ao exibir uma integração em modo leitura', () => {
    render(
      <IntegrationForm
        integration={{
          name: 'Clientes',
          basePath: '/api/clientes',
          active: 'S',
        }}
        readOnly
        onCancel={vi.fn()}
        onSubmit={vi.fn()}
      />,
    )

    expect(screen.getByLabelText('Ativa')).toBeDisabled()
    expect(screen.getByLabelText('Autenticação')).toBeDisabled()
    expect(screen.queryByRole('button', {
      name: 'Salvar alterações',
    })).not.toBeInTheDocument()
  })

  it('envia API Key como tipo de autenticação selecionado', () => {
    const onSubmit = vi.fn()

    render(<IntegrationForm onCancel={vi.fn()} onSubmit={onSubmit} />)

    fireEvent.change(screen.getByLabelText('Nome'), {
      target: { value: 'Pedidos' },
    })
    fireEvent.change(screen.getByLabelText('Base Path'), {
      target: { value: '/api/pedidos' },
    })
    fireEvent.change(screen.getByLabelText('Autenticação'), {
      target: { value: 'API_KEY' },
    })
    fireEvent.click(screen.getByRole('button', {
      name: 'Salvar integração',
    }))

    expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
      authType: 'API_KEY',
    }))
    expect(screen.getByText(
      'Todos os endpoints desta integração exigirão o header X-API-Key.',
    )).toBeInTheDocument()
  })
})

