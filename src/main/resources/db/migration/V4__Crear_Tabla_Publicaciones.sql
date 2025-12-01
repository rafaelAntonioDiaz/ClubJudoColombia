CREATE TABLE publicaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_autor BIGINT NOT NULL,
    contenido TEXT,
    imagen_url VARCHAR(255),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    CONSTRAINT fk_publicacion_autor FOREIGN KEY (id_autor)
        REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;