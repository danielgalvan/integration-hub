# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e disponibilizadas como endpoints HTTP de forma segura, documentada e controlada.

A proposta é reduzir a necessidade de desenvolver uma nova aplicação ou serviço para cada integração, centralizando esse processo em uma única plataforma.

## Objetivo

Em ambientes com múltiplas integrações, é comum existirem consultas específicas que precisam ser disponibilizadas para outros sistemas através de APIs.

O Integration Hub fornece uma camada intermediária entre os sistemas consumidores e o banco de dados:

```text
Sistema consumidor
        │
        ▼
┌───────────────────┐
│  Integration Hub  │
│     REST APIs     │
└─────────┬─────────┘
          │
          ▼
     ┌──────────┐
     │  Oracle  │
     └──────────┘
```

A plataforma permitirá cadastrar e gerenciar endpoints sem a necessidade de implementar individualmente toda a estrutura de uma nova API.

## Funcionalidades previstas

Cada integração poderá possuir configurações como:

- Nome e descrição
- Rota do endpoint
- Método HTTP
- Consulta SQL
- Parâmetros de entrada
- Controle de acesso
- Status do endpoint
- Documentação
- Histórico de execução

A partir dessas configurações, o Integration Hub será responsável por receber a requisição, validar os parâmetros, executar a consulta e retornar o resultado através de uma API REST.

## Arquitetura

A arquitetura inicial será composta por:

- **Frontend:** React
- **Backend:** Java + Spring Boot
- **Banco de dados:** Oracle
- **Acesso ao banco:** Spring JDBC
- **Pool de conexões:** HikariCP
- **Documentação:** OpenAPI / Swagger
- **Segurança:** Spring Security
- **Build:** Maven

```text
                 ┌─────────────────────┐
                 │        React        │
                 │    Administração    │
                 └──────────┬──────────┘
                            │
                            │ REST
                            ▼
                 ┌─────────────────────┐
                 │     Spring Boot     │
                 │   Integration Hub   │
                 └──────────┬──────────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
     Gerenciamento de              Execução dos
       integrações                   endpoints
              │                           │
              └─────────────┬─────────────┘
                            │
                            ▼
                       ┌──────────┐
                       │  Oracle  │
                       └──────────┘
```

## Perfis de acesso

Inicialmente, a plataforma contará com dois perfis principais de acesso.

### Criador

Responsável pela criação e manutenção das integrações.

Entre suas permissões estarão:

- Criar endpoints
- Alterar endpoints
- Definir consultas SQL
- Configurar parâmetros
- Definir permissões
- Publicar e desativar endpoints
- Testar endpoints
- Consultar documentação
- Acompanhar execuções

### Consumidor

Responsável pela utilização das APIs disponibilizadas.

Entre suas permissões estarão:

- Visualizar endpoints disponíveis
- Consultar documentação
- Visualizar parâmetros
- Testar endpoints autorizados
- Consumir APIs

## Segurança

O Integration Hub não será um executor genérico de SQL exposto aos consumidores.

As consultas serão previamente cadastradas e controladas pela plataforma. Os consumidores terão acesso somente aos endpoints para os quais possuírem autorização.

Entre os mecanismos previstos estão:

- Autenticação
- Autorização por endpoint
- Consultas parametrizadas
- Validação de parâmetros
- Proteção contra SQL Injection
- Controle das operações SQL permitidas
- Auditoria das chamadas
- Registro de erros
- Timeout de execução
- Limitação de resultados
- Controle de endpoints ativos e inativos

As credenciais de acesso ao banco de dados não devem ser armazenadas diretamente no código-fonte ou versionadas no repositório.

## Documentação das APIs

Os endpoints disponibilizados pela plataforma deverão possuir documentação através de **OpenAPI / Swagger**.

A documentação permitirá consultar:

- Rotas disponíveis
- Métodos HTTP
- Parâmetros de entrada
- Tipos de dados
- Exemplos de requisição
- Exemplos de resposta
- Códigos HTTP
- Descrição do endpoint

Além da documentação, usuários autorizados poderão testar os endpoints diretamente pela interface.

## Escalabilidade

O projeto será iniciado com um número reduzido de integrações, mas sua arquitetura deverá permitir crescimento progressivo.

A criação de novos endpoints deverá ocorrer principalmente através de configuração, evitando a necessidade de implementar, compilar e publicar código específico para cada nova integração.

A arquitetura também deverá permitir evolução futura para execução distribuída e múltiplas instâncias do backend.

O acesso ao Oracle utiliza um `DataSource` gerenciado pelo Spring Boot e pool de conexões HikariCP, permitindo o reaproveitamento eficiente das conexões entre as requisições.

## Tecnologias

### Backend

- Java 21
- Spring Boot 4.0.7
- Spring Web MVC
- Spring JDBC
- HikariCP
- Oracle JDBC
- Oracle Database
- Spring Boot Actuator
- Maven
- Spring Security *(previsto)*
- OpenAPI / Swagger *(previsto)*

### Frontend

- React
- JavaScript
- HTML
- CSS

## Estrutura inicial

O repositório é organizado em projetos independentes para backend e frontend:

```text
integration-hub/
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── frontend/
└── README.md
```

## Configuração do banco de dados

O backend utiliza uma conexão Oracle gerenciada pelo Spring Boot.

A configuração do `DataSource` é realizada através do arquivo:

```text
backend/src/main/resources/application.properties
```

As informações específicas do ambiente são fornecidas através de variáveis de ambiente, evitando que endereços, usuários e senhas sejam armazenados no repositório.

São necessárias as seguintes variáveis:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

A configuração utilizada pelo Spring Boot é:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

A URL deve seguir o padrão JDBC do Oracle:

```text
jdbc:oracle:thin:@//host:porta/service
```

### Pool de conexões

O projeto utiliza **HikariCP** para gerenciamento do pool de conexões.

Configuração inicial:

```properties
spring.datasource.hikari.pool-name=IntegrationHubPool
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

Essa configuração poderá ser ajustada conforme o volume de integrações e requisições aumentar.

## Executando o backend

A partir da pasta `backend`, configure as variáveis de ambiente antes de iniciar a aplicação.

Exemplo utilizando Git Bash:

```bash
export DB_URL='jdbc:oracle:thin:@//host:1521/service'
export DB_USERNAME='usuario'
export DB_PASSWORD='senha'
```

Em seguida:

```bash
./mvnw spring-boot:run
```

O backend será iniciado por padrão na porta `8081`:

```text
http://localhost:8081
```

As variáveis definidas através de `export` permanecem disponíveis somente durante a sessão atual do terminal.

## Health Check

O Integration Hub possui um endpoint simplificado para verificar a disponibilidade da aplicação e da conexão com o banco de dados:

```text
GET /api/health
```

Exemplo:

```json
{
  "status": "OK",
  "database": "Online"
}
```

O campo `database` é validado utilizando o `DataSource` configurado na aplicação.

Também está disponível o health check técnico fornecido pelo Spring Boot Actuator:

```text
GET /actuator/health
```

O Actuator fornece informações adicionais sobre a aplicação e permite verificar individualmente o estado da conexão Oracle.

## Status

🚧 **Projeto em desenvolvimento**

A estrutura base do backend já está em funcionamento.

Atualmente estão disponíveis:

- Backend com Java 21 e Spring Boot 4.0.7
- Servidor HTTP na porta 8081
- Conexão com Oracle
- Configuração do banco através de variáveis de ambiente
- Pool de conexões com HikariCP
- Spring JDBC
- Health check da aplicação
- Health check da conexão com o banco
- Spring Boot Actuator
- Maven Wrapper

### Próximas etapas

- Modelagem das tabelas de configuração
- Cadastro e gerenciamento de endpoints
- Definição dos parâmetros dos endpoints
- Execução parametrizada de consultas
- OpenAPI / Swagger
- Autenticação e autorização
- Controle dos perfis Criador e Consumidor
- Histórico e auditoria das execuções
- Interface administrativa em React

---

**Integration Hub** — Plataforma para centralização, padronização e gerenciamento de APIs de integração.