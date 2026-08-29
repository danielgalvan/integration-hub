import { useState } from 'react'
import './ChangePasswordPage.css'

function ChangePasswordPage({
  onChangePassword,
  onLogout,
}) {
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] =
    useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()

    if (!newPassword || !confirmPassword) {
      setError('Informe e confirme a nova senha.')
      return
    }

    if (newPassword.length < 6) {
      setError(
        'A nova senha deve possuir no mínimo 6 caracteres.',
      )
      return
    }

    if (newPassword !== confirmPassword) {
      setError('As senhas não conferem.')
      return
    }

    try {
      setLoading(true)
      setError(null)

      await onChangePassword(newPassword)
    } catch (err) {
      setError(
        err?.message ||
          'Não foi possível alterar a senha.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="change-password-page">
      <div className="change-password-page__panel">
        <div className="change-password-page__brand">
          <div className="change-password-page__logo">
            IH
          </div>

          <div>
            <h1 className="change-password-page__title">
              Integration Hub
            </h1>

            <p className="change-password-page__subtitle">
              Plataforma de integrações
            </p>
          </div>
        </div>

        <div className="change-password-page__content">
          <div className="change-password-page__heading">
            <h2>Alterar senha</h2>

            <p>
              Sua senha atual é temporária. Defina uma
              nova senha para continuar.
            </p>
          </div>

          <form
            className="change-password-page__form"
            onSubmit={handleSubmit}
          >
            <div className="change-password-page__field">
              <label htmlFor="newPassword">
                Nova senha
              </label>

              <input
                id="newPassword"
                name="newPassword"
                type="password"
                autoComplete="new-password"
                value={newPassword}
                onChange={(event) =>
                  setNewPassword(event.target.value)
                }
                placeholder="Digite a nova senha"
                disabled={loading}
                autoFocus
              />
            </div>

            <div className="change-password-page__field">
              <label htmlFor="confirmPassword">
                Confirmar senha
              </label>

              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(event) =>
                  setConfirmPassword(
                    event.target.value,
                  )
                }
                placeholder="Digite novamente a senha"
                disabled={loading}
              />
            </div>

            {error && (
              <div
                className="change-password-page__error"
                role="alert"
              >
                {error}
              </div>
            )}

            <button
              className="change-password-page__button"
              type="submit"
              disabled={loading}
            >
              {loading
                ? 'Salvando...'
                : 'Alterar senha'}
            </button>

            <button
              className="change-password-page__logout"
              type="button"
              onClick={onLogout}
              disabled={loading}
            >
              Sair
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

export default ChangePasswordPage
