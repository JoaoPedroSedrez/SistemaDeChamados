import CrudPage from './CrudPage.jsx';
import { PERFIS } from '../constants.js';

const campos = [
  { nome: 'nome', label: 'Nome', obrigatorio: true, max: 100 },
  { nome: 'email', label: 'E-mail', tipo: 'email', obrigatorio: true, max: 120 },
  { nome: 'perfil', label: 'Perfil', tipo: 'select', obrigatorio: true, opcoes: PERFIS },
];
const colunas = [
  { chave: 'nome', titulo: 'Nome' },
  { chave: 'email', titulo: 'E-mail' },
  { chave: 'perfil', titulo: 'Perfil' },
];

export default function Tecnicos() {
  return <CrudPage titulo="Técnicos" recurso="tecnicos" campos={campos} colunas={colunas} />;
}
