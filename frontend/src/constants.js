export const STATUS = ['ABERTO', 'EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE', 'FECHADO'];
export const PRIORIDADES = ['BAIXA', 'MEDIA', 'ALTA', 'CRITICA'];

export const PERFIS = ['ADMIN', 'TECNICO'];

export const rotulo = (valor) => (valor ? String(valor).replaceAll('_', ' ') : '');
