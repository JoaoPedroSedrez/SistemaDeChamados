# SistemaDeChamados

API REST de um sistema de chamados (helpdesk), feita na disciplina de Design e Arquitetura de Sistemas.
Java 21 + Spring Boot, com banco H2 em memória.

## Rodando o projeto

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080` e o console do H2 fica em `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:chamados`, usuário `sa`, sem senha).

## Listagem de chamados

`GET /chamados` aceita filtros, paginação e ordenação. Nenhum parâmetro é obrigatório.

| Parâmetro    | O que faz                                    | Exemplo              |
|--------------|----------------------------------------------|----------------------|
| `status`     | `ABERTO`, `EM_ATENDIMENTO`, `AGUARDANDO_CLIENTE` ou `FECHADO` | `status=ABERTO` |
| `prioridade` | `BAIXA`, `MEDIA`, `ALTA` ou `CRITICA`        | `prioridade=CRITICA` |
| `clienteId`  | só os chamados de um cliente                 | `clienteId=2`        |
| `page`       | página (começa em 0)                         | `page=0`             |
| `size`       | quantidade por página (padrão 10)            | `size=5`             |
| `sort`       | `campo,asc` ou `campo,desc` (padrão `dataAbertura,desc`) | `sort=titulo,asc` |

Campos aceitos no `sort`: `id`, `titulo`, `dataAbertura`, `dataFechamento`, `prioridade`, `status`,
`cliente.nome` e `categoria.nome`. Prioridade e status são ordenados pelo nome (ordem alfabética).

Também existe `GET /clientes/{id}/chamados`, que lista os chamados de um cliente com a mesma paginação.

Alguns exemplos:

```bash
curl "http://localhost:8080/chamados?status=ABERTO"
curl "http://localhost:8080/chamados?prioridade=ALTA&clienteId=1"
curl "http://localhost:8080/chamados?page=1&size=5&sort=titulo,asc"
curl "http://localhost:8080/clientes/1/chamados"
```

Resposta:

```json
{
  "itens": [ { "id": 3, "titulo": "Sem internet", "status": "ABERTO", "prioridade": "ALTA" } ],
  "pagina": 0,
  "tamanho": 10,
  "totalItens": 1,
  "totalPaginas": 1
}
```

Cada item tem o mesmo formato de `GET /chamados/{id}` (o exemplo acima está resumido).

### Erros

- `status`/`prioridade` com valor que não existe → **400**
- `sort` com campo não permitido → **400**
- `/clientes/{id}/chamados` com cliente que não existe → **404**
- `clienteId` de cliente que não existe em `/chamados` → **200** com lista vazia
