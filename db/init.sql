-- Minimal schema for this assessment

CREATE TABLE IF NOT EXISTS ai_models (
  model_id  VARCHAR(128) NOT NULL PRIMARY KEY,
  owned_by  VARCHAR(128) NULL,
  enabled   TINYINT      NOT NULL DEFAULT 1,
  created   BIGINT       NULL
);

CREATE TABLE IF NOT EXISTS chat_completion (
  id               VARCHAR(64)  NOT NULL PRIMARY KEY,
  user_id          VARCHAR(64)  NULL,
  model            VARCHAR(128) NULL,
  request_messages TEXT        NULL,
  response_content MEDIUMTEXT  NULL,
  status           VARCHAR(32)  NULL,
  created_at       BIGINT       NULL
);

-- Example: enable a model (adjust to the actual model ids you use)
-- INSERT INTO ai_models (model_id, owned_by, enabled, created)
-- VALUES ('qwen-turbo', 'dashscope', 1, UNIX_TIMESTAMP());
