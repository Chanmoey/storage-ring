DROP TABLE IF EXISTS user_info;

CREATE TABLE user_info
(
    id          VARCHAR(32) NOT NULL,
    user_name   VARCHAR(32) NOT NULL,
    `password`  VARCHAR(64) NOT NULL COMMENT 'use md5 encryption',
    system_role VARCHAR(32) NOT NULL COMMENT '用户角色',
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    email       VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY AK_UQ_USER_NAME (`user_name`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    COMMENT = '用户信息';

DROP TABLE IF EXISTS token_info;

CREATE TABLE token_info
(
    token        VARCHAR(256) NOT NULL,
    expire_time  INT(11)      NOT NULL COMMENT '过期时间，天数',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refresh_time TIMESTAMP    NOT NULL,
    available    TINYINT      NOT NULL,
    creator      VARCHAR(32)  NOT NULL,
    PRIMARY KEY (token)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    COMMENT = 'token 信息表';

DROP TABLE IF EXISTS bucket;

CREATE TABLE bucket
(
    id          VARCHAR(32),
    bucket_name VARCHAR(32),
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detail      VARCHAR(256),
    creator     VARCHAR(32) NOT NULL,
    UNIQUE KEY AK_KEY_BUCKET_NAME (bucket_name),
    PRIMARY KEY (id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    COMMENT = 'bucket，存储桶，类似TX的OSS';

--
-- Table structure for table auth
--

DROP TABLE IF EXISTS auth;

CREATE TABLE auth
(
    id          VARCHAR(32) NOT NULL,
    bucket_name VARCHAR(32) NOT NULL,
    token       VARCHAR(32) NOT NULL
        COMMENT '持有该token，则可以访问该bucket',
    AUTH_TIME   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    COMMENT = '对象存储服务授权表';