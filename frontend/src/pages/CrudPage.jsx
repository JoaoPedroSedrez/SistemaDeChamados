import { useState } from 'react';
import { api, mensagemErro } from '../api.js';
import { Campo, Confirmar, Erro, Seletor, useCarregar } from '../componentes.jsx';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';

export default function CrudPage({ titulo, recurso, campos, colunas, extras }) {
  const { dados, erro, carregando, recarregar } = useCarregar(() => api[recurso].listar(), [recurso]);
  const [editando, setEditando] = useState(null);
  const [excluindo, setExcluindo] = useState(null);
  const [form, setForm] = useState({});
  const [erros, setErros] = useState({});
  const [erroApi, setErroApi] = useState('');
  const [erroLista, setErroLista] = useState('');

  function abrir(item) {
    setEditando(item || {});
    setForm(Object.fromEntries(campos.map((c) => [c.nome, item?.[c.nome] ?? ''])));
    setErros({});
    setErroApi('');
  }

  function validar() {
    const e = {};
    for (const c of campos) {
      const v = String(form[c.nome] ?? '').trim();
      if (c.obrigatorio && !v) e[c.nome] = `${c.label} é obrigatório`;
      else if (c.max && v.length > c.max) e[c.nome] = `${c.label} deve ter no máximo ${c.max} caracteres`;
      else if (c.tipo === 'email' && v && !/^\S+@\S+\.\S+$/.test(v)) e[c.nome] = 'E-mail inválido';
    }
    return e;
  }

  async function salvar(ev) {
    ev.preventDefault();
    const e = validar();
    setErros(e);
    if (Object.keys(e).length) return;
    const corpo = Object.fromEntries(campos.map((c) => [c.nome, form[c.nome] === '' ? null : form[c.nome]]));
    try {
      if (editando.id) await api[recurso].atualizar(editando.id, corpo);
      else await api[recurso].criar(corpo);
      setEditando(null);
      recarregar();
    } catch (err) {
      setErroApi(mensagemErro(err));
    }
  }

  async function confirmarRemocao() {
    const item = excluindo;
    setExcluindo(null);
    setErroLista('');
    try {
      await api[recurso].remover(item.id);
      recarregar();
    } catch (err) {
      setErroLista(mensagemErro(err));
    }
  }

  return (
    <section className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">{titulo}</h1>
        <Button onClick={() => abrir(null)}>Novo</Button>
      </div>

      <Erro msg={erro || erroLista} />
      {carregando && <p className="text-muted-foreground">Carregando...</p>}
      {dados && !dados.length && <p className="text-muted-foreground">Nenhum registro cadastrado.</p>}
      {dados && dados.length > 0 && (
        <Card>
          <CardContent className="px-2">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  {colunas.map((c) => <TableHead key={c.chave}>{c.titulo}</TableHead>)}
                  <TableHead>Ações</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>{item.id}</TableCell>
                    {colunas.map((c) => (
                      <TableCell key={c.chave}>{c.render ? c.render(item) : item[c.chave] ?? '-'}</TableCell>
                    ))}
                    <TableCell>
                      <div className="flex flex-wrap gap-2">
                        {extras && extras(item)}
                        <Button variant="outline" size="sm" onClick={() => abrir(item)}>Editar</Button>
                        <Button variant="destructive" size="sm" onClick={() => setExcluindo(item)}>Excluir</Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      <Dialog open={editando !== null} onOpenChange={(aberto) => !aberto && setEditando(null)}>
        <DialogContent>
          <form onSubmit={salvar} noValidate className="space-y-4">
            <DialogHeader>
              <DialogTitle>{editando?.id ? `Editar #${editando.id}` : 'Novo registro'}</DialogTitle>
              <DialogDescription>{titulo}</DialogDescription>
            </DialogHeader>
            {campos.map((c) => (
              <Campo key={c.nome} id={`campo-${c.nome}`} label={c.label} erro={erros[c.nome]}>
                {c.tipo === 'select' ? (
                  <Seletor
                    id={`campo-${c.nome}`}
                    value={form[c.nome]}
                    onChange={(v) => setForm({ ...form, [c.nome]: v })}
                    opcoes={c.opcoes.map((o) => ({ valor: o, rotulo: o }))}
                  />
                ) : (
                  <Input
                    id={`campo-${c.nome}`}
                    type={c.tipo === 'email' ? 'email' : 'text'}
                    value={form[c.nome] ?? ''}
                    onChange={(e) => setForm({ ...form, [c.nome]: e.target.value })}
                  />
                )}
              </Campo>
            ))}
            <Erro msg={erroApi} />
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setEditando(null)}>Cancelar</Button>
              <Button type="submit">Salvar</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Confirmar
        aberto={excluindo !== null}
        titulo="Excluir registro"
        descricao={`Deseja excluir o registro ${excluindo?.id ?? ''}? Esta ação não pode ser desfeita.`}
        onConfirmar={confirmarRemocao}
        onCancelar={() => setExcluindo(null)}
      />
    </section>
  );
}
