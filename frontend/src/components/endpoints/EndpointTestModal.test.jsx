import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
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

  it('executa o endpoint com os parâmetros informados e mostra o resultado', async () => {
    endpointService.executeEndpoint.mockResolvedValue({
      success: true,
      status: 200,
      duration: 12,
      url: 'http://localhost:8081/api/clientes/buscar?codigo=10',
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
})
