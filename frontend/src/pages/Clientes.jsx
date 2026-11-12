import { Link } from 'react-router-dom';
import CrudPage from './CrudPage.jsx';
import { Button } from '@/components/ui/button';

const campos = [
  { nome: 'nome', label: 'Nome', obrigatorio: true, max: 100 },
  { nome: 'email', label: 'E-mail', tipo: 'email', obrigatorio: true },
  { nome: 'telefone', label: 'Telefone', max: 20 },
];
const colunas = [
  { chave: 'nome', titulo: 'Nome' },
  { chave: 'email', titulo: 'E-mail' },
  { chave: 'telefone', titulo: 'Telefone' },
];

export default function Clientes() {
  return (
    <CrudPage
      titulo="Clientes"
      recurso="clientes"
      campos={campos}
      colunas={colunas}
      extras={(c) => (
        <Button asChild variant="secondary" size="sm">
          <Link to={`/clientes/${c.id}/chamados`}>Chamados</Link>
        </Button>
      )}
    />
  );
}
