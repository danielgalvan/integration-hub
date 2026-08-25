import { useEffect, useState } from 'react'
import './EndpointsPage.css'
import MessageDialog from '../components/common/MessageDialog'
import EndpointList from '../components/endpoints/EndpointList'
import {
  getEndpointsByIntegration,
} from '../services/endpointService'

function EndpointsPage({
  integration,
  onBack,
}) {
  const [endpoints, setEndpoints] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function load() {
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

    load()
  }, [integration])

  function handleCloseError() {
    setError(null)
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
            Endpoints
          </h2>

          <p className="endpoints-page__description">
            Integração: {integration.name}
          </p>
        </div>

        <button
          type="button"
          className="endpoints-page__back"
          onClick={onBack}
        >
          Voltar para integrações
        </button>
      </div>

      <div className="endpoints-page__content">
        {loading && (
          <div className="endpoints-page__message">
            Carregando endpoints...
          </div>
        )}

        {!loading && (
          <EndpointList endpoints={endpoints} />
        )}
      </div>

      <MessageDialog
        open={error !== null}
        title="Não foi possível carregar os endpoints"
        message={error || ''}
        onClose={handleCloseError}
      />
    </section>
  )
}

export default EndpointsPage