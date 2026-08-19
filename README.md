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
│    REST APIs      │
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

* Nome e descrição
* Rota do endpoint
* Método HTTP
* Consulta SQL
* Parâmetros de entrada
* Conexão utilizada
* Controle de acesso
* Status do endpoint
* Documentação
* Histórico de execução

A partir dessas configurações, o Integration Hub será responsável por receber a requisição, validar os parâmetros, executar a consulta e retornar o resultado através de uma API REST.

## Arquitetura

A arquitetura inicial será composta por:

* **Frontend:** React
* **Backend:** Java + Spring Boot
* **Banco de dados:** Oracle
* **Documentação:** OpenAPI / Swagger
* **Segurança:** Spring Security
* **Build:** Maven

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
     Gerenciamento de             Execução dos
       integrações                  endpoints
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

* Criar endpoints
* Alterar endpoints
* Definir consultas SQL
* Configurar parâmetros
* Definir permissões
* Publicar e desativar endpoints
* Testar endpoints
* Consultar documentação
* Acompanhar execuções

### Consumidor

Responsável pela utilização das APIs disponibilizadas.

Entre suas permissões estarão:

* Visualizar endpoints disponíveis
* Consultar documentação
* Visualizar parâmetros
* Testar endpoints autorizados
* Consumir APIs

## Segurança

O Integration Hub não será um executor genérico de SQL exposto aos consumidores.

As consultas serão previamente cadastradas e controladas pela plataforma. Os consumidores terão acesso somente aos endpoints para os quais possuírem autorização.

Entre os mecanismos previstos estão:

* Autenticação
* Autorização por endpoint
* Consultas parametrizadas
* Validação de parâmetros
* Proteção contra SQL Injection
* Controle das operações SQL permitidas
* Auditoria das chamadas
* Registro de erros
* Timeout de execução
* Limitação de resultados
* Controle de endpoints ativos e inativos

## Documentação das APIs

Os endpoints disponibilizados pela plataforma deverão possuir documentação através de **OpenAPI / Swagger**.

A documentação permitirá consultar:

* Rotas disponíveis
* Métodos HTTP
* Parâmetros de entrada
* Tipos de dados
* Exemplos de requisição
* Exemplos de resposta
* Códigos HTTP
* Descrição do endpoint

Além da documentação, usuários autorizados poderão testar os endpoints diretamente pela interface.

## Escalabilidade

O projeto será iniciado com um número reduzido de integrações, mas sua arquitetura deverá permitir crescimento progressivo.

A criação de novos endpoints deverá ocorrer principalmente através de configuração, evitando a necessidade de implementar, compilar e publicar código específico para cada nova integração.

A arquitetura também deverá permitir evolução futura para execução distribuída e múltiplas instâncias do backend.

## Tecnologias

### Backend

* Java
* Spring Boot
* Spring Security
* JDBC
* Oracle Database
* OpenAPI / Swagger
* Maven

### Frontend

* React
* JavaScript
* HTML
* CSS

## Estrutura inicial

A estrutura do repositório será organizada em:

```text
integration-hub/
├── backend/
├── frontend/
└── README.md
```

## Status

🚧 **Projeto em desenvolvimento**

O projeto encontra-se em fase inicial de definição da arquitetura e implementação da estrutura base.

---

**Integration Hub** — Plataforma para centralização, padronização e gerenciamento de APIs de integração.
