import './Header.css'

function Header({
  user,
  onLogout,
}) {
  const displayName =
    user?.name || user?.username || 'Usuário'

  const initials = getInitials(displayName)

  const roleName = getRoleName(user?.role)

  return (
    <header className="header">
      <div className="header__content">
        <div>
          <div className="header__eyebrow">
            Integration Hub
          </div>

          <h1 className="header__title">
            Integrações
          </h1>
        </div>

        <div className="header__actions">
          <div className="header__user">
            <div className="header__avatar">
              {initials}
            </div>

            <div className="header__user-info">
              <span className="header__user-name">
                {displayName}
              </span>

              <span className="header__user-role">
                {roleName}
              </span>
            </div>
          </div>

          <button
            type="button"
            className="header__logout"
            onClick={onLogout}
          >
            Sair
          </button>
        </div>
      </div>
    </header>
  )
}

function getRoleName(role) {
  switch (role) {
    case 'A':
      return 'Administrador'

    case 'C':
      return 'Criador'

    case 'U':
      return 'Consumidor'

    default:
      return ''
  }
}

function getInitials(name) {
  const parts = name
    .trim()
    .split(/\s+/)
    .filter(Boolean)

  if (parts.length === 0) {
    return ''
  }

  if (parts.length === 1) {
    return parts[0]
      .substring(0, 2)
      .toUpperCase()
  }

  return (
    parts[0][0] +
    parts[parts.length - 1][0]
  ).toUpperCase()
}

export default Header
