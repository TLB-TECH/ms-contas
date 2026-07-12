package com.tlbtech.ms_contas.dto;

import com.tlbtech.ms_contas.model.TipoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContaRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Banco é obrigatório")
        String banco,

        @NotNull(message = "Tipo é obrigatório")
        TipoConta tipo,

        @NotNull(message = "Saldo é obrigatório")
        BigDecimal saldo

) {}
