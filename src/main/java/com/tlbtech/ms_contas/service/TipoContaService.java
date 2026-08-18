package com.tlbtech.ms_contas.service;

import com.tlbtech.ms_contas.dto.TipoContaRequestDTO;
import com.tlbtech.ms_contas.dto.TipoContaResponseDTO;
import com.tlbtech.ms_contas.model.CategoriaTipoConta;
import com.tlbtech.ms_contas.model.TipoConta;
import com.tlbtech.ms_contas.model.TipoContaSeed;
import com.tlbtech.ms_contas.repository.ContaRepository;
import com.tlbtech.ms_contas.repository.TipoContaRepository;
import com.tlbtech.ms_contas.repository.TipoContaSeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoContaService {

    private final TipoContaRepository repository;
    private final ContaRepository contaRepository;
    private final TipoContaSeedRepository seedRepository;

    public TipoContaResponseDTO criar(TipoContaRequestDTO dto) {
        String email = getEmailAutenticado();

        if (repository.existsByNomeAndUsuarioIdAndAtivoTrue(dto.nome(), email)) {
            throw new IllegalArgumentException("Já existe um tipo de conta com esse nome.");
        }

        TipoConta tipoConta = TipoConta.builder()
                .nome(dto.nome())
                .categoria(dto.categoria())
                .usuarioId(email)
                .build();

        return TipoContaResponseDTO.fromEntity(repository.save(tipoConta));
    }

    public List<TipoContaResponseDTO> listar(Boolean ativo) {
        String email = getEmailAutenticado();
        garantirTiposPadrao(email);

        List<TipoConta> resultado;

        if (ativo == null) {
            resultado = repository.findByUsuarioId(email);
        } else if (ativo) {
            resultado = repository.findByUsuarioIdAndAtivoTrue(email);
        } else {
            resultado = repository.findByUsuarioIdAndAtivoFalse(email);
        }

        return resultado.stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(TipoContaResponseDTO::fromEntity)
                .toList();
    }

    public TipoContaResponseDTO buscarPorId(Long id) {
        String email = getEmailAutenticado();
        TipoConta tipoConta = repository
                .findByIdAndUsuarioIdAndAtivoTrue(id, email)
                .orElseThrow(() -> new RuntimeException("Tipo de conta não encontrado."));
        return TipoContaResponseDTO.fromEntity(tipoConta);
    }

    public TipoContaResponseDTO atualizar(Long id, TipoContaRequestDTO dto) {
        String email = getEmailAutenticado();
        TipoConta tipoConta = repository
                .findByIdAndUsuarioIdAndAtivoTrue(id, email)
                .orElseThrow(() -> new RuntimeException("Tipo de conta não encontrado."));

        tipoConta.setNome(dto.nome());
        tipoConta.setCategoria(dto.categoria());

        return TipoContaResponseDTO.fromEntity(repository.save(tipoConta));
    }

    public void inativar(Long id) {
        String email = getEmailAutenticado();
        TipoConta tipoConta = repository
                .findByIdAndUsuarioIdAndAtivoTrue(id, email)
                .orElseThrow(() -> new RuntimeException("Tipo de conta não encontrado ou já inativo."));

        tipoConta.setAtivo(false);
        repository.save(tipoConta);
    }

    @Transactional
    public void excluir(Long id) {
        String email = getEmailAutenticado();
        TipoConta tipoConta = repository
                .findByIdAndUsuarioIdAndAtivoFalse(id, email)
                .orElseThrow(() -> new IllegalStateException("Tipo de conta não encontrado ou ainda ativo. Inative antes de excluir."));

        if (contaRepository.existsByTipoContaIdAndAtivoTrue(id)) {
            throw new IllegalStateException("Não é possível excluir: existem contas ativas cadastradas com este tipo.");
        }

        contaRepository.desvincularTipoContaDeContasInativas(id);
        repository.delete(tipoConta);
    }

    public TipoContaResponseDTO reativar(Long id) {
        String email = getEmailAutenticado();
        TipoConta tipoConta = repository
                .findByIdAndUsuarioId(id, email)
                .orElseThrow(() -> new RuntimeException("Tipo de conta não encontrado."));

        if (tipoConta.getAtivo()) {
            throw new IllegalStateException("Tipo de conta já está ativo.");
        }

        tipoConta.setAtivo(true);
        return TipoContaResponseDTO.fromEntity(repository.save(tipoConta));
    }

    @Transactional
    public void garantirTiposPadrao(String email) {
        if (seedRepository.existsById(email)) {
            return;
        }

        List<TipoConta> padrao = List.of(
                novoTipoPadrao("Corrente", CategoriaTipoConta.BANCO, email),
                novoTipoPadrao("Poupança", CategoriaTipoConta.BANCO, email),
                novoTipoPadrao("CDB", CategoriaTipoConta.APLICACAO, email),
                novoTipoPadrao("LCI", CategoriaTipoConta.APLICACAO, email),
                novoTipoPadrao("LCA", CategoriaTipoConta.APLICACAO, email),
                novoTipoPadrao("Tesouro Direto", CategoriaTipoConta.APLICACAO, email),
                novoTipoPadrao("Fundo", CategoriaTipoConta.APLICACAO, email)
        );

        repository.saveAll(padrao);
        seedRepository.save(TipoContaSeed.builder().usuarioId(email).build());
    }

    private TipoConta novoTipoPadrao(String nome, CategoriaTipoConta categoria, String email) {
        return TipoConta.builder()
                .nome(nome)
                .categoria(categoria)
                .usuarioId(email)
                .build();
    }

    private String getEmailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
