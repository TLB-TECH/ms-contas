package com.tlbtech.ms_contas.service;

import com.tlbtech.ms_contas.dto.*;
import com.tlbtech.ms_contas.model.CategoriaTipoConta;
import com.tlbtech.ms_contas.model.Conta;
import com.tlbtech.ms_contas.model.LancamentoBancario;
import com.tlbtech.ms_contas.model.OrigemMovimentoBancario;
import com.tlbtech.ms_contas.model.TipoConta;
import com.tlbtech.ms_contas.model.TipoMovimentoBancario;
import com.tlbtech.ms_contas.repository.ContaRepository;
import com.tlbtech.ms_contas.repository.LancamentoBancarioRepository;
import com.tlbtech.ms_contas.repository.TipoContaRepository;
import com.tlbtech.ms_contas.repository.TipoContaSeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository repository;
    private final TipoContaRepository tipoContaRepository;
    private final LancamentoBancarioRepository lancamentoBancarioRepository;
    private final TipoContaSeedRepository tipoContaSeedRepository;

    public List<ContaResponseDTO> listar() {
        return repository.findByUsuarioIdAndAtivoTrue(getEmailAutenticado())
                .stream()
                .map(ContaResponseDTO::fromEntity)
                .toList();
    }

    public ContaResponseDTO buscarPorId(Long id) {
        return ContaResponseDTO.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO dto) {
        String email = getEmailAutenticado();
        Conta conta = Conta.builder()
                .nome(dto.nome())
                .banco(dto.banco())
                .tipoConta(buscarTipoConta(dto.tipoContaId()))
                .saldo(BigDecimal.ZERO)
                .usuarioId(email)
                .build();
        conta = repository.save(conta);

        if (dto.saldo().compareTo(BigDecimal.ZERO) != 0) {
            TipoMovimentoBancario tipo = dto.saldo().compareTo(BigDecimal.ZERO) > 0
                    ? TipoMovimentoBancario.ENTRADA : TipoMovimentoBancario.SAIDA;
            registrarMovimento(conta, tipo, dto.saldo().abs(), OrigemMovimentoBancario.SALDO_INICIAL,
                    LocalDate.now(), "Saldo inicial", null, null);
        }

        return ContaResponseDTO.fromEntity(conta);
    }

    /** Editar conta nunca mexe no saldo — o campo `saldo` do DTO é ignorado aqui de propósito.
     *  Qualquer entrada/saída deve passar por um lançamento (título efetivado, transferência ou
     *  saldo inicial no cadastro), nunca pela edição de nome/banco/tipo da conta. */
    @Transactional
    public ContaResponseDTO atualizar(Long id, ContaRequestDTO dto) {
        Conta conta = buscarEntidade(id);
        conta.setNome(dto.nome());
        conta.setBanco(dto.banco());
        conta.setTipoConta(buscarTipoConta(dto.tipoContaId()));
        repository.save(conta);

        return ContaResponseDTO.fromEntity(conta);
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

        UUID transferenciaId = UUID.randomUUID();
        registrarMovimento(origem, TipoMovimentoBancario.SAIDA, dto.valor(), OrigemMovimentoBancario.TRANSFERENCIA,
                LocalDate.now(), dto.descricao(), null, transferenciaId);
        registrarMovimento(destino, TipoMovimentoBancario.ENTRADA, dto.valor(), OrigemMovimentoBancario.TRANSFERENCIA,
                LocalDate.now(), dto.descricao(), null, transferenciaId);
    }

    public List<LancamentoBancarioResponseDTO> listarLancamentos(Long contaId) {
        buscarEntidade(contaId);
        return lancamentoBancarioRepository.findByContaIdOrderByDataDescCriadoEmDesc(contaId)
                .stream()
                .map(LancamentoBancarioResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public LancamentoBancarioResponseDTO registrarLancamentoManual(Long contaId, LancamentoBancarioRequestDTO dto) {
        Conta conta = buscarEntidade(contaId);

        OrigemMovimentoBancario origem = dto.tituloId() != null
                ? OrigemMovimentoBancario.TITULO
                : OrigemMovimentoBancario.MANUAL;

        LancamentoBancario lancamento = registrarMovimento(conta, dto.tipo(), dto.valor(),
                origem, dto.data(), dto.descricao(), dto.tituloId(), null);
        return LancamentoBancarioResponseDTO.fromEntity(lancamento);
    }

    public BigDecimal totalBancos() {
        return repository.sumSaldoByCategoria(getEmailAutenticado(), CategoriaTipoConta.BANCO);
    }

    public BigDecimal totalAplicacoes() {
        return repository.sumSaldoByCategoria(getEmailAutenticado(), CategoriaTipoConta.APLICACAO);
    }

    /** Apaga de vez contas, lançamentos bancários, tipos de conta e o marcador de seed do usuário
     *  (exclusão de conta em cascata). Ordem importa: lançamentos bancários têm FK pra contas, e
     *  contas têm FK pra tipos de conta — precisa apagar de baixo pra cima. */
    @Transactional
    public void excluirDadosUsuario(String usuarioId) {
        lancamentoBancarioRepository.deleteByContaUsuarioId(usuarioId);
        repository.deleteByUsuarioId(usuarioId);
        tipoContaRepository.deleteByUsuarioId(usuarioId);
        tipoContaSeedRepository.deleteById(usuarioId);
    }

    private LancamentoBancario registrarMovimento(Conta conta, TipoMovimentoBancario tipo, BigDecimal valor,
                                                    OrigemMovimentoBancario origem, LocalDate data, String descricao,
                                                    Long tituloId, UUID transferenciaId) {
        BigDecimal delta = tipo == TipoMovimentoBancario.ENTRADA ? valor : valor.negate();
        conta.setSaldo(conta.getSaldo().add(delta));
        repository.save(conta);

        LancamentoBancario lancamento = LancamentoBancario.builder()
                .conta(conta)
                .tipo(tipo)
                .valor(valor)
                .data(data != null ? data : LocalDate.now())
                .descricao(descricao)
                .origem(origem)
                .tituloId(tituloId)
                .transferenciaId(transferenciaId)
                .build();
        return lancamentoBancarioRepository.save(lancamento);
    }

    private Conta buscarEntidade(Long id) {
        return repository.findByIdAndUsuarioId(id, getEmailAutenticado())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }

    private TipoConta buscarTipoConta(Long tipoContaId) {
        String email = getEmailAutenticado();
        return tipoContaRepository.findByIdAndUsuarioId(tipoContaId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tipo de conta não encontrado"));
    }

    private String getEmailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
