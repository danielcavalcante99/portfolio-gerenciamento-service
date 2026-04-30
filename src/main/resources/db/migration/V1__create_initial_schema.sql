CREATE TABLE membros
(
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    cargo         VARCHAR(80)  NOT NULL,
    criado_em     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP
);

CREATE TYPE status_projeto AS ENUM (
    'EM_ANALISE',
    'ANALISE_REALIZADA',
    'ANALISE_APROVADA',
    'INICIADO',
    'EM_ANDAMENTO',
    'ENCERRADO',
    'CANCELADO'
);

CREATE TABLE projetos
(
    id                BIGSERIAL PRIMARY KEY,
    nome              VARCHAR(150)   NOT NULL,
    data_inicio       DATE           NOT NULL,
    data_prevista_fim DATE           NOT NULL,
    data_real_fim     DATE,
    orcamento_total   NUMERIC(15, 2) NOT NULL,
    descricao         TEXT,
    gerente_id        BIGINT         NOT NULL,
    status            status_projeto NOT NULL,
    criado_em         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em     TIMESTAMP,

    CONSTRAINT fk_projetos_gerente
        FOREIGN KEY (gerente_id)
            REFERENCES membros (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_datas_projeto
        CHECK (data_prevista_fim >= data_inicio),

    CONSTRAINT chk_data_real_fim
        CHECK (data_real_fim IS NULL OR data_real_fim >= data_inicio),

    CONSTRAINT chk_orcamento_total
        CHECK (orcamento_total >= 0)
);

CREATE TABLE projeto_membros
(
    projeto_id BIGINT NOT NULL,
    membro_id  BIGINT NOT NULL,

    CONSTRAINT pk_projeto_membros
        PRIMARY KEY (projeto_id, membro_id),

    CONSTRAINT fk_projeto_membros_projeto
        FOREIGN KEY (projeto_id)
            REFERENCES projetos (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_projeto_membros_membro
        FOREIGN KEY (membro_id)
            REFERENCES membros (id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_projetos_status ON projetos (status);
CREATE INDEX idx_projetos_gerente_id ON projetos (gerente_id);
CREATE INDEX idx_projetos_data_inicio ON projetos (data_inicio);
CREATE INDEX idx_projetos_data_prevista_fim ON projetos (data_prevista_fim);
CREATE INDEX idx_projeto_membros_membro_id ON projeto_membros (membro_id);