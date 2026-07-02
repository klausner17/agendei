-- A pessoa que faz login agora é o próprio profissional.
-- Remove a entidade User separada e a coluna de vínculo user_id.
ALTER TABLE professionals DROP COLUMN user_id;
DROP TABLE IF EXISTS users;

-- Email do profissional passa a ser a credencial de login e deve ser único.
CREATE UNIQUE INDEX IF NOT EXISTS professionals_email ON professionals (email);
