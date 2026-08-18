package com.tlbtech.ms_contas.dto;

import com.tlbtech.ms_contas.model.LancamentoBancario;
import com.tlbtech.ms_contas.model.OrigemMovimentoBancario;
import com.tlbtech.ms_contas.model.TipoMovimentoBancario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LancamentoBancarioResponseDTO(
        Long id,
        Long contaId,
        TipoMovimentoBancario tipo,
        BigDecimal valor,
        LocalDate data,
        String descricao,
        OrigemMovimentoBancario origem,
        Long tituloId,
        UUID transferenciaId,
        LocalDateTime criadoEm
) {
    public static LancamentoBancarioResponseDTO fromEntity(LancamentoBancario l) {
        return new LancamentoBancarioResponseDTO(
                l.getId(),
                l.getConta().getId(),
                l.getTipo(),
                l.getValor(),
                l.getData(),
                l.getDescricao(),
                l.getOrigem(),
                l.getTituloId(),
                l.getTransferenciaId(),
                l.getCriadoEm()
        );
    }
}
