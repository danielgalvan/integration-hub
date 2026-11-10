
import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'

import {
  fireEvent,
  render,
  screen,
  waitFor,
  within,
} from '@testing-library/react'

import ExecutionsPage from './ExecutionsPage'

import {
  getExecutions,
  getExecutionById,
} from '../services/executionService'

import {
  getIntegrations,
} from '../services/integrationService'

import {
  getEndpointsByIntegration,
} from '../services/endpointService'

vi.mock('../services/executionService', () => ({
  getExecutions: vi.fn(),
  getExecutionById: vi.fn(),
}))

vi.mock('../services/integrationService', () => ({
  getIntegrations: vi.fn(),
}))

vi.mock('../services/endpointService', () => ({
  getEndpointsByIntegration: vi.fn(),
}))

const mockIntegrations = [
  { id: 1, name: 'Pedidos' },
  { id: 2, name: 'Clientes' },
]

const mockEndpoints = [
  { id: 10, name: 'Buscar pedidos' },
  { id: 11, name: 'Listar pedidos' },
]

const mockExecution = {
  id: 17,
  integrationId: 1,
  integrationName: 'Pedidos',
  endpointId: 10,
  endpointName: 'Buscar pedidos',
  executionStatus: 'SUCCESS',
  httpStatus: 200,
  requestMethod: 'GET',
  requestPath: '/api/pedidos/buscar',
  requestParameters: '{"id":1}',
  durationMs: 169,
  startedAt: '2026-11-10T14:41:05',
  finishedAt: '2026-11-10T14:41:05',
  createdAt: '2026-11-10T14:41:05',
  credentialName: null,
  errorType: null,
  errorMessage: null,
}

const mockErrorExecution = {
  ...mockExecution,
  id: 18,
  executionStatus: 'ERROR',
  httpStatus: 500,
  errorType: 'INTERNAL_ERROR',
  errorMessage: 'Falha ao executar endpoint',
}

const mockPage = {
  content: [
    mockExecution,
    mockErrorExecution,
  ],
  page: 0,
  size: 20,
  totalElements: 2,
  totalPages: 1,
}

function renderPage(role = 'A') {
  return render(
    <ExecutionsPage role={role} />,
  )
}

describe('ExecutionsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    getIntegrations.mockResolvedValue(
      mockIntegrations,
    )

    getEndpointsByIntegration.mockResolvedValue(
      mockEndpoints,
    )

    getExecutions.mockResolvedValue(
      mockPage,
    )

    getExecutionById.mockResolvedValue(
      mockExecution,
    )
  })

  describe('controle de acesso', () => {
    it('deve permitir acesso ao administrador', async () => {
      renderPage('A')

      expect(
        screen.getByRole('heading', {
          name: 'Histórico de execuções',
        }),
      ).toBeInTheDocument()

      expect(
        await screen.findAllByText('Buscar pedidos'),
      ).toHaveLength(2)
    })

    it.each(['C', 'U', null])(
      'deve bloquear acesso para o perfil %s',
      (role) => {
        renderPage(role)

        expect(
          screen.getByText(
            'Você não possui permissão para acessar esta página.',
          ),
        ).toBeInTheDocument()

        expect(
          getExecutions,
        ).not.toHaveBeenCalled()

        expect(
          getIntegrations,
        ).not.toHaveBeenCalled()
      },
    )
  })

  describe('listagem', () => {
    it('deve carregar execuções na montagem', async () => {
      renderPage()

      await waitFor(() => {
        expect(getExecutions).toHaveBeenCalledWith({
          integrationId: '',
          endpointId: '',
          executionStatus: '',
          startedFrom: '',
          startedTo: '',
          page: 0,
          size: 20,
        })
      })

      expect(
        await screen.findAllByText('Sucesso'),
      ).toHaveLength(2)

      expect(
        screen.getAllByText('Erro'),
      ).toHaveLength(2)

      expect(
        screen.getByText('2 execução(ões) encontrada(s)'),
      ).toBeInTheDocument()
    })

    it('deve exibir histórico vazio', async () => {
      getExecutions.mockResolvedValue({
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      })

      renderPage()

      expect(
        await screen.findByText(
          'Nenhuma execução encontrada.',
        ),
      ).toBeInTheDocument()
    })

    it('deve exibir erro ao carregar histórico', async () => {
      getExecutions.mockRejectedValue(
        new Error('Falha na consulta'),
      )

      renderPage()

      expect(
        await screen.findByRole('alert'),
      ).toHaveTextContent(
        'Falha na consulta',
      )

      expect(
        screen.getByText(
          'Nenhuma execução encontrada.',
        ),
      ).toBeInTheDocument()
    })

    it('deve carregar integrações para o filtro', async () => {
      renderPage()

      const select = screen.getByLabelText(
        'Integração',
      )

      await waitFor(() => {
        expect(
          within(select).getByRole('option', {
            name: 'Pedidos',
          }),
        ).toBeInTheDocument()
      })

      expect(
        within(select).getByRole('option', {
          name: 'Clientes',
        }),
      ).toBeInTheDocument()
    })

    it('deve exibir erro ao carregar integrações', async () => {
      getIntegrations.mockRejectedValue(
        new Error('Falha nas integrações'),
      )

      renderPage()

      expect(
        await screen.findByText(
          'Falha nas integrações',
        ),
      ).toBeInTheDocument()
    })
  })

  describe('filtros', () => {
    async function waitForReady() {
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Filtrar' })).not.toBeDisabled()
      })
    }

    async function waitForIntegrations() {
      const select = screen.getByLabelText('Integração')
      await waitFor(() => {
        expect(within(select).getByRole('option', { name: 'Pedidos' })).toBeInTheDocument()
      })
    }

    it('deve filtrar por integração e endpoint', async () => {
      renderPage()
      await waitForReady()
      await waitForIntegrations()

      const integrationSelect =
        screen.getByLabelText('Integração')

      fireEvent.change(integrationSelect, {
        target: { value: '1' },
      })

      expect(integrationSelect).toHaveValue('1')

      await waitFor(() => {
        expect(
          getEndpointsByIntegration,
        ).toHaveBeenCalledWith('1')
      })

      const endpointSelect =
        screen.getByLabelText('Endpoint')

      await waitFor(() => {
        expect(endpointSelect).not.toBeDisabled()
      })

      fireEvent.change(endpointSelect, {
        target: { value: '10' },
      })

      expect(endpointSelect).toHaveValue('10')

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Filtrar',
        }),
      )

      await waitFor(() => {
        expect(getExecutions).toHaveBeenCalledWith(
          expect.objectContaining({
            integrationId: '1',
            endpointId: '10',
          }),
        )
      })
    })

    it('deve filtrar por status', async () => {
      renderPage()
      await waitForReady()

      fireEvent.change(
        screen.getByLabelText('Status'),
        {
          target: { value: 'ERROR' },
        },
      )

      expect(screen.getByLabelText('Status')).toHaveValue('ERROR')

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Filtrar',
        }),
      )

      await waitFor(() => {
        expect(getExecutions).toHaveBeenCalledWith(
          expect.objectContaining({
            executionStatus: 'ERROR',
          }),
        )
      })
    })

    it('deve filtrar por período', async () => {
      renderPage()
      await waitForReady()

      fireEvent.change(
        screen.getByLabelText('Data inicial'),
        {
          target: {
            value: '2026-11-10T08:00',
          },
        },
      )

      fireEvent.change(
        screen.getByLabelText('Data final'),
        {
          target: {
            value: '2026-11-10T18:00',
          },
        },
      )

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Filtrar',
        }),
      )

      await waitFor(() => {
        expect(getExecutions).toHaveBeenCalledWith(
          expect.objectContaining({
            startedFrom: '2026-11-10T08:00',
            startedTo: '2026-11-10T18:00',
          }),
        )
      })
    })

    it('deve limpar filtros', async () => {
      renderPage()
      await waitForReady()

      const statusSelect =
        screen.getByLabelText('Status')

      fireEvent.change(statusSelect, {
        target: { value: 'ERROR' },
      })

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Filtrar',
        }),
      )

      await waitFor(() => {
        expect(getExecutions).toHaveBeenCalledWith(
          expect.objectContaining({
            executionStatus: 'ERROR',
          }),
        )
      })

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Limpar',
        }),
      )

      expect(statusSelect).toHaveValue('')

      await waitFor(() => {
        expect(getExecutions).toHaveBeenLastCalledWith(
          expect.objectContaining({
            executionStatus: '',
            integrationId: '',
            endpointId: '',
            page: 0,
          }),
        )
      })
    })

    it('deve limpar endpoint ao trocar integração', async () => {
      renderPage()
      await waitForReady()
      await waitForIntegrations()

      const integrationSelect =
        screen.getByLabelText('Integração')

      fireEvent.change(integrationSelect, {
        target: { value: '1' },
      })

      expect(integrationSelect).toHaveValue('1')

      await waitFor(() => {
        expect(
          screen.getByLabelText('Endpoint'),
        ).not.toBeDisabled()
      })

      fireEvent.change(
        screen.getByLabelText('Endpoint'),
        {
          target: { value: '10' },
        },
      )

      expect(
        screen.getByLabelText('Endpoint'),
      ).toHaveValue('10')

      fireEvent.change(integrationSelect, {
        target: { value: '2' },
      })

      expect(
        screen.getByLabelText('Endpoint'),
      ).toHaveValue('')
    })

    it('deve exibir erro ao carregar endpoints', async () => {
      getEndpointsByIntegration.mockRejectedValue(
        new Error('Falha nos endpoints'),
      )

      renderPage()
      await waitForReady()
      await waitForIntegrations()

      fireEvent.change(
        screen.getByLabelText('Integração'),
        {
          target: { value: '1' },
        },
      )

      expect(
        await screen.findByText(
          'Falha nos endpoints',
        ),
      ).toBeInTheDocument()
    })
  })

  describe('paginação e atualização', () => {
    it('deve avançar e retornar páginas', async () => {
      getExecutions.mockImplementation(
        async ({ page }) => ({
          ...mockPage,
          page,
          totalElements: 45,
          totalPages: 3,
        }),
      )

      renderPage()

      expect(
        await screen.findByText('Página 1 de 3'),
      ).toBeInTheDocument()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Próxima',
        }),
      )

      await waitFor(() => {
        expect(getExecutions).toHaveBeenCalledWith(
          expect.objectContaining({
            page: 1,
          }),
        )
      })

      expect(
        screen.getByText('Página 2 de 3'),
      ).toBeInTheDocument()

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Anterior',
        }),
      )

      await waitFor(() => {
        expect(getExecutions).toHaveBeenLastCalledWith(
          expect.objectContaining({
            page: 0,
          }),
        )
      })
    })

    it('deve atualizar o histórico manualmente', async () => {
      renderPage()

      await screen.findAllByText('Sucesso')

      const initialCalls =
        getExecutions.mock.calls.length

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Atualizar',
        }),
      )

      await waitFor(() => {
        expect(
          getExecutions.mock.calls.length,
        ).toBeGreaterThan(initialCalls)
      })
    })
  })

  describe('detalhes da execução', () => {
    it('deve abrir modal com detalhes', async () => {
      renderPage()

      const buttons = await screen.findAllByRole(
        'button',
        {
          name: 'Detalhes',
        },
      )

      fireEvent.click(buttons[0])

      const dialog = await screen.findByRole(
        'dialog',
      )

      expect(
        within(dialog).getByText(
          'Detalhes da execução #17',
        ),
      ).toBeInTheDocument()

      expect(
        within(dialog).getByText(
          '/api/pedidos/buscar',
        ),
      ).toBeInTheDocument()

      expect(
        within(dialog).getByText(
          '{"id":1}',
        ),
      ).toBeInTheDocument()

      expect(
        getExecutionById,
      ).toHaveBeenCalledWith(17)
    })

    it('deve fechar modal de detalhes', async () => {
      renderPage()

      const buttons = await screen.findAllByRole(
        'button',
        {
          name: 'Detalhes',
        },
      )

      fireEvent.click(buttons[0])

      await screen.findByRole('dialog')

      fireEvent.click(
        screen.getByRole('button', {
          name: 'Fechar',
        }),
      )

      expect(
        screen.queryByRole('dialog'),
      ).not.toBeInTheDocument()
    })

    it('deve exibir erro ao consultar detalhes', async () => {
      getExecutionById.mockRejectedValue(
        new Error('Execução não encontrada.'),
      )

      renderPage()

      const buttons = await screen.findAllByRole(
        'button',
        {
          name: 'Detalhes',
        },
      )

      fireEvent.click(buttons[0])

      expect(
        await screen.findByText(
          'Execução não encontrada.',
        ),
      ).toBeInTheDocument()

      expect(
        screen.queryByRole('dialog'),
      ).not.toBeInTheDocument()
    })
  })
})
