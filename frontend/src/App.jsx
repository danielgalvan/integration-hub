import './App.css'
import Header from './components/layout/Header'
import Sidebar from './components/layout/Sidebar'
import IntegrationsPage from './pages/IntegrationsPage'

function App() {
  return (
    <div className="app">
      <Sidebar />

      <div className="app__content">
        <Header />

        <main className="app__main">
          <IntegrationsPage />
        </main>
      </div>
    </div>
  )
}

export default App