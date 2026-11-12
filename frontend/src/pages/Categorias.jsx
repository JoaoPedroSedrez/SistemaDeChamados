import CrudPage from './CrudPage.jsx';

const campos = [
  { nome: 'nome', label: 'Nome', obrigatorio: true, max: 100 },
  { nome: 'descricao', label: 'Descrição', max: 255 },
];
const colunas = [
  { chave: 'nome', titulo: 'Nome' },
  { chave: 'descricao', titulo: 'Descrição' },
];

export default function Categorias() {
  return <CrudPage titulo="Categorias" recurso="categorias" campos={campos} colunas={colunas} />;
}
