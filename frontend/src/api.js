import axios from 'axios';
import { mock } from './mock.js';

const USE_MOCK = false;

const http = axios.create({ baseURL: '/api' });
const dados = (promessa) => promessa.then((r) => r.data);

const limpar = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null));

function crud(caminho) {
  return {
    listar: () => dados(http.get(caminho)),
    buscar: (id) => dados(http.get(`${caminho}/${id}`)),
    criar: (obj) => dados(http.post(caminho, obj)),
    atualizar: (id, obj) => dados(http.put(`${caminho}/${id}`, obj)),
    remover: (id) => dados(http.delete(`${caminho}/${id}`)),
  };
}

const real = {
  categorias: crud('/categorias'),
  tecnicos: crud('/tecnicos'),
  clientes: {
    ...crud('/clientes'),
    chamados: (id, params) => dados(http.get(`/clientes/${id}/chamados`, { params: limpar(params) })),
  },
  chamados: {
    ...crud('/chamados'),
    listar: (params) => dados(http.get('/chamados', { params: limpar(params) })),
    atribuir: (id, tecnicoId) => dados(http.patch(`/chamados/${id}/atribuir`, { tecnicoId: Number(tecnicoId) })),
    mudarStatus: (id, status) => dados(http.patch(`/chamados/${id}/status`, { status })),
    comentar: (chamadoId, corpo) => dados(http.post(`/chamados/${chamadoId}/comentarios`, corpo)),
  },
};

export const api = USE_MOCK ? mock : real;

export function mensagemErro(e) {
  const corpo = e?.response?.data;
  if (corpo?.detalhes?.length) return corpo.detalhes.join(' • ');
  return corpo?.erro || e?.message || 'Erro inesperado';
}
