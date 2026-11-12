const BIG_DECIMAL = /^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/
const DATE = /^(\d{4})-(\d{2})-(\d{2})$/
const TIMESTAMP = /^(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2}):(\d{2})$/

export function validateParameterValue(parameter, value) {
  if (!value) {
    return parameter.required
      ? 'Este parâmetro é obrigatório.'
      : ''
  }

  switch (parameter.type?.toUpperCase()) {
    case 'NUMBER':
      return BIG_DECIMAL.test(value)
        ? ''
        : 'Informe um número válido, usando ponto como separador decimal.'

    case 'DATE':
      return isValidDate(value)
        ? ''
        : 'Use uma data válida no formato yyyy-MM-dd.'

    case 'TIMESTAMP':
      return isValidTimestamp(value)
        ? ''
        : "Use uma data e hora válidas no formato yyyy-MM-dd'T'HH:mm:ss."

    default:
      return ''
  }
}

function isValidTimestamp(value) {
  const match = TIMESTAMP.exec(value)

  if (!match || !isValidDate(match[1])) {
    return false
  }

  const [, , hours, minutes, seconds] = match

  return Number(hours) <= 23
    && Number(minutes) <= 59
    && Number(seconds) <= 59
}

function isValidDate(value) {
  const match = DATE.exec(value)

  if (!match) {
    return false
  }

  const [, yearText, monthText, dayText] = match
  const year = Number(yearText)
  const month = Number(monthText)
  const day = Number(dayText)

  if (month < 1 || month > 12 || day < 1) {
    return false
  }

  const isLeapYear = year % 4 === 0
    && (year % 100 !== 0 || year % 400 === 0)
  const daysByMonth = [
    31,
    isLeapYear ? 29 : 28,
    31,
    30,
    31,
    30,
    31,
    31,
    30,
    31,
    30,
    31,
  ]

  return day <= daysByMonth[month - 1]
}
