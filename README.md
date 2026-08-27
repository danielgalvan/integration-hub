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

O backend é responsável pela persistência das configurações, resolução das rotas dinâmicas, validação dos parâmetros, execução das consultas SQL, autenticação administrativa e disponibilização das APIs administrativas e dinâmicas.

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

A autenticação já está implementada no backend. A integração da autenticação JWT com o frontend é a próxima etapa do desenvolvimento da V1.

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

# Autenticação administrativa

O backend possui autenticação administrativa baseada em JWT.

O fluxo é:

```text
Usuário
   │
   │ username + password
   ▼
POST /api/auth/login
   │
   ▼
AuthController
   │
   ▼
AuthService
   │
   ├── valida username
   │
   ├── valida senha com BCrypt
   │
   ▼
JwtService
   │
   ▼
JWT
```

A autenticação é stateless.

O backend não cria sessão HTTP para armazenar o usuário autenticado.

Cada requisição administrativa protegida deve fornecer o JWT.

---

# Login

O endpoint de autenticação é público:

```http
POST /api/auth/login
```

Exemplo de requisição:

```json
{
  "username": "admin",
  "password": "senha"
}
```

Quando as credenciais são válidas, a API retorna:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

O campo `expiresIn` é informado em segundos.

Com a configuração padrão de 60 minutos:

```text
60 × 60 = 3600 segundos
```

Quando usuário ou senha são inválidos, a autenticação é rejeitada com:

```text
401 Unauthorized
```

---

# Senha administrativa

A senha administrativa não é armazenada em texto puro na configuração utilizada pelo backend.

A aplicação utiliza:

```text
BCryptPasswordEncoder
```

O valor configurado em:

```text
integration-hub.security.admin.password
```

deve ser um hash BCrypt.

Exemplo:

```text
$2a$10$...
```

Durante o login:

```text
senha informada
      │
      ▼
PasswordEncoder.matches()
      │
      ▼
hash BCrypt configurado
```

A senha original não precisa ser recuperada ou descriptografada.

O BCrypt realiza a comparação entre a senha recebida e o hash armazenado.

---

# JWT

A geração e validação dos tokens é realizada por:

```text
JwtService
```

O token possui atualmente:

```text
subject = username
role    = ADMIN
issuedAt
expiration
```

Exemplo conceitual:

```json
{
  "sub": "admin",
  "role": "ADMIN",
  "iat": 1787840000,
  "exp": 1787843600
}
```

Os valores reais de `iat` e `exp` são definidos no momento da geração do token.

O token é assinado utilizando o segredo configurado em:

```text
integration-hub.security.jwt.secret
```

A validade é configurada através de:

```text
integration-hub.security.jwt.expiration-minutes
```

O valor padrão é:

```text
60 minutos
```

---

# JwtAuthenticationFilter

As requisições passam pelo:

```text
JwtAuthenticationFilter
```

O filtro procura pelo header:

```http
Authorization: Bearer <token>
```

Fluxo:

```text
Requisição
    │
    ▼
Authorization header existe?
    │
    ├── não ──► continua sem autenticação
    │
    ▼
Bearer token
    │
    ▼
JwtService.isTokenValid()
    │
    ├── inválido ──► continua sem autenticação
    │
    ▼
obtém username
    │
    ▼
cria Authentication
    │
    ▼
SecurityContext
```

Quando uma rota exige autenticação e nenhum usuário válido foi colocado no `SecurityContext`, o Spring Security retorna:

```text
401 Unauthorized
```

O filtro adiciona atualmente a authority:

```text
ROLE_ADMIN
```

Na V1, as rotas administrativas exigem apenas que o usuário esteja autenticado.

O uso efetivo de autorização baseada em roles poderá ser evoluído posteriormente.

---

# Controle de acesso

A configuração de segurança fica em:

```text
br.com.integrationhub.config.SecurityConfig
```

A aplicação utiliza:

```text
SessionCreationPolicy.STATELESS
```

As seguintes rotas são públicas:

```text
/api/auth/**
/api/health
```

As seguintes rotas administrativas exigem autenticação:

```text
/api/integrations/**
/api/endpoints/**
```

As demais rotas permanecem públicas na configuração atual.

Isso permite que os endpoints dinâmicos de consumo continuem acessíveis sem JWT durante a V1.

Exemplo:

```text
/api/pedidos/listar
```

continua sendo um endpoint de consumo público.

Já:

```text
/api/integrations
/api/endpoints
```

exigem autenticação.

---

# Utilização do token

Após realizar o login, o cliente deve enviar o token nas chamadas administrativas.

Exemplo:

```http
GET /api/integrations
Authorization: Bearer eyJ...
```

Sem o token:

```text
401 Unauthorized
```

Com token inválido ou expirado:

```text
401 Unauthorized
```

Com token válido:

```text
requisição administrativa autorizada
```

A integração desse fluxo com o frontend é uma das etapas restantes da V1.

---

# Configuração de segurança

A configuração principal utiliza variáveis de ambiente.

```yaml
integration-hub:
  security:
    admin:
      username: ${ADMIN_USERNAME}
      password: ${ADMIN_PASSWORD}

    jwt:
      secret: ${JWT_SECRET}
      expiration-minutes: ${JWT_EXPIRATION_MINUTES:60}
```

As variáveis utilizadas são:

```text
ADMIN_USERNAME
ADMIN_PASSWORD
JWT_SECRET
JWT_EXPIRATION_MINUTES
```

`ADMIN_PASSWORD` deve conter o hash BCrypt da senha administrativa.

`JWT_SECRET` contém o segredo utilizado para assinatura e validação dos tokens.

O segredo JWT não é a senha do administrador e não deve ser confundido com o hash BCrypt.

Nenhum desses valores reais deve ser versionado.

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

O backend de autenticação já está implementado.

O frontend ainda precisa incorporar o fluxo administrativo de autenticação.

O fluxo previsto é:

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

deverão incluir o token.

Também será necessário tratar:

```text
401 Unauthorized
```

para sessões inexistentes, inválidas ou expiradas.

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

A suíte atual validada antes da inclusão dos testes específicos de autenticação possui:

```text
108 testes
0 failures
0 errors
0 skipped
```

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

O `IntegrationServiceTest` utiliza mocks de:

```text
IntegrationRepository
EndpointRepository
```

Isso permite testar as regras da camada de serviço sem necessidade de conexão real com o Oracle.

A proteção de exclusão deve garantir que o repository responsável pela remoção não seja chamado quando existirem endpoints associados à integração.

---

# Testes de autenticação

A segurança já possui cobertura das rotas administrativas através do:

```text
SecurityConfigTest
```

Os próximos testes específicos de autenticação devem cobrir:

```text
AuthServiceTest
AuthControllerTest
```

Os cenários incluem:

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
```

Isso complementa os testes já existentes da configuração de segurança.

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

A arquitetura prevê dois perfis conceituais principais.

## Criador

Responsável pela configuração das integrações.

Poderá:

- cadastrar integrações;
- atualizar integrações;
- excluir integrações quando permitido;
- cadastrar endpoints;
- definir consultas SQL;
- definir parâmetros;
- testar consultas;
- visualizar documentação;
- ativar ou desativar integrações.

As APIs administrativas necessárias para essas operações já exigem autenticação JWT.

Na V1 existe uma única conta administrativa configurada externamente.

## Consumidor

Responsável pelo consumo e validação das integrações disponibilizadas.

Poderá:

- consultar integrações disponibilizadas para consumo;
- visualizar documentação;
- visualizar os parâmetros necessários;
- testar endpoints autorizados;
- consumir as APIs publicadas.

Os endpoints dinâmicos permanecem públicos na V1.

---

# Autorização baseada em roles

O JWT já contém:

```text
role = ADMIN
```

O `JwtAuthenticationFilter` também cria a autenticação com:

```text
ROLE_ADMIN
```

Entretanto, a configuração atual utiliza:

```text
.authenticated()
```

para as APIs administrativas.

Isso é suficiente para a V1, que possui uma única conta administrativa.

Uma evolução futura poderá utilizar:

```text
hasRole(...)
hasAuthority(...)
```

para implementar RBAC e múltiplos perfis de usuário.

Esse recurso não é necessário para conclusão da V1.

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
- RBAC com múltiplos usuários e perfis;
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
- validação do usuário administrativo;
- validação da senha administrativa;
- BCrypt;
- `PasswordEncoder`;
- geração de JWT;
- validação de JWT;
- expiração configurável;
- claim `role = ADMIN`;
- `JwtAuthenticationFilter`;
- sessão stateless;
- `/api/auth/**` público;
- `/api/health` público;
- `/api/integrations/**` protegido;
- `/api/endpoints/**` protegido;
- retorno `401 Unauthorized` sem autenticação válida;
- testes da proteção das rotas administrativas.

## Frontend

- React 19 com Vite;
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
- 108 testes passando antes da inclusão dos testes específicos de autenticação;
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
[ ] armazenamento/controlе do JWT no frontend
[ ] envio do Bearer token pelo frontend
[ ] tratamento de 401 no frontend
[ ] tela de login
```

---

# Próximas etapas

A sequência imediata prevista para conclusão da V1 é:

1. adicionar os testes específicos de `AuthService`;
2. adicionar os testes específicos de `AuthController`;
3. implementar o serviço de autenticação no frontend;
4. implementar armazenamento e controle do JWT no frontend;
5. enviar `Authorization: Bearer <token>` nas chamadas administrativas;
6. tratar `401 Unauthorized` no frontend;
7. criar a tela de login;
8. validar o fluxo completo frontend → autenticação → APIs administrativas;
9. executar a suíte completa de testes;
10. validar o GitHub Actions;
11. preparar o ambiente cloud;
12. migrar ou disponibilizar o Oracle em cloud;
13. publicar o backend;
14. publicar o frontend;
15. executar validação final da V1 no ambiente cloud.

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
[ ] frontend possuir tela de login
[ ] frontend enviar JWT nas chamadas administrativas
[ ] frontend tratar expiração/invalidação da sessão
[ ] aplicação estiver publicada em cloud
[ ] Oracle estiver disponível para o ambiente cloud
[ ] fluxo completo estiver validado no ambiente publicado
```

---

# Status da V1

O núcleo funcional do Integration Hub já permite configurar integrações e endpoints no Oracle e disponibilizar consultas `GET` dinamicamente sem a necessidade de criar um controller Java específico para cada nova consulta.

O CRUD administrativo de integrações e endpoints está funcional.

O frontend React já permite administrar integrações e endpoints, gerar parâmetros automaticamente a partir do SQL e testar endpoints diretamente pela interface.

O backend agora possui autenticação administrativa baseada em JWT, senha protegida com BCrypt e bloqueio das APIs administrativas sem autenticação válida.

Com isso, o trabalho restante da aplicação antes da publicação está concentrado principalmente na integração da autenticação com o frontend.

Após essa etapa, a última grande fase da V1 será a publicação do banco, backend e frontend em ambiente cloud.

A expansão para novos métodos dinâmicos, múltiplos usuários, RBAC avançado, mensageria e recursos de escalabilidade permanece fora do escopo inicial.
