import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import IntegrationList from './IntegrationList'

describe('IntegrationList', () => {
  it('informa quando não há integrações cadastradas', () => {
    render(<IntegrationList />)

    expect(
      screen.getByText('Nenhuma integração carregada'),
    ).toBeInTheDocument()
  })

  it('abre os endpoints da integração selecionada', () => {
    const integration = {
      id: 1,
      name: 'Clientes',
      basePath: '/clientes',
      active: 'S',
    }
    const onOpenEndpoints = vi.fn()

    render(
      <IntegrationList
        integrations={[integration]}
        onOpenEndpoints={onOpenEndpoints}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />,
    )

    fireEvent.click(
      screen.getByRole('button', { name: 'Endpoints' }),
    )

    expect(onOpenEndpoints).toHaveBeenCalledWith(integration)
  })
})
