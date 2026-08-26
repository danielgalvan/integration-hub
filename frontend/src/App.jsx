import { useState } from 'react'
import './App.css'
import Header from './components/layout/Header'
import Sidebar from './components/layout/Sidebar'
import EndpointsPage from './pages/EndpointsPage'
import IntegrationsPage from './pages/IntegrationsPage'

function App() {
  const [currentPage, setCurrentPage] = useState('integrations')
  const [selectedIntegration, setSelectedIntegration] = useState(null)

  function handleOpenEndpoints(integration) {
    setSelectedIntegration(integration)
    setCurrentPage('endpoints')
  }

  function handleBackToIntegrations() {
    setSelectedIntegration(null)
    setCurrentPage('integrations')
  }

  return (
    <div className="app">
      <Sidebar onOpenIntegrations={handleBackToIntegrations} />

      <div className="app__content">
        <Header />

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
