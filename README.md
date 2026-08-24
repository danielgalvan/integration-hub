# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e
disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de
integrações, permitindo que consultas SQL sejam configuradas e
disponibilizadas dinamicamente como endpoints HTTP de forma segura,
documentada e controlada.

O desenvolvimento está sendo realizado de forma incremental.

A primeira versão possui foco em integrações de leitura utilizando
`GET`, permitindo validar a arquitetura, o modelo de domínio, a
persistência das configurações, a execução dinâmica das consultas, sua
documentação e a interface administrativa antes da expansão para outros
recursos.

------------------------------------------------------------------------

## Arquitetura

O projeto utiliza uma arquitetura dividida entre backend e frontend.

### Backend

-   Java 21
-   Spring Boot 4.0.7
-   Spring Web
-   Spring JDBC
-   HikariCP
-   Oracle Database
-   Jackson 3
-   Maven
-   OpenAPI 3.1
-   Swagger UI

O backend é responsável pela persistência das configurações, resolução
das rotas dinâmicas, validação dos parâmetros, execução das consultas
SQL e disponibilização das APIs administrativas e dinâmicas.

Durante o desenvolvimento local, o backend utiliza:

``` text
http://localhost:8081
```

### Frontend

-   React 19
-   Vite
-   JavaScript
-   ESLint
-   npm

O frontend fornece a interface administrativa do Integration Hub.

A implementação atual já possui a estrutura visual principal da
aplicação e a tela de integrações conectada ao backend.

Durante o desenvolvimento local, a aplicação é disponibilizada em:

``` text
http://localhost:5175
```

O frontend está organizado separando:

``` text
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

------------------------------------------------------------------------

## Estrutura do projeto

``` text
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
│   │   │   │       │   ├── DynamicOpenApiCustomizer.java
│   │   │   │       │   ├── OpenApiConfig.java
│   │   │   │       │   └── WebConfig.java
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
│   │   │       ├── application.properties
│   │   │       └── application-local.yml
│   │   │
│   │   └── test/
│   │
│   ├── .mvn/
│   │   └── wrapper/
│   │       └── maven-wrapper.properties
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── frontend/
│   ├── public/
│   │
│   ├── src/
│   │   ├── assets/
│   │   │
│   │   ├── components/
│   │   │   ├── common/
│   │   │   │   ├── Button.css
│   │   │   │   └── Button.jsx
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
├── .gitignore
└── README.md
```

O arquivo `application-local.yml` contém configurações específicas do
ambiente local e não deve possuir credenciais versionadas em
repositórios públicos.

------------------------------------------------------------------------

# Modelo de integração

O Integration Hub separa uma integração em dois níveis:

``` text
Integration
    │
    │ 1:N
    ▼
Endpoint
```

Uma `Integration` funciona como agrupador lógico e define o caminho base
da API.

Cada `Endpoint` representa uma operação pertencente à integração e
contém as informações necessárias para executar uma consulta.

------------------------------------------------------------------------

## Integration

Representa um agrupamento de endpoints relacionados.

Principais propriedades:

``` text
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

``` text
id:          8
name:        Pedidos
description: Integração para consulta de pedidos
basePath:    /api/pedidos
active:      S
createdBy:   SYSTEM
```

O campo `active` utiliza:

``` text
S = ativo
N = inativo
```

Enquanto a aplicação não possuir autenticação, o usuário de criação é
definido como:

``` text
SYSTEM
```

### Regras do basePath

Toda integração deve possuir um `basePath` válido.

Na V1 são aplicadas as seguintes regras:

``` text
✓ obrigatório
✓ deve iniciar com /api/
✓ não deve terminar com /
✓ não deve conter espaços
```

Exemplos válidos:

``` text
/api/pedidos
/api/clientes
/api/pedidos/especiais
```

Exemplos inválidos:

``` text
/pedidos
api/pedidos
/api/pedidos/
/api/meus pedidos
```

Uma configuração inválida é rejeitada com `400 Bad Request` antes da
persistência.

------------------------------------------------------------------------

## Endpoint

Representa uma operação pertencente a uma integração.

Principais propriedades:

``` text
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

``` text
integrationId: 8
name:          Listar pedidos
description:   Lista pedidos por status
path:          /listar
method:        GET
active:        S
createdBy:     SYSTEM
```

O campo `integrationId` identifica a `Integration` à qual o endpoint
pertence.

Na V1, apenas endpoints com método:

``` text
GET
```

são suportados para execução dinâmica.

------------------------------------------------------------------------

# Persistência Oracle

As configurações do Integration Hub são persistidas no Oracle Database.

Todas as tabelas próprias da aplicação utilizam o prefixo:

``` text
IH_
```

As tabelas principais são:

``` text
IH_INTEGRATION
IH_ENDPOINT
```

Relacionamento:

``` text
IH_INTEGRATION
      │
      │ 1:N
      ▼
IH_ENDPOINT
```

Os scripts de instalação ficam em:

``` text
backend/database/install/
```

------------------------------------------------------------------------

# Parâmetros dos endpoints

Os parâmetros necessários para executar um endpoint são representados
por:

``` text
EndpointParameter
```

Cada parâmetro possui:

``` text
name
type
required
```

Exemplo:

``` json
{
  "name": "pedido_id",
  "type": "NUMBER",
  "required": true
}
```

Um endpoint pode possuir vários parâmetros:

``` json
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

``` text
List<EndpointParameter>
```

No Oracle, os parâmetros são armazenados em formato JSON na coluna
`PARAMETERS`, do tipo `CLOB`.

A serialização e desserialização são realizadas pelo backend utilizando
Jackson.

------------------------------------------------------------------------

# Tipos de parâmetros suportados

A execução dinâmica suporta atualmente:

``` text
VARCHAR2
NUMBER
DATE
TIMESTAMP
```

Os valores recebidos pela query string são convertidos e validados pelo
backend antes da execução da consulta.

### VARCHAR2

``` http
GET /api/pedidos/listar?status=ABERTO
```

### NUMBER

``` http
GET /api/pedidos/itens?pedido_id=1
```

### DATE

Formato aceito:

``` text
yyyy-MM-dd
```

Exemplo:

``` http
GET /api/pedidos/por-data?data=2026-08-20
```

Um valor em outro formato é rejeitado com `400 Bad Request`.

### TIMESTAMP

Parâmetros `TIMESTAMP` permitem que endpoints dinâmicos recebam valores
contendo data e hora.

O tipo também é representado na documentação OpenAPI como `date-time`.

### Parâmetros obrigatórios

Quando um parâmetro obrigatório não é informado, a execução é
interrompida antes do acesso ao banco.

Exemplo:

``` http
GET /api/pedidos/listar
```

Resposta:

``` text
400 Bad Request
Parâmetro obrigatório não informado: status
```

### NUMBER inválido

``` http
GET /api/pedidos/itens?pedido_id=abc
```

Resposta:

``` text
400 Bad Request
Parâmetro pedido_id deve ser numérico
```

------------------------------------------------------------------------

# Composição dos endpoints

O endereço final é formado pela combinação do `basePath` da integração
com o `path` do endpoint.

Exemplo:

``` text
Integration.basePath
/api/pedidos

Endpoint.path
/listar
```

Resultado:

``` text
/api/pedidos/listar
```

Uma mesma integração pode possuir diversos endpoints:

``` text
/api/pedidos
    │
    ├── /listar
    ├── /itens
    ├── /por-data
    └── /por-data-hora
```

------------------------------------------------------------------------

# Resolução dinâmica

O `DynamicEndpointController` recebe requisições `GET` sob `/api/**` que
não possuem um controller específico e utiliza o caminho completo da
requisição para localizar a integração configurada.

``` text
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

Após encontrar a integração, o restante da URL é utilizado para
localizar o endpoint correspondente.

Exemplo:

``` text
Request
/api/pedidos/itens

Integration.basePath
/api/pedidos

Endpoint.path
/itens
```

------------------------------------------------------------------------

# Resolução do basePath

A resolução considera o `basePath` mais específico compatível com a
requisição.

Caso existam:

``` text
/api/pedidos
/api/pedidos/especiais
```

uma requisição para:

``` text
/api/pedidos/especiais/listar
```

utiliza:

``` text
/api/pedidos/especiais
```

e não:

``` text
/api/pedidos
```

Também é validado o limite do segmento da URL, evitando que:

``` text
/api/pedidos
```

seja considerado correspondente a:

``` text
/api/pedidos-especiais
```

Somente integrações ativas são consideradas durante a resolução.

------------------------------------------------------------------------

# Execução dinâmica de SQL

O SQL armazenado em `IH_ENDPOINT.SQL_TEXT` é executado dinamicamente
pelo backend.

Exemplo:

``` sql
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

``` text
basePath: /api/pedidos
path:     /listar
```

a consulta fica disponível através de:

``` http
GET /api/pedidos/listar?status=ABERTO
```

Os parâmetros recebidos são validados e utilizados como bind variables.

Isso evita a necessidade de criar um controller Java específico para
cada consulta disponibilizada pelo Integration Hub.

------------------------------------------------------------------------

# OpenAPI e Swagger

O backend disponibiliza documentação OpenAPI 3.1 integrada ao Swagger
UI.

A documentação OpenAPI pode ser consultada em:

``` text
http://localhost:8081/v3/api-docs
```

A interface Swagger UI está disponível em:

``` text
http://localhost:8081/swagger-ui/index.html
```

A documentação utiliza:

``` text
Título: Integration Hub API
Versão: v1
```

Os controllers administrativos são documentados normalmente pelo
Springdoc.

Os endpoints dinâmicos são adicionados à especificação OpenAPI através
do `DynamicOpenApiCustomizer`.

``` text
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

Dessa forma, endpoints configurados no Oracle aparecem automaticamente
na documentação.

O agrupamento no Swagger utiliza o nome da `Integration` como tag.

A rota interna:

``` text
/api/**
```

não é apresentada ao consumidor na documentação.

Os tipos dos parâmetros são convertidos para os schemas correspondentes:

``` text
VARCHAR2  → string
NUMBER    → number
DATE      → date
TIMESTAMP → date-time
```

Para endpoints dinâmicos são documentadas as respostas:

``` text
200 → consulta executada com sucesso
400 → parâmetro inválido ou obrigatório não informado
404 → integração ou endpoint não encontrado
500 → erro durante a execução da consulta
```

Os endpoints dinâmicos podem ser executados diretamente pelo recurso
**Try it out** do Swagger UI.

------------------------------------------------------------------------

# Tratamento de erros

O backend possui tratamento centralizado de exceções através de:

``` text
GlobalExceptionHandler
```

O objetivo é fornecer respostas HTTP consistentes para erros de
validação, recursos não encontrados, problemas de banco e falhas
inesperadas.

------------------------------------------------------------------------

# Ambiente Oracle de desenvolvimento

O projeto possui um ambiente Oracle local dedicado ao desenvolvimento.

A infraestrutura é executada em uma máquina virtual isolada, permitindo
desenvolver e testar o Integration Hub sem depender de ambientes Oracle
externos.

O ambiente utiliza:

-   VirtualBox
-   Oracle Linux
-   Oracle Database Free 23ai
-   Oracle Net Listener
-   rede em modo Bridge para comunicação entre host e VM
-   endereço IPv4 estático para a VM

O Listener utiliza a porta padrão:

``` text
1521
```

A PDB utilizada pelo ambiente de desenvolvimento é:

``` text
freepdb1
```

Informações específicas do ambiente, como endereço IP e credenciais, não
devem ser documentadas no repositório.

------------------------------------------------------------------------

# Inicialização do ambiente Oracle

Para facilitar o desenvolvimento local, pode ser utilizado o script:

``` text
start-integration-hub-db.bat
```

O script é responsável apenas pela infraestrutura Oracle e não inicia o
backend Spring Boot.

Fluxo:

``` text
Executar BAT
    │
    ▼
Verificar estado da VM
    │
    ▼
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

O Oracle Database pode levar alguns segundos adicionais para ficar
disponível após a rede da VM começar a responder.

Por esse motivo, o script aguarda a porta `1521` estar acessível antes
de considerar o ambiente pronto.

------------------------------------------------------------------------

# Pool de conexões

O backend utiliza HikariCP para gerenciamento das conexões com o Oracle.

A aplicação mantém um conjunto reutilizável de conexões, evitando a
criação de uma nova conexão para cada requisição.

A configuração atual utiliza um pool reduzido, adequado ao ambiente de
desenvolvimento.

O pool poderá ser ajustado posteriormente conforme o volume de
requisições e a necessidade de escalabilidade da aplicação.

------------------------------------------------------------------------

# Configuração do banco

A aplicação suporta configurações diferentes conforme o ambiente de
execução.

## Configuração padrão

As informações de conexão podem ser fornecidas através das seguintes
variáveis de ambiente:

``` text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Exemplo Linux:

``` bash
export DB_URL='jdbc:oracle:thin:@//HOST:1521/SERVICE'
export DB_USERNAME='USUARIO'
export DB_PASSWORD='SENHA'
```

Exemplo PowerShell:

``` powershell
$env:DB_URL="jdbc:oracle:thin:@//HOST:1521/SERVICE"
$env:DB_USERNAME="USUARIO"
$env:DB_PASSWORD="SENHA"
```

Nenhuma credencial real deve ser adicionada ao repositório.

## Profile local

Para desenvolvimento local pode ser utilizado:

``` text
application-local.yml
```

Estrutura:

``` yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//HOST:1521/SERVICE
    username: USUARIO
    password: SENHA
```

O profile deve ser ativado através de:

``` text
local
```

O arquivo local permite executar o projeto sem precisar definir
manualmente as variáveis de ambiente a cada nova sessão do terminal.

Credenciais reais e informações específicas da máquina de
desenvolvimento não devem ser versionadas.

------------------------------------------------------------------------

# Executando o backend

Primeiro, certifique-se de que o ambiente Oracle esteja disponível.

Depois entre no diretório:

``` bash
cd backend
```

## Desenvolvimento local

Windows:

``` powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

O log deverá indicar que o profile `local` está ativo.

Linux ou Git Bash:

``` bash
./mvnw spring-boot:run
```

O backend utiliza a porta:

``` text
8081
```

------------------------------------------------------------------------

# Executando o frontend

Entre no diretório:

``` bash
cd frontend
```

Instale as dependências, caso necessário:

``` bash
npm install
```

Inicie o servidor de desenvolvimento:

``` bash
npm run dev
```

O frontend utiliza a porta:

``` text
5175
```

A aplicação estará disponível em:

``` text
http://localhost:5175
```

Para validar o frontend:

``` bash
npm run lint
npm run build
```

------------------------------------------------------------------------

# Comunicação frontend e backend

Durante o desenvolvimento local:

``` text
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

Como frontend e backend utilizam portas diferentes, o navegador
considera as aplicações origens distintas.

O backend possui configuração de CORS para permitir o acesso do frontend
local às APIs necessárias.

A configuração fica centralizada em:

``` text
br.com.integrationhub.config.WebConfig
```

A origem local autorizada é:

``` text
http://localhost:5175
```

Essa configuração é destinada ao ambiente de desenvolvimento e poderá
ser externalizada por ambiente em etapas posteriores.

------------------------------------------------------------------------

# Frontend

A interface administrativa está sendo construída de forma incremental.

A estrutura segue a separação:

``` text
App
├── Sidebar
└── Content
    ├── Header
    └── Page
```

Os componentes visuais possuem seus respectivos arquivos CSS.

Exemplo:

``` text
Sidebar.jsx
Sidebar.css

IntegrationList.jsx
IntegrationList.css
```

O `index.css` é reservado para estilos globais, normalizações e
definições compartilhadas.

## Tela de integrações

A primeira tela conectada ao backend é a página de integrações.

Fluxo:

``` text
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

A página é responsável por orquestrar o carregamento e os estados da
tela.

O `integrationService.js` concentra a comunicação HTTP com o backend.

O `IntegrationList` é responsável pela apresentação das integrações.

A implementação atual contempla:

-   carregamento das integrações reais;
-   apresentação do nome da integração;
-   apresentação do `basePath`;
-   indicação visual de integração ativa ou inativa;
-   estado de carregamento;
-   tratamento visual de erro;
-   estado vazio quando não existem integrações.

O botão **Nova integração** já faz parte do layout, mas seu formulário
ainda será implementado.

------------------------------------------------------------------------

# Health Check

A aplicação disponibiliza um endpoint próprio para verificar tanto o
funcionamento do backend quanto a conectividade com o Oracle.

``` http
GET /api/health
```

Exemplo:

``` text
http://localhost:8081/api/health
```

Resposta esperada:

``` json
{
  "database": "Online",
  "status": "OK"
}
```

------------------------------------------------------------------------

# API de Integrações

## Listar integrações

``` http
GET /api/integrations
```

Esse endpoint também é utilizado pela interface React para carregar a
tela de integrações.

## Buscar integração

``` http
GET /api/integrations/{id}
```

## Cadastrar integração

``` http
POST /api/integrations
```

Exemplo:

``` json
{
  "name": "Clientes",
  "description": "Integração para consulta de clientes",
  "basePath": "/api/clientes",
  "active": "S"
}
```

------------------------------------------------------------------------

# API de Endpoints

## Listar endpoints

``` http
GET /api/endpoints
```

## Buscar endpoint

``` http
GET /api/endpoints/{id}
```

## Listar endpoints de uma integração

``` http
GET /api/endpoints/integration/{integrationId}
```

## Cadastrar endpoint

``` http
POST /api/endpoints
```

Exemplo:

``` json
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

O `POST` é utilizado para configuração administrativa do Integration
Hub.

Os endpoints dinamicamente disponibilizados para consumidores possuem
inicialmente apenas operações `GET`.

------------------------------------------------------------------------

# Consultas parametrizadas

As consultas configuradas nos endpoints devem utilizar bind parameters.

Exemplo:

``` sql
select id,
       nome
  from cliente
 where id = :id
```

O valor de `id` é recebido pela requisição HTTP e enviado ao banco
separadamente da instrução SQL.

Valores recebidos pela API não devem ser concatenados diretamente ao
SQL.

Esse modelo reduz riscos de SQL Injection e permite que o Oracle
reutilize planos de execução com maior eficiência.

------------------------------------------------------------------------

# Validação do projeto

## Backend

Linux:

``` bash
cd backend
./mvnw clean verify
```

Windows:

``` powershell
cd backend
.\mvnw.cmd clean verify
```

## Frontend

``` bash
cd frontend
npm run lint
npm run build
```

O projeto utiliza validações automatizadas para detectar problemas de
compilação, testes e qualidade antes que alterações sejam incorporadas.

------------------------------------------------------------------------

# CI/CD

O repositório possui workflow do GitHub Actions para validação
automática do projeto.

O fluxo inclui as validações necessárias do backend e do frontend
conforme a configuração vigente do workflow.

O objetivo é impedir que problemas de compilação, testes ou build sejam
incorporados sem detecção.

------------------------------------------------------------------------

# Escopo da V1

A primeira versão do Integration Hub possui foco em:

-   cadastro de integrações;
-   cadastro de endpoints;
-   persistência das configurações no Oracle;
-   relacionamento `Integration 1:N Endpoint`;
-   endpoints de consumo do tipo `GET`;
-   consultas SQL parametrizadas;
-   validação dos parâmetros recebidos;
-   conexão com Oracle através de pool;
-   resolução dinâmica de `basePath + path`;
-   execução dinâmica das consultas;
-   retorno dos resultados em JSON;
-   documentação OpenAPI/Swagger;
-   tratamento padronizado de erros;
-   interface administrativa em React;
-   listagem visual das integrações;
-   ambiente Oracle local para desenvolvimento.

Funcionalidades adicionais serão incorporadas de maneira incremental
após a estabilização desse fluxo.

------------------------------------------------------------------------

# Fora do escopo inicial

Não fazem parte da primeira implementação:

-   operações dinâmicas `POST`;
-   operações dinâmicas `PUT`;
-   operações dinâmicas `PATCH`;
-   operações dinâmicas `DELETE`;
-   mensageria;
-   processamento assíncrono;
-   orquestração em Kubernetes;
-   recursos avançados de escalabilidade distribuída.

------------------------------------------------------------------------

# Segurança

A execução de SQL configurável exige controles específicos.

Entre os princípios do projeto estão:

-   utilização obrigatória de bind parameters;
-   proibição de concatenação direta de parâmetros no SQL;
-   validação dos parâmetros antes da execução;
-   separação entre configuração e consumo das integrações;
-   controle de acesso;
-   armazenamento seguro das credenciais;
-   restrição dos tipos de SQL permitidos;
-   auditoria das execuções em etapas futuras.

Na V1, o foco é em consultas de leitura.

------------------------------------------------------------------------

# Perfis de acesso previstos

A plataforma deverá possuir dois perfis principais.

## Criador

Responsável pela configuração das integrações.

Poderá:

-   cadastrar integrações;
-   cadastrar endpoints;
-   definir consultas SQL;
-   definir parâmetros;
-   testar consultas;
-   visualizar documentação;
-   ativar ou desativar integrações.

## Consumidor

Responsável pelo consumo e validação das integrações disponibilizadas.

Poderá:

-   consultar integrações disponíveis;
-   visualizar documentação;
-   visualizar os parâmetros necessários;
-   testar endpoints autorizados;
-   consumir as APIs publicadas.

O mecanismo de autenticação e autorização será implementado em uma etapa
posterior.

------------------------------------------------------------------------

# Estado atual

Atualmente estão implementados e validados:

-   aplicação Spring Boot com Java 21;
-   backend executando na porta `8081`;
-   conexão JDBC com Oracle;
-   persistência das configurações no Oracle;
-   repositories Oracle para integrações e endpoints;
-   configuração de datasource por ambiente;
-   profile `local` para desenvolvimento;
-   HikariCP;
-   health check da aplicação e do Oracle;
-   cadastro e consulta de integrações;
-   cadastro e consulta de endpoints;
-   consulta de endpoints por integração;
-   relacionamento `Integration 1:N Endpoint`;
-   parâmetros `VARCHAR2`, `NUMBER`, `DATE` e `TIMESTAMP`;
-   validação de parâmetros obrigatórios;
-   conversão dos parâmetros antes da execução;
-   resolução dinâmica de `basePath + path`;
-   seleção do `basePath` mais específico;
-   execução dinâmica das consultas SQL;
-   bind parameters;
-   retorno das consultas em JSON;
-   tratamento centralizado de erros;
-   OpenAPI 3.1;
-   Swagger UI;
-   documentação automática dos endpoints dinâmicos;
-   execução dos endpoints pelo Swagger UI;
-   Maven Wrapper;
-   testes automatizados do backend;
-   build com `clean verify`;
-   workflow de validação no GitHub Actions;
-   VM dedicada para Oracle;
-   Oracle Linux;
-   Oracle Database Free 23ai;
-   Oracle Net Listener;
-   endereço IPv4 estático para a VM;
-   script de inicialização e validação do ambiente Oracle;
-   frontend React 19 com Vite;
-   frontend executando na porta `5175`;
-   estrutura do frontend separada em `components`, `pages` e
    `services`;
-   layout principal com Sidebar e Header;
-   página de integrações;
-   `IntegrationList`;
-   comunicação frontend → backend através de `integrationService.js`;
-   listagem das integrações persistidas no Oracle;
-   estados de loading, erro e lista vazia;
-   configuração de CORS para o ambiente local.

------------------------------------------------------------------------

# Próximas etapas

A sequência imediata prevista para o frontend é:

1.  implementar o formulário de nova integração;
2.  conectar o formulário ao cadastro de integrações;
3.  implementar a página de endpoints;
4.  implementar a listagem de endpoints;
5.  conectar os endpoints ao backend;
6.  implementar navegação entre Integrações e Endpoints;
7.  evoluir os componentes comuns conforme surgirem necessidades reais;
8.  implementar autenticação e controle de acesso em etapa posterior.

------------------------------------------------------------------------

# Princípio de desenvolvimento

O Integration Hub está sendo desenvolvido de forma incremental.

A prioridade é manter uma arquitetura simples enquanto os requisitos
fundamentais são validados.

Novas tecnologias e componentes serão adicionados apenas quando houver
uma necessidade concreta de arquitetura, desempenho, segurança ou
escalabilidade.

O objetivo da V1 é estabelecer um fluxo funcional e confiável:

``` text
Cadastrar integração
        │
        ▼
Cadastrar endpoint
        │
        ▼
Definir SQL e parâmetros
        │
        ▼
Publicar endpoint GET
        │
        ▼
Executar consulta Oracle
        │
        ▼
Retornar JSON
```

Paralelamente, a interface React evolui para permitir que essas
configurações sejam gerenciadas visualmente.
