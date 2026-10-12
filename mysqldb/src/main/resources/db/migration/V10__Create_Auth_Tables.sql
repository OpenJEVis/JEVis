-- Authentication tables: dedicated password storage, login tokens, service API keys and 2FA.
-- Secrets are never stored in plaintext: passwords use a slow hash (argon2id / legacy pbkdf2),
-- tokens and API keys are random 256 bit values stored as SHA-256 hex, TOTP secrets are AES-GCM encrypted
-- with a key that is kept outside of the database (webservice.auth.mfakeyfile).

CREATE TABLE IF NOT EXISTS `user_credential`
(
    `user_id`
    bigint
(
    20
) NOT NULL,
    `password_hash` varchar
(
    255
) NOT NULL,
    `hash_algo` varchar
(
    16
) NOT NULL,
    `password_changed` datetime DEFAULT NULL,
    `failed_attempts` int
(
    11
) NOT NULL DEFAULT '0',
    `locked_until` datetime DEFAULT NULL,
    `must_change` tinyint
(
    1
) NOT NULL DEFAULT '0',
    PRIMARY KEY
(
    `user_id`
),
    CONSTRAINT `fk_user_credential_object` FOREIGN KEY
(
    `user_id`
) REFERENCES `object`
(
    `id`
) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `auth_token`
(
    `token_hash`
    char
(
    64
) NOT NULL,
    `user_id` bigint
(
    20
) NOT NULL,
    `type` enum
(
    'access',
    'refresh',
    'mfa',
    'handoff'
) NOT NULL,
    `created` datetime NOT NULL,
    `last_used` datetime DEFAULT NULL,
    `expires` datetime NOT NULL,
    `client_info` varchar
(
    255
) DEFAULT NULL,
    `revoked` tinyint
(
    1
) NOT NULL DEFAULT '0',
    PRIMARY KEY
(
    `token_hash`
),
    KEY `idx_auth_token_user`
(
    `user_id`
),
    KEY `idx_auth_token_expires`
(
    `expires`
),
    CONSTRAINT `fk_auth_token_object` FOREIGN KEY
(
    `user_id`
) REFERENCES `object`
(
    `id`
) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `api_key`
(
    `key_id`
    varchar
(
    16
) NOT NULL,
    `secret_hash` char
(
    64
) NOT NULL,
    `user_id` bigint
(
    20
) NOT NULL,
    `name` varchar
(
    255
) NOT NULL,
    `created` datetime NOT NULL,
    `created_by` bigint
(
    20
) DEFAULT NULL,
    `expires` datetime DEFAULT NULL,
    `last_used` datetime DEFAULT NULL,
    `revoked` tinyint
(
    1
) NOT NULL DEFAULT '0',
    PRIMARY KEY
(
    `key_id`
),
    KEY `idx_api_key_user`
(
    `user_id`
),
    CONSTRAINT `fk_api_key_object` FOREIGN KEY
(
    `user_id`
) REFERENCES `object`
(
    `id`
) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `user_mfa`
(
    `user_id`
    bigint
(
    20
) NOT NULL,
    `type` enum
(
    'totp'
) NOT NULL,
    `secret_enc` varbinary
(
    256
) NOT NULL,
    `confirmed` tinyint
(
    1
) NOT NULL DEFAULT '0',
    `created` datetime NOT NULL,
    `last_used_step` bigint
(
    20
) NOT NULL DEFAULT '0',
    PRIMARY KEY
(
    `user_id`,
    `type`
),
    CONSTRAINT `fk_user_mfa_object` FOREIGN KEY
(
    `user_id`
) REFERENCES `object`
(
    `id`
) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `user_recovery_code`
(
    `user_id`
    bigint
(
    20
) NOT NULL,
    `code_hash` char
(
    64
) NOT NULL,
    `used` datetime DEFAULT NULL,
    PRIMARY KEY
(
    `user_id`,
    `code_hash`
),
    CONSTRAINT `fk_user_recovery_code_object` FOREIGN KEY
(
    `user_id`
) REFERENCES `object`
(
    `id`
) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Copy the current (latest) password hash of every user into the new table.
-- The old 'Password' samples are kept until the rollout is verified (removed in a later migration).
INSERT INTO `user_credential` (`user_id`, `password_hash`, `hash_algo`, `password_changed`, `failed_attempts`,
                               `must_change`)
SELECT s.`object`, s.`value`, 'pbkdf2-sha1', s.`timestamp`, 0, 0
FROM `sample` s
         JOIN `object` o ON o.`id` = s.`object` AND o.`type` = 'User'
         JOIN (SELECT `object`, MAX(`timestamp`) AS ts
               FROM `sample`
               WHERE `attribute` = 'Password'
               GROUP BY `object`) latest ON latest.`object` = s.`object` AND latest.ts = s.`timestamp`
WHERE s.`attribute` = 'Password'
  AND s.`value` IS NOT NULL
  AND s.`value` <> ''
  AND NOT EXISTS (SELECT 1 FROM `user_credential` c WHERE c.`user_id` = s.`object`);
