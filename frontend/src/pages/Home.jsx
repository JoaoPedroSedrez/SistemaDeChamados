import { Link } from 'react-router-dom';
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const modulos = [
  { to: '/chamados', titulo: 'Chamados', texto: 'Listar, filtrar, abrir e acompanhar chamados.' },
  { to: '/chamados/novo', titulo: 'Abrir chamado', texto: 'Registrar um novo chamado.' },
  { to: '/clientes', titulo: 'Clientes', texto: 'Cadastro de clientes e seus chamados.' },
  { to: '/tecnicos', titulo: 'Técnicos', texto: 'Cadastro de técnicos.' },
  { to: '/categorias', titulo: 'Categorias', texto: 'Categorias dos chamados.' },
];

export default function Home() {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">Sistema de chamados</h1>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {modulos.map((m) => (
          <Link key={m.to} to={m.to} className="block">
            <Card className="h-full transition-colors hover:border-primary">
              <CardHeader>
                <CardTitle>{m.titulo}</CardTitle>
                <CardDescription>{m.texto}</CardDescription>
              </CardHeader>
            </Card>
          </Link>
        ))}
      </div>
    </section>
  );
}
