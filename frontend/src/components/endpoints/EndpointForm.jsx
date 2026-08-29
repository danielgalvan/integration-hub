import { useState } from 'react'
import './EndpointForm.css'

function EndpointForm({
  integrationId,
  endpoint = null,
  readOnly = false,
  onCancel,
  onSubmit,
  onValidationError,
}) {
  const [name, setName] = useState(
    endpoint?.name || '',
  )

  const [description, setDescription] = useState(
    endpoint?.description || '',
  )

  const [path, setPath] = useState(
    endpoint?.path || '',
  )

  const [method, setMethod] = useState(
    endpoint?.method || 'GET',
  )

  const [sqlText, setSqlText] = useState(
    endpoint?.sqlText || '',
  )

  const [active, setActive] = useState(
    endpoint?.active || 'S',
  )

  const [parameters, setParameters] = useState(
    endpoint?.parameters || [],
  )

  const isEditing = endpoint !== null

  function extractParameterNames(sql) {
    const matches = sql.matchAll(
      /:([a-zA-Z][a-zA-Z0-9_]*)/g,
    )

    return [
      ...new Set(
        Array.from(
          matches,
          (match) => match[1],
        ),
      ),
    ].filter(
      (parameterName) =>
        parameterName !== '__ih_max_results',
    )
  }

  function handleGenerateParameters() {
    if (readOnly) {
      return
    }

    const parameterNames =
      extractParameterNames(sqlText)

    const generatedParameters =
      parameterNames.map(
        (parameterName) => {
          const existingParameter =
            parameters.find(
              (parameter) =>
                parameter.name === parameterName,
            )

          if (existingParameter) {
            return existingParameter
          }

          return {
            name: parameterName,
            type: 'VARCHAR2',
            required: false,
          }
        },
      )

    setParameters(generatedParameters)
  }

  function handleParameterChange(
    index,
    field,
    value,
  ) {
    if (readOnly) {
      return
    }

    setParameters(
      parameters.map(
        (parameter, parameterIndex) => {
          if (parameterIndex !== index) {
            return parameter
          }

          return {
            ...parameter,
            [field]: value,
          }
        },
      ),
    )
  }

  function parametersAreSynchronized() {
    const sqlParameterNames =
      extractParameterNames(sqlText)

    const configuredParameterNames =
      parameters.map(
        (parameter) => parameter.name,
      )

    if (
      sqlParameterNames.length !==
      configuredParameterNames.length
    ) {
      return false
    }

    return sqlParameterNames.every(
      (parameterName) =>
        configuredParameterNames.includes(
          parameterName,
        ),
    )
  }

  function handleSubmit(event) {
    event.preventDefault()

    if (readOnly) {
      return
    }

    if (!parametersAreSynchronized()) {
      onValidationError(
        'O SQL foi alterado. Gere novamente os parâmetros antes de salvar.',
      )

      return
    }

    onSubmit({
      integrationId,
      name,
      description,
      path,
      method,
      sqlText,
      parameters,
      active,
    })
  }

  return (
    <form
      className="endpoint-form"
      onSubmit={handleSubmit}
    >
      <div className="endpoint-form__field">
        <label htmlFor="name">
          Nome
        </label>

        <input
          id="name"
          name="name"
          type="text"
          value={name}
          onChange={(event) =>
            setName(event.target.value)
          }
          placeholder="Ex: Buscar pedido"
          required
          readOnly={readOnly}
        />
      </div>

      <div className="endpoint-form__field">
        <label htmlFor="description">
          Descrição
        </label>

        <textarea
          id="description"
          name="description"
          rows="3"
          value={description}
          onChange={(event) =>
            setDescription(
              event.target.value,
            )
          }
          placeholder="Descreva o objetivo do endpoint"
          readOnly={readOnly}
        />
      </div>

      <div className="endpoint-form__row">
        <div className="endpoint-form__field">
          <label htmlFor="path">
            Path
          </label>

          <input
            id="path"
            name="path"
            type="text"
            value={path}
            onChange={(event) =>
              setPath(event.target.value)
            }
            placeholder="/buscar"
            required
            readOnly={readOnly}
          />
        </div>

        <div className="endpoint-form__field">
          <label htmlFor="method">
            Método
          </label>

          <select
            id="method"
            name="method"
            value={method}
            onChange={(event) =>
              setMethod(event.target.value)
            }
            disabled={readOnly}
          >
            <option value="GET">
              GET
            </option>
          </select>
        </div>

        <div className="endpoint-form__field">
          <label htmlFor="active">
            Status
          </label>

          <select
            id="active"
            name="active"
            value={active}
            onChange={(event) =>
              setActive(event.target.value)
            }
            disabled={readOnly}
          >
            <option value="S">
              Ativo
            </option>

            <option value="N">
              Inativo
            </option>
          </select>
        </div>
      </div>

      <div className="endpoint-form__field">
        <label htmlFor="sqlText">
          SQL
        </label>

        <textarea
          id="sqlText"
          name="sqlText"
          rows="8"
          value={sqlText}
          onChange={(event) =>
            setSqlText(event.target.value)
          }
          placeholder="select id, nome from cliente where id = :id"
          className="endpoint-form__sql"
          required
          readOnly={readOnly}
        />
      </div>

      <div className="endpoint-form__parameters">
        <div className="endpoint-form__parameters-header">
          <div>
            <h3>
              Parâmetros
            </h3>

            <p>
              {readOnly
                ? 'Parâmetros configurados para este endpoint.'
                : 'Gere automaticamente os parâmetros utilizados no SQL.'}
            </p>
          </div>

          {!readOnly && (
            <button
              type="button"
              className="endpoint-form__generate-parameters"
              onClick={handleGenerateParameters}
            >
              Gerar parâmetros
            </button>
          )}
        </div>

        {parameters.length === 0 && (
          <div className="endpoint-form__parameters-empty">
            Nenhum parâmetro encontrado no SQL.
          </div>
        )}

        {parameters.map((parameter, index) => (
          <div
            key={parameter.name}
            className="endpoint-form__parameter"
          >
            <div className="endpoint-form__field">
              <label
                htmlFor={`parameter-name-${index}`}
              >
                Nome
              </label>

              <input
                id={`parameter-name-${index}`}
                type="text"
                value={parameter.name}
                readOnly
              />
            </div>

            <div className="endpoint-form__field">
              <label
                htmlFor={`parameter-type-${index}`}
              >
                Tipo
              </label>

              <select
                id={`parameter-type-${index}`}
                value={parameter.type}
                onChange={(event) =>
                  handleParameterChange(
                    index,
                    'type',
                    event.target.value,
                  )
                }
                disabled={readOnly}
              >
                <option value="VARCHAR2">
                  VARCHAR2
                </option>

                <option value="NUMBER">
                  NUMBER
                </option>

                <option value="DATE">
                  DATE
                </option>

                <option value="TIMESTAMP">
                  TIMESTAMP
                </option>
              </select>
            </div>

            <label className="endpoint-form__required">
              <input
                type="checkbox"
                checked={parameter.required}
                onChange={(event) =>
                  handleParameterChange(
                    index,
                    'required',
                    event.target.checked,
                  )
                }
                disabled={readOnly}
              />

              Obrigatório
            </label>
          </div>
        ))}
      </div>

      <div className="endpoint-form__actions">
        <button
          type="button"
          className="endpoint-form__cancel"
          onClick={onCancel}
        >
          {readOnly ? 'Voltar' : 'Cancelar'}
        </button>

        {!readOnly && (
          <button
            type="submit"
            className="endpoint-form__submit"
          >
            {isEditing
              ? 'Salvar alterações'
              : 'Salvar endpoint'}
          </button>
        )}
      </div>
    </form>
  )
}

export default EndpointForm
