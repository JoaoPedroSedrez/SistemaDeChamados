import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { mensagemErro } from './api.js';
import { rotulo } from './constants.js';
import { Alert, AlertDescription } from '@/components/ui/alert';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';

export const formatarData = (d) => (d ? new Date(d).toLocaleString('pt-BR') : '-');

export function Erro({ msg }) {
  if (!msg) return null;
  return (
    <Alert variant="destructive">
      <AlertDescription>{msg}</AlertDescription>
    </Alert>
  );
}

export function Campo({ id, label, erro, children }) {
  return (
    <div className="grid gap-2">
      <Label htmlFor={id}>{label}</Label>
      {children}
      {erro && <p className="text-sm text-destructive">{erro}</p>}
    </div>
  );
}

const VAZIO = '__vazio__';
export function Seletor({ id, value, onChange, opcoes, placeholder = 'Selecione', vazio }) {
  const atual = value === '' || value == null ? (vazio ? VAZIO : '') : String(value);
  return (
    <Select value={atual} onValueChange={(v) => onChange(v === VAZIO ? '' : v)}>
      <SelectTrigger id={id} className="w-full">
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        {vazio && <SelectItem value={VAZIO}>{vazio}</SelectItem>}
        {opcoes.map((o) => (
          <SelectItem key={o.valor} value={String(o.valor)}>{o.rotulo}</SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}

export function Confirmar({ aberto, titulo, descricao, onConfirmar, onCancelar }) {
  return (
    <AlertDialog open={aberto} onOpenChange={(o) => !o && onCancelar()}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{titulo}</AlertDialogTitle>
          <AlertDialogDescription>{descricao}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancelar</AlertDialogCancel>
          <AlertDialogAction onClick={onConfirmar}>Excluir</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

export function BadgeStatus({ status }) {
  return <Badge variant="outline">{rotulo(status)}</Badge>;
}

export function BadgePrioridade({ prioridade }) {
  const variante = prioridade === 'CRITICA' ? 'destructive' : prioridade === 'ALTA' ? 'default' : 'secondary';
  return <Badge variant={variante}>{rotulo(prioridade)}</Badge>;
}

export function useCarregar(fn, deps) {
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [tick, setTick] = useState(0);

  useEffect(() => {
    let vivo = true;
    setCarregando(true);
    setErro('');
    fn()
      .then((d) => vivo && setDados(d))
      .catch((e) => vivo && setErro(mensagemErro(e)))
      .finally(() => vivo && setCarregando(false));
    return () => {
      vivo = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, tick]);

  return { dados, erro, carregando, recarregar: () => setTick((t) => t + 1) };
}

export function Paginacao({ pagina, totalPaginas, totalItens, onMudar }) {
  if (!totalPaginas) return null;
  return (
    <div className="mt-4 flex flex-wrap items-center gap-3">
      <Button variant="outline" size="sm" disabled={pagina <= 0} onClick={() => onMudar(pagina - 1)}>
        Anterior
      </Button>
      <span className="text-sm text-muted-foreground">
        Página {pagina + 1} de {totalPaginas} ({totalItens} itens)
      </span>
      <Button variant="outline" size="sm" disabled={pagina + 1 >= totalPaginas} onClick={() => onMudar(pagina + 1)}>
        Próxima
      </Button>
    </div>
  );
}

export function TabelaChamados({ itens, onRemover }) {
  if (!itens.length) return <p className="text-muted-foreground">Nenhum chamado encontrado.</p>;
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>ID</TableHead>
          <TableHead>Título</TableHead>
          <TableHead>Cliente</TableHead>
          <TableHead>Categoria</TableHead>
          <TableHead>Prioridade</TableHead>
          <TableHead>Status</TableHead>
          <TableHead>Técnico</TableHead>
          <TableHead>Abertura</TableHead>
          {onRemover && <TableHead>Ações</TableHead>}
        </TableRow>
      </TableHeader>
      <TableBody>
        {itens.map((c) => (
          <TableRow key={c.id}>
            <TableCell>{c.id}</TableCell>
            <TableCell>
              <Link to={`/chamados/${c.id}`} className="font-medium underline-offset-4 hover:underline">{c.titulo}</Link>
            </TableCell>
            <TableCell>{c.cliente?.nome}</TableCell>
            <TableCell>{c.categoria?.nome}</TableCell>
            <TableCell><BadgePrioridade prioridade={c.prioridade} /></TableCell>
            <TableCell><BadgeStatus status={c.status} /></TableCell>
            <TableCell>{c.tecnico?.nome || '-'}</TableCell>
            <TableCell>{formatarData(c.dataAbertura)}</TableCell>
            {onRemover && (
              <TableCell>
                <div className="flex gap-2">
                  <Button asChild variant="outline" size="sm">
                    <Link to={`/chamados/${c.id}/editar`}>Editar</Link>
                  </Button>
                  <Button variant="destructive" size="sm" onClick={() => onRemover(c)}>Excluir</Button>
                </div>
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
