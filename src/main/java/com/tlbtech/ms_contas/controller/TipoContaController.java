package com.tlbtech.ms_contas.controller;

import com.tlbtech.ms_contas.dto.TipoContaRequestDTO;
import com.tlbtech.ms_contas.dto.TipoContaResponseDTO;
import com.tlbtech.ms_contas.service.TipoContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tipos-conta")
@RequiredArgsConstructor
public class TipoContaController {

    private final TipoContaService service;

    @PostMapping
    public ResponseEntity<TipoContaResponseDTO> criar(@RequestBody @Valid TipoContaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<TipoContaResponseDTO>> listar(
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(service.listar(ativo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoContaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoContaResponseDTO> atualizar(@PathVariable Long id,
                                                           @RequestBody @Valid TipoContaRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<TipoContaResponseDTO> reativar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reativar(id));
    }

    @DeleteMapping("/{id}/excluir")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> tratarIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }
}
