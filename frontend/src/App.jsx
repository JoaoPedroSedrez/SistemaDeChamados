import { NavLink, Route, Routes } from 'react-router-dom';
import Home from './pages/Home.jsx';
import Categorias from './pages/Categorias.jsx';
import Clientes from './pages/Clientes.jsx';
import Tecnicos from './pages/Tecnicos.jsx';
import Chamados from './pages/Chamados.jsx';
import ChamadoForm from './pages/ChamadoForm.jsx';
import ChamadoDetalhe from './pages/ChamadoDetalhe.jsx';
import ClienteChamados from './pages/ClienteChamados.jsx';

const links = [
  { to: '/', texto: 'Início', end: true },
  { to: '/chamados', texto: 'Chamados' },
  { to: '/clientes', texto: 'Clientes' },
  { to: '/tecnicos', texto: 'Técnicos' },
  { to: '/categorias', texto: 'Categorias' },
];

export default function App() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b">
        <nav className="mx-auto flex max-w-6xl flex-wrap items-center gap-1 p-3">
          {links.map((l) => (
            <NavLink
              key={l.to}
              to={l.to}
              end={l.end}
              className={({ isActive }) =>
                `rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                  isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-accent'
                }`
              }
            >
              {l.texto}
            </NavLink>
          ))}
        </nav>
      </header>
      <main className="mx-auto max-w-6xl p-4 md:p-6">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/categorias" element={<Categorias />} />
          <Route path="/clientes" element={<Clientes />} />
          <Route path="/clientes/:id/chamados" element={<ClienteChamados />} />
          <Route path="/tecnicos" element={<Tecnicos />} />
          <Route path="/chamados" element={<Chamados />} />
          <Route path="/chamados/novo" element={<ChamadoForm />} />
          <Route path="/chamados/:id" element={<ChamadoDetalhe />} />
          <Route path="/chamados/:id/editar" element={<ChamadoForm />} />
          <Route path="*" element={<p>Página não encontrada.</p>} />
        </Routes>
      </main>
    </div>
  );
}
