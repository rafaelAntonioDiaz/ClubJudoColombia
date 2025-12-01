CREATE TABLE comentarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_publicacion BIGINT NOT NULL,
    id_autor BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comentario_publicacion FOREIGN KEY (id_publicacion)
        REFERENCES publicaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_comentario_autor FOREIGN KEY (id_autor)
        REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;