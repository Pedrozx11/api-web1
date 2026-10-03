package br.gov.saude.sinan.model;

import br.gov.saude.sinan.util.Texto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Ficha de Notificação/Conclusão (SINAN). Os números entre parênteses
 * referem-se aos campos da ficha.
 */
@Entity
@Table(name = "notificacao")
@Getter
@Setter
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---- Dados gerais ----
    /** (1) 2 - Individual (valor fixo) */
    private Integer tipoNotificacao = 2;
    /** (2) Agravo/doença */
    @Column(nullable = false, length = 100)
    private String agravo;
    /** (2) Código (CID10) */
    @Column(nullable = false, length = 5)
    private String codigoCid10;
    /** (3) Data da notificação */
    @Column(nullable = false)
    private LocalDate dataNotificacao;
    /** (4) UF */
    @Column(nullable = false, length = 2)
    private String ufNotificacao;
    /** (5) Município de notificação */
    @Column(nullable = false, length = 100)
    private String municipioNotificacao;
    private String codigoIbgeMunicipioNotificacao;
    /** (6) Unidade de saúde */
    @Column(nullable = false, length = 150)
    private String unidadeSaude;
    private String codigoUnidadeSaude;
    /** (7) Data dos primeiros sintomas */
    @Column(nullable = false)
    private LocalDate dataPrimeirosSintomas;

    // ---- Notificação individual ----
    /** (8) Nome do paciente */
    @Column(nullable = false, length = 100)
    private String nomePaciente;
    /** (9) Data de nascimento */
    private LocalDate dataNascimento;
    /** (10) Idade: unidade (1-hora, 2-dia, 3-mês, 4-ano) e valor */
    private Integer idadeUnidade;
    private Integer idadeValor;
    /** (11) Sexo: M, F ou I */
    @Column(nullable = false, length = 1)
    private String sexo;
    /** (12) Gestante */
    private Integer gestante;
    /** (13) Raça/Cor */
    @Column(nullable = false)
    private Integer racaCor;
    /** (14) Escolaridade */
    private Integer escolaridade;
    /** (15) Número do Cartão SUS */
    private String cartaoSus;
    /** (16) Nome da mãe */
    @Column(length = 100)
    private String nomeMae;

    // ---- Dados de residência ----
    /** (17) UF */
    @Column(length = 2)
    private String ufResidencia;
    /** (18) Município de residência */
    private String municipioResidencia;
    private String codigoIbgeMunicipioResidencia;
    /** (19) Distrito */
    private String distritoResidencia;
    /** (20) Bairro */
    private String bairroResidencia;
    /** (21) Logradouro */
    private String logradouro;
    /** (22) Número */
    private String numeroResidencia;
    /** (23) Complemento */
    private String complemento;
    /** (24) e (25) Geo campos */
    private String geoCampo1;
    private String geoCampo2;
    /** (26) Ponto de referência */
    private String pontoReferencia;
    /** (27) CEP */
    private String cep;
    /** (28) (DDD) Telefone */
    private String telefone;
    /** (29) Zona */
    private Integer zona;
    /** (30) País (se residente fora do Brasil) */
    private String paisResidencia;

    // ---- Conclusão ----
    /** (31) Data da investigação */
    private LocalDate dataInvestigacao;
    /** (32) Classificação final: 1-Confirmado, 2-Descartado */
    private Integer classificacaoFinal;
    /** (33) Critério de confirmação/descarte: 1-Laboratorial, 2-Clínico-epidemiológico */
    private Integer criterioConfirmacao;
    /** (34) O caso é autóctone do município de residência? */
    private Integer autoctone;
    /** (35) a (39) Local provável da fonte de infecção */
    @Column(length = 2)
    private String ufInfeccao;
    private String paisInfeccao;
    private String municipioInfeccao;
    private String distritoInfeccao;
    private String bairroInfeccao;
    /** (40) Doença relacionada ao trabalho */
    private Integer doencaTrabalho;
    /** (41) Evolução do caso */
    private Integer evolucao;
    /** (42) Data do óbito */
    private LocalDate dataObito;
    /** (43) Data do encerramento */
    private LocalDate dataEncerramento;

    // ---- Observações e investigador ----
    @Column(length = 1000)
    private String observacoes;
    private String investigadorMunicipioUnidade;
    private String investigadorCodigoUnidade;
    private String investigadorNome;
    private String investigadorFuncao;

    // ---- Colunas normalizadas (usadas na verificação de duplicidade, RN01) ----
    @JsonIgnore
    @Column(length = 100)
    private String agravoNorm;
    @JsonIgnore
    @Column(length = 100)
    private String nomePacienteNorm;
    @JsonIgnore
    @Column(length = 100)
    private String nomeMaeNorm;

    @PrePersist
    @PreUpdate
    void normalizar() {
        agravoNorm = Texto.normalizar(agravo);
        nomePacienteNorm = Texto.normalizar(nomePaciente);
        nomeMaeNorm = Texto.normalizar(nomeMae);
        codigoCid10 = Texto.maiusculas(codigoCid10);
        ufNotificacao = Texto.maiusculas(ufNotificacao);
        ufResidencia = Texto.maiusculas(ufResidencia);
        ufInfeccao = Texto.maiusculas(ufInfeccao);
        sexo = Texto.maiusculas(sexo);
    }
}
