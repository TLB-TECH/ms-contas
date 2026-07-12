package com.tlbtech.ms_contas.service;

import com.tlbtech.ms_contas.dto.*;
import com.tlbtech.ms_contas.model.Conta;
import com.tlbtech.ms_contas.model.TipoConta;
import com.tlbtech.ms_contas.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository repository;

    private static final List<TipoConta> TIPOS_BANCO = List.of(
            TipoConta.CORRENTE, TipoConta.POUPANCA
    );

    private static final List<TipoConta> TIPOS_APLICACAO = List.of(
            TipoConta.CDB, TipoConta.LCI, TipoConta.LCA,
            TipoConta.TESOURO, TipoConta.FUNDO
    );

    public List<ContaResponseDTO> listar() {
        return repository.findByAtivoTrue()
                .stream()
                .map(ContaResponseDTO::fromEntity)
                .toList();
    }

    public ContaResponseDTO buscarPorId(Long id) {
        return ContaResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO dto) {
        Conta conta = Conta.builder()
                .nome(dto.nome())
                .banco(dto.banco())
                .tipo(dto.tipo())
                .saldo(dto.saldo())
                .build();
        return ContaResponseDTO.fromEntity(repository.save(conta));
    }

    @Transactional
    public ContaResponseDTO atualizar(Long id, ContaRequestDTO dto) {
        Conta conta = buscarEntidade(id);
        conta.setNome(dto.nome());
        conta.setBanco(dto.banco());
        conta.setTipo(dto.tipo());
        conta.setSaldo(dto.saldo());
        return ContaResponseDTO.fromEntity(repository.save(conta));
    }

    @Transactional
    public void excluir(Long id) {
        Conta conta = buscarEntidade(id);
        conta.setAtivo(false);
        repository.save(conta);
    }

    @Transactional
    public void transferir(TransferenciaRequestDTO dto) {
        if (dto.contaOrigemId().equals(dto.contaDestinoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Conta de origem e destino não podem ser iguais");
        }

        Conta origem = buscarEntidade(dto.contaOrigemId());
        Conta destino = buscarEntidade(dto.contaDestinoId());

        if (origem.getSaldo().compareTo(dto.valor()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Saldo insuficiente na conta de origem");
        }

        origem.setSaldo(origem.getSaldo().subtract(dto.valor()));
        destino.setSaldo(destino.getSaldo().add(dto.valor()));

        repository.save(origem);
        repository.save(destino);
    }

    public BigDecimal totalBancos() {
        return repository.sumSaldoByTipos(TIPOS_BANCO);
    }

    public BigDecimal totalAplicacoes() {
        return repository.sumSaldoByTipos(TIPOS_APLICACAO);
    }

    private Conta buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }
}