-- participant
CREATE TABLE participant (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- draw_code
CREATE TABLE draw_code (
    id              BIGSERIAL PRIMARY KEY,
    participant_id  BIGINT       NOT NULL REFERENCES participant (id),
    code            VARCHAR(100) NOT NULL UNIQUE,
    remaining_count INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    expires_at      TIMESTAMP,
    last_used_at    TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- reward
CREATE TABLE reward (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    weight      INT          NOT NULL DEFAULT 1,
    stock       INT,
    image       VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- draw_result
CREATE TABLE draw_result (
    id                   BIGSERIAL PRIMARY KEY,
    draw_code_id         BIGINT       NOT NULL REFERENCES draw_code (id),
    participant_id       BIGINT       NOT NULL REFERENCES participant (id),
    reward_id            BIGINT       NOT NULL REFERENCES reward (id),
    reward_name_snapshot VARCHAR(200) NOT NULL,
    draw_no              INT          NOT NULL,
    drawn_at             TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_draw_code_code ON draw_code (code);
CREATE INDEX idx_draw_result_draw_code_id ON draw_result (draw_code_id);
CREATE INDEX idx_draw_result_participant_id ON draw_result (participant_id);
