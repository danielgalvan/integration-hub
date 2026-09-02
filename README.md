# Integration Hub

O **Integration Hub** é uma plataforma para criar, gerenciar e disponibilizar APIs de integração sobre bases Oracle sem a necessidade de implementar um controller Java para cada consulta.

A V1 é focada em **endpoints dinâmicos de leitura (`GET`)**, com SQL parametrizado, autenticação administrativa, RBAC, API Key por integração, documentação OpenAPI e interface web.

> **Ambiente online:** https://integrationhub.duckdns.org

---

## Arquitetura

### Produção

```text
Internet
   │
   │ HTTPS
   ▼
Nginx
   ├── /           → React
   └── /api/**     → Spring Boot :8081
                          │
                          │ JDBC / Wallet
                          ▼
                  Oracle Autonomous Database
```

O ambiente publicado utiliza:

- Oracle Cloud Infrastructure (OCI);
- Oracle Linux 9;
- Nginx como servidor web e reverse proxy;
- Spring Boot executado como serviço `systemd`;
- Oracle Autonomous Database;
- DuckDNS para hostname;
- Let's Encrypt + Certbot para HTTPS e renovação automática.

### Desenvolvimento local

```text
React + Vite :5175
       │
       │ proxy /api
       ▼
Spring Boot :8081
       │
       ▼
Oracle
```

O frontend utiliza URLs relativas (`/api/...`). Em desenvolvimento, o Vite encaminha `/api` para `http://localhost:8081`; em produção, o Nginx faz o mesmo encaminhamento para o backend.

---

## Stack

### Backend

- Java 21
- Spring Boot 4.0.7
- Spring Web MVC
- Spring Security
- Spring JDBC
- HikariCP
- Oracle JDBC
- Oracle Wallet / Autonomous Database
- JJWT
- BCrypt
- OpenAPI 3.1 / Swagger UI
- Maven
- JUnit / Mockito

### Frontend

- React 19
- Vite 8
- JavaScript
- Vitest
- Testing Library
- ESLint
- npm

---

## Funcionalidades da V1

### Integrações

Uma `Integration` agrupa endpoints sob um `basePath`.

```text
Integration
    │
    │ 1:N
    ▼
Endpoint
```

Exemplo:

```text
name:      Pedidos
basePath:  /api/pedidos
active:    S
authType:  API_KEY
```

Regras principais do `basePath`:

- obrigatório;
- deve iniciar com `/api/`;
- não deve terminar com `/`;
- não pode conter espaços;
- a resolução considera o `basePath` ativo mais específico.

### Endpoints dinâmicos

Cada endpoint define:

```text
integrationId
name
description
path
method
sqlText
parameters
active
```

Na V1, somente `GET` é disponibilizado dinamicamente.

Exemplo:

```text
basePath: /api/pedidos
path:     /listar
```

Resultado:

```http
GET /api/pedidos/listar?status=ABERTO
```

O backend resolve a integração e o endpoint em tempo de execução e executa o SQL configurado no Oracle.

---

## SQL dinâmico e parâmetros

As consultas devem utilizar **bind parameters**:

```sql
select id,
       numero,
       status,
       valor_total
from pedido
where status = :status
```

Tipos suportados:

| Tipo | Formato |
| --- | --- |
| `VARCHAR2` | texto |
| `NUMBER` | número |
| `DATE` | `yyyy-MM-dd` |
| `TIMESTAMP` | data/hora |

Exemplo de parâmetro:

```json
{
  "name": "pedido_id",
  "type": "NUMBER",
  "required": true
}
```

Os parâmetros são identificados a partir das bind variables do SQL e podem ser gerados automaticamente pelo frontend.

### Restrições de segurança do SQL

A V1:

- aceita somente uma instrução iniciada por `SELECT`;
- utiliza bind parameters;
- rejeita ponto e vírgula;
- rejeita comentários SQL;
- rejeita `SELECT ... FOR UPDATE`;
- valida e converte parâmetros antes da execução;
- limita a quantidade máxima de resultados.

O limite padrão é:

```text
integration-hub.dynamic.max-results = 1000
```

---

## Autenticação e autorização

### Administração

As APIs administrativas utilizam usuários persistidos em `IH_USERS`, senhas BCrypt e JWT stateless.

```http
POST /api/auth/login
```

Exemplo:

```json
{
  "username": "admin",
  "password": "senha",
  "environment": "development"
}
```

Chamadas protegidas utilizam:

```http
Authorization: Bearer <token>
```

`401 Unauthorized` representa ausência ou invalidez da autenticação.
`403 Forbidden` representa usuário autenticado sem permissão para a operação.

### Perfis

| Recurso | Administrador | Criador | Consumidor |
| --- | :---: | :---: | :---: |
| Consultar integrações/endpoints | ✓ | ✓ | ✓ |
| Criar/editar/excluir integrações | ✓ | ✓ | — |
| Criar/editar/excluir endpoints | ✓ | ✓ | — |
| Testar endpoints | ✓ | ✓ | ✓ |
| Gerenciar usuários | ✓ | — | — |
| Gerar/regenerar API Key | ✓ | ✓ | — |

Perfis internos:

```text
A = Administrador
C = Criador
U = Consumidor
```

O frontend aplica as permissões e mantém o Consumidor em modo somente leitura.

### Senhas

O gerenciamento de usuários inclui:

- criação de usuário;
- senha temporária;
- troca obrigatória de senha;
- reset administrativo;
- armazenamento somente do hash BCrypt.

---

## Autenticação dos endpoints dinâmicos

A autenticação administrativa é independente da autenticação de consumo.

Cada integração define:

```text
NONE
API_KEY
```

### `NONE`

O endpoint pode ser consumido sem API Key.

```http
GET /api/pedidos/listar?status=ABERTO
```

### `API_KEY`

Todos os endpoints da integração exigem:

```http
X-API-Key: ihub_xxxxxxxxxxxxxxxxx
```

A chave:

- é gerada aleatoriamente;
- é exibida somente na geração/regeneração;
- é armazenada no Oracle apenas como hash BCrypt;
- é compartilhada pelos endpoints da mesma integração;
- invalida a chave anterior quando regenerada.

Geração/regeneração:

```http
POST /api/integrations/{id}/api-key
Authorization: Bearer <token>
```

---

## Ambientes e DataSources

O backend suporta múltiplas conexões Oracle configuradas em:

```text
integration-hub.datasource.connections
```

A tela de login carrega os ambientes dinamicamente:

```http
GET /api/environments
```

Exemplo:

```json
[
  {
    "id": "development",
    "name": "Desenvolvimento Local"
  },
  {
    "id": "cloud",
    "name": "Oracle Cloud"
  }
]
```

Quando existe apenas uma conexão configurada, ela também é utilizada como DataSource padrão para operações sem ambiente explícito, como health check e bootstrap. Com múltiplas conexões, o ambiente selecionado continua obrigatório.

### Configuração local

Copie:

```text
backend/src/main/resources/application-local.example.yml
```

para:

```text
backend/src/main/resources/application-local.yml
```

Exemplo:

```yaml
integration-hub:
  datasource:
    connections:
      development:
        name: Desenvolvimento Local
        url: jdbc:oracle:thin:@//localhost:1521/freepdb1
        username: seu_usuario
        password: sua_senha

      cloud:
        name: Oracle Cloud
        url: jdbc:oracle:thin:@ihub_high?TNS_ADMIN=C:/caminho/para/wallet
        username: seu_usuario_cloud
        password: sua_senha_cloud

  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5175}

  security:
    jwt:
      secret: SUA_CHAVE_JWT
      expiration-minutes: 60
```

`application-local.yml`, configurações cloud reais, credenciais, Wallets e segredos não devem ser versionados.

O CORS é configurável por ambiente através de:

```text
integration-hub.cors.allowed-origins
```

ou:

```text
CORS_ALLOWED_ORIGINS
```

---

## Persistência Oracle

As tabelas próprias da aplicação utilizam o prefixo `IH_`.

Principais tabelas:

```text
IH_INTEGRATION
IH_ENDPOINT
IH_USERS
```

`IH_INTEGRATION` armazena também:

```text
AUTH_TYPE
API_KEY_HASH
API_KEY_CREATED_AT
```

Os parâmetros dos endpoints são persistidos em JSON.

Scripts de instalação ficam em:

```text
backend/database/install/
```

---

## Principais APIs

### Públicas

```http
GET  /api/health
GET  /api/environments
POST /api/auth/login
```

### Sessão

```http
GET /api/auth/me
PUT /api/auth/password
```

### Integrações

```http
GET    /api/integrations
GET    /api/integrations/{id}
POST   /api/integrations
PUT    /api/integrations/{id}
DELETE /api/integrations/{id}

POST   /api/integrations/{id}/api-key
```

### Endpoints

```http
GET    /api/endpoints
GET    /api/endpoints/{id}
GET    /api/endpoints/integration/{integrationId}
POST   /api/endpoints
PUT    /api/endpoints/{id}
DELETE /api/endpoints/{id}
```

### Usuários

As rotas `/api/users/**` são administrativas e restritas ao perfil Administrador.

---

## Health check e documentação

Health check:

```http
GET /api/health
```

Resposta esperada:

```json
{
  "status": "OK",
  "database": "Online"
}
```

OpenAPI:

```text
http://localhost:8081/v3/api-docs
```

Swagger UI:

```text
http://localhost:8081/swagger-ui/index.html
```

Os endpoints dinâmicos são adicionados automaticamente à especificação OpenAPI com base nas configurações persistidas no Oracle.

---

## Executando localmente

### Backend

Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Linux / Git Bash:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Backend:

```text
http://localhost:8081
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5175
```

O Vite encaminha `/api` para o backend local em `localhost:8081`.

---

## Validação

### Backend

```bash
cd backend
./mvnw clean verify
```

Windows:

```powershell
cd backend
.\mvnw.cmd clean verify
```

### Frontend

```bash
cd frontend
npm run lint
npm run test
npm run build
```

---

## CI

O workflow:

```text
.github/workflows/validate.yml
```

é executado em `push` e `pull_request` para `main`, além de permitir execução manual.

Ele valida:

```text
Backend
  Java 21
  Maven clean verify

Frontend
  Node 24
  npm ci
  lint
  testes
  build
```

---

## Segurança

A V1 adota, entre outros, os seguintes controles:

- JWT stateless nas APIs administrativas;
- BCrypt para senhas e API Keys;
- RBAC por operação;
- API Key opcional por integração;
- bind parameters obrigatórios;
- validação de parâmetros antes do banco;
- execução dinâmica restrita a `SELECT`;
- bloqueio de comentários, `;` e `FOR UPDATE`;
- limite de resultados;
- CORS configurável por ambiente;
- HTTPS no ambiente publicado;
- segredos e credenciais fora do repositório;
- tratamento centralizado de erros.

---

## Estrutura resumida

```text
integration-hub/
├── .github/workflows/
├── backend/
│   ├── database/install/
│   ├── src/main/java/br/com/integrationhub/
│   │   ├── auth/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── exception/
│   │   ├── integration/
│   │   ├── security/
│   │   ├── service/
│   │   └── user/
│   └── src/test/
│
├── frontend/
│   ├── public/
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       └── utils/
│
└── README.md
```

---

## Escopo da V1

A V1 contempla:

- CRUD administrativo de integrações e endpoints;
- endpoints dinâmicos `GET`;
- SQL parametrizado e execução segura;
- parâmetros `VARCHAR2`, `NUMBER`, `DATE` e `TIMESTAMP`;
- resolução dinâmica de `basePath + path`;
- autenticação administrativa com JWT;
- usuários persistidos no Oracle;
- RBAC `A/C/U`;
- senha temporária, troca obrigatória e reset;
- autenticação `NONE` ou `API_KEY` por integração;
- OpenAPI/Swagger;
- frontend administrativo React;
- testes automatizados e CI;
- múltiplos ambientes Oracle;
- execução local e publicação em OCI;
- Oracle Autonomous Database;
- frontend e backend publicados com HTTPS.

### Fora do escopo inicial

- endpoints dinâmicos `POST`, `PUT`, `PATCH` e `DELETE`;
- mensageria;
- processamento assíncrono;
- Kubernetes;
- OAuth2;
- refresh token;
- recursos avançados de escalabilidade distribuída.

---

## Status

A **V1 está funcional e publicada em cloud**.

O fluxo completo foi validado com:

```text
Frontend React
      ↓ HTTPS
Nginx
      ↓
Spring Boot
      ↓
Oracle Autonomous Database
```

Estão operacionais o login, seleção de ambiente, RBAC, administração de usuários, integrações, endpoints dinâmicos, API Keys, health check e acesso externo por navegador desktop e mobile.

As próximas evoluções podem se concentrar em novos métodos dinâmicos, melhorias operacionais de deploy, observabilidade e evolução das capacidades de integração.
