import { useEffect, useState } from 'react'
import './EndpointsPage.css'
import MessageDialog from '../components/common/MessageDialog'
import EndpointForm from '../components/endpoints/EndpointForm'
import EndpointList from '../components/endpoints/EndpointList'
import {
  createEndpoint,
  getEndpointsByIntegration,
} from '../services/endpointService'

function EndpointsPage({
  integration,
  onBack,
}) {
  const [endpoints, setEndpoints] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showForm, setShowForm] = useState(false)

  async function loadEndpoints() {
    if (!integration) {
      setEndpoints([])
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      setError(null)

      const data = await getEndpointsByIntegration(
        integration.id,
      )

      setEndpoints(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    async function load() {
      if (!integration) {
        setEndpoints([])
        setLoading(false)
        return
      }

      try {
        const data = await getEndpointsByIntegration(
          integration.id,
        )

        setEndpoints(data)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [integration])

  function handleOpenForm() {
    setShowForm(true)
  }

  function handleCloseForm() {
    setShowForm(false)
  }

  function handleCloseError() {
    setError(null)
  }

  async function handleCreateEndpoint(endpoint) {
    try {
      setError(null)

      await createEndpoint(endpoint)

      setShowForm(false)

      await loadEndpoints()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!integration) {
    return (
      <section className="endpoints-page">
        <div className="endpoints-page__header">
          <div>
            <h2 className="endpoints-page__title">
              Endpoints
            </h2>

            <p className="endpoints-page__description">
              Selecione uma integração para visualizar seus endpoints.
            </p>
          </div>

          <button
            type="button"
            className="endpoints-page__back"
            onClick={onBack}
          >
            Voltar
          </button>
        </div>
      </section>
    )
  }

  return (
    <section className="endpoints-page">
      <div className="endpoints-page__header">
        <div>
          <h2 className="endpoints-page__title">
            {showForm ? 'Novo endpoint' : 'Endpoints'}
          </h2>

          <p className="endpoints-page__description">
            {showForm
              ? `Cadastre um novo endpoint para a integração ${integration.name}.`
              : `Integração: ${integration.name}`}
          </p>
        </div>

        {!showForm && (
          <div className="endpoints-page__header-actions">
            <button
              type="button"
              className="endpoints-page__new"
              onClick={handleOpenForm}
            >
              + Novo endpoint
            </button>

            <button
              type="button"
              className="endpoints-page__back"
              onClick={onBack}
            >
              Voltar para integrações
            </button>
          </div>
        )}
      </div>

      <div className="endpoints-page__content">
        {showForm ? (
          <EndpointForm
            integrationId={integration.id}
            onCancel={handleCloseForm}
            onSubmit={handleCreateEndpoint}
            onValidationError={setError}
          />
        ) : (
          <>
            {loading && (
              <div className="endpoints-page__message">
                Carregando endpoints...
              </div>
            )}

            {!loading && (
              <EndpointList endpoints={endpoints} />
            )}
          </>
        )}
      </div>

      <MessageDialog
        open={error !== null}
        title="Não foi possível concluir a operação"
        message={error || ''}
        onClose={handleCloseError}
      />
    </section>
  )
}

export default EndpointsPage