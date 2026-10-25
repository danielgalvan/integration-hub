
import {
  fireEvent,
  render,
  screen,
  waitFor,
} from '@testing-library/react'
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'

import IntegrationCredentialsDialog from './IntegrationCredentialsDialog'

import {
  getIntegrationCredentials,
  createIntegrationCredential,
  updateIntegrationCredential,
  setIntegrationCredentialActive,
  getIntegrationCredentialApiKey,
  regenerateIntegrationCredentialApiKey,
} from '../../services/integrationCredentialService'

vi.mock('../../services/integrationCredentialService', () => ({
  getIntegrationCredentials: vi.fn(),
  createIntegrationCredential: vi.fn(),
  updateIntegrationCredential: vi.fn(),
  setIntegrationCredentialActive: vi.fn(),
  getIntegrationCredentialApiKey: vi.fn(),
  regenerateIntegrationCredentialApiKey: vi.fn(),
}))

const integration = {
  id: 10,
  name: 'Integração Tasy',
  authType: 'API_KEY',
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

function renderDialog(props = {}) {
  const onClose = vi.fn()

  render(
    <IntegrationCredentialsDialog
      integration={integration}
      onClose={onClose}
      {...props}
    />,
  )

  return { onClose }
}

async function waitForList() {
  return screen.findByText('Sistema Tasy')
}

function clickButton(name) {
  fireEvent.click(
    screen.getByRole('button', { name }),
  )
}

function clickCredentialButton(name, index = 0) {
  fireEvent.click(
    screen.getAllByRole('button', { name })[index],
  )
}

function changeCredentialName(name) {
  fireEvent.change(
    screen.getByLabelText('Nome'),
    {
      target: { value: name },
    },
  )
}

describe('IntegrationCredentialsDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    getIntegrationCredentials.mockResolvedValue([
      activeCredential,
      inactiveCredential,
    ])

    createIntegrationCredential.mockResolvedValue({
      apiKey: 'ihub_new_key',
    })

    updateIntegrationCredential.mockResolvedValue({
      ...activeCredential,
      name: 'Tasy Produção',
    })

    setIntegrationCredentialActive.mockResolvedValue({})

    getIntegrationCredentialApiKey.mockResolvedValue({
      apiKey: 'ihub_existing_key',
    })

    regenerateIntegrationCredentialApiKey.mockResolvedValue({
      apiKey: 'ihub_regenerated_key',
    })

    vi.stubGlobal('confirm', vi.fn(() => true))
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('exibe o título e o nome da integração', async () => {
    renderDialog()

    expect(
      screen.getByRole('dialog', {
        name: 'Credenciais da integração Integração Tasy',
      }),
    ).toBeInTheDocument()

    expect(
      screen.getByText('Integração Tasy'),
    ).toBeInTheDocument()

    await waitForList()
  })

  it('carrega as credenciais da integração', async () => {
    renderDialog()

    await waitForList()

    expect(
      getIntegrationCredentials,
    ).toHaveBeenCalledWith(10)

    expect(
      screen.getByText('Sistema Financeiro'),
    ).toBeInTheDocument()
  })

  it('exibe mensagem quando não existem credenciais', async () => {
    getIntegrationCredentials.mockResolvedValue([])

    renderDialog()

    expect(
      await screen.findByText(
        'Nenhuma credencial cadastrada',
      ),
    ).toBeInTheDocument()
  })

  it('exibe erro quando falha o carregamento', async () => {
    getIntegrationCredentials.mockRejectedValue(
      new Error('Erro ao carregar credenciais'),
    )

    renderDialog()

    expect(
      await screen.findByRole('alert'),
    ).toHaveTextContent(
      'Erro ao carregar credenciais',
    )
  })

  it('abre e cancela o formulário de criação', async () => {
    renderDialog()
    await waitForList()

    clickButton('Nova credencial')

    expect(
      screen.getByRole('heading', {
        name: 'Nova credencial',
      }),
    ).toBeInTheDocument()

    clickButton('Cancelar')

    expect(
      screen.getByText('Sistema Tasy'),
    ).toBeInTheDocument()

    expect(
      createIntegrationCredential,
    ).not.toHaveBeenCalled()
  })

  it('cria uma credencial e exibe a API Key', async () => {
    renderDialog()
    await waitForList()

    clickButton('Nova credencial')

    changeCredentialName('  Novo Sistema  ')

    clickButton('Salvar')

    await waitFor(() => {
      expect(
        createIntegrationCredential,
      ).toHaveBeenCalledWith(
        10,
        'Novo Sistema',
      )
    })

    expect(
      await screen.findByRole('status'),
    ).toHaveTextContent(
      'Credencial criada com sucesso.',
    )

    const apiKeyInput = await screen.findByLabelText(
      'API Key',
    )

    expect(apiKeyInput).toHaveValue('ihub_new_key')
    expect(apiKeyInput).toHaveAttribute(
      'type',
      'password',
    )

    expect(
      getIntegrationCredentials,
    ).toHaveBeenCalledTimes(2)
  })

  it('edita o nome de uma credencial', async () => {
    renderDialog()
    await waitForList()

    clickCredentialButton('Editar')

    expect(
      screen.getByLabelText('Nome'),
    ).toHaveValue('Sistema Tasy')

    changeCredentialName('Tasy Produção')

    clickButton('Salvar')

    await waitFor(() => {
      expect(
        updateIntegrationCredential,
      ).toHaveBeenCalledWith(
        10,
        1,
        'Tasy Produção',
      )
    })

    expect(
      await screen.findByRole('status'),
    ).toHaveTextContent(
      'Credencial atualizada com sucesso.',
    )
  })

  it('ativa uma credencial inativa', async () => {
    renderDialog()
    await waitForList()

    clickButton('Ativar')

    await waitFor(() => {
      expect(
        setIntegrationCredentialActive,
      ).toHaveBeenCalledWith(
        10,
        2,
        true,
      )
    })

    expect(window.confirm).toHaveBeenCalled()

    expect(
      await screen.findByRole('status'),
    ).toHaveTextContent(
      'Credencial ativada com sucesso.',
    )
  })

  it('desativa uma credencial ativa', async () => {
    renderDialog()
    await waitForList()

    clickButton('Desativar')

    await waitFor(() => {
      expect(
        setIntegrationCredentialActive,
      ).toHaveBeenCalledWith(
        10,
        1,
        false,
      )
    })

    expect(
      await screen.findByRole('status'),
    ).toHaveTextContent(
      'Credencial desativada com sucesso.',
    )
  })

  it('não altera o status quando a confirmação é cancelada', async () => {
    window.confirm.mockReturnValue(false)

    renderDialog()
    await waitForList()

    clickButton('Desativar')

    expect(
      setIntegrationCredentialActive,
    ).not.toHaveBeenCalled()

    expect(
      getIntegrationCredentials,
    ).toHaveBeenCalledTimes(1)
  })

  it('consulta a API Key e permite mostrar ou ocultar', async () => {
    renderDialog()
    await waitForList()

    clickCredentialButton('Visualizar API Key')

    await waitFor(() => {
      expect(
        getIntegrationCredentialApiKey,
      ).toHaveBeenCalledWith(10, 1)
    })

    const input = await screen.findByLabelText(
      'API Key',
    )

    expect(input).toHaveAttribute('type', 'password')
    expect(input).toHaveValue('ihub_existing_key')

    clickButton('Mostrar')

    expect(input).toHaveAttribute('type', 'text')

    clickButton('Ocultar')

    expect(input).toHaveAttribute('type', 'password')

    clickButton('Fechar chave')

    expect(
      screen.queryByLabelText('API Key'),
    ).not.toBeInTheDocument()
  })

  it('copia a API Key para a área de transferência', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined)

    const navigatorMock = Object.create(navigator)

    Object.defineProperty(navigatorMock, 'clipboard', {
      configurable: true,
      value: { writeText },
    })

    vi.stubGlobal('navigator', navigatorMock)

    renderDialog()
    await waitForList()

    clickCredentialButton('Visualizar API Key')

    await screen.findByLabelText('API Key')

    clickButton('Copiar')

    await waitFor(() => {
      expect(writeText).toHaveBeenCalledWith(
        'ihub_existing_key',
      )
    })

    expect(
      await screen.findByRole('status'),
    ).toHaveTextContent('API Key copiada.')
  })

  it('regenera a API Key', async () => {
    renderDialog()
    await waitForList()

    clickCredentialButton('Regenerar API Key')

    await waitFor(() => {
      expect(
        regenerateIntegrationCredentialApiKey,
      ).toHaveBeenCalledWith(10, 1)
    })

    expect(
      await screen.findByRole('status'),
    ).toHaveTextContent(
      'API Key regenerada com sucesso.',
    )

    expect(
      screen.getByLabelText('API Key'),
    ).toHaveValue('ihub_regenerated_key')
  })

  it('não regenera quando a confirmação é cancelada', async () => {
    window.confirm.mockReturnValue(false)

    renderDialog()
    await waitForList()

    clickCredentialButton('Regenerar API Key')

    expect(
      regenerateIntegrationCredentialApiKey,
    ).not.toHaveBeenCalled()
  })

  it('exibe erro quando a criação falha', async () => {
    createIntegrationCredential.mockRejectedValue(
      new Error(
        'Já existe uma credencial com esse nome',
      ),
    )

    renderDialog()
    await waitForList()

    clickButton('Nova credencial')

    changeCredentialName('Sistema Tasy')

    clickButton('Salvar')

    expect(
      await screen.findByRole('alert'),
    ).toHaveTextContent(
      'Já existe uma credencial com esse nome',
    )

    expect(
      screen.getByRole('heading', {
        name: 'Nova credencial',
      }),
    ).toBeInTheDocument()
  })

  it('fecha o dialog ao clicar no botão do rodapé', async () => {
    const { onClose } = renderDialog()

    await waitForList()

    const closeButtons = screen.getAllByRole('button', {
      name: 'Fechar',
    })

    expect(closeButtons).toHaveLength(2)

    fireEvent.click(closeButtons[1])

    expect(onClose).toHaveBeenCalledTimes(1)
  })
})
