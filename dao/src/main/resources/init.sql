CREATE TABLE translation_requests
(
    request_time    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address      VARCHAR(16) NOT NULL,
    input_text      TEXT        NOT NULL,
    translated_text TEXT        NOT NULL
);
