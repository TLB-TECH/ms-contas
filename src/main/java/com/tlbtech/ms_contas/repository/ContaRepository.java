package com.tlbtech.ms_contas.repository;

import com.tlbtech.ms_contas.model.Conta;
import com.tlbtech.ms_contas.model.TipoConta;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.math.BigDecimal;
import java.util.List;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findByAtivoTrue();

    List<Conta> findByTipoInAndAtivoTrue(List<TipoConta> tipos);

    @Query("SELECT COALESCE(SUM(c.saldo), 0) FROM Conta c WHERE c.ativo = true AND c.tipo IN :tipos")
    BigDecimal sumSaldoByTipos(List<TipoConta> tipos);
}
