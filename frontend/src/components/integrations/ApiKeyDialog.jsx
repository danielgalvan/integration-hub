import { useState } from 'react'
import './ApiKeyDialog.css'

function ApiKeyDialog({
  apiKey,
  onClose,
}) {
  const [copied, setCopied] = useState(false)

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(
        apiKey,
      )

      setCopied(true)

      window.setTimeout(() => {
        setCopied(false)
      }, 2000)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div
      className="api-key-dialog__overlay"
      role="presentation"
    >
      <div
        className="api-key-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="api-key-dialog-title"
      >
        <div className="api-key-dialog__header">
          <div>
            <h2
              id="api-key-dialog-title"
              className="api-key-dialog__title"
            >
              API Key gerada
            </h2>

            <p className="api-key-dialog__description">
              Copie e armazene esta chave em um
              local seguro.
            </p>
          </div>

          <button
            type="button"
            className="api-key-dialog__close"
            onClick={onClose}
            aria-label="Fechar"
          >
            ×
          </button>
        </div>

        <div className="api-key-dialog__content">
          <div className="api-key-dialog__warning">
            <strong>Atenção:</strong>{' '}
            esta API Key será exibida apenas
            uma vez. Depois de fechar esta
            janela, não será possível
            visualizá-la novamente.
          </div>

          <div className="api-key-dialog__key-container">
            <code className="api-key-dialog__key">
              {apiKey}
            </code>

            <button
              type="button"
              className="api-key-dialog__copy"
              onClick={handleCopy}
            >
              {copied
                ? 'Copiado!'
                : 'Copiar'}
            </button>
          </div>

          <div className="api-key-dialog__usage">
            <span className="api-key-dialog__usage-label">
              Header para consumo dos endpoints
            </span>

            <code className="api-key-dialog__usage-value">
              X-API-Key: {apiKey}
            </code>
          </div>
        </div>

        <div className="api-key-dialog__actions">
          <button
            type="button"
            className="api-key-dialog__confirm"
            onClick={onClose}
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  )
}

export default ApiKeyDialog
