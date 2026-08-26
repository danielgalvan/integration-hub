import './Sidebar.css'

function Sidebar({ onOpenIntegrations }) {
  return (
    <aside className="sidebar">
      <div className="sidebar__brand">
        <div className="sidebar__logo">IH</div>

        <div>
          <div className="sidebar__title">Integration Hub</div>
          <div className="sidebar__subtitle">API Management</div>
        </div>
      </div>

      <nav className="sidebar__nav">
        <button
          type="button"
          className="sidebar__item sidebar__item--active"
          onClick={onOpenIntegrations}
        >
          <span className="sidebar__icon">⌘</span>
          <span>Integrações</span>
        </button>
      </nav>

      <div className="sidebar__footer">
        <span className="sidebar__status"></span>

        <div>
          <div className="sidebar__environment">Ambiente local</div>
          <div className="sidebar__version">Integration Hub v1</div>
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
