CREATE TABLE campos_entrenamiento (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_judoka BIGINT NOT NULL,
  id_sensei BIGINT NULL,
  nombre VARCHAR(255) NOT NULL,
  ubicacion VARCHAR(255),
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  objetivo VARCHAR(255),
  completado TINYINT(1) NOT NULL DEFAULT 0,
  puntos_ascenso INT NOT NULL DEFAULT 0,

  CONSTRAINT fk_campo_judoka FOREIGN KEY (id_judoka) REFERENCES judokas(id_judoka) ON DELETE CASCADE,
  CONSTRAINT fk_campo_sensei FOREIGN KEY (id_sensei) REFERENCES senseis(id_sensei) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;