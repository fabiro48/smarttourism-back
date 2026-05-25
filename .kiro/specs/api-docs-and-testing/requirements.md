# Requirements Document

## Introduction

El proyecto **smart-tourism-backend** (Spring Boot 3.3, Java 17, PostgreSQL) está completamente implementado y expone una API REST bajo `/api/v1`. Este feature agrega tres entregables de soporte al desarrollo y las pruebas:

1. **Documentación OpenAPI / Swagger** — integración de `springdoc-openapi` para generar documentación interactiva de todos los endpoints REST, incluyendo descripciones, schemas de request/response, ejemplos, códigos de error y autenticación JWT Bearer.
2. **Colección Postman completa** — archivo JSON exportable con todos los endpoints organizados por módulo, variables de entorno, scripts de pre-request para manejo automático del token JWT, y escenarios de prueba (happy path + casos de error).
3. **docker-compose para desarrollo local** — archivo `docker-compose.yml` que levante el backend + PostgreSQL + MailHog, con variables de entorno configuradas para ejecutar la regresión Postman completa localmente.

Los módulos cubiertos son: `auth`, `experiences`, `schedules`, `reservations`, `payments`, `reviews`, `admin`.

---

## Glossary

- **Sistema**: El backend de la plataforma Turismo Inteligente Santander (`smarttourism-back`).
- **OpenAPI_Spec**: La especificación OpenAPI 3.0 generada automáticamente por `springdoc-openapi` a partir de las anotaciones del código fuente.
- **Swagger_UI**: La interfaz web interactiva servida por `springdoc-openapi` que permite explorar y ejecutar los endpoints documentados.
- **Postman_Collection**: Archivo JSON en formato Postman Collection v2.1 que contiene todos los endpoints organizados por carpeta/módulo.
- **Postman_Environment**: Archivo JSON en formato Postman Environment que define las variables (`baseUrl`, `jwtToken`, `adminToken`, `touristToken`, UUIDs de recursos) usadas por la colección.
- **Pre-request Script**: Script JavaScript ejecutado por Postman antes de cada request para inyectar automáticamente el token JWT en el header `Authorization`.
- **docker-compose**: Archivo `docker-compose.yml` que orquesta los servicios necesarios para el entorno de desarrollo local.
- **MailHog**: Servidor SMTP de prueba que captura correos electrónicos sin enviarlos realmente, accesible vía interfaz web en el puerto 8025.
- **JWT**: JSON Web Token, mecanismo de autenticación Bearer usado por todos los endpoints protegidos.
- **Bearer_Token**: Token JWT incluido en el header HTTP `Authorization: Bearer <token>`.
- **Happy_Path**: Escenario de prueba que verifica el comportamiento correcto del sistema con entradas válidas.
- **Error_Scenario**: Escenario de prueba que verifica el comportamiento del sistema ante entradas inválidas o condiciones de error.
- **springdoc-openapi**: Librería Java que genera automáticamente la especificación OpenAPI 3.0 a partir de las anotaciones de Spring MVC y Jakarta Validation.

---

## Requirements

### Requirement 1: Integración de springdoc-openapi en el proyecto

**User Story:** Como desarrollador del equipo, quiero que el proyecto incluya la dependencia `springdoc-openapi-starter-webmvc-ui`, para que la documentación OpenAPI se genere automáticamente al compilar y ejecutar el backend.

#### Acceptance Criteria

1. THE Sistema SHALL incluir la dependencia `org.springdoc:springdoc-openapi-starter-webmvc-ui` en el `pom.xml` con una versión compatible con Spring Boot 3.3.
2. WHEN el backend está en ejecución, THE Swagger_UI SHALL estar disponible en la ruta `/swagger-ui.html` o `/swagger-ui/index.html`.
3. WHEN el backend está en ejecución, THE OpenAPI_Spec SHALL estar disponible en formato JSON en la ruta `/v3/api-docs`.
4. THE Sistema SHALL configurar el bean `OpenAPI` con el título `"Smart Tourism Santander API"`, la versión `"1.0.0"` y una descripción del sistema.
5. THE Sistema SHALL configurar el esquema de seguridad `bearerAuth` de tipo `http` con esquema `bearer` y formato `JWT` en la definición OpenAPI global, de modo que todos los endpoints protegidos muestren el candado de autenticación en Swagger UI.
6. IF la variable de entorno `SPRINGDOC_SWAGGER_UI_ENABLED` está definida como `false`, THEN THE Sistema SHALL deshabilitar Swagger UI en cualquier perfil de ejecución (desarrollo, pruebas o producción) sin requerir recompilación.

---

### Requirement 2: Documentación del módulo Auth

**User Story:** Como consumidor de la API, quiero que los endpoints de autenticación estén completamente documentados en Swagger UI, para entender cómo registrarme y obtener un token JWT.

#### Acceptance Criteria

1. THE OpenAPI_Spec SHALL documentar el endpoint `POST /api/v1/auth/register` con descripción, el schema completo de `RegisterRequest` (campos: `fullName`, `email`, `password`, `phone`, `documentNumber`), el schema de `AuthResponse` (campos: `token`, `user.id`, `user.fullName`, `user.email`, `user.role`), y los códigos de respuesta HTTP 201, 400 y 409.
2. THE OpenAPI_Spec SHALL documentar el endpoint `POST /api/v1/auth/login` con descripción, el schema de `LoginRequest` (campos: `email`, `password`), el schema de `AuthResponse`, y los códigos de respuesta HTTP 200, 400, 401 y 403.
3. THE OpenAPI_Spec SHALL incluir al menos un ejemplo de request válido para `POST /api/v1/auth/register` y `POST /api/v1/auth/login`.
4. THE OpenAPI_Spec SHALL documentar el schema de `ErrorResponse` (campos: `timestamp`, `status`, `error`, `message`) como respuesta de error reutilizable para todos los códigos 4xx y 5xx.

---

### Requirement 3: Documentación del módulo Experiences

**User Story:** Como consumidor de la API, quiero que todos los endpoints de experiencias estén documentados con sus parámetros de filtro, schemas y ejemplos, para poder integrar el catálogo en el frontend.

#### Acceptance Criteria

1. THE OpenAPI_Spec SHALL documentar `GET /api/v1/experiences` con todos los query parameters opcionales (`category`, `location`, `difficulty`, `minPrice`, `maxPrice`, `available`), los parámetros de paginación (`page`, `size`, `sort`), el schema de respuesta paginada con `ExperienceResponse`, y los códigos HTTP 200 y 400.
2. THE OpenAPI_Spec SHALL documentar `GET /api/v1/experiences/{id}` con el path parameter `id` (UUID), el schema completo de `ExperienceResponse` incluyendo los campos `averageRating`, `reviewCount` y la lista de `schedules`, y los códigos HTTP 200 y 404.
3. THE OpenAPI_Spec SHALL documentar `POST /api/v1/experiences`, `PUT /api/v1/experiences/{id}` y `DELETE /api/v1/experiences/{id}` con el schema de `ExperienceRequest`, los códigos HTTP correspondientes (201, 200, 204, 400, 401, 403, 404), y la anotación de seguridad `bearerAuth`.
4. THE OpenAPI_Spec SHALL documentar los endpoints de schedules (`POST`, `PUT`, `DELETE` en `/api/v1/experiences/{experienceId}/schedules/{scheduleId}`) con el schema de `ScheduleRequest` (campos: `dayOfWeek`, `startTime`, `endTime`, `availableSlots`), el schema de `ScheduleResponse`, y la anotación de seguridad `bearerAuth`.

---

### Requirement 4: Documentación de los módulos Reservations, Payments y Reviews

**User Story:** Como consumidor de la API, quiero que los endpoints de reservas, pagos y reseñas estén documentados con sus reglas de negocio y ejemplos, para implementar correctamente el flujo de reserva en el frontend.

#### Acceptance Criteria

1. THE OpenAPI_Spec SHALL documentar `POST /api/v1/reservations` con el schema de `ReservationRequest` (campos: `experienceId`, `scheduleId`, `reservationDate`, `quantity`), el schema de `ReservationResponse` incluyendo `totalAmount` y `expirationDate`, los códigos HTTP 201, 400, 401, 403 y 409, y la anotación de seguridad `bearerAuth`.
2. THE OpenAPI_Spec SHALL documentar `GET /api/v1/reservations/me` con el schema de respuesta (lista de `ReservationResponse`), el código HTTP 200, y la anotación de seguridad `bearerAuth`.
3. THE OpenAPI_Spec SHALL documentar `PATCH /api/v1/reservations/{id}/cancel` con el path parameter `id`, los códigos HTTP 204, 401, 403 y 422, y la anotación de seguridad `bearerAuth`.
4. THE OpenAPI_Spec SHALL documentar `POST /api/v1/payments/simulate` con el schema de `PaymentRequest` (campo: `reservationId`), el schema de `PaymentResponse` incluyendo `paymentStatus` y `transactionReference`, los códigos HTTP 200, 400, 401, 403 y 422, y la anotación de seguridad `bearerAuth`.
5. THE OpenAPI_Spec SHALL documentar `POST /api/v1/reviews` con el schema de `ReviewRequest` (campos: `experienceId`, `rating`, `comment`), el schema de `ReviewResponse`, los códigos HTTP 201, 400, 401, 403 y 409, y la anotación de seguridad `bearerAuth`.
6. THE OpenAPI_Spec SHALL documentar `GET /api/v1/experiences/{id}/reviews` con el path parameter `id`, el schema de respuesta (lista de `ReviewResponse`), y los códigos HTTP 200 y 404.

---

### Requirement 5: Documentación del módulo Admin y Actuator

**User Story:** Como administrador de la plataforma, quiero que los endpoints de administración y el health check estén documentados, para poder supervisar el sistema y gestionar usuarios y reservas desde la documentación interactiva.

#### Acceptance Criteria

1. THE OpenAPI_Spec SHALL documentar `GET /api/v1/admin/reservations` con los query parameters opcionales (`status`, `experienceId`, `startDate`, `endDate`) y los parámetros de paginación, el schema de respuesta paginada con `ReservationResponse`, los códigos HTTP 200, 401 y 403, y la anotación de seguridad `bearerAuth`.
2. THE OpenAPI_Spec SHALL documentar `GET /api/v1/admin/users` con los parámetros de paginación, el schema de respuesta paginada con `UserResponse` (campos: `id`, `fullName`, `email`, `phone`, `documentNumber`, `role`, `active`, `createdAt`, `updatedAt`), los códigos HTTP 200, 401 y 403, y la anotación de seguridad `bearerAuth`.
3. THE OpenAPI_Spec SHALL documentar `PATCH /api/v1/admin/users/{userId}/status` con el path parameter `userId`, el schema de `UpdateUserStatusRequest` (campo: `active`), el schema de `UserResponse`, los códigos HTTP 200, 400, 401, 403 y 404, y la anotación de seguridad `bearerAuth`.
4. THE OpenAPI_Spec SHALL documentar `GET /actuator/health` como endpoint público con el schema de respuesta de Spring Actuator y el código HTTP 200.
5. THE OpenAPI_Spec SHALL agrupar los endpoints en tags: `auth`, `experiences`, `schedules`, `reservations`, `payments`, `reviews`, `admin`, `actuator`.

---

### Requirement 6: Colección Postman — estructura y variables de entorno

**User Story:** Como desarrollador o QA, quiero una colección Postman completa y exportable, para poder ejecutar pruebas de regresión contra el backend local o remoto sin configuración manual.

#### Acceptance Criteria

1. THE Postman_Collection SHALL estar en formato Postman Collection v2.1 (JSON) y SHALL ser importable directamente en Postman sin errores.
2. THE Postman_Collection SHALL organizar los requests en carpetas que correspondan a los módulos: `Auth`, `Experiences`, `Schedules`, `Reservations`, `Payments`, `Reviews`, `Admin`, `Health`.
3. THE Postman_Environment SHALL definir las variables: `baseUrl` (valor por defecto `http://localhost:8080`), `adminEmail`, `adminPassword`, `touristEmail`, `touristPassword`, `jwtToken`, `adminToken`, `touristToken`, `experienceId`, `scheduleId`, `reservationId`.
4. THE Postman_Collection SHALL incluir un Pre-request Script a nivel de colección que verifique si `jwtToken` está definido y no ha expirado; IF el token está ausente o expirado, THEN el script SHALL ejecutar automáticamente `POST /api/v1/auth/login` con las credenciales de la variable de entorno y SHALL almacenar el token resultante en la variable `jwtToken`; WHILE el token es válido y no ha expirado, THE Pre-request Script SHALL omitir la ejecución del login y reutilizar el token existente.
5. THE Postman_Collection SHALL configurar el header `Authorization: Bearer {{jwtToken}}` en todos los requests que requieran autenticación, usando la variable de entorno.
6. THE Postman_Collection SHALL incluir un request de `POST /api/v1/auth/login` con credenciales de admin que almacene el token en `adminToken`, y otro con credenciales de turista que almacene el token en `touristToken`.

---

### Requirement 7: Colección Postman — escenarios de prueba

**User Story:** Como QA del proyecto, quiero que cada request de la colección Postman incluya tests automatizados para happy path y casos de error, para poder ejecutar una regresión completa con un solo clic.

#### Acceptance Criteria

1. THE Postman_Collection SHALL incluir tests de Postman (scripts `pm.test`) para cada request que verifiquen al menos: el código de estado HTTP esperado, que el body de respuesta sea JSON válido, y que los campos obligatorios del response estén presentes.
2. WHEN el request es `POST /api/v1/auth/register` o `POST /api/v1/auth/login` con credenciales válidas, THE Postman_Collection SHALL incluir un test que verifique que el campo `token` está presente en la respuesta y SHALL almacenar el token en la variable de entorno correspondiente.
3. THE Postman_Collection SHALL incluir al menos un escenario de error por módulo: registro con email duplicado (HTTP 409), login con contraseña incorrecta (HTTP 401), creación de reserva sin cupos (HTTP 409), pago de reserva expirada (HTTP 422), reseña sin reserva confirmada (HTTP 403), acceso a endpoint admin con rol TOURIST (HTTP 403).
4. WHEN el request es `POST /api/v1/reservations`, THE Postman_Collection SHALL incluir un test que almacene el `id` de la reserva creada en la variable de entorno `reservationId`.
5. WHEN el request es `POST /api/v1/experiences` (admin), THE Postman_Collection SHALL incluir un test que almacene el `id` de la experiencia creada en la variable de entorno `experienceId`.
6. THE Postman_Collection SHALL incluir un flujo de prueba de extremo a extremo documentado en la descripción de la carpeta raíz: registro → login → consulta de experiencias → creación de reserva → simulación de pago → creación de reseña.

---

### Requirement 8: docker-compose para desarrollo local

**User Story:** Como desarrollador, quiero un archivo `docker-compose.yml` que levante el entorno completo de desarrollo local con un solo comando, para poder ejecutar la regresión Postman sin configuración manual de servicios externos.

#### Acceptance Criteria

1. THE docker-compose SHALL definir el servicio `db` usando la imagen `postgres:16-alpine` con las variables de entorno `POSTGRES_DB`, `POSTGRES_USER` y `POSTGRES_PASSWORD`, y SHALL exponer el puerto `5432`.
2. THE docker-compose SHALL definir el servicio `mailhog` usando la imagen `mailhog/mailhog:latest`, SHALL exponer el puerto SMTP `1025` para el backend y el puerto web `8025` para la interfaz de inspección de correos.
3. THE docker-compose SHALL definir el servicio `backend` construido desde el `Dockerfile` del proyecto, con todas las variables de entorno requeridas por `application.yml`: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CORS_ALLOWED_ORIGINS`.
4. WHEN el servicio `backend` se inicia, THE docker-compose SHALL garantizar que el servicio `db` esté saludable antes de iniciar el backend, usando `depends_on` con `condition: service_healthy` y un `healthcheck` en el servicio `db`; IF el healthcheck del servicio `db` falla repetidamente, THEN el servicio `backend` SHALL reintentar el inicio con un intervalo de espera configurable (`restart: on-failure`) en lugar de fallar permanentemente.
5. THE docker-compose SHALL configurar `MAIL_HOST=mailhog` y `MAIL_PORT=1025` en el servicio `backend` para que los correos sean capturados por MailHog en lugar de enviarse realmente.
6. THE docker-compose SHALL definir un volumen nombrado `postgres_data` para persistir los datos de PostgreSQL entre reinicios del contenedor.
7. THE docker-compose SHALL exponer el puerto `8080` del servicio `backend` al host, de modo que la colección Postman con `baseUrl=http://localhost:8080` pueda ejecutarse directamente contra el entorno dockerizado.
8. THE docker-compose SHALL incluir comentarios que expliquen el propósito de cada servicio y las variables de entorno configuradas.

---

### Requirement 9: Configuración de springdoc en application.yml

**User Story:** Como desarrollador, quiero que la configuración de springdoc-openapi esté centralizada en `application.yml`, para poder controlar el comportamiento de la documentación mediante variables de entorno sin modificar el código.

#### Acceptance Criteria

1. THE Sistema SHALL agregar la sección `springdoc` en `application.yml` con las propiedades: `api-docs.path=/v3/api-docs`, `swagger-ui.path=/swagger-ui.html`, `swagger-ui.enabled=true` (sobreescribible por variable de entorno), y `swagger-ui.try-it-out-enabled=true`.
2. THE Sistema SHALL configurar `springdoc.swagger-ui.operations-sorter=alpha` para que los endpoints aparezcan ordenados alfabéticamente por método HTTP dentro de cada tag.
3. THE Sistema SHALL configurar `springdoc.swagger-ui.tags-sorter=alpha` para que los tags (módulos) aparezcan ordenados alfabéticamente en Swagger UI.
4. IF la variable de entorno `SPRINGDOC_SWAGGER_UI_ENABLED` está definida como `false`, THEN THE Sistema SHALL deshabilitar Swagger UI en cualquier perfil de ejecución sin afectar la disponibilidad del endpoint `/v3/api-docs`.

---

### Requirement 10: Calidad y mantenibilidad de la documentación

**User Story:** Como equipo de desarrollo, queremos que la documentación OpenAPI sea precisa, completa y fácil de mantener, para que refleje siempre el estado real de la API sin esfuerzo manual adicional.

#### Acceptance Criteria

1. THE OpenAPI_Spec SHALL generarse automáticamente a partir de las anotaciones existentes de Spring MVC (`@RestController`, `@RequestMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`) y Jakarta Validation (`@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Email`, `@Future`, `@Positive`) sin duplicar información en anotaciones adicionales de OpenAPI cuando no sea necesario.
2. THE OpenAPI_Spec SHALL incluir descripciones de operación y de tag generadas automáticamente a partir de los nombres de métodos y clases de los controllers; WHERE los nombres de métodos o clases no sean suficientemente descriptivos, THE Sistema SHALL permitir agregar anotaciones `@Operation` y `@Tag` para enriquecer la descripción sin requerir que todos los controllers las incluyan obligatoriamente.
3. THE OpenAPI_Spec SHALL reflejar correctamente los tipos de datos Java: `UUID` como `string (format: uuid)`, `LocalDate` como `string (format: date)`, `LocalDateTime` como `string (format: date-time)`, `LocalTime` como `string (format: time)`, `BigDecimal` como `number`, y los enums (`Role`, `Difficulty`, `ReservationStatus`, `PaymentStatus`, `DayOfWeek`) como `string` con los valores permitidos listados.
4. THE OpenAPI_Spec SHALL marcar como `required` todos los campos que tengan la anotación `@NotNull` o `@NotBlank` en los DTOs de request.
5. WHEN se agrega un nuevo endpoint al backend, THE Sistema SHALL reflejar automáticamente el nuevo endpoint en la OpenAPI_Spec sin requerir cambios manuales en la documentación, siempre que el controller use las anotaciones estándar de Spring MVC.
