import { useEffect, useState } from 'react'
import './IntegrationsPage.css'
import IntegrationForm from '../components/integrations/IntegrationForm'
import IntegrationList from '../components/integrations/IntegrationList'
import {
  createIntegration,
  getIntegrations,
} from '../services/integrationService'

function IntegrationsPage() {
  const [integrations, setIntegrations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showForm, setShowForm] = useState(false)

  async function loadIntegrations() {
    try {
      setLoading(true)
      setError(null)

      const data = await getIntegrations()
      setIntegrations(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadIntegrations()
  }, [])

  function handleOpenForm() {
    setShowForm(true)
  }

  function handleCloseForm() {
    setShowForm(false)
  }

  async function handleCreateIntegration(integration) {
    try {
      setError(null)

      await createIntegration(integration)

      setShowForm(false)

      await loadIntegrations()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <section className="integrations-page">
      <div className="integrations-page__header">
        <div>
          <h2 className="integrations-page__title">
            {showForm ? 'Nova integração' : 'Integrações'}
          </h2>

          <p className="integrations-page__description">
            {showForm
              ? 'Cadastre uma nova integração no Integration Hub.'
              : 'Gerencie as integrações disponíveis no Integration Hub.'}
          </p>
        </div>

        {!showForm && (
          <button
            type="button"
            className="integrations-page__new"
            onClick={handleOpenForm}
          >
            + Nova integração
          </button>
        )}
      </div>

      <div className="integrations-page__content">
        {showForm ? (
          <>
            {error && (
              <div className="integrations-page__message integrations-page__message--error">
                {error}
              </div>
            )}

            <IntegrationForm
              onCancel={handleCloseForm}
              onSubmit={handleCreateIntegration}
            />
          </>
        ) : (
          <>
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
          </>
        )}
      </div>
    </section>
  )
}

export default IntegrationsPage