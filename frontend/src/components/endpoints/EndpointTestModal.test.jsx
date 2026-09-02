import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import EndpointTestModal from './EndpointTestModal'

const endpointService = vi.hoisted(() => ({ executeEndpoint: vi.fn() }))
vi.mock('../../services/endpointService', () => endpointService)

const integration = { id: 1, basePath: '/api/clientes' }
const endpoint = {
  id: 2,
  name: 'Buscar cliente',
  method: 'GET',
  path: '/buscar',
  parameters: [{ name: 'codigo', type: 'NUMBER', required: true }],
}

describe('EndpointTestModal', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('executa o endpoint com os parâmetros informados e mostra o resultado', async () => {
    endpointService.executeEndpoint.mockResolvedValue({
      success: true,
      status: 200,
      duration: 12,
      url: '/api/clientes/buscar?codigo=10',
      data: [{ CODIGO: 10 }],
    })

    render(<EndpointTestModal open integration={integration} endpoint={endpoint} onClose={vi.fn()} />)
    fireEvent.change(screen.getByRole('textbox', { name: /codigo/i }), {
      target: { value: '10' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Executar' }))

    await waitFor(() => {
      expect(endpointService.executeEndpoint).toHaveBeenCalledWith(
        integration,
        endpoint,
        { codigo: '10' },
      )
    })
    expect(await screen.findByText('HTTP 200')).toBeInTheDocument()
    expect(screen.getByText('12 ms')).toBeInTheDocument()
    expect(screen.getByText(/"CODIGO": 10/)).toBeInTheDocument()
  })

  it('mostra o erro retornado pela execução', async () => {
    endpointService.executeEndpoint.mockRejectedValue(new Error('Parâmetro inválido'))

    render(<EndpointTestModal open integration={integration} endpoint={endpoint} onClose={vi.fn()} />)
    fireEvent.change(screen.getByRole('textbox', { name: /codigo/i }), {
      target: { value: 'abc' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Executar' }))

    expect(await screen.findByText('Parâmetro inválido')).toBeInTheDocument()
  })

  it('inicia limpo ao fechar e abrir novamente', () => {
    const { unmount } = render(
      <EndpointTestModal
        open
        integration={integration}
        endpoint={endpoint}
        onClose={vi.fn()}
      />,
    )
    fireEvent.change(screen.getByRole('textbox', { name: /codigo/i }), {
      target: { value: '10' },
    })

    unmount()

    render(
      <EndpointTestModal
        open
        integration={integration}
        endpoint={endpoint}
        onClose={vi.fn()}
      />,
    )

    expect(screen.getByRole('textbox', { name: /codigo/i })).toHaveValue('')
  })

  it('configura campos de data e hora com as máscaras esperadas', () => {
    const endpointWithDates = {
      ...endpoint,
      parameters: [
        { name: 'data', type: 'DATE', required: true },
        { name: 'momento', type: 'TIMESTAMP', required: false },
      ],
    }

    render(
      <EndpointTestModal
        open
        integration={integration}
        endpoint={endpointWithDates}
        onClose={vi.fn()}
      />,
    )

    expect(screen.getByRole('textbox', { name: /data/i }))
      .toHaveAttribute('placeholder', 'aaaa-mm-dd')
    expect(screen.getByRole('textbox', { name: /momento/i }))
      .toHaveAttribute('placeholder', 'aaaa-mm-ddThh:mm:ss')
  })

  it('copia a URL do resultado', async () => {
    const writeText = vi.fn().mockResolvedValue()
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: { writeText },
    })
    endpointService.executeEndpoint.mockResolvedValue({
      success: true,
      status: 200,
      duration: 1,
      url: '/api/clientes/buscar?codigo=10',
      data: [],
    })

    render(<EndpointTestModal open integration={integration} endpoint={endpoint} onClose={vi.fn()} />)
    fireEvent.change(screen.getByRole('textbox', { name: /codigo/i }), {
      target: { value: '10' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Executar' }))
    await screen.findByText('HTTP 200')
    fireEvent.click(screen.getByRole('button', { name: 'Copiar URL' }))

    await waitFor(() => {
      expect(writeText).toHaveBeenCalledWith(
        '/api/clientes/buscar?codigo=10',
      )
    })
    expect(screen.getByRole('button', { name: 'Copiada!' })).toBeInTheDocument()
  })
})

