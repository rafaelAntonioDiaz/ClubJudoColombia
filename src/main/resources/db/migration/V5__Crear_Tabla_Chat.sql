CREATE TABLE mensajes_chat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_autor BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_autor FOREIGN KEY (id_autor)
        REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;