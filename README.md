# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e disponibilizadas dinamicamente como endpoints HTTP de forma segura, documentada e controlada.

O desenvolvimento está sendo realizado de forma incremental.

A primeira versão possui foco em integrações de leitura utilizando `GET`, permitindo validar a arquitetura, o modelo de domínio, a persistência das configurações e a execução dinâmica das consultas antes da expansão para outros métodos HTTP.

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

### Frontend

- React

O frontend será desenvolvido em uma etapa posterior, após a consolidação da API e da execução dinâmica das integrações.

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
id:           8
name:         Pedidos
description:  Integração para consulta de pedidos
basePath:     /api/pedidos
active:       S
createdBy:    SYSTEM
```

O campo `active` utiliza:

```text
S = ativo
N = inativo
```

Os campos de auditoria permitem identificar o usuário responsável pela criação e futura alteração dos registros.

Enquanto a aplicação não possuir autenticação, o usuário de criação é definido como:

```text
SYSTEM
```

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
integrationId:  8
name:           Listar pedidos
description:    Lista pedidos por status
path:           /listar
method:         GET
active:         S
createdBy:      SYSTEM
```

O campo `integrationId` identifica a `Integration` à qual o endpoint pertence.

Na V1, apenas endpoints com método:

```text
GET
```

são suportados.

---

# Parâmetros dos endpoints

Os parâmetros necessários para executar um endpoint são representados no backend pelo model:

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

A serialização e desserialização entre os objetos Java e o JSON persistido são realizadas pelo backend utilizando Jackson.

---

# Tipos de parâmetros suportados

Atualmente a execução dinâmica suporta os tipos:

```text
VARCHAR2
NUMBER
```

Os valores recebidos através da query string são convertidos pelo backend antes da execução da consulta.

Exemplo com `VARCHAR2`:

```http
GET /api/pedidos/listar?status=ABERTO
```

Exemplo com `NUMBER`:

```http
GET /api/pedidos/itens?pedido_id=1
```

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

Parâmetros numéricos também são validados.

Exemplo inválido:

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

O endereço final de uma integração é formado pela combinação do `basePath` da integração com o `path` do endpoint.

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
    └── /detalhes
```

Resultando em:

```text
/api/pedidos/listar
/api/pedidos/itens
/api/pedidos/detalhes
```

A resolução dinâmica dessas rotas já está implementada.

---

# Resolução dinâmica

O `DynamicEndpointController` recebe requisições `GET` que não possuem um controller específico e utiliza o caminho completo da requisição para localizar a integração configurada.

O fluxo é:

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

A integração é localizada através do `basePath` configurado.

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

A resolução da integração considera o `basePath` mais específico compatível com a requisição.

Por exemplo, caso existam duas integrações:

```text
/api/pedidos
/api/pedidos/especiais
```

uma requisição para:

```text
/api/pedidos/especiais/listar
```

deve utilizar:

```text
/api/pedidos/especiais
```

e não:

```text
/api/pedidos
```

A consulta utilizada pelo repository prioriza o maior `basePath` compatível com a URL.

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

Os parâmetros recebidos através da requisição são validados e utilizados como bind variables na execução da consulta.

Isso evita a necessidade de criar um controller Java específico para cada consulta disponibilizada pelo Integration Hub.

---

# Exemplo de endpoint dinâmico

Integração:

```text
id:       8
name:     Pedidos
basePath: /api/pedidos
active:   S
```

Endpoint:

```text
name:   Listar pedidos
path:   /listar
method: GET
```

Parâmetro:

```json
{
  "name": "status",
  "type": "VARCHAR2",
  "required": true
}
```

Requisição:

```http
GET /api/pedidos/listar?status=ABERTO
```

Exemplo de resposta:

```json
[
  {
    "ID": 1,
    "NUMERO": "PED-0001",
    "CLIENTE_NOME": "Cliente Exemplo 01",
    "STATUS": "ABERTO",
    "VALOR_TOTAL": 249.8,
    "DATA_PEDIDO": "2026-08-18T12:15:00.000Z"
  },
  {
    "ID": 4,
    "NUMERO": "PED-0004",
    "CLIENTE_NOME": "Cliente Exemplo 04",
    "STATUS": "ABERTO",
    "VALOR_TOTAL": 529.5,
    "DATA_PEDIDO": "2026-08-21T19:40:00.000Z"
  }
]
```

---

# Exemplo com parâmetro NUMBER

Um endpoint também pode utilizar parâmetros numéricos.

Exemplo:

```text
basePath: /api/pedidos
path:     /itens
```

SQL:

```sql
select id,
       pedido_id,
       produto,
       quantidade,
       valor_unitario
  from pedido_item
 where pedido_id = :pedido_id
```

Parâmetro:

```json
{
  "name": "pedido_id",
  "type": "NUMBER",
  "required": true
}
```

Requisição:

```http
GET /api/pedidos/itens?pedido_id=1
```

Exemplo de resposta:

```json
[
  {
    "ID": 1,
    "PEDIDO_ID": 1,
    "PRODUTO": "Teclado Mecanico",
    "QUANTIDADE": 1,
    "VALOR_UNITARIO": 149.9
  },
  {
    "ID": 2,
    "PEDIDO_ID": 1,
    "PRODUTO": "Mouse sem Fio",
    "QUANTIDADE": 1,
    "VALOR_UNITARIO": 99.9
  }
]
```

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

## Endpoint inexistente

```text
404 Not Found
```

Exemplo:

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

Exemplo:

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

As respostas de erro utilizam uma estrutura padronizada contendo:

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

O fluxo para integrações é:

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

O relacionamento é:

```text
IH_INTEGRATION
      │
      │ 1:N
      ▼
IH_ENDPOINT
```

A relação é garantida no banco através de uma foreign key entre:

```text
IH_ENDPOINT.INTEGRATION_ID
            ↓
IH_INTEGRATION.ID
```

---

# IH_INTEGRATION

A tabela `IH_INTEGRATION` armazena as integrações configuradas na plataforma.

Estrutura:

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
- `UPDATED_BY` será utilizado em futuras alterações;
- `UPDATED_AT` será utilizado em futuras alterações.

---

# IH_ENDPOINT

A tabela `IH_ENDPOINT` armazena os endpoints pertencentes às integrações.

Estrutura:

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
- `PARAMETERS` utiliza `CLOB`;
- `PARAMETERS` armazena JSON;
- `ACTIVE` aceita apenas `S` ou `N`;
- `METHOD` está limitado a `GET` na V1;
- `CREATED_BY` possui valor padrão `SYSTEM`;
- `CREATED_AT` é preenchido automaticamente.

A combinação abaixo possui restrição de unicidade:

```text
INTEGRATION_ID + PATH + METHOD
```

Isso impede que uma mesma integração possua duas operações iguais para a mesma rota.

---

# Geração de identificadores

Os identificadores são gerados através de sequences Oracle.

Para integrações:

```text
IH_INTEGRATION_SEQ
```

Para endpoints:

```text
IH_ENDPOINT_SEQ
```

As sequences utilizam:

```sql
increment by 1
nocache
nocycle
```

Sequences Oracle não garantem ausência absoluta de intervalos entre identificadores.

Um número pode ser consumido sem resultar em registro persistido, por exemplo, quando uma operação é revertida.

---

# Scripts de banco

Os objetos próprios do Integration Hub possuem scripts de instalação versionados junto ao projeto.

Estrutura atual:

```text
backend/
└── database/
    └── install/
        ├── 001_create_ih_integration.sql
        └── 002_create_ih_endpoint.sql
```

O primeiro script cria:

```text
IH_INTEGRATION
IH_INTEGRATION_SEQ
```

O segundo cria:

```text
IH_ENDPOINT
IH_ENDPOINT_SEQ
```

Os scripts são numerados para manter uma ordem explícita de instalação e permitir a reprodução da estrutura em novos ambientes Oracle.

---

# Ambiente Oracle de desenvolvimento

O projeto possui um ambiente Oracle local dedicado ao desenvolvimento.

A infraestrutura é executada em uma máquina virtual isolada utilizando:

- VirtualBox;
- Oracle Linux;
- Oracle Database Free 23ai;
- Oracle Net Listener;
- rede em modo Bridge;
- endereço IPv4 estático para a VM.

O banco e o Oracle Net Listener são iniciados automaticamente durante o boot do Oracle Linux.

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

Para facilitar o desenvolvimento local, pode ser utilizado um script local:

```text
start-integration-hub-db.bat
```

Esse script não faz parte do repositório e é responsável apenas pela infraestrutura Oracle.

O backend Spring Boot não é iniciado por ele.

Fluxo:

```text
Executar BAT
    │
    ▼
Verificar estado da VM
    │
    ▼
Iniciar VM quando necessário
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

Essa separação permite reiniciar o backend Spring Boot durante o desenvolvimento sem precisar reiniciar toda a infraestrutura Oracle.

---

# Pool de conexões

O backend utiliza **HikariCP** para gerenciamento das conexões com o Oracle.

A aplicação mantém um conjunto reutilizável de conexões, evitando a criação de uma nova conexão para cada requisição.

A configuração atual utiliza um pool reduzido, adequado ao ambiente de desenvolvimento.

O pool poderá ser ajustado posteriormente conforme o volume de requisições e a necessidade de escalabilidade da aplicação.

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

O profile utilizado é:

```text
local
```

O arquivo local permite executar o projeto sem precisar definir manualmente as variáveis de ambiente a cada nova sessão do terminal.

Credenciais reais e informações específicas da máquina de desenvolvimento não devem ser versionadas.

---

# Executando o backend

Primeiro, certifique-se de que o ambiente Oracle esteja disponível.

Depois entre no diretório do backend:

```bash
cd backend
```

## Desenvolvimento local

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

O backend possui um endpoint para verificar o estado da aplicação e da conexão com o banco:

```http
GET /api/health
```

O health check permite validar se:

- a aplicação está respondendo;
- o datasource está configurado;
- o Oracle está acessível.

---

# APIs administrativas

As configurações das integrações e endpoints são gerenciadas através de APIs administrativas.

Exemplos:

```http
GET /api/integrations
GET /api/integrations/{id}
POST /api/integrations

GET /api/endpoints
GET /api/endpoints/{id}
POST /api/endpoints
```

Esses endpoints administram as configurações que posteriormente serão utilizadas pela execução dinâmica.

---

# APIs dinâmicas

As APIs dinâmicas não exigem a criação de um controller Java específico para cada operação.

A URL é determinada pelas configurações armazenadas no Oracle.

Exemplos atualmente utilizados no ambiente de desenvolvimento:

```http
GET /api/pedidos/listar?status=ABERTO
GET /api/pedidos/itens?pedido_id=1
```

Fluxo simplificado:

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

O backend possui testes automatizados para controllers, services, repositories e execução dinâmica.

Entre os cenários cobertos estão:

- criação e consulta de integrações;
- criação e consulta de endpoints;
- persistência Oracle;
- resolução de integração pelo caminho da requisição;
- seleção do `basePath` mais específico;
- prevenção de correspondência parcial de `basePath`;
- execução de endpoint dinâmico;
- repasse dos query parameters;
- parâmetro obrigatório ausente;
- parâmetro `NUMBER` inválido;
- integração inexistente;
- endpoint inexistente;
- tratamento de erro de banco;
- health check.

A suíte atual possui:

```text
Tests run: 39
Failures: 0
Errors: 0
Skipped: 0
```

Para executar os testes:

```bash
./mvnw clean test
```

Para executar a validação completa:

```bash
./mvnw clean verify
```

---

# Integração contínua

O projeto utiliza **GitHub Actions** para validar automaticamente o backend.

O workflow está localizado em:

```text
.github/workflows/validate.yml
```

A validação é executada em alterações relevantes através de:

```text
push → main
pull request → main
```

O workflow utiliza filtros de `paths`, evitando execuções desnecessárias quando não existem alterações relacionadas ao backend ou ao próprio workflow.

A execução também pode ser disponibilizada manualmente através de:

```yaml
workflow_dispatch:
```

A validação utiliza:

```text
Java 21
Temurin
Maven Wrapper
Maven dependency cache
clean verify
```

O comando executado no CI é:

```bash
./mvnw --batch-mode --no-transfer-progress clean verify
```

Alterações que quebrem compilação ou testes podem ser identificadas automaticamente pelo pipeline.

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

O objetivo é permitir que o mesmo artefato seja utilizado em diferentes ambientes apenas alterando a configuração externa.

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
✓ resolução dinâmica de basePath
✓ resolução dinâmica de path
✓ execução dinâmica de endpoints GET
✓ execução de SQL configurado
✓ bind variables
✓ parâmetros VARCHAR2
✓ parâmetros NUMBER
✓ validação de parâmetros obrigatórios
✓ tratamento de NUMBER inválido
✓ resposta JSON dinâmica
✓ tratamento padronizado de erros
✓ testes automatizados
✓ validação através de GitHub Actions
```

Isso permite que uma consulta Oracle configurada como `Integration + Endpoint` seja exposta como uma API `GET` sem a necessidade de implementar um controller específico para aquela consulta.

---

# Próximos passos

Com a execução dinâmica de endpoints `GET` consolidada, as próximas evoluções previstas incluem:

1. ampliar os tipos de parâmetros suportados, incluindo datas e timestamps;
2. adicionar documentação OpenAPI/Swagger;
3. implementar autenticação e autorização;
4. diferenciar os perfis **Criador** e **Consumidor**;
5. iniciar o frontend React para gerenciamento das integrações;
6. adicionar operações de edição e exclusão de integrações e endpoints;
7. evoluir logs e observabilidade;
8. preparar o projeto para publicação em ambiente remoto;
9. avaliar suporte futuro a outros métodos HTTP.

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
Retornar JSON
```

A partir dessa base, o projeto poderá evoluir para recursos de segurança, documentação, interface visual, observabilidade e novos tipos de integração.