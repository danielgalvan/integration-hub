**# Integration Hub**

O **\*\*Integration Hub\*\*** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e disponibilizadas dinamicamente como endpoints HTTP de forma segura, documentada e controlada.

O desenvolvimento está sendo realizado de forma incremental.

A primeira versão possui foco em integrações de leitura utilizando \`GET\`, permitindo validar a arquitetura, o modelo de domínio, a persistência das configurações, a execução dinâmica das consultas, sua documentação e a interface administrativa antes da expansão para outros recursos.

**---**

**## Arquitetura**

O projeto utiliza uma arquitetura dividida entre backend e frontend.

**### Backend**

\- Java 21

\- Spring Boot 4.0.7

\- Spring Web

\- Spring JDBC

\- HikariCP

\- Oracle Database

\- Jackson 3

\- Maven

\- OpenAPI 3.1

\- Swagger UI

\- JUnit

\- Mockito

O backend é responsável pela persistência das configurações, resolução das rotas dinâmicas, validação dos parâmetros, execução das consultas SQL e disponibilização das APIs administrativas e dinâmicas.

Durante o desenvolvimento local, o backend utiliza:

\`\`\`text

http\://localhost:8081

\`\`\`

**### Frontend**

\- React 19

\- Vite

\- JavaScript

\- ESLint

\- npm

O frontend fornece a interface administrativa do Integration Hub.

A implementação atual já possui a estrutura visual principal da aplicação, gerenciamento de integrações e endpoints conectado ao backend, operações de cadastro, edição e exclusão, geração automática de parâmetros a partir do SQL e componentes reutilizáveis para confirmação e apresentação de mensagens.

Durante o desenvolvimento local, a aplicação é disponibilizada em:

\`\`\`text

http\://localhost:5175

\`\`\`

O frontend está organizado separando:

\`\`\`text

pages

    ↓

composição e orquestração das telas

components

    ↓

componentes visuais reutilizáveis

services

    ↓

comunicação HTTP com o backend

\`\`\`

**---**

**## Estrutura do projeto**

\`\`\`text

integration-hub/

├── .github/

│   └── workflows/

│       └── validate.yml

│

├── backend/

│   ├── database/

│   │   └── install/

│   │       ├── 001\_create\_ih\_integration.sql

│   │       └── 002\_create\_ih\_endpoint.sql

│   │

│   ├── src/

│   │   ├── main/

│   │   │   ├── java/

│   │   │   │   └── br/com/integrationhub/

│   │   │   │       ├── IntegrationHubApplication.java

│   │   │   │       ├── config/

│   │   │   │       │   ├── DynamicOpenApiCustomizer.java

│   │   │   │       │   ├── OpenApiConfig.java

│   │   │   │       │   └── WebConfig.java

│   │   │   │       ├── controller/

│   │   │   │       │   └── HealthController.java

│   │   │   │       ├── exception/

│   │   │   │       │   ├── ApiError.java

│   │   │   │       │   └── GlobalExceptionHandler.java

│   │   │   │       ├── service/

│   │   │   │       │   └── DatabaseHealthService.java

│   │   │   │       └── integration/

│   │   │   │           ├── controller/

│   │   │   │           │   ├── DynamicEndpointController.java

│   │   │   │           │   ├── EndpointController.java

│   │   │   │           │   └── IntegrationController.java

│   │   │   │           ├── model/

│   │   │   │           │   ├── Endpoint.java

│   │   │   │           │   ├── EndpointParameter.java

│   │   │   │           │   └── Integration.java

│   │   │   │           ├── repository/

│   │   │   │           │   ├── EndpointRepository.java

│   │   │   │           │   ├── IntegrationRepository.java

│   │   │   │           │   ├── OracleEndpointRepository.java

│   │   │   │           │   └── OracleIntegrationRepository.java

│   │   │   │           └── service/

│   │   │   │               ├── DynamicEndpointService.java

│   │   │   │               ├── EndpointService.java

│   │   │   │               └── IntegrationService.java

│   │   │   └── resources/

│   │   │       ├── application.properties

│   │   │       └── application-local.yml

│   │   └── test/

│   │

│   ├── .mvn/

│   ├── mvnw

│   ├── mvnw\.cmd

│   └── pom.xml

│

├── frontend/

│   ├── public/

│   ├── src/

│   │   ├── assets/

│   │   ├── components/

│   │   │   ├── common/

│   │   │   │   ├── Button.css

│   │   │   │   ├── Button.jsx

│   │   │   │   ├── ConfirmDialog.css

│   │   │   │   ├── ConfirmDialog.jsx

│   │   │   │   ├── MessageDialog.css

│   │   │   │   └── MessageDialog.jsx

│   │   │   ├── endpoints/

│   │   │   │   ├── EndpointForm.css

│   │   │   │   ├── EndpointForm.jsx

│   │   │   │   ├── EndpointList.css

│   │   │   │   └── EndpointList.jsx

│   │   │   ├── integrations/

│   │   │   │   ├── IntegrationForm.css

│   │   │   │   ├── IntegrationForm.jsx

│   │   │   │   ├── IntegrationList.css

│   │   │   │   └── IntegrationList.jsx

│   │   │   └── layout/

│   │   │       ├── Header.css

│   │   │       ├── Header.jsx

│   │   │       ├── Sidebar.css

│   │   │       └── Sidebar.jsx

│   │   ├── pages/

│   │   │   ├── EndpointsPage.css

│   │   │   ├── EndpointsPage.jsx

│   │   │   ├── IntegrationsPage.css

│   │   │   └── IntegrationsPage.jsx

│   │   ├── services/

│   │   │   ├── endpointService.js

│   │   │   └── integrationService.js

│   │   ├── App.css

│   │   ├── App.jsx

│   │   ├── index.css

│   │   └── main.jsx

│   ├── eslint.config.js

│   ├── index.html

│   ├── package.json

│   ├── package-lock.json

│   └── vite.config.js

│

├── .gitignore

└── README.md

\`\`\`

O arquivo \`application-local.yml\` contém configurações específicas do ambiente local e não deve possuir credenciais versionadas em repositórios públicos.

**---**

**# Modelo de integração**

O Integration Hub separa uma integração em dois níveis:

\`\`\`text

Integration

    │

    │ 1\:N

    ▼

Endpoint

\`\`\`

Uma \`Integration\` funciona como agrupador lógico e define o caminho base da API.

Cada \`Endpoint\` representa uma operação pertencente à integração e contém as informações necessárias para executar uma consulta.

**---**

**## Integration**

Representa um agrupamento de endpoints relacionados.

Principais propriedades:

\`\`\`text

id

name

description

basePath

active

createdBy

createdAt

updatedBy

updatedAt

\`\`\`

Exemplo:

\`\`\`text

id:          8

name:        Pedidos

description: Integração para consulta de pedidos

basePath:    /api/pedidos

active:      S

createdBy:   SYSTEM

\`\`\`

O campo \`active\` utiliza:

\`\`\`text

S = ativo

N = inativo

\`\`\`

Enquanto a aplicação não possuir autenticação, o usuário de criação e atualização é definido como:

\`\`\`text

SYSTEM

\`\`\`

**### Regras do basePath**

Toda integração deve possuir um \`basePath\` válido.

Na V1 são aplicadas as seguintes regras:

\`\`\`text

✓ obrigatório

✓ deve iniciar com /api/

✓ não deve terminar com /

✓ não deve conter espaços

\`\`\`

Exemplos válidos:

\`\`\`text

/api/pedidos

/api/clientes

/api/pedidos/especiais

\`\`\`

Exemplos inválidos:

\`\`\`text

/pedidos

api/pedidos

/api/pedidos/

/api/meus pedidos

\`\`\`

Uma configuração inválida é rejeitada com \`400 Bad Request\` antes da persistência.

**---**

**## Endpoint**

Representa uma operação pertencente a uma integração.

Principais propriedades:

\`\`\`text

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

\`\`\`

Exemplo:

\`\`\`text

integrationId: 8

name:          Listar pedidos

description:   Lista pedidos por status

path:          /listar

method:        GET

active:        S

createdBy:     SYSTEM

\`\`\`

O campo \`integrationId\` identifica a \`Integration\` à qual o endpoint pertence.

Na V1, apenas endpoints com método \`GET\` são suportados para execução dinâmica.

**---**

**# Persistência Oracle**

As configurações do Integration Hub são persistidas no Oracle Database.

Todas as tabelas próprias da aplicação utilizam o prefixo:

\`\`\`text

IH\_

\`\`\`

As tabelas principais são:

\`\`\`text

IH\_INTEGRATION

IH\_ENDPOINT

\`\`\`

Relacionamento:

\`\`\`text

IH\_INTEGRATION

      │

      │ 1\:N

      ▼

IH\_ENDPOINT

\`\`\`

Os scripts de instalação ficam em:

\`\`\`text

backend/database/install/

\`\`\`

**---**

**# Parâmetros dos endpoints**

Os parâmetros necessários para executar um endpoint são representados por \`EndpointParameter\`.

Cada parâmetro possui:

\`\`\`text

name

type

required

\`\`\`

Exemplo:

\`\`\`json

{

  "name": "pedido\_id",

  "type": "NUMBER",

  "required": true

}

\`\`\`

Um endpoint pode possuir vários parâmetros:

\`\`\`json

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

\`\`\`

No Java, essa estrutura é representada por:

\`\`\`text

List\<EndpointParameter>

\`\`\`

No Oracle, os parâmetros são armazenados em formato JSON na coluna \`PARAMETERS\`, do tipo \`CLOB\`.

A serialização e desserialização são realizadas pelo backend utilizando Jackson.

**---**

**# Tipos de parâmetros suportados**

A execução dinâmica suporta atualmente:

\`\`\`text

VARCHAR2

NUMBER

DATE

TIMESTAMP

\`\`\`

Os valores recebidos pela query string são convertidos e validados pelo backend antes da execução da consulta.

**### VARCHAR2**

\`\`\`http

GET /api/pedidos/listar?status=ABERTO

\`\`\`

**### NUMBER**

\`\`\`http

GET /api/pedidos/itens?pedido\_id=1

\`\`\`

**### DATE**

Formato aceito:

\`\`\`text

yyyy-MM-dd

\`\`\`

Exemplo:

\`\`\`http

GET /api/pedidos/por-data?data=2026-08-20

\`\`\`

Um valor em outro formato é rejeitado com \`400 Bad Request\`.

**### TIMESTAMP**

Parâmetros \`TIMESTAMP\` permitem que endpoints dinâmicos recebam valores contendo data e hora.

O tipo também é representado na documentação OpenAPI como \`date-time\`.

**### Parâmetros obrigatórios**

Quando um parâmetro obrigatório não é informado, a execução é interrompida antes do acesso ao banco.

Exemplo:

\`\`\`http

GET /api/pedidos/listar

\`\`\`

Resposta:

\`\`\`text

400 Bad Request

Parâmetro obrigatório não informado: status

\`\`\`

**### NUMBER inválido**

\`\`\`http

GET /api/pedidos/itens?pedido\_id=abc

\`\`\`

Resposta:

\`\`\`text

400 Bad Request

Parâmetro pedido\_id deve ser numérico

\`\`\`

**---**

**# Composição dos endpoints**

O endereço final é formado pela combinação do \`basePath\` da integração com o \`path\` do endpoint.

Exemplo:

\`\`\`text

Integration.basePath

/api/pedidos

Endpoint.path

/listar

\`\`\`

Resultado:

\`\`\`text

/api/pedidos/listar

\`\`\`

Uma mesma integração pode possuir diversos endpoints:

\`\`\`text

/api/pedidos

    │

    ├── /listar

    ├── /itens

    ├── /por-data

    └── /por-data-hora

\`\`\`

**---**

**# Resolução dinâmica**

O \`DynamicEndpointController\` recebe requisições \`GET\` sob \`/api/\*\*\` que não possuem um controller específico e utiliza o caminho completo da requisição para localizar a integração configurada.

\`\`\`text

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

\`\`\`

A integração é localizada através do \`basePath\`.

Após encontrar a integração, o restante da URL é utilizado para localizar o endpoint correspondente.

Exemplo:

\`\`\`text

Request

/api/pedidos/itens

Integration.basePath

/api/pedidos

Endpoint.path

/itens

\`\`\`

**---**

**# Resolução do basePath**

A resolução considera o \`basePath\` mais específico compatível com a requisição.

Caso existam:

\`\`\`text

/api/pedidos

/api/pedidos/especiais

\`\`\`

uma requisição para:

\`\`\`text

/api/pedidos/especiais/listar

\`\`\`

utiliza:

\`\`\`text

/api/pedidos/especiais

\`\`\`

e não \`/api/pedidos\`.

Também é validado o limite do segmento da URL, evitando que \`/api/pedidos\` seja considerado correspondente a \`/api/pedidos-especiais\`.

Somente integrações ativas são consideradas durante a resolução.

**---**

**# Execução dinâmica de SQL**

O SQL armazenado em \`IH\_ENDPOINT.SQL\_TEXT\` é executado dinamicamente pelo backend.

Exemplo:

\`\`\`sql

select id,

       numero,

       cliente\_nome,

       status,

       valor\_total,

       data\_pedido

  from pedido

 where status = \:status

\`\`\`

Considerando:

\`\`\`text

basePath: /api/pedidos

path:     /listar

\`\`\`

a consulta fica disponível através de:

\`\`\`http

GET /api/pedidos/listar?status=ABERTO

\`\`\`

Os parâmetros recebidos são validados e utilizados como bind variables.

Isso evita a necessidade de criar um controller Java específico para cada consulta disponibilizada pelo Integration Hub.

A execução dinâmica é limitada a consultas de leitura e possui limite configurável de resultados através da propriedade:

\`\`\`text

integration-hub.dynamic.max-results

\`\`\`

O valor padrão é \`1000\`.

**---**

**# OpenAPI e Swagger**

O backend disponibiliza documentação OpenAPI 3.1 integrada ao Swagger UI.

A documentação OpenAPI pode ser consultada em:

\`\`\`text

http\://localhost:8081/v3/api-docs

\`\`\`

A interface Swagger UI está disponível em:

\`\`\`text

http\://localhost:8081/swagger-ui/index.html

\`\`\`

A documentação utiliza:

\`\`\`text

Título: Integration Hub API

Versão: v1

\`\`\`

Os controllers administrativos são documentados normalmente pelo Springdoc.

Os endpoints dinâmicos são adicionados à especificação OpenAPI através do \`DynamicOpenApiCustomizer\`.

\`\`\`text

IH\_INTEGRATION

      │

      ▼

IntegrationService

      │

      ▼

IH\_ENDPOINT

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

\`\`\`

Dessa forma, endpoints configurados no Oracle aparecem automaticamente na documentação.

O agrupamento no Swagger utiliza o nome da \`Integration\` como tag.

A rota interna \`/api/\*\*\` não é apresentada ao consumidor na documentação.

Os tipos dos parâmetros são convertidos para os schemas correspondentes:

\`\`\`text

VARCHAR2  → string

NUMBER    → number

DATE      → date

TIMESTAMP → date-time

\`\`\`

Para endpoints dinâmicos são documentadas as respostas:

\`\`\`text

200 → consulta executada com sucesso

400 → parâmetro inválido ou obrigatório não informado

404 → integração ou endpoint não encontrado

500 → erro durante a execução da consulta

\`\`\`

Os endpoints dinâmicos podem ser executados diretamente pelo recurso **\*\*Try it out\*\*** do Swagger UI.

**---**

**# Tratamento de erros**

O backend possui tratamento centralizado de exceções através de:

\`\`\`text

GlobalExceptionHandler

\`\`\`

O objetivo é fornecer respostas HTTP consistentes para erros de validação, recursos não encontrados, conflitos, métodos HTTP não suportados, problemas de banco e falhas inesperadas.

Formato de erro:

\`\`\`json

{

  "timestamp": "2026-08-24T20:00:39.5506495",

  "status": 409,

  "error": "Conflict",

  "message": "A integração possui endpoints vinculados",

  "path": "/api/integrations/12"

}

\`\`\`

Entre os status tratados pela API estão:

\`\`\`text

400 Bad Request

404 Not Found

405 Method Not Allowed

409 Conflict

500 Internal Server Error

\`\`\`

**---**

**# Ambiente Oracle de desenvolvimento**

O projeto possui um ambiente Oracle local dedicado ao desenvolvimento.

A infraestrutura é executada em uma máquina virtual isolada, permitindo desenvolver e testar o Integration Hub sem depender de ambientes Oracle externos.

O ambiente utiliza:

\- VirtualBox

\- Oracle Linux

\- Oracle Database Free 23ai

\- Oracle Net Listener

\- rede em modo Bridge para comunicação entre host e VM

\- endereço IPv4 estático para a VM

O Listener utiliza a porta padrão:

\`\`\`text

1521

\`\`\`

A PDB utilizada pelo ambiente de desenvolvimento é:

\`\`\`text

freepdb1

\`\`\`

Informações específicas do ambiente, como endereço IP e credenciais, não devem ser documentadas no repositório.

**---**

**# Inicialização do ambiente Oracle**

O ambiente Oracle pode ser iniciado através do VirtualBox.

Após a inicialização da VM, deve-se aguardar a rede e o Oracle Listener estarem disponíveis antes de iniciar o backend.

Fluxo:

\`\`\`text

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

\`\`\`

O Oracle Database pode levar alguns segundos adicionais para ficar disponível após a rede da VM começar a responder.

**---**

**# Pool de conexões**

O backend utiliza HikariCP para gerenciamento das conexões com o Oracle.

A aplicação mantém um conjunto reutilizável de conexões, evitando a criação de uma nova conexão para cada requisição.

A configuração atual utiliza um pool reduzido, adequado ao ambiente de desenvolvimento.

O pool poderá ser ajustado posteriormente conforme o volume de requisições e a necessidade de escalabilidade da aplicação.

**---**

**# Configuração do banco**

A aplicação suporta configurações diferentes conforme o ambiente de execução.

**## Configuração padrão**

As informações de conexão podem ser fornecidas através das seguintes variáveis de ambiente:

\`\`\`text

DB\_URL

DB\_USERNAME

DB\_PASSWORD

\`\`\`

Exemplo Linux:

\`\`\`bash

export DB\_URL='jdbc\:oracle\:thin:@//HOST:1521/SERVICE'

export DB\_USERNAME='USUARIO'

export DB\_PASSWORD='SENHA'

\`\`\`

Exemplo PowerShell:

\`\`\`powershell

$env\:DB\_URL="jdbc\:oracle\:thin:@//HOST:1521/SERVICE"

$env\:DB\_USERNAME="USUARIO"

$env\:DB\_PASSWORD="SENHA"

\`\`\`

Nenhuma credencial real deve ser adicionada ao repositório.

**## Profile local**

Para desenvolvimento local pode ser utilizado:

\`\`\`text

application-local.yml

\`\`\`

Estrutura:

\`\`\`yaml

spring:

  datasource:

    url: jdbc\:oracle\:thin:@//HOST:1521/SERVICE

    username: USUARIO

    password: SENHA

\`\`\`

O profile deve ser ativado através de \`local\`.

O arquivo local permite executar o projeto sem precisar definir manualmente as variáveis de ambiente a cada nova sessão do terminal.

Credenciais reais e informações específicas da máquina de desenvolvimento não devem ser versionadas.

**---**

**# Executando o backend**

Primeiro, certifique-se de que o ambiente Oracle esteja disponível.

Depois entre no diretório:

\`\`\`bash

cd backend

\`\`\`

**## Desenvolvimento local**

Windows:

\`\`\`powershell

.\mvnw\.cmd spring-boot\:run "-Dspring-boot.run.profiles=local"

\`\`\`

O log deverá indicar que o profile \`local\` está ativo.

Linux ou Git Bash:

\`\`\`bash

./mvnw spring-boot\:run

\`\`\`

O backend utiliza a porta \`8081\`.

**---**

**# Executando o frontend**

Entre no diretório:

\`\`\`bash

cd frontend

\`\`\`

Instale as dependências, caso necessário:

\`\`\`bash

npm install

\`\`\`

Inicie o servidor de desenvolvimento:

\`\`\`bash

npm run dev

\`\`\`

O frontend utiliza a porta \`5175\`.

A aplicação estará disponível em:

\`\`\`text

http\://localhost:5175

\`\`\`

Para validar o frontend:

\`\`\`bash

npm run lint

npm run build

\`\`\`

**---**

**# Comunicação frontend e backend**

Durante o desenvolvimento local:

\`\`\`text

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

\`\`\`

Como frontend e backend utilizam portas diferentes, o navegador considera as aplicações origens distintas.

O backend possui configuração de CORS para permitir o acesso do frontend local às APIs necessárias.

A configuração fica centralizada em:

\`\`\`text

br.com.integrationhub.config.WebConfig

\`\`\`

A origem local autorizada é:

\`\`\`text

http\://localhost:5175

\`\`\`

Essa configuração é destinada ao ambiente de desenvolvimento e poderá ser externalizada por ambiente em etapas posteriores.

**---**

**# Frontend**

A interface administrativa está sendo construída de forma incremental.

A estrutura segue a separação:

\`\`\`text

App

├── Sidebar

└── Content

    ├── Header

    └── Page

\`\`\`

Os componentes visuais possuem seus respectivos arquivos CSS.

O \`index.css\` é reservado para estilos globais, normalizações e definições compartilhadas.

**## Tela de integrações**

A página de integrações está conectada ao backend.

Fluxo de consulta:

\`\`\`text

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

\`\`\`

A implementação atual contempla:

\- carregamento das integrações persistidas no Oracle;

\- apresentação do nome e \`basePath\`;

\- indicação visual de integração ativa ou inativa;

\- estado de carregamento;

\- estado vazio;

\- cadastro de novas integrações;

\- exclusão de integrações;

\- confirmação antes da exclusão;

\- apresentação de erros através de diálogo personalizado;

\- atualização automática da lista após operações bem-sucedidas.

**### Cadastro**

O botão **\*\*Nova integração\*\*** abre o \`IntegrationForm\`.

O formulário envia os dados através de:

\`\`\`http

POST /api/integrations

\`\`\`

Após o cadastro, a listagem é carregada novamente.

**### Exclusão**

Cada integração pode solicitar sua exclusão através da interface.

Antes da chamada ao backend, o \`ConfirmDialog\` solicita confirmação do usuário.

\`\`\`text

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

\`\`\`

A confirmação utiliza um componente próprio da aplicação em vez do \`window\.confirm\` do navegador.

**### MessageDialog**

Mensagens de erro relevantes são apresentadas através do componente reutilizável \`MessageDialog\`.

Por exemplo, se uma integração possuir endpoints vinculados, o backend retorna:

\`\`\`text

409 Conflict

\`\`\`

com a mensagem:

\`\`\`text

A integração possui endpoints vinculados

\`\`\`

O frontend apresenta essa mensagem em diálogo, mantendo a experiência visual consistente com a aplicação.

**## Tela de endpoints**

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
- apresentação de erros através do `MessageDialog`.

As bind variables são identificadas a partir do SQL. Por exemplo, uma consulta que utiliza `:id` e `:status` permite gerar esses parâmetros automaticamente no formulário, evitando configuração manual duplicada.

**---**

**# Health Check**

A aplicação disponibiliza um endpoint próprio para verificar tanto o funcionamento do backend quanto a conectividade com o Oracle.

\`\`\`http

GET /api/health

\`\`\`

Exemplo:

\`\`\`text

http\://localhost:8081/api/health

\`\`\`

Resposta esperada:

\`\`\`json

{

  "database": "Online",

  "status": "OK"

}

\`\`\`

**---**

**# API de Integrações**

A API administrativa de integrações possui atualmente operações de consulta, cadastro, atualização e exclusão.

**## Listar integrações**

\`\`\`http

GET /api/integrations

\`\`\`

**## Buscar integração**

\`\`\`http

GET /api/integrations/{id}

\`\`\`

**## Cadastrar integração**

\`\`\`http

POST /api/integrations

\`\`\`

Exemplo:

\`\`\`json

{

  "name": "Clientes",

  "description": "Integração para consulta de clientes",

  "basePath": "/api/clientes",

  "active": "S"

}

\`\`\`

**## Atualizar integração**

\`\`\`http

PUT /api/integrations/{id}

\`\`\`

A atualização preserva os dados de criação e registra as informações de atualização.

Exemplo:

\`\`\`json

{

  "name": "Itens do Pedido",

  "description": "Itens vinculados aos pedidos",

  "basePath": "/api/itens",

  "active": "S"

}

\`\`\`

**## Excluir integração**

\`\`\`http

DELETE /api/integrations/{id}

\`\`\`

A exclusão somente é permitida quando a integração não possui endpoints vinculados.

Quando existem endpoints associados, a operação retorna:

\`\`\`text

409 Conflict

\`\`\`

Exemplo de resposta:

\`\`\`json

{

  "status": 409,

  "error": "Conflict",

  "message": "A integração possui endpoints vinculados",

  "path": "/api/integrations/12"

}

\`\`\`

Essa proteção evita a remoção de uma integração que ainda possui configurações dependentes.

**---**

**# API de Endpoints**

A API administrativa de endpoints possui operações de consulta, cadastro, atualização e exclusão.

**## Listar endpoints**

\`\`\`http

GET /api/endpoints

\`\`\`

**## Buscar endpoint**

\`\`\`http

GET /api/endpoints/{id}

\`\`\`

**## Listar endpoints de uma integração**

\`\`\`http

GET /api/endpoints/integration/{integrationId}

\`\`\`

**## Cadastrar endpoint**

\`\`\`http

POST /api/endpoints

\`\`\`

Exemplo:

\`\`\`json

{

  "integrationId": 1,

  "name": "Buscar cliente",

  "description": "Consulta um cliente pelo identificador",

  "path": "/buscar",

  "method": "GET",

  "sqlText": "select id, nome from cliente where id = \:id",

  "parameters": [

    {

      "name": "id",

      "type": "NUMBER",

      "required": true

    }

  ],

  "active": "S"

}

\`\`\`

O \`POST\` é utilizado para configuração administrativa do Integration Hub.

Os endpoints dinamicamente disponibilizados para consumidores possuem inicialmente apenas operações \`GET\`.

**---**

**# Consultas parametrizadas**

As consultas configuradas nos endpoints devem utilizar bind parameters.

Exemplo:

\`\`\`sql

select id,

       nome

  from cliente

 where id = \:id

\`\`\`

O valor de \`id\` é recebido pela requisição HTTP e enviado ao banco separadamente da instrução SQL.

Valores recebidos pela API não devem ser concatenados diretamente ao SQL.

Esse modelo reduz riscos de SQL Injection e permite que o Oracle reutilize planos de execução com maior eficiência.

**---**

**# Testes automatizados**

O backend possui testes automatizados utilizando JUnit e Mockito.

Os testes cobrem componentes importantes da execução e das regras de negócio, incluindo:

\- validação de \`basePath\`;

\- valores padrão de \`active\` e \`createdBy\`;

\- persistência através dos repositories simulados;

\- execução dinâmica;

\- conversão e validação de parâmetros;

\- limite máximo de resultados;

\- comportamento dos controllers;

\- respostas para recursos inexistentes;

\- atualização de integrações;

\- exclusão de integrações;

\- bloqueio da exclusão quando existem endpoints vinculados.

O \`IntegrationServiceTest\` utiliza mocks de:

\`\`\`text

IntegrationRepository

EndpointRepository

\`\`\`

Isso permite testar as regras da camada de serviço sem necessidade de conexão real com o Oracle.

A proteção de exclusão deve garantir que o repository responsável pela remoção não seja chamado quando existirem endpoints associados à integração.

**---**

**# Validação do projeto**

**## Backend**

Linux:

\`\`\`bash

cd backend

./mvnw clean verify

\`\`\`

Windows:

\`\`\`powershell

cd backend

.\mvnw\.cmd clean verify

\`\`\`

**## Frontend**

\`\`\`bash

cd frontend

npm run lint

npm run build

\`\`\`

O projeto utiliza validações automatizadas para detectar problemas de compilação, testes e qualidade antes que alterações sejam incorporadas.

**---**

**# CI/CD**

O repositório possui workflow do GitHub Actions para validação automática do projeto.

O workflow é executado após alterações versionadas conforme os gatilhos configurados e valida o projeto antes da continuidade do desenvolvimento.

O processo contempla as validações configuradas para backend e frontend, incluindo testes, compilação, lint e build.

O objetivo é detectar automaticamente regressões ou problemas introduzidos por novos commits.

**---**

**# Escopo da V1**

A primeira versão do Integration Hub possui foco em:

\- cadastro e gerenciamento administrativo de integrações;

\- cadastro, edição e exclusão de endpoints;

\- persistência das configurações no Oracle;

\- relacionamento \`Integration 1\:N Endpoint\`;

\- endpoints de consumo do tipo \`GET\`;

\- consultas SQL parametrizadas;

\- validação dos parâmetros recebidos;

\- conexão com Oracle através de pool;

\- resolução dinâmica de \`basePath + path\`;

\- execução dinâmica das consultas;

\- retorno dos resultados em JSON;

\- documentação OpenAPI/Swagger;

\- tratamento padronizado de erros;

\- interface administrativa em React;

\- criação, edição e exclusão de integrações pelo frontend;

\- componentes de diálogo reutilizáveis;

\- ambiente Oracle local para desenvolvimento;

\- testes automatizados e validação por CI.

Funcionalidades adicionais serão incorporadas de maneira incremental após a estabilização desse fluxo.

**---**

**# Operações administrativas x endpoints dinâmicos**

As operações \`POST\`, \`PUT\` e \`DELETE\` existentes na API administrativa não significam que endpoints dinâmicos desses tipos já sejam suportados.

Por exemplo:

\`\`\`http

PUT /api/integrations/{id}

DELETE /api/integrations/{id}

\`\`\`

são operações de administração da configuração do Integration Hub.

A execução das APIs configuradas pelos usuários continua limitada a:

\`\`\`text

GET

\`\`\`

na V1.

Essa separação permite evoluir a interface administrativa sem ampliar prematuramente o escopo do mecanismo dinâmico.

**---**

**# Fora do escopo inicial**

Não fazem parte da primeira implementação dos endpoints dinâmicos:

\- operações dinâmicas \`POST\`;

\- operações dinâmicas \`PUT\`;

\- operações dinâmicas \`PATCH\`;

\- operações dinâmicas \`DELETE\`;

\- mensageria;

\- processamento assíncrono;

\- orquestração em Kubernetes;

\- recursos avançados de escalabilidade distribuída.

**---**

**# Segurança**

A execução de SQL configurável exige controles específicos.

Entre os princípios do projeto estão:

\- utilização obrigatória de bind parameters;

\- proibição de concatenação direta de parâmetros no SQL;

\- validação dos parâmetros antes da execução;

\- separação entre configuração e consumo das integrações;

\- controle de acesso;

\- armazenamento seguro das credenciais;

\- restrição dos tipos de SQL permitidos;

\- limite máximo de resultados;

\- auditoria das execuções em etapas futuras.

Na V1, o foco é em consultas de leitura.

**---**

**# Perfis de acesso previstos**

A plataforma deverá possuir dois perfis principais.

**## Criador**

Responsável pela configuração das integrações.

Poderá:

\- cadastrar integrações;

\- atualizar integrações;

\- excluir integrações quando permitido;

\- cadastrar endpoints;

\- definir consultas SQL;

\- definir parâmetros;

\- testar consultas;

\- visualizar documentação;

\- ativar ou desativar integrações.

**## Consumidor**

Responsável pelo consumo e validação das integrações disponibilizadas.

Poderá:

\- consultar integrações disponíveis;

\- visualizar documentação;

\- visualizar os parâmetros necessários;

\- testar endpoints autorizados;

\- consumir as APIs publicadas.

O mecanismo de autenticação e autorização será implementado em uma etapa posterior.

**---**

**# Estado atual**

Atualmente estão implementados e validados:

\- aplicação Spring Boot com Java 21;

\- backend executando na porta \`8081\`;

\- conexão JDBC com Oracle;

\- persistência das configurações no Oracle;

\- repositories Oracle para integrações e endpoints;

\- configuração de datasource por ambiente;

\- profile \`local\` para desenvolvimento;

\- HikariCP;

\- health check da aplicação e do Oracle;

\- cadastro e consulta de integrações;

\- atualização de integrações via API;

\- exclusão de integrações via API;

\- proteção contra exclusão de integração com endpoints vinculados;

\- resposta \`409 Conflict\` para conflitos de exclusão;

\- cadastro e consulta de endpoints;

\- consulta de endpoints por integração;

- atualização de endpoints via API;

- exclusão de endpoints via API;

\- relacionamento \`Integration 1\:N Endpoint\`;

\- parâmetros \`VARCHAR2\`, \`NUMBER\`, \`DATE\` e \`TIMESTAMP\`;

\- validação de parâmetros obrigatórios;

\- conversão dos parâmetros antes da execução;

\- resolução dinâmica de \`basePath + path\`;

\- seleção do \`basePath\` mais específico;

\- execução dinâmica das consultas SQL;

\- bind parameters;

\- limite configurável de resultados;

\- retorno das consultas em JSON;

\- tratamento centralizado de erros;

\- OpenAPI 3.1;

\- Swagger UI;

\- documentação automática dos endpoints dinâmicos;

\- execução dos endpoints pelo Swagger UI;

\- Maven Wrapper;

\- testes automatizados com JUnit e Mockito;

\- testes das regras administrativas de integrações e endpoints;

\- build com \`clean verify\`;

\- workflow de validação no GitHub Actions;

\- VM dedicada para Oracle;

\- Oracle Linux;

\- Oracle Database Free 23ai;

\- Oracle Net Listener;

\- frontend React 19 com Vite;

\- frontend executando na porta \`5175\`;

\- estrutura do frontend separada em \`components\`, \`pages\` e \`services\`;

\- layout principal com Sidebar e Header;

\- página de integrações;

\- \`IntegrationForm\`;

\- \`IntegrationList\`;

\- cadastro de integrações pelo frontend;

\- edição de integrações pelo frontend;

\- exclusão de integrações pelo frontend;

- cadastro de endpoints pelo frontend;

- edição de endpoints pelo frontend;

- exclusão de endpoints pelo frontend;

- geração automática de parâmetros a partir das bind variables do SQL;

- configuração de tipo e obrigatoriedade dos parâmetros;

- validação de sincronização entre SQL e parâmetros antes do salvamento;

\- \`ConfirmDialog\` para confirmação de ações destrutivas;

\- \`MessageDialog\` para mensagens e erros;

\- comunicação frontend → backend através de \`integrationService.js\`;

\- listagem das integrações persistidas no Oracle;

\- estados de loading e lista vazia;

\- tratamento visual de erros;

\- configuração de CORS para o ambiente local.

**---**

**# Próximas etapas**

A sequência imediata prevista é:

1\. evoluir a página de endpoints e sua integração com o backend;

2\. implementar as operações administrativas restantes de endpoints conforme a necessidade;

3\. ampliar os testes automatizados conforme novos fluxos forem adicionados;

4\. evoluir os componentes comuns somente quando surgirem necessidades reais;

5\. revisar validações e experiência de uso dos formulários;

6\. implementar autenticação e controle de acesso em etapa posterior.

**---**

**# Status da V1**

O núcleo da V1 já permite configurar integrações e endpoints no Oracle e disponibilizar consultas \`GET\` dinamicamente sem a necessidade de criar um controller Java específico para cada nova consulta.

O projeto também já possui uma interface administrativa React integrada ao backend, com criação, edição e exclusão de integrações e endpoints, geração automática de parâmetros a partir do SQL, além da infraestrutura de testes, documentação OpenAPI e validação automática por GitHub Actions.

O CRUD administrativo de integrações e endpoints está funcional. O desenvolvimento continua de forma incremental, priorizando validações, testes, experiência de uso e estabilização da V1 antes da expansão dos tipos de endpoints dinâmicos.