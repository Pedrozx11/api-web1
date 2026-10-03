# SINAN – API de Notificação/Conclusão de Agravos

API REST (Spring Boot 3 / Java 17) baseada na **Ficha de Notificação/Conclusão do SINAN**, com front-end em HTML/CSS/JS puro (sem frameworks), servido pela própria aplicação.

## Integrantes

- Pedro Henrique Oliveira Coura Rocha (Pai do Pingu)

## Como executar

Requisitos: JDK 17+ (testado com 21) e Maven 3.9+.

```bash
git clone <URL-DO-REPOSITORIO>
cd <PASTA-DO-REPOSITORIO>
mvn spring-boot:run
```

A aplicação sobe na porta 8080 quando aparecer `Started SinanApplication`. Para parar, use `Ctrl + C`.

**Sem instalar nada:** no GitHub, abra o repositório, clique em **Code → Codespaces → Create codespace on main** e rode `mvn spring-boot:run` no terminal. Quando aparecer o aviso da porta 8080, clique em **Open in Browser**.

> Observação: o `pom.xml` fixa a versão do Lombok (`lombok.version`) para funcionar com JDKs recentes.

- Consulta: http://localhost:8080/
- Cadastro: http://localhost:8080/cadastro.html
- Console do banco (H2): http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/sinan`, usuário `sa`, senha vazia)

Os dados ficam na pasta `./data` (H2 em arquivo).

## Endpoints

| Verbo  | URI                          | Sucesso            | Erros                |
|--------|------------------------------|--------------------|----------------------|
| GET    | `/notificacao?[filtros]`     | 200 (lista)        | 400                  |
| GET    | `/notificacao/{id}`          | 200                | 404                  |
| POST   | `/notificacao`               | 201 + `Location`   | 400                  |
| PUT    | `/notificacao/{id}`          | 200                | 400, 404             |
| DELETE | `/notificacao/{id}`          | 204                | 404                  |

### Filtros de `GET /notificacao`

Todos opcionais e combináveis: `nomePaciente`, `agravo`, `codigoCid10`, `ufNotificacao`, `municipioNotificacao`, `sexo`, `classificacaoFinal`, `evolucao`, `dataNotificacaoInicio`, `dataNotificacaoFim` (AAAA-MM-DD) e `duplicadas=true`.

Exemplo: `GET /notificacao?agravo=dengue&duplicadas=true&ufNotificacao=PB`

(O desafio opcional de paginação/ordenação não foi implementado; a lista vem ordenada por data da notificação, da mais recente para a mais antiga.)

### Erros (Problem Detail – RFC 9457)

Todas as respostas de erro usam `application/problem+json`. Exemplo de 400:

```json
{
  "type": "about:blank",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/notificacao",
  "erros": [
    { "campo": "gestante", "mensagem": "Gestante é obrigatório para o sexo feminino." }
  ]
}
```

## Regras de negócio

- **RN01 – Duplicidade** (`duplicadas=true`): mesmo agravo, nome do paciente, data de nascimento e nome da mãe (sem diferenciar maiúsculas/minúsculas nem espaços extras) e datas de notificação com até 3 dias de diferença (inclusive). Notificações com algum desses campos em branco são ignoradas. A duplicidade é verificada sobre toda a base; os demais filtros são aplicados em seguida.
- **RN02 – Idade e gestante**
  - Idade (unidade + valor) é obrigatória quando não há data de nascimento; havendo data de nascimento, o sistema calcula a idade na data da notificação.
  - Gestante é obrigatório para o sexo feminino; para o masculino só aceita 6 (Não se aplica; preenchido automaticamente se omitido); para sexo ignorado aceita 6 ou 9.
- **RN03 – Residência**: UF obrigatória quando reside no Brasil; município obrigatório quando a UF é informada; país obrigatório quando reside em outro país (e não deve ser informado junto com a UF).
- Outras consistências: critério de confirmação obrigatório quando há classificação final; data do óbito obrigatória (e só aceita) quando a evolução é óbito (2 ou 3); primeiros sintomas, nascimento e encerramento coerentes com a data da notificação.

Campos obrigatórios da ficha: agravo e CID10, data da notificação, UF e município de notificação, unidade de saúde, data dos primeiros sintomas, nome do paciente, sexo e raça/cor. O nome da mãe é opcional.

## Estrutura

```
src/main/java/br/gov/saude/sinan
├── controller/   NotificacaoController
├── service/      NotificacaoService (CRUD, filtros, RN01)
├── repository/   NotificacaoRepository
├── model/        Notificacao (entidade)
├── dto/          NotificacaoRequest, NotificacaoFiltro
├── validation/   NotificacaoValida / NotificacaoValidator (RN02, RN03)
├── exception/    GlobalExceptionHandler (Problem Detail)
└── util/         Texto
src/main/resources/static   index.html, cadastro.html, css/, js/
```