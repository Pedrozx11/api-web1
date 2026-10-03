package br.gov.saude.sinan.service;

import br.gov.saude.sinan.dto.NotificacaoFiltro;
import br.gov.saude.sinan.dto.NotificacaoRequest;
import br.gov.saude.sinan.exception.RecursoNaoEncontradoException;
import br.gov.saude.sinan.exception.RequisicaoInvalidaException;
import br.gov.saude.sinan.model.Notificacao;
import br.gov.saude.sinan.repository.NotificacaoRepository;
import br.gov.saude.sinan.util.Texto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {

    /** RN01: diferença máxima (inclusive) em dias entre as datas de notificação. */
    private static final long LIMITE_DIAS_DUPLICIDADE = 3;

    private static final Sort ORDEM_PADRAO =
            Sort.by(Sort.Order.desc("dataNotificacao"), Sort.Order.desc("id"));

    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Notificacao criar(NotificacaoRequest request) {
        Notificacao notificacao = new Notificacao();
        aplicar(notificacao, request);
        return repository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public Notificacao buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação " + id + " não encontrada."));
    }

    @Transactional
    public Notificacao atualizar(Long id, NotificacaoRequest request) {
        Notificacao notificacao = buscar(id);
        aplicar(notificacao, request);
        return repository.save(notificacao);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listar(NotificacaoFiltro filtro) {
        if (filtro.dataNotificacaoInicio() != null && filtro.dataNotificacaoFim() != null
                && filtro.dataNotificacaoInicio().isAfter(filtro.dataNotificacaoFim())) {
            throw new RequisicaoInvalidaException(
                    "A data inicial da notificação não pode ser posterior à data final.");
        }
        Set<Long> idsDuplicados = Boolean.TRUE.equals(filtro.duplicadas()) ? idsDuplicados() : null;
        return repository.findAll(montarEspecificacao(filtro, idsDuplicados), ORDEM_PADRAO);
    }

    // ------------------------------------------------------------------
    // Persistência
    // ------------------------------------------------------------------

    private void aplicar(Notificacao destino, NotificacaoRequest origem) {
        BeanUtils.copyProperties(origem, destino);
        calcularIdade(destino);
        if ("M".equalsIgnoreCase(destino.getSexo()) && destino.getGestante() == null) {
            destino.setGestante(6); // 6 - Não se aplica
        }
    }

    /** Quando a data de nascimento é informada, a idade é calculada na data da notificação. */
    private void calcularIdade(Notificacao n) {
        LocalDate nasc = n.getDataNascimento();
        LocalDate ref = n.getDataNotificacao();
        if (nasc == null || ref == null) {
            return;
        }
        Period p = Period.between(nasc, ref);
        long dias = ChronoUnit.DAYS.between(nasc, ref);
        if (p.getYears() >= 1) {
            n.setIdadeUnidade(4);
            n.setIdadeValor(p.getYears());
        } else if (p.toTotalMonths() >= 1) {
            n.setIdadeUnidade(3);
            n.setIdadeValor((int) p.toTotalMonths());
        } else if (dias >= 1) {
            n.setIdadeUnidade(2);
            n.setIdadeValor((int) dias);
        } else {
            n.setIdadeUnidade(1);
            n.setIdadeValor(0);
        }
    }

    // ------------------------------------------------------------------
    // Consulta
    // ------------------------------------------------------------------

    private Specification<Notificacao> montarEspecificacao(NotificacaoFiltro f, Set<Long> idsDuplicados) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            String nome = Texto.normalizar(f.nomePaciente());
            if (nome != null) {
                p.add(cb.like(root.get("nomePacienteNorm"), "%" + nome + "%"));
            }
            String agravo = Texto.normalizar(f.agravo());
            if (agravo != null) {
                p.add(cb.like(root.get("agravoNorm"), "%" + agravo + "%"));
            }
            String cid = Texto.maiusculas(f.codigoCid10());
            if (cid != null) {
                p.add(cb.like(root.get("codigoCid10"), cid + "%"));
            }
            String uf = Texto.maiusculas(f.ufNotificacao());
            if (uf != null) {
                p.add(cb.equal(root.get("ufNotificacao"), uf));
            }
            String municipio = Texto.normalizar(f.municipioNotificacao());
            if (municipio != null) {
                p.add(cb.like(cb.lower(root.get("municipioNotificacao")), "%" + municipio + "%"));
            }
            String sexo = Texto.maiusculas(f.sexo());
            if (sexo != null) {
                p.add(cb.equal(root.get("sexo"), sexo));
            }
            if (f.classificacaoFinal() != null) {
                p.add(cb.equal(root.get("classificacaoFinal"), f.classificacaoFinal()));
            }
            if (f.evolucao() != null) {
                p.add(cb.equal(root.get("evolucao"), f.evolucao()));
            }
            if (f.dataNotificacaoInicio() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("dataNotificacao"), f.dataNotificacaoInicio()));
            }
            if (f.dataNotificacaoFim() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("dataNotificacao"), f.dataNotificacaoFim()));
            }
            if (idsDuplicados != null) {
                if (idsDuplicados.isEmpty()) {
                    p.add(cb.disjunction());
                } else {
                    p.add(root.get("id").in(idsDuplicados));
                }
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    // ------------------------------------------------------------------
    // RN01 - Notificações possivelmente duplicadas
    // ------------------------------------------------------------------

    /**
     * Retorna os ids das notificações duplicadas de ao menos outra notificação.
     * Critérios (todos ao mesmo tempo): mesmo agravo, mesmo nome do paciente, mesma data de
     * nascimento, mesmo nome da mãe (textos sem diferenciar maiúsculas/minúsculas nem espaços extras)
     * e datas de notificação com diferença de até 3 dias (inclusive).
     * Notificações com algum desses campos em branco são ignoradas.
     * <p>
     * A verificação considera todas as notificações da base; os demais filtros da consulta
     * são aplicados depois, sobre o resultado.
     */
    private Set<Long> idsDuplicados() {
        Specification<Notificacao> completas = (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("agravoNorm")),
                cb.isNotNull(root.get("nomePacienteNorm")),
                cb.isNotNull(root.get("nomeMaeNorm")),
                cb.isNotNull(root.get("dataNascimento")),
                cb.isNotNull(root.get("dataNotificacao")));

        Map<List<Object>, List<Notificacao>> grupos = repository.findAll(completas).stream()
                .collect(Collectors.groupingBy(n -> List.of(
                        n.getAgravoNorm(), n.getNomePacienteNorm(),
                        n.getDataNascimento(), n.getNomeMaeNorm())));

        Set<Long> duplicados = new HashSet<>();
        for (List<Notificacao> grupo : grupos.values()) {
            if (grupo.size() < 2) {
                continue;
            }
            List<Notificacao> ordenado = new ArrayList<>(grupo);
            ordenado.sort(Comparator.comparing(Notificacao::getDataNotificacao));
            // Com as datas em ordem, basta comparar vizinhos: se alguma outra notificação
            // está a até 3 dias, o vizinho mais próximo também está.
            for (int i = 1; i < ordenado.size(); i++) {
                Notificacao anterior = ordenado.get(i - 1);
                Notificacao atual = ordenado.get(i);
                long dias = ChronoUnit.DAYS.between(anterior.getDataNotificacao(), atual.getDataNotificacao());
                if (dias <= LIMITE_DIAS_DUPLICIDADE) {
                    duplicados.add(anterior.getId());
                    duplicados.add(atual.getId());
                }
            }
        }
        return duplicados;
    }
}
