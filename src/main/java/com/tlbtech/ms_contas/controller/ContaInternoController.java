package com.tlbtech.ms_contas.controller;

import com.tlbtech.ms_contas.service.ContaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interno")
@RequiredArgsConstructor
public class ContaInternoController {

    private final ContaService contaService;

    @Value("${internal.secret}")
    private String internalSecret;

    @DeleteMapping("/contas/usuarios/{usuarioId}")
    public ResponseEntity<Void> excluirDadosUsuario(
            @PathVariable String usuarioId,
            @RequestHeader("X-Internal-Secret") String secret) {

        if (!internalSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        contaService.excluirDadosUsuario(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
