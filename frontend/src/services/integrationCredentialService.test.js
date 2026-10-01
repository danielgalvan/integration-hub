import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'
import { apiFetch } from '../utils/api'
import {
  createIntegrationCredential,
  getIntegrationCredentialApiKey,
  getIntegrationCredentials,
  regenerateIntegrationCredentialApiKey,
  setIntegrationCredentialActive,
  updateIntegrationCredential,
} from './integrationCredentialService'

vi.mock('../utils/api', () => ({
  apiFetch: vi.fn(),
}))

describe('integrationCredentialService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('carrega as credenciais da integração', async () => {
    const credentials = [
      {
        id: 1,
        integrationId: 10,
        name: 'Sistema Tasy',
        active: 'S',
      },
    ]

    apiFetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(
        credentials,
      ),
    })

    const result =
      await getIntegrationCredentials(10)

    expect(apiFetch).toHaveBeenCalledWith(
      '/api/integrations/10/credentials',
    )

    expect(result).toEqual(credentials)
  })

  it('cria uma credencial', async () => {
    const response = {
      apiKey: 'ihub_test',
    }

    apiFetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(
        response,
      ),
    })

    const result =
      await createIntegrationCredential(
        10,
        'Sistema Tasy',
      )

    expect(apiFetch).toHaveBeenCalledWith(
      '/api/integrations/10/credentials',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: 'Sistema Tasy',
        }),
      },
    )

    expect(result).toEqual(response)
  })

  it('atualiza o nome da credencial', async () => {
    const credential = {
      id: 1,
      integrationId: 10,
      name: 'Sistema Financeiro',
      active: 'S',
    }

    apiFetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(
        credential,
      ),
    })

    const result =
      await updateIntegrationCredential(
        10,
        1,
        'Sistema Financeiro',
      )

    expect(apiFetch).toHaveBeenCalledWith(
      '/api/integrations/10/credentials/1',
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: 'Sistema Financeiro',
        }),
      },
    )

    expect(result).toEqual(credential)
  })

  it('ativa uma credencial', async () => {
    const credential = {
      id: 1,
      integrationId: 10,
      name: 'Sistema Tasy',
      active: 'S',
    }

    apiFetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(
        credential,
      ),
    })

    const result =
      await setIntegrationCredentialActive(
        10,
        1,
        true,
      )

    expect(apiFetch).toHaveBeenCalledWith(
      '/api/integrations/10/credentials/1/active',
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          active: true,
        }),
      },
    )

    expect(result).toEqual(credential)
  })

  it('desativa uma credencial', async () => {
    const credential = {
      id: 1,
      integrationId: 10,
      name: 'Sistema Tasy',
      active: 'N',
    }

    apiFetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(
        credential,
      ),
    })

    const result =
      await setIntegrationCredentialActive(
        10,
        1,
        false,
      )

    expect(apiFetch).toHaveBeenCalledWith(
      '/api/integrations/10/credentials/1/active',
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          active: false,
        }),
      },
    )

    expect(result).toEqual(credential)
  })

  it('consulta a API Key da credencial', async () => {
    const response = {
      apiKey: 'ihub_test',
    }

    apiFetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(
        response,
      ),
    })

    const result =
      await getIntegrationCredentialApiKey(
        10,
        1,
      )

    expect(apiFetch).toHaveBeenCalledWith(
      '/api/integrations/10/credentials/1/api-key',
    )

    expect(result).toEqual(response)
  })

  it('regenera a API Key da credencial', async () => {
    const response = {
      apiKey: 'ihub_new_test',
    }

    apiFetch.mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue(
        response,
      ),
    })

    const result =
      await regenerateIntegrationCredentialApiKey(
        10,
        1,
      )

    expect(apiFetch).toHaveBeenCalledWith(
      '/api/integrations/10/credentials/1/api-key',
      {
        method: 'POST',
      },
    )

    expect(result).toEqual(response)
  })

  it('usa a mensagem retornada pelo backend em caso de erro', async () => {
    apiFetch.mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue({
        message:
          'Credencial não encontrada',
      }),
    })

    await expect(
      getIntegrationCredentials(10),
    ).rejects.toThrow(
      'Credencial não encontrada',
    )
  })

  it('usa a mensagem padrão quando o backend não retorna json', async () => {
    apiFetch.mockResolvedValue({
      ok: false,
      json: vi
        .fn()
        .mockRejectedValue(
          new Error('Invalid JSON'),
        ),
    })

    await expect(
      getIntegrationCredentials(10),
    ).rejects.toThrow(
      'Não foi possível carregar as credenciais.',
    )
  })

  it('usa mensagem específica quando não consegue ativar a credencial', async () => {
    apiFetch.mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue(null),
    })

    await expect(
      setIntegrationCredentialActive(
        10,
        1,
        true,
      ),
    ).rejects.toThrow(
      'Não foi possível ativar a credencial.',
    )
  })

  it('usa mensagem específica quando não consegue desativar a credencial', async () => {
    apiFetch.mockResolvedValue({
      ok: false,
      json: vi.fn().mockResolvedValue(null),
    })

    await expect(
      setIntegrationCredentialActive(
        10,
        1,
        false,
      ),
    ).rejects.toThrow(
      'Não foi possível desativar a credencial.',
    )
  })
})
