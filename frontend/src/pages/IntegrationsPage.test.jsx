import {
  fireEvent,
  render,
  screen,
  waitFor,
  within,
} from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import IntegrationsPage from './IntegrationsPage'

const integrationService = vi.hoisted(() => ({
  createIntegration: vi.fn(),
  deleteIntegration: vi.fn(),
  generateIntegrationApiKey: vi.fn(),
  getIntegrations: vi.fn(),
  updateIntegration: vi.fn(),
}))

vi.mock('../services/integrationService', () => integrationService)

const integration = {
  id: 1,
  name: 'Clientes',
  description: 'Consulta de clientes',
  basePath: '/clientes',
  active: 'S',
  authType: 'NONE',
}

function renderPage(props = {}) {
  render(
    <IntegrationsPage
      role="A"
      onOpenEndpoints={vi.fn()}
      {...props}
    />,
  )
}

function fillIntegrationForm({
  name = 'Pedidos',
  description = 'Consulta de pedidos',
  basePath = '/pedidos',
} = {}) {
  fireEvent.change(screen.getByLabelText('Nome'), {
    target: { value: name },
  })
  fireEvent.change(screen.getByLabelText('Descrição'), {
    target: { value: description },
  })
  fireEvent.change(screen.getByLabelText('Base Path'), {
    target: { value: basePath },
  })
}

describe('IntegrationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    integrationService.getIntegrations.mockResolvedValue([integration])
    integrationService.createIntegration.mockResolvedValue(integration)
    integrationService.updateIntegration.mockResolvedValue(integration)
    integrationService.deleteIntegration.mockResolvedValue()
    integrationService.generateIntegrationApiKey.mockResolvedValue({
      apiKey: 'ihub_chave_teste',
    })
  })

  it('carrega as integrações da API', async () => {
    renderPage()

    expect(await screen.findByText('Clientes')).toBeInTheDocument()
    expect(integrationService.getIntegrations).toHaveBeenCalledOnce()
  })

  it('exibe o erro retornado ao carregar integrações', async () => {
    integrationService.getIntegrations.mockRejectedValueOnce(
      new Error('Oracle indisponível'),
    )

    renderPage()

    expect(
      await screen.findByText('Oracle indisponível'),
    ).toBeInTheDocument()
  })

  it('cadastra uma integração e recarrega a lista', async () => {
    renderPage()
    await screen.findByText('Clientes')

    fireEvent.click(
      screen.getByRole('button', { name: '+ Nova integração' }),
    )
    fillIntegrationForm()
    fireEvent.click(
      screen.getByRole('button', { name: 'Salvar integração' }),
    )

    await waitFor(() => {
      expect(integrationService.createIntegration).toHaveBeenCalledWith({
        name: 'Pedidos',
        description: 'Consulta de pedidos',
        basePath: '/pedidos',
        active: 'S',
        authType: 'NONE',
      })
    })
    expect(integrationService.getIntegrations).toHaveBeenCalledTimes(2)
  })

  it('edita uma integração existente', async () => {
    renderPage()
    await screen.findByText('Clientes')

    fireEvent.click(screen.getByRole('button', { name: 'Editar' }))
    fillIntegrationForm({ name: 'Clientes ativos' })
    fireEvent.click(
      screen.getByRole('button', { name: 'Salvar alterações' }),
    )

    await waitFor(() => {
      expect(integrationService.updateIntegration).toHaveBeenCalledWith(1, {
        name: 'Clientes ativos',
        description: 'Consulta de pedidos',
        basePath: '/pedidos',
        active: 'S',
        authType: 'NONE',
      })
    })
  })

  it('exclui uma integração após confirmação', async () => {
    renderPage()
    await screen.findByText('Clientes')

    fireEvent.click(screen.getByRole('button', { name: 'Excluir' }))
    fireEvent.click(
      screen.getAllByRole('button', { name: 'Excluir' }).at(-1),
    )

    await waitFor(() => {
      expect(integrationService.deleteIntegration).toHaveBeenCalledWith(1)
    })
    expect(integrationService.getIntegrations).toHaveBeenCalledTimes(2)
  })

  it('permite ao consumidor apenas visualizar a integração', async () => {
    renderPage({ role: 'U' })
    await screen.findByText('Clientes')

    expect(
      screen.queryByRole('button', { name: '+ Nova integração' }),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByRole('button', { name: 'Excluir' }),
    ).not.toBeInTheDocument()

    fireEvent.click(
      screen.getByRole('button', { name: 'Visualizar' }),
    )

    expect(
      screen.getByRole('button', { name: 'Voltar' }),
    ).toBeInTheDocument()
    expect(
      screen.queryByRole('button', { name: 'Salvar alterações' }),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText('Nome')).toHaveAttribute(
      'readonly',
    )
  })

  it('gera API Key após confirmação e a exibe uma única vez', async () => {
    integrationService.getIntegrations.mockResolvedValue([{
      ...integration,
      authType: 'API_KEY',
    }])

    renderPage()
    await screen.findByText('Clientes')

    fireEvent.click(screen.getByRole('button', {
      name: 'Gerar API Key',
    }))
    expect(screen.getByRole('dialog')).toHaveTextContent(
      'Uma API Key será gerada',
    )
    fireEvent.click(
      within(screen.getByRole('dialog')).getByRole('button', {
        name: 'Gerar API Key',
      }),
    )

    await waitFor(() => expect(
      integrationService.generateIntegrationApiKey,
    ).toHaveBeenCalledWith(1))
    expect(await screen.findByText('ihub_chave_teste')).toBeInTheDocument()
    expect(integrationService.getIntegrations).toHaveBeenCalledTimes(2)
  })
})
