# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e disponibilizadas dinamicamente como endpoints HTTP de forma segura, documentada e controlada.

O desenvolvimento está sendo realizado de forma incremental.

A primeira versão possui foco em integrações de leitura utilizando `GET`, permitindo validar a arquitetura, o modelo de domínio, a persistência das configurações, a execução dinâmica das consultas, sua documentação, a interface administrativa e o controle de acesso antes da expansão para outros recursos.

---

## Arquitetura

O projeto utiliza uma arquitetura dividida entre backend, frontend e banco de dados Oracle.

```text
┌─────────────────────────┐
│        Frontend         │
│      React + Vite       │
│    localhost:5175       │
└────────────┬────────────┘
             │
             │ HTTP
             │ Authorization: Bearer JWT
             ▼
┌─────────────────────────┐
│         Backend         │
│ Spring Boot + Java 21   │
│    localhost:8081       │
└────────────┬────────────┘
             │
             │ JDBC / HikariCP
             ▼
┌─────────────────────────┐
│      Oracle Database    │
│       Free 23ai         │
└─────────────────────────┘
```

### Backend

- Java 21
- Spring Boot 4.0.7
- Spring Web
- Spring Security
- Spring JDBC
- HikariCP
- Oracle Database
- Jackson 3
- JJWT
- BCrypt
- Maven
- OpenAPI 3.1
- Swagger UI
- JUnit
- Mockito

O backend é responsável pela persistência das configurações, resolução das rotas dinâmicas, validação dos parâmetros, execução das consultas SQL, autenticação dos usuários, autorização baseada em perfis e disponibilização das APIs administrativas e dinâmicas.

Durante o desenvolvimento local, o backend utiliza:

```text
http://localhost:8081
```

### Frontend

- React 19
- Vite
- JavaScript
- ESLint
- npm

O frontend fornece a interface administrativa do Integration Hub.

A implementação atual já possui a estrutura visual principal da aplicação, gerenciamento de integrações e endpoints conectado ao backend, operações de cadastro, edição e exclusão, geração automática de parâmetros a partir do SQL e componentes reutilizáveis para confirmação e apresentação de mensagens.

A autenticação está implementada de ponta a ponta com usuários persistidos no Oracle. O frontend oferece tela de login, armazena o JWT da sessão, aplica permissões conforme o perfil, suporta troca obrigatória de senha e envia o token nas chamadas protegidas. Respostas `401 Unauthorized` encerram a sessão; respostas `403 Forbidden` indicam ausência de permissão para a operação.

Durante o desenvolvimento local, a aplicação é disponibilizada em:

```text
http://localhost:5175
```

O frontend está organizado separando:

```text
pages
    ↓
composição e orquestração das telas

components
    ↓
componentes visuais reutilizáveis

services
    ↓
comunicação HTTP com o backend
```

---

## Estrutura do projeto

```text
integration-hub/
├── .github/
│   └── workflows/
│       └── validate.yml
│
├── backend/
│   ├── database/
│   │   └── install/
│   │       ├── 001_create_ih_integration.sql
│   │       └── 002_create_ih_endpoint.sql
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── br/com/integrationhub/
│   │   │   │       ├── IntegrationHubApplication.java
│   │   │   │       │
│   │   │   │       ├── auth/
│   │   │   │       │   ├── AuthController.java
│   │   │   │       │   ├── AuthService.java
│   │   │   │       │   ├── LoginRequest.java
│   │   │   │       │   └── LoginResponse.java
│   │   │   │       │
│   │   │   │       ├── config/
│   │   │   │       │   ├── DynamicOpenApiCustomizer.java
│   │   │   │       │   ├── OpenApiConfig.java
│   │   │   │       │   ├── SecurityConfig.java
│   │   │   │       │   └── WebConfig.java
│   │   │   │       │
│   │   │   │       ├── controller/
│   │   │   │       │   └── HealthController.java
│   │   │   │       │
│   │   │   │       ├── exception/
│   │   │   │       │   ├── ApiError.java
│   │   │   │       │   └── GlobalExceptionHandler.java
│   │   │   │       │
│   │   │   │       ├── security/
│   │   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │   │       │   └── JwtService.java
│   │   │   │       │
│   │   │   │       ├── service/
│   │   │   │       │   └── DatabaseHealthService.java
│   │   │   │       │
│   │   │   │       └── integration/
│   │   │   │           ├── controller/
│   │   │   │           │   ├── DynamicEndpointController.java
│   │   │   │           │   ├── EndpointController.java
│   │   │   │           │   └── IntegrationController.java
│   │   │   │           │
│   │   │   │           ├── model/
│   │   │   │           │   ├── Endpoint.java
│   │   │   │           │   ├── EndpointParameter.java
│   │   │   │           │   └── Integration.java
│   │   │   │           │
│   │   │   │           ├── repository/
│   │   │   │           │   ├── EndpointRepository.java
│   │   │   │           │   ├── IntegrationRepository.java
│   │   │   │           │   ├── OracleEndpointRepository.java
│   │   │   │           │   └── OracleIntegrationRepository.java
│   │   │   │           │
│   │   │   │           └── service/
│   │   │   │               ├── DynamicEndpointService.java
│   │   │   │               ├── EndpointService.java
│   │   │   │               └── IntegrationService.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── application-local.yml
│   │   │
│   │   └── test/
│   │
│   ├── .mvn/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   │
│   │   ├── components/
│   │   │   ├── common/
│   │   │   │   ├── Button.css
│   │   │   │   ├── Button.jsx
│   │   │   │   ├── ConfirmDialog.css
│   │   │   │   ├── ConfirmDialog.jsx
│   │   │   │   ├── MessageDialog.css
│   │   │   │   └── MessageDialog.jsx
│   │   │   │
│   │   │   ├── endpoints/
│   │   │   │   ├── EndpointForm.css
│   │   │   │   ├── EndpointForm.jsx
│   │   │   │   ├── EndpointList.css
│   │   │   │   └── EndpointList.jsx
│   │   │   │
│   │   │   ├── integrations/
│   │   │   │   ├── IntegrationForm.css
│   │   │   │   ├── IntegrationForm.jsx
│   │   │   │   ├── IntegrationList.css
│   │   │   │   └── IntegrationList.jsx
│   │   │   │
│   │   │   └── layout/
│   │   │       ├── Header.css
│   │   │       ├── Header.jsx
│   │   │       ├── Sidebar.css
│   │   │       └── Sidebar.jsx
│   │   │
│   │   ├── pages/
│   │   │   ├── EndpointsPage.css
│   │   │   ├── EndpointsPage.jsx
│   │   │   ├── IntegrationsPage.css
│   │   │   └── IntegrationsPage.jsx
│   │   │
│   │   ├── services/
│   │   │   ├── endpointService.js
│   │   │   └── integrationService.js
│   │   │
│   │   ├── App.css
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   │
│   ├── eslint.config.js
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   └── vite.config.js
│
├── .editorconfig
├── .gitignore
└── README.md
```

O arquivo `application-local.yml` contém configurações específicas do ambiente local e é ignorado pelo Git.

Credenciais, hashes de senha, segredos JWT e informações específicas da máquina de desenvolvimento não devem ser versionados.

---

# Modelo de integração

O Integration Hub separa uma integração em dois níveis:

```text
Integration
    │
    │ 1:N
    ▼
Endpoint
```

Uma `Integration` funciona como agrupador lógico e define o caminho base da API.

Cada `Endpoint` representa uma operação pertencente à integração e contém as informações necessárias para executar uma consulta.

---

## Integration

Representa um agrupamento de endpoints relacionados.

Principais propriedades:

```text
id
name
description
basePath
active
createdBy
createdAt
updatedBy
updatedAt
```

Exemplo:

```text
id:          8
name:        Pedidos
description: Integração para consulta de pedidos
basePath:    /api/pedidos
active:      S
createdBy:   SYSTEM
```

O campo `active` utiliza:

```text
S = ativo
N = inativo
```

Na implementação atual, os campos de auditoria das configurações administrativas ainda utilizam o usuário técnico definido pela aplicação.

A autenticação administrativa já está disponível, mas a associação do usuário autenticado aos campos `createdBy` e `updatedBy` poderá ser evoluída posteriormente.

### Regras do basePath

Toda integração deve possuir um `basePath` válido.

Na V1 são aplicadas as seguintes regras:

```text
✓ obrigatório
✓ deve iniciar com /api/
✓ não deve terminar com /
✓ não deve conter espaços
```

Exemplos válidos:

```text
/api/pedidos
/api/clientes
/api/pedidos/especiais
```

Exemplos inválidos:

```text
/pedidos
api/pedidos
/api/pedidos/
/api/meus pedidos
```

Uma configuração inválida é rejeitada com `400 Bad Request` antes da persistência.

---

## Endpoint

Representa uma operação pertencente a uma integração.

Principais propriedades:

```text
id
integrationId
name
description
path
method
sqlText
parameters
active
createdBy
createdAt
updatedBy
updatedAt
```

Exemplo:

```text
integrationId: 8
name:          Listar pedidos
description:   Lista pedidos por status
path:          /listar
method:        GET
active:        S
createdBy:     SYSTEM
```

O campo `integrationId` identifica a `Integration` à qual o endpoint pertence.

Na V1, apenas endpoints com método `GET` são suportados para execução dinâmica.

---

# Persistência Oracle

As configurações do Integration Hub são persistidas no Oracle Database.

Todas as tabelas próprias da aplicação utilizam o prefixo:

```text
IH_
```

As tabelas principais são:

```text
IH_INTEGRATION
IH_ENDPOINT
IH_USERS
```

Relacionamento:

```text
IH_INTEGRATION
      │
      │ 1:N
      ▼
IH_ENDPOINT
```

Os scripts de instalação ficam em:

```text
backend/database/install/
```

---

# Parâmetros dos endpoints

Os parâmetros necessários para executar um endpoint são representados por `EndpointParameter`.

Cada parâmetro possui:

```text
name
type
required
```

Exemplo:

```json
{
  "name": "pedido_id",
  "type": "NUMBER",
  "required": true
}
```

Um endpoint pode possuir vários parâmetros:

```json
[
  {
    "name": "id",
    "type": "NUMBER",
    "required": true
  },
  {
    "name": "status",
    "type": "VARCHAR2",
    "required": false
  }
]
```

No Java, essa estrutura é representada por:

```text
List<EndpointParameter>
```

No Oracle, os parâmetros são armazenados em formato JSON na coluna `PARAMETERS`, do tipo `CLOB`.

A serialização e desserialização são realizadas pelo backend utilizando Jackson.

---

# Tipos de parâmetros suportados

A execução dinâmica suporta atualmente:

```text
VARCHAR2
NUMBER
DATE
TIMESTAMP
```

Os valores recebidos pela query string são convertidos e validados pelo backend antes da execução da consulta.

### VARCHAR2

```http
GET /api/pedidos/listar?status=ABERTO
```

### NUMBER

```http
GET /api/pedidos/itens?pedido_id=1
```

### DATE

Formato aceito:

```text
yyyy-MM-dd
```

Exemplo:

```http
GET /api/pedidos/por-data?data=2026-08-20
```

Um valor em outro formato é rejeitado com `400 Bad Request`.

### TIMESTAMP

Parâmetros `TIMESTAMP` permitem que endpoints dinâmicos recebam valores contendo data e hora.

O tipo também é representado na documentação OpenAPI como `date-time`.

### Parâmetros obrigatórios

Quando um parâmetro obrigatório não é informado, a execução é interrompida antes do acesso ao banco.

Exemplo:

```http
GET /api/pedidos/listar
```

Resposta:

```text
400 Bad Request

Parâmetro obrigatório não informado: status
```

### NUMBER inválido

```http
GET /api/pedidos/itens?pedido_id=abc
```

Resposta:

```text
400 Bad Request

Parâmetro pedido_id deve ser numérico
```

---

# Composição dos endpoints

O endereço final é formado pela combinação do `basePath` da integração com o `path` do endpoint.

Exemplo:

```text
Integration.basePath
/api/pedidos

Endpoint.path
/listar
```

Resultado:

```text
/api/pedidos/listar
```

Uma mesma integração pode possuir diversos endpoints:

```text
/api/pedidos
    │
    ├── /listar
    ├── /itens
    ├── /por-data
    └── /por-data-hora
```

---

# Resolução dinâmica

O `DynamicEndpointController` recebe requisições `GET` sob `/api/**` que não possuem um controller específico e utiliza o caminho completo da requisição para localizar a integração configurada.

```text
Requisição HTTP
        │
        ▼
DynamicEndpointController
        │
        ▼
IntegrationService
        │
        ▼
IntegrationRepository
        │
        ▼
findBestMatchByRequestPath
        │
        ▼
EndpointService
        │
        ▼
DynamicEndpointService
        │
        ▼
NamedParameterJdbcTemplate
        │
        ▼
Oracle
```

A integração é localizada através do `basePath`.

Após encontrar a integração, o restante da URL é utilizado para localizar o endpoint correspondente.

Exemplo:

```text
Request
/api/pedidos/itens

Integration.basePath
/api/pedidos

Endpoint.path
/itens
```

---

# Resolução do basePath

A resolução considera o `basePath` mais específico compatível com a requisição.

Caso existam:

```text
/api/pedidos
/api/pedidos/especiais
```

uma requisição para:

```text
/api/pedidos/especiais/listar
```

utiliza:

```text
/api/pedidos/especiais
```

e não `/api/pedidos`.

Também é validado o limite do segmento da URL, evitando que `/api/pedidos` seja considerado correspondente a `/api/pedidos-especiais`.

Somente integrações ativas são consideradas durante a resolução.

---

# Execução dinâmica de SQL

O SQL armazenado em `IH_ENDPOINT.SQL_TEXT` é executado dinamicamente pelo backend.

Exemplo:

```sql
select id,
       numero,
       cliente_nome,
       status,
       valor_total,
       data_pedido
  from pedido
 where status = :status
```

Considerando:

```text
basePath: /api/pedidos
path:     /listar
```

a consulta fica disponível através de:

```http
GET /api/pedidos/listar?status=ABERTO
```

Os parâmetros recebidos são validados e utilizados como bind variables.

Isso evita a necessidade de criar um controller Java específico para cada consulta disponibilizada pelo Integration Hub.

A execução dinâmica segue uma política restritiva na V1:

```text
✓ aceita somente SQL iniciado por SELECT
✓ aceita somente uma instrução
✓ rejeita ponto e vírgula
✓ rejeita comentários SQL
✓ rejeita SELECT ... FOR UPDATE
✓ utiliza bind parameters
✓ valida parâmetros antes da execução
✓ limita a quantidade máxima de resultados
```

A rejeição de `SELECT ... FOR UPDATE` evita bloqueios de registros durante o consumo dos endpoints.

O limite máximo de resultados é configurável através da propriedade:

```text
integration-hub.dynamic.max-results
```

O valor padrão é:

```text
1000
```

---

# Consultas parametrizadas

As consultas configuradas nos endpoints devem utilizar bind parameters.

Exemplo:

```sql
select id,
       nome
  from cliente
 where id = :id
```

O valor de `id` é recebido pela requisição HTTP e enviado ao banco separadamente da instrução SQL.

Valores recebidos pela API não devem ser concatenados diretamente ao SQL.

Esse modelo reduz riscos de SQL Injection e permite que o Oracle reutilize planos de execução com maior eficiência.

---

# Autenticação e usuários

A autenticação da V1 utiliza usuários persistidos no Oracle, senha protegida com BCrypt e JWT stateless.

```text
Usuário
   │ username + password
   ▼
POST /api/auth/login
   │
   ▼
AuthService
   │
   ├── localiza usuário em IH_USERS
   ├── valida status
   ├── valida senha com BCrypt
   ▼
JwtService
   │
   ▼
JWT com username + perfil
```

A tabela `IH_USERS` mantém os usuários da aplicação. Os perfis suportados são:

```text
A = Administrador
C = Criador
U = Consumidor
```

O status do usuário utiliza:

```text
A = ativo
I = inativo
```

A autenticação é stateless. Cada requisição protegida deve fornecer:

```http
Authorization: Bearer <token>
```

# Login

```http
POST /api/auth/login
```

O login é público. Credenciais inválidas ou usuário sem autenticação válida resultam em `401 Unauthorized`.

Quando o usuário está autenticado, mas seu perfil não permite determinada operação, a API retorna `403 Forbidden`.

O JWT contém a identidade do usuário e seu perfil, que é convertido pelo filtro de autenticação para a authority correspondente.

# Senhas

As senhas são armazenadas somente como hash BCrypt em `IH_USERS`.

O fluxo de usuários contempla senha temporária e troca obrigatória. O indicador `ie_trocar_senha` utiliza `S/N` para informar se a senha deve ser alterada no próximo acesso.

Ao criar um usuário ou executar o reset administrativo de senha, o backend gera uma senha temporária, persiste apenas seu hash BCrypt, marca a troca como obrigatória e devolve a senha temporária uma única vez ao administrador.

O usuário que entrar com senha temporária deve concluir a troca antes de utilizar normalmente a aplicação.

A administração também possui reset de senha:

```http
POST /api/users/{id}/reset-password
```

# Controle de acesso e RBAC

A V1 implementa autorização por perfil.

| Recurso | Administrador | Criador | Consumidor |
| --- | --- | --- | --- |
| Consultar integrações/endpoints | ✓ | ✓ | ✓ |
| Criar/editar/excluir integrações | ✓ | ✓ | — |
| Criar/editar/excluir endpoints | ✓ | ✓ | — |
| Executar endpoints dinâmicos GET | ✓ | ✓ | ✓ |
| Administrar usuários | ✓ | — | — |

Em termos de API:

```text
GET /api/integrations/**  → A, C, U
GET /api/endpoints/**     → A, C, U
POST/PUT/DELETE           → A, C
/api/users/**             → A
endpoints dinâmicos GET   → A, C, U
```

O `/api/health` e o login permanecem públicos. Os endpoints dinâmicos não são mais públicos: exigem JWT válido.

# API de usuários

O gerenciamento de usuários é exclusivo do perfil Administrador e contempla consulta, cadastro, atualização e reset de senha. A senha temporária gerada em cadastro/reset deve ser apresentada somente no momento da operação e não é persistida em texto puro.

# Utilização do token

O frontend centraliza as chamadas autenticadas através de `apiFetch`, que inclui automaticamente o Bearer token. `401 Unauthorized` remove a sessão local e retorna ao login. `403 Forbidden` preserva a sessão e informa que o usuário autenticado não possui permissão para a operação.

---

---

# OpenAPI e Swagger

O backend disponibiliza documentação OpenAPI 3.1 integrada ao Swagger UI.

A documentação OpenAPI pode ser consultada em:

```text
http://localhost:8081/v3/api-docs
```

A interface Swagger UI está disponível em:

```text
http://localhost:8081/swagger-ui/index.html
```

A documentação utiliza:

```text
Título: Integration Hub API
Versão: v1
```

Os controllers administrativos são documentados normalmente pelo Springdoc.

Os endpoints dinâmicos são adicionados à especificação OpenAPI através do `DynamicOpenApiCustomizer`.

```text
IH_INTEGRATION
      │
      ▼
IntegrationService
      │
      ▼
IH_ENDPOINT
      │
      ▼
EndpointService
      │
      ▼
DynamicOpenApiCustomizer
      │
      ▼
OpenAPI
      │
      ▼
Swagger UI
```

Dessa forma, endpoints configurados no Oracle aparecem automaticamente na documentação.

O agrupamento no Swagger utiliza o nome da `Integration` como tag.

A rota interna `/api/**` não é apresentada ao consumidor na documentação.

Os tipos dos parâmetros são convertidos para os schemas correspondentes:

```text
VARCHAR2  → string
NUMBER    → number
DATE      → date
TIMESTAMP → date-time
```

Para endpoints dinâmicos são documentadas as respostas:

```text
200 → consulta executada com sucesso
400 → parâmetro inválido ou obrigatório não informado
404 → integração ou endpoint não encontrado
500 → erro durante a execução da consulta
```

Os endpoints dinâmicos podem ser executados diretamente pelo recurso **Try it out** do Swagger UI.

---

# Tratamento de erros

O backend possui tratamento centralizado de exceções através de:

```text
GlobalExceptionHandler
```

O objetivo é fornecer respostas HTTP consistentes para erros de validação, recursos não encontrados, conflitos, métodos HTTP não suportados, problemas de banco e falhas inesperadas.

Formato de erro:

```json
{
  "timestamp": "2026-08-24T20:00:39.5506495",
  "status": 409,
  "error": "Conflict",
  "message": "A integração possui endpoints vinculados",
  "path": "/api/integrations/12"
}
```

Entre os status tratados pela API estão:

```text
400 Bad Request
401 Unauthorized
404 Not Found
405 Method Not Allowed
409 Conflict
500 Internal Server Error
```

O `401 Unauthorized` também é utilizado pelo Spring Security quando uma rota administrativa protegida é acessada sem autenticação válida.

---

# Ambiente Oracle de desenvolvimento

O projeto possui um ambiente Oracle local dedicado ao desenvolvimento.

A infraestrutura é executada em uma máquina virtual isolada, permitindo desenvolver e testar o Integration Hub sem depender de ambientes Oracle externos.

O ambiente utiliza:

- VirtualBox
- Oracle Linux
- Oracle Database Free 23ai
- Oracle Net Listener
- rede em modo Bridge para comunicação entre host e VM
- endereço IPv4 estático para a VM

O Listener utiliza a porta padrão:

```text
1521
```

A PDB utilizada pelo ambiente de desenvolvimento é:

```text
freepdb1
```

Informações específicas do ambiente, como endereço IP e credenciais, não devem ser documentadas no repositório.

---

# Inicialização do ambiente Oracle

O ambiente Oracle pode ser iniciado através do VirtualBox.

Após a inicialização da VM, deve-se aguardar a rede e o Oracle Listener estarem disponíveis antes de iniciar o backend.

Fluxo:

```text
Iniciar VM
    │
    ▼
Aguardar rede da VM
    │
    ▼
Aguardar Oracle Listener :1521
    │
    ▼
AMBIENTE PRONTO
```

O Oracle Database pode levar alguns segundos adicionais para ficar disponível após a rede da VM começar a responder.

---

# Pool de conexões

O backend utiliza HikariCP para gerenciamento das conexões com o Oracle.

A aplicação mantém um conjunto reutilizável de conexões, evitando a criação de uma nova conexão para cada requisição.

A configuração atual é:

```yaml
hikari:
  pool-name: IntegrationHubPool
  minimum-idle: 1
  maximum-pool-size: 5
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000
```

O pool reduzido é adequado ao ambiente atual de desenvolvimento.

Esses valores poderão ser ajustados posteriormente conforme o volume de requisições e a infraestrutura utilizada em cloud.

---

# Configuração do banco

A aplicação suporta configurações diferentes conforme o ambiente de execução.

## Configuração padrão

As informações de conexão podem ser fornecidas através das seguintes variáveis de ambiente:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Exemplo Linux:

```bash
export DB_URL='jdbc:oracle:thin:@//HOST:1521/SERVICE'
export DB_USERNAME='USUARIO'
export DB_PASSWORD='SENHA'
```

Exemplo PowerShell:

```powershell
$env:DB_URL="jdbc:oracle:thin:@//HOST:1521/SERVICE"
$env:DB_USERNAME="USUARIO"
$env:DB_PASSWORD="SENHA"
```

Nenhuma credencial real deve ser adicionada ao repositório.

---

## Configuração principal

O arquivo:

```text
backend/src/main/resources/application.yml
```

centraliza as configurações compartilhadas da aplicação.

Estrutura atual:

```yaml
spring:
  application:
    name: integration-hub

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: oracle.jdbc.OracleDriver

    hikari:
      pool-name: IntegrationHubPool
      minimum-idle: 1
      maximum-pool-size: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

server:
  port: 8081

integration-hub:
  dynamic:
    max-results: ${DYNAMIC_MAX_RESULTS:1000}

  security:
    admin:
      username: ${ADMIN_USERNAME}
      password: ${ADMIN_PASSWORD}

    jwt:
      secret: ${JWT_SECRET}
      expiration-minutes: ${JWT_EXPIRATION_MINUTES:60}
```

Dessa forma, configurações sensíveis podem ser fornecidas externamente em ambientes de execução.

---

# Profile local

Para desenvolvimento local pode ser utilizado:

```text
application-local.yml
```

Estrutura:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//HOST:1521/SERVICE
    username: USUARIO
    password: SENHA

integration-hub:
  security:
    admin:
      username: admin
      password: '$2a$10$HASH_BCRYPT'

    jwt:
      secret: SEGREDO_JWT
      expiration-minutes: 60
```

O campo:

```text
integration-hub.security.admin.password
```

deve conter o hash BCrypt, e não a senha em texto puro.

O profile deve ser ativado através de `local`.

O arquivo local permite executar o projeto sem precisar definir manualmente as variáveis de ambiente a cada nova sessão do terminal.

O `application-local.yml` é ignorado pelo Git.

Credenciais reais, hashes e informações específicas da máquina de desenvolvimento não devem ser versionadas.

---

# Executando o backend

Primeiro, certifique-se de que o ambiente Oracle esteja disponível.

Depois entre no diretório:

```bash
cd backend
```

## Desenvolvimento local

Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

O log deverá indicar que o profile `local` está ativo.

Linux ou Git Bash:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

O backend utiliza a porta:

```text
8081
```

---

# Executando o frontend

Entre no diretório:

```bash
cd frontend
```

Instale as dependências, caso necessário:

```bash
npm install
```

Inicie o servidor de desenvolvimento:

```bash
npm run dev
```

O frontend utiliza a porta:

```text
5175
```

A aplicação estará disponível em:

```text
http://localhost:5175
```

Para validar o frontend:

```bash
npm run lint
npm run test
npm run build
```

---

# Comunicação frontend e backend

Durante o desenvolvimento local:

```text
React / Vite
localhost:5175
      │
      │ HTTP
      ▼
Spring Boot
localhost:8081
      │
      ▼
Oracle Database
```

Como frontend e backend utilizam portas diferentes, o navegador considera as aplicações origens distintas.

O backend possui configuração de CORS para permitir o acesso do frontend local às APIs necessárias.

A configuração fica centralizada em:

```text
br.com.integrationhub.config.WebConfig
```

A origem local autorizada é:

```text
http://localhost:5175
```

Essa configuração é destinada ao ambiente de desenvolvimento e poderá ser externalizada por ambiente durante a publicação em cloud.

Com a autenticação administrativa, as chamadas protegidas realizadas pelo frontend deverão enviar:

```http
Authorization: Bearer <token>
```

---

# Frontend

A interface administrativa está sendo construída de forma incremental.

A estrutura segue a separação:

```text
App
├── Sidebar
└── Content
    ├── Header
    └── Page
```

Os componentes visuais possuem seus respectivos arquivos CSS.

O `index.css` é reservado para estilos globais, normalizações e definições compartilhadas.

---

## Tela de integrações

A página de integrações está conectada ao backend.

Fluxo de consulta:

```text
IntegrationsPage
      │
      ▼
integrationService.js
      │
      ▼
GET /api/integrations
      │
      ▼
Spring Boot
      │
      ▼
Oracle
      │
      ▼
IntegrationList
```

A implementação atual contempla:

- carregamento das integrações persistidas no Oracle;
- apresentação do nome e `basePath`;
- indicação visual de integração ativa ou inativa;
- estado de carregamento;
- estado vazio;
- cadastro de novas integrações;
- edição de integrações;
- exclusão de integrações;
- confirmação antes da exclusão;
- apresentação de erros através de diálogo personalizado;
- atualização automática da lista após operações bem-sucedidas.

### Cadastro

O botão **Nova integração** abre o `IntegrationForm`.

O formulário envia os dados através de:

```http
POST /api/integrations
```

Após o cadastro, a listagem é carregada novamente.

### Exclusão

Cada integração pode solicitar sua exclusão através da interface.

Antes da chamada ao backend, o `ConfirmDialog` solicita confirmação do usuário.

```text
IntegrationList
      │
      ▼
IntegrationsPage
      │
      ▼
ConfirmDialog
      │
      ▼
integrationService.deleteIntegration()
      │
      ▼
DELETE /api/integrations/{id}
```

A confirmação utiliza um componente próprio da aplicação em vez do `window.confirm` do navegador.

### MessageDialog

Mensagens de erro relevantes são apresentadas através do componente reutilizável `MessageDialog`.

Por exemplo, se uma integração possuir endpoints vinculados, o backend retorna:

```text
409 Conflict
```

com a mensagem:

```text
A integração possui endpoints vinculados
```

O frontend apresenta essa mensagem em diálogo, mantendo a experiência visual consistente com a aplicação.

---

## Tela de endpoints

A página de endpoints permite administrar as operações pertencentes a uma integração selecionada.

A implementação atual contempla:

- listagem dos endpoints da integração;
- cadastro de endpoints;
- edição de endpoints;
- exclusão com confirmação;
- indicação visual de método e status;
- geração automática dos parâmetros encontrados no SQL;
- configuração de tipo e obrigatoriedade dos parâmetros;
- validação de sincronização entre o SQL e os parâmetros antes do salvamento;
- atualização automática da lista após operações bem-sucedidas;
- apresentação de erros através do `MessageDialog`;
- modal para teste de endpoints;
- apresentação da URL executada;
- apresentação da duração da chamada;
- apresentação da resposta retornada.

As bind variables são identificadas a partir do SQL.

Por exemplo, uma consulta que utiliza:

```sql
where id = :id
  and status = :status
```

permite gerar automaticamente:

```text
id
status
```

como parâmetros do endpoint.

Isso evita configuração manual duplicada e reduz inconsistências entre o SQL e a definição dos parâmetros.

---

# Autenticação no frontend

O fluxo administrativo de autenticação está implementado:

```text
Tela de login
      │
      ▼
POST /api/auth/login
      │
      ▼
JWT
      │
      ▼
armazenamento da sessão no frontend
      │
      ▼
Authorization: Bearer <token>
      │
      ▼
APIs administrativas
```

As chamadas para:

```text
/api/integrations/**
/api/endpoints/**
```

incluem automaticamente o cabeçalho:

```http
Authorization: Bearer <token>
```

Quando a API retorna:

```text
401 Unauthorized
```

o token local é removido e a aplicação retorna à tela de login. O logout também remove a sessão armazenada.

---

# Health Check

A aplicação disponibiliza um endpoint próprio para verificar tanto o funcionamento do backend quanto a conectividade com o Oracle.

```http
GET /api/health
```

Exemplo:

```text
http://localhost:8081/api/health
```

Resposta esperada:

```json
{
  "database": "Online",
  "status": "OK"
}
```

O health check permanece público e não exige JWT.

---

# API de autenticação

## Login

```http
POST /api/auth/login
```

A rota é pública.

Requisição:

```json
{
  "username": "admin",
  "password": "senha"
}
```

Resposta:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

O token retornado deve ser utilizado nas APIs administrativas.

Exemplo:

```http
Authorization: Bearer eyJ...
```

Credenciais inválidas resultam em:

```text
401 Unauthorized
```

---

# API de Integrações

A API administrativa de integrações possui atualmente operações de consulta, cadastro, atualização e exclusão.

Todas as rotas:

```text
/api/integrations/**
```

exigem autenticação JWT.

## Listar integrações

```http
GET /api/integrations
```

## Buscar integração

```http
GET /api/integrations/{id}
```

## Cadastrar integração

```http
POST /api/integrations
```

Exemplo:

```json
{
  "name": "Clientes",
  "description": "Integração para consulta de clientes",
  "basePath": "/api/clientes",
  "active": "S"
}
```

## Atualizar integração

```http
PUT /api/integrations/{id}
```

A atualização preserva os dados de criação e registra as informações de atualização.

Exemplo:

```json
{
  "name": "Itens do Pedido",
  "description": "Itens vinculados aos pedidos",
  "basePath": "/api/itens",
  "active": "S"
}
```

## Excluir integração

```http
DELETE /api/integrations/{id}
```

A exclusão somente é permitida quando a integração não possui endpoints vinculados.

Quando existem endpoints associados, a operação retorna:

```text
409 Conflict
```

Exemplo de resposta:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "A integração possui endpoints vinculados",
  "path": "/api/integrations/12"
}
```

Essa proteção evita a remoção de uma integração que ainda possui configurações dependentes.

---

# API de Endpoints

A API administrativa de endpoints possui operações de consulta, cadastro, atualização e exclusão.

Todas as rotas:

```text
/api/endpoints/**
```

exigem autenticação JWT.

## Listar endpoints

```http
GET /api/endpoints
```

## Buscar endpoint

```http
GET /api/endpoints/{id}
```

## Listar endpoints de uma integração

```http
GET /api/endpoints/integration/{integrationId}
```

## Cadastrar endpoint

```http
POST /api/endpoints
```

Exemplo:

```json
{
  "integrationId": 1,
  "name": "Buscar cliente",
  "description": "Consulta um cliente pelo identificador",
  "path": "/buscar",
  "method": "GET",
  "sqlText": "select id, nome from cliente where id = :id",
  "parameters": [
    {
      "name": "id",
      "type": "NUMBER",
      "required": true
    }
  ],
  "active": "S"
}
```

O `POST` é utilizado para configuração administrativa do Integration Hub.

Os endpoints dinamicamente disponibilizados para consumidores possuem inicialmente apenas operações `GET`.

---

# Operações administrativas x endpoints dinâmicos

As operações `POST`, `PUT` e `DELETE` existentes na API administrativa não significam que endpoints dinâmicos desses tipos já sejam suportados.

Por exemplo:

```http
PUT /api/integrations/{id}
DELETE /api/integrations/{id}
```

são operações de administração da configuração do Integration Hub.

A execução das APIs configuradas pelos usuários continua limitada a:

```text
GET
```

na V1.

Essa separação permite evoluir a interface administrativa sem ampliar prematuramente o escopo do mecanismo dinâmico.

---

# Testes automatizados

O backend possui testes automatizados utilizando JUnit e Mockito.

Os testes cobrem componentes importantes da execução e das regras de negócio, incluindo:

- validação de `basePath`;
- valores padrão de `active` e `createdBy`;
- persistência através dos repositories simulados;
- repositories Oracle;
- execução dinâmica;
- conversão e validação de parâmetros;
- parâmetros `VARCHAR2`;
- parâmetros `NUMBER`;
- parâmetros `DATE`;
- parâmetros `TIMESTAMP`;
- parâmetros obrigatórios;
- limite máximo de resultados;
- rejeição de `SELECT ... FOR UPDATE`;
- rejeição de ponto e vírgula;
- rejeição de comentários SQL;
- serialização e desserialização dos parâmetros JSON no repositório Oracle;
- comportamento dos controllers;
- preservação das rotas administrativas;
- resposta `405 Method Not Allowed` para métodos não suportados nas rotas dinâmicas;
- respostas para recursos inexistentes;
- atualização de integrações;
- exclusão de integrações;
- bloqueio da exclusão quando existem endpoints vinculados;
- configuração CORS;
- proteção das APIs administrativas;
- bloqueio de `/api/integrations/**` sem token;
- bloqueio de `/api/endpoints/**` sem token;
- acesso administrativo com token válido;
- rejeição de token inválido.
- geração, validação, expiração e adulteração de JWT;
- comportamento do filtro JWT para requisições sem token, token válido e token inválido;
- validação de campos obrigatórios no login;
- acesso público ao login e ao health check.

O `IntegrationServiceTest` utiliza mocks de:

```text
IntegrationRepository
EndpointRepository
```

Isso permite testar as regras da camada de serviço sem necessidade de conexão real com o Oracle.

A proteção de exclusão deve garantir que o repository responsável pela remoção não seja chamado quando existirem endpoints associados à integração.

---

# Testes de autenticação

A autenticação possui cobertura em:

```text
AuthServiceTest
AuthControllerTest
JwtServiceTest
JwtAuthenticationFilterTest
SecurityConfigTest
```

Os cenários cobertos incluem:

```text
✓ login com credenciais válidas
✓ geração do JWT
✓ tokenType Bearer
✓ expiresIn
✓ validação da senha através do PasswordEncoder
✓ usuário inválido
✓ senha inválida
✓ username obrigatório
✓ password obrigatório
✓ 401 para credenciais inválidas
✓ JWT válido, expirado, adulterado e assinado por outra chave
✓ filtro de autenticação com e sem Bearer token
✓ login e health check públicos
```

No frontend, a cobertura contempla o login, persistência e remoção da sessão, inclusão do Bearer token, tratamento de `401`, logout e os fluxos principais da aplicação autenticada.

---

# Validação do projeto

## Backend

Linux:

```bash
cd backend
./mvnw clean verify
```

Windows:

```powershell
cd backend
.\mvnw.cmd clean verify
```

O build deve terminar com:

```text
BUILD SUCCESS
```

## Frontend

```bash
cd frontend

npm run lint
npm run test
npm run build
```

O projeto utiliza validações automatizadas para detectar problemas de compilação, testes e qualidade antes que alterações sejam incorporadas.

---

# CI/CD

O repositório possui workflow do GitHub Actions para validação automática do projeto.

Arquivo:

```text
.github/workflows/validate.yml
```

O workflow valida backend e frontend.

Fluxo geral:

```text
Push / execução manual
        │
        ├───────────────┐
        ▼               ▼
Validar backend     Validar frontend
        │               │
        ▼               ▼
Java 21             npm ci
        │               │
        ▼               ▼
Maven verify        lint
                        │
                        ▼
                     testes
                        │
                        ▼
                      build
```

Para o backend é executado:

```text
clean verify
```

Para o frontend são executados:

```text
npm ci
npm run lint
npm run test
npm run build
```

O objetivo é detectar automaticamente regressões ou problemas introduzidos por novos commits.

O workflow também pode ser executado manualmente através do GitHub Actions.

---

# Segurança

A execução de SQL configurável exige controles específicos.

Entre os princípios e mecanismos atualmente utilizados estão:

- utilização obrigatória de bind parameters;
- proibição de concatenação direta de parâmetros no SQL;
- validação dos parâmetros antes da execução;
- separação entre configuração e consumo das integrações;
- autenticação das APIs administrativas;
- JWT;
- sessão stateless;
- senha administrativa protegida por BCrypt;
- armazenamento externo das credenciais;
- armazenamento externo do segredo JWT;
- aceitação exclusiva de uma instrução iniciada por `SELECT`;
- rejeição de ponto e vírgula;
- rejeição de comentários SQL;
- bloqueio de `SELECT ... FOR UPDATE`;
- limite máximo de resultados;
- tratamento de tokens inválidos ou expirados;
- auditoria das execuções em etapas futuras.

Na V1, o foco dos endpoints dinâmicos continua sendo consultas de leitura.

---

# Perfis de acesso

A V1 possui três perfis efetivos de acesso:

## Administrador

Possui acesso completo às integrações, endpoints e gerenciamento de usuários, incluindo criação, edição, ativação/inativação e reset de senha.

## Criador

Pode consultar e manter integrações e endpoints, definir SQL e parâmetros e testar endpoints dinâmicos. Não possui acesso à administração de usuários.

## Consumidor

Possui acesso somente de leitura às configurações permitidas e pode executar endpoints dinâmicos autenticados. No frontend, o perfil Consumidor opera em modo readonly, sem ações de criação, edição ou exclusão.

# Autorização baseada em roles

O RBAC está implementado na V1. O perfil do usuário é incluído no JWT e transformado em authority pelo `JwtAuthenticationFilter`. A configuração de segurança diferencia leitura, manutenção e administração de usuários.

A ausência de autenticação válida produz `401 Unauthorized`; a tentativa de executar uma operação sem a role necessária produz `403 Forbidden`.

---

---

# Escopo da V1

A primeira versão do Integration Hub possui foco em:

- cadastro e gerenciamento administrativo de integrações;
- cadastro, edição e exclusão de endpoints;
- persistência das configurações no Oracle;
- relacionamento `Integration 1:N Endpoint`;
- endpoints de consumo do tipo `GET`;
- consultas SQL parametrizadas;
- validação dos parâmetros recebidos;
- conexão com Oracle através de pool;
- resolução dinâmica de `basePath + path`;
- execução dinâmica das consultas;
- retorno dos resultados em JSON;
- documentação OpenAPI/Swagger;
- tratamento padronizado de erros;
- interface administrativa em React;
- criação, edição e exclusão de integrações pelo frontend;
- criação, edição e exclusão de endpoints pelo frontend;
- componentes de diálogo reutilizáveis;
- autenticação administrativa;
- validação de usuário e senha;
- BCrypt;
- geração e validação de JWT;
- proteção das rotas administrativas;
- usuários persistidos no Oracle;
- perfis Administrador, Criador e Consumidor;
- RBAC;
- senha temporária, troca obrigatória e reset administrativo;
- proteção JWT dos endpoints dinâmicos;
- ambiente Oracle local para desenvolvimento;
- testes automatizados;
- validação por CI;
- publicação da aplicação e banco em ambiente cloud.

Funcionalidades adicionais serão incorporadas de maneira incremental após a estabilização desse fluxo.

---

# Fora do escopo inicial

Não fazem parte da primeira implementação dos endpoints dinâmicos:

- operações dinâmicas `POST`;
- operações dinâmicas `PUT`;
- operações dinâmicas `PATCH`;
- operações dinâmicas `DELETE`;
- mensageria;
- processamento assíncrono;
- orquestração em Kubernetes;
- refresh token;
- autenticação avançada de consumidores;
- recursos avançados de escalabilidade distribuída.

---

# Estado atual

Atualmente estão implementados e validados:

## Backend e infraestrutura

- aplicação Spring Boot com Java 21;
- backend executando na porta `8081`;
- conexão JDBC com Oracle;
- persistência das configurações no Oracle;
- repositories Oracle para integrações e endpoints;
- configuração de datasource por ambiente;
- profile `local` para desenvolvimento;
- HikariCP;
- health check da aplicação e do Oracle;
- Maven Wrapper;
- tratamento centralizado de erros;
- OpenAPI 3.1;
- Swagger UI;
- workflow de validação no GitHub Actions;
- `.editorconfig`;
- VM dedicada para Oracle;
- Oracle Linux;
- Oracle Database Free 23ai;
- Oracle Net Listener.

## Integrações

- cadastro de integrações;
- consulta de integrações;
- atualização de integrações;
- exclusão de integrações;
- proteção contra exclusão de integração com endpoints vinculados;
- resposta `409 Conflict` para conflitos de exclusão;
- validação de `basePath`;
- relacionamento `Integration 1:N Endpoint`.

## Endpoints

- cadastro de endpoints;
- consulta de endpoints;
- consulta de endpoints por integração;
- atualização de endpoints;
- exclusão de endpoints;
- parâmetros `VARCHAR2`;
- parâmetros `NUMBER`;
- parâmetros `DATE`;
- parâmetros `TIMESTAMP`;
- validação de parâmetros obrigatórios;
- conversão dos parâmetros antes da execução;
- resolução dinâmica de `basePath + path`;
- seleção do `basePath` mais específico;
- execução dinâmica das consultas SQL;
- bind parameters;
- limite configurável de resultados;
- retorno das consultas em JSON;
- validações de segurança do SQL;
- documentação automática dos endpoints dinâmicos;
- execução dos endpoints pelo Swagger UI.

## Segurança

- Spring Security;
- `POST /api/auth/login`;
- usuários persistidos em `IH_USERS`;
- validação de usuário e senha via Oracle;
- perfis Administrador, Criador e Consumidor;
- RBAC por operação;
- senha temporária e troca obrigatória;
- reset administrativo de senha;
- BCrypt;
- `PasswordEncoder`;
- geração de JWT;
- validação de JWT;
- expiração configurável;
- claim de perfil no JWT;
- `JwtAuthenticationFilter`;
- sessão stateless;
- login público;
- `/api/health` público;
- `/api/integrations/**` protegido;
- `/api/endpoints/**` protegido;
- `401 Unauthorized` sem autenticação válida;
- `403 Forbidden` para operação sem permissão;
- endpoints dinâmicos protegidos por JWT;
- testes da proteção das rotas administrativas.

## Frontend

- React 19 com Vite;
- interface adaptada ao perfil autenticado;
- modo readonly para Consumidor;
- gerenciamento de usuários para Administrador;
- fluxo de senha temporária e troca obrigatória;
- diálogo para apresentação de senha temporária;
- execução autenticada de endpoints dinâmicos;
- frontend executando na porta `5175`;
- estrutura separada em `components`, `pages` e `services`;
- layout principal com Sidebar e Header;
- página de integrações;
- `IntegrationForm`;
- `IntegrationList`;
- cadastro de integrações;
- edição de integrações;
- exclusão de integrações;
- página de endpoints;
- cadastro de endpoints;
- edição de endpoints;
- exclusão de endpoints;
- geração automática de parâmetros a partir das bind variables do SQL;
- configuração de tipo e obrigatoriedade dos parâmetros;
- validação de sincronização entre SQL e parâmetros antes do salvamento;
- modal para testar endpoints diretamente pela interface;
- apresentação dos parâmetros de teste;
- apresentação da URL executada;
- apresentação da duração;
- apresentação da resposta;
- `ConfirmDialog`;
- `MessageDialog`;
- comunicação frontend → backend;
- estados de loading e lista vazia;
- tratamento visual de erros;
- configuração de CORS para o ambiente local.

## Testes e CI

- testes automatizados com JUnit;
- testes com Mockito;
- testes de repositories;
- testes de services;
- testes de controllers;
- testes da execução dinâmica;
- testes das regras administrativas;
- testes da segurança;
- build com `clean verify`;
- testes do fluxo de autenticação no backend e frontend;
- validação automática do backend pelo GitHub Actions;
- validação automática do frontend pelo GitHub Actions.

---

# Checklist da autenticação da V1

O fluxo de autenticação da V1 foi dividido nas seguintes etapas:

```text
1. Backend preparado para configuração de segurança
2. POST /api/auth/login
3. Backend validando usuário e senha
4. JWT retornado após login
5. Backend bloqueando APIs administrativas sem JWT
6. Frontend enviando JWT nas APIs administrativas
7. Tela de login no frontend
```

Estado atual:

```text
[x] configuração de segurança
[x] POST /api/auth/login
[x] validação de usuário
[x] validação de senha com BCrypt
[x] geração de JWT
[x] validação de JWT
[x] proteção de /api/integrations/**
[x] proteção de /api/endpoints/**
[x] armazenamento e controle do JWT no frontend
[x] envio do Bearer token pelo frontend
[x] tratamento de 401 no frontend
[x] tela de login
```

---

# Próximas etapas

A sequência imediata para conclusão da V1 está concentrada na publicação e validação em cloud:

1. concluir a preparação do ambiente cloud;
2. disponibilizar o Oracle do ambiente cloud;
3. publicar o backend;
4. publicar o frontend;
5. executar a validação integrada final da V1 no ambiente publicado.

---

---

# Publicação em cloud

A publicação em cloud é a última grande etapa prevista para a V1.

O objetivo é substituir a dependência do ambiente exclusivamente local por uma infraestrutura acessível externamente.

Arquitetura esperada:

```text
Internet
    │
    ▼
Frontend
    │
    │ HTTPS
    ▼
Backend Spring Boot
    │
    │ JDBC
    ▼
Oracle Database
```

No ambiente cloud deverão ser externalizadas pelo menos:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
ADMIN_USERNAME
ADMIN_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MINUTES
DYNAMIC_MAX_RESULTS
```

`ADMIN_PASSWORD` deverá continuar contendo um hash BCrypt.

`JWT_SECRET` deverá ser um segredo próprio do ambiente e não deverá ser armazenado no repositório.

A configuração local continuará disponível para desenvolvimento.

---

# Critérios para conclusão da V1

A V1 poderá ser considerada concluída quando:

```text
[x] integrações puderem ser configuradas
[x] endpoints puderem ser configurados
[x] configurações forem persistidas no Oracle
[x] endpoints GET forem resolvidos dinamicamente
[x] SQL parametrizado for executado com segurança
[x] parâmetros forem validados e convertidos
[x] resultados forem retornados em JSON
[x] endpoints forem documentados via OpenAPI
[x] frontend permitir administração das configurações
[x] backend possuir autenticação administrativa
[x] senha administrativa utilizar BCrypt
[x] backend gerar e validar JWT
[x] APIs administrativas exigirem autenticação
[x] frontend possuir tela de login
[x] frontend enviar JWT nas chamadas administrativas
[x] frontend tratar expiração/invalidação da sessão
[x] usuários forem persistidos no Oracle
[x] RBAC estiver aplicado aos perfis A/C/U
[x] frontend respeitar permissões e modo readonly do Consumidor
[x] senha temporária, troca obrigatória e reset estiverem implementados
[x] endpoints dinâmicos exigirem JWT
[x] testes automatizados, lint, build e CI estiverem validados
[ ] aplicação estiver publicada em cloud
[ ] Oracle estiver disponível para o ambiente cloud
[ ] fluxo completo estiver validado no ambiente publicado
```

---

# Status da V1

O núcleo funcional do Integration Hub já permite configurar integrações e endpoints no Oracle e disponibilizar consultas `GET` dinamicamente sem a necessidade de criar um controller Java específico para cada nova consulta.

O CRUD administrativo de integrações e endpoints está funcional.

O frontend React já permite administrar integrações e endpoints, gerar parâmetros automaticamente a partir do SQL e testar endpoints diretamente pela interface.

O backend possui autenticação baseada em usuários do Oracle, BCrypt, JWT e RBAC para os perfis Administrador, Criador e Consumidor. O frontend aplica essas permissões, incluindo modo readonly para Consumidor, gerenciamento de usuários para Administrador, senha temporária, troca obrigatória e reset de senha. Os endpoints dinâmicos também exigem autenticação JWT.

Com isso, o núcleo funcional e de segurança da V1 está implementado. O trabalho restante está concentrado na publicação e validação final em cloud.

Após essa etapa, a última grande fase da V1 será a publicação do banco, backend e frontend em ambiente cloud.

A expansão para novos métodos dinâmicos, múltiplos usuários, RBAC avançado, mensageria e recursos de escalabilidade permanece fora do escopo inicial.
