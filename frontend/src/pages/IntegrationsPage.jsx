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
  updateIntegration,
} from '../services/integrationService'

function IntegrationsPage({
  role,
  onOpenEndpoints,
}) {
  const [integrations, setIntegrations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [
    integrationToEdit,
    setIntegrationToEdit,
  ] = useState(null)
  const [
    integrationToDelete,
    setIntegrationToDelete,
  ] = useState(null)

  const canEdit =
    role === 'A' || role === 'C'

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

    load()
  }, [])

  function handleOpenForm() {
    if (!canEdit) {
      return
    }

    setIntegrationToEdit(null)
    setShowForm(true)
  }

  function handleEditIntegration(integration) {
    setIntegrationToEdit(integration)
    setShowForm(true)
  }

  function handleCloseForm() {
    setShowForm(false)
    setIntegrationToEdit(null)
  }

  function handleCloseError() {
    setError(null)
  }

  async function handleSubmitIntegration(
    integration,
  ) {
    if (!canEdit) {
      return
    }

    try {
      setError(null)

      if (integrationToEdit) {
        await updateIntegration(
          integrationToEdit.id,
          integration,
        )
      } else {
        await createIntegration(
          integration,
        )
      }

      setShowForm(false)
      setIntegrationToEdit(null)

      await loadIntegrations()
    } catch (err) {
      setError(err.message)
    }
  }

  function handleDeleteIntegration(integration) {
    if (!canEdit) {
      return
    }

    setIntegrationToDelete(integration)
  }

  function handleCancelDelete() {
    setIntegrationToDelete(null)
  }

  async function handleConfirmDelete() {
    if (
      !integrationToDelete ||
      !canEdit
    ) {
      return
    }

    try {
      setError(null)

      await deleteIntegration(
        integrationToDelete.id,
      )

      setIntegrationToDelete(null)

      await loadIntegrations()
    } catch (err) {
      setIntegrationToDelete(null)
      setError(err.message)
    }
  }

  const isEditing =
    integrationToEdit !== null

  const isReadOnly =
    !canEdit && isEditing

  return (
    <section className="integrations-page">
      <div className="integrations-page__header">
        <div>
          <h2 className="integrations-page__title">
            {showForm
              ? isReadOnly
                ? 'Visualizar integração'
                : isEditing
                  ? 'Editar integração'
                  : 'Nova integração'
              : 'Integrações'}
          </h2>

          <p className="integrations-page__description">
            {showForm
              ? isReadOnly
                ? 'Consulte os dados da integração selecionada.'
                : isEditing
                  ? 'Atualize os dados da integração selecionada.'
                  : 'Cadastre uma nova integração no Integration Hub.'
              : canEdit
                ? 'Gerencie as integrações disponíveis no Integration Hub.'
                : 'Consulte as integrações disponíveis no Integration Hub.'}
          </p>
        </div>

        {!showForm && canEdit && (
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
            integration={integrationToEdit}
            readOnly={!canEdit}
            onCancel={handleCloseForm}
            onSubmit={handleSubmitIntegration}
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
                canEdit={canEdit}
                onOpenEndpoints={onOpenEndpoints}
                onEdit={handleEditIntegration}
                onDelete={handleDeleteIntegration}
              />
            )}
          </>
        )}
      </div>

      <ConfirmDialog
        open={
          canEdit &&
          integrationToDelete !== null
        }
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
