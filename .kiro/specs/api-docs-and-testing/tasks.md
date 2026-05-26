# Implementation Plan: api-docs-and-testing

## Overview

Agrega tres entregables de soporte al proyecto `smarttourism-back` (Spring Boot 3.3.5, Java 17):
1. Documentación OpenAPI / Swagger con `springdoc-openapi-starter-webmvc-ui`.
2. Colección Postman completa (Collection v2.1 + Environment JSON).
3. `docker-compose.yml` para entorno de desarrollo local (backend + PostgreSQL + MailHog).

El backend ya está implementado; este plan no modifica lógica de negocio.

---

## Tasks

- [x] 1. Agregar dependencia springdoc-openapi y configuración base
  - [x] 1.1 Agregar dependencia `org.springdoc:springdoc-openapi-starter-webmvc-ui` en `pom.xml` con versión compatible con Spring Boot 3.3 (ej. `2.6.0`)
    - Verificar compatibilidad con la versión `3.3.5` del parent de Spring Boot
    - _Requirements: 1.1_
  - [x] 1.2 Agregar la sección `springdoc` en `src/main/resources/application.yml`
    - Propiedades: `api-docs.path`, `swagger-ui.path`, `swagger-ui.enabled` (sobreescribible por `SPRINGDOC_SWAGGER_UI_ENABLED`), `swagger-ui.try-it-out-enabled=true`, `swagger-ui.operations-sorter=alpha`, `swagger-ui.tags-sorter=alpha`, `show-actuator=true`
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 1.6_

- [x] 2. Crear `OpenApiConfig.java` y actualizar `SecurityConfig.java`
  - [x] 2.1 Crear `src/main/java/com/smarttourism/backend/config/OpenApiConfig.java`
    - Bean `OpenAPI` con título `"Smart Tourism Santander API"`, versión `"1.0.0"` y descripción del sistema
    - Registrar esquema de seguridad `bearerAuth` (tipo `http`, esquema `bearer`, formato `JWT`)
    - Aplicar `SecurityRequirement` global con `bearerAuth`
    - _Requirements: 1.4, 1.5_
  - [x] 2.2 Modificar `SecurityConfig.java` para agregar las rutas de Swagger UI y `/v3/api-docs/**` a la whitelist de rutas públicas en el `SecurityFilterChain`
    - Rutas a permitir: `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`, `/v3/api-docs`
    - _Requirements: 1.2, 1.3_
  - [x] 2.3 Escribir prueba unitaria `OpenApiConfigTest` que verifique el bean `OpenAPI`: título, versión y presencia del esquema `bearerAuth`
    - Verificar que `openApi.getInfo().getTitle()` es `"Smart Tourism Santander API"`
    - Verificar que `openApi.getComponents().getSecuritySchemes()` contiene `"bearerAuth"`
    - _Requirements: 1.4, 1.5_

- [ ] 3. Agregar anotaciones `@Tag` y `@Operation` a los controllers
  - [x] 3.1 Agregar `@Tag` a nivel de clase en los ocho controllers: `AuthController`, `ExperienceController`, `ScheduleController`, `ReservationController`, `PaymentController`, `ReviewController`, `AdminUserController`, `AdminReservationController`
    - Tags: `auth`, `experiences`, `schedules`, `reservations`, `payments`, `reviews`, `admin` (los dos admin comparten el mismo tag)
    - Agregar `@Operation` en métodos donde el nombre no sea suficientemente descriptivo
    - _Requirements: 5.5, 10.2_
  - [x] 3.2 Escribir prueba de integración `OpenApiTagsPropertyTest` (jqwik) que verifique la Propiedad 1
    - **Property 1: Todos los endpoints de la especificación OpenAPI tienen al menos un tag**
    - Parsear `/v3/api-docs` con `@SpringBootTest(webEnvironment = RANDOM_PORT)`; generar entradas con `@ForAll("endpointOperations")`
    - Verificar que `operation.getTags()` no es vacío para cada operación
    - **Validates: Requirements 5.5**
    - _Requirements: 5.5_

- [x] 4. Checkpoint — Verificar disponibilidad de Swagger UI y spec OpenAPI
  - Asegurar que todos los tests pasan. Verificar manualmente que `/swagger-ui.html` y `/v3/api-docs` son accesibles. Preguntar al usuario si hay dudas antes de continuar.

- [x] 5. Escribir pruebas de integración para la especificación OpenAPI
  - [x] 5.1 Crear `SwaggerUiAvailabilityTest` que verifique `GET /swagger-ui.html` retorna 200/302 sin autenticación
    - Usar `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`
    - _Requirements: 1.2_
  - [x] 5.2 Crear `OpenApiSpecTest` que verifique `GET /v3/api-docs` retorna 200 y JSON válido con los campos `openapi`, `info`, `paths` y `components`
    - Verificar que `info.title` es `"Smart Tourism Santander API"` y `info.version` es `"1.0.0"`
    - _Requirements: 1.3, 1.4_
  - [x] 5.3 Escribir prueba de integración `OpenApiTypeMappingPropertyTest` (jqwik) que verifique la Propiedad 5
    - **Property 5: Los tipos de datos Java se mapean correctamente en la especificación OpenAPI**
    - Parsear los schemas de `/v3/api-docs`; generar entradas con `@ForAll("schemaProperties")` para propiedades de tipo `UUID`, `LocalDate`, `LocalDateTime`, `LocalTime`, `BigDecimal` y enums
    - Verificar que el tipo y formato OpenAPI coincide con la tabla de mapeo del diseño
    - **Validates: Requirements 10.3**
    - _Requirements: 10.3_

- [x] 6. Crear la colección Postman y el archivo de entorno
  - [x] 6.1 Crear `postman/SmartTourism.postman_environment.json` con las variables: `baseUrl` (`http://localhost:8080`), `adminEmail`, `adminPassword`, `touristEmail`, `touristPassword`, `jwtToken`, `adminToken`, `touristToken`, `experienceId`, `scheduleId`, `reservationId`
    - _Requirements: 6.3_
  - [x] 6.2 Crear `postman/SmartTourism.postman_collection.json` en formato Postman Collection v2.1 con las carpetas: `Auth`, `Experiences`, `Schedules`, `Reservations`, `Payments`, `Reviews`, `Admin`, `Health`
    - Incluir todos los requests listados en el diseño (happy path + escenarios de error `[Error]`)
    - Incluir el Pre-request Script a nivel de colección para manejo automático del token JWT
    - Configurar header `Authorization: Bearer {{jwtToken}}` en todos los requests protegidos
    - Incluir scripts `pm.test` en cada request: código HTTP esperado, body JSON válido, campos obligatorios presentes
    - Incluir tests que almacenen `token` → `jwtToken`/`adminToken`/`touristToken` en los requests de login
    - Incluir tests que almacenen `id` → `experienceId` y `reservationId` en los requests de creación
    - Incluir descripción del flujo E2E en la carpeta raíz: registro → login → experiencias → reserva → pago → reseña
    - _Requirements: 6.1, 6.2, 6.4, 6.5, 6.6, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  - [x] 6.3 Escribir prueba unitaria `PostmanCollectionValidationTest` que parsee `SmartTourism.postman_collection.json` y verifique: formato v2.1, presencia de las 8 carpetas, presencia del Pre-request Script a nivel de colección
    - _Requirements: 6.1, 6.2, 6.4_
  - [x] 6.4 Escribir prueba de propiedades `PostmanAuthHeaderPropertyTest` (jqwik) que verifique la Propiedad 2
    - **Property 2: Todos los requests protegidos de la colección Postman tienen el header Authorization**
    - Parsear la colección; generar entradas con `@ForAll("protectedRequests")`
    - Verificar que cada request protegido tiene el header `Authorization` con valor `Bearer {{jwtToken}}`
    - **Validates: Requirements 6.5**
    - _Requirements: 6.5_
  - [x] 6.5 Escribir prueba de propiedades `PostmanTestScriptsPropertyTest` (jqwik) que verifique la Propiedad 3
    - **Property 3: Todos los requests de la colección Postman tienen al menos un test automatizado**
    - Parsear la colección; generar entradas con `@ForAll("postmanRequests")`
    - Verificar que cada request tiene al menos un evento `"test"` con una línea que contenga `pm.test`
    - **Validates: Requirements 7.1**
    - _Requirements: 7.1_

- [x] 7. Checkpoint — Verificar colección Postman
  - Asegurar que todos los tests pasan. Importar manualmente la colección y el entorno en Postman para verificar que no hay errores de importación. Preguntar al usuario si hay dudas antes de continuar.

- [x] 8. Crear `docker-compose.yml`
  - [x] 8.1 Crear `docker-compose.yml` en la raíz del proyecto con los servicios `db` (postgres:16-alpine), `mailhog` (mailhog/mailhog:latest) y `backend` (construido desde `Dockerfile`)
    - Servicio `db`: variables `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`; puerto `5432`; healthcheck con `pg_isready`; volumen `postgres_data`
    - Servicio `mailhog`: puertos `1025` (SMTP) y `8025` (web)
    - Servicio `backend`: todas las variables requeridas por `application.yml` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `MAIL_HOST=mailhog`, `MAIL_PORT=1025`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CORS_ALLOWED_ORIGINS`); `depends_on: db: condition: service_healthy`; `depends_on: mailhog: condition: service_started`; `restart: on-failure`; puerto `8080`
    - Incluir comentarios explicativos en cada servicio y variable de entorno
    - Definir volumen nombrado `postgres_data`
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8_
  - [x] 8.2 Escribir prueba de propiedades `DockerComposeEnvVarsPropertyTest` (jqwik) que verifique la Propiedad 4
    - **Property 4: Todas las variables de entorno requeridas por application.yml están definidas en docker-compose**
    - Extraer todas las referencias `${VAR_NAME}` de `application.yml` con un generador `@ForAll("applicationYmlEnvVars")`
    - Parsear `docker-compose.yml` y extraer las variables del servicio `backend`
    - Verificar que cada variable extraída de `application.yml` está presente en `docker-compose.yml`
    - **Validates: Requirements 8.3**
    - _Requirements: 8.3_

- [ ] 9. Checkpoint final — Asegurar que todos los tests pasan
  - Ejecutar `mvn test` y verificar que todos los tests pasan sin errores. Preguntar al usuario si hay dudas antes de dar por completado el feature.

---

## Notes

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido.
- Las pruebas de la colección Postman y docker-compose son pruebas de archivos estáticos: parsean los JSON/YAML generados y verifican su estructura sin levantar el servidor.
- Las pruebas de la especificación OpenAPI (tareas 5.1, 5.2, 5.3) requieren `@SpringBootTest` con `WebEnvironment.RANDOM_PORT` y una base de datos de prueba (Testcontainers ya está en `pom.xml`).
- Los generadores jqwik para las propiedades 1, 2, 3 y 5 parsean los archivos de salida (spec JSON, colección Postman) y generan entradas del conjunto de elementos encontrados.
- La propiedad 4 es determinista: extrae todas las variables `${VAR}` de `application.yml` y verifica que cada una esté en docker-compose. Se implementa como `@Property` para consistencia con el framework jqwik.
- Cada prueba de propiedad debe incluir el comentario: `// Feature: api-docs-and-testing, Property N: <texto>` y usar `@Property(tries = 100)`.

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["2.3", "3.1"] },
    { "id": 3, "tasks": ["3.2", "5.1", "5.2", "6.1"] },
    { "id": 4, "tasks": ["5.3", "6.2"] },
    { "id": 5, "tasks": ["6.3", "6.4", "6.5", "8.1"] },
    { "id": 6, "tasks": ["8.2"] }
  ]
}
```
