package br.gov.saude.sinan.controller;

import br.gov.saude.sinan.dto.NotificacaoFiltro;
import br.gov.saude.sinan.dto.NotificacaoRequest;
import br.gov.saude.sinan.model.Notificacao;
import br.gov.saude.sinan.service.NotificacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/notificacao")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    /** GET /notificacao?[filtros] - 200 com a lista (vazia quando nada é encontrado). */
    @GetMapping
    public List<Notificacao> listar(@Valid NotificacaoFiltro filtro) {
        return service.listar(filtro);
    }

    /** GET /notificacao/{id} - 200, ou 404 se não existir. */
    @GetMapping("/{id}")
    public Notificacao buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    /** POST /notificacao - 201 Created com o cabeçalho Location. */
    @PostMapping
    public ResponseEntity<Notificacao> criar(@Valid @RequestBody NotificacaoRequest request) {
        Notificacao criada = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    /** PUT /notificacao/{id} - 200 com o registro atualizado, ou 404. */
    @PutMapping("/{id}")
    public Notificacao atualizar(@PathVariable Long id, @Valid @RequestBody NotificacaoRequest request) {
        return service.atualizar(id, request);
    }

    /** DELETE /notificacao/{id} - 204 sem corpo, ou 404. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
