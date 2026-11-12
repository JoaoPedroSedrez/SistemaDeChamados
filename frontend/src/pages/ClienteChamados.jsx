import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { Erro, Paginacao, TabelaChamados, useCarregar } from '../componentes.jsx';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

export default function ClienteChamados() {
  const { id } = useParams();
  const [pagina, setPagina] = useState(0);
  const cliente = useCarregar(() => api.clientes.buscar(id), [id]);
  const { dados, erro, carregando } = useCarregar(() => api.clientes.chamados(id, { page: pagina, size: 10 }), [id, pagina]);

  return (
    <section className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Chamados de {cliente.dados?.nome || `cliente #${id}`}</h1>
        <Button asChild variant="outline">
          <Link to="/clientes">Voltar para clientes</Link>
        </Button>
      </div>
      <Erro msg={erro || cliente.erro} />
      {carregando && <p className="text-muted-foreground">Carregando...</p>}
      {dados && (
        <>
          <Card>
            <CardContent className="px-2">
              <TabelaChamados itens={dados.itens} />
            </CardContent>
          </Card>
          <Paginacao pagina={dados.pagina} totalPaginas={dados.totalPaginas} totalItens={dados.totalItens} onMudar={setPagina} />
        </>
      )}
    </section>
  );
}
