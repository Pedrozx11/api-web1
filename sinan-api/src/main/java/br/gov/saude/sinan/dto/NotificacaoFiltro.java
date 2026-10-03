package br.gov.saude.sinan.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Parâmetros aceitos em GET /notificacao. Todos são opcionais e combináveis. */
public record NotificacaoFiltro(
        String nomePaciente,
        String agravo,
        String codigoCid10,
        String ufNotificacao,
        String municipioNotificacao,
        String sexo,
        Integer classificacaoFinal,
        Integer evolucao,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNotificacaoInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNotificacaoFim,
        Boolean duplicadas) {
}
