import { useEffect, useState } from 'react'
import './App.css'
import Header from './components/layout/Header'
import Sidebar from './components/layout/Sidebar'
import EndpointsPage from './pages/EndpointsPage'
import IntegrationsPage from './pages/IntegrationsPage'
import LoginPage from './pages/LoginPage'
import { login } from './services/authService'
import {
  getToken,
  removeToken,
  saveToken,
} from './utils/authStorage'

function App() {
  const [token, setToken] = useState(() => getToken())
  const [currentPage, setCurrentPage] = useState('integrations')
  const [selectedIntegration, setSelectedIntegration] = useState(null)

  useEffect(() => {
    function handleUnauthorized() {
      removeToken()
      setToken(null)
      setSelectedIntegration(null)
      setCurrentPage('integrations')
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
  }, [])

  async function handleLogin(credentials) {
    const response = await login(
      credentials.username,
      credentials.password,
    )

    saveToken(response.token)
    setToken(response.token)
  }

  function handleOpenEndpoints(integration) {
    setSelectedIntegration(integration)
    setCurrentPage('endpoints')
  }

  function handleBackToIntegrations() {
    setSelectedIntegration(null)
    setCurrentPage('integrations')
  }

  function handleLogout() {
    removeToken()
    setToken(null)
    setSelectedIntegration(null)
    setCurrentPage('integrations')
  }

  if (!token) {
    return (
      <LoginPage
        onLogin={handleLogin}
      />
    )
  }

  return (
    <div className="app">
      <Sidebar
        onOpenIntegrations={handleBackToIntegrations}
      />

      <div className="app__content">
        <Header
          onLogout={handleLogout}
        />

        <main className="app__main">
          {currentPage === 'integrations' && (
            <IntegrationsPage
              onOpenEndpoints={handleOpenEndpoints}
            />
          )}

          {currentPage === 'endpoints' && (
            <EndpointsPage
              integration={selectedIntegration}
              onBack={handleBackToIntegrations}
            />
          )}
        </main>
      </div>
    </div>
  )
}

export default App
