const form = document.getElementById('ficha');
const aviso = document.getElementById('aviso');
const titulo = document.getElementById('titulo');
const checkExterior = document.getElementById('exterior');
const campoPais = document.getElementById('campo-pais');
const campoUf = document.getElementById('campo-uf');
const campoMunicipio = document.getElementById('campo-municipio');
const campoIbge = document.getElementById('campo-ibge');

const id = new URLSearchParams(location.search).get('id');

/** Campos do formulário que correspondem a propriedades da API. */
function camposNomeados() {
  return [...form.elements].filter((el) => el.name);
}

/** Monta o JSON enviado à API: vazio vira null e campos data-int viram número. */
function coletar() {
  const dados = {};
  camposNomeados().forEach((el) => {
    const texto = el.value.trim();
    if (texto === '') {
      dados[el.name] = null;
    } else {
      dados[el.name] = el.hasAttribute('data-int') ? Number(texto) : texto;
    }
  });
  return dados;
}

function preencher(dados) {
  camposNomeados().forEach((el) => {
    el.value = dados[el.name] ?? '';
  });
  checkExterior.checked = Boolean(dados.paisResidencia);
  alternarResidencia();
}

/** Residente no Brasil: UF/município. Residente fora: apenas país (campos 17, 18 e 30). */
function alternarResidencia() {
  const exterior = checkExterior.checked;
  campoPais.hidden = !exterior;
  [campoUf, campoMunicipio, campoIbge].forEach((c) => { c.hidden = exterior; });
  if (exterior) {
    form.ufResidencia.value = '';
    form.municipioResidencia.value = '';
    form.codigoIbgeMunicipioResidencia.value = '';
  } else {
    form.paisResidencia.value = '';
  }
}

function limparErros() {
  aviso.hidden = true;
  form.querySelectorAll('.invalido').forEach((l) => l.classList.remove('invalido'));
  form.querySelectorAll('small.erro').forEach((s) => s.remove());
}

function marcarErro(campo, mensagem) {
  const el = form.elements[campo];
  const rotulo = el ? el.closest('label') : null;
  if (!rotulo) return;
  rotulo.classList.add('invalido');
  const small = document.createElement('small');
  small.className = 'erro';
  small.textContent = mensagem;
  rotulo.appendChild(small);
}

function tratarProblema(problema) {
  const erros = problema.erros || [];
  erros.forEach((e) => marcarErro(e.campo, e.mensagem));
  const semCampo = erros.filter((e) => !form.elements[e.campo]).map((e) => `${e.campo}: ${e.mensagem}`);
  mostrarAviso(aviso, 'erro', problema.detail || problema.title || 'Não foi possível salvar.', semCampo);
  const primeiro = form.querySelector('.invalido input, .invalido select, .invalido textarea');
  if (primeiro) primeiro.focus();
}

async function salvar(evento) {
  evento.preventDefault();
  limparErros();
  const botao = document.getElementById('salvar');
  botao.disabled = true;
  try {
    const dados = coletar();
    if (id) {
      await requisitar(`${API}/${id}`, { method: 'PUT', body: JSON.stringify(dados) });
      location.href = 'index.html';
      return;
    }
    const criada = await requisitar(API, { method: 'POST', body: JSON.stringify(dados) });
    form.reset();
    alternarResidencia();
    mostrarAviso(aviso, 'ok', `Notificação ${criada.id} cadastrada com sucesso.`);
  } catch (problema) {
    tratarProblema(problema);
  } finally {
    botao.disabled = false;
  }
}

/** Sexo masculino: gestante é "6 - Não se aplica" (instruções do campo 12). */
form.sexo.addEventListener('change', () => {
  if (form.sexo.value === 'M' && form.gestante.value === '') {
    form.gestante.value = '6';
  }
});

checkExterior.addEventListener('change', alternarResidencia);
form.addEventListener('submit', salvar);

async function iniciar() {
  preencherUFs();
  alternarResidencia();
  if (!id) return;

  titulo.textContent = `Alterar notificação ${id}`;
  document.title = `Alterar notificação ${id} - SINAN`;
  try {
    preencher(await requisitar(`${API}/${id}`));
  } catch (problema) {
    mostrarAviso(aviso, 'erro', problema.detail || 'Não foi possível carregar a notificação.');
    document.getElementById('salvar').disabled = true;
  }
}

iniciar();
