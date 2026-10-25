
import {
  fireEvent,
  render,
  screen,
} from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import IntegrationList from './IntegrationList'

const integrationWithApiKey = {
  id: 1,
  name: 'Pedidos',
  basePath: '/api/pedidos',
  active: 'S',
  authType: 'API_KEY',
}

function renderList({
  integrations = [integrationWithApiKey],
  canEdit = true,
  onOpenEndpoints = vi.fn(),
  onEdit = vi.fn(),
  onDelete = vi.fn(),
  onGenerateApiKey = vi.fn(),
  onManageCredentials = vi.fn(),
} = {}) {
  render(
    <IntegrationList
      integrations={integrations}
      canEdit={canEdit}
      onOpenEndpoints={onOpenEndpoints}
      onEdit={onEdit}
      onDelete={onDelete}
      onGenerateApiKey={onGenerateApiKey}
      onManageCredentials={onManageCredentials}
    />,
  )
}

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

    renderList({
      integrations: [integration],
      onOpenEndpoints,
    })

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Endpoints',
      }),
    )

    expect(
      onOpenEndpoints,
    ).toHaveBeenCalledWith(integration)
  })

  it('exibe e gera API Key apenas para perfis que podem editar', () => {
    const onGenerateApiKey = vi.fn()

    renderList({
      onGenerateApiKey,
    })

    expect(
      screen.getByText('API Key'),
    ).toBeInTheDocument()

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Gerar API Key',
      }),
    )

    expect(
      onGenerateApiKey,
    ).toHaveBeenCalledWith(integrationWithApiKey)
  })

  it('oculta a geração de API Key para consumidor', () => {
    renderList({
      canEdit: false,
      integrations: [{
        ...integrationWithApiKey,
        apiKeyCreatedAt: '2026-08-29T16:00:00',
      }],
    })

    expect(
      screen.queryByRole('button', {
        name: 'Regenerar API Key',
      }),
    ).not.toBeInTheDocument()

    expect(
      screen.queryByRole('button', {
        name: 'Gerar API Key',
      }),
    ).not.toBeInTheDocument()
  })

  it('abre o gerenciamento de credenciais da integração', () => {
    const onManageCredentials = vi.fn()

    renderList({
      onManageCredentials,
    })

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Credenciais',
      }),
    )

    expect(
      onManageCredentials,
    ).toHaveBeenCalledTimes(1)

    expect(
      onManageCredentials,
    ).toHaveBeenCalledWith(integrationWithApiKey)
  })

  it('oculta o gerenciamento de credenciais para consumidor', () => {
    const onManageCredentials = vi.fn()

    renderList({
      canEdit: false,
      onManageCredentials,
    })

    expect(
      screen.queryByRole('button', {
        name: 'Credenciais',
      }),
    ).not.toBeInTheDocument()

    expect(
      onManageCredentials,
    ).not.toHaveBeenCalled()
  })

  it('oculta o gerenciamento de credenciais quando não utiliza API Key', () => {
    const onManageCredentials = vi.fn()

    renderList({
      integrations: [{
        ...integrationWithApiKey,
        authType: 'NONE',
      }],
      onManageCredentials,
    })

    expect(
      screen.queryByRole('button', {
        name: 'Credenciais',
      }),
    ).not.toBeInTheDocument()

    expect(
      screen.queryByRole('button', {
        name: 'Gerar API Key',
      }),
    ).not.toBeInTheDocument()
  })
})
