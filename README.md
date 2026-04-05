# Bambu Task API

API RESTful para gestionar tareas con Spring Boot, JWT, roles USER/ADMIN y PostgreSQL.

## Stack
- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- JUnit + Mockito
- Docker / Docker Compose

## Requisitos
- Java 17+
- Docker + Docker Compose

## Levantar con Docker
Ejecuta en la raiz del proyecto:

```bash
docker-compose up --build
```

Con la configuracion por defecto se levanta:
- PostgreSQL en puerto 5432
- API en puerto 8080

## Variables de entorno opcionales
Puedes sobreescribir valores via .env o variables de sistema:

- DB_USERNAME (default: bambu)
- DB_PASSWORD (default: bambu123)
- DB_NAME (default: bambu_tasks)
- DB_URL (default: jdbc:postgresql://db:5432/${DB_NAME})
- JWT_SECRET (default: secret)
- JWT_EXPIRATION (default: 86400000 ms)

## Endpoints principales
Base URL: http://localhost:8080

### Auth
- POST /api/auth/init-admin (solo si no existen usuarios y pruebas, se puede eliminar sin problema)
- POST /api/auth/register (todos pueden registrarse como USER)
- POST /api/auth/login

Ejemplo init-admin (primer admin, solo funciona si la BD está vacía):

```json
{
  "username": "admin",
  "password": "admin123456"
}
```

Ejemplo register (nuevo usuario como USER):

```json
{
  "username": "john doe",
  "password": "password123"
}
```

Ejemplo login:

```json
{
  "username": "admin", 
  "password": "admin123456"
}
```

Respuesta de auth:

```json
{
  "token": "<jwt>"
}
```

Usa el token en headers:

```text
Authorization: Bearer <jwt>
```

### Tasks
- POST /api/tasks
- GET /api/tasks?page=0&size=10&status=PENDING
- GET /api/tasks/{id}
- PUT /api/tasks/{id}
- DELETE /api/tasks/{id}

Ejemplo create/update:

```json
{
  "title": "Terminar prueba tecnica",
  "description": "Implementar API y pruebas",
  "status": "IN_PROGRESS"
}
```

## Reglas de autorizacion
- USER: solo puede crear, ver, editar y eliminar sus propias tareas.
- ADMIN: puede ver y eliminar tareas de cualquier usuario.

### Creación de roles
- Al registrarse via `/api/auth/register`, los usuarios son creados como USER automáticamente.
- El primer ADMIN se crea llamando a `/api/auth/init-admin` cuando la BD está vacía (antes de que existan usuarios).
- Después de inicializar el primer admin, el endpoint `/api/auth/init-admin` rechazará nuevas llamadas con 409 Conflict.
- Para crear más ADMINs después del primero, se requiere acceso directo a la BD o implementar un endpoint admin-only (no incluido en esta prueba tecnica).

## Manejo de errores
La API responde JSON estructurado con:
- error
- message
- timestamp

Codigos esperados:
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict


