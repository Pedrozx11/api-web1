const formFiltros = document.getElementById('filtros');
const corpo = document.getElementById('corpo');
const contagem = document.getElementById('contagem');
const aviso = document.getElementById('aviso');

function celula(texto) {
  const td = document.createElement('td');
  td.textContent = texto ?? '';
  return td;
}

function montarLinha(n) {
  const tr = document.createElement('tr');
  tr.appendChild(celula(n.id));
  tr.appendChild(celula(`${n.agravo} (${n.codigoCid10})`));
  tr.appendChild(celula(n.nomePaciente));
  tr.appendChild(celula(formatarData(n.dataNascimento)));
  tr.appendChild(celula(n.nomeMae));
  tr.appendChild(celula(formatarData(n.dataNotificacao)));
  tr.appendChild(celula(`${n.municipioNotificacao}/${n.ufNotificacao}`));
  tr.appendChild(celula(ROTULOS.classificacaoFinal[n.classificacaoFinal] || '-'));
  tr.appendChild(celula(ROTULOS.evolucao[n.evolucao] || '-'));

  const tdAlterar = document.createElement('td');
  tdAlterar.className = 'acoes';
  const editar = document.createElement('a');
  editar.className = 'botao secundario pequeno';
  editar.href = `cadastro.html?id=${n.id}`;
  editar.textContent = 'Alterar';
  editar.setAttribute('aria-label', `Alterar notificação ${n.id}`);
  tdAlterar.appendChild(editar);
  tr.appendChild(tdAlterar);

  const tdExcluir = document.createElement('td');
  tdExcluir.className = 'acoes';
  const excluir = document.createElement('button');
  excluir.type = 'button';
  excluir.className = 'perigo';
  excluir.textContent = 'Excluir';
  excluir.setAttribute('aria-label', `Excluir notificação ${n.id}`);
  excluir.addEventListener('click', () => excluirNotificacao(n));
  tdExcluir.appendChild(excluir);
  tr.appendChild(tdExcluir);

  return tr;
}

function mostrarProblema(problema) {
  const lista = (problema.erros || []).map((e) => `${e.campo}: ${e.mensagem}`);
  mostrarAviso(aviso, 'erro', problema.detail || problema.title || 'Erro ao consultar.', lista);
}

async function carregar() {
  const params = new URLSearchParams();
  new FormData(formFiltros).forEach((valor, chave) => {
    if (String(valor).trim() !== '') params.append(chave, String(valor).trim());
  });
  const consulta = params.toString();

  try {
    const lista = await requisitar(consulta ? `${API}?${consulta}` : API);
    aviso.hidden = true;
    corpo.replaceChildren();
    if (lista.length === 0) {
      const tr = document.createElement('tr');
      const td = celula('Nenhuma notificação encontrada para os filtros informados.');
      td.colSpan = 11;
      td.className = 'vazio';
      tr.appendChild(td);
      corpo.appendChild(tr);
    } else {
      lista.forEach((n) => corpo.appendChild(montarLinha(n)));
    }
    contagem.textContent = `${lista.length} notificação(ões) encontrada(s).`;
  } catch (problema) {
    mostrarProblema(problema);
  }
}

async function excluirNotificacao(n) {
  const ok = confirm(`Excluir a notificação ${n.id} de "${n.nomePaciente}" (${n.agravo})?\nEsta ação não pode ser desfeita.`);
  if (!ok) return;
  try {
    await requisitar(`${API}/${n.id}`, { method: 'DELETE' });
    await carregar();
    mostrarAviso(aviso, 'ok', `Notificação ${n.id} excluída.`);
  } catch (problema) {
    mostrarProblema(problema);
  }
}

formFiltros.addEventListener('submit', (e) => {
  e.preventDefault();
  carregar();
});

document.getElementById('limpar').addEventListener('click', () => {
  formFiltros.reset();
  carregar();
});

preencherUFs();
carregar();
