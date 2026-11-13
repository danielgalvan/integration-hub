# Integration Hub — Angular

Segunda implementação independente do frontend. O React continua em
`../frontend`; ambas as interfaces consomem a mesma API Spring Boot e o mesmo
banco Oracle.

## Tecnologias

- Angular 22 e Angular Material 22;
- TypeScript 6;
- componentes standalone e rotas carregadas sob demanda;
- Signals para estado local e de sessão;
- Reactive Forms para validação de formulários;
- SCSS, layout responsivo e tema claro/escuro;
- `HttpClient` com interceptor JWT e guards de rota.

Requisitos de Node.js: `22.22.3+` na linha 22 ou `24.15.0+` na linha 24.

## Funcionalidades

- seleção de ambiente no login, sessão JWT, logout por sessão expirada e troca obrigatória de senha;
- perfis Administrador (`A`), Criador (`C`) e Consumidor (`U`), com telas administrativas protegidas;
- listagem, cadastro, edição e exclusão de integrações;
- geração da API Key legada e gerenciamento de várias credenciais por integração;
- cadastro, edição, visualização somente leitura, exclusão e teste de endpoints `GET`;
- teste de endpoints com parâmetros tipados, header `X-API-Key`, status, duração, URL e cópia do endereço;
- administração de usuários, redefinição de senha e exibição única de senha temporária;
- histórico paginado com filtros de integração, endpoint, status e período, além de detalhes completos da execução.

## Organização do código

O código fica em `src/app` e é organizado por funcionalidade:

```text
src/app/
├── core/
│   ├── auth/              # API de autenticação, sessão, guards e interceptor JWT
│   ├── models/            # tipos transversais e autenticação
│   └── services/          # serviços de infraestrutura, como o tema
├── layout/               # shell da aplicação e navegação
├── shared/dialogs/       # confirmação e exibição segura de segredos
└── features/
    ├── login/pages/      # login e troca obrigatória de senha
    ├── integrations/     # páginas, componentes, diálogos, serviços e modelos
    ├── endpoints/        # páginas, componentes, diálogos, serviços, modelos e URL builder
    ├── executions/       # histórico, filtros, detalhes e serviço HTTP
    └── users/            # administração, formulários, serviço HTTP e modelos
```

Convenções para novas alterações:

- `pages/` contém componentes associados diretamente às rotas. As páginas
  continuam standalone e são carregadas sob demanda por `loadComponent`.
- `components/` contém formulários, listas e filtros da própria funcionalidade.
- `dialogs/` contém diálogos usados apenas por uma funcionalidade; diálogos
  genéricos permanecem em `shared/dialogs/`.
- `services/` contém chamadas HTTP e operações reutilizáveis do domínio. Os
  serviços não dependem de componentes nem controlam a apresentação.
- `models/` contém os tipos do domínio. Tipos transversais e de autenticação
  ficam em `core/models/`.
- Templates e estilos externos são usados nos componentes maiores; templates
  pequenos podem permanecer inline quando isso deixa o componente mais claro.
- `core/` é reservado para infraestrutura transversal. Não adicionar serviços
  de negócio de uma feature nessa camada.

As features podem consumir modelos e serviços de outra feature quando houver
dependência funcional, como o histórico usando os catálogos de integrações e
endpoints. Componentes puramente visuais recebem dados por inputs e comunicam
ações por outputs; chamadas HTTP ficam nos serviços da feature.

## Executar localmente

Use Node.js `22.22.3+` na linha 22 ou `24.15.0+` na linha 24. Inicie primeiro o
backend Spring Boot na porta `8081`. Em `frontend-angular/`, instale as
dependências e inicie a interface:

```bash
npm ci
npm start
```

A aplicação fica em `http://localhost:4200`. O `proxy.conf.json` encaminha
`/api` para `http://localhost:8081`, sem exigir configuração adicional de CORS
no desenvolvimento local. O React continua independente em `../frontend`;
ambas as interfaces usam os mesmos contratos do backend.

## Verificações

```bash
npm run lint
npm test
npm run build
```

`npm test` executa a suíte Vitest configurada pelo Angular. Execute lint, testes
e build antes de integrar mudanças; os fluxos que dependem do Oracle ou de um
backend disponível devem ser validados separadamente no ambiente local.
