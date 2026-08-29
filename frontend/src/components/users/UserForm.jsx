import { useEffect, useState } from 'react'
import './UserForm.css'

function UserForm({
  user,
  saving,
  onSave,
  onCancel,
}) {
  const [form, setForm] = useState({
    username: '',
    name: '',
    email: '',
    status: 'A',
    type: 'U',
  })

  const [error, setError] = useState(null)

  useEffect(() => {
    if (user) {
      setForm({
        username: user.username || '',
        name: user.name || '',
        email: user.email || '',
        status: user.status || 'A',
        type: user.type || 'U',
      })

      setError(null)
      return
    }

    setForm({
      username: '',
      name: '',
      email: '',
      status: 'A',
      type: 'U',
    })

    setError(null)
  }, [user])

  function handleChange(event) {
    const { name, value } = event.target

    setForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    if (!form.username.trim()) {
      setError('Usuário é obrigatório.')
      return
    }

    if (!form.name.trim()) {
      setError('Nome é obrigatório.')
      return
    }

    if (
      form.email.trim() &&
      !isValidEmail(form.email.trim())
    ) {
      setError('Informe um e-mail válido.')
      return
    }

    setError(null)

    const payload = {
      username: form.username.trim(),
      name: form.name.trim(),
      email: form.email.trim() || null,
      type: form.type,
    }

    if (user) {
      payload.status = form.status
    }

    onSave(payload)
  }

  return (
    <div className="user-form">
      <div className="user-form__header">
        <div>
          <p className="user-form__eyebrow">
            {user ? 'Edição' : 'Cadastro'}
          </p>

          <h2>
            {user
              ? 'Editar usuário'
              : 'Novo usuário'}
          </h2>

          <p className="user-form__description">
            {user
              ? 'Atualize os dados e o perfil de acesso do usuário.'
              : 'Cadastre o usuário. A senha temporária será gerada automaticamente.'}
          </p>
        </div>
      </div>

      <form
        className="user-form__form"
        onSubmit={handleSubmit}
      >
        <div className="user-form__grid">
          <div className="user-form__field">
            <label htmlFor="username">
              Usuário
            </label>

            <input
              id="username"
              name="username"
              type="text"
              value={form.username}
              onChange={handleChange}
              disabled={saving}
              maxLength={100}
              autoComplete="off"
            />
          </div>

          <div className="user-form__field">
            <label htmlFor="name">
              Nome
            </label>

            <input
              id="name"
              name="name"
              type="text"
              value={form.name}
              onChange={handleChange}
              disabled={saving}
              maxLength={200}
              autoComplete="off"
            />
          </div>

          <div className="user-form__field">
            <label htmlFor="email">
              E-mail
            </label>

            <input
              id="email"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              disabled={saving}
              maxLength={200}
              autoComplete="off"
            />
          </div>

          <div className="user-form__field">
            <label htmlFor="type">
              Perfil
            </label>

            <select
              id="type"
              name="type"
              value={form.type}
              onChange={handleChange}
              disabled={saving}
            >
              <option value="A">
                Administrador
              </option>

              <option value="C">
                Criador
              </option>

              <option value="U">
                Consumidor
              </option>
            </select>
          </div>

          {user && (
            <div className="user-form__field">
              <label htmlFor="status">
                Status
              </label>

              <select
                id="status"
                name="status"
                value={form.status}
                onChange={handleChange}
                disabled={saving}
              >
                <option value="A">
                  Ativo
                </option>

                <option value="I">
                  Inativo
                </option>
              </select>
            </div>
          )}
        </div>

        {!user && (
          <div className="user-form__info">
            <strong>
              Senha temporária
            </strong>

            <span>
              O sistema irá gerar uma senha temporária após o cadastro.
              O usuário deverá alterá-la no primeiro login.
            </span>
          </div>
        )}

        {error && (
          <div
            className="user-form__error"
            role="alert"
          >
            {error}
          </div>
        )}

        <div className="user-form__actions">
          <button
            type="button"
            className="user-form__cancel-button"
            onClick={onCancel}
            disabled={saving}
          >
            Cancelar
          </button>

          <button
            type="submit"
            className="user-form__save-button"
            disabled={saving}
          >
            {saving
              ? 'Salvando...'
              : user
                ? 'Salvar alterações'
                : 'Criar usuário'}
          </button>
        </div>
      </form>
    </div>
  )
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export default UserForm
