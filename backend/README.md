# OftAPP - Backend REST API (Spring Boot)

Backend oficial para la plataforma médica **OftAPP**, diseñado para gestionar la autenticación clínica, fichas de pacientes, trazabilidad de exámenes oftalmológicos y sincronización offline desde la aplicación Android.

---

## 🛠️ Stack Tecnológico

* **Framework:** Spring Boot 3.3.5
* **Lenguaje:** Java 17+
* **Persistencia:** Spring Data JPA + Hibernate
* **Base de Datos:** H2 Database (In-Memory)
* **Validación:** Jakarta Bean Validation (Hibernate Validator)
* **Documentación:** OpenAPI 3 / Swagger UI (`springdoc-openapi-starter-webmvc-ui:2.6.0`)
* **Gestor de Construcción:** Apache Maven

---

## 🚀 Ejecución del Proyecto

Desde la carpeta `backend/`:

```bash
cd backend
mvn spring-boot:run
```

O si prefieres empaquetar el JAR ejecutable:

```bash
mvn clean package -DskipTests
java -jar target/oftapp-backend-1.0.0-SNAPSHOT.jar
```

---

## 🌐 Servicios Disponibles

* **API Base URL:** `http://localhost:8080` (o `http://10.0.2.2:8080` para emulador Android)
* **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **OpenAPI Docs (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
* **Consola H2:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * **JDBC URL:** `jdbc:h2:mem:oftappdb`
  * **User Name:** `sa`
  * **Password:** *(en blanco)*

---

## 👥 Datos Semilla Precargados (DataSeeder)

### Usuarios de Prueba (`POST /api/v1/auth/login`)
| Email | Contraseña | Rol Clínico | Nombre | RUT |
|---|---|---|---|---|
| `medico@oftapp.cl` | `OftApp2026!` | `MEDICO` | Dra. Carla Ruiz | `11.111.111-1` |
| `tecnologo@oftapp.cl` | `OftApp2026!` | `TECNOLOGO` | Lic. Andrés Gómez | `22.222.222-2` |
| `admin@oftapp.cl` | `OftApp2026!` | `ADMIN` | Carlos Vega | `33.333.333-3` |
| `paciente@oftapp.cl` | `OftApp2026!` | `PACIENTE` | Juan Pérez | `12.345.678-9` |

---

## 📋 Catálogo de Endpoints REST

### 1. Autenticación (`/api/v1/auth`)
* `POST /api/v1/auth/login`: Autentica usuario y retorna token de sesión simulado.

### 2. Pacientes (`/api/v1/pacientes`)
* `GET /api/v1/pacientes`: Lista todos los pacientes registrados.
* `POST /api/v1/pacientes`: Crea una nueva ficha de paciente.

### 3. Exámenes Oftalmológicos (`/api/v1/examenes` y `/api/v1/sync`)
* `GET /api/v1/examenes?pacienteId={id}&rol={rol}`: Obtiene exámenes. Si `rol=PACIENTE`, filtra estrictamente por `pacienteId`.
* `GET /api/v1/examenes/{id}`: Obtiene el detalle clínico de un examen.
* `POST /api/v1/examenes`: Registra un examen. Valida que el header `X-User-Role` no sea `PACIENTE` (403 Forbidden).
* `PUT /api/v1/examenes/{id}/estado`: Actualiza estado a `VALIDADO` y registra observaciones.
* `POST /api/v1/sync`: Sincroniza un lote de exámenes capturados en modo offline en Android, asignando estado `SINCRONIZADO` y nuevos IDs remotos.
