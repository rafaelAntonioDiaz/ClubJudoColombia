# ==========================================
# ETAPA 1: CONSTRUCTOR (El Herrero)
# ==========================================
# Usamos el JDK 21 oficial (Pesado, solo para compilar)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# 1. Copiamos los archivos de Gradle primero (para aprovechar la caché de Docker)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# 2. Descargamos las dependencias (Si el código cambia, esto no se repite)
RUN ./gradlew dependencies --no-daemon

# 3. Copiamos el código fuente de nuestra app
COPY src/ src/

# 4. LA MAGIA: Compilamos el JAR ignorando los tests (ya los probamos)
# y activando el "Production Mode" de Vaadin para comprimir el Frontend.
RUN ./gradlew clean build -Pvaadin.productionMode=true -x test --no-daemon

# ==========================================
# ETAPA 2: PRODUCCIÓN (El Corredor)
# ==========================================
# Usamos un JRE ligero de Alpine (Pesa 5 veces menos que el JDK)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Creamos un usuario sin privilegios por SEGURIDAD (No correr como root)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos SOLO el archivo .jar ya compilado de la Etapa 1
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# Exponemos el puerto donde escucha Spring Boot
EXPOSE 8080

# Comando de arranque optimizado para contenedores pequeños (Max 512MB RAM)
ENTRYPOINT ["java", "-Xmx512m", "-XX:+UseSerialGC", "-jar", "app.jar"]