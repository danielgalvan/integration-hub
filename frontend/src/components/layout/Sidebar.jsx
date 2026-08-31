import './Sidebar.css'

function Sidebar({
  role,
  environment,
  onOpenIntegrations,
  onOpenUsers,
}) {
  const isAdmin = role === 'A'

  function getEnvironmentLabel() {
    switch (environment) {
      case 'DEVELOPMENT':
      case 'development':
        return 'Desenvolvimento'

      case 'HOMOLOGATION':
        return 'Homologação'

      case 'PRODUCTION':
        return 'Produção'

      case 'cloud':
        return 'Oracle Cloud'

      default:
        return environment || 'Ambiente'
    }
  }

  return (
    <aside className="sidebar">
      <div className="sidebar__brand">
        <div className="sidebar__logo">IH</div>

        <div>
          <div className="sidebar__title">
            Integration Hub
          </div>

          <div className="sidebar__subtitle">
            API Management
          </div>
        </div>
      </div>

      <nav className="sidebar__nav">
        <button
          type="button"
          className="sidebar__item"
          onClick={onOpenIntegrations}
        >
          <span className="sidebar__icon">⌘</span>
          <span>Integrações</span>
        </button>

        {isAdmin && (
          <button
            type="button"
            className="sidebar__item"
            onClick={onOpenUsers}
          >
            <span className="sidebar__icon">👤</span>
            <span>Users</span>
          </button>
        )}
      </nav>

      <div className="sidebar__footer">
        <span className="sidebar__status"></span>

        <div>
          <div className="sidebar__environment">
            {getEnvironmentLabel()}
          </div>

          <div className="sidebar__version">
            Integration Hub v1
          </div>
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
