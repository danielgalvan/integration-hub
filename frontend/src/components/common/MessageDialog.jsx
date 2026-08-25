import './MessageDialog.css'

function MessageDialog({
  open,
  title = 'Atenção',
  message,
  onClose,
}) {
  if (!open) {
    return null
  }

  return (
    <div className="message-dialog__backdrop">
      <div
        className="message-dialog"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="message-dialog-title"
      >
        <h3
          id="message-dialog-title"
          className="message-dialog__title"
        >
          {title}
        </h3>

        <p className="message-dialog__message">
          {message}
        </p>

        <div className="message-dialog__actions">
          <button
            type="button"
            className="message-dialog__close"
            onClick={onClose}
          >
            OK
          </button>
        </div>
      </div>
    </div>
  )
}

export default MessageDialog