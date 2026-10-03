package br.gov.saude.sinan.validation;

import br.gov.saude.sinan.dto.NotificacaoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificacaoValidator implements ConstraintValidator<NotificacaoValida, NotificacaoRequest> {

    /** Idade máxima aceita por unidade: 1-hora, 2-dia, 3-mês, 4-ano. */
    private static final Map<Integer, Integer> IDADE_MAX = Map.of(1, 23, 2, 29, 3, 11, 4, 130);

    @Override
    public boolean isValid(NotificacaoRequest r, ConstraintValidatorContext ctx) {
        if (r == null) {
            return true;
        }
        List<String[]> erros = new ArrayList<>();

        validarIdade(r, erros);          // RN02 (campo 10)
        validarGestante(r, erros);       // RN02 (campo 12)
        validarResidencia(r, erros);     // RN03 (campos 17, 18 e 30)
        validarDominios(r, erros);
        validarConclusao(r, erros);
        validarDatas(r, erros);

        if (erros.isEmpty()) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        for (String[] e : erros) {
            ctx.buildConstraintViolationWithTemplate(e[1])
                    .addPropertyNode(e[0])
                    .addConstraintViolation();
        }
        return false;
    }

    // ---------- RN02: idade ----------
    private void validarIdade(NotificacaoRequest r, List<String[]> erros) {
        boolean semNascimento = r.getDataNascimento() == null;
        Integer unidade = r.getIdadeUnidade();
        Integer valor = r.getIdadeValor();

        if (semNascimento || unidade != null || valor != null) {
            String sufixo = semNascimento ? " quando a data de nascimento não é informada." : ".";
            if (unidade == null) {
                erros.add(new String[]{"idadeUnidade", "Unidade da idade é obrigatória" + sufixo});
            }
            if (valor == null) {
                erros.add(new String[]{"idadeValor", "Idade é obrigatória" + sufixo});
            }
            if (unidade != null && valor != null && IDADE_MAX.containsKey(unidade)
                    && valor > IDADE_MAX.get(unidade)) {
                erros.add(new String[]{"idadeValor", "Idade acima do limite permitido para a unidade informada."});
            }
        }
    }

    // ---------- RN02: gestante ----------
    private void validarGestante(NotificacaoRequest r, List<String[]> erros) {
        Integer g = r.getGestante();
        String sexo = r.getSexo() == null ? "" : r.getSexo().trim().toUpperCase();

        if (g != null && !contem(g, 1, 2, 3, 4, 5, 6, 9)) {
            erros.add(new String[]{"gestante", "Valor de gestante inválido."});
            return;
        }
        switch (sexo) {
            case "F" -> {
                if (g == null) {
                    erros.add(new String[]{"gestante", "Gestante é obrigatório para o sexo feminino."});
                }
            }
            case "M" -> {
                if (g != null && g != 6) {
                    erros.add(new String[]{"gestante", "Para o sexo masculino, use 6 - Não se aplica."});
                }
            }
            case "I" -> {
                if (g != null && g != 6 && g != 9) {
                    erros.add(new String[]{"gestante", "Para sexo ignorado, use 6 - Não se aplica ou 9 - Ignorado."});
                }
            }
            default -> {
            }
        }
    }

    // ---------- RN03: residência ----------
    private void validarResidencia(NotificacaoRequest r, List<String[]> erros) {
        boolean temUf = !vazio(r.getUfResidencia());
        boolean temPais = !vazio(r.getPaisResidencia());

        if (!temUf && !temPais) {
            erros.add(new String[]{"ufResidencia",
                    "UF de residência é obrigatória quando o paciente reside no Brasil."});
            erros.add(new String[]{"paisResidencia",
                    "País de residência é obrigatório quando o paciente reside em outro país."});
        }
        if (temUf && vazio(r.getMunicipioResidencia())) {
            erros.add(new String[]{"municipioResidencia",
                    "Município de residência é obrigatório quando a UF é informada."});
        }
        if (temUf && temPais) {
            erros.add(new String[]{"paisResidencia",
                    "País só deve ser informado quando o paciente reside fora do Brasil."});
        }
    }

    // ---------- Domínios com valores não contíguos ----------
    private void validarDominios(NotificacaoRequest r, List<String[]> erros) {
        dominio(erros, "racaCor", r.getRacaCor(), "Raça/Cor inválida.", 1, 2, 3, 4, 5, 9);
        dominio(erros, "zona", r.getZona(), "Zona inválida.", 1, 2, 3, 9);
        dominio(erros, "doencaTrabalho", r.getDoencaTrabalho(), "Valor inválido.", 1, 2, 9);
        dominio(erros, "evolucao", r.getEvolucao(), "Evolução do caso inválida.", 1, 2, 3, 9);
    }

    // ---------- Conclusão ----------
    private void validarConclusao(NotificacaoRequest r, List<String[]> erros) {
        if (r.getClassificacaoFinal() != null && r.getCriterioConfirmacao() == null) {
            erros.add(new String[]{"criterioConfirmacao",
                    "Critério de confirmação/descarte é obrigatório quando a classificação final é informada."});
        }
        if (r.getClassificacaoFinal() == null && r.getCriterioConfirmacao() != null) {
            erros.add(new String[]{"classificacaoFinal",
                    "Informe a classificação final para usar o critério de confirmação/descarte."});
        }
        Integer evolucao = r.getEvolucao();
        boolean obito = evolucao != null && (evolucao == 2 || evolucao == 3);
        if (obito && r.getDataObito() == null) {
            erros.add(new String[]{"dataObito", "Data do óbito é obrigatória quando a evolução é óbito."});
        }
        if (!obito && r.getDataObito() != null) {
            erros.add(new String[]{"dataObito", "Data do óbito só deve ser informada quando a evolução é óbito."});
        }
    }

    // ---------- Consistência entre datas ----------
    private void validarDatas(NotificacaoRequest r, List<String[]> erros) {
        LocalDate notif = r.getDataNotificacao();
        LocalDate sint = r.getDataPrimeirosSintomas();
        LocalDate nasc = r.getDataNascimento();

        if (notif != null && sint != null && sint.isAfter(notif)) {
            erros.add(new String[]{"dataPrimeirosSintomas",
                    "Data dos primeiros sintomas não pode ser posterior à data da notificação."});
        }
        if (nasc != null && notif != null && nasc.isAfter(notif)) {
            erros.add(new String[]{"dataNascimento",
                    "Data de nascimento não pode ser posterior à data da notificação."});
        }
        if (notif != null && r.getDataEncerramento() != null && r.getDataEncerramento().isBefore(notif)) {
            erros.add(new String[]{"dataEncerramento",
                    "Data do encerramento não pode ser anterior à data da notificação."});
        }
        if (nasc != null && r.getDataObito() != null && r.getDataObito().isBefore(nasc)) {
            erros.add(new String[]{"dataObito", "Data do óbito não pode ser anterior ao nascimento."});
        }
    }

    // ---------- auxiliares ----------
    private static void dominio(List<String[]> erros, String campo, Integer valor, String msg, int... aceitos) {
        if (valor != null && !contem(valor, aceitos)) {
            erros.add(new String[]{campo, msg});
        }
    }

    private static boolean contem(int valor, int... aceitos) {
        for (int a : aceitos) {
            if (a == valor) {
                return true;
            }
        }
        return false;
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
