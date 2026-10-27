package br.edu.ifsp.saocarlos.inscricoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BancoDeDadosTest {

    @Inject
    AgroalDataSource dataSource;

    @Test
    void sobeComH2EmMemoria() throws Exception {
        try (Connection conexao = dataSource.getConnection();
                Statement comando = conexao.createStatement();
                ResultSet resultado = comando.executeQuery("SELECT 1")) {
            assertTrue(resultado.next());
            assertEquals(1, resultado.getInt(1));
            assertTrue(conexao.getMetaData().getURL().startsWith("jdbc:h2:mem:"));
        }
    }
}
