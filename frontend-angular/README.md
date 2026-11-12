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

Os recursos estão organizados em `src/app/core`, `layout`, `features` e
`shared`. As telas usam componentes separados para formulários, tabelas,
diálogos e filtros.

## Executar localmente

Inicie primeiro o backend na porta `8081`. Depois:

```bash
npm ci
npm start
```

A aplicação fica em `http://localhost:4200`. O `proxy.conf.json` encaminha
`/api` para `http://localhost:8081`, sem exigir configuração adicional de
CORS no desenvolvimento local.

## Verificações

```bash
npm run lint
npm run build
npm test
```
