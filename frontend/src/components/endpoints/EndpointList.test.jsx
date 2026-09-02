import {
  fireEvent,
  render,
  screen,
} from '@testing-library/react'
import {
  describe,
  expect,
  it,
  vi,
} from 'vitest'
import EndpointList from './EndpointList'

const endpoint = {
  id: 1,
  name: 'Buscar cliente',
  method: 'GET',
  path: '/buscar',
  active: 'S',
}

describe('EndpointList', () => {
  it('exibe o estado vazio', () => {
    render(<EndpointList />)

    expect(
      screen.getByText(
        'Nenhum endpoint cadastrado',
      ),
    ).toBeInTheDocument()
  })

  it('dispara as ações do endpoint ativo quando pode editar', () => {
    const onTest = vi.fn()
    const onEdit = vi.fn()
    const onDelete = vi.fn()

    render(
      <EndpointList
        endpoints={[endpoint]}
        canEdit
        onTest={onTest}
        onEdit={onEdit}
        onDelete={onDelete}
      />,
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Testar',
      }),
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Editar',
      }),
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Excluir',
      }),
    )

    expect(onTest)
      .toHaveBeenCalledWith(endpoint)

    expect(onEdit)
      .toHaveBeenCalledWith(endpoint)

    expect(onDelete)
      .toHaveBeenCalledWith(endpoint)
  })

  it('exibe visualizar e não exibe excluir quando não pode editar', () => {
    const onTest = vi.fn()
    const onEdit = vi.fn()
    const onDelete = vi.fn()

    render(
      <EndpointList
        endpoints={[endpoint]}
        canEdit={false}
        onTest={onTest}
        onEdit={onEdit}
        onDelete={onDelete}
      />,
    )

    expect(
      screen.getByRole('button', {
        name: 'Testar',
      }),
    ).toBeInTheDocument()

    expect(
      screen.getByRole('button', {
        name: 'Visualizar',
      }),
    ).toBeInTheDocument()

    expect(
      screen.queryByRole('button', {
        name: 'Excluir',
      }),
    ).not.toBeInTheDocument()

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Visualizar',
      }),
    )

    expect(onEdit)
      .toHaveBeenCalledWith(endpoint)

    expect(onDelete)
      .not.toHaveBeenCalled()
  })

  it('não permite testar um endpoint inativo', () => {
    render(
      <EndpointList
        endpoints={[
          {
            ...endpoint,
            active: 'N',
          },
        ]}
        canEdit
        onTest={vi.fn()}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />,
    )

    expect(
      screen.getByRole('button', {
        name: 'Testar',
      }),
    ).toBeDisabled()
  })
})

