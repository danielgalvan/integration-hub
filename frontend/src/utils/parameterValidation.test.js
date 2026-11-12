import { describe, expect, it } from 'vitest'
import { validateParameterValue } from './parameterValidation'

describe('validateParameterValue', () => {
  it('aceita números decimais e notação exponencial', () => {
    expect(validateParameterValue({ type: 'NUMBER' }, '149.90')).toBe('')
    expect(validateParameterValue({ type: 'NUMBER' }, '-1.2e3')).toBe('')
  })

  it('rejeita números com vírgula decimal', () => {
    expect(validateParameterValue({ type: 'NUMBER' }, '149,90'))
      .toContain('número válido')
  })

  it('aceita datas reais no formato yyyy-MM-dd e rejeita datas impossíveis', () => {
    expect(validateParameterValue({ type: 'DATE' }, '2024-02-29')).toBe('')
    expect(validateParameterValue({ type: 'DATE' }, '2026-02-29'))
      .toContain('data válida')
  })

  it('valida formato e limites de horário do TIMESTAMP', () => {
    expect(validateParameterValue({ type: 'TIMESTAMP' }, '2026-08-20T23:59:59'))
      .toBe('')
    expect(validateParameterValue({ type: 'TIMESTAMP' }, '2026-08-20T24:00:00'))
      .toContain('data e hora válidas')
  })

  it('rejeita parâmetro obrigatório vazio e permite parâmetro opcional vazio', () => {
    expect(validateParameterValue({ type: 'NUMBER', required: true }, ''))
      .toBe('Este parâmetro é obrigatório.')
    expect(validateParameterValue({ type: 'NUMBER', required: false }, ''))
      .toBe('')
  })
})
