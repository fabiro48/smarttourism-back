# Requirements Document

## Introduction

El módulo de **Horarios (Schedules)** gestiona las franjas horarias disponibles para cada experiencia turística en la plataforma Smart Tourism. Un horario define el día de la semana, hora de inicio, hora de fin y cupos disponibles para una experiencia específica. Los horarios son el recurso central que conecta las experiencias con las reservas: los turistas seleccionan un horario al crear una reserva, y el sistema controla la disponibilidad de cupos a través de este recurso.

El módulo actualmente expone endpoints CRUD bajo `/api/v1/experiences/{experienceId}/schedules` restringidos al rol `ADMIN`. Esta especificación formaliza los requisitos funcionales del módulo, incluyendo la creación, actualización, consulta, desactivación de horarios, las reglas de validación temporal, y la integración con el sistema de reservas para el control de cupos.

---

## Glossary

- **Sistema**: El backend de la plataforma Smart Tourism.
- **Horario (Schedule)**: Franja horaria asociada a una experiencia turística, definida por día de la semana, hora de inicio, hora de fin y cupos disponibles.
- **Experiencia (Experience)**: Actividad turística ofertada en la plataforma a la que se asocian uno o más horarios.
- **Administrador**: Usuario con rol `ADMIN` que gestiona los horarios de las experiencias.
- **Turista**: Usuario con rol `TOURIST` que consulta horarios disponibles al explorar experiencias.
- **Cupos_Disponibles (availableSlots)**: Número entero positivo que representa la cantidad de plazas disponibles para reservar en un horario específico.
- **Día_De_Semana (DayOfWeek)**: Enumeración con valores `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`.
- **Borrado_Lógico**: Mecanismo de desactivación que establece el campo `active = false` sin eliminar físicamente el registro de la base de datos.
- **Reserva**: Solicitud de un turista para participar en una experiencia en un horario específico; referencia directamente al Horario mediante `schedule_id`.

---

## Requirements

### Requirement 1: Creación de horarios para una experiencia

**User Story:** Como administrador, quiero crear horarios para una experiencia turística, para definir cuándo y con qué capacidad está disponible para los turistas.

#### Acceptance Criteria

1. WHEN un administrador autenticado envía una solicitud `POST /api/v1/experiences/{experienceId}/schedules` con `dayOfWeek`, `startTime`, `endTime` y `availableSlots` válidos, THE Sistema SHALL crear un nuevo Horario con estado `active = true` asociado a la Experiencia indicada, y SHALL retornar los datos del Horario creado (id, dayOfWeek, startTime, endTime, availableSlots) con código HTTP 201.
2. IF la Experiencia identificada por `experienceId` no existe o tiene estado `active = false`, THEN THE Sistema SHALL retornar un error HTTP 404 con un mensaje indicando que la experiencia no fue encontrada.
3. IF el campo `dayOfWeek` está ausente o contiene un valor fuera de la enumeración Día_De_Semana (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY), THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje indicando el campo inválido.
4. IF el campo `startTime` o `endTime` está ausente o no cumple el formato HH:mm, THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje indicando que la hora de inicio y la hora de fin son obligatorias y deben tener formato válido.
5. IF el campo `availableSlots` está ausente, es nulo o tiene un valor menor o igual a cero o mayor a 1000, THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje indicando que los cupos disponibles deben ser un entero entre 1 y 1000.
6. IF el valor de `startTime` es igual o posterior al valor de `endTime`, THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje indicando que la hora de inicio debe ser anterior a la hora de fin.
7. IF el usuario no está autenticado o no posee el rol ADMIN, THEN THE Sistema SHALL retornar un error HTTP 401 (no autenticado) o HTTP 403 (sin permisos) sin crear el Horario.

---

### Requirement 2: Actualización de horarios existentes

**User Story:** Como administrador, quiero actualizar los datos de un horario existente, para corregir errores o ajustar la disponibilidad de una experiencia.

#### Acceptance Criteria

1. WHEN un administrador autenticado envía una solicitud `PUT /api/v1/experiences/{experienceId}/schedules/{scheduleId}` con los campos `dayOfWeek`, `startTime`, `endTime` y `availableSlots` válidos, THE Sistema SHALL actualizar todos los campos modificables del Horario identificado y SHALL retornar los datos actualizados con código HTTP 200.
2. IF el Horario identificado por `scheduleId` no existe o tiene estado `active = false`, THEN THE Sistema SHALL retornar un error HTTP 404 con un mensaje indicando que el horario no fue encontrado.
3. IF el Horario identificado por `scheduleId` no pertenece a la Experiencia identificada por `experienceId` en la URL, THEN THE Sistema SHALL retornar un error HTTP 404 con un mensaje indicando que el horario no fue encontrado.
4. IF el campo `dayOfWeek` está ausente o contiene un valor fuera de la enumeración Día_De_Semana, o el campo `startTime` o `endTime` está ausente o en formato no reconocido, o el campo `availableSlots` está ausente, es nulo o tiene un valor menor o igual a cero, o `startTime` es igual o posterior a `endTime`, THEN THE Sistema SHALL retornar un error HTTP 400 con los mensajes de validación correspondientes a cada campo inválido.
5. THE Sistema SHALL requerir que todos los campos modificables (`dayOfWeek`, `startTime`, `endTime`, `availableSlots`) estén presentes en la solicitud de actualización; no se permiten actualizaciones parciales.
6. THE Sistema SHALL preservar la asociación del Horario con su Experiencia original durante la actualización; el campo `experience_id` no es modificable.
7. THE Sistema SHALL preservar el estado `active` del Horario durante la actualización; el campo `active` no es modificable mediante este endpoint.

---

### Requirement 3: Desactivación de horarios (borrado lógico)

**User Story:** Como administrador, quiero desactivar un horario que ya no está disponible, para que los turistas no puedan reservar en esa franja sin perder el historial de reservas asociadas.

#### Acceptance Criteria

1. WHEN un administrador autenticado envía una solicitud `DELETE /api/v1/experiences/{experienceId}/schedules/{scheduleId}`, THE Sistema SHALL establecer el campo `active = false` en el Horario identificado y SHALL retornar código HTTP 204 sin cuerpo de respuesta.
2. IF el Horario identificado por `scheduleId` no existe, THEN THE Sistema SHALL retornar un error HTTP 404 con un mensaje indicando que el horario no fue encontrado.
3. IF el `experienceId` proporcionado en la URL no corresponde a la Experiencia asociada al Horario identificado por `scheduleId`, THEN THE Sistema SHALL retornar un error HTTP 404 con un mensaje indicando que el horario no fue encontrado.
4. IF el Horario identificado por `scheduleId` ya tiene el campo `active = false`, THEN THE Sistema SHALL retornar código HTTP 204 sin cuerpo de respuesta sin modificar el registro.
5. THE Sistema SHALL conservar el registro del Horario desactivado en la base de datos para mantener la integridad referencial con las reservas históricas asociadas; las reservas existentes vinculadas al Horario desactivado no serán modificadas ni canceladas por la operación de desactivación.
6. WHEN un Horario es desactivado, THE Sistema SHALL excluir ese Horario de las consultas públicas de horarios disponibles para la experiencia, de modo que no sea retornado en respuestas donde se filtran Horarios con `active = true`.

---

### Requirement 4: Consulta de horarios activos de una experiencia

**User Story:** Como turista, quiero ver los horarios disponibles de una experiencia, para elegir la franja horaria que mejor se ajuste a mi itinerario al momento de reservar.

#### Acceptance Criteria

1. WHEN un cliente envía una solicitud `GET /api/v1/experiences/{experienceId}`, THE Sistema SHALL incluir en la respuesta la lista de Horarios activos (`active = true`) de la Experiencia, con los campos `id`, `dayOfWeek`, `startTime`, `endTime` y `availableSlots` de cada Horario.
2. THE Sistema SHALL retornar únicamente los Horarios con `active = true` en el endpoint de detalle de experiencia (`GET /api/v1/experiences/{experienceId}`) y en el listado de experiencias (`GET /api/v1/experiences`).
3. WHEN una Experiencia no tiene Horarios activos asociados, THE Sistema SHALL retornar una lista vacía en el campo de horarios de la respuesta de la Experiencia.
4. THE Sistema SHALL ordenar los Horarios retornados de forma consistente por `dayOfWeek` (lunes a domingo) y luego por `startTime` ascendente.
5. IF la Experiencia identificada por `experienceId` no existe o tiene estado `active = false`, THEN THE Sistema SHALL retornar un error HTTP 404 con un mensaje indicando que la experiencia no fue encontrada.

---

### Requirement 5: Control de acceso a la gestión de horarios

**User Story:** Como plataforma, necesito restringir la gestión de horarios exclusivamente a administradores, para garantizar que solo personal autorizado modifique la disponibilidad de las experiencias.

#### Acceptance Criteria

1. THE Sistema SHALL restringir el acceso a los endpoints de creación (`POST /api/v1/experiences/{experienceId}/schedules`), actualización (`PUT /api/v1/experiences/{experienceId}/schedules/{scheduleId}`) y desactivación (`DELETE /api/v1/experiences/{experienceId}/schedules/{scheduleId}`) exclusivamente a usuarios autenticados con rol `ADMIN`.
2. IF un usuario con rol `TOURIST` envía una solicitud a cualquiera de los endpoints de gestión de horarios (POST, PUT o DELETE), THEN THE Sistema SHALL retornar un error HTTP 403 con un mensaje indicando permisos insuficientes, sin modificar ningún recurso.
3. IF una solicitud a los endpoints de gestión de horarios no incluye un token JWT en el encabezado `Authorization`, THEN THE Sistema SHALL retornar un error HTTP 401 con un mensaje indicando que la autenticación es requerida.
4. IF una solicitud a los endpoints de gestión de horarios incluye un token JWT expirado o con firma inválida, THEN THE Sistema SHALL retornar un error HTTP 401 con un mensaje indicando que el token no es válido.
5. THE Sistema SHALL permitir la consulta de horarios activos (a través del endpoint `GET /api/v1/experiences/{experienceId}`) a cualquier usuario autenticado o no autenticado, sin restricción de rol.

---

### Requirement 6: Integración con el sistema de reservas para control de cupos

**User Story:** Como plataforma, necesito que el módulo de horarios mantenga la integridad de los cupos disponibles en coordinación con el sistema de reservas, para evitar sobreventa y garantizar una experiencia confiable a los turistas.

#### Acceptance Criteria

1. WHEN una reserva es creada para un Horario específico, THE Sistema SHALL decrementar el campo `availableSlots` del Horario en el valor del campo `quantity` de la reserva.
2. WHEN una reserva transita al estado `CANCELLED` o `EXPIRED`, THE Sistema SHALL incrementar el campo `availableSlots` del Horario asociado en el valor del campo `quantity` de esa reserva.
3. IF una solicitud de reserva tiene un `quantity` mayor que el valor actual de `availableSlots` del Horario, THEN THE Sistema SHALL rechazar la operación sin modificar `availableSlots` y SHALL retornar un error indicando cupos insuficientes junto con la cantidad disponible actual.
4. THE Sistema SHALL adquirir un bloqueo exclusivo sobre el registro del Horario antes de verificar y modificar `availableSlots` durante la creación de reservas, de modo que solicitudes concurrentes sobre el mismo Horario se procesen secuencialmente y no se produzca sobreventa.
5. THE Sistema SHALL ejecutar las operaciones de decremento y restauración de `availableSlots` dentro de la misma transacción de base de datos que la creación o cambio de estado de la reserva correspondiente, de modo que ambas operaciones se confirmen o reviertan de forma atómica.
6. THE Sistema SHALL garantizar que el campo `availableSlots` de un Horario no sea menor que cero en ningún momento; la invariante `availableSlots >= 0` se mantiene tras cualquier operación de decremento o restauración.

---

### Requirement 7: Validación de consistencia temporal del horario

**User Story:** Como administrador, quiero que el sistema valide la coherencia de los datos temporales de un horario, para evitar configuraciones erróneas que confundan a los turistas.

#### Acceptance Criteria

1. IF el valor de `startTime` es igual o posterior al valor de `endTime` en una operación de creación o actualización de Horarios, THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje indicando que la hora de inicio debe ser estrictamente anterior a la hora de fin.
2. THE Sistema SHALL aceptar valores de `startTime` y `endTime` exclusivamente en formato ISO 8601 de hora local (`HH:mm` o `HH:mm:ss`) y SHALL comparar ambos valores con precisión de segundos, tratando el formato `HH:mm` como equivalente a `HH:mm:00`.
3. IF se proporciona un valor en `startTime` o `endTime` que no corresponde al formato `HH:mm` ni al formato `HH:mm:ss`, THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje indicando los formatos aceptados.
4. IF se proporcionan valores de `startTime` y `endTime` en formato válido pero que representan una duración menor a 1 minuto (por ejemplo `startTime = "09:00:00"` y `endTime = "09:00:30"`), THEN THE Sistema SHALL retornar un error HTTP 400 con un mensaje indicando que la duración mínima del horario es de 1 minuto.
