# Integration Hub

O **Integration Hub** é uma plataforma para criação, gerenciamento e disponibilização de APIs de integração sobre bases de dados Oracle.

O objetivo do projeto é simplificar e padronizar a criação de integrações, permitindo que consultas SQL sejam configuradas e posteriormente disponibilizadas como endpoints HTTP de forma segura, documentada e controlada.

O desenvolvimento está sendo realizado de forma incremental. A primeira versão terá foco em integrações de leitura utilizando `GET`, permitindo validar a arquitetura, o modelo de domínio e a execução das consultas antes da expansão para outros métodos HTTP.

---

## Arquitetura

O projeto utiliza uma arquitetura inicialmente dividida entre backend e frontend.

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

O frontend será desenvolvido em uma etapa posterior, após a consolidação da API e da execução dinâmica das integrações.

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
```

Exemplo:

```text
id:          1
name:        Clientes
basePath:    /api/clientes
active:      true
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
active:         true
```

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

# Persistência atual

Nesta fase do desenvolvimento, os cadastros de integrações e endpoints continuam sendo mantidos em memória pela aplicação.

São utilizados:

```text
InMemoryIntegrationRepository
InMemoryEndpointRepository
```

Os identificadores são gerados em memória e os dados são perdidos sempre que a aplicação é reiniciada.

Essa implementação é propositalmente temporária e permite validar o domínio, os serviços e os contratos da API antes da introdução da persistência definitiva.

---

# Persistência Oracle

A persistência definitiva do Integration Hub será realizada em Oracle Database.

Todas as tabelas pertencentes à aplicação utilizarão o prefixo:

```text
IH_
```

As primeiras tabelas previstas são:

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

A criação dessas tabelas será realizada após a validação do modelo atualmente implementado em memória.

---

# Ambiente Oracle de desenvolvimento

O projeto possui um ambiente Oracle local dedicado ao desenvolvimento.

A infraestrutura é executada em uma máquina virtual isolada, permitindo desenvolver e testar o Integration Hub sem depender de ambientes Oracle externos.

O ambiente utiliza:

* VirtualBox
* Oracle Linux
* Oracle Database Free 23ai
* Oracle Net Listener
* rede em modo Bridge para comunicação entre host e VM
* endereço IPv4 estático para a VM

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

Para facilitar o desenvolvimento local, pode ser utilizado o script:

```text
start-integration-hub-db.bat
```

O script é responsável apenas pela infraestrutura Oracle e não inicia o backend Spring Boot.

Fluxo:

```text
Executar BAT
    │
    ▼
Verificar estado da VM
    │
    ▼
Iniciar VM em modo headless
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

Essa separação permite reiniciar o backend Spring Boot durante o desenvolvimento sem precisar reiniciar ou revalidar toda a infraestrutura Oracle.

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

O profile deve ser ativado através de:

```text
local
```

O arquivo local permite executar o projeto sem precisar definir manualmente as variáveis de ambiente a cada nova sessão do terminal.

Credenciais reais e informações específicas da máquina de desenvolvimento não devem ser versionadas.

Uma futura alternativa é manter no repositório apenas um arquivo de exemplo:

```text
application-local.example.yml
```

---

# Executando o backend

Primeiro, certifique-se de que o ambiente Oracle esteja disponível.

Caso utilize a VM local, execute:

```text
start-integration-hub-db.bat
```

Aguarde:

```text
AMBIENTE PRONTO
```

Depois entre no diretório do backend:

```bash
cd backend
```

## Desenvolvimento local

No Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

O log deverá indicar que o profile `local` está ativo.

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

Portanto, a aplicação estará disponível em:

```text
http://localhost:8081
```

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

Resposta esperada quando a aplicação e o banco estiverem disponíveis:

```json
{
  "database": "Online",
  "status": "OK"
}
```

O endpoint permite identificar separadamente problemas na aplicação e indisponibilidade do banco.

---

# API de Integrações

## Listar integrações

```http
GET /api/integrations
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
  "active": true
}
```

O identificador é atualmente gerado pelo repositório em memória.

---

# API de Endpoints

## Listar endpoints

```http
GET /api/endpoints
```

---

## Buscar endpoint

```http
GET /api/endpoints/{id}
```

Exemplo:

```http
GET /api/endpoints/1
```

---

## Listar endpoints de uma integração

```http
GET /api/endpoints/integration/{integrationId}
```

Exemplo:

```http
GET /api/endpoints/integration/1
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

O `POST` é utilizado para configuração administrativa do Integration Hub.

Os endpoints dinamicamente disponibilizados para consumidores terão inicialmente apenas operações `GET`.

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

A cada execução configurada no workflow é realizado:

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

* cadastro de integrações;
* cadastro de endpoints;
* relacionamento `Integration 1:N Endpoint`;
* endpoints de consumo do tipo `GET`;
* consultas SQL parametrizadas;
* validação dos parâmetros recebidos;
* conexão com Oracle através de pool;
* execução dinâmica das consultas;
* retorno dos resultados em JSON;
* documentação da API;
* ambiente Oracle local para desenvolvimento.

Funcionalidades adicionais serão incorporadas de maneira incremental após a estabilização desse fluxo.

---

# Fora do escopo inicial

Não fazem parte da primeira implementação:

* operações dinâmicas `POST`;
* operações dinâmicas `PUT`;
* operações dinâmicas `PATCH`;
* operações dinâmicas `DELETE`;
* mensageria;
* processamento assíncrono;
* orquestração em Kubernetes;
* persistência de configurações fora do Oracle;
* recursos avançados de escalabilidade distribuída.

Essas funcionalidades poderão ser avaliadas conforme o crescimento e as necessidades reais da plataforma.

---

# Segurança

A execução de SQL configurável exige controles específicos.

Entre os princípios previstos para o projeto estão:

* utilização obrigatória de bind parameters;
* proibição de concatenação direta de parâmetros no SQL;
* validação dos parâmetros antes da execução;
* separação entre configuração e consumo das integrações;
* controle de acesso;
* armazenamento seguro das credenciais;
* restrição dos tipos de SQL permitidos;
* auditoria das execuções em etapas futuras.

Na V1, o foco será em consultas de leitura.

---

# Perfis de acesso previstos

A plataforma deverá possuir dois perfis principais.

## Criador

Responsável pela configuração das integrações.

Poderá:

* cadastrar integrações;
* cadastrar endpoints;
* definir consultas SQL;
* definir parâmetros;
* testar consultas;
* visualizar documentação;
* ativar ou desativar integrações.

## Consumidor

Responsável pelo consumo e validação das integrações disponibilizadas.

Poderá:

* consultar integrações disponíveis;
* visualizar documentação;
* visualizar os parâmetros necessários;
* testar endpoints autorizados;
* consumir as APIs publicadas.

O mecanismo de autenticação e autorização será implementado em uma etapa posterior.

---

# Documentação da API

Está prevista a adoção de:

```text
OpenAPI
Swagger UI
```

A documentação deverá permitir visualizar:

* integrações disponíveis;
* endpoints;
* métodos HTTP;
* parâmetros;
* exemplos de requisição;
* exemplos de resposta;
* testes diretamente pela interface.

---

# Estado atual

Atualmente estão implementados e validados:

* aplicação Spring Boot com Java 21;
* backend executando na porta `8081`;
* conexão JDBC com Oracle;
* configuração de datasource por variáveis de ambiente;
* profile `local` para desenvolvimento;
* configuração local através de `application-local.yml`;
* HikariCP;
* health check da aplicação;
* health check do Oracle;
* cadastro de integrações em memória;
* geração automática de IDs em memória;
* consulta de integrações;
* cadastro de endpoints em memória;
* consulta de endpoints;
* consulta de endpoints por integração;
* relacionamento `Integration 1:N Endpoint`;
* estrutura de controller, service e repository;
* testes automatizados do backend;
* Maven Wrapper;
* build com `clean verify`;
* workflow de validação no GitHub Actions;
* VM dedicada para Oracle;
* Oracle Linux configurado;
* Oracle Database Free 23ai instalado;
* Oracle Net Listener configurado;
* inicialização automática dos serviços Oracle;
* endereço IPv4 estático para a VM;
* comunicação entre a máquina de desenvolvimento e a VM;
* script para inicialização e validação automática do ambiente Oracle;
* validação da disponibilidade do Listener antes da inicialização do backend;
* conexão ponta a ponta entre Spring Boot e Oracle validada.

A persistência das configurações do Integration Hub ainda permanece em memória.

A execução dinâmica dos SQLs cadastrados também ainda será implementada.

---

# Próximas etapas

A sequência prevista de desenvolvimento é:

1. preparar o schema Oracle para o Integration Hub;
2. criar a tabela `IH_INTEGRATION`;
3. criar a tabela `IH_ENDPOINT`;
4. definir índices, constraints e relacionamentos;
5. substituir gradualmente os repositories em memória pela persistência Oracle;
6. implementar resolução dinâmica de `basePath + path`;
7. implementar validação dos parâmetros dos endpoints;
8. implementar execução dinâmica das consultas SQL;
9. retornar os resultados das consultas em JSON;
10. adicionar tratamento padronizado de erros;
11. adicionar Swagger/OpenAPI;
12. implementar autenticação e controle de acesso;
13. iniciar o frontend em React.

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
