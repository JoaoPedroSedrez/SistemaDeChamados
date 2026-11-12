import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, mensagemErro } from '../api.js';
import { PRIORIDADES, rotulo } from '../constants.js';
import { Campo, Erro, Seletor, useCarregar } from '../componentes.jsx';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';

const vazio = { titulo: '', descricao: '', prioridade: '', clienteId: '', categoriaId: '', tecnicoId: '' };

export default function ChamadoForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const clientes = useCarregar(() => api.clientes.listar(), []);
  const categorias = useCarregar(() => api.categorias.listar(), []);
  const tecnicos = useCarregar(() => api.tecnicos.listar(), []);

  const [form, setForm] = useState(vazio);
  const [erros, setErros] = useState({});
  const [erroApi, setErroApi] = useState('');

  useEffect(() => {
    if (!id) return;
    api.chamados
      .buscar(id)
      .then((c) =>
        setForm({
          titulo: c.titulo,
          descricao: c.descricao,
          prioridade: c.prioridade,
          clienteId: c.cliente?.id ?? '',
          categoriaId: c.categoria?.id ?? '',
          tecnicoId: c.tecnico?.id ?? '',
        })
      )
      .catch((e) => setErroApi(mensagemErro(e)));
  }, [id]);

  const mudar = (campo) => (valor) => setForm({ ...form, [campo]: valor });

  function validar() {
    const e = {};
    if (!form.titulo.trim()) e.titulo = 'Título é obrigatório';
    else if (form.titulo.length > 150) e.titulo = 'Título deve ter no máximo 150 caracteres';
    if (!form.descricao.trim()) e.descricao = 'Descrição é obrigatória';
    else if (form.descricao.length > 1000) e.descricao = 'Descrição deve ter no máximo 1000 caracteres';
    if (!form.prioridade) e.prioridade = 'Prioridade é obrigatória';
    if (!form.clienteId) e.clienteId = 'Cliente é obrigatório';
    if (!form.categoriaId) e.categoriaId = 'Categoria é obrigatória';
    return e;
  }

  async function salvar(ev) {
    ev.preventDefault();
    const e = validar();
    setErros(e);
    if (Object.keys(e).length) return;
    const corpo = {
      titulo: form.titulo,
      descricao: form.descricao,
      prioridade: form.prioridade,
      clienteId: Number(form.clienteId),
      categoriaId: Number(form.categoriaId),
      tecnicoId: form.tecnicoId ? Number(form.tecnicoId) : null,
    };
    try {
      const salvo = id ? await api.chamados.atualizar(id, corpo) : await api.chamados.criar(corpo);
      navigate(`/chamados/${salvo.id}`);
    } catch (err) {
      setErroApi(mensagemErro(err));
    }
  }

  return (
    <section className="mx-auto max-w-xl">
      <Card>
        <CardHeader>
          <CardTitle className="text-2xl">{id ? `Editar chamado #${id}` : 'Abrir chamado'}</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={salvar} noValidate className="space-y-4">
            <Campo id="titulo" label="Título" erro={erros.titulo}>
              <Input id="titulo" value={form.titulo} onChange={(e) => mudar('titulo')(e.target.value)} />
            </Campo>
            <Campo id="descricao" label="Descrição" erro={erros.descricao}>
              <Textarea id="descricao" rows={5} value={form.descricao} onChange={(e) => mudar('descricao')(e.target.value)} />
            </Campo>
            <Campo id="prioridade" label="Prioridade" erro={erros.prioridade}>
              <Seletor
                id="prioridade"
                value={form.prioridade}
                onChange={mudar('prioridade')}
                opcoes={PRIORIDADES.map((p) => ({ valor: p, rotulo: rotulo(p) }))}
              />
            </Campo>
            <Campo id="clienteId" label="Cliente" erro={erros.clienteId}>
              <Seletor
                id="clienteId"
                value={form.clienteId}
                onChange={mudar('clienteId')}
                opcoes={(clientes.dados || []).map((c) => ({ valor: c.id, rotulo: c.nome }))}
              />
            </Campo>
            <Campo id="categoriaId" label="Categoria" erro={erros.categoriaId}>
              <Seletor
                id="categoriaId"
                value={form.categoriaId}
                onChange={mudar('categoriaId')}
                opcoes={(categorias.dados || []).map((c) => ({ valor: c.id, rotulo: c.nome }))}
              />
            </Campo>
            <Campo id="tecnicoId" label="Técnico (opcional)">
              <Seletor
                id="tecnicoId"
                value={form.tecnicoId}
                onChange={mudar('tecnicoId')}
                vazio="Sem técnico"
                opcoes={(tecnicos.dados || []).map((t) => ({ valor: t.id, rotulo: t.nome }))}
              />
            </Campo>
            <Erro msg={erroApi} />
            <div className="flex gap-2">
              <Button type="submit">Salvar</Button>
              <Button asChild variant="outline">
                <Link to={id ? `/chamados/${id}` : '/chamados'}>Cancelar</Link>
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </section>
  );
}
