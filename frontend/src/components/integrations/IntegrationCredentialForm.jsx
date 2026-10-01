import { useState } from 'react'
import './IntegrationCredentialForm.css'

function IntegrationCredentialForm({
  credential = null,
  loading = false,
  onSave,
  onCancel,
}) {
  const [name, setName] = useState(
    credential?.name ?? '',
  )
  const [error, setError] = useState('')

  const editing = Boolean(credential)

  function handleSubmit(event) {
    event.preventDefault()

    const trimmedName = name.trim()

    if (!trimmedName) {
      setError('Informe o nome da credencial.')
      return
    }

    setError('')

    onSave({
      name: trimmedName,
    })
  }

  function handleNameChange(event) {
    setName(event.target.value)

    if (error) {
      setError('')
    }
  }

  return (
    <form
      className="integration-credential-form"
      onSubmit={handleSubmit}
    >
      <div className="integration-credential-form__header">
        <h2 className="integration-credential-form__title">
          {editing
            ? 'Editar credencial'
            : 'Nova credencial'}
        </h2>

        <p className="integration-credential-form__description">
          {editing
            ? 'Altere a identificação da credencial.'
            : 'Informe um nome para identificar quem utilizará esta credencial.'}
        </p>
      </div>

      <div className="integration-credential-form__field">
        <label
          className="integration-credential-form__label"
          htmlFor="credential-name"
        >
          Nome
        </label>

        <input
          id="credential-name"
          className={`integration-credential-form__input ${
            error
              ? 'integration-credential-form__input--error'
              : ''
          }`}
          type="text"
          value={name}
          onChange={handleNameChange}
          placeholder="Ex.: Sistema Tasy"
          maxLength={100}
          disabled={loading}
          autoFocus
        />

        {error && (
          <span
            className="integration-credential-form__error"
            role="alert"
          >
            {error}
          </span>
        )}
      </div>

      <div className="integration-credential-form__actions">
        <button
          type="button"
          className="integration-credential-form__cancel"
          onClick={onCancel}
          disabled={loading}
        >
          Cancelar
        </button>

        <button
          type="submit"
          className="integration-credential-form__save"
          disabled={loading}
        >
          {loading
            ? 'Salvando...'
            : 'Salvar'}
        </button>
      </div>
    </form>
  )
}

export default IntegrationCredentialForm
