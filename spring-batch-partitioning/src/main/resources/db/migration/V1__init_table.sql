CREATE TABLE cooking_log (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '요리 이름',
    description TEXT NOT NULL COMMENT '요리 설명',
    status VARCHAR(20) NOT NULL COMMENT '요리 상태',
    cooked_at DATETIME NOT NULL COMMENT '요리한 일시'
) COMMENT='요리 기록 테이블';

CREATE INDEX idx_cooking_log_name ON cooking_log(name);
CREATE INDEX idx_cooking_log_cooked_at ON cooking_log(cooked_at);
CREATE INDEX idx_cooking_log_cooked_at_id ON cooking_log (cooked_at DESC, id DESC);
