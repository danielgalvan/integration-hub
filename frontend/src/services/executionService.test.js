
import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'

import { apiFetch } from '../utils/api'

import {
  getExecutions,
  getExecutionById,
} from './executionService'

vi.mock('../utils/api', () => ({
  apiFetch: vi.fn(),
}))

const mockExecution = {
  id: 17,
  integrationId: 1,
  integrationName: 'Pedidos',
  endpointId: 1,
  endpointName: 'Todos os Pedidos',
  executionStatus: 'SUCCESS',
  httpStatus: 200,
  requestMethod: 'GET',
  requestPath: '/api/pedidos/buscar',
  requestParameters: '{}',
  durationMs: 169,
  startedAt: '2026-11-10T14:41:05.239292',
  finishedAt: '2026-11-10T14:41:05.408747',
  errorType: null,
  errorMessage: null,
}

const mockPage = {
  content: [mockExecution],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
}

function mockResponse({
  ok = true,
  status = 200,
  data = null,
} = {}) {
  return {
    ok,
    status,
    json: vi.fn().mockResolvedValue(data),
  }
}

describe('executionService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getExecutions', () => {
    it('deve consultar execuções com paginação padrão', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({ data: mockPage }),
      )

      const result = await getExecutions()

      expect(apiFetch).toHaveBeenCalledWith(
        '/api/admin/executions?page=0&size=20',
      )

      expect(result).toEqual(mockPage)
    })

    it('deve enviar todos os filtros informados', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({ data: mockPage }),
      )

      await getExecutions({
        integrationId: 1,
        endpointId: 2,
        executionStatus: 'SUCCESS',
        startedFrom: '2026-11-10T08:00:00',
        startedTo: '2026-11-10T18:00:00',
        page: 2,
        size: 10,
      })

      const url = apiFetch.mock.calls[0][0]
      const parsedUrl = new URL(
        url,
        'http://localhost',
      )

      expect(parsedUrl.pathname).toBe(
        '/api/admin/executions',
      )

      expect(
        Object.fromEntries(
          parsedUrl.searchParams.entries(),
        ),
      ).toEqual({
        integrationId: '1',
        endpointId: '2',
        executionStatus: 'SUCCESS',
        startedFrom: '2026-11-10T08:00:00',
        startedTo: '2026-11-10T18:00:00',
        page: '2',
        size: '10',
      })
    })

    it('não deve enviar filtros vazios', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({ data: mockPage }),
      )

      await getExecutions({
        integrationId: '',
        endpointId: null,
        executionStatus: '',
        startedFrom: '',
        startedTo: null,
      })

      expect(apiFetch).toHaveBeenCalledWith(
        '/api/admin/executions?page=0&size=20',
      )
    })

    it('deve retornar uma página vazia', async () => {
      const emptyPage = {
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      }

      apiFetch.mockResolvedValue(
        mockResponse({ data: emptyPage }),
      )

      const result = await getExecutions()

      expect(result).toEqual(emptyPage)
    })

    it('deve utilizar a mensagem de erro retornada pela API', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({
          ok: false,
          status: 400,
          data: {
            message: 'Número da página inválido',
          },
        }),
      )

      await expect(
        getExecutions({ page: -1 }),
      ).rejects.toThrow(
        'Número da página inválido',
      )
    })

    it('deve utilizar mensagem padrão quando a API não informar o erro', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({
          ok: false,
          status: 500,
          data: null,
        }),
      )

      await expect(
        getExecutions(),
      ).rejects.toThrow(
        'Não foi possível carregar o histórico de execuções.',
      )
    })

    it('deve tratar resposta de erro sem JSON válido', async () => {
      apiFetch.mockResolvedValue({
        ok: false,
        status: 500,
        json: vi.fn().mockRejectedValue(
          new SyntaxError('JSON inválido'),
        ),
      })

      await expect(
        getExecutions(),
      ).rejects.toThrow(
        'Não foi possível carregar o histórico de execuções.',
      )
    })
  })

  describe('getExecutionById', () => {
    it('deve consultar uma execução pelo ID', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({ data: mockExecution }),
      )

      const result = await getExecutionById(17)

      expect(apiFetch).toHaveBeenCalledWith(
        '/api/admin/executions/17',
      )

      expect(result).toEqual(mockExecution)
    })

    it.each([
      undefined,
      null,
      '',
    ])('deve rejeitar ID não informado: %s', async (id) => {
      await expect(
        getExecutionById(id),
      ).rejects.toThrow(
        'ID da execução não informado.',
      )

      expect(apiFetch).not.toHaveBeenCalled()
    })

    it('deve retornar erro específico para HTTP 404', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({
          ok: false,
          status: 404,
        }),
      )

      await expect(
        getExecutionById(999999),
      ).rejects.toThrow(
        'Execução não encontrada.',
      )
    })

    it('deve utilizar a mensagem de erro retornada pela API', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({
          ok: false,
          status: 403,
          data: {
            message: 'Acesso negado',
          },
        }),
      )

      await expect(
        getExecutionById(17),
      ).rejects.toThrow(
        'Acesso negado',
      )
    })

    it('deve utilizar mensagem padrão quando a API não informar o erro', async () => {
      apiFetch.mockResolvedValue(
        mockResponse({
          ok: false,
          status: 500,
          data: null,
        }),
      )

      await expect(
        getExecutionById(17),
      ).rejects.toThrow(
        'Não foi possível carregar os detalhes da execução.',
      )
    })

    it('deve tratar resposta de erro sem JSON válido', async () => {
      apiFetch.mockResolvedValue({
        ok: false,
        status: 500,
        json: vi.fn().mockRejectedValue(
          new SyntaxError('JSON inválido'),
        ),
      })

      await expect(
        getExecutionById(17),
      ).rejects.toThrow(
        'Não foi possível carregar os detalhes da execução.',
      )
    })
  })
})
