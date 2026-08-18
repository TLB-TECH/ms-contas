package com.tlbtech.ms_contas.repository;

import com.tlbtech.ms_contas.model.TipoConta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoContaRepository extends JpaRepository<TipoConta, Long> {

    List<TipoConta> findByUsuarioIdAndAtivoTrue(String usuarioId);

    List<TipoConta> findByUsuarioIdAndAtivoFalse(String usuarioId);

    List<TipoConta> findByUsuarioId(String usuarioId);

    Optional<TipoConta> findByIdAndUsuarioIdAndAtivoTrue(Long id, String usuarioId);

    Optional<TipoConta> findByIdAndUsuarioIdAndAtivoFalse(Long id, String usuarioId);

    Optional<TipoConta> findByIdAndUsuarioId(Long id, String usuarioId);

    boolean existsByNomeAndUsuarioIdAndAtivoTrue(String nome, String usuarioId);

    void deleteByUsuarioId(String usuarioId);
}
