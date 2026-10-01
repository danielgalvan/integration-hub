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
import IntegrationCredentialList from './IntegrationCredentialList'

function renderList({
  credentials = [],
  onViewApiKey = vi.fn(),
  onEdit = vi.fn(),
  onToggleActive = vi.fn(),
  onRegenerateApiKey = vi.fn(),
} = {}) {
  render(
    <IntegrationCredentialList
      credentials={credentials}
      onViewApiKey={onViewApiKey}
      onEdit={onEdit}
      onToggleActive={onToggleActive}
      onRegenerateApiKey={
        onRegenerateApiKey
      }
    />,
  )

  return {
    onViewApiKey,
    onEdit,
    onToggleActive,
    onRegenerateApiKey,
  }
}

const activeCredential = {
  id: 1,
  integrationId: 10,
  name: 'Sistema Tasy',
  active: 'S',
  lastUsedAt: null,
}

const inactiveCredential = {
  id: 2,
  integrationId: 10,
  name: 'Sistema Financeiro',
  active: 'N',
  lastUsedAt: null,
}

describe('IntegrationCredentialList', () => {
  it('mostra mensagem quando não existem credenciais', () => {
    renderList()

    expect(
      screen.getByText(
        'Nenhuma credencial cadastrada',
      ),
    ).toBeInTheDocument()

    expect(
      screen.getByText(
        /Crie uma credencial para permitir o consumo/,
      ),
    ).toBeInTheDocument()
  })

  it('exibe as credenciais cadastradas', () => {
    renderList({
      credentials: [
        activeCredential,
        inactiveCredential,
      ],
    })

    expect(
      screen.getByText('Sistema Tasy'),
    ).toBeInTheDocument()

    expect(
      screen.getByText('Sistema Financeiro'),
    ).toBeInTheDocument()
  })

  it('exibe o status das credenciais', () => {
    renderList({
      credentials: [
        activeCredential,
        inactiveCredential,
      ],
    })

    expect(
      screen.getByText('Ativa'),
    ).toBeInTheDocument()

    expect(
      screen.getByText('Inativa'),
    ).toBeInTheDocument()
  })

  it('informa quando a credencial nunca foi utilizada', () => {
    renderList({
      credentials: [activeCredential],
    })

    expect(
      screen.getByText(
        'Nunca utilizada',
      ),
    ).toBeInTheDocument()
  })

  it('formata a data do último uso', () => {
    renderList({
      credentials: [
        {
          ...activeCredential,
          lastUsedAt:
            '2026-10-01T14:32:18',
        },
      ],
    })

    expect(
      screen.queryByText(
        'Nunca utilizada',
      ),
    ).not.toBeInTheDocument()

    expect(
      screen.getByText(
        /01\/10\/2026/,
      ),
    ).toBeInTheDocument()
  })

  it('mantém o valor original quando a data do último uso é inválida', () => {
    renderList({
      credentials: [
        {
          ...activeCredential,
          lastUsedAt: 'data-invalida',
        },
      ],
    })

    expect(
      screen.getByText(
        'data-invalida',
      ),
    ).toBeInTheDocument()
  })

  it('chama visualização da API Key com a credencial selecionada', () => {
    const { onViewApiKey } =
      renderList({
        credentials: [activeCredential],
      })

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Visualizar API Key',
      }),
    )

    expect(
      onViewApiKey,
    ).toHaveBeenCalledWith(
      activeCredential,
    )
  })

  it('chama edição com a credencial selecionada', () => {
    const { onEdit } = renderList({
      credentials: [activeCredential],
    })

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Editar',
      }),
    )

    expect(
      onEdit,
    ).toHaveBeenCalledWith(
      activeCredential,
    )
  })

  it('permite desativar uma credencial ativa', () => {
    const { onToggleActive } =
      renderList({
        credentials: [activeCredential],
      })

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Desativar',
      }),
    )

    expect(
      onToggleActive,
    ).toHaveBeenCalledWith(
      activeCredential,
    )
  })

  it('permite ativar uma credencial inativa', () => {
    const { onToggleActive } =
      renderList({
        credentials: [
          inactiveCredential,
        ],
      })

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Ativar',
      }),
    )

    expect(
      onToggleActive,
    ).toHaveBeenCalledWith(
      inactiveCredential,
    )
  })

  it('chama regeneração da API Key com a credencial selecionada', () => {
    const { onRegenerateApiKey } =
      renderList({
        credentials: [activeCredential],
      })

    fireEvent.click(
      screen.getByRole('button', {
        name: 'Regenerar API Key',
      }),
    )

    expect(
      onRegenerateApiKey,
    ).toHaveBeenCalledWith(
      activeCredential,
    )
  })
})
