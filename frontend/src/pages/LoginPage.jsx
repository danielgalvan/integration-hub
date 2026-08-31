import { useEffect, useState } from 'react'
import { getEnvironments } from '../services/environmentService'
import './LoginPage.css'

function LoginPage({ onLogin }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [environment, setEnvironment] = useState('')
  const [environments, setEnvironments] = useState([])
  const [loadingEnvironments, setLoadingEnvironments] =
    useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function loadEnvironments() {
      try {
        const data = await getEnvironments()

        setEnvironments(data)

        if (data.length > 0) {
          setEnvironment(data[0].id)
        }
      } catch (err) {
        setError(
          err?.message ||
            'Não foi possível carregar os ambientes.',
        )
      } finally {
        setLoadingEnvironments(false)
      }
    }

    loadEnvironments()
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()

    if (!username.trim() || !password) {
      setError('Informe usuário e senha.')
      return
    }

    if (!environment) {
      setError('Selecione um ambiente.')
      return
    }

    const selectedEnvironment =
      environments.find(
        (item) => item.id === environment,
      )

    if (!selectedEnvironment) {
      setError('Ambiente inválido.')
      return
    }

    try {
      setLoading(true)
      setError(null)

      await onLogin({
        username: username.trim(),
        password,
        environment: selectedEnvironment.id,
        environmentName: selectedEnvironment.name,
      })
    } catch (err) {
      setError(
        err?.message ||
          'Não foi possível realizar o login.',
      )
    } finally {
      setLoading(false)
    }
  }

  const formDisabled =
    loading || loadingEnvironments

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
            autoComplete="off"
          >
            <div className="login-page__field">
              <label htmlFor="username">
                Usuário
              </label>

              <input
                id="username"
                name="ihub-username"
                type="text"
                autoComplete="off"
                value={username}
                onChange={(event) =>
                  setUsername(event.target.value)
                }
                placeholder="Digite seu usuário"
                disabled={formDisabled}
                autoFocus
              />
            </div>

            <div className="login-page__field">
              <label htmlFor="password">
                Senha
              </label>

              <input
                id="password"
                name="ihub-password"
                type="password"
                autoComplete="new-password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Digite sua senha"
                disabled={formDisabled}
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
                disabled={formDisabled}
              >
                {loadingEnvironments && (
                  <option value="">
                    Carregando ambientes...
                  </option>
                )}

                {!loadingEnvironments &&
                  environments.length === 0 && (
                    <option value="">
                      Nenhum ambiente disponível
                    </option>
                  )}

                {environments.map((item) => (
                  <option
                    key={item.id}
                    value={item.id}
                  >
                    {item.name}
                  </option>
                ))}
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
              disabled={
                formDisabled || !environment
              }
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
