
import { useCallback, useEffect, useState } from 'react'
import './ExecutionsPage.css'

import {
  getExecutions,
  getExecutionById,
} from '../services/executionService'

import { getIntegrations } from '../services/integrationService'
import { getEndpointsByIntegration } from '../services/endpointService'

const PAGE_SIZE = 20

const INITIAL_FILTERS = {
  integrationId: '',
  endpointId: '',
  executionStatus: '',
  startedFrom: '',
  startedTo: '',
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('pt-BR')
}

function formatDuration(value) {
  if (value === null || value === undefined) {
    return '-'
  }

  return `${value} ms`
}

function formatValue(value) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }

  return String(value)
}

function getStatusLabel(status) {
  const labels = {
    SUCCESS: 'Sucesso',
    ERROR: 'Erro',
    UNAUTHORIZED: 'Não autorizado',
    NOT_FOUND: 'Não encontrado',
  }

  return labels[status] || status || '-'
}

function getStatusClass(status) {
  switch (status) {
    case 'SUCCESS':
      return 'executions-page__status--success'
    case 'ERROR':
      return 'executions-page__status--error'
    case 'UNAUTHORIZED':
      return 'executions-page__status--warning'
    case 'NOT_FOUND':
      return 'executions-page__status--warning'
    default:
      return ''
  }
}

function ExecutionDetail({ execution, onClose }) {
  if (!execution) {
    return null
  }

  const fields = [
    ['ID', execution.id],
    ['Integração', execution.integrationName],
    ['ID da integração', execution.integrationId],
    ['Endpoint', execution.endpointName],
    ['ID do endpoint', execution.endpointId],
    ['Método', execution.requestMethod],
    ['Caminho', execution.requestPath],
    ['Status', getStatusLabel(execution.executionStatus)],
    ['HTTP', execution.httpStatus],
    ['Duração', formatDuration(execution.durationMs)],
    ['Credencial', execution.credentialName],
    ['Início', formatDate(execution.startedAt)],
    ['Fim', formatDate(execution.finishedAt)],
    ['Criado em', formatDate(execution.createdAt)],
    ['Tipo de erro', execution.errorType],
    ['Mensagem de erro', execution.errorMessage],
  ]

  return (
    <div
      className="executions-page__modal-overlay"
      onClick={onClose}
    >
      <div
        className="executions-page__modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="execution-detail-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="executions-page__modal-header">
          <div>
            <h3 id="execution-detail-title">
              Detalhes da execução #{execution.id}
            </h3>

            <p>
              Informações registradas no histórico.
            </p>
          </div>

          <button
            type="button"
            className="executions-page__close"
            onClick={onClose}
            aria-label="Fechar detalhes"
          >
            ×
          </button>
        </div>

        <div className="executions-page__modal-content">
          <div className="executions-page__details">
            {fields.map(([label, value]) => (
              <div
                className="executions-page__detail"
                key={label}
              >
                <span>{label}</span>
                <strong>{formatValue(value)}</strong>
              </div>
            ))}
          </div>

          <div className="executions-page__parameters">
            <h4>Parâmetros da requisição</h4>

            <pre>
              {execution.requestParameters || '{}'}
            </pre>
          </div>
        </div>

        <div className="executions-page__modal-footer">
          <button
            type="button"
            onClick={onClose}
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  )
}

function ExecutionsPage({ role }) {
  const [integrations, setIntegrations] = useState([])
  const [endpoints, setEndpoints] = useState([])

  const [filters, setFilters] = useState({
    ...INITIAL_FILTERS,
  })

  const [appliedFilters, setAppliedFilters] = useState({
    ...INITIAL_FILTERS,
  })

  const [page, setPage] = useState(0)

  const [executions, setExecutions] = useState([])
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const [loading, setLoading] = useState(false)
  const [loadingEndpoints, setLoadingEndpoints] = useState(false)
  const [loadingDetail, setLoadingDetail] = useState(false)

  const [error, setError] = useState(null)
  const [detailError, setDetailError] = useState(null)
  const [selectedExecution, setSelectedExecution] = useState(null)

  const [refreshKey, setRefreshKey] = useState(0)

  const isAdmin = role === 'A'

  useEffect(() => {
    if (!isAdmin) {
      return
    }

    let active = true

    async function loadIntegrations() {
      try {
        const data = await getIntegrations()

        if (active) {
          setIntegrations(data)
        }
      } catch (err) {
        if (active) {
          setError(
            err?.message ||
              'Não foi possível carregar as integrações.',
          )
        }
      }
    }

    loadIntegrations()

    return () => {
      active = false
    }
  }, [isAdmin])

  useEffect(() => {
    if (!isAdmin || !filters.integrationId) {
      setEndpoints([])
      setLoadingEndpoints(false)
      return
    }

    let active = true

    async function loadEndpoints() {
      try {
        setLoadingEndpoints(true)

        const data = await getEndpointsByIntegration(
          filters.integrationId,
        )

        if (active) {
          setEndpoints(data)
        }
      } catch (err) {
        if (active) {
          setEndpoints([])
          setError(
            err?.message ||
              'Não foi possível carregar os endpoints.',
          )
        }
      } finally {
        if (active) {
          setLoadingEndpoints(false)
        }
      }
    }

    loadEndpoints()

    return () => {
      active = false
    }
  }, [filters.integrationId, isAdmin])

  useEffect(() => {
    if (!isAdmin) {
      return
    }

    let active = true

    async function loadExecutions() {
      try {
        setLoading(true)
        setError(null)

        const data = await getExecutions({
          ...appliedFilters,
          page,
          size: PAGE_SIZE,
        })

        if (!active) {
          return
        }

        setExecutions(data.content || [])
        setTotalElements(data.totalElements || 0)
        setTotalPages(data.totalPages || 0)
      } catch (err) {
        if (active) {
          setExecutions([])
          setTotalElements(0)
          setTotalPages(0)
          setError(
            err?.message ||
              'Não foi possível carregar o histórico.',
          )
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadExecutions()

    return () => {
      active = false
    }
  }, [appliedFilters, page, refreshKey, isAdmin])

  const handleFilterChange = useCallback((event) => {
    const { name, value } = event.target

    setFilters((current) => ({
      ...current,
      [name]: value,
      ...(name === 'integrationId'
        ? { endpointId: '' }
        : {}),
    }))
  }, [])

  function handleSearch(event) {
    event.preventDefault()

    setPage(0)
    setAppliedFilters({ ...filters })
  }

  function handleClearFilters() {
    setFilters({ ...INITIAL_FILTERS })
    setAppliedFilters({ ...INITIAL_FILTERS })
    setPage(0)
    setRefreshKey((current) => current + 1)
  }

  function handleRefresh() {
    setRefreshKey((current) => current + 1)
  }

  async function handleOpenDetail(id) {
    try {
      setLoadingDetail(true)
      setDetailError(null)
      setSelectedExecution(null)

      const data = await getExecutionById(id)

      setSelectedExecution(data)
    } catch (err) {
      setDetailError(
        err?.message ||
          'Não foi possível carregar os detalhes.',
      )
    } finally {
      setLoadingDetail(false)
    }
  }

  function handleCloseDetail() {
    setSelectedExecution(null)
  }

  if (!isAdmin) {
    return (
      <section className="executions-page">
        <h2 className="executions-page__title">
          Histórico de execuções
        </h2>

        <p className="executions-page__message">
          Você não possui permissão para acessar esta página.
        </p>
      </section>
    )
  }

  return (
    <section className="executions-page">
      <div className="executions-page__header">
        <div>
          <h2 className="executions-page__title">
            Histórico de execuções
          </h2>

          <p className="executions-page__description">
            Consulte as chamadas realizadas pelas integrações,
            seus resultados e tempos de execução.
          </p>
        </div>

        <button
          type="button"
          className="executions-page__refresh"
          onClick={handleRefresh}
          disabled={loading}
        >
          Atualizar
        </button>
      </div>

      <form
        className="executions-page__filters"
        onSubmit={handleSearch}
      >
        <div className="executions-page__filter">
          <label htmlFor="execution-integration">
            Integração
          </label>

          <select
            id="execution-integration"
            name="integrationId"
            value={filters.integrationId}
            onChange={handleFilterChange}
          >
            <option value="">Todas</option>

            {integrations.map((integration) => (
              <option
                key={integration.id}
                value={integration.id}
              >
                {integration.name}
              </option>
            ))}
          </select>
        </div>

        <div className="executions-page__filter">
          <label htmlFor="execution-endpoint">
            Endpoint
          </label>

          <select
            id="execution-endpoint"
            name="endpointId"
            value={filters.endpointId}
            onChange={handleFilterChange}
            disabled={
              !filters.integrationId ||
              loadingEndpoints
            }
          >
            <option value="">Todos</option>

            {endpoints.map((endpoint) => (
              <option
                key={endpoint.id}
                value={endpoint.id}
              >
                {endpoint.name}
              </option>
            ))}
          </select>
        </div>

        <div className="executions-page__filter">
          <label htmlFor="execution-status">
            Status
          </label>

          <select
            id="execution-status"
            name="executionStatus"
            value={filters.executionStatus}
            onChange={handleFilterChange}
          >
            <option value="">Todos</option>
            <option value="SUCCESS">Sucesso</option>
            <option value="ERROR">Erro</option>
            <option value="UNAUTHORIZED">
              Não autorizado
            </option>
            <option value="NOT_FOUND">
              Não encontrado
            </option>
          </select>
        </div>

        <div className="executions-page__filter">
          <label htmlFor="execution-started-from">
            Data inicial
          </label>

          <input
            id="execution-started-from"
            name="startedFrom"
            type="datetime-local"
            value={filters.startedFrom}
            onChange={handleFilterChange}
          />
        </div>

        <div className="executions-page__filter">
          <label htmlFor="execution-started-to">
            Data final
          </label>

          <input
            id="execution-started-to"
            name="startedTo"
            type="datetime-local"
            value={filters.startedTo}
            onChange={handleFilterChange}
          />
        </div>

        <div className="executions-page__filter-actions">
          <button
            type="submit"
            className="executions-page__search"
            disabled={loading}
          >
            Filtrar
          </button>

          <button
            type="button"
            className="executions-page__clear"
            onClick={handleClearFilters}
            disabled={loading}
          >
            Limpar
          </button>
        </div>
      </form>

      {error && (
        <div
          className="executions-page__error"
          role="alert"
        >
          {error}
        </div>
      )}

      {detailError && (
        <div
          className="executions-page__error"
          role="alert"
        >
          {detailError}
        </div>
      )}

      <div className="executions-page__content">
        {loading ? (
          <div className="executions-page__message">
            Carregando histórico de execuções...
          </div>
        ) : executions.length === 0 ? (
          <div className="executions-page__message">
            Nenhuma execução encontrada.
          </div>
        ) : (
          <div className="executions-page__table-wrapper">
            <table className="executions-page__table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Data / Hora</th>
                  <th>Integração</th>
                  <th>Endpoint</th>
                  <th>Status</th>
                  <th>HTTP</th>
                  <th>Duração</th>
                  <th>Ações</th>
                </tr>
              </thead>

              <tbody>
                {executions.map((execution) => (
                  <tr key={execution.id}>
                    <td>{execution.id}</td>

                    <td>
                      {formatDate(execution.startedAt)}
                    </td>

                    <td>
                      {execution.integrationName || '-'}
                    </td>

                    <td>
                      {execution.endpointName || '-'}
                    </td>

                    <td>
                      <span
                        className={
                          `executions-page__status ${
                            getStatusClass(
                              execution.executionStatus,
                            )
                          }`
                        }
                      >
                        {getStatusLabel(
                          execution.executionStatus,
                        )}
                      </span>
                    </td>

                    <td>
                      {formatValue(execution.httpStatus)}
                    </td>

                    <td>
                      {formatDuration(execution.durationMs)}
                    </td>

                    <td>
                      <button
                        type="button"
                        className="executions-page__details-button"
                        onClick={() =>
                          handleOpenDetail(execution.id)
                        }
                        disabled={loadingDetail}
                      >
                        Detalhes
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="executions-page__pagination">
        <span>
          {totalElements} execução(ões) encontrada(s)
        </span>

        <div className="executions-page__pagination-controls">
          <button
            type="button"
            onClick={() =>
              setPage((current) => current - 1)
            }
            disabled={loading || page === 0}
          >
            Anterior
          </button>

          <span>
            Página {page + 1} de {Math.max(totalPages, 1)}
          </span>

          <button
            type="button"
            onClick={() =>
              setPage((current) => current + 1)
            }
            disabled={
              loading ||
              page + 1 >= totalPages
            }
          >
            Próxima
          </button>
        </div>
      </div>

      {loadingDetail && (
        <div
          className="executions-page__message"
          role="status"
        >
          Carregando detalhes da execução...
        </div>
      )}

      {selectedExecution && (
        <ExecutionDetail
          execution={selectedExecution}
          onClose={handleCloseDetail}
        />
      )}
    </section>
  )
}

export default ExecutionsPage
