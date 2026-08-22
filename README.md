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
│   │       └── 001_create_ih_integration.sql
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
│   │   │   │           │   └── Endpoint.java
│   │   │   │           │
│   │   │   │           ├── repository/
│   │   │   │           │   ├── IntegrationRepository.java
│   │   │   │           │   ├── OracleIntegrationRepository.java
│   │   │   │           │   ├── EndpointRepository.java
│   │   │   │           │   └── InMemoryEndpointRepository.java
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

Quando autenticação e controle de acesso forem implementados, esses campos poderão receber o usuário autenticado.

---

## Endpoint

Representa uma operação pertencente a uma integração.

Principais propriedades previstas:

```text
id
integrationId
name
description
path
method
sql
parameters
active
```

Exemplo:

```text
id:             1
integrationId:  1
name:           Buscar cliente
path:           /buscar
method:         GET
active:         S
```

A persistência dos endpoints ainda está em memória nesta etapa do projeto.

---

# Composição dos endpoints

O endereço final de uma integração é formado pela combinação do `basePath` da integração com o `path` do endpoint.

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

Uma mesma integração poderá possuir diversos endpoints:

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

Na V1, apenas operações do tipo `GET` serão disponibilizadas para execução.

---

# Persistência

A persistência está sendo migrada de forma incremental para Oracle.

Atualmente:

```text
Integration
    │
    ▼
OracleIntegrationRepository
    │
    ▼
IH_INTEGRATION
    │
    ▼
Oracle Database
```

As integrações já são persistidas definitivamente no Oracle.

Os endpoints ainda utilizam:

```text
InMemoryEndpointRepository
```

Portanto, os endpoints cadastrados ainda são perdidos quando a aplicação é reiniciada.

Essa implementação temporária será substituída pela persistência Oracle na próxima etapa do desenvolvimento.

---

# Persistência Oracle

Todas as tabelas pertencentes ao Integration Hub utilizam o prefixo:

```text
IH_
```

A primeira tabela implementada é:

```text
IH_INTEGRATION
```

A próxima tabela prevista é:

```text
IH_ENDPOINT
```

O relacionamento será:

```text
IH_INTEGRATION
      │
      │ 1:N
      ▼
IH_ENDPOINT
```

---

# IH_INTEGRATION

A tabela `IH_INTEGRATION` armazena as integrações configuradas na plataforma.

Estrutura atual:

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

# Geração de identificadores

Os identificadores da `IH_INTEGRATION` são gerados através da sequence:

```text
IH_INTEGRATION_SEQ
```

Configuração:

```sql
create sequence ih_integration_seq
    start with 1
    increment by 1
    nocache
    nocycle;
```

O objetivo é manter identificadores crescentes e previsíveis.

Sequences Oracle garantem geração crescente e única, mas não garantem ausência absoluta de intervalos entre os números.

Um número pode ser consumido sem resultar em registro persistido, por exemplo, caso uma transação seja revertida.

---

# Scripts de banco

Os objetos próprios do Integration Hub devem possuir scripts de instalação versionados junto ao projeto.

Estrutura:

```text
backend/
└── database/
    └── install/
        └── 001_create_ih_integration.sql
```

O primeiro script é responsável pela criação de:

```text
IH_INTEGRATION
IH_INTEGRATION_SEQ
```

Novos objetos serão adicionados em scripts numerados para manter uma ordem explícita de instalação.

Exemplo previsto:

```text
001_create_ih_integration.sql
002_create_ih_endpoint.sql
```

Essa abordagem permite reproduzir a estrutura necessária em um novo ambiente Oracle.

---

# Ambiente Oracle de desenvolvimento

O projeto possui um ambiente Oracle local dedicado ao desenvolvimento.

A infraestrutura é executada em uma máquina virtual isolada, permitindo desenvolver e testar o Integration Hub sem depender de ambientes Oracle externos.

O ambiente utiliza:

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

Por esse motivo, o script aguarda a porta `1521` estar acessível antes de considerar o ambiente pronto.

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

O endpoint permite identificar separadamente problemas na aplicação e indisponibilidade do banco.

---

# API de Integrações

As operações de Integration já utilizam persistência Oracle.

## Listar integrações

```http
GET /api/integrations
```

Os registros são recuperados diretamente da tabela:

```text
IH_INTEGRATION
```

---

## Buscar integração

```http
GET /api/integrations/{id}
```

Exemplo:

```http
GET /api/integrations/1
```

---

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

A data de criação é preenchida automaticamente.

Exemplo de resposta:

```json
{
  "id": 1,
  "name": "Clientes",
  "description": "Integração para consulta de clientes",
  "basePath": "/api/clientes",
  "active": "S",
  "createdBy": "SYSTEM",
  "createdAt": "2026-08-22T16:21:45",
  "updatedBy": null,
  "updatedAt": null
}
```

---

# API de Endpoints

A persistência dos endpoints ainda é realizada em memória.

## Listar endpoints

```http
GET /api/endpoints
```

---

## Buscar endpoint

```http
GET /api/endpoints/{id}
```

---

## Listar endpoints de uma integração

```http
GET /api/endpoints/integration/{integrationId}
```

---

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
  "sql": "select id, nome from cliente where id = :id",
  "parameters": [
    "id"
  ],
  "active": true
}
```

Essa estrutura ainda será revisada durante a implementação da tabela `IH_ENDPOINT`.

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

# Fluxo previsto de execução

O fluxo principal da plataforma será:

```text
Cliente HTTP
    │
    ▼
Endpoint dinâmico
    │
    ▼
Identificação da Integration
    │
    ▼
Identificação do Endpoint
    │
    ▼
Validação dos parâmetros
    │
    ▼
Execução SQL
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

Um endpoint cadastrado como:

```text
Integration.basePath = /api/clientes
Endpoint.path        = /buscar
```

poderá futuramente ser consumido como:

```http
GET /api/clientes/buscar?id=123
```

O Integration Hub localizará a configuração correspondente, validará os parâmetros, executará o SQL e retornará o resultado em JSON.

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

Funcionalidades adicionais serão incorporadas de maneira incremental após a estabilização desse fluxo.

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
- configuração local através de `application-local.yml`;
- HikariCP;
- health check da aplicação;
- health check do Oracle;
- tabela `IH_INTEGRATION`;
- sequence `IH_INTEGRATION_SEQ`;
- script de instalação da `IH_INTEGRATION`;
- persistência Oracle das integrações;
- geração de IDs através de sequence;
- cadastro de integrações via API;
- listagem de integrações via API;
- busca de integração por ID;
- busca de integração por `basePath`;
- campos de auditoria da Integration;
- `OracleIntegrationRepository`;
- cadastro de endpoints em memória;
- consulta de endpoints em memória;
- consulta de endpoints por integração;
- relacionamento lógico `Integration 1:N Endpoint`;
- estrutura de controller, service e repository;
- testes automatizados do backend;
- Maven Wrapper;
- build com `clean verify`;
- workflow de validação no GitHub Actions;
- VM dedicada para Oracle;
- Oracle Linux configurado;
- Oracle Database Free 23ai instalado;
- Oracle Net Listener configurado;
- inicialização automática dos serviços Oracle;
- endereço IPv4 estático para a VM;
- comunicação entre a máquina de desenvolvimento e a VM;
- conexão ponta a ponta entre Spring Boot e Oracle validada.

A persistência de `Integration` já está concluída no Oracle.

A persistência de `Endpoint` ainda permanece em memória.

A execução dinâmica dos SQLs cadastrados ainda será implementada.

---

# Próximas etapas

A sequência prevista de desenvolvimento é:

1. definir o modelo definitivo de `IH_ENDPOINT`;
2. criar `002_create_ih_endpoint.sql`;
3. criar a tabela `IH_ENDPOINT`;
4. definir a FK entre `IH_ENDPOINT` e `IH_INTEGRATION`;
5. definir constraints e índices da `IH_ENDPOINT`;
6. criar geração de identificadores para Endpoint;
7. implementar `OracleEndpointRepository`;
8. remover `InMemoryEndpointRepository`;
9. validar cadastro e consulta de endpoints no Oracle;
10. implementar resolução dinâmica de `basePath + path`;
11. implementar validação dos parâmetros;
12. implementar execução dinâmica das consultas SQL;
13. retornar os resultados em JSON;
14. adicionar tratamento padronizado de erros;
15. adicionar Swagger/OpenAPI;
16. implementar autenticação e controle de acesso;
17. iniciar o frontend em React.

---

# Princípio de desenvolvimento

O Integration Hub está sendo desenvolvido de forma incremental.

A prioridade é manter uma arquitetura simples enquanto os requisitos fundamentais são validados.

Novas tecnologias e componentes serão adicionados apenas quando houver uma necessidade concreta de arquitetura, desempenho, segurança ou escalabilidade.

O objetivo da V1 é estabelecer um fluxo funcional e confiável:

```text
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