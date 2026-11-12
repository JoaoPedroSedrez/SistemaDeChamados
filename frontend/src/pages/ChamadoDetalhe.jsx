import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, mensagemErro } from '../api.js';
import { STATUS, rotulo } from '../constants.js';
import {
  BadgePrioridade,
  BadgeStatus,
  Campo,
  Confirmar,
  Erro,
  Seletor,
  formatarData,
  useCarregar,
} from '../componentes.jsx';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';

export default function ChamadoDetalhe() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { dados: c, erro, carregando, recarregar } = useCarregar(() => api.chamados.buscar(id), [id]);
  const tecnicos = useCarregar(() => api.tecnicos.listar(), []);

  const [tecnicoId, setTecnicoId] = useState('');
  const [status, setStatus] = useState('');
  const [comentario, setComentario] = useState({ mensagem: '', autor: '' });
  const [erroAcao, setErroAcao] = useState('');
  const [excluindo, setExcluindo] = useState(false);

  useEffect(() => {
    if (c) {
      setTecnicoId(c.tecnico?.id ?? '');
      setStatus(c.status);
    }
  }, [c]);

  async function executar(acao) {
    setErroAcao('');
    try {
      await acao();
      recarregar();
    } catch (e) {
      setErroAcao(mensagemErro(e));
    }
  }

  const atribuir = () => {
    if (!tecnicoId) return setErroAcao('Técnico é obrigatório');
    return executar(() => api.chamados.atribuir(id, tecnicoId));
  };

  const mudarStatus = () => executar(() => api.chamados.mudarStatus(id, status));

  const comentar = (ev) => {
    ev.preventDefault();
    if (!comentario.mensagem.trim()) return setErroAcao('Mensagem é obrigatória');
    if (!comentario.autor.trim()) return setErroAcao('Autor é obrigatório');
    return executar(async () => {
      await api.chamados.comentar(id, comentario);
      setComentario({ mensagem: '', autor: '' });
    });
  };

  async function remover() {
    setExcluindo(false);
    try {
      await api.chamados.remover(id);
      navigate('/chamados');
    } catch (e) {
      setErroAcao(mensagemErro(e));
    }
  }

  if (carregando && !c) return <p className="text-muted-foreground">Carregando...</p>;
  if (erro && !c) return <Erro msg={erro} />;
  if (!c) return null;

  return (
    <section className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Chamado #{c.id}: {c.titulo}</h1>
        <div className="flex gap-2">
          <Button asChild variant="outline">
            <Link to={`/chamados/${c.id}/editar`}>Editar</Link>
          </Button>
          <Button variant="destructive" onClick={() => setExcluindo(true)}>Excluir</Button>
        </div>
      </div>

      <Card>
        <CardContent>
          <dl className="grid grid-cols-[max-content_1fr] items-center gap-x-6 gap-y-2">
            <dt className="font-medium">Descrição</dt><dd>{c.descricao}</dd>
            <dt className="font-medium">Status</dt><dd><BadgeStatus status={c.status} /></dd>
            <dt className="font-medium">Prioridade</dt><dd><BadgePrioridade prioridade={c.prioridade} /></dd>
            <dt className="font-medium">Cliente</dt>
            <dd>
              {c.cliente ? (
                <Link to={`/clientes/${c.cliente.id}/chamados`} className="underline underline-offset-4">{c.cliente.nome}</Link>
              ) : '-'}
            </dd>
            <dt className="font-medium">Categoria</dt><dd>{c.categoria?.nome || '-'}</dd>
            <dt className="font-medium">Técnico</dt><dd>{c.tecnico?.nome || 'Não atribuído'}</dd>
            <dt className="font-medium">Abertura</dt><dd>{formatarData(c.dataAbertura)}</dd>
            <dt className="font-medium">Fechamento</dt><dd>{formatarData(c.dataFechamento)}</dd>
          </dl>
        </CardContent>
      </Card>

      <Erro msg={erroAcao} />

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Atribuir técnico</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <Seletor
              id="atribuir-tecnico"
              value={tecnicoId}
              onChange={setTecnicoId}
              opcoes={(tecnicos.dados || []).map((t) => ({ valor: t.id, rotulo: t.nome }))}
            />
            <Button onClick={atribuir}>Atribuir</Button>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Mudar status</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <Seletor
              id="mudar-status"
              value={status}
              onChange={setStatus}
              opcoes={STATUS.map((s) => ({ valor: s, rotulo: rotulo(s) }))}
            />
            <Button onClick={mudarStatus}>Salvar status</Button>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader><CardTitle>Comentários</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          {!c.comentarios.length && <p className="text-muted-foreground">Nenhum comentário ainda.</p>}
          <ul className="space-y-3">
            {c.comentarios.map((m) => (
              <li key={m.id} className="rounded-md border p-3">
                <p className="text-sm text-muted-foreground">
                  <strong className="text-foreground">{m.autor}</strong> em {formatarData(m.dataCriacao)}
                </p>
                <p className="mt-1 whitespace-pre-wrap">{m.mensagem}</p>
              </li>
            ))}
          </ul>

          <form onSubmit={comentar} noValidate className="max-w-xl space-y-3 border-t pt-4">
            <Campo id="comentario-autor" label="Autor">
              <Input
                id="comentario-autor"
                maxLength={100}
                value={comentario.autor}
                onChange={(e) => setComentario({ ...comentario, autor: e.target.value })}
              />
            </Campo>
            <Campo id="comentario-mensagem" label="Mensagem">
              <Textarea
                id="comentario-mensagem"
                rows={3}
                maxLength={1000}
                value={comentario.mensagem}
                onChange={(e) => setComentario({ ...comentario, mensagem: e.target.value })}
              />
            </Campo>
            <Button type="submit">Comentar</Button>
          </form>
        </CardContent>
      </Card>

      <Confirmar
        aberto={excluindo}
        titulo="Excluir chamado"
        descricao={`Deseja excluir o chamado #${c.id}? Esta ação não pode ser desfeita.`}
        onConfirmar={remover}
        onCancelar={() => setExcluindo(false)}
      />
    </section>
  );
}
