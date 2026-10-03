package br.gov.saude.sinan.repository;

import br.gov.saude.sinan.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long>, JpaSpecificationExecutor<Notificacao> {
}
