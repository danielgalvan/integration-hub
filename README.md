# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e disponibilizadas dinamicamente como endpoints HTTP de forma segura, documentada e controlada.

O desenvolvimento está sendo realizado de forma incremental.

A primeira versão possui foco em integrações de leitura utilizando `GET`, permitindo validar a arquitetura, o modelo de domínio, a persistência das configurações, a execução dinâmica das consultas e sua documentação antes da expansão para outros recursos.

---

## Arquitetura

O projeto utiliza uma arquitetura dividida entre backend e frontend.

### Backend

- Java 21
- Spring Boot 4.0.7
- Spring Web
- Spring JDBC
- HikariCP
- Oracle Database
- Jackson 3
- Maven
- OpenAPI 3.1
- Swagger UI

### Frontend

- React

O frontend será desenvolvido após a consolidação da API e da execução dinâmica das integrações.

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
│   │   │   │       ├── config/
│   │   │   │       │   ├── OpenApiConfig.java
│   │   │   │       │   └── DynamicOpenApiCustomizer.java
│   │   │   │       │
│   │   │   │       ├── controller/
│   │   │   │       │   └── HealthController.java
│   │   │   │       │
│   │   │   │       ├── exception/
│   │   │   │       │   └── GlobalExceptionHandler.java
│   │   │   │       │
│   │   │   │       ├── service/
│   │   │   │       │   └── DatabaseHealthService.java
│   │   │   │       │
│   │   │   │       └── integration/
│   │   │   │           ├── controller/
│   │   │   │           │   ├── DynamicEndpointController.java
│   │   │   │           │   ├── IntegrationController.java
│   │   │   │           │   └── EndpointController.java
│   │   │   │           │
│   │   │   │           ├── model/
│   │   │   │           │   ├── Integration.java
│   │   │   │           │   ├── Endpoint.java
│   │   │   │           │   └── EndpointParameter.java
│   │   │   │           │
│   │   │   │           ├── repository/
│   │   │   │           │   ├── IntegrationRepository.java
│   │   │   │           │   ├── OracleIntegrationRepository.java
│   │   │   │           │   ├── EndpointRepository.java
│   │   │   │           │   └── OracleEndpointRepository.java
│   │   │   │           │
│   │   │   │           └── service/
│   │   │   │               ├── IntegrationService.java
│   │   │   │               ├── EndpointService.java
│   │   │   │               └── DynamicEndpointService.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-local.yml
│   │   │
│   │   └── test/
│   │
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
└── README.md
```

O arquivo `application-local.yml` contém configurações específicas do ambiente local e não deve possuir credenciais versionadas em repositórios públicos.

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

Enquanto a aplicação não possuir autenticação, o usuário de criação é definido como:

```text
SYSTEM
```

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

Na V1, apenas endpoints com método:

```text
GET
```

são suportados.

---

# Parâmetros dos endpoints

Os parâmetros necessários para executar um endpoint são representados por:

```text
EndpointParameter
```

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

Um valor em outro formato, como:

```http
GET /api/pedidos/por-data?data=20/08/2026
```

é rejeitado com:

```text
400 Bad Request
Parâmetro data deve estar no formato yyyy-MM-dd
```

### TIMESTAMP

Parâmetros `TIMESTAMP` permitem que endpoints dinâmicos recebam valores contendo data e hora.

O tipo também é representado corretamente na documentação OpenAPI como `date-time`.

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

Resultando em:

```text
/api/pedidos/listar
/api/pedidos/itens
/api/pedidos/por-data
/api/pedidos/por-data-hora
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

e não:

```text
/api/pedidos
```

Também é validado o limite do segmento da URL, evitando que:

```text
/api/pedidos
```

seja considerado correspondente a:

```text
/api/pedidos-especiais
```

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

O fluxo é:

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

Exemplos:

```text
Clientes
├── GET /api/clientes/buscar
└── GET /api/clientes/listar

Pedidos
├── GET /api/pedidos/buscar
├── GET /api/pedidos/listar
├── GET /api/pedidos/itens
├── GET /api/pedidos/por-data
└── GET /api/pedidos/por-data-hora
```

O agrupamento no Swagger utiliza o nome da `Integration` como tag.

A rota interna:

```text
/api/**
```

não é apresentada ao consumidor na documentação.

Em seu lugar, são documentadas as rotas efetivamente configuradas.

Os tipos dos parâmetros também são convertidos para os schemas correspondentes do OpenAPI:

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

A execução dinâmica possui tratamento padronizado dos principais erros.

## Parâmetro obrigatório ausente

```text
400 Bad Request
```

Exemplo:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Parâmetro obrigatório não informado: status",
  "path": "/api/pedidos/listar"
}
```

## Parâmetro NUMBER inválido

```text
400 Bad Request
```

Exemplo:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Parâmetro pedido_id deve ser numérico",
  "path": "/api/pedidos/itens"
}
```

## DATE inválido

```text
400 Bad Request
```

Exemplo:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Parâmetro data deve estar no formato yyyy-MM-dd",
  "path": "/api/pedidos/por-data"
}
```

## Endpoint inexistente

```text
404 Not Found
```

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Endpoint não encontrado",
  "path": "/api/pedidos/nao-existe"
}
```

## Integração inexistente

```text
404 Not Found
```

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Integração não encontrada",
  "path": "/api/qualquer-coisa/listar"
}
```

## Erro durante a execução da consulta

Erros de acesso ou execução no banco são tratados pelo backend e retornam:

```text
500 Internal Server Error
```

As respostas de erro utilizam estrutura padronizada contendo:

```text
timestamp
status
error
message
path
```

---

# Persistência

As configurações de `Integration` e `Endpoint` são persistidas no Oracle.

```text
IntegrationController
        │
        ▼
IntegrationService
        │
        ▼
IntegrationRepository
        │
        ▼
OracleIntegrationRepository
        │
        ▼
IH_INTEGRATION
```

Para endpoints:

```text
EndpointController
        │
        ▼
EndpointService
        │
        ▼
EndpointRepository
        │
        ▼
OracleEndpointRepository
        │
        ▼
IH_ENDPOINT
```

Não são utilizados repositories em memória para `Integration` ou `Endpoint`.

Os registros permanecem disponíveis após reinicializações da aplicação.

---

# Persistência Oracle

Todas as tabelas pertencentes ao Integration Hub utilizam o prefixo:

```text
IH_
```

Atualmente existem:

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

A relação é garantida através de:

```text
IH_ENDPOINT.INTEGRATION_ID
            ↓
IH_INTEGRATION.ID
```

---

# IH_INTEGRATION

A tabela `IH_INTEGRATION` armazena as integrações configuradas.

```text
ID
NAME
DESCRIPTION
BASE_PATH
ACTIVE
CREATED_BY
CREATED_AT
UPDATED_BY
UPDATED_AT
```

Características principais:

- `ID` é a chave primária;
- `BASE_PATH` possui restrição de unicidade;
- `ACTIVE` aceita apenas `S` ou `N`;
- `CREATED_BY` possui valor padrão `SYSTEM`;
- `CREATED_AT` é preenchido automaticamente;
- `UPDATED_BY` e `UPDATED_AT` serão utilizados em alterações.

---

# IH_ENDPOINT

A tabela `IH_ENDPOINT` armazena os endpoints pertencentes às integrações.

```text
ID
INTEGRATION_ID
NAME
DESCRIPTION
PATH
METHOD
SQL_TEXT
PARAMETERS
ACTIVE
CREATED_BY
CREATED_AT
UPDATED_BY
UPDATED_AT
```

Características principais:

- `ID` é a chave primária;
- `INTEGRATION_ID` referencia `IH_INTEGRATION.ID`;
- `SQL_TEXT` utiliza `CLOB`;
- `PARAMETERS` utiliza `CLOB` e armazena JSON;
- `ACTIVE` aceita apenas `S` ou `N`;
- `METHOD` está limitado a `GET` na V1;
- `CREATED_BY` possui valor padrão `SYSTEM`;
- `CREATED_AT` é preenchido automaticamente.

A combinação:

```text
INTEGRATION_ID + PATH + METHOD
```

possui restrição de unicidade.

---

# Geração de identificadores

Os identificadores são gerados através de sequences Oracle.

```text
IH_INTEGRATION_SEQ
IH_ENDPOINT_SEQ
```

As sequences utilizam:

```sql
increment by 1
nocache
nocycle
```

Sequences Oracle não garantem ausência absoluta de intervalos entre identificadores.

---

# Scripts de banco

Os objetos próprios do Integration Hub possuem scripts de instalação versionados junto ao projeto.

```text
backend/
└── database/
    └── install/
        ├── 001_create_ih_integration.sql
        └── 002_create_ih_endpoint.sql
```

Os scripts são numerados para manter uma ordem explícita de instalação e permitir a reprodução da estrutura em novos ambientes Oracle.

---

# Ambiente Oracle de desenvolvimento

O projeto possui um ambiente Oracle local dedicado ao desenvolvimento.

A infraestrutura utiliza:

- VirtualBox;
- Oracle Linux;
- Oracle Database Free 23ai;
- Oracle Net Listener;
- rede em modo Bridge;
- endereço IPv4 estático para a VM.

O banco e o Oracle Net Listener são iniciados automaticamente durante o boot.

O Listener utiliza a porta:

```text
1521
```

A PDB utilizada pelo ambiente de desenvolvimento é:

```text
freepdb1
```

Informações específicas do ambiente, como endereço IP e credenciais, não devem ser documentadas no repositório.

---

# Pool de conexões

O backend utiliza **HikariCP** para gerenciamento das conexões com o Oracle.

A aplicação mantém conexões reutilizáveis, evitando a criação de uma nova conexão para cada requisição.

A configuração atual utiliza um pool reduzido, adequado ao desenvolvimento.

---

# Configuração do banco

As informações de conexão podem ser fornecidas através de:

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

## Profile local

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
```

Credenciais reais e informações específicas da máquina não devem ser versionadas.

---

# Executando o backend

Primeiro, certifique-se de que o Oracle esteja disponível.

Entre no diretório:

```bash
cd backend
```

No PowerShell:

```powershell
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

A aplicação utiliza a porta:

```text
8081
```

Após a inicialização:

```text
http://localhost:8081
```

---

# Health check

```http
GET /api/health
```

Permite validar:

- aplicação respondendo;
- datasource configurado;
- Oracle acessível.

---

# APIs administrativas

As configurações são gerenciadas através de APIs administrativas.

```http
GET  /api/integrations
GET  /api/integrations/{id}
POST /api/integrations

GET  /api/endpoints
GET  /api/endpoints/{id}
GET  /api/endpoints/integration/{integrationId}
POST /api/endpoints
```

---

# APIs dinâmicas

As APIs dinâmicas não exigem um controller Java específico para cada operação.

Exemplos:

```http
GET /api/pedidos/listar?status=ABERTO
GET /api/pedidos/itens?pedido_id=1
GET /api/pedidos/por-data?data=2026-08-20
```

Fluxo:

```text
Configuração no Oracle
        │
        ▼
Integration + Endpoint
        │
        ▼
URL dinâmica
        │
        ▼
Validação dos parâmetros
        │
        ▼
Execução do SQL
        │
        ▼
JSON
```

---

# Testes automatizados

O backend possui testes automatizados para controllers, services, repositories, execução dinâmica e geração da documentação OpenAPI.

Entre os cenários cobertos estão:

- criação e consulta de integrações;
- criação e consulta de endpoints;
- persistência Oracle;
- validação do `basePath`;
- resolução da integração pelo caminho;
- seleção do `basePath` mais específico;
- prevenção de correspondência parcial;
- execução de endpoint dinâmico;
- repasse dos query parameters;
- parâmetro obrigatório ausente;
- validação de `NUMBER`;
- validação de `DATE`;
- suporte a `TIMESTAMP`;
- integração inexistente;
- endpoint inexistente;
- tratamento de erro de banco;
- health check;
- geração dos endpoints dinâmicos no OpenAPI;
- remoção de `/api/**` da documentação;
- mapeamento dos tipos para schemas OpenAPI;
- exclusão de integrações e endpoints inativos da documentação;
- documentação apenas de operações `GET` na V1;
- respostas OpenAPI `200`, `400`, `404` e `500`.

A suíte atual possui:

```text
Tests run: 61
Failures: 0
Errors: 0
Skipped: 0
```

Para executar:

```bash
./mvnw clean test
```

Validação completa:

```bash
./mvnw clean verify
```

---

# Integração contínua

O projeto utiliza **GitHub Actions** para validar automaticamente o backend.

Workflow:

```text
.github/workflows/validate.yml
```

Executado em alterações relevantes através de:

```text
push → main
pull request → main
```

A validação utiliza:

```text
Java 21
Temurin
Maven Wrapper
Maven dependency cache
clean verify
```

Comando:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

---

# Segurança das configurações

Credenciais e informações específicas de ambiente não devem ser adicionadas ao repositório.

Devem permanecer fora do versionamento:

```text
DB_URL real
DB_USERNAME real
DB_PASSWORD real
application-local.yml com credenciais
endereços específicos do ambiente local
```

O mesmo artefato pode ser utilizado em diferentes ambientes alterando apenas a configuração externa.

---

# Estado atual do projeto

A base funcional da V1 já permite:

```text
✓ conexão com Oracle
✓ health check da aplicação e banco

✓ persistência de Integration
✓ persistência de Endpoint
✓ parâmetros persistidos como JSON

✓ criação de integrações
✓ criação de endpoints
✓ validação de basePath sob /api/

✓ resolução dinâmica de basePath
✓ resolução dinâmica de path
✓ execução dinâmica de endpoints GET
✓ execução de SQL configurado
✓ bind variables

✓ parâmetros VARCHAR2
✓ parâmetros NUMBER
✓ parâmetros DATE
✓ parâmetros TIMESTAMP
✓ validação de parâmetros obrigatórios
✓ validação dos tipos recebidos

✓ resposta JSON dinâmica
✓ tratamento padronizado de erros

✓ OpenAPI 3.1
✓ Swagger UI
✓ geração dinâmica da documentação
✓ agrupamento dos endpoints por Integration
✓ execução dos endpoints pelo Swagger UI

✓ testes automatizados
✓ 61 testes passando
✓ validação através de GitHub Actions
```

Isso permite que uma consulta Oracle configurada como `Integration + Endpoint` seja exposta como uma API `GET`, validada e documentada automaticamente sem a necessidade de implementar um controller específico para aquela consulta.

---

# Próximos passos

Com a execução dinâmica e a documentação OpenAPI consolidadas, os próximos passos da V1 são:

1. iniciar o frontend React para gerenciamento das integrações;
2. adicionar operações de edição e exclusão de integrações e endpoints;
3. evoluir logs e observabilidade;
4. preparar o projeto para publicação em ambiente remoto.

Autenticação, autorização, diferenciação entre perfis **Criador** e **Consumidor** e suporte a outros métodos HTTP ficam fora do escopo inicial da V1.

---

# Objetivo da V1

A V1 do Integration Hub tem como objetivo consolidar o fluxo:

```text
Cadastrar integração
        │
        ▼
Cadastrar endpoint
        │
        ▼
Definir SQL
        │
        ▼
Definir parâmetros
        │
        ▼
Persistir configuração
        │
        ▼
Resolver URL dinamicamente
        │
        ▼
Validar parâmetros
        │
        ▼
Executar SQL no Oracle
        │
        ▼
Documentar endpoint
        │
        ▼
Disponibilizar no Swagger
        │
        ▼
Retornar JSON
```

A partir dessa base, o projeto poderá evoluir para interface visual, edição das configurações, observabilidade, publicação remota e, posteriormente, recursos adicionais fora do escopo inicial da V1.