# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e posteriormente disponibilizadas como endpoints HTTP de forma segura, documentada e controlada.

O desenvolvimento está sendo realizado de forma incremental. A primeira versão terá foco em integrações de leitura utilizando `GET`, permitindo validar a arquitetura, o modelo de domínio e a execução das consultas antes da expansão para outros métodos HTTP.

---

## Arquitetura

O projeto utiliza uma arquitetura inicialmente dividida entre backend e frontend.

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
│   │   │   │       ├── service/
│   │   │   │       │   └── DatabaseHealthService.java
│   │   │   │       │
│   │   │   │       └── integration/
│   │   │   │           ├── UsuarioController.java
│   │   │   │           ├── UsuarioService.java
│   │   │   │           │
│   │   │   │           ├── controller/
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
│   │   │   │               └── EndpointService.java
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
id:           1
name:         Clientes
description:  Integração para consulta de clientes
basePath:     /api/clientes
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
id:             1
integrationId:  1
name:           Buscar cliente
description:    Consulta um cliente pelo identificador
path:           /buscar
method:         GET
active:         S
createdBy:      SYSTEM
```

O campo `integrationId` identifica a `Integration` à qual o endpoint pertence.

Na V1, apenas endpoints com método:

```text
GET
```

serão permitidos.

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
  "name": "id",
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
    "name": "situacao",
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

# Composição dos endpoints

O endereço final de uma integração será formado pela combinação do `basePath` da integração com o `path` do endpoint.

Exemplo:

```text
Integration.basePath
/api/clientes

Endpoint.path
/buscar
```

Resultado:

```text
/api/clientes/buscar
```

Uma mesma integração pode possuir diversos endpoints:

```text
/api/clientes
    │
    ├── /buscar
    ├── /listar
    └── /detalhes
```

Resultando em:

```text
/api/clientes/buscar
/api/clientes/listar
/api/clientes/detalhes
```

A resolução dinâmica dessas rotas será implementada na próxima etapa do projeto.

---

# Persistência

As configurações de `Integration` e `Endpoint` já são persistidas no Oracle.

O fluxo atual é:

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

Não existem mais repositories em memória para `Integration` ou `Endpoint`.

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

Exemplo:

```text
Integration 1
GET /buscar
```

não pode ser cadastrado duas vezes.

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

O objetivo é manter identificadores únicos, crescentes e previsíveis.

Sequences Oracle não garantem ausência absoluta de intervalos entre identificadores. Um número pode ser consumido sem resultar em registro persistido, por exemplo, quando uma operação é revertida.

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

No Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

O log deverá indicar:

```text
The following 1 profile is active: "local"
```

## Utilizando variáveis de ambiente

Linux ou Git Bash:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

O backend utiliza a porta:

```text
8081
```

A aplicação estará disponível em:

```text
http://localhost:8081
```

---

# Health Check

A aplicação disponibiliza um endpoint próprio para verificar tanto o funcionamento do backend quanto a conectividade com o Oracle.

```http
GET /api/health
```

Resposta esperada:

```json
{
  "database": "Online",
  "status": "OK"
}
```

---

# API de Integrações

As operações de Integration utilizam persistência Oracle.

## Listar integrações

```http
GET /api/integrations
```

Os registros são recuperados diretamente da tabela:

```text
IH_INTEGRATION
```

## Buscar integração

```http
GET /api/integrations/{id}
```

Exemplo:

```http
GET /api/integrations/1
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

Não é necessário informar:

```text
id
createdBy
createdAt
updatedBy
updatedAt
```

O identificador é obtido através da `IH_INTEGRATION_SEQ`.

Enquanto não houver autenticação, `createdBy` utiliza:

```text
SYSTEM
```

---

# API de Endpoints

As operações de Endpoint também utilizam persistência Oracle.

## Listar endpoints

```http
GET /api/endpoints
```

Os registros são recuperados diretamente da tabela:

```text
IH_ENDPOINT
```

## Buscar endpoint

```http
GET /api/endpoints/{id}
```

Exemplo:

```http
GET /api/endpoints/2
```

## Listar endpoints de uma integração

```http
GET /api/endpoints/integration/{integrationId}
```

Exemplo:

```http
GET /api/endpoints/integration/1
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

Não é necessário informar:

```text
id
createdBy
createdAt
updatedBy
updatedAt
```

O identificador é obtido através da `IH_ENDPOINT_SEQ`.

Os parâmetros são serializados em JSON antes de serem armazenados no Oracle.

Ao consultar um endpoint, o JSON armazenado é desserializado novamente para objetos `EndpointParameter`.

---

# Consultas parametrizadas

As consultas configuradas nos endpoints devem utilizar **bind parameters**.

Exemplo:

```sql
select id,
       nome
from cliente
where id = :id
```

O valor de `id` será recebido pela requisição HTTP e enviado ao banco separadamente da instrução SQL.

Valores recebidos pela API não devem ser concatenados diretamente ao SQL.

Esse modelo reduz riscos de SQL Injection e permite que o Oracle reutilize planos de execução com maior eficiência.

---

# Fluxo previsto de execução dinâmica

Com a persistência das configurações concluída, a próxima etapa é utilizar essas configurações para executar endpoints dinamicamente.

O fluxo previsto é:

```text
Cliente HTTP
    │
    ▼
GET /api/clientes/buscar?id=123
    │
    ▼
Resolver Integration pelo basePath
    │
    ▼
Resolver Endpoint pelo path
    │
    ▼
Validar Integration ativa
    │
    ▼
Validar Endpoint ativo
    │
    ▼
Validar método HTTP
    │
    ▼
Validar parâmetros
    │
    ▼
Executar SQL_TEXT
    │
    ▼
Oracle Database
    │
    ▼
Resultado
    │
    ▼
JSON
```

Exemplo de configuração:

```text
Integration.basePath = /api/clientes
Endpoint.path        = /buscar
Endpoint.method      = GET
```

A rota dinâmica será:

```http
GET /api/clientes/buscar?id=123
```

O Integration Hub deverá localizar a configuração correspondente, validar os parâmetros, executar o SQL configurado e retornar o resultado em JSON.

---

# Validação do projeto

Para executar compilação e testes:

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

O projeto possui testes automatizados para validar componentes do backend.

O mesmo processo de build e testes é executado pelo pipeline do GitHub Actions.

---

# CI/CD

O repositório possui um workflow do **GitHub Actions** responsável pela validação automática do backend.

O fluxo executa:

```text
checkout
    │
    ▼
configuração do Java
    │
    ▼
Maven
    │
    ▼
clean verify
    │
    ▼
compilação + testes
```

Isso permite detectar problemas de compilação e regressões antes que novas alterações sejam incorporadas ao projeto.

---

# Escopo da V1

A primeira versão do Integration Hub terá foco em:

- cadastro de integrações;
- cadastro de endpoints;
- relacionamento `Integration 1:N Endpoint`;
- persistência Oracle das configurações;
- endpoints de consumo do tipo `GET`;
- consultas SQL parametrizadas;
- validação dos parâmetros recebidos;
- conexão com Oracle através de pool;
- execução dinâmica das consultas;
- retorno dos resultados em JSON;
- documentação da API;
- ambiente Oracle local para desenvolvimento.

---

# Fora do escopo inicial

Não fazem parte da primeira implementação:

- operações dinâmicas `POST`;
- operações dinâmicas `PUT`;
- operações dinâmicas `PATCH`;
- operações dinâmicas `DELETE`;
- mensageria;
- processamento assíncrono;
- orquestração em Kubernetes;
- persistência das configurações fora do Oracle;
- recursos avançados de escalabilidade distribuída.

Essas funcionalidades poderão ser avaliadas conforme o crescimento e as necessidades reais da plataforma.

---

# Segurança

A execução de SQL configurável exige controles específicos.

Entre os princípios previstos para o projeto estão:

- utilização obrigatória de bind parameters;
- proibição de concatenação direta de parâmetros no SQL;
- validação dos parâmetros antes da execução;
- separação entre configuração e consumo das integrações;
- controle de acesso;
- armazenamento seguro das credenciais;
- restrição dos tipos de SQL permitidos;
- auditoria das execuções em etapas futuras.

Na V1, o foco será em consultas de leitura.

---

# Perfis de acesso previstos

A plataforma deverá possuir dois perfis principais.

## Criador

Responsável pela configuração das integrações.

Poderá:

- cadastrar integrações;
- cadastrar endpoints;
- definir consultas SQL;
- definir parâmetros;
- testar consultas;
- visualizar documentação;
- ativar ou desativar integrações.

## Consumidor

Responsável pelo consumo e validação das integrações disponibilizadas.

Poderá:

- consultar integrações disponíveis;
- visualizar documentação;
- visualizar os parâmetros necessários;
- testar endpoints autorizados;
- consumir as APIs publicadas.

O mecanismo de autenticação e autorização será implementado em uma etapa posterior.

---

# Documentação da API

Está prevista a adoção de:

```text
OpenAPI
Swagger UI
```

A documentação deverá permitir visualizar:

- integrações disponíveis;
- endpoints;
- métodos HTTP;
- parâmetros;
- exemplos de requisição;
- exemplos de resposta;
- testes diretamente pela interface.

---

# Estado atual

Atualmente estão implementados e validados:

- aplicação Spring Boot com Java 21;
- backend executando na porta `8081`;
- conexão JDBC com Oracle;
- configuração de datasource por variáveis de ambiente;
- profile `local` para desenvolvimento;
- HikariCP;
- health check da aplicação;
- health check do Oracle;
- tabela `IH_INTEGRATION`;
- sequence `IH_INTEGRATION_SEQ`;
- tabela `IH_ENDPOINT`;
- sequence `IH_ENDPOINT_SEQ`;
- relacionamento `IH_INTEGRATION 1:N IH_ENDPOINT`;
- foreign key entre Endpoint e Integration;
- constraints de integridade;
- scripts de instalação `001` e `002`;
- persistência Oracle das integrações;
- persistência Oracle dos endpoints;
- `OracleIntegrationRepository`;
- `OracleEndpointRepository`;
- cadastro de integrações via API;
- listagem de integrações;
- busca de integração por ID;
- busca de integração por `basePath`;
- cadastro de endpoints via API;
- listagem de endpoints;
- busca de endpoint por ID;
- listagem de endpoints por Integration;
- campos de auditoria;
- model `EndpointParameter`;
- parâmetros estruturados por nome, tipo e obrigatoriedade;
- armazenamento dos parâmetros como JSON em `CLOB`;
- serialização e desserialização dos parâmetros;
- SQL dos endpoints armazenado em `CLOB`;
- Maven Wrapper;
- testes automatizados;
- build com `clean verify`;
- workflow de validação no GitHub Actions;
- VM dedicada para Oracle;
- Oracle Linux configurado;
- Oracle Database Free 23ai instalado;
- Oracle Net Listener configurado;
- inicialização automática dos serviços Oracle;
- endereço IPv4 estático para a VM;
- comunicação entre máquina de desenvolvimento e VM;
- conexão ponta a ponta entre Spring Boot e Oracle validada.

A camada de configuração e persistência das integrações e endpoints está concluída para esta etapa.

A execução dinâmica dos endpoints cadastrados ainda será implementada.

---

# Próximas etapas

A sequência prevista de desenvolvimento passa a ser:

1. implementar resolução dinâmica de `basePath + path`;
2. localizar a Integration correspondente à requisição;
3. localizar o Endpoint correspondente à rota;
4. validar se Integration e Endpoint estão ativos;
5. validar o método HTTP configurado;
6. validar parâmetros obrigatórios;
7. converter os parâmetros conforme o tipo configurado;
8. executar `SQL_TEXT` utilizando bind parameters;
9. retornar o resultado da consulta em JSON;
10. adicionar tratamento padronizado de erros;
11. adicionar validações de segurança para o SQL configurado;
12. adicionar Swagger/OpenAPI;
13. ampliar os testes automatizados;
14. implementar autenticação e controle de acesso;
15. iniciar o frontend em React.

---

# Princípio de desenvolvimento

O Integration Hub está sendo desenvolvido de forma incremental.

A prioridade é manter uma arquitetura simples enquanto os requisitos fundamentais são validados.

Novas tecnologias e componentes serão adicionados apenas quando houver uma necessidade concreta de arquitetura, desempenho, segurança ou escalabilidade.

A infraestrutura de configuração já permite:

```text
Cadastrar Integration
        │
        ▼
Persistir em IH_INTEGRATION
        │
        ▼
Cadastrar Endpoint
        │
        ▼
Persistir em IH_ENDPOINT
        │
        ▼
Armazenar SQL e parâmetros
```

O próximo marco da V1 será completar:

```text
Requisição HTTP dinâmica
        │
        ▼
Resolver Integration + Endpoint
        │
        ▼
Validar parâmetros
        │
        ▼
Executar SQL_TEXT
        │
        ▼
Oracle Database
        │
        ▼
Retornar JSON
```