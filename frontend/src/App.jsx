import {
  useCallback,
  useEffect,
  useState,
} from 'react'
import './App.css'
import Header from './components/layout/Header'
import Sidebar from './components/layout/Sidebar'
import ChangePasswordPage from './pages/ChangePasswordPage'
import EndpointsPage from './pages/EndpointsPage'
import IntegrationsPage from './pages/IntegrationsPage'
import LoginPage from './pages/LoginPage'
import UsersPage from './pages/UsersPage'
import {
  changePassword,
  getAuthenticatedUser,
  login,
} from './services/authService'
import {
  clearAuth,
  getEnvironmentName,
  getRole,
  getToken,
  isPasswordChangeRequired,
  saveAuth,
  setPasswordChangeRequired,
} from './utils/authStorage'

function App() {
  const [token, setToken] = useState(
    () => getToken(),
  )

  const [role, setRole] = useState(
    () => getRole(),
  )

  const [
    environmentName,
    setEnvironmentName,
  ] = useState(
    () => getEnvironmentName(),
  )

  const [user, setUser] = useState(null)

  const [
    passwordChangeRequired,
    setPasswordChangeRequiredState,
  ] = useState(
    () => isPasswordChangeRequired(),
  )

  const [
    currentPage,
    setCurrentPage,
  ] = useState('integrations')

  const [
    selectedIntegration,
    setSelectedIntegration,
  ] = useState(null)

  const clearSession = useCallback(() => {
    clearAuth()

    setToken(null)
    setRole(null)
    setEnvironmentName(null)
    setUser(null)

    setPasswordChangeRequiredState(false)

    setSelectedIntegration(null)
    setCurrentPage('integrations')
  }, [])

  useEffect(() => {
    function handleUnauthorized() {
      clearSession()
    }

    window.addEventListener(
      'ihub:unauthorized',
      handleUnauthorized,
    )

    return () => {
      window.removeEventListener(
        'ihub:unauthorized',
        handleUnauthorized,
      )
    }
  }, [clearSession])

  useEffect(() => {
    if (!token || passwordChangeRequired) {
      return
    }

    async function loadAuthenticatedUser() {
      try {
        const authenticatedUser =
          await getAuthenticatedUser()

        setUser(authenticatedUser)
      } catch {
        setUser(null)
      }
    }

    loadAuthenticatedUser()
  }, [
    token,
    passwordChangeRequired,
  ])

  async function handleLogin(credentials) {
    const response = await login(
      credentials.username,
      credentials.password,
      credentials.environment,
    )

    const tokenRole =
      getRoleFromToken(response.token)

    saveAuth({
      token: response.token,
      role: tokenRole,
      environment: credentials.environment,
      environmentName:
        credentials.environmentName,
      passwordChangeRequired:
        response.passwordChangeRequired,
    })

    setToken(response.token)
    setRole(tokenRole)
    setEnvironmentName(
      credentials.environmentName,
    )

    setPasswordChangeRequiredState(
      response.passwordChangeRequired,
    )

    setUser(null)
    setSelectedIntegration(null)
    setCurrentPage('integrations')
  }

  async function handleChangePassword(
    newPassword,
  ) {
    await changePassword(
      token,
      newPassword,
    )

    setPasswordChangeRequired(false)

    setPasswordChangeRequiredState(false)
  }

  function handleOpenEndpoints(integration) {
    setSelectedIntegration(integration)
    setCurrentPage('endpoints')
  }

  function handleBackToIntegrations() {
    setSelectedIntegration(null)
    setCurrentPage('integrations')
  }

  function handleOpenUsers() {
    if (role !== 'A') {
      return
    }

    setSelectedIntegration(null)
    setCurrentPage('users')
  }

  function handleLogout() {
    clearSession()
  }

  if (!token) {
    return (
      <LoginPage
        onLogin={handleLogin}
      />
    )
  }

  if (passwordChangeRequired) {
    return (
      <ChangePasswordPage
        onChangePassword={
          handleChangePassword
        }
        onLogout={handleLogout}
      />
    )
  }

  return (
    <div className="app">
      <Sidebar
        role={role}
        environmentName={environmentName}
        onOpenIntegrations={
          handleBackToIntegrations
        }
        onOpenUsers={handleOpenUsers}
      />

      <div className="app__content">
        <Header
          user={user}
          onLogout={handleLogout}
        />

        <main className="app__main">
          {currentPage === 'integrations' && (
            <IntegrationsPage
              role={role}
              onOpenEndpoints={
                handleOpenEndpoints
              }
            />
          )}

          {currentPage === 'endpoints' && (
            <EndpointsPage
              role={role}
              integration={
                selectedIntegration
              }
              onBack={
                handleBackToIntegrations
              }
            />
          )}

          {currentPage === 'users' &&
            role === 'A' && (
              <UsersPage />
            )}
        </main>
      </div>
    </div>
  )
}

function getRoleFromToken(token) {
  try {
    const payload =
      token.split('.')[1]

    const normalizedPayload =
      payload
        .replace(/-/g, '+')
        .replace(/_/g, '/')

    const decodedPayload =
      decodeURIComponent(
        atob(normalizedPayload)
          .split('')
          .map(
            (character) =>
              `%${character
                .charCodeAt(0)
                .toString(16)
                .padStart(2, '0')}`,
          )
          .join(''),
      )

    return JSON.parse(
      decodedPayload,
    ).role
  } catch {
    return null
  }
}

export default App
