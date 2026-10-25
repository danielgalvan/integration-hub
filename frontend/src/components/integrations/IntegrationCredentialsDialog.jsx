import { useEffect, useState } from 'react'
import IntegrationCredentialList from './IntegrationCredentialList'
import IntegrationCredentialForm from './IntegrationCredentialForm'
import {
  getIntegrationCredentials,
  createIntegrationCredential,
  updateIntegrationCredential,
  setIntegrationCredentialActive,
  getIntegrationCredentialApiKey,
  regenerateIntegrationCredentialApiKey,
} from '../../services/integrationCredentialService'
import './IntegrationCredentialsDialog.css'

function IntegrationCredentialsDialog({
  integration,
  onClose,
}) {
  const [credentials, setCredentials] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [formMode, setFormMode] = useState(null)
  const [selectedCredential, setSelectedCredential] = useState(null)
  const [visibleApiKey, setVisibleApiKey] = useState(null)
  const [showApiKey, setShowApiKey] = useState(false)

  const integrationId = integration.id

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const data = await getIntegrationCredentials(integrationId)

        if (!cancelled) {
          setCredentials(data)
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    load()

    return () => {
      cancelled = true
    }
  }, [integrationId])

  function clearFeedback() {
    setError('')
    setMessage('')
  }

  async function reloadCredentials() {
    const data = await getIntegrationCredentials(integrationId)
    setCredentials(data)
  }

  function handleCreate() {
    clearFeedback()
    setSelectedCredential(null)
    setFormMode('create')
    setVisibleApiKey(null)
  }

  function handleEdit(credential) {
    clearFeedback()
    setSelectedCredential(credential)
    setFormMode('edit')
    setVisibleApiKey(null)
  }

  function handleCancelForm() {
    setFormMode(null)
    setSelectedCredential(null)
  }

  async function handleSave({ name }) {
    setSaving(true)
    clearFeedback()

    try {
      if (formMode === 'edit') {
        await updateIntegrationCredential(
          integrationId,
          selectedCredential.id,
          name,
        )

        setMessage('Credencial atualizada com sucesso.')
      } else {
        const result = await createIntegrationCredential(
          integrationId,
          name,
        )

        setMessage('Credencial criada com sucesso.')

        if (result?.apiKey) {
          setVisibleApiKey(result.apiKey)
          setShowApiKey(false)
        }
      }

      setFormMode(null)
      setSelectedCredential(null)

      await reloadCredentials()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleViewApiKey(credential) {
    clearFeedback()
    setVisibleApiKey(null)

    try {
      const result = await getIntegrationCredentialApiKey(
        integrationId,
        credential.id,
      )

      setVisibleApiKey(result.apiKey)
      setShowApiKey(false)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleToggleActive(credential) {
    const active = credential.active !== 'S'

    const confirmed = window.confirm(
      active
        ? `Deseja ativar a credencial "${credential.name}"?`
        : `Deseja desativar a credencial "${credential.name}"?`,
    )

    if (!confirmed) return

    clearFeedback()

    try {
      await setIntegrationCredentialActive(
        integrationId,
        credential.id,
        active,
      )

      await reloadCredentials()

      setMessage(
        active
          ? 'Credencial ativada com sucesso.'
          : 'Credencial desativada com sucesso.',
      )
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleRegenerateApiKey(credential) {
    const confirmed = window.confirm(
      `Deseja regenerar a API Key de "${credential.name}"? A chave anterior deixará de funcionar imediatamente.`,
    )

    if (!confirmed) return

    clearFeedback()
    setVisibleApiKey(null)

    try {
      const result = await regenerateIntegrationCredentialApiKey(
        integrationId,
        credential.id,
      )

      setVisibleApiKey(result.apiKey)
      setShowApiKey(false)

      setMessage('API Key regenerada com sucesso.')
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleCopyApiKey() {
    try {
      await navigator.clipboard.writeText(visibleApiKey)
      setMessage('API Key copiada.')
    } catch {
      setError('Não foi possível copiar a API Key.')
    }
  }

  return (
    <div className="integration-credentials-dialog__overlay">
      <div
        className="integration-credentials-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={`Credenciais da integração ${integration.name}`}
      >
        <div className="integration-credentials-dialog__header">
          <div>
            <h2>Credenciais da integração</h2>
            <p>{integration.name}</p>
          </div>

          <button
            type="button"
            className="integration-credentials-dialog__close"
            onClick={onClose}
            disabled={saving}
            aria-label="Fechar"
          >
            ×
          </button>
        </div>

        <div className="integration-credentials-dialog__body">
          {error && (
            <div
              className="integration-credentials-dialog__error"
              role="alert"
            >
              {error}
            </div>
          )}

          {message && (
            <div
              className="integration-credentials-dialog__message"
              role="status"
            >
              {message}
            </div>
          )}

          {formMode ? (
            <IntegrationCredentialForm
              key={
                formMode === 'edit'
                  ? `edit-${selectedCredential?.id}`
                  : 'create'
              }
              credential={
                formMode === 'edit'
                  ? selectedCredential
                  : null
              }
              loading={saving}
              onSave={handleSave}
              onCancel={handleCancelForm}
            />
          ) : (
            <>
              <div className="integration-credentials-dialog__toolbar">
                <button
                  type="button"
                  onClick={handleCreate}
                  disabled={loading}
                >
                  Nova credencial
                </button>
              </div>

              {loading ? (
                <p>Carregando credenciais...</p>
              ) : (
                <IntegrationCredentialList
                  credentials={credentials}
                  onViewApiKey={handleViewApiKey}
                  onEdit={handleEdit}
                  onToggleActive={handleToggleActive}
                  onRegenerateApiKey={handleRegenerateApiKey}
                />
              )}
            </>
          )}

          {visibleApiKey && !formMode && (
            <div className="integration-credentials-dialog__api-key">
              <h3>API Key</h3>

              <p>
                Esta chave permite consumir os endpoints
                da integração. Mantenha-a em segurança.
              </p>

              <div className="integration-credentials-dialog__api-key-value">
                <input
                  type={showApiKey ? 'text' : 'password'}
                  value={visibleApiKey}
                  readOnly
                  aria-label="API Key"
                />

                <button
                  type="button"
                  onClick={() => setShowApiKey(!showApiKey)}
                >
                  {showApiKey ? 'Ocultar' : 'Mostrar'}
                </button>

                <button
                  type="button"
                  onClick={handleCopyApiKey}
                >
                  Copiar
                </button>
              </div>

              <button
                type="button"
                className="integration-credentials-dialog__hide-key"
                onClick={() => {
                  setVisibleApiKey(null)
                  setShowApiKey(false)
                }}
              >
                Fechar chave
              </button>
            </div>
          )}
        </div>

        <div className="integration-credentials-dialog__footer">
          <button
            type="button"
            onClick={onClose}
            disabled={saving}
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  )
}

export default IntegrationCredentialsDialog
