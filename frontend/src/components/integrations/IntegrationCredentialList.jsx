import './IntegrationCredentialList.css'

function IntegrationCredentialList({
  credentials = [],
  onViewApiKey,
  onEdit,
  onToggleActive,
  onRegenerateApiKey,
}) {
  if (credentials.length === 0) {
    return (
      <div className="integration-credential-list integration-credential-list--empty">
        <div className="integration-credential-list__empty-icon">
          🔑
        </div>

        <h3 className="integration-credential-list__empty-title">
          Nenhuma credencial cadastrada
        </h3>

        <p className="integration-credential-list__empty-description">
          Crie uma credencial para permitir o
          consumo dos endpoints desta integração.
        </p>
      </div>
    )
  }

  return (
    <div className="integration-credential-list">
      {credentials.map((credential) => {
        const active =
          credential.active === 'S'

        return (
          <div
            key={credential.id}
            className="integration-credential-list__item"
          >
            <div className="integration-credential-list__info">
              <h3 className="integration-credential-list__name">
                {credential.name}
              </h3>

              <div className="integration-credential-list__details">
                <span>
                  Último uso:{' '}
                  <strong>
                    {formatLastUsedAt(
                      credential.lastUsedAt,
                    )}
                  </strong>
                </span>
              </div>
            </div>

            <div className="integration-credential-list__right">
              <span
                className={`integration-credential-list__status ${
                  active
                    ? 'integration-credential-list__status--active'
                    : 'integration-credential-list__status--inactive'
                }`}
              >
                {active
                  ? 'Ativa'
                  : 'Inativa'}
              </span>

              <div className="integration-credential-list__actions">
                <button
                  type="button"
                  className="integration-credential-list__view"
                  onClick={() =>
                    onViewApiKey(credential)
                  }
                >
                  Visualizar API Key
                </button>

                <button
                  type="button"
                  className="integration-credential-list__edit"
                  onClick={() =>
                    onEdit(credential)
                  }
                >
                  Editar
                </button>

                <button
                  type="button"
                  className="integration-credential-list__toggle"
                  onClick={() =>
                    onToggleActive(credential)
                  }
                >
                  {active
                    ? 'Desativar'
                    : 'Ativar'}
                </button>

                <button
                  type="button"
                  className="integration-credential-list__regenerate"
                  onClick={() =>
                    onRegenerateApiKey(
                      credential,
                    )
                  }
                >
                  Regenerar API Key
                </button>
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}

function formatLastUsedAt(value) {
  if (!value) {
    return 'Nunca utilizada'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(
    'pt-BR',
    {
      dateStyle: 'short',
      timeStyle: 'medium',
    },
  ).format(date)
}

export default IntegrationCredentialList
