const API = '/notificacao';

const UFS = ['AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'];

const ROTULOS = {
  classificacaoFinal: { 1: 'Confirmado', 2: 'Descartado' },
  evolucao: { 1: 'Cura', 2: 'Óbito pelo agravo', 3: 'Óbito por outras causas', 9: 'Ignorado' },
};

/** Preenche todos os <select data-uf> com as UFs. */
function preencherUFs() {
  document.querySelectorAll('select[data-uf]').forEach((select) => {
    select.innerHTML = '<option value="">Todas</option>'
      + UFS.map((uf) => `<option value="${uf}">${uf}</option>`).join('');
    if (select.dataset.uf === 'form') {
      select.firstElementChild.textContent = 'Selecione';
    }
  });
}

/** 2026-03-12 -> 12/03/2026 (sem passar por Date, evitando problemas de fuso). */
function formatarData(iso) {
  if (!iso) return '';
  const [a, m, d] = iso.split('-');
  return `${d}/${m}/${a}`;
}

/**
 * Chama a API. Em caso de sucesso devolve o JSON (ou null no 204).
 * Em caso de erro lança o objeto Problem Detail devolvido pela API.
 */
async function requisitar(url, opcoes = {}) {
  let resposta;
  try {
    resposta = await fetch(url, {
      ...opcoes,
      headers: { Accept: 'application/json', ...(opcoes.body ? { 'Content-Type': 'application/json' } : {}) },
    });
  } catch (e) {
    throw { title: 'Sem conexão', detail: 'Não foi possível se comunicar com o servidor.' };
  }
  if (resposta.ok) {
    return resposta.status === 204 ? null : resposta.json();
  }
  let problema;
  try {
    problema = await resposta.json();
  } catch (e) {
    problema = { title: 'Erro', detail: `Falha na requisição (HTTP ${resposta.status}).` };
  }
  problema.status = problema.status || resposta.status;
  throw problema;
}

/** Mostra um aviso (erro ou sucesso) dentro de um elemento. */
function mostrarAviso(elemento, tipo, mensagem, lista = []) {
  elemento.className = `aviso ${tipo}`;
  elemento.textContent = '';
  const p = document.createElement('strong');
  p.textContent = mensagem;
  elemento.appendChild(p);
  if (lista.length) {
    const ul = document.createElement('ul');
    lista.forEach((texto) => {
      const li = document.createElement('li');
      li.textContent = texto;
      ul.appendChild(li);
    });
    elemento.appendChild(ul);
  }
  elemento.hidden = false;
  elemento.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}
