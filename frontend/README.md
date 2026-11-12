# Front-end — Sistema de Chamados

React + Vite + Tailwind + shadcn/ui.

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

## Integração com o backend

Hoje as telas usam dados falsos de `src/mock.js`. Para usar o Spring Boot:

1. Em `src/api.js`, troque `USE_MOCK = true` por `false`.
2. Suba o backend em `http://localhost:8080`.

O Vite repassa `/api/*` para `http://localhost:8080/*` (veja `vite.config.js`), então não precisa configurar CORS no backend em dev.

## Componentes shadcn

```bash
npx shadcn@latest add <componente>
```
