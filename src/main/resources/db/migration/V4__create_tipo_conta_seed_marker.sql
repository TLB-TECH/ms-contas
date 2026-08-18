CREATE TABLE tipos_conta_seed (
    usuario_id VARCHAR(255) PRIMARY KEY,
    criado_em  TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO tipos_conta_seed (usuario_id)
SELECT DISTINCT usuario_id FROM tipos_conta;
