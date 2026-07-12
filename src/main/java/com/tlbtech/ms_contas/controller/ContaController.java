package com.tlbtech.ms_contas.controller;

import com.tlbtech.ms_contas.dto.*;
import com.tlbtech.ms_contas.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService service;

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ContaResponseDTO> criar(@RequestBody @Valid ContaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ContaRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transferir")
    public ResponseEntity<Void> transferir(@RequestBody @Valid TransferenciaRequestDTO dto) {
        service.transferir(dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/totais/bancos")
    public ResponseEntity<BigDecimal> totalBancos() {
        return ResponseEntity.ok(service.totalBancos());
    }

    @GetMapping("/totais/aplicacoes")
    public ResponseEntity<BigDecimal> totalAplicacoes() {
        return ResponseEntity.ok(service.totalAplicacoes());
    }
}