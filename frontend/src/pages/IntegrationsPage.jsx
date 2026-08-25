import { useEffect, useState } from 'react'
import './IntegrationsPage.css'
import ConfirmDialog from '../components/common/ConfirmDialog'
import MessageDialog from '../components/common/MessageDialog'
import IntegrationForm from '../components/integrations/IntegrationForm'
import IntegrationList from '../components/integrations/IntegrationList'
import {
  createIntegration,
  deleteIntegration,
  getIntegrations,
} from '../services/integrationService'

function IntegrationsPage() {
  const [integrations, setIntegrations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [integrationToDelete, setIntegrationToDelete] = useState(null)

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
    async function load() {
      try {
        const data = await getIntegrations()
        setIntegrations(data)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [])

  function handleOpenForm() {
    setShowForm(true)
  }

  function handleCloseForm() {
    setShowForm(false)
  }

  function handleCloseError() {
    setError(null)
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

  function handleDeleteIntegration(integration) {
    setIntegrationToDelete(integration)
  }

  function handleCancelDelete() {
    setIntegrationToDelete(null)
  }

  async function handleConfirmDelete() {
    if (!integrationToDelete) {
      return
    }

    try {
      setError(null)

      await deleteIntegration(integrationToDelete.id)

      setIntegrationToDelete(null)

      await loadIntegrations()
    } catch (err) {
      setIntegrationToDelete(null)
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
          <IntegrationForm
            onCancel={handleCloseForm}
            onSubmit={handleCreateIntegration}
          />
        ) : (
          <>
            {loading && (
              <div className="integrations-page__message">
                Carregando integrações...
              </div>
            )}

            {!loading && (
              <IntegrationList
                integrations={integrations}
                onDelete={handleDeleteIntegration}
              />
            )}
          </>
        )}
      </div>

      <ConfirmDialog
        open={integrationToDelete !== null}
        title="Excluir integração?"
        message={
          integrationToDelete
            ? `A integração "${integrationToDelete.name}" será removida permanentemente. Essa ação não pode ser desfeita.`
            : ''
        }
        confirmLabel="Excluir"
        cancelLabel="Cancelar"
        onConfirm={handleConfirmDelete}
        onCancel={handleCancelDelete}
      />

      <MessageDialog
        open={error !== null}
        title="Não foi possível concluir a operação"
        message={error || ''}
        onClose={handleCloseError}
      />
    </section>
  )
}

export default IntegrationsPage