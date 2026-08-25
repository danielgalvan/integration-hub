import { useState } from 'react'
import './IntegrationForm.css'

function IntegrationForm({
  integration = null,
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
        <label htmlFor="name">Nome</label>

        <input
          id="name"
          name="name"
          type="text"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="Ex: Pedidos"
          required
        />
      </div>

      <div className="integration-form__field">
        <label htmlFor="description">Descrição</label>

        <textarea
          id="description"
          name="description"
          rows="3"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          placeholder="Descreva o objetivo da integração"
        />
      </div>

      <div className="integration-form__field">
        <label htmlFor="basePath">Base Path</label>

        <input
          id="basePath"
          name="basePath"
          type="text"
          value={basePath}
          onChange={(event) => setBasePath(event.target.value)}
          placeholder="/api/pedidos"
          required
        />
      </div>

      <div className="integration-form__field">
        <label htmlFor="active">Status</label>

        <select
          id="active"
          name="active"
          value={active}
          onChange={(event) => setActive(event.target.value)}
        >
          <option value="S">Ativa</option>
          <option value="N">Inativa</option>
        </select>
      </div>

      <div className="integration-form__actions">
        <button
          type="button"
          className="integration-form__cancel"
          onClick={onCancel}
        >
          Cancelar
        </button>

        <button
          type="submit"
          className="integration-form__submit"
        >
          {isEditing
            ? 'Salvar alterações'
            : 'Salvar integração'}
        </button>
      </div>
    </form>
  )
}

export default IntegrationForm