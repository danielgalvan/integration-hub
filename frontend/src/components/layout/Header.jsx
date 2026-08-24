import './Header.css'

function Header() {
  return (
    <header className="header">
      <div className="header__content">
        <div>
          <div className="header__eyebrow">Integration Hub</div>
          <h1 className="header__title">Integrações</h1>
        </div>

        <div className="header__actions">
          <div className="header__user">
            <div className="header__avatar">
              DG
            </div>

            <div className="header__user-info">
              <span className="header__user-name">Daniel</span>
              <span className="header__user-role">Administrador</span>
            </div>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Header