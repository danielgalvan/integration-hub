import './IntegrationList.css'

function IntegrationList({
  integrations = [],
  canEdit,
  onOpenEndpoints,
  onEdit,
  onDelete,
  onGenerateApiKey,
}) {
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
      {integrations.map((integration) => {
        const usesApiKey =
          integration.authType === 'API_KEY'

        const hasApiKey =
          Boolean(integration.apiKeyCreatedAt)

        return (
          <div
            key={integration.id}
            className="integration-list__item"
          >
            <div className="integration-list__info">
              <h3 className="integration-list__name">
                {integration.name}
              </h3>

              <span className="integration-list__path">
                {integration.basePath}
              </span>

              <div className="integration-list__authentication">
                <span className="integration-list__authentication-label">
                  Autenticação:
                </span>

                <span className="integration-list__authentication-value">
                  {usesApiKey
                    ? 'API Key'
                    : 'Sem autenticação'}
                </span>
              </div>
            </div>

            <div className="integration-list__right">
              <span
                className={`integration-list__status ${
                  integration.active === 'S'
                    ? 'integration-list__status--active'
                    : 'integration-list__status--inactive'
                }`}
              >
                {integration.active === 'S'
                  ? 'Ativa'
                  : 'Inativa'}
              </span>

              <div className="integration-list__actions">
                <button
                  type="button"
                  className="integration-list__endpoints"
                  onClick={() =>
                    onOpenEndpoints(integration)
                  }
                >
                  Endpoints
                </button>

                <button
                  type="button"
                  className="integration-list__edit"
                  onClick={() =>
                    onEdit(integration)
                  }
                >
                  {canEdit
                    ? 'Editar'
                    : 'Visualizar'}
                </button>

                {canEdit && usesApiKey && (
                  <button
                    type="button"
                    className="integration-list__api-key"
                    onClick={() =>
                      onGenerateApiKey(integration)
                    }
                  >
                    {hasApiKey
                      ? 'Regenerar API Key'
                      : 'Gerar API Key'}
                  </button>
                )}

                {canEdit && (
                  <button
                    type="button"
                    className="integration-list__delete"
                    onClick={() =>
                      onDelete(integration)
                    }
                  >
                    Excluir
                  </button>
                )}
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}

export default IntegrationList
