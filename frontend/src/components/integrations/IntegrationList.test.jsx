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

  it('exibe e gera API Key apenas para perfis que podem editar', () => {
    const integration = {
      id: 1,
      name: 'Pedidos',
      basePath: '/api/pedidos',
      active: 'S',
      authType: 'API_KEY',
    }
    const onGenerateApiKey = vi.fn()

    render(
      <IntegrationList
        integrations={[integration]}
        canEdit
        onOpenEndpoints={vi.fn()}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
        onGenerateApiKey={onGenerateApiKey}
      />,
    )

    expect(screen.getByText('API Key')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', {
      name: 'Gerar API Key',
    }))
    expect(onGenerateApiKey).toHaveBeenCalledWith(integration)
  })

  it('oculta a geração de API Key para consumidor', () => {
    render(
      <IntegrationList
        integrations={[{
          id: 1,
          name: 'Pedidos',
          basePath: '/api/pedidos',
          active: 'S',
          authType: 'API_KEY',
          apiKeyCreatedAt: '2026-08-29T16:00:00',
        }]}
        canEdit={false}
        onOpenEndpoints={vi.fn()}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
        onGenerateApiKey={vi.fn()}
      />,
    )

    expect(screen.queryByRole('button', {
      name: 'Regenerar API Key',
    })).not.toBeInTheDocument()
  })
})
