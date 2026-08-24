import './IntegrationList.css'

function IntegrationList({ integrations = [] }) {
  if (integrations.length === 0) {
    return (
      <div className="integration-list integration-list--empty">
        <div className="integration-list__empty-icon">
          ↔
        </div>

        <h3 className="integration-list__empty-title">
          Nenhuma integração carregada
        </h3>

        <p className="integration-list__empty-description">
          As integrações cadastradas serão exibidas aqui.
        </p>
      </div>
    )
  }

  return (
    <div className="integration-list">
      {integrations.map((integration) => (
        <div
          key={integration.id}
          className="integration-list__item"
        >
          <div>
            <h3 className="integration-list__name">
              {integration.name}
            </h3>

            <span className="integration-list__path">
              {integration.basePath}
            </span>
          </div>

          <span
            className={`integration-list__status ${
              integration.active === 'S'
                ? 'integration-list__status--active'
                : 'integration-list__status--inactive'
            }`}
          >
            {integration.active === 'S' ? 'Ativa' : 'Inativa'}
          </span>
        </div>
      ))}
    </div>
  )
}

export default IntegrationList