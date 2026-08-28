import { useState } from 'react'
import './LoginPage.css'

function LoginPage({ onLogin }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [environment, setEnvironment] = useState('DEVELOPMENT')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()

    if (!username.trim() || !password) {
      setError('Informe usuário e senha.')
      return
    }

    try {
      setLoading(true)
      setError(null)

      await onLogin({
        username: username.trim(),
        password,
        environment,
      })
    } catch (err) {
      setError(
        err?.message || 'Não foi possível realizar o login.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-page__panel">
        <div className="login-page__brand">
          <div className="login-page__logo">
            IH
          </div>

          <div>
            <h1 className="login-page__title">
              Integration Hub
            </h1>

            <p className="login-page__subtitle">
              Plataforma de integrações
            </p>
          </div>
        </div>

        <div className="login-page__content">
          <div className="login-page__heading">
            <h2>Acessar plataforma</h2>

            <p>
              Informe suas credenciais e selecione
              o ambiente que deseja acessar.
            </p>
          </div>

          <form
            className="login-page__form"
            onSubmit={handleSubmit}
          >
            <div className="login-page__field">
              <label htmlFor="username">
                Usuário
              </label>

              <input
                id="username"
                name="username"
                type="text"
                autoComplete="username"
                value={username}
                onChange={(event) =>
                  setUsername(event.target.value)
                }
                placeholder="Digite seu usuário"
                disabled={loading}
                autoFocus
              />
            </div>

            <div className="login-page__field">
              <label htmlFor="password">
                Senha
              </label>

              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Digite sua senha"
                disabled={loading}
              />
            </div>

            <div className="login-page__field">
              <label htmlFor="environment">
                Ambiente
              </label>

              <select
                id="environment"
                name="environment"
                value={environment}
                onChange={(event) =>
                  setEnvironment(event.target.value)
                }
                disabled={loading}
              >
                <option value="DEVELOPMENT">
                  Desenvolvimento
                </option>

                <option value="HOMOLOGATION">
                  Homologação
                </option>
              </select>
            </div>

            {error && (
              <div
                className="login-page__error"
                role="alert"
              >
                {error}
              </div>
            )}

            <button
              className="login-page__button"
              type="submit"
              disabled={loading}
            >
              {loading
                ? 'Entrando...'
                : 'Entrar'}
            </button>
          </form>
        </div>

        <div className="login-page__footer">
          Integration Hub
        </div>
      </div>
    </div>
  )
}

export default LoginPage
