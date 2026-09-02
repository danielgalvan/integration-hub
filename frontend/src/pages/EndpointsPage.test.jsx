import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import EndpointsPage from './EndpointsPage'

const endpointService = vi.hoisted(() => ({
  createEndpoint: vi.fn(),
  deleteEndpoint: vi.fn(),
  getEndpointsByIntegration: vi.fn(),
  updateEndpoint: vi.fn(),
}))

vi.mock('../services/endpointService', () => endpointService)

const integration = { id: 1, name: 'Clientes' }
const endpoint = {
  id: 10,
  integrationId: 1,
  name: 'Buscar cliente',
  description: 'Busca por código',
  path: '/buscar',
  method: 'GET',
  sqlText: 'select id from cliente',
  parameters: [],
  active: 'S',
}

function renderPage(props = {}) {
  render(
    <EndpointsPage
      role="A"
      integration={integration}
      onBack={vi.fn()}
      {...props}
    />,
  )
}

function fillEndpointForm({
  name = 'Listar clientes',
  description = 'Lista todos',
  path = '/listar',
  sqlText = 'select id from cliente',
} = {}) {
  fireEvent.change(screen.getByLabelText('Nome'), {
    target: { value: name },
  })
  fireEvent.change(screen.getByLabelText('Descrição'), {
    target: { value: description },
  })
  fireEvent.change(screen.getByLabelText('Path'), {
    target: { value: path },
  })
  fireEvent.change(screen.getByLabelText('SQL'), {
    target: { value: sqlText },
  })
}

describe('EndpointsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    endpointService.getEndpointsByIntegration.mockResolvedValue([endpoint])
    endpointService.createEndpoint.mockResolvedValue(endpoint)
    endpointService.updateEndpoint.mockResolvedValue(endpoint)
    endpointService.deleteEndpoint.mockResolvedValue()
  })

  it('solicita os endpoints da integração selecionada', async () => {
    renderPage()

    expect(await screen.findByText('Buscar cliente')).toBeInTheDocument()
    expect(endpointService.getEndpointsByIntegration).toHaveBeenCalledWith(1)
  })

  it('exibe uma orientação sem integração selecionada', () => {
    renderPage({ integration: null })

    expect(
      screen.getByText('Selecione uma integração para visualizar seus endpoints.'),
    ).toBeInTheDocument()
    expect(endpointService.getEndpointsByIntegration).not.toHaveBeenCalled()
  })

  it('exibe o erro retornado ao carregar endpoints', async () => {
    endpointService.getEndpointsByIntegration.mockRejectedValueOnce(
      new Error('Não foi possível consultar endpoints'),
    )

    renderPage()

    expect(
      await screen.findByText('Não foi possível consultar endpoints'),
    ).toBeInTheDocument()
  })

  it('cadastra um endpoint e recarrega a lista', async () => {
    renderPage()
    await screen.findByText('Buscar cliente')

    fireEvent.click(
      screen.getByRole('button', { name: '+ Novo endpoint' }),
    )
    fillEndpointForm()
    fireEvent.click(
      screen.getByRole('button', { name: 'Salvar endpoint' }),
    )

    await waitFor(() => {
      expect(endpointService.createEndpoint).toHaveBeenCalledWith({
        integrationId: 1,
        name: 'Listar clientes',
        description: 'Lista todos',
        path: '/listar',
        method: 'GET',
        sqlText: 'select id from cliente',
        parameters: [],
        active: 'S',
      })
    })
  })

  it('edita um endpoint existente', async () => {
    renderPage()
    await screen.findByText('Buscar cliente')

    fireEvent.click(screen.getByRole('button', { name: 'Editar' }))
    fillEndpointForm({ name: 'Buscar cliente ativo' })
    fireEvent.click(
      screen.getByRole('button', { name: 'Salvar alterações' }),
    )

    await waitFor(() => {
      expect(endpointService.updateEndpoint).toHaveBeenCalledWith(10, {
        integrationId: 1,
        name: 'Buscar cliente ativo',
        description: 'Lista todos',
        path: '/listar',
        method: 'GET',
        sqlText: 'select id from cliente',
        parameters: [],
        active: 'S',
      })
    })
  })

  it('exclui um endpoint após confirmação', async () => {
    renderPage()
    await screen.findByText('Buscar cliente')

    fireEvent.click(screen.getByRole('button', { name: 'Excluir' }))
    fireEvent.click(
      screen.getAllByRole('button', { name: 'Excluir' }).at(-1),
    )

    await waitFor(() => {
      expect(endpointService.deleteEndpoint).toHaveBeenCalledWith(10)
    })
  })

  it('permite ao consumidor apenas visualizar endpoint', async () => {
    renderPage({ role: 'U' })
    await screen.findByText('Buscar cliente')

    expect(
      screen.queryByRole('button', { name: '+ Novo endpoint' }),
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
    expect(screen.getByLabelText('SQL')).toHaveAttribute(
      'readonly',
    )
  })
})

