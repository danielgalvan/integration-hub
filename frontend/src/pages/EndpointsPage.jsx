import { useCallback, useEffect, useState } from 'react'
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
  role,
  integration,
  onBack,
}) {
  const [endpoints, setEndpoints] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [
    endpointToEdit,
    setEndpointToEdit,
  ] = useState(null)
  const [
    endpointToDelete,
    setEndpointToDelete,
  ] = useState(null)
  const [
    endpointToTest,
    setEndpointToTest,
  ] = useState(null)

  const canEdit =
    role === 'A' || role === 'C'

  const loadEndpoints = useCallback(async () => {
    if (!integration) {
      setEndpoints([])
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      setError(null)

      const data =
        await getEndpointsByIntegration(
          integration.id,
        )

      setEndpoints(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [integration])

  useEffect(() => {
    async function load() {
      await loadEndpoints()
    }

    load()
  }, [loadEndpoints])

  function handleOpenForm() {
    if (!canEdit) {
      return
    }

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

  async function handleSubmitEndpoint(
    endpoint,
  ) {
    if (!canEdit) {
      return
    }

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
    if (!canEdit) {
      return
    }

    setEndpointToDelete(endpoint)
  }

  function handleCancelDelete() {
    setEndpointToDelete(null)
  }

  async function handleConfirmDelete() {
    if (
      !endpointToDelete ||
      !canEdit
    ) {
      return
    }

    try {
      setError(null)

      await deleteEndpoint(
        endpointToDelete.id,
      )

      setEndpointToDelete(null)

      await loadEndpoints()
    } catch (err) {
      setEndpointToDelete(null)
      setError(err.message)
    }
  }

  const isEditing =
    endpointToEdit !== null

  const isReadOnly =
    !canEdit && isEditing

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
              ? isReadOnly
                ? 'Visualizar endpoint'
                : isEditing
                  ? 'Editar endpoint'
                  : 'Novo endpoint'
              : 'Endpoints'}
          </h2>

          <p className="endpoints-page__description">
            {showForm
              ? isReadOnly
                ? `Consulte os dados do endpoint da integração ${integration.name}.`
                : isEditing
                  ? `Edite o endpoint da integração ${integration.name}.`
                  : `Cadastre um novo endpoint para a integração ${integration.name}.`
              : `Integração: ${integration.name}`}
          </p>
        </div>

        {!showForm && (
          <div className="endpoints-page__header-actions">
            {canEdit && (
              <button
                type="button"
                className="endpoints-page__new"
                onClick={handleOpenForm}
              >
                + Novo endpoint
              </button>
            )}

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
            readOnly={!canEdit}
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
                canEdit={canEdit}
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
        open={
          canEdit &&
          endpointToDelete !== null
        }
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
