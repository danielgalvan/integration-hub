# Integration Hub — React

Frontend administrativo do Integration Hub, construído em React. Esta interface
continua sendo a aplicação servida em produção; o frontend Angular é mantido em
`../frontend-angular` e pode ser executado em paralelo contra a mesma API.

## Tecnologias

- React 19 e Vite 8;
- JavaScript com componentes funcionais e hooks;
- CSS por página e componente;
- Vitest, jsdom e Testing Library;
- ESLint.

## Funcionalidades

- login por ambiente, sessão JWT e troca obrigatória de senha;
- perfis Administrador (`A`), Criador (`C`) e Consumidor (`U`);
- cadastro, edição, consulta e exclusão de integrações;
- geração da API Key legada e administração de múltiplas credenciais;
- cadastro, edição, consulta, exclusão e teste de endpoints `GET`;
- validação de parâmetros `NUMBER`, `DATE` e `TIMESTAMP` antes da execução;
- histórico paginado de execuções com filtros e detalhes;
- administração de usuários e redefinição de senha.

O token e os dados da sessão ficam em `sessionStorage`. Ao restaurar uma sessão,
o frontend consulta `/api/auth/me` antes de liberar as telas protegidas. Se a API
estiver indisponível, a sessão permanece bloqueada e a tela permite tentar a
validação novamente ou encerrar a sessão.

## Executar localmente

Requisitos: Node.js 22 ou 24 e npm. Inicie o backend Integration Hub na porta
`8081` com um perfil e uma conexão Oracle configurados. Em seguida, na pasta
`frontend/`:

```bash
npm ci
npm run dev
```

O Vite disponibiliza a aplicação em `http://localhost:5175` e encaminha as
requisições `/api/**` para `http://localhost:8081` por meio do proxy definido em
`vite.config.js`.

## Verificações

```bash
npm run lint
npm test
npm run build
```

Os testes usam Vitest em ambiente `jsdom`; componentes são exercitados com
Testing Library e chamadas HTTP são simuladas nos testes dos serviços.
