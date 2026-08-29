import { useState } from 'react'
import './TemporaryPasswordDialog.css'

function TemporaryPasswordDialog({
  password,
  onClose,
}) {
  const [copied, setCopied] = useState(false)

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(password)
      setCopied(true)

      setTimeout(() => {
        setCopied(false)
      }, 2000)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div
      className="temporary-password-dialog__backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <div
        className="temporary-password-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="temporary-password-title"
      >
        <div className="temporary-password-dialog__header">
          <div className="temporary-password-dialog__icon">
            ✓
          </div>

          <div>
            <h2 id="temporary-password-title">
              Senha temporária
            </h2>

            <p>
              O usuário deverá alterar esta senha
              no primeiro acesso.
            </p>
          </div>
        </div>

        <div className="temporary-password-dialog__content">
          <label>
            Senha gerada
          </label>

          <div className="temporary-password-dialog__password">
            <code>{password}</code>

            <button
              type="button"
              onClick={handleCopy}
            >
              {copied ? 'Copiado!' : 'Copiar'}
            </button>
          </div>

          <div className="temporary-password-dialog__warning">
            <strong>Importante</strong>

            <span>
              Copie e envie esta senha ao usuário.
              Ela não poderá ser consultada novamente.
            </span>
          </div>
        </div>

        <div className="temporary-password-dialog__actions">
          <button
            type="button"
            onClick={onClose}
          >
            Entendi
          </button>
        </div>
      </div>
    </div>
  )
}

export default TemporaryPasswordDialog
