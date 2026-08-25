import './EndpointList.css'

function EndpointList({
  endpoints = [],
  onDelete,
}) {
  if (endpoints.length === 0) {
    return (
      <div className="endpoint-list endpoint-list--empty">
        <div className="endpoint-list__empty-icon">
          ↔
        </div>

        <h3 className="endpoint-list__empty-title">
          Nenhum endpoint cadastrado
        </h3>

        <p className="endpoint-list__empty-description">
          Os endpoints desta integração serão exibidos aqui.
        </p>
      </div>
    )
  }

  return (
    <div className="endpoint-list">
      {endpoints.map((endpoint) => (
        <div
          key={endpoint.id}
          className="endpoint-list__item"
        >
          <div className="endpoint-list__info">
            <h3 className="endpoint-list__name">
              {endpoint.name}
            </h3>

            <div className="endpoint-list__details">
              <span className="endpoint-list__method">
                {endpoint.method}
              </span>

              <span className="endpoint-list__path">
                {endpoint.path}
              </span>
            </div>
          </div>

          <div className="endpoint-list__right">
            <span
              className={`endpoint-list__status ${
                endpoint.active === 'S'
                  ? 'endpoint-list__status--active'
                  : 'endpoint-list__status--inactive'
              }`}
            >
              {endpoint.active === 'S' ? 'Ativo' : 'Inativo'}
            </span>

            <div className="endpoint-list__actions">
              <button
                type="button"
                className="endpoint-list__delete"
                onClick={() => onDelete(endpoint)}
              >
                Excluir
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

export default EndpointList