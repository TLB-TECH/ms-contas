package com.tlbtech.ms_contas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferenciaRequestDTO(

        @NotNull(message = "Conta de origem é obrigatória")
        Long contaOrigemId,

        @NotNull(message = "Conta de origem é obrigatória")
        Long contaDestinoId,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal valor,

        String descricao
) {}
