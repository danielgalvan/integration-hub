# Integration Hub

O **Integration Hub** é uma plataforma para criar, gerenciar e
disponibilizar APIs de integração sobre bases Oracle sem a necessidade
de implementar um controller Java para cada consulta.

A **V1 está funcional e publicada**. A **V2 está em desenvolvimento**,
com foco em múltiplas credenciais por integração, rastreabilidade das
execuções e melhoria do bootstrap de novas instalações.

> **Ambiente online:** https://integrationhub.duckdns.org

------------------------------------------------------------------------

## Arquitetura

### Produção

``` text
Internet
   │
   │ HTTPS
   ▼
Nginx
   ├── /         → React
   └── /api/**   → Spring Boot :8081
                      │
                      │ JDBC / Wallet
                      ▼
              Oracle Autonomous Database
```

O ambiente publicado utiliza:

-   Oracle Cloud Infrastructure (OCI);
-   Oracle Linux 9;
-   Nginx como servidor web e reverse proxy;
-   Spring Boot executado como serviço `systemd`;
-   Oracle Autonomous Database;
-   DuckDNS para hostname;
-   Let's Encrypt + Certbot para HTTPS e renovação automática.

### Desenvolvimento local

``` text
React + Vite :5175
       │
       │ proxy /api
       ▼
Spring Boot :8081
       │
       ▼
Oracle
```

O frontend utiliza URLs relativas (`/api/...`). Em desenvolvimento, o
Vite encaminha `/api` para `http://localhost:8081`; em produção, o Nginx
faz o encaminhamento para o backend.

------------------------------------------------------------------------

## Stack

### Backend

-   Java 21
-   Spring Boot 4.0.7
-   Spring Web MVC
-   Spring Security
-   Spring JDBC
-   HikariCP
-   Oracle JDBC
-   Oracle Wallet / Autonomous Database
-   JJWT
-   BCrypt
-   AES-256-GCM para proteção reversível das credenciais de integração
    da V2
-   OpenAPI 3.1 / Swagger UI
-   Maven
-   JUnit / Mockito

### Frontend

-   React 19
-   Vite 8
-   JavaScript
-   Vitest
-   Testing Library
-   ESLint
-   npm

------------------------------------------------------------------------

## Funcionalidades

### Integrações

Uma `Integration` agrupa endpoints sob um `basePath`.

``` text
Integration
    │
    │ 1:N
    ▼
Endpoint
```

Exemplo:

``` text
name:      Pedidos
basePath:  /api/pedidos
active:    S
authType:  API_KEY
```

Regras principais do `basePath`:

-   obrigatório;
-   deve iniciar com `/api/`;
-   não deve terminar com `/`;
-   não pode conter espaços;
-   a resolução considera o `basePath` ativo mais específico.

### Endpoints dinâmicos

Cada endpoint define:

``` text
integrationId
name
description
path
method
sqlText
parameters
active
```

Atualmente, somente `GET` é disponibilizado dinamicamente.

Exemplo:

``` text
basePath: /api/pedidos
path:     /listar
```

Resultado:

``` http
GET /api/pedidos/listar?status=ABERTO
```

O backend resolve a integração e o endpoint em tempo de execução e
executa o SQL configurado no Oracle.

------------------------------------------------------------------------

## SQL dinâmico e parâmetros

As consultas devem utilizar **bind parameters**:

``` sql
select id,
       numero,
       status,
       valor_total
from pedido
where status = :status
```

Tipos suportados:

  Tipo          Formato
  ------------- --------------
  `VARCHAR2`    texto
  `NUMBER`      número
  `DATE`        `yyyy-MM-dd`
  `TIMESTAMP`   data/hora

Exemplo:

``` json
{
  "name": "pedido_id",
  "type": "NUMBER",
  "required": true
}
```

Os parâmetros são identificados a partir das bind variables do SQL e
podem ser gerados automaticamente pelo frontend.

### Restrições de segurança do SQL

O Integration Hub:

-   aceita somente uma instrução iniciada por `SELECT`;
-   utiliza bind parameters;
-   rejeita ponto e vírgula;
-   rejeita comentários SQL;
-   rejeita `SELECT ... FOR UPDATE`;
-   valida e converte parâmetros antes da execução;
-   limita a quantidade máxima de resultados.

O limite padrão é:

``` text
integration-hub.dynamic.max-results = 1000
```

------------------------------------------------------------------------

## Autenticação e autorização administrativa

As APIs administrativas utilizam usuários persistidos em `IH_USERS`,
senhas BCrypt e JWT stateless.

``` http
POST /api/auth/login
```

Exemplo:

``` json
{
  "username": "admin",
  "password": "senha",
  "environment": "development"
}
```

Chamadas protegidas utilizam:

``` http
Authorization: Bearer <token>
```

`401 Unauthorized` representa ausência ou invalidez da autenticação.

`403 Forbidden` representa usuário autenticado sem permissão para a
operação.

### Perfis

  Recurso                                Administrador   Criador   Consumidor
  ------------------------------------- --------------- --------- ------------
  Consultar integrações/endpoints              ✓            ✓          ✓
  Criar/editar/excluir integrações             ✓            ✓         ---
  Criar/editar/excluir endpoints               ✓            ✓         ---
  Testar endpoints                             ✓            ✓          ✓
  Gerenciar usuários                           ✓           ---        ---
  Gerenciar credenciais de integração          ✓            ✓         ---

Perfis internos:

``` text
A = Administrador
C = Criador
U = Consumidor
```

O frontend aplica as permissões e mantém o Consumidor em modo somente
leitura.

### Senhas

O gerenciamento de usuários inclui:

-   criação de usuário;
-   senha temporária;
-   troca obrigatória de senha;
-   reset administrativo;
-   armazenamento somente do hash BCrypt.

------------------------------------------------------------------------

## Autenticação dos endpoints dinâmicos

A autenticação administrativa é independente da autenticação de consumo.

Cada integração define:

``` text
NONE
API_KEY
```

### `NONE`

O endpoint pode ser consumido sem API Key.

``` http
GET /api/pedidos/listar?status=ABERTO
```

### `API_KEY`

Todos os endpoints da integração exigem o header:

``` http
X-API-Key: ihub_xxxxxxxxxxxxxxxxx
```

### Credenciais de integração --- V2

A V2 introduz **múltiplas credenciais por integração**.

Uma credencial representa um consumidor externo da integração, por
exemplo:

``` text
Sistema Tasy
Sistema Financeiro
Cliente X
Teste Daniel
```

Cada credencial pertence a uma integração e possui:

``` text
id
integrationId
name
apiKey
active
lastUsedAt
createdBy
createdAt
updatedBy
updatedAt
```

As credenciais são independentes dos usuários administrativos do IHUB.

Uma integração pode possuir várias credenciais e cada credencial poderá
consumir todos os endpoints pertencentes àquela integração.

A API Key:

-   é gerada aleatoriamente com prefixo `ihub_`;
-   é protegida no Oracle com criptografia reversível AES-256-GCM;
-   pode ser consultada novamente por usuários autorizados;
-   pode ser regenerada;
-   pode ser ativada ou desativada;
-   nunca é retornada nas listagens de credenciais;
-   não deve ser enviada como parâmetro da URL;
-   deve ser enviada no header `X-API-Key`.

A chave mestra AES utilizada para proteger as credenciais fica fora do
banco e do repositório:

``` text
API_KEY_ENCRYPTION_KEY
```

A perda ou substituição dessa chave impede a descriptografia das API
Keys já armazenadas.

> **Status da V2:** o gerenciamento administrativo das múltiplas
> credenciais já está implementado. A autenticação dos endpoints
> dinâmicos ainda está sendo migrada do mecanismo legado de uma única
> API Key para as novas credenciais.

### APIs de credenciais

Somente `ADMIN` e `CREATOR` possuem acesso.

``` http
GET  /api/integrations/{integrationId}/credentials
GET  /api/integrations/{integrationId}/credentials/{credentialId}

POST /api/integrations/{integrationId}/credentials
PUT  /api/integrations/{integrationId}/credentials/{credentialId}
PUT  /api/integrations/{integrationId}/credentials/{credentialId}/active

GET  /api/integrations/{integrationId}/credentials/{credentialId}/api-key
POST /api/integrations/{integrationId}/credentials/{credentialId}/api-key
```

Exemplo de criação:

``` http
POST /api/integrations/1/credentials
Authorization: Bearer <token>
Content-Type: application/json
```

``` json
{
  "name": "Sistema Tasy"
}
```

A criação retorna a API Key gerada:

``` json
{
  "apiKey": "ihub_xxxxxxxxxxxxxxxxx"
}
```

A listagem retorna apenas os metadados da credencial, sem expor
`apiKeyEncrypted` ou a API Key.

### Mecanismo legado da V1

Durante a migração da V2, ainda existem em `IH_INTEGRATION`:

``` text
API_KEY_HASH
API_KEY_CREATED_AT
```

e a rota legada:

``` http
POST /api/integrations/{id}/api-key
```

A autenticação dos endpoints dinâmicos ainda utiliza esse mecanismo até
a conclusão da etapa de migração da V2.

------------------------------------------------------------------------

## Ambientes e DataSources

O backend suporta conexões Oracle configuradas em:

``` text
integration-hub.datasource.connections
```

A tela de login carrega os ambientes dinamicamente:

``` http
GET /api/environments
```

Exemplo:

``` json
[
  {
    "id": "development",
    "name": "Desenvolvimento Local"
  },
  {
    "id": "cloud",
    "name": "Oracle Cloud"
  }
]
```

Quando existe apenas uma conexão configurada, ela também é utilizada
como DataSource padrão para operações sem ambiente explícito, como
health check e bootstrap. Com múltiplas conexões, o ambiente selecionado
continua obrigatório.

### Configuração local

Configurações reais, credenciais, Wallets e segredos não devem ser
versionados.

Configurações sensíveis são fornecidas por arquivos locais ignorados
pelo Git ou variáveis de ambiente, incluindo:

``` text
JWT_SECRET
API_KEY_ENCRYPTION_KEY
```

O CORS é configurável por ambiente através de:

``` text
integration-hub.cors.allowed-origins
```

ou:

``` text
CORS_ALLOWED_ORIGINS
```

------------------------------------------------------------------------

## Persistência Oracle

As tabelas próprias da aplicação utilizam o prefixo `IH_`.

Principais tabelas atuais:

``` text
IH_INTEGRATION
IH_ENDPOINT
IH_USERS
IH_INTEGRATION_CREDENTIAL
```

### `IH_INTEGRATION_CREDENTIAL`

Estrutura utilizada pelas múltiplas credenciais da V2:

``` text
ID
INTEGRATION_ID
NAME
API_KEY_ENCRYPTED
ACTIVE
LAST_USED_AT
CREATED_BY
CREATED_AT
UPDATED_BY
UPDATED_AT
```

Existe uma restrição de unicidade para:

``` text
(INTEGRATION_ID, NAME)
```

Portanto, uma mesma integração não pode possuir duas credenciais com o
mesmo nome.

`IH_INTEGRATION` ainda mantém temporariamente os campos legados:

``` text
AUTH_TYPE
API_KEY_HASH
API_KEY_CREATED_AT
```

Os scripts de instalação ficam em:

``` text
backend/database/install/
```

Entre eles:

``` text
003_create_ih_integration_credential.sql
```

------------------------------------------------------------------------

## Principais APIs

### Públicas

``` http
GET  /api/health
GET  /api/environments
POST /api/auth/login
```

### Sessão

``` http
GET /api/auth/me
PUT /api/auth/password
```

### Integrações

``` http
GET    /api/integrations
GET    /api/integrations/{id}
POST   /api/integrations
PUT    /api/integrations/{id}
DELETE /api/integrations/{id}
```

A rota abaixo pertence ao mecanismo legado da V1 e será tratada durante
a migração:

``` http
POST /api/integrations/{id}/api-key
```

### Credenciais

``` http
GET  /api/integrations/{integrationId}/credentials
GET  /api/integrations/{integrationId}/credentials/{credentialId}

POST /api/integrations/{integrationId}/credentials
PUT  /api/integrations/{integrationId}/credentials/{credentialId}
PUT  /api/integrations/{integrationId}/credentials/{credentialId}/active

GET  /api/integrations/{integrationId}/credentials/{credentialId}/api-key
POST /api/integrations/{integrationId}/credentials/{credentialId}/api-key
```

### Endpoints

``` http
GET    /api/endpoints
GET    /api/endpoints/{id}
GET    /api/endpoints/integration/{integrationId}
POST   /api/endpoints
PUT    /api/endpoints/{id}
DELETE /api/endpoints/{id}
```

### Usuários

As rotas `/api/users/**` são administrativas e restritas ao perfil
Administrador.

------------------------------------------------------------------------

## Health check e documentação

Health check:

``` http
GET /api/health
```

Resposta esperada:

``` json
{
  "status": "OK",
  "database": "Online"
}
```

OpenAPI:

``` text
http://localhost:8081/v3/api-docs
```

Swagger UI:

``` text
http://localhost:8081/swagger-ui/index.html
```

Os endpoints dinâmicos são adicionados automaticamente à especificação
OpenAPI com base nas configurações persistidas no Oracle.

------------------------------------------------------------------------

## Executando localmente

### Backend

Windows:

``` powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Backend:

``` text
http://localhost:8081
```

### Frontend

``` bash
cd frontend
npm install
npm run dev
```

Frontend:

``` text
http://localhost:5175
```

O Vite encaminha `/api` para o backend local em `localhost:8081`.

------------------------------------------------------------------------

## Validação

### Backend

Windows:

``` powershell
cd backend
.\mvnw.cmd clean verify
```

Linux / Git Bash:

``` bash
cd backend
./mvnw clean verify
```

### Frontend

``` bash
cd frontend
npm run lint
npm run test
npm run build
```

------------------------------------------------------------------------

## CI

O workflow:

``` text
.github/workflows/validate.yml
```

é executado em `push` e `pull_request` para `main`, além de permitir
execução manual.

Ele valida:

``` text
Backend
  Java 21
  Maven clean verify

Frontend
  Node 24
  npm ci
  lint
  testes
  build
```

------------------------------------------------------------------------

## Segurança

Entre os controles atualmente adotados estão:

-   JWT stateless nas APIs administrativas;
-   BCrypt para senhas;
-   RBAC por operação;
-   API Key opcional por integração;
-   AES-256-GCM para proteção reversível das credenciais de integração
    da V2;
-   chave mestra de criptografia fora do banco e do repositório;
-   credenciais de integração restritas a `ADMIN` e `CREATOR`;
-   API Keys omitidas das listagens;
-   bind parameters;
-   validação de parâmetros antes do banco;
-   execução dinâmica restrita a `SELECT`;
-   bloqueio de comentários, `;` e `FOR UPDATE`;
-   limite de resultados;
-   CORS configurável por ambiente;
-   HTTPS no ambiente publicado;
-   segredos e credenciais fora do repositório;
-   tratamento centralizado de erros.

------------------------------------------------------------------------

## Estrutura resumida

``` text
integration-hub/
├── .github/workflows/
├── backend/
│   ├── database/install/
│   ├── src/main/java/br/com/integrationhub/
│   │   ├── auth/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── exception/
│   │   ├── integration/
│   │   ├── security/
│   │   ├── service/
│   │   └── user/
│   └── src/test/
│
├── frontend/
│   ├── public/
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       └── utils/
│
└── README.md
```

------------------------------------------------------------------------

## Status

### V1

A **V1 está fechada, funcional e publicada em cloud**.

Ela contempla:

-   CRUD administrativo de integrações e endpoints;
-   endpoints dinâmicos `GET`;
-   SQL parametrizado e execução segura;
-   parâmetros `VARCHAR2`, `NUMBER`, `DATE` e `TIMESTAMP`;
-   resolução dinâmica de `basePath + path`;
-   autenticação administrativa com JWT;
-   usuários persistidos no Oracle;
-   RBAC `A/C/U`;
-   senha temporária, troca obrigatória e reset;
-   autenticação `NONE` ou `API_KEY` por integração;
-   OpenAPI/Swagger;
-   frontend administrativo React;
-   testes automatizados e CI;
-   execução local e publicação em OCI;
-   Oracle Autonomous Database;
-   frontend e backend publicados com HTTPS.

### V2 --- em desenvolvimento

A V2 concentra-se em três evoluções principais:

1.  **Múltiplas credenciais/API Keys por integração**
2.  **Histórico e observabilidade das execuções**
3.  **Bootstrap de instalação e garantia de pelo menos um Administrador
    ativo**

#### Progresso

``` text
✓ Modelo Oracle de múltiplas credenciais
✓ Modelo/repository Java
✓ Criptografia AES-256-GCM
✓ Backend administrativo de credenciais
✓ Criar/listar/consultar/renomear credenciais
✓ Ativar/desativar credenciais
✓ Consultar e regenerar API Key
✓ Proteção ADMIN/CREATOR
✓ Testes automatizados do backend de credenciais
✓ Validação integrada com Oracle Cloud

→ Em andamento:
  autenticação dos endpoints dinâmicos pelas novas credenciais

○ Próximas etapas:
  frontend de gerenciamento de credenciais
  migração definitiva do mecanismo legado da V1
  histórico de execuções
  filtros/paginação e tela de execuções
  retenção do histórico
  bootstrap da primeira instalação
  proteção do último ADMIN
  testes finais, documentação e release v2.0.0
```

### Fora do escopo atual

-   endpoints dinâmicos `POST`, `PUT`, `PATCH` e `DELETE`;
-   respostas hierárquicas/agregadas;
-   mensageria;
-   processamento assíncrono;
-   Kubernetes;
-   OAuth2;
-   refresh token;
-   recursos avançados de escalabilidade distribuída.
