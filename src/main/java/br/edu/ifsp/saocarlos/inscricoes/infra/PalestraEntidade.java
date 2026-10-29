package br.edu.ifsp.saocarlos.inscricoes.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Mapeamento JPA da tabela {@code palestra}. */
@Entity
@Table(name = "palestra")
public class PalestraEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 200)
    public String titulo;

    @Column(nullable = false, length = 2000)
    public String descricao;

    @Column(name = "total_vagas", nullable = false)
    public int totalVagas;
}
