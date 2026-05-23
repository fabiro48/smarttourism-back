# Requirements Document

## Introduction

El backend de la plataforma **Turismo Inteligente smart-tourism** es un sistema monolítico modular desarrollado con Java 21 y Spring Boot 3. Su propósito es gestionar el catálogo de experiencias turísticas del departamento de Santander, permitir a los turistas consultar, reservar y pagar experiencias, y ofrecer a los administradores herramientas para gestionar el contenido y las operaciones de la plataforma.

El sistema expone una API REST bajo el prefijo `/api/v1`, protegida con JWT, y está diseñado para desplegarse en Koyeb mediante Docker. La arquitectura sigue el patrón de capas: Presentación → Aplicación → Dominio → Persistencia.

---

## Glossary

- **Sistema**: El backend de la plataforma Turismo Inteligente Santander.
- **Turista**: Usuario registrado con rol `TOURIST` que puede consultar, reservar y calificar experiencias.
- **Administrador**: Usuario con rol `ADMIN` que gestiona el contenido y las operaciones de la plataforma.
- **Experiencia**: Actividad turística ofertada en la plataforma, con categoría, ubicación, dificultad, capacidad y precio.
- **Horario (Schedule)**: Franja horaria asociada a una experiencia, con día de la semana, hora de inicio, hora de fin y cupos disponibles.
- **Reserva**: Solicitud de un turista para participar en una experiencia en una fecha y horario específicos.
- **Pago**: Transacción simulada que confirma o rechaza una reserva.
- **Reseña**: Calificación y comentario que un turista deja sobre una experiencia completada.
- **JWT**: JSON Web Token, mecanismo de autenticación sin estado utilizado para proteger los endpoints.
- **Flyway**: Herramienta de migración de base de datos que gestiona el versionado del esquema.
- **MapStruct**: Librería de mapeo entre entidades y DTOs en tiempo de compilación.
- **CORS**: Política de intercambio de recursos entre orígenes, necesaria para que el frontend pueda consumir la API.
- **ReservationStatus**: Estado de una reserva. Valores: `PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`, `EXPIRED`, `NO_SHOW`.
- **PaymentStatus**: Estado de un pago. Valores: `PENDING`, `APPROVED`, `REJECTED`, `EXPIRED`.

---

## Requirements

### Requirement 1: Registro de usuarios

**User Story:** Como visitante de la plataforma, quiero registrarme con mis datos personales, para poder acceder a las funcionalidades de reserva y calificación de experiencias.

#### Acceptance Criteria

1. WHEN un visitante envía una solicitud `POST /api/v1/auth/register` con `fullName`, `email`, `password`, `phone` y `documentNumber` válidos, THE Sistema SHALL crear un nuevo usuario con rol `TOURIST` y estado activo, y SHALL retornar un token JWT válido junto con los datos del usuario creado.
2. IF el `email` proporcionado ya existe en la base de datos, THEN THE Sistema SHALL retornar un error HTTP 409 con un mensaje descriptivo indicando que el correo ya está registrado.
3. IF el `documentNumber` proporcionado ya existe en la base de datos, THEN THE Sistema SHALL retornar un error HTTP 409 con un mensaje descriptivo indicando que el documento ya está registrado.
4. IF alguno de los campos obligatorios (`fullName`, `email`, `password`, `documentNumber`) está ausente o vacío, THEN THE Sistema SHALL retornar un error HTTP 400 con la lista de campos inválidos.
5. THE Sistema SHALL almacenar la contraseña del usuario cifrada con BCrypt antes de persistirla en la base de datos.
6. WHEN un usuario es registrado exitosamente, THE Sistema SHALL asignar automáticamente el rol `TOURIST` sin requerir intervención del administrador.

---

### Requirement 2: Autenticación con email y contraseña

**User Story:** Como usuario registrado, quiero iniciar sesión con mi email y contraseña, para obtener un token JWT que me permita acceder a los recursos protegidos de la plataforma.

#### Acceptance Criteria

1. WHEN un usuario envía una solicitud `POST /api/v1/auth/login` con `email` y `password` correctos, THE Sistema SHALL retornar un token JWT con tiempo de expiración configurado y los datos básicos del usuario autenticado.
2. IF el `email` no existe en el sistema, THEN THE Sistema SHALL retornar un error HTTP 401 con un mensaje genérico que no revele si el email existe o no, y SHALL garantizar que en ningún caso de fallo de autenticación se retorne un código HTTP 200.
3. IF la `password` es incorrecta, THEN THE Sistema SHALL retornar un error HTTP 401 con el mismo mensaje genérico del criterio anterior, y SHALL garantizar que en ningún caso de fallo de autenticación se retorne un código HTTP 200.
4. IF el usuario tiene estado inactivo o bloqueado, THEN THE Sistema SHALL retornar un error HTTP 403 indicando que la cuenta no está habilitada.
5. THE Sistema SHALL validar el token JWT en cada solicitud a endpoints protegidos y SHALL rechazar tokens expirados, malformados o con firma inválida con un error HTTP 401.
6. THE Sistema SHALL incluir el rol del usuario (`ADMIN` o `TOURIST`) en el payload del JWT para autorización basada en roles.

---

### Requirement 3: Consulta del catálogo de experiencias

**User Story:** Como turista, quiero consultar el catálogo de experiencias disponibles, para explorar las opciones turísticas de Santander.

#### Acceptance Criteria

1. WHEN un cliente envía una solicitud `GET /api/v1/experiences`, THE Sistema SHALL retornar una lista paginada de experiencias activas con sus datos principales: `id`, `title`, `description`, `category`, `location`, `duration`, `difficulty`, `price` e `images`.
2. WHEN un cliente envía una solicitud `GET /api/v1/experiences/{id}` con un identificador válido, THE Sistema SHALL retornar el detalle completo de la experiencia incluyendo sus horarios disponibles.
3. IF el `id` de la experiencia no existe o la experiencia está inactiva, THEN THE Sistema SHALL retornar un error HTTP 404 con un mensaje descriptivo.
4. THE Sistema SHALL retornar únicamente experiencias con el campo `active = true` en los listados públicos.
5. THE Sistema SHALL responder a las solicitudes de consulta del catálogo en menos de 3 segundos bajo condiciones normales de operación.

---

### Requirement 4: Filtrado de experiencias

**User Story:** Como turista, quiero filtrar las experiencias por categoría, ubicación, dificultad, precio y disponibilidad, para encontrar rápidamente las opciones que se ajusten a mis preferencias.

#### Acceptance Criteria

1. WHEN un cliente envía una solicitud `GET /api/v1/experiences` con el parámetro `category`, THE Sistema SHALL retornar únicamente las experiencias activas que coincidan con la categoría especificada.
2. WHEN un cliente envía una solicitud `GET /api/v1/experiences` con el parámetro `location`, THE Sistema SHALL retornar únicamente las experiencias activas cuya ubicación contenga el valor especificado (búsqueda parcial, insensible a mayúsculas).
3. WHEN un cliente envía una solicitud `GET /api/v1/experiences` con el parámetro `difficulty`, THE Sistema SHALL retornar únicamente las experiencias activas con el nivel de dificultad especificado.
4. WHEN un cliente envía una solicitud `GET /api/v1/experiences` con los parámetros `minPrice` y/o `maxPrice`, THE Sistema SHALL retornar únicamente las experiencias activas cuyo precio esté dentro del rango especificado.
5. WHEN un cliente envía una solicitud `GET /api/v1/experiences` con el parámetro `available=true`, THE Sistema SHALL retornar únicamente las experiencias activas que tengan al menos un horario con `availableSlots > 0`.
6. WHEN se aplican múltiples filtros en una misma solicitud, THE Sistema SHALL excluir de los resultados las experiencias que no cumplan con todas las condiciones de filtro especificadas (operador AND), de modo que solo se retornen experiencias que satisfagan simultáneamente todos los criterios proporcionados.
7. IF se proporcionan valores de filtro inválidos (por ejemplo, `minPrice` negativo), THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje descriptivo.

---

### Requirement 5: Gestión de reservas por el turista

**User Story:** Como turista autenticado, quiero crear, consultar y cancelar mis reservas, para administrar mi participación en las experiencias turísticas.

#### Acceptance Criteria

1. WHEN un turista autenticado envía una solicitud `POST /api/v1/reservations` con `experienceId`, `scheduleId`, `reservationDate` y `quantity` válidos, THE Sistema SHALL crear una reserva con estado `PENDING_PAYMENT`, SHALL descontar temporalmente los cupos solicitados del horario correspondiente, y SHALL retornar los datos de la reserva creada incluyendo `totalAmount` y `expirationDate`.
2. WHEN se crea una reserva, THE Sistema SHALL calcular `totalAmount` como `price * quantity` y SHALL calcular `expirationDate` como la fecha y hora de creación más 15 minutos.
3. IF el horario solicitado no tiene suficientes `availableSlots` para la `quantity` solicitada, THEN THE Sistema SHALL retornar un error HTTP 409 indicando que no hay cupos disponibles, sin crear la reserva.
4. IF la `reservationDate` es anterior a la fecha actual, THEN THE Sistema SHALL retornar un error HTTP 400 indicando que la fecha de reserva no es válida.
5. WHEN un turista autenticado envía una solicitud `GET /api/v1/reservations/me`, THE Sistema SHALL retornar la lista de todas las reservas del turista autenticado, ordenadas por fecha de creación descendente.
6. WHEN un turista autenticado envía una solicitud `PATCH /api/v1/reservations/{id}/cancel` sobre una reserva propia con estado `PENDING_PAYMENT` o `CONFIRMED`, THE Sistema SHALL cambiar el estado de la reserva a `CANCELLED` y SHALL restaurar los cupos al horario correspondiente.
7. IF un turista intenta cancelar una reserva que no le pertenece, THEN THE Sistema SHALL retornar un error HTTP 403.
8. IF un turista intenta cancelar una reserva con estado `CANCELLED`, `EXPIRED` o `NO_SHOW`, THEN THE Sistema SHALL retornar un error HTTP 422 indicando que la reserva no puede ser cancelada en su estado actual.

---

### Requirement 6: Reglas de negocio de reservas

**User Story:** Como plataforma, necesito aplicar reglas de negocio sobre las reservas para garantizar la integridad de los cupos y la equidad entre los usuarios.

#### Acceptance Criteria

1. THE Sistema SHALL garantizar que en ningún momento el número de cupos reservados para un horario supere la capacidad máxima definida en el campo `availableSlots` del horario (control de sobreventa).
2. WHEN han transcurrido 15 minutos desde la creación de una reserva con estado `PENDING_PAYMENT` sin que se haya registrado un pago aprobado, THE Sistema SHALL cambiar automáticamente el estado de la reserva a `EXPIRED` y SHALL restaurar los cupos al horario correspondiente.
3. THE Sistema SHALL ejecutar la verificación de expiración de reservas mediante un proceso programado con una frecuencia máxima de 1 minuto.
4. WHEN un turista cancela una reserva con estado `CONFIRMED` y la `reservationDate` es al menos 2 días posterior a la fecha de cancelación, THE Sistema SHALL registrar que el turista es elegible para un reembolso parcial del 30% del `totalAmount`.
5. THE Sistema SHALL garantizar que no existe mecanismo de lista de espera; una vez agotados los cupos de un horario, no se aceptan nuevas reservas para ese horario hasta que se liberen cupos por cancelación o expiración.
6. THE Sistema SHALL utilizar transacciones de base de datos para garantizar la atomicidad en las operaciones de creación y cancelación de reservas, evitando condiciones de carrera en la asignación de cupos.

---

### Requirement 7: Simulación de pagos

**User Story:** Como turista con una reserva pendiente, quiero simular el pago de mi reserva, para confirmarla y asegurar mi participación en la experiencia.

#### Acceptance Criteria

1. WHEN un turista autenticado envía una solicitud `POST /api/v1/payments/simulate` con un `reservationId` válido y en estado `PENDING_PAYMENT`, THE Sistema SHALL crear un registro de pago y SHALL retornar el resultado de la simulación con el `paymentStatus` resultante.
2. WHEN el pago simulado resulta en estado `APPROVED`, THE Sistema SHALL actualizar el estado de la reserva asociada a `CONFIRMED` únicamente si el estado del pago es explícitamente `APPROVED`; cualquier otro resultado del pago SHALL impedir la confirmación de la reserva.
3. WHEN el pago simulado resulta en estado `REJECTED`, THE Sistema SHALL mantener la reserva en estado `PENDING_PAYMENT` y SHALL retornar un mensaje indicando que el pago fue rechazado.
4. IF la reserva asociada al `reservationId` tiene estado `EXPIRED`, `CANCELLED` o `CONFIRMED`, THEN THE Sistema SHALL retornar un error HTTP 422 indicando que no se puede procesar el pago para esa reserva.
5. IF el `reservationId` no existe o no pertenece al turista autenticado, THEN THE Sistema SHALL retornar un error HTTP 404 o HTTP 403 respectivamente.
6. THE Sistema SHALL registrar la fecha y hora del pago (`paymentDate`) y un `transactionReference` único para cada intento de pago.
7. WHEN una reserva es confirmada por pago aprobado, THE Sistema SHALL garantizar que los cupos previamente bloqueados quedan definitivamente asignados a esa reserva.

---

### Requirement 8: Calificación de experiencias

**User Story:** Como turista que ha completado una experiencia, quiero dejar una calificación y comentario, para compartir mi opinión con otros usuarios y contribuir a la calidad de la plataforma.

#### Acceptance Criteria

1. WHEN un turista autenticado envía una solicitud `POST /api/v1/reviews` con `experienceId`, `rating` y `comment` válidos, THE Sistema SHALL crear una reseña asociada al turista y a la experiencia, y SHALL retornar los datos de la reseña creada.
2. THE Sistema SHALL aceptar únicamente valores de `rating` entre 1 y 5 (enteros inclusivos); IF se proporciona un valor fuera de este rango, THEN THE Sistema SHALL retornar un error HTTP 400.
3. IF el turista no tiene ninguna reserva con estado `CONFIRMED` para la experiencia que intenta calificar, THEN THE Sistema SHALL retornar un error HTTP 403 indicando que solo se pueden calificar experiencias reservadas.
4. IF el turista ya ha dejado una reseña para la misma experiencia, THEN THE Sistema SHALL retornar un error HTTP 409 indicando que ya existe una reseña del turista para esa experiencia.
5. WHEN un cliente envía una solicitud `GET /api/v1/experiences/{id}`, THE Sistema SHALL incluir el promedio de calificaciones (`averageRating`) y el número total de reseñas (`reviewCount`) de la experiencia.

---

### Requirement 9: Panel de administración

**User Story:** Como administrador, quiero gestionar experiencias, horarios, usuarios y reservas desde la API, para mantener el catálogo actualizado y supervisar las operaciones de la plataforma.

#### Acceptance Criteria

1. WHEN un administrador autenticado envía una solicitud `POST /api/v1/experiences` con los datos completos de una experiencia, THE Sistema SHALL crear la experiencia con estado activo y SHALL retornar los datos de la experiencia creada.
2. WHEN un administrador autenticado envía una solicitud `PUT /api/v1/experiences/{id}` con datos actualizados, THE Sistema SHALL actualizar la experiencia y SHALL retornar los datos actualizados.
3. WHEN un administrador autenticado envía una solicitud `DELETE /api/v1/experiences/{id}`, THE Sistema SHALL desactivar la experiencia (borrado lógico, `active = false`) sin eliminarla físicamente de la base de datos.
4. THE Sistema SHALL proteger todos los endpoints de administración con autorización basada en rol `ADMIN`; IF un usuario con rol `TOURIST` intenta acceder a estos endpoints, THEN THE Sistema SHALL retornar un error HTTP 403.
5. WHEN un administrador autenticado envía solicitudes de gestión de horarios (`POST`, `PUT`, `DELETE`) sobre `/api/v1/experiences/{id}/schedules`, THE Sistema SHALL crear, actualizar o desactivar los horarios de la experiencia correspondiente.
6. WHEN un administrador autenticado envía una solicitud `GET /api/v1/admin/reservations`, THE Sistema SHALL retornar una lista paginada de todas las reservas del sistema con filtros opcionales por estado, experiencia y rango de fechas.
7. WHEN un administrador autenticado envía una solicitud `GET /api/v1/admin/users`, THE Sistema SHALL retornar una lista paginada de todos los usuarios registrados en el sistema.
8. WHEN un administrador autenticado actualiza el estado de un usuario (`PATCH /api/v1/admin/users/{id}/status`), THE Sistema SHALL actualizar el estado del usuario y SHALL impedir que usuarios inactivos puedan autenticarse.

---

### Requirement 10: Notificaciones por correo electrónico

**User Story:** Como turista, quiero recibir notificaciones por correo electrónico sobre el estado de mis reservas, para estar informado de los cambios importantes sin necesidad de consultar la plataforma.

#### Acceptance Criteria

1. WHEN una reserva es creada con estado `PENDING_PAYMENT`, THE Sistema SHALL enviar un correo electrónico al turista con los detalles de la reserva y el tiempo límite para completar el pago (15 minutos).
2. WHEN una reserva cambia a estado `CONFIRMED` por pago aprobado, THE Sistema SHALL enviar un correo electrónico al turista con la confirmación de la reserva y los detalles de la experiencia.
3. WHEN una reserva cambia a estado `CANCELLED` por solicitud del turista, THE Sistema SHALL enviar un correo electrónico al turista confirmando la cancelación e indicando si aplica reembolso parcial.
4. WHEN una reserva cambia a estado `EXPIRED` por el proceso automático, THE Sistema SHALL enviar un correo electrónico al turista notificando la expiración y los cupos liberados.
5. THE Sistema SHALL enviar los correos electrónicos de forma asíncrona mediante SMTP para no bloquear la respuesta de la API al turista; sin embargo, el resultado del envío de correo no afectará el flujo principal de la operación.
6. IF el envío de un correo electrónico falla, THEN THE Sistema SHALL registrar el error en los logs del sistema sin afectar el flujo principal de la operación que originó la notificación.

---

### Requirement 11: Configuración de CORS y soporte para frontend

**User Story:** Como desarrollador del frontend, quiero que el backend tenga CORS configurado correctamente, para que la aplicación web pueda consumir la API sin restricciones de origen.

#### Acceptance Criteria

1. THE Sistema SHALL configurar CORS para permitir solicitudes desde los orígenes autorizados del frontend (configurables por variable de entorno).
2. THE Sistema SHALL incluir en la configuración de CORS los métodos HTTP `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`.
3. THE Sistema SHALL incluir en la configuración de CORS las cabeceras `Authorization`, `Content-Type` y `Accept`.
4. WHEN el frontend envía una solicitud preflight `OPTIONS`, THE Sistema SHALL responder con las cabeceras CORS apropiadas y un código HTTP 200.

---

### Requirement 12: Arquitectura modular y calidad técnica

**User Story:** Como equipo de desarrollo, queremos que el backend tenga una arquitectura modular y mantenible, para facilitar el desarrollo, las pruebas y el despliegue continuo.

#### Acceptance Criteria

1. THE Sistema SHALL organizar el código en paquetes por módulo funcional: `auth`, `users`, `experiences`, `reservations`, `payments`, `reviews`, `notifications`, `admin`, `config`, `security` y `common`.
2. THE Sistema SHALL utilizar Flyway para gestionar todas las migraciones de base de datos, garantizando que el esquema sea reproducible y versionado.
3. THE Sistema SHALL utilizar MapStruct para el mapeo entre entidades JPA y DTOs, evitando el mapeo manual en las capas de aplicación y presentación.
4. THE Sistema SHALL exponer un endpoint de health check (`GET /actuator/health`) que retorne el estado del sistema y de la conexión a la base de datos de forma independiente; el estado general del sistema puede reportarse como saludable incluso cuando la base de datos reporta un estado degradado, siempre que el sistema pueda seguir operando.
5. THE Sistema SHALL incluir un `Dockerfile` que permita construir y ejecutar la aplicación en un contenedor Docker compatible con el despliegue en Koyeb.
6. THE Sistema SHALL leer la configuración sensible (credenciales de base de datos, secreto JWT, credenciales SMTP, orígenes CORS) desde variables de entorno, sin valores hardcodeados en el código fuente, independientemente del entorno de ejecución (desarrollo, pruebas o producción).
7. THE Sistema SHALL retornar respuestas de error en un formato JSON uniforme que incluya al menos: `timestamp`, `status`, `error` y `message`.
