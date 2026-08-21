# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e posteriormente disponibilizadas como endpoints HTTP de forma segura, documentada e controlada.

O projeto está sendo desenvolvido de forma incremental, começando por integrações do tipo **GET** e evoluindo posteriormente para outros métodos HTTP.

---

## Arquitetura

O projeto utiliza:

### Backend

* Java 21
* Spring Boot 4.0.7
* Spring Web
* Spring JDBC
* HikariCP
* Oracle Database
* Maven

### Frontend

* React

O frontend será desenvolvido em uma etapa posterior.

---

## Estrutura do projeto

```text
integration-hub/
├── .github/
│   └── workflows/
│
├── backend/
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
│   │   │   │           │   ├── InMemoryIntegrationRepository.java
│   │   │   │           │   ├── EndpointRepository.java
│   │   │   │           │   └── InMemoryEndpointRepository.java
│   │   │   │           │
│   │   │   │           └── service/
│   │   │   │               ├── IntegrationService.java
│   │   │   │               └── EndpointService.java
│   │   │   │
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   │
│   │   └── test/
│   │
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
└── README.md
```

---

## Modelo de integração

O Integration Hub separa uma integração em dois níveis:

```text
Integration
    │
    │ 1:N
    ▼
Endpoint
```

### Integration

Representa um agrupamento de endpoints relacionados.

Principais propriedades:

```text
id
name
description
basePath
active
```

Exemplo:

```text
id:          1
name:        Ordem de Compra
basePath:    /api/ordemCompra
active:      true
```

### Endpoint

Representa uma operação pertencente a uma integração.

Principais propriedades:

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
name:           Buscar Ordem
path:           /getOrdem
method:         GET
active:         true
```

---

## Composição dos endpoints

O endereço final de uma integração será formado pela combinação do `basePath` da integração com o `path` do endpoint.

Exemplo:

```text
Integration.basePath
/api/ordemCompra

Endpoint.path
/getOrdem
```

Resultado:

```text
/api/ordemCompra/getOrdem
```

Uma mesma integração poderá possuir diversos endpoints:

```text
/api/ordemCompra
        │
        ├── /getOrdem
        │
        └── /atualizaOrdem
```

Resultando em:

```text
/api/ordemCompra/getOrdem
/api/ordemCompra/atualizaOrdem
```

---

## Persistência atual

Nesta fase do desenvolvimento, os cadastros de integrações e endpoints são mantidos **em memória** pela aplicação.

São utilizados:

```text
InMemoryIntegrationRepository
InMemoryEndpointRepository
```

Os identificadores são gerados em memória e os dados são perdidos sempre que a aplicação é reiniciada.

Essa implementação é temporária e permite desenvolver e validar o domínio da aplicação antes da criação das tabelas definitivas.

---

## Persistência Oracle

A persistência definitiva será realizada em Oracle Database.

Todas as tabelas pertencentes ao Integration Hub utilizarão o prefixo:

```text
IH_
```

As primeiras tabelas previstas são:

```text
IH_INTEGRATION
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

O ambiente de desenvolvimento utilizará inicialmente uma instância Oracle local isolada do ambiente externo.

---

## Pool de conexões

O backend utiliza o pool de conexões padrão do Spring Boot:

```text
HikariCP
```

A aplicação mantém um pool reutilizável para acesso ao Oracle, evitando a criação de uma nova conexão para cada requisição.

---

## Configuração do banco

As informações sensíveis de conexão não são armazenadas diretamente no repositório.

A configuração utiliza variáveis de ambiente.

Exemplo:

```bash
export DB_URL='jdbc:oracle:thin:@//HOST:1521/SERVICE'
export DB_USERNAME='USUARIO'
export DB_PASSWORD='SENHA'
```

O `application.properties` referencia essas variáveis.

---

## Executando o backend

Entre na pasta:

```bash
cd backend
```

Configure as variáveis de ambiente necessárias para conexão com o banco e execute:

```bash
./mvnw spring-boot:run
```

O backend estará disponível em:

```text
http://localhost:8081
```

---

## Health Check

A aplicação disponibiliza um endpoint próprio para verificar o funcionamento da API e a conectividade com o banco.

```http
GET /api/health
```

Exemplo:

```text
http://localhost:8081/api/health
```

---

## API de Integrações

### Listar integrações

```http
GET /api/integrations
```

### Buscar integração

```http
GET /api/integrations/{id}
```

Exemplo:

```text
GET /api/integrations/1
```

### Cadastrar integração

```http
POST /api/integrations
```

Exemplo de body:

```json
{
  "name": "Ordem de Compra",
  "description": "Integração de ordens de compra",
  "basePath": "/api/ordemCompra",
  "active": true
}
```

O identificador é gerado automaticamente pelo repositório em memória.

---

## API de Endpoints

### Listar endpoints

```http
GET /api/endpoints
```

### Buscar endpoint

```http
GET /api/endpoints/{id}
```

Exemplo:

```text
GET /api/endpoints/1
```

### Listar endpoints de uma integração

```http
GET /api/endpoints/integration/{integrationId}
```

Exemplo:

```text
GET /api/endpoints/integration/1
```

### Cadastrar endpoint

```http
POST /api/endpoints
```

Exemplo de body:

```json
{
  "integrationId": 1,
  "name": "Buscar Ordem",
  "description": "Busca uma ordem de compra",
  "path": "/getOrdem",
  "method": "GET",
  "sql": "select * from ordem_compra where nr_ordem = :nr_ordem",
  "parameters": [
    "nr_ordem"
  ],
  "active": true
}
```

---

## Validação do projeto

Para executar a compilação e os testes:

```bash
cd backend
./mvnw clean verify
```

O mesmo processo é utilizado pelo workflow de validação do projeto no GitHub Actions.

---

## Segurança das consultas

Os endpoints configuráveis deverão utilizar parâmetros SQL através de **bind parameters**.

Exemplo:

```sql
select *
from ordem_compra
where nr_ordem = :nr_ordem
```

Valores recebidos pela API não deverão ser concatenados diretamente ao SQL.

---

## Escopo inicial

A primeira versão do Integration Hub terá foco em:

* cadastro de integrações;
* cadastro de endpoints;
* relacionamento entre integrações e endpoints;
* endpoints do tipo GET;
* consultas parametrizadas;
* conexão Oracle através de pool;
* documentação da API;
* execução das integrações através de endpoints HTTP.

Outros métodos HTTP serão incorporados posteriormente.

---

## Próximas etapas

As próximas etapas previstas são:

1. configurar o Oracle Database local para desenvolvimento;
2. criar o schema de desenvolvimento;
3. criar `IH_INTEGRATION`;
4. criar `IH_ENDPOINT`;
5. substituir os repositórios em memória por persistência Oracle;
6. implementar a execução dinâmica dos endpoints cadastrados;
7. validar parâmetros antes da execução;
8. retornar os resultados das consultas em JSON;
9. adicionar documentação via Swagger/OpenAPI;
10. implementar autenticação e controle de acesso;
11. iniciar o frontend em React.

---

## Status atual

Atualmente estão funcionando:

* aplicação Spring Boot na porta `8081`;
* conexão com Oracle;
* health check da aplicação e banco;
* pool de conexões;
* cadastro de integrações em memória;
* geração automática de IDs em memória;
* consulta de integrações;
* cadastro de endpoints em memória;
* consulta de endpoints;
* consulta de endpoints por integração;
* relacionamento `Integration 1:N Endpoint`;
* build e validação via Maven;
* workflow de validação no GitHub Actions.

A execução dinâmica do SQL configurado nos endpoints ainda será implementada.
