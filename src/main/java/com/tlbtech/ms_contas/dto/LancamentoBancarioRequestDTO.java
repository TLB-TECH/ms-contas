package com.tlbtech.ms_contas.dto;

import com.tlbtech.ms_contas.model.TipoMovimentoBancario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoBancarioRequestDTO(

        @NotNull(message = "Tipo é obrigatório")
        TipoMovimentoBancario tipo,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "Data é obrigatória")
        LocalDate data,

        String descricao,

        Long tituloId

) {}
