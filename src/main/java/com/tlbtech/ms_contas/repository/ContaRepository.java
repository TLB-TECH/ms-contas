package com.tlbtech.ms_contas.repository;

import com.tlbtech.ms_contas.model.CategoriaTipoConta;
import com.tlbtech.ms_contas.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findByUsuarioIdAndAtivoTrue(String usuarioId);

    Optional<Conta> findByIdAndUsuarioId(Long id, String usuarioId);

    boolean existsByTipoContaIdAndAtivoTrue(Long tipoContaId);

    @Modifying
    @Query("UPDATE Conta c SET c.tipoConta = null WHERE c.tipoConta.id = :tipoContaId AND c.ativo = false")
    void desvincularTipoContaDeContasInativas(Long tipoContaId);

    @Query("SELECT COALESCE(SUM(c.saldo), 0) FROM Conta c WHERE c.usuarioId = :usuarioId AND c.ativo = true AND c.tipoConta.categoria = :categoria")
    BigDecimal sumSaldoByCategoria(String usuarioId, CategoriaTipoConta categoria);

    void deleteByUsuarioId(String usuarioId);
}
