import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api, mensagemErro } from '../api.js';
import { PRIORIDADES, STATUS, rotulo } from '../constants.js';
import { Campo, Confirmar, Erro, Paginacao, Seletor, TabelaChamados, useCarregar } from '../componentes.jsx';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

export default function Chamados() {
  const [filtros, setFiltros] = useState({ status: '', prioridade: '', clienteId: '' });
  const [pagina, setPagina] = useState(0);
  const [excluindo, setExcluindo] = useState(null);
  const [erroAcao, setErroAcao] = useState('');

  const clientes = useCarregar(() => api.clientes.listar(), []);
  const { dados, erro, carregando, recarregar } = useCarregar(
    () => api.chamados.listar({ ...filtros, page: pagina, size: 10 }),
    [filtros, pagina]
  );

  function mudarFiltro(campo, valor) {
    setFiltros({ ...filtros, [campo]: valor });
    setPagina(0);
  }

  async function confirmarRemocao() {
    const c = excluindo;
    setExcluindo(null);
    setErroAcao('');
    try {
      await api.chamados.remover(c.id);
      recarregar();
    } catch (e) {
      setErroAcao(mensagemErro(e));
    }
  }

  return (
    <section className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Chamados</h1>
        <Button asChild>
          <Link to="/chamados/novo">Abrir chamado</Link>
        </Button>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <Campo id="filtro-status" label="Status">
          <Seletor
            id="filtro-status"
            value={filtros.status}
            onChange={(v) => mudarFiltro('status', v)}
            vazio="Todos"
            opcoes={STATUS.map((s) => ({ valor: s, rotulo: rotulo(s) }))}
          />
        </Campo>
        <Campo id="filtro-prioridade" label="Prioridade">
          <Seletor
            id="filtro-prioridade"
            value={filtros.prioridade}
            onChange={(v) => mudarFiltro('prioridade', v)}
            vazio="Todas"
            opcoes={PRIORIDADES.map((p) => ({ valor: p, rotulo: rotulo(p) }))}
          />
        </Campo>
        <Campo id="filtro-cliente" label="Cliente">
          <Seletor
            id="filtro-cliente"
            value={filtros.clienteId}
            onChange={(v) => mudarFiltro('clienteId', v)}
            vazio="Todos"
            opcoes={(clientes.dados || []).map((c) => ({ valor: c.id, rotulo: c.nome }))}
          />
        </Campo>
      </div>

      <Erro msg={erro || erroAcao} />
      {carregando && <p className="text-muted-foreground">Carregando...</p>}
      {dados && (
        <>
          <Card>
            <CardContent className="px-2">
              <TabelaChamados itens={dados.itens} onRemover={setExcluindo} />
            </CardContent>
          </Card>
          <Paginacao pagina={dados.pagina} totalPaginas={dados.totalPaginas} totalItens={dados.totalItens} onMudar={setPagina} />
        </>
      )}

      <Confirmar
        aberto={excluindo !== null}
        titulo="Excluir chamado"
        descricao={`Deseja excluir o chamado #${excluindo?.id ?? ''}? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarRemocao}
        onCancelar={() => setExcluindo(null)}
      />
    </section>
  );
}
