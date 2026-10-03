package br.gov.saude.sinan.dto;

import br.gov.saude.sinan.validation.NotificacaoValida;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Dados de entrada (POST/PUT). Validações de campo individuais ficam aqui;
 * as regras que dependem de mais de um campo (RN02, RN03 etc.) ficam em
 * {@link NotificacaoValida}.
 */
@Data
@NotificacaoValida
public class NotificacaoRequest {

    public static final String UF_REGEX =
            "(?i)^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$";

    // ---- Dados gerais ----
    @NotBlank(message = "Agravo/doença é obrigatório.")
    @Size(max = 100, message = "Agravo/doença deve ter no máximo 100 caracteres.")
    private String agravo;

    @NotBlank(message = "Código CID10 é obrigatório.")
    @Pattern(regexp = "^[A-Za-z][0-9]{2}(\\.?[0-9A-Za-z])?$",
            message = "Código CID10 inválido (exemplos: A90, A90.0).")
    private String codigoCid10;

    @NotNull(message = "Data da notificação é obrigatória.")
    @PastOrPresent(message = "Data da notificação não pode ser futura.")
    private LocalDate dataNotificacao;

    @NotBlank(message = "UF de notificação é obrigatória.")
    @Pattern(regexp = UF_REGEX, message = "UF de notificação inválida.")
    private String ufNotificacao;

    @NotBlank(message = "Município de notificação é obrigatório.")
    @Size(max = 100, message = "Município deve ter no máximo 100 caracteres.")
    private String municipioNotificacao;

    @Pattern(regexp = "^\\d{6,7}$", message = "Código IBGE deve ter 6 ou 7 dígitos.")
    private String codigoIbgeMunicipioNotificacao;

    @NotBlank(message = "Unidade de saúde é obrigatória.")
    @Size(max = 150, message = "Unidade de saúde deve ter no máximo 150 caracteres.")
    private String unidadeSaude;

    @Pattern(regexp = "^\\d{7}$", message = "Código da unidade (CNES) deve ter 7 dígitos.")
    private String codigoUnidadeSaude;

    @NotNull(message = "Data dos primeiros sintomas é obrigatória.")
    @PastOrPresent(message = "Data dos primeiros sintomas não pode ser futura.")
    private LocalDate dataPrimeirosSintomas;

    // ---- Notificação individual ----
    @NotBlank(message = "Nome do paciente é obrigatório.")
    @Size(max = 100, message = "Nome do paciente deve ter no máximo 100 caracteres.")
    private String nomePaciente;

    @Past(message = "Data de nascimento deve ser anterior a hoje.")
    private LocalDate dataNascimento;

    @Min(value = 1, message = "Unidade da idade inválida.")
    @Max(value = 4, message = "Unidade da idade inválida.")
    private Integer idadeUnidade;

    @Min(value = 0, message = "Idade não pode ser negativa.")
    private Integer idadeValor;

    @NotBlank(message = "Sexo é obrigatório.")
    @Pattern(regexp = "(?i)^[MFI]$", message = "Sexo deve ser M, F ou I.")
    private String sexo;

    private Integer gestante;

    @NotNull(message = "Raça/Cor é obrigatória.")
    private Integer racaCor;

    @Min(value = 0, message = "Escolaridade inválida.")
    @Max(value = 10, message = "Escolaridade inválida.")
    private Integer escolaridade;

    @Pattern(regexp = "^\\d{15}$", message = "Cartão SUS deve ter 15 dígitos.")
    private String cartaoSus;

    @Size(max = 100, message = "Nome da mãe deve ter no máximo 100 caracteres.")
    private String nomeMae;

    // ---- Dados de residência ----
    @Pattern(regexp = UF_REGEX, message = "UF de residência inválida.")
    private String ufResidencia;

    @Size(max = 100, message = "Município deve ter no máximo 100 caracteres.")
    private String municipioResidencia;

    @Pattern(regexp = "^\\d{6,7}$", message = "Código IBGE deve ter 6 ou 7 dígitos.")
    private String codigoIbgeMunicipioResidencia;

    @Size(max = 100, message = "Distrito deve ter no máximo 100 caracteres.")
    private String distritoResidencia;

    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres.")
    private String bairroResidencia;

    @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres.")
    private String logradouro;

    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres.")
    private String numeroResidencia;

    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres.")
    private String complemento;

    @Size(max = 20, message = "Geo campo 1 deve ter no máximo 20 caracteres.")
    private String geoCampo1;

    @Size(max = 20, message = "Geo campo 2 deve ter no máximo 20 caracteres.")
    private String geoCampo2;

    @Size(max = 150, message = "Ponto de referência deve ter no máximo 150 caracteres.")
    private String pontoReferencia;

    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido (exemplo: 58000-000).")
    private String cep;

    @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
            message = "Telefone inválido (exemplo: (83) 99999-9999).")
    private String telefone;

    private Integer zona;

    @Size(max = 100, message = "País deve ter no máximo 100 caracteres.")
    private String paisResidencia;

    // ---- Conclusão ----
    private LocalDate dataInvestigacao;

    @Min(value = 1, message = "Classificação final inválida.")
    @Max(value = 2, message = "Classificação final inválida.")
    private Integer classificacaoFinal;

    @Min(value = 1, message = "Critério de confirmação/descarte inválido.")
    @Max(value = 2, message = "Critério de confirmação/descarte inválido.")
    private Integer criterioConfirmacao;

    @Min(value = 1, message = "Valor inválido.")
    @Max(value = 3, message = "Valor inválido.")
    private Integer autoctone;

    @Pattern(regexp = UF_REGEX, message = "UF da infecção inválida.")
    private String ufInfeccao;

    @Size(max = 100, message = "País deve ter no máximo 100 caracteres.")
    private String paisInfeccao;

    @Size(max = 100, message = "Município deve ter no máximo 100 caracteres.")
    private String municipioInfeccao;

    @Size(max = 100, message = "Distrito deve ter no máximo 100 caracteres.")
    private String distritoInfeccao;

    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres.")
    private String bairroInfeccao;

    private Integer doencaTrabalho;

    private Integer evolucao;

    @PastOrPresent(message = "Data do óbito não pode ser futura.")
    private LocalDate dataObito;

    private LocalDate dataEncerramento;

    // ---- Observações e investigador ----
    @Size(max = 1000, message = "Observações deve ter no máximo 1000 caracteres.")
    private String observacoes;

    @Size(max = 150, message = "Município/Unidade de saúde deve ter no máximo 150 caracteres.")
    private String investigadorMunicipioUnidade;

    @Pattern(regexp = "^\\d{7}$", message = "Código da unidade (CNES) deve ter 7 dígitos.")
    private String investigadorCodigoUnidade;

    @Size(max = 100, message = "Nome do investigador deve ter no máximo 100 caracteres.")
    private String investigadorNome;

    @Size(max = 100, message = "Função deve ter no máximo 100 caracteres.")
    private String investigadorFuncao;
}
