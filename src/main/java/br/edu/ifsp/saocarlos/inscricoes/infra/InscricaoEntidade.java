package br.edu.ifsp.saocarlos.inscricoes.infra;

import br.edu.ifsp.saocarlos.inscricoes.dominio.SituacaoInscricao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** Mapeamento JPA da tabela {@code inscricao}. */
@Entity
@Table(name = "inscricao", indexes = {
        @Index(name = "idx_inscricao_palestra_situacao", columnList = "palestra_id, situacao"),
        @Index(name = "idx_inscricao_palestra_email", columnList = "palestra_id, email")
})
public class InscricaoEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "palestra_id", nullable = false)
    public PalestraEntidade palestra;

    @Column(nullable = false, length = 200)
    public String nome;

    @Column(nullable = false, length = 320)
    public String email;

    @Column(name = "data_hora_inscricao", nullable = false)
    public Instant dataHoraInscricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public SituacaoInscricao situacao;

    @Column(name = "data_hora_cancelamento")
    public Instant dataHoraCancelamento;
}
