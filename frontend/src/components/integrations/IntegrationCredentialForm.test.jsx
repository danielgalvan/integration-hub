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
import IntegrationCredentialForm from './IntegrationCredentialForm'

function renderForm({
  credential = null,
  loading = false,
  onSave = vi.fn(),
  onCancel = vi.fn(),
} = {}) {
  render(
    <IntegrationCredentialForm
      credential={credential}
      loading={loading}
      onSave={onSave}
      onCancel={onCancel}
    />,
  )

  return {
    onSave,
    onCancel,
  }
}

describe('IntegrationCredentialForm', () => {
  it('exibe formulário para nova credencial', () => {
    renderForm()

    expect(
      screen.getByText('Nova credencial'),
    ).toBeInTheDocument()

    expect(
      screen.getByLabelText('Nome'),
    ).toHaveValue('')

    expect(
      screen.getByRole('button', {
        name: 'Salvar',
      }),
    ).toBeInTheDocument()
  })

  it('exibe formulário preenchido na edição', () => {
    renderForm({
      credential: {
        id: 1,
        integrationId: 10,
        name: 'Sistema Tasy',
        active: 'S',
      },
    })

    expect(
      screen.getByText('Editar credencial'),
    ).toBeInTheDocument()

    expect(
      screen.getByLabelText('Nome'),
    ).toHaveValue('Sistema Tasy')
  })

  it('salva uma nova credencial', () => {
    const { onSave } = renderForm()

    fireEvent.change(
      screen.getByLabelText('Nome'),
      {
        target: {
          value: 'Sistema Financeiro',
        },
      },
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Salvar',
      }),
    )

    expect(onSave).toHaveBeenCalledTimes(1)

    expect(onSave).toHaveBeenCalledWith({
      name: 'Sistema Financeiro',
    })
  })

  it('remove espaços do início e fim antes de salvar', () => {
    const { onSave } = renderForm()

    fireEvent.change(
      screen.getByLabelText('Nome'),
      {
        target: {
          value: '  Sistema Tasy  ',
        },
      },
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Salvar',
      }),
    )

    expect(onSave).toHaveBeenCalledWith({
      name: 'Sistema Tasy',
    })
  })

  it('não salva quando o nome está vazio', () => {
    const { onSave } = renderForm()

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Salvar',
      }),
    )

    expect(
      screen.getByRole('alert'),
    ).toHaveTextContent(
      'Informe o nome da credencial.',
    )

    expect(onSave).not.toHaveBeenCalled()
  })

  it('não salva quando o nome contém apenas espaços', () => {
    const { onSave } = renderForm()

    fireEvent.change(
      screen.getByLabelText('Nome'),
      {
        target: {
          value: '   ',
        },
      },
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Salvar',
      }),
    )

    expect(
      screen.getByRole('alert'),
    ).toHaveTextContent(
      'Informe o nome da credencial.',
    )

    expect(onSave).not.toHaveBeenCalled()
  })

  it('remove a mensagem de erro ao alterar o nome', () => {
    renderForm()

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Salvar',
      }),
    )

    expect(
      screen.getByRole('alert'),
    ).toBeInTheDocument()

    fireEvent.change(
      screen.getByLabelText('Nome'),
      {
        target: {
          value: 'Sistema Tasy',
        },
      },
    )

    expect(
      screen.queryByRole('alert'),
    ).not.toBeInTheDocument()
  })

  it('chama cancelamento', () => {
    const { onCancel } = renderForm()

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Cancelar',
      }),
    )

    expect(onCancel).toHaveBeenCalledTimes(1)
  })

  it('desabilita os campos e botões durante o salvamento', () => {
    renderForm({
      loading: true,
    })

    expect(
      screen.getByLabelText('Nome'),
    ).toBeDisabled()

    expect(
      screen.getByRole('button', {
        name: 'Cancelar',
      }),
    ).toBeDisabled()

    expect(
      screen.getByRole('button', {
        name: 'Salvando...',
      }),
    ).toBeDisabled()
  })

  it('salva a alteração de uma credencial existente', () => {
    const { onSave } = renderForm({
      credential: {
        id: 1,
        integrationId: 10,
        name: 'Sistema Tasy',
        active: 'S',
      },
    })

    fireEvent.change(
      screen.getByLabelText('Nome'),
      {
        target: {
          value: 'Tasy Produção',
        },
      },
    )

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Salvar',
      }),
    )

    expect(onSave).toHaveBeenCalledWith({
      name: 'Tasy Produção',
    })
  })

  it('limita o nome a 100 caracteres', () => {
    renderForm()

    expect(
      screen.getByLabelText('Nome'),
    ).toHaveAttribute(
      'maxlength',
      '100',
    )
  })
})
