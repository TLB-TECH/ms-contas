-- Contas bancárias passam a ser isoladas por usuário (antes eram globais, compartilhadas
-- por qualquer login). Backfill: até aqui só existia um usuário real no sistema.

ALTER TABLE contas ADD COLUMN usuario_id VARCHAR(255);
UPDATE contas SET usuario_id = 'taciolb@gmail.com' WHERE usuario_id IS NULL;
ALTER TABLE contas ALTER COLUMN usuario_id SET NOT NULL;
