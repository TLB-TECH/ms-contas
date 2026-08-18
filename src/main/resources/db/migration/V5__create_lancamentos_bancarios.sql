CREATE TABLE lancamentos_bancarios (
    id                BIGSERIAL PRIMARY KEY,
    conta_id          BIGINT NOT NULL,
    tipo              VARCHAR(10) NOT NULL,
    valor             NUMERIC(15,2) NOT NULL,
    data              DATE NOT NULL,
    descricao         VARCHAR(255),
    origem            VARCHAR(20) NOT NULL,
    titulo_id         BIGINT,
    transferencia_id  UUID,
    criado_em         TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lancamentos_bancarios_conta FOREIGN KEY (conta_id) REFERENCES contas(id)
);

CREATE INDEX idx_lancamentos_bancarios_conta_id ON lancamentos_bancarios (conta_id);

-- Reconciliação retroativa: contas existentes com saldo != 0 recebem um lançamento
-- SALDO_INICIAL equivalente, sem alterar o saldo atual.
INSERT INTO lancamentos_bancarios (conta_id, tipo, valor, data, descricao, origem, criado_em)
SELECT
    id,
    CASE WHEN saldo >= 0 THEN 'ENTRADA' ELSE 'SAIDA' END,
    ABS(saldo),
    criado_em::date,
    'Saldo inicial (migração retroativa)',
    'SALDO_INICIAL',
    criado_em
FROM contas
WHERE saldo <> 0;
