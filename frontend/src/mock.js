import { PERFIS } from './constants.js';

const db = {
  categorias: [
    { id: 1, nome: 'Hardware', descricao: 'Problemas com equipamentos' },
    { id: 2, nome: 'Software', descricao: 'Erros e dúvidas de sistemas' },
    { id: 3, nome: 'Rede', descricao: null },
  ],
  clientes: [
    { id: 1, nome: 'Maria Souza', email: 'maria@exemplo.com', telefone: '(47) 99999-0001' },
    { id: 2, nome: 'Carlos Lima', email: 'carlos@exemplo.com', telefone: null },
    { id: 3, nome: 'Ana Prado', email: 'ana@exemplo.com', telefone: '(47) 99999-0003' },
  ],
  tecnicos: [
    { id: 1, nome: 'Rafael Mendes', email: 'rafael@suporte.com', perfil: PERFIS[0] },
    { id: 2, nome: 'Juliana Reis', email: 'juliana@suporte.com', perfil: PERFIS[1] },
    { id: 3, nome: 'Paulo Vieira', email: 'paulo@suporte.com', perfil: PERFIS[1] },
  ],
  chamados: [
    { id: 1, titulo: 'Notebook não liga', descricao: 'Após queda de energia o notebook não dá sinal de vídeo.', dataAbertura: '2026-11-02T09:30:00', dataFechamento: null, prioridade: 'ALTA', status: 'EM_ATENDIMENTO', clienteId: 1, categoriaId: 1, tecnicoId: 1 },
    { id: 2, titulo: 'Erro ao emitir relatório', descricao: 'O relatório mensal fecha o sistema com erro 500.', dataAbertura: '2026-11-04T14:10:00', dataFechamento: null, prioridade: 'CRITICA', status: 'ABERTO', clienteId: 2, categoriaId: 2, tecnicoId: null },
    { id: 3, titulo: 'Wi-Fi instável no 2º andar', descricao: 'Queda de conexão várias vezes ao dia.', dataAbertura: '2026-10-28T08:00:00', dataFechamento: '2026-11-01T17:45:00', prioridade: 'MEDIA', status: 'FECHADO', clienteId: 3, categoriaId: 3, tecnicoId: 2 },
    { id: 4, titulo: 'Instalar impressora nova', descricao: 'Instalação e configuração na rede.', dataAbertura: '2026-11-06T11:20:00', dataFechamento: null, prioridade: 'BAIXA', status: 'AGUARDANDO_CLIENTE', clienteId: 1, categoriaId: 1, tecnicoId: 3 },
  ],
  comentarios: [
    { id: 1, chamadoId: 1, mensagem: 'Equipamento recebido para análise.', autor: 'Rafael Mendes', dataCriacao: '2026-11-02T10:00:00' },
    { id: 2, chamadoId: 1, mensagem: 'Suspeita de falha na fonte.', autor: 'Rafael Mendes', dataCriacao: '2026-11-03T15:30:00' },
    { id: 3, chamadoId: 4, mensagem: 'Preciso do modelo exato da impressora.', autor: 'Paulo Vieira', dataCriacao: '2026-11-06T12:00:00' },
  ],
};

const wait = (valor) =>
  new Promise((resolve) => setTimeout(() => resolve(JSON.parse(JSON.stringify(valor ?? null))), 200));

const falha = (status, message) => Promise.reject({ response: { status, data: { message } } });

const agora = () => new Date().toISOString().slice(0, 19);
const proximo = (lista) => Math.max(0, ...lista.map((i) => i.id)) + 1;
const porId = (lista, id) => lista.find((i) => i.id === Number(id));
const naoEncontrado = () => falha(404, 'Registro não encontrado');

function crud(nome) {
  return {
    listar: () => wait(db[nome]),
    buscar: (id) => {
      const item = porId(db[nome], id);
      return item ? wait(item) : naoEncontrado();
    },
    criar: (dados) => {
      const novo = { id: proximo(db[nome]), ...dados };
      db[nome].push(novo);
      return wait(novo);
    },
    atualizar: (id, dados) => {
      const i = db[nome].findIndex((x) => x.id === Number(id));
      if (i < 0) return naoEncontrado();
      db[nome][i] = { ...db[nome][i], ...dados, id: Number(id) };
      return wait(db[nome][i]);
    },
    remover: (id) => {
      const i = db[nome].findIndex((x) => x.id === Number(id));
      if (i < 0) return naoEncontrado();
      db[nome].splice(i, 1);
      return wait(null);
    },
  };
}

const montar = (c) => ({
  id: c.id,
  titulo: c.titulo,
  descricao: c.descricao,
  dataAbertura: c.dataAbertura,
  dataFechamento: c.dataFechamento,
  prioridade: c.prioridade,
  status: c.status,
  cliente: porId(db.clientes, c.clienteId) || null,
  categoria: porId(db.categorias, c.categoriaId) || null,
  tecnico: c.tecnicoId ? porId(db.tecnicos, c.tecnicoId) || null : null,
});

function paginar(lista, params) {
  const tamanho = Number(params.size) || 10;
  const pagina = Number(params.page) || 0;
  return {
    itens: lista.slice(pagina * tamanho, (pagina + 1) * tamanho),
    pagina,
    tamanho,
    totalItens: lista.length,
    totalPaginas: Math.ceil(lista.length / tamanho),
  };
}

function pesquisar(params = {}) {
  let lista = [...db.chamados];
  if (params.status) lista = lista.filter((c) => c.status === params.status);
  if (params.prioridade) lista = lista.filter((c) => c.prioridade === params.prioridade);
  if (params.clienteId) lista = lista.filter((c) => c.clienteId === Number(params.clienteId));
  lista.sort((a, b) => b.dataAbertura.localeCompare(a.dataAbertura));
  return wait(paginar(lista.map(montar), params));
}

const chamados = {
  listar: pesquisar,
  buscar: (id) => {
    const c = porId(db.chamados, id);
    if (!c) return naoEncontrado();
    const comentarios = db.comentarios
      .filter((x) => x.chamadoId === c.id)
      .sort((a, b) => a.dataCriacao.localeCompare(b.dataCriacao));
    return wait({ ...montar(c), comentarios });
  },
  criar: (dados) => {
    const novo = {
      id: proximo(db.chamados),
      titulo: dados.titulo,
      descricao: dados.descricao,
      dataAbertura: agora(),
      dataFechamento: null,
      prioridade: dados.prioridade,
      status: 'ABERTO',
      clienteId: Number(dados.clienteId),
      categoriaId: Number(dados.categoriaId),
      tecnicoId: dados.tecnicoId ? Number(dados.tecnicoId) : null,
    };
    db.chamados.push(novo);
    return wait(montar(novo));
  },
  atualizar: (id, dados) => {
    const c = porId(db.chamados, id);
    if (!c) return naoEncontrado();
    Object.assign(c, {
      titulo: dados.titulo,
      descricao: dados.descricao,
      prioridade: dados.prioridade,
      clienteId: Number(dados.clienteId),
      categoriaId: Number(dados.categoriaId),
      tecnicoId: dados.tecnicoId ? Number(dados.tecnicoId) : null,
    });
    return wait(montar(c));
  },
  remover: (id) => {
    const i = db.chamados.findIndex((x) => x.id === Number(id));
    if (i < 0) return naoEncontrado();
    db.chamados.splice(i, 1);
    db.comentarios = db.comentarios.filter((x) => x.chamadoId !== Number(id));
    return wait(null);
  },
  atribuir: (id, tecnicoId) => {
    const c = porId(db.chamados, id);
    if (!c) return naoEncontrado();
    if (!porId(db.tecnicos, tecnicoId)) return falha(404, 'Técnico não encontrado');
    c.tecnicoId = Number(tecnicoId);
    return wait(montar(c));
  },
  mudarStatus: (id, status) => {
    const c = porId(db.chamados, id);
    if (!c) return naoEncontrado();
    c.status = status;
    c.dataFechamento = status === 'FECHADO' ? agora() : null;
    return wait(montar(c));
  },
  comentar: (chamadoId, dados) => {
    if (!porId(db.chamados, chamadoId)) return naoEncontrado();
    const novo = { id: proximo(db.comentarios), chamadoId: Number(chamadoId), mensagem: dados.mensagem, autor: dados.autor, dataCriacao: agora() };
    db.comentarios.push(novo);
    return wait(novo);
  },
};

export const mock = {
  categorias: crud('categorias'),
  tecnicos: crud('tecnicos'),
  clientes: {
    ...crud('clientes'),
    chamados: (id, params = {}) =>
      porId(db.clientes, id) ? pesquisar({ ...params, clienteId: id }) : naoEncontrado(),
  },
  chamados,
};
