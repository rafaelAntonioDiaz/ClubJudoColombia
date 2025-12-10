package com.RafaelDiaz.ClubJudoColombia.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Entidad que representa a un Usuario en el sistema.
 * * Esta clase se mapea a la tabla 'usuarios' en la base de datos.
 * Implementa Serializable, una buena práctica para entidades JPA,
 * aunque no siempre es estrictamente necesario, ayuda con la caché de segundo nivel y la serialización.
 */
@Entity // Le dice a JPA que esta clase es una entidad y debe ser mapeada a una tabla.
@Table(name = "usuarios") // (Buena práctica) Especifica el nombre de la tabla en la BD.
// Usamos plural ("usuarios") para la tabla.
public class Usuario implements Serializable {

    /**
     * Identificador único para cada usuario.
     * Es la clave primaria (Primary Key) de la tabla.
     */
    @Id // Marca este campo como la clave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Le dice a JPA que la BD (MySQL) se encargará de generar este valor.
    // (MySQL usará AUTO_INCREMENT).
    @Column(name = "id_usuario") // (Opcional pero recomendado) Nombre de la columna.
    private Long id;

    /**
     * Nombre de usuario (para login). Debe ser único.
     */
    @Column(name = "username", unique = true, nullable = false, length = 100)
    // unique=true: Asegura que no haya dos usuarios con el mismo username.
    // nullable=false: Este campo no puede ser nulo en la BD.
    // length=100: Limita el tamaño del campo.
    private String username;

    /**
     * Contraseña del usuario (almacenada como un hash, NUNCA en texto plano).
     * Más adelante integraremos Spring Security para manejar esto correctamente.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Almacenaremos el HASH, no la contraseña.

    /**
     * Nombre real del usuario.
     */
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /**
     * Apellido real del usuario.
     */
    @Column(name = "apellido", nullable = false, length = 150)
    private String apellido;

    @Column(name = "email", length = 150)
    private String email;


    /**
     * Estado del usuario (activo, inactivo, pendiente de verificación).
     * Usamos un booleano para simplicidad (true = activo, false = inactivo).
     */
    @Column(name = "activo", nullable = false)
    private boolean activo = true; // Valor por defecto
    // ... (después del campo 'private boolean activo = true;')

    /**
     * Relación Muchos-a-Muchos (ManyToMany) con la entidad Rol.
     * Un usuario puede tener múltiples roles, y un rol puede tener múltiples usuarios.
     *
     * FetchType.EAGER: Le dice a JPA que cargue los roles del usuario
     * inmediatamente cuando se carga el usuario. Es útil para seguridad.
     *
     * @JoinTable: Configura la tabla intermedia que gestionará esta relación.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_roles", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "id_usuario"), // Columna que referencia a esta entidad (Usuario)
            inverseJoinColumns = @JoinColumn(name = "id_rol") // Columna que referencia a la otra entidad (Rol)
    )
    private java.util.Set<Rol> roles = new java.util.HashSet<>();

    // --- Constructores ---

    /**
     * Constructor vacío requerido por JPA.
     * Las entidades siempre deben tener un constructor sin argumentos (public o protected).
     */
    public Usuario() {
    }

    /**
     * Constructor de conveniencia para crear nuevos usuarios.
     * No incluimos el 'id' porque será generado por la base de datos.
     */
    public Usuario(String username, String passwordHash, String nombre, String apellido) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = true; // Por defecto, los nuevos usuarios están activos
    }

    // --- Getters y Setters ---
    // Son necesarios para que JPA (y Vaadin) puedan leer y escribir
    // los valores de los campos privados.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }




    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Usuario otroUsuario = (Usuario) obj;
        // Comparamos solo por ID. Si ambos ID son nulos (objetos nuevos), no son iguales.
        // Si uno tiene ID y el otro no, no son iguales.
        // Si ambos tienen ID, los ID deben ser iguales.
        return id != null && id.equals(otroUsuario.id);
    }
}