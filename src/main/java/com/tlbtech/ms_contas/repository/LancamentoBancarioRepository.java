package com.tlbtech.ms_contas.repository;

import com.tlbtech.ms_contas.model.LancamentoBancario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LancamentoBancarioRepository extends JpaRepository<LancamentoBancario, Long> {

    List<LancamentoBancario> findByContaIdOrderByDataDescCriadoEmDesc(Long contaId);

    @Modifying
    @Query("DELETE FROM LancamentoBancario l WHERE l.conta.usuarioId = :usuarioId")
    void deleteByContaUsuarioId(@Param("usuarioId") String usuarioId);
}
