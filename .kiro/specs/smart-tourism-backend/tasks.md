# Implementation Plan: smart-tourism-backend

## Overview

Implementación incremental del backend de Turismo Inteligente Santander usando Java 21 y Spring Boot 3. Las tareas siguen el orden natural de dependencias: estructura base → seguridad → dominio → módulos funcionales → integración final. Cada módulo incluye sub-tareas de pruebas de propiedades (jqwik) y unitarias como opcionales.

## Tasks

- [x] 1. Configurar estructura base del proyecto Spring Boot
  - Crear proyecto Maven con Spring Boot 3, Java 21 y las dependencias necesarias: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-mail`, `spring-boot-starter-actuator`, `spring-boot-starter-validation`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`, `mapstruct`, `mapstruct-processor`, `flyway-core`, `postgresql`, `lombok`
  - Agregar dependencias de prueba: `jqwik 1.8.4`, `testcontainers-postgresql`, `spring-boot-starter-test`
  - Crear la estructura de paquetes bajo `com.smarttourism.backend`: `auth`, `users`, `experiences`, `schedules`, `reservations`, `payments`, `reviews`, `notifications`, `admin`, `security`, `config`, `common`
  - Crear `application.yml` que lea toda la configuración sensible desde variables de entorno: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CORS_ALLOWED_ORIGINS`
  - Crear `Dockerfile` multi-stage (build con Maven + runtime con JRE 21-slim) compatible con Koyeb
  - _Requirements: 12.1, 12.5, 12.6_

- [x] 2. Implementar migraciones Flyway y entidades JPA
  - [x] 2.1 Crear scripts de migración Flyway en `src/main/resources/db/migration/`
    - `V1__create_users_table.sql`: tabla `users` con columnas `id` (UUID PK), `full_name`, `email` (UNIQUE), `password`, `phone`, `document_number` (UNIQUE), `role` (ENUM), `active`, `created_at`, `updated_at`
    - `V2__create_experiences_table.sql`: tabla `experiences` con columnas `id`, `title`, `description`, `category`, `location`, `duration`, `difficulty` (ENUM), `price`, `images` (TEXT[]), `active`, `created_at`, `updated_at`
    - `V3__create_schedules_table.sql`: tabla `schedules` con FK a `experiences`, columnas `day_of_week` (ENUM), `start_time`, `end_time`, `available_slots`, `active`
    - `V4__create_reservations_table.sql`: tabla `reservations` con FKs a `users`, `experiences`, `schedules`; columnas `reservation_date`, `quantity`, `total_amount`, `status` (ENUM), `expiration_date`, `created_at`, `updated_at`
    - `V5__create_payments_table.sql`: tabla `payments` con FK a `reservations`; columnas `payment_date`, `transaction_reference` (UNIQUE), `status` (ENUM), `amount`, `created_at`
    - `V6__create_reviews_table.sql`: tabla `reviews` con FKs a `users`, `experiences`; columnas `rating`, `comment`, `created_at`; constraint UNIQUE(`tourist_id`, `experience_id`)
    - `V7__seed_admin_user.sql`: insertar usuario administrador inicial con contraseña BCrypt
    - _Requirements: 12.2_
  - [x] 2.2 Crear entidades JPA en sus respectivos módulos
    - `User` en `users/entity/User.java`: mapear todos los campos, usar `@Enumerated(EnumType.STRING)` para `role`, implementar `UserDetails` de Spring Security
    - `Experience` en `experiences/entity/Experience.java`: mapear campos incluyendo `images` como `@Column(columnDefinition = "text[]")`
    - `Schedule` en `schedules/entity/Schedule.java`: relación `@ManyToOne` con `Experience`
    - `Reservation` en `reservations/entity/Reservation.java`: relaciones `@ManyToOne` con `User`, `Experience`, `Schedule`
    - `Payment` en `payments/entity/Payment.java`: relación `@ManyToOne` con `Reservation`
    - `Review` en `reviews/entity/Review.java`: relaciones `@ManyToOne` con `User`, `Experience`
    - Crear enums: `Role`, `Difficulty`, `DayOfWeek`, `ReservationStatus`, `PaymentStatus`
    - _Requirements: 12.1, 12.2_

- [x] 3. Implementar capa común: excepciones, DTOs compartidos y manejo global de errores
  - Crear jerarquía de excepciones en `common/exception/`: `ResourceNotFoundException`, `DuplicateResourceException`, `BusinessRuleException` (con subclases `InsufficientSlotsException`, `InvalidReservationStateException`, `PaymentNotAllowedException`), `InvalidDateException`, `UnauthorizedAccessException`
  - Crear `common/dto/ErrorResponse.java` con campos `timestamp`, `status`, `error`, `message`
  - Crear `GlobalExceptionHandler` en `common/exception/` con `@RestControllerAdvice` que mapee cada excepción a su código HTTP correspondiente y retorne `ErrorResponse`
  - _Requirements: 12.7_

- [x] 4. Implementar módulo de seguridad (JWT + Spring Security)
  - [x] 4.1 Implementar `JwtService` en `security/`
    - Método `generateToken(UserDetails userDetails)`: genera JWT con subject=email, claim `role`, expiración configurable desde variable de entorno
    - Método `validateToken(String token, UserDetails userDetails)`: verifica firma, expiración y subject
    - Método `extractEmail(String token)`: extrae el subject del token
    - Usar `io.jsonwebtoken` (JJWT) con secreto leído de `${JWT_SECRET}`
    - _Requirements: 2.1, 2.5, 2.6_
  - [x]* 4.2 Escribir prueba de propiedad para JwtService
    - **Property 3: Token JWT contiene rol y es verificable**
    - **Validates: Requirements 2.1, 2.6**
    - Generar usuarios con emails y roles arbitrarios, verificar que el token generado contiene el rol correcto y es validable con la misma clave
  - [x]* 4.3 Escribir prueba de propiedad para fallo de autenticación    - **Property 4: Fallo de autenticación nunca retorna HTTP 200**
    - **Validates: Requirements 2.2, 2.3**
    - Generar credenciales inválidas arbitrarias y verificar que la respuesta nunca es 200
  - [x] 4.4 Implementar `JwtAuthenticationFilter` en `security/`
    - Extender `OncePerRequestFilter`
    - Extraer token del header `Authorization: Bearer <token>`, validarlo con `JwtService`, cargar `UserDetails` y setear en `SecurityContextHolder`
    - _Requirements: 2.5_
  - [x] 4.5 Implementar `UserDetailsServiceImpl` in `security/`
    - Cargar usuario por email desde `UserRepository`, lanzar `UsernameNotFoundException` si no existe
    - _Requirements: 2.4_
  - [x] 4.6 Configurar `SecurityFilterChain` en `security/SecurityConfig.java`
    - Deshabilitar CSRF, configurar sesión stateless
    - Rutas públicas: `POST /api/v1/auth/**`, `GET /api/v1/experiences/**`, `GET /api/v1/reviews/**`, `GET /actuator/health`
    - Rutas TOURIST: `POST /api/v1/reservations`, `GET /api/v1/reservations/me`, `PATCH /api/v1/reservations/*/cancel`, `POST /api/v1/payments/**`, `POST /api/v1/reviews`
    - Rutas ADMIN: `POST/PUT/DELETE /api/v1/experiences/**`, `POST/PUT/DELETE /api/v1/experiences/*/schedules/**`, `/api/v1/admin/**`
    - Agregar `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`
    - _Requirements: 2.4, 9.4_

- [x] 5. Implementar módulo de autenticación (`auth`)
  - [x] 5.1 Crear DTOs en `auth/dto/`: `RegisterRequest`, `LoginRequest`, `AuthResponse` (con `token`, `user`)
  - [x] 5.2 Implementar `AuthService` en `auth/service/`
    - Método `register(RegisterRequest)`: validar unicidad de email y documentNumber (lanzar `DuplicateResourceException` si ya existen), hashear contraseña con `BCryptPasswordEncoder`, crear usuario con rol `TOURIST` y `active=true`, generar y retornar JWT
    - Método `login(LoginRequest)`: autenticar con `AuthenticationManager`, verificar que el usuario esté activo (lanzar `UnauthorizedAccessException` si no), generar y retornar JWT
    - _Requirements: 1.1, 1.2, 1.3, 1.5, 1.6, 2.1, 2.2, 2.3, 2.4_
  - [x]* 5.3 Escribir prueba de propiedad para unicidad de email y documento
    - **Property 1: Unicidad de email y documento en registro**
    - **Validates: Requirements 1.2, 1.3**
    - Generar pares de solicitudes de registro con email o documentNumber duplicado y verificar que la segunda siempre retorna 409
  - [x]* 5.4 Escribir prueba de propiedad para almacenamiento de contraseña
    - **Property 2: Contraseña almacenada como hash BCrypt**
    - **Validates: Requirements 1.5**
    - Para contraseñas arbitrarias, verificar que el valor persistido es un hash BCrypt válido y distinto del texto plano
  - [x] 5.5 Implementar `AuthController` en `auth/controller/`
    - `POST /api/v1/auth/register` → `AuthService.register()`, retornar 201
    - `POST /api/v1/auth/login` → `AuthService.login()`, retornar 200
    - Usar `@Valid` en los request bodies
    - _Requirements: 1.1, 1.4, 2.1_

- [x] 6. Checkpoint — Verificar autenticación base
  - Asegurar que todos los tests del módulo `auth` y `security` pasan. Verificar que el filtro JWT funciona correctamente con pruebas de integración básicas. Consultar al usuario si hay dudas.

- [x] 7. Implementar módulo de experiencias (`experiences`)
  - [x] 7.1 Crear `ExperienceRepository` en `experiences/repository/`
    - Extender `JpaRepository<Experience, UUID>` y `JpaSpecificationExecutor<Experience>`
    - _Requirements: 3.1, 4.1_
  - [x] 7.2 Crear `ExperienceSpecification` en `experiences/specification/`
    - Implementar predicados para filtros: `category` (exact match), `location` (LIKE insensible a mayúsculas), `difficulty` (exact match), `minPrice`/`maxPrice` (rango), `available` (join con schedules donde `availableSlots > 0`)
    - Combinar predicados con AND cuando se proporcionan múltiples filtros
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_
  - [x] 7.3 Crear DTOs y mapper: `ExperienceRequest`, `ExperienceResponse` (con `averageRating`, `reviewCount`, lista de `ScheduleResponse`), `ExperienceMapper` (MapStruct)
    - _Requirements: 3.1, 3.2, 8.5_
  - [x] 7.4 Implementar `ExperienceService` en `experiences/service/`
    - `getExperiences(filtros, pageable)`: aplicar `ExperienceSpecification`, retornar solo activas
    - `getExperienceById(UUID)`: retornar experiencia con horarios y estadísticas de reseñas, lanzar `ResourceNotFoundException` si no existe o está inactiva
    - `createExperience(ExperienceRequest)`: crear y persistir experiencia activa
    - `updateExperience(UUID, ExperienceRequest)`: actualizar campos, lanzar 404 si no existe
    - `deleteExperience(UUID)`: borrado lógico (`active = false`)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 9.1, 9.2, 9.3_
  - [x]* 7.5 Escribir prueba de propiedad para filtros conjuntivos
    - **Property 5: Filtros de experiencias son conjuntivos (AND)**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6**
    - Generar combinaciones arbitrarias de filtros válidos y verificar que cada resultado satisface todos los filtros aplicados simultáneamente
  - [x] 7.6 Implementar `ExperienceController` en `experiences/controller/`
    - `GET /api/v1/experiences` con parámetros opcionales de filtro y paginación → `ExperienceService.getExperiences()`
    - `GET /api/v1/experiences/{id}` → `ExperienceService.getExperienceById()`
    - `POST /api/v1/experiences` (ADMIN) → `ExperienceService.createExperience()`
    - `PUT /api/v1/experiences/{id}` (ADMIN) → `ExperienceService.updateExperience()`
    - `DELETE /api/v1/experiences/{id}` (ADMIN) → `ExperienceService.deleteExperience()`
    - _Requirements: 3.1, 3.2, 3.3, 9.1, 9.2, 9.3, 9.4_

- [x] 8. Implementar módulo de horarios (`schedules`)
  - Crear `ScheduleRepository` extendiendo `JpaRepository<Schedule, UUID>`
  - Crear DTOs: `ScheduleRequest`, `ScheduleResponse` y `ScheduleMapper` (MapStruct)
  - Implementar `ScheduleService`: `createSchedule(UUID experienceId, ScheduleRequest)`, `updateSchedule(UUID, ScheduleRequest)`, `deactivateSchedule(UUID)` (borrado lógico)
  - Implementar `ScheduleController` bajo `/api/v1/experiences/{id}/schedules` (solo ADMIN): `POST`, `PUT /{scheduleId}`, `DELETE /{scheduleId}`
  - _Requirements: 9.5_

- [x] 9. Implementar módulo de reservas (`reservations`)
  - [x] 9.1 Crear `ReservationRepository` en `reservations/repository/`
    - Método `findByTouristIdOrderByCreatedAtDesc(UUID touristId)`
    - Método `findByStatusAndExpirationDateBefore(ReservationStatus status, LocalDateTime dateTime)` para el scheduler
    - _Requirements: 5.5, 6.2_
  - [x] 9.2 Crear DTOs y mapper: `ReservationRequest`, `ReservationResponse` (con `totalAmount`, `expirationDate`, `status`), `ReservationMapper` (MapStruct)
    - _Requirements: 5.1, 5.2_
  - [x] 9.3 Implementar `ReservationService` en `reservations/service/`
    - `createReservation(UUID touristId, ReservationRequest)`: dentro de `@Transactional`, hacer `SELECT FOR UPDATE` sobre el `Schedule`, verificar `availableSlots >= quantity` (lanzar `InsufficientSlotsException` si no), decrementar `availableSlots`, calcular `totalAmount = price * quantity`, calcular `expirationDate = now() + 15 min`, crear reserva con estado `PENDING_PAYMENT`
    - `getMyReservations(UUID touristId)`: retornar lista ordenada por `createdAt` desc
    - `cancelReservation(UUID reservationId, UUID touristId)`: verificar propiedad (lanzar `UnauthorizedAccessException` si no), verificar estado cancelable (`PENDING_PAYMENT` o `CONFIRMED`, lanzar `InvalidReservationStateException` si no), cambiar estado a `CANCELLED`, restaurar `availableSlots` en el `Schedule`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 6.1, 6.4, 6.5, 6.6_
  - [x]* 9.4 Escribir prueba de propiedad para control de sobreventa
    - **Property 6: Control de sobreventa — los cupos nunca son negativos**
    - **Validates: Requirements 6.1, 5.3**
    - Generar secuencias de creaciones de reservas concurrentes y verificar que `availableSlots` nunca es negativo y la suma de reservas activas nunca supera la capacidad original
  - [x]* 9.5 Escribir prueba de propiedad para restauración de cupos
    - **Property 7: Restauración de cupos en cancelación y expiración**
    - **Validates: Requirements 5.6, 6.2**
    - Para reservas que transitan a `CANCELLED` o `EXPIRED`, verificar que `availableSlots` se incrementa exactamente en `quantity`
  - [x] 9.6 Implementar `ExpirationService` en `reservations/service/`
    - Método `expireReservations()` anotado con `@Scheduled(fixedDelay = 60000)`
    - Buscar reservas con `status = PENDING_PAYMENT` y `expirationDate < now()`
    - Por cada reserva: dentro de `@Transactional`, cambiar estado a `EXPIRED`, restaurar `availableSlots` en el `Schedule`, disparar notificación asíncrona
    - _Requirements: 6.2, 6.3_
  - [x]* 9.7 Escribir prueba de propiedad para expiración automática
    - **Property 8: Expiración automática a los 15 minutos**
    - **Validates: Requirements 6.2, 6.3**
    - Crear reservas con `expirationDate` en el pasado y verificar que `ExpirationService` las marca como `EXPIRED` y restaura cupos
  - [x] 9.8 Implementar `ReservationController` en `reservations/controller/`
    - `POST /api/v1/reservations` (TOURIST) → `ReservationService.createReservation()`
    - `GET /api/v1/reservations/me` (TOURIST) → `ReservationService.getMyReservations()`
    - `PATCH /api/v1/reservations/{id}/cancel` (TOURIST) → `ReservationService.cancelReservation()`
    - _Requirements: 5.1, 5.5, 5.6_

- [x] 10. Checkpoint — Verificar módulo de reservas
  - Asegurar que todos los tests de reservas pasan, incluyendo las propiedades de sobreventa y restauración de cupos. Consultar al usuario si hay dudas.

- [x] 11. Implementar módulo de pagos (`payments`)
  - [x] 11.1 Crear `PaymentRepository` extendiendo `JpaRepository<Payment, UUID>`
  - [x] 11.2 Crear DTOs y mapper: `PaymentRequest` (con `reservationId`), `PaymentResponse` (con `paymentStatus`, `transactionReference`, `amount`), `PaymentMapper` (MapStruct)
    - _Requirements: 7.1, 7.6_
  - [x] 11.3 Implementar `PaymentService` en `payments/service/`
    - `simulatePayment(UUID reservationId, UUID touristId)`: verificar que la reserva existe y pertenece al turista (lanzar 404/403), verificar que el estado es `PENDING_PAYMENT` (lanzar `PaymentNotAllowedException` si no), dentro de `@Transactional`: generar `transactionReference` UUID único, simular resultado (lógica aleatoria configurable), crear registro `Payment`, si `APPROVED` actualizar reserva a `CONFIRMED`, retornar `PaymentResponse`
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_
  - [x]* 11.4 Escribir prueba de propiedad para confirmación de reserva solo por pago APPROVED
    - **Property 9: Confirmación de reserva solo por pago APPROVED**
    - **Validates: Requirements 7.2, 7.3**
    - Para todos los posibles `PaymentStatus` distintos de `APPROVED`, verificar que la reserva no cambia a `CONFIRMED`; para `APPROVED`, verificar que sí cambia
  - [x] 11.5 Implementar `PaymentController` en `payments/controller/`
    - `POST /api/v1/payments/simulate` (TOURIST) → `PaymentService.simulatePayment()`
    - _Requirements: 7.1_

- [x] 12. Implementar módulo de reseñas (`reviews`)
  - [x] 12.1 Crear `ReviewRepository` en `reviews/repository/`
    - Método `existsByTouristIdAndExperienceId(UUID touristId, UUID experienceId)`
    - Método `findByExperienceId(UUID experienceId)` para calcular promedio
    - _Requirements: 8.4, 8.5_
  - [x] 12.2 Crear DTOs y mapper: `ReviewRequest` (con `experienceId`, `rating`, `comment`), `ReviewResponse`, `ReviewMapper` (MapStruct)
    - Agregar validación `@Min(1) @Max(5)` en `rating` del `ReviewRequest`
    - _Requirements: 8.1, 8.2_
  - [x] 12.3 Implementar `ReviewService` en `reviews/service/`
    - `createReview(UUID touristId, ReviewRequest)`: verificar que el turista tiene al menos una reserva `CONFIRMED` para la experiencia (lanzar `UnauthorizedAccessException` con 403 si no), verificar unicidad (lanzar `DuplicateResourceException` si ya existe reseña), crear y persistir reseña
    - `getReviewsByExperience(UUID experienceId)`: retornar lista de reseñas
    - _Requirements: 8.1, 8.3, 8.4_
  - [x]* 12.4 Escribir prueba de propiedad para reseña solo con reserva CONFIRMED
    - **Property 10: Reseña solo para experiencias con reserva CONFIRMED**
    - **Validates: Requirements 8.3**
    - Generar turistas sin reserva confirmada para una experiencia y verificar que la creación de reseña retorna 403
  - [x]* 12.5 Escribir prueba de propiedad para rating en rango válido
    - **Property 11: Rating dentro del rango válido [1, 5]**
    - **Validates: Requirements 8.2**
    - Generar valores de rating fuera del rango [1,5] y verificar rechazo con 400; generar valores dentro del rango y verificar aceptación
  - [x] 12.6 Implementar `ReviewController` en `reviews/controller/`
    - `POST /api/v1/reviews` (TOURIST) → `ReviewService.createReview()`
    - `GET /api/v1/experiences/{id}/reviews` (público) → `ReviewService.getReviewsByExperience()`
    - _Requirements: 8.1, 8.5_

- [x] 13. Implementar módulo de notificaciones (`notifications`)
  - Crear `NotificationService` en `notifications/service/`
    - Inyectar `JavaMailSender` y `@Value` para el email remitente
    - Método `sendReservationCreatedEmail(String to, ReservationResponse reservation)`: enviar correo con detalles de reserva y tiempo límite de pago; anotar con `@Async`
    - Método `sendPaymentResultEmail(String to, PaymentResponse payment, ReservationResponse reservation)`: enviar correo con resultado del pago; anotar con `@Async`
    - Método `sendCancellationEmail(String to, ReservationResponse reservation, boolean eligibleForRefund)`: enviar correo de cancelación; anotar con `@Async`
    - Método `sendExpirationEmail(String to, ReservationResponse reservation)`: enviar correo de expiración; anotar con `@Async`
    - Capturar excepciones de envío dentro de cada método y registrar con `log.error()`
  - Configurar `@EnableAsync` en `config/AsyncConfig.java`
  - Integrar llamadas a `NotificationService` en `AuthService` (registro), `ReservationService` (creación, cancelación), `PaymentService` (resultado de pago) y `ExpirationService` (expiración)
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_

- [x] 14. Implementar módulo de administración (`admin`)
  - [x] 14.1 Implementar `AdminReservationController` en `admin/controller/`
    - `GET /api/v1/admin/reservations` (ADMIN): retornar lista paginada con filtros opcionales por `status`, `experienceId` y rango de fechas
    - Crear `AdminReservationService` con método `getAllReservations(filtros, pageable)` usando `JPA Specification`
    - _Requirements: 9.6_
  - [x] 14.2 Implementar `AdminUserController` y `AdminUserService` en `admin/`
    - `GET /api/v1/admin/users` (ADMIN): retornar lista paginada de todos los usuarios
    - `PATCH /api/v1/admin/users/{id}/status` (ADMIN): actualizar campo `active` del usuario; si se desactiva, el usuario no podrá autenticarse (verificado en `UserDetailsServiceImpl`)
    - _Requirements: 9.7, 9.8_
  - [x]* 14.3 Escribir prueba de propiedad para endpoints de administración inaccesibles para TOURIST
    - **Property 12: Endpoints de administración inaccesibles para TOURIST**
    - **Validates: Requirements 9.4**
    - Generar tokens JWT con rol `TOURIST` y verificar que cualquier solicitud a endpoints de administración retorna 403

- [x] 15. Implementar configuración de CORS y Actuator
  - Crear `CorsConfig.java` en `config/` con `@Bean CorsConfigurationSource`
    - Leer orígenes permitidos desde `${CORS_ALLOWED_ORIGINS}` (lista separada por comas)
    - Métodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
    - Headers permitidos: `Authorization`, `Content-Type`, `Accept`
    - `allowCredentials = true`
  - Configurar `management.endpoints.web.exposure.include=health` en `application.yml`
  - Configurar `management.endpoint.health.show-details=always` para mostrar estado de DB
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 12.4_
- [x] 16. Implementar prueba de propiedad para formato de error uniforme
  - [x]* 16.1 Escribir prueba de propiedad para formato de error uniforme
    - **Property 13: Formato de error uniforme**
    - **Validates: Requirements 12.7**
    - Para distintos tipos de errores (4xx, 5xx), verificar que el cuerpo de respuesta siempre contiene los campos `timestamp`, `status`, `error` y `message`

- [x] 17. Pruebas de integración con Testcontainers
  - [x]* 17.1 Escribir prueba de integración para el flujo completo: registro → login → reserva → pago → reseña
    - Levantar contenedor PostgreSQL con Testcontainers
    - Verificar que el flujo completo funciona end-to-end con datos reales en base de datos
    - _Requirements: 1.1, 2.1, 5.1, 7.1, 8.1_
  - [x]* 17.2 Escribir prueba de integración para configuración de CORS y health check
    - Verificar que las solicitudes preflight `OPTIONS` retornan 200 con headers CORS correctos
    - Verificar que `GET /actuator/health` retorna estado del sistema y de la base de datos
    - _Requirements: 11.4, 12.4_

- [x] 18. Checkpoint final — Verificar integridad completa del sistema
  - Ejecutar todos los tests (unitarios, de propiedades e integración) y asegurar que pasan
  - Verificar que el `Dockerfile` construye correctamente la imagen
  - Verificar que la aplicación arranca con variables de entorno configuradas
  - Consultar al usuario si hay dudas antes de considerar la implementación completa

## Notes

- Las sub-tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia los requisitos específicos para trazabilidad
- Los checkpoints garantizan validación incremental del sistema
- Las pruebas de propiedades usan **jqwik 1.8.4** con `@Property(tries = 100)` mínimo
- Cada prueba de propiedad debe incluir el comentario: `// Feature: smart-tourism-backend, Property N: <texto>`
- Las pruebas de integración usan **Testcontainers** con un perfil `test` separado
- El bloqueo pesimista (`SELECT FOR UPDATE`) en la creación de reservas es crítico para evitar sobreventa
- Toda la configuración sensible se lee desde variables de entorno, sin valores hardcodeados

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["2.1"] },
    { "id": 1, "tasks": ["2.2"] },
    { "id": 2, "tasks": ["4.1", "4.4", "4.5"] },
    { "id": 3, "tasks": ["4.2", "4.3", "4.6"] },
    { "id": 4, "tasks": ["5.1", "7.1"] },
    { "id": 5, "tasks": ["5.2", "7.2", "7.3"] },
    { "id": 6, "tasks": ["5.3", "5.4", "5.5", "7.4"] },
    { "id": 7, "tasks": ["7.5", "7.6", "9.1", "9.2"] },
    { "id": 8, "tasks": ["9.3", "11.1", "12.1"] },
    { "id": 9, "tasks": ["9.4", "9.5", "9.6", "11.2", "12.2"] },
    { "id": 10, "tasks": ["9.7", "9.8", "11.3", "12.3", "14.1", "14.2"] },
    { "id": 11, "tasks": ["11.4", "11.5", "12.4", "12.5", "12.6", "14.3"] },
    { "id": 12, "tasks": ["16.1"] },
    { "id": 13, "tasks": ["17.1", "17.2"] }
  ]
}
```
