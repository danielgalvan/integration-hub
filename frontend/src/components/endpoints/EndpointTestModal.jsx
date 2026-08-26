import { useState } from 'react'
import './EndpointTestModal.css'
import { executeEndpoint } from '../../services/endpointService'

function getInputProperties(parameterType) {
  switch (parameterType?.toUpperCase()) {
    case 'DATE':
      return {
        type: 'text',
        placeholder: 'aaaa-mm-dd',
        inputMode: 'numeric',
      }

    case 'TIMESTAMP':
      return {
        type: 'text',
        placeholder: 'aaaa-mm-ddThh:mm:ss',
        inputMode: 'numeric',
      }

    case 'NUMBER':
      return {
        type: 'text',
        inputMode: 'decimal',
      }

    default:
      return {
        type: 'text',
      }
  }
}

function getParameterHint(parameterType) {
  switch (parameterType?.toUpperCase()) {
    case 'DATE':
      return 'DATE — formato: aaaa-mm-dd'

    case 'TIMESTAMP':
      return 'TIMESTAMP — formato: aaaa-mm-ddThh:mm:ss'

    default:
      return parameterType
  }
}

function createInitialParameters(endpoint) {
  const initialParameters = {}

  endpoint?.parameters?.forEach((parameter) => {
    initialParameters[parameter.name] = ''
  })

  return initialParameters
}

function EndpointTestModal({
  open,
  integration,
  endpoint,
  onClose,
}) {
  const [parameters, setParameters] = useState({})
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [executing, setExecuting] = useState(false)
  const [copied, setCopied] = useState(false)

  if (!open || !endpoint || !integration) {
    return null
  }

  function handleClose() {
    setParameters(createInitialParameters(endpoint))
    setResult(null)
    setError(null)
    setExecuting(false)
    setCopied(false)
    onClose()
  }

  function handleParameterChange(event) {
    const { name, value } = event.target

    setParameters((currentParameters) => ({
      ...currentParameters,
      [name]: value,
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()

    try {
      setExecuting(true)
      setError(null)
      setResult(null)
      setCopied(false)

      const response = await executeEndpoint(
        integration,
        endpoint,
        parameters,
      )

      setResult(response)
    } catch (err) {
      setError(err.message)
    } finally {
      setExecuting(false)
    }
  }

  async function handleCopyUrl() {
    try {
      await navigator.clipboard.writeText(result.url)
      setCopied(true)
    } catch {
      setError('Não foi possível copiar a URL.')
    }
  }

  return (
    <div
      className="endpoint-test-modal__backdrop"
      role="presentation"
    >
      <section
        className="endpoint-test-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="endpoint-test-modal-title"
      >
        <header className="endpoint-test-modal__header">
          <div>
            <h2
              id="endpoint-test-modal-title"
              className="endpoint-test-modal__title"
            >
              Testar endpoint
            </h2>

            <p className="endpoint-test-modal__description">
              {endpoint.method} {integration.basePath}
              {endpoint.path}
            </p>
          </div>

          <button
            type="button"
            className="endpoint-test-modal__close"
            aria-label="Fechar"
            onClick={handleClose}
          >
            ×
          </button>
        </header>

        <form
          className="endpoint-test-modal__form"
          onSubmit={handleSubmit}
        >
          <div className="endpoint-test-modal__parameters">
            <h3 className="endpoint-test-modal__section-title">
              Parâmetros
            </h3>

            {endpoint.parameters?.length > 0 ? (
              endpoint.parameters.map((parameter) => {
                const inputProperties = getInputProperties(
                  parameter.type,
                )

                return (
                  <label
                    key={parameter.name}
                    className="endpoint-test-modal__field"
                  >
                    <span>
                      {parameter.name}

                      {parameter.required && (
                        <strong> *</strong>
                      )}
                    </span>

                    <input
                      type={inputProperties.type}
                      name={parameter.name}
                      value={parameters[parameter.name] || ''}
                      placeholder={inputProperties.placeholder}
                      inputMode={inputProperties.inputMode}
                      required={parameter.required}
                      onChange={handleParameterChange}
                    />

                    <small>
                      {getParameterHint(parameter.type)}
                    </small>
                  </label>
                )
              })
            ) : (
              <p className="endpoint-test-modal__empty">
                Este endpoint não possui parâmetros.
              </p>
            )}
          </div>

          <div className="endpoint-test-modal__actions">
            <button
              type="button"
              className="endpoint-test-modal__cancel"
              disabled={executing}
              onClick={handleClose}
            >
              Fechar
            </button>

            <button
              type="submit"
              className="endpoint-test-modal__execute"
              disabled={executing}
            >
              {executing ? 'Executando...' : 'Executar'}
            </button>
          </div>
        </form>

        {error && (
          <div className="endpoint-test-modal__error">
            {error}
          </div>
        )}

        {result && (
          <div className="endpoint-test-modal__result">
            <h3 className="endpoint-test-modal__section-title">
              Resultado
            </h3>

            <div className="endpoint-test-modal__metadata">
              <span
                className={
                  result.success
                    ? 'endpoint-test-modal__status endpoint-test-modal__status--success'
                    : 'endpoint-test-modal__status endpoint-test-modal__status--error'
                }
              >
                HTTP {result.status}
              </span>

              <span>{result.duration} ms</span>
            </div>

            <div className="endpoint-test-modal__url">
              <div className="endpoint-test-modal__url-header">
                <strong>URL executada</strong>

                <button
                  type="button"
                  className="endpoint-test-modal__copy-url"
                  onClick={handleCopyUrl}
                >
                  {copied ? 'Copiada!' : 'Copiar URL'}
                </button>
              </div>

              <code>{result.url}</code>
            </div>

            <pre className="endpoint-test-modal__response">
              {typeof result.data === 'string'
                ? result.data
                : JSON.stringify(result.data, null, 2)}
            </pre>

            <div className="endpoint-test-modal__actions endpoint-test-modal__result-actions">
              <button
                type="button"
                className="endpoint-test-modal__cancel"
                onClick={handleClose}
              >
                Fechar
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  )
}

export default EndpointTestModal
