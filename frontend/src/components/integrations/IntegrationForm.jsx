import { useState } from 'react'
import './IntegrationForm.css'

function IntegrationForm({
  integration = null,
  readOnly = false,
  onCancel,
  onSubmit,
}) {
  const [name, setName] = useState(
    integration?.name || '',
  )

  const [description, setDescription] = useState(
    integration?.description || '',
  )

  const [basePath, setBasePath] = useState(
    integration?.basePath || '',
  )

  const [active, setActive] = useState(
    integration?.active || 'S',
  )

  const isEditing = integration !== null

  function handleSubmit(event) {
    event.preventDefault()

    if (readOnly) {
      return
    }

    onSubmit({
      name,
      description,
      basePath,
      active,
    })
  }

  return (
    <form
      className="integration-form"
      onSubmit={handleSubmit}
    >
      <div className="integration-form__field">
        <label htmlFor="name">
          Nome
        </label>

        <input
          id="name"
          name="name"
          type="text"
          value={name}
          onChange={(event) =>
            setName(event.target.value)
          }
          placeholder="Ex: Pedidos"
          required
          readOnly={readOnly}
        />
      </div>

      <div className="integration-form__field">
        <label htmlFor="description">
          Descrição
        </label>

        <textarea
          id="description"
          name="description"
          rows="3"
          value={description}
          onChange={(event) =>
            setDescription(
              event.target.value,
            )
          }
          placeholder="Descreva o objetivo da integração"
          readOnly={readOnly}
        />
      </div>

      <div className="integration-form__field">
        <label htmlFor="basePath">
          Base Path
        </label>

        <input
          id="basePath"
          name="basePath"
          type="text"
          value={basePath}
          onChange={(event) =>
            setBasePath(event.target.value)
          }
          placeholder="/api/pedidos"
          required
          readOnly={readOnly}
        />
      </div>

      <div className="integration-form__field">
        <span className="integration-form__label">
          Status
        </span>

        <label className="integration-form__checkbox">
          <input
            id="active"
            name="active"
            type="checkbox"
            checked={active === 'S'}
            onChange={(event) =>
              setActive(
                event.target.checked
                  ? 'S'
                  : 'N',
              )
            }
            disabled={readOnly}
          />

          <span>Ativa</span>
        </label>
      </div>

      <div className="integration-form__actions">
        <button
          type="button"
          className="integration-form__cancel"
          onClick={onCancel}
        >
          {readOnly ? 'Voltar' : 'Cancelar'}
        </button>

        {!readOnly && (
          <button
            type="submit"
            className="integration-form__submit"
          >
            {isEditing
              ? 'Salvar alterações'
              : 'Salvar integração'}
          </button>
        )}
      </div>
    </form>
  )
}

export default IntegrationForm
