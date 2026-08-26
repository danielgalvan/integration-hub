import { useEffect, useState } from 'react'
import './EndpointsPage.css'
import ConfirmDialog from '../components/common/ConfirmDialog'
import MessageDialog from '../components/common/MessageDialog'
import EndpointForm from '../components/endpoints/EndpointForm'
import EndpointList from '../components/endpoints/EndpointList'
import EndpointTestModal from '../components/endpoints/EndpointTestModal'
import {
  createEndpoint,
  deleteEndpoint,
  getEndpointsByIntegration,
  updateEndpoint,
} from '../services/endpointService'

function EndpointsPage({
  integration,
  onBack,
}) {
  const [endpoints, setEndpoints] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [endpointToEdit, setEndpointToEdit] = useState(null)
  const [endpointToDelete, setEndpointToDelete] = useState(null)
  const [endpointToTest, setEndpointToTest] = useState(null)

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

  function handleOpenForm() {
    setEndpointToEdit(null)
    setShowForm(true)
  }

  function handleEditEndpoint(endpoint) {
    setEndpointToEdit(endpoint)
    setShowForm(true)
  }

  function handleCloseForm() {
    setShowForm(false)
    setEndpointToEdit(null)
  }

  function handleCloseError() {
    setError(null)
  }

  async function handleSubmitEndpoint(endpoint) {
    try {
      setError(null)

      if (endpointToEdit) {
        await updateEndpoint(
          endpointToEdit.id,
          endpoint,
        )
      } else {
        await createEndpoint(endpoint)
      }

      setShowForm(false)
      setEndpointToEdit(null)

      await loadEndpoints()
    } catch (err) {
      setError(err.message)
    }
  }

  function handleTestEndpoint(endpoint) {
    setEndpointToTest(endpoint)
  }

  function handleCloseTest() {
    setEndpointToTest(null)
  }

  function handleDeleteEndpoint(endpoint) {
    setEndpointToDelete(endpoint)
  }

  function handleCancelDelete() {
    setEndpointToDelete(null)
  }

  async function handleConfirmDelete() {
    if (!endpointToDelete) {
      return
    }

    try {
      setError(null)

      await deleteEndpoint(endpointToDelete.id)

      setEndpointToDelete(null)

      await loadEndpoints()
    } catch (err) {
      setEndpointToDelete(null)
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
            {showForm
              ? endpointToEdit
                ? 'Editar endpoint'
                : 'Novo endpoint'
              : 'Endpoints'}
          </h2>

          <p className="endpoints-page__description">
            {showForm
              ? endpointToEdit
                ? `Edite o endpoint da integração ${integration.name}.`
                : `Cadastre um novo endpoint para a integração ${integration.name}.`
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
            endpoint={endpointToEdit}
            onCancel={handleCloseForm}
            onSubmit={handleSubmitEndpoint}
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
              <EndpointList
                endpoints={endpoints}
                onTest={handleTestEndpoint}
                onEdit={handleEditEndpoint}
                onDelete={handleDeleteEndpoint}
              />
            )}
          </>
        )}
      </div>

      {endpointToTest && (
        <EndpointTestModal
          key={endpointToTest.id}
          open
          integration={integration}
          endpoint={endpointToTest}
          onClose={handleCloseTest}
        />
      )}

      <ConfirmDialog
        open={endpointToDelete !== null}
        title="Excluir endpoint?"
        message={
          endpointToDelete
            ? `O endpoint "${endpointToDelete.name}" será removido permanentemente. Essa ação não pode ser desfeita.`
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

export default EndpointsPage
