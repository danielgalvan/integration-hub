import { useEffect, useState } from 'react'
import './IntegrationsPage.css'
import IntegrationList from '../components/integrations/IntegrationList'
import { getIntegrations } from '../services/integrationService'

function IntegrationsPage() {
  const [integrations, setIntegrations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function loadIntegrations() {
      try {
        const data = await getIntegrations()
        setIntegrations(data)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    loadIntegrations()
  }, [])

  return (
    <section className="integrations-page">
      <div className="integrations-page__header">
        <div>
          <h2 className="integrations-page__title">
            Integrações
          </h2>

          <p className="integrations-page__description">
            Gerencie as integrações disponíveis no Integration Hub.
          </p>
        </div>

        <button
          type="button"
          className="integrations-page__new"
        >
          + Nova integração
        </button>
      </div>

      <div className="integrations-page__content">
        {loading && (
          <div className="integrations-page__message">
            Carregando integrações...
          </div>
        )}

        {error && (
          <div className="integrations-page__message integrations-page__message--error">
            {error}
          </div>
        )}

        {!loading && !error && (
          <IntegrationList integrations={integrations} />
        )}
      </div>
    </section>
  )
}

export default IntegrationsPage