# Requirements Document

## Introduction

Este documento define los requisitos para implementar el endpoint `GET /api/v1/payments/me` en el backend Spring Boot de SmartTourism. El frontend Angular ya consume este endpoint para mostrar el historial de pagos del turista autenticado, pero actualmente no existe en el backend. La implementación incluye el método de consulta en el repositorio, la lógica de servicio y el endpoint REST en el controlador existente.

## Glossary

- **Payment_Controller**: Controlador REST existente en `com.smarttourism.backend.payments.controller` que expone los endpoints de pagos bajo `/api/v1/payments`.
- **Payment_Service**: Servicio de la capa de negocio que gestiona la lógica de pagos en `com.smarttourism.backend.payments.service`.
- **Payment_Repository**: Interfaz JPA que proporciona acceso a datos de la entidad `Payment`.
- **Payment_Mapper**: Mapper MapStruct que convierte entidades `Payment` a DTOs `PaymentResponse`.
- **Payment_Response**: DTO de respuesta que contiene los datos del pago y la información aplanada de la reserva asociada.
- **Tourist**: Usuario autenticado con rol `TOURIST` que realiza pagos y consulta su historial.
- **JWT_Token**: Token de autenticación JSON Web Token emitido al turista tras el login.
- **createdAt**: Campo de tipo `LocalDateTime` en la entidad `Payment` que registra la fecha y hora de creación del pago.

## Requirements

### Requirement 1: Consultar historial de pagos del turista autenticado

**User Story:** Como turista, quiero consultar mi historial de pagos para ver el estado de todos los pagos que he realizado en la plataforma.

#### Acceptance Criteria

1. WHEN el Tourist realiza una petición GET a `/api/v1/payments/me` con un JWT_Token válido, THE Payment_Controller SHALL retornar una respuesta HTTP 200 con una lista de Payment_Response correspondientes al turista autenticado.
2. WHEN el Tourist realiza una petición GET a `/api/v1/payments/me` con un JWT_Token válido y no tiene pagos registrados, THE Payment_Controller SHALL retornar una respuesta HTTP 200 con una lista vacía.
3. THE Payment_Controller SHALL ordenar la lista de Payment_Response por el campo createdAt en orden descendente (más reciente primero).

### Requirement 2: Restricción de acceso por autenticación y rol

**User Story:** Como sistema, quiero restringir el acceso al historial de pagos únicamente a turistas autenticados para proteger la información financiera de los usuarios.

#### Acceptance Criteria

1. WHEN un usuario no autenticado realiza una petición GET a `/api/v1/payments/me` sin JWT_Token, THE Payment_Controller SHALL retornar una respuesta HTTP 401 Unauthorized.
2. WHEN un usuario autenticado con un rol diferente a TOURIST realiza una petición GET a `/api/v1/payments/me`, THE Payment_Controller SHALL retornar una respuesta HTTP 403 Forbidden.
3. THE Payment_Controller SHALL utilizar la anotación `@PreAuthorize("hasRole('TOURIST')")` existente a nivel de clase para aplicar la restricción de rol.

### Requirement 3: Aislamiento de datos entre turistas

**User Story:** Como turista, quiero que mi historial de pagos sea privado y que solo pueda ver mis propios pagos, no los de otros turistas.

#### Acceptance Criteria

1. THE Payment_Service SHALL filtrar los pagos utilizando exclusivamente el ID del turista extraído del JWT_Token de la petición autenticada.
2. THE Payment_Repository SHALL proporcionar un método de consulta que busque pagos por el ID del turista propietario de la reserva asociada.
3. WHEN el Tourist consulta su historial, THE Payment_Service SHALL retornar únicamente los pagos cuya reserva pertenezca al turista autenticado.

### Requirement 4: Consulta en el repositorio de datos

**User Story:** Como desarrollador, quiero un método de consulta en el repositorio que permita buscar pagos por turista de forma eficiente y ordenada.

#### Acceptance Criteria

1. THE Payment_Repository SHALL exponer un método que acepte un UUID de turista y retorne una lista de entidades Payment cuya reserva pertenezca a dicho turista.
2. THE Payment_Repository SHALL ordenar los resultados por createdAt en orden descendente dentro de la consulta a base de datos.
3. THE Payment_Repository SHALL utilizar fetch join o carga eager de la relación `reservation` y sus asociaciones (`tourist`, `experience`) para evitar problemas de N+1 queries.

### Requirement 5: Conversión de entidades a DTOs de respuesta

**User Story:** Como desarrollador, quiero reutilizar el mapper existente para convertir las entidades Payment al DTO PaymentResponse, manteniendo consistencia con el endpoint de simulación de pago.

#### Acceptance Criteria

1. THE Payment_Service SHALL utilizar el Payment_Mapper existente para convertir cada entidad Payment a un Payment_Response.
2. THE Payment_Response SHALL incluir los campos: id, reservationId, amount, paymentStatus, createdAt, experienceTitle, transactionReference, reservationStatus, touristId, touristName, touristEmail, experienceId, experienceLocation, reservationDate, quantity, totalAmount y expirationDate.
3. THE Payment_Mapper SHALL resolver los campos aplanados (experienceTitle, reservationId, touristName, etc.) desde las relaciones de la entidad Payment con Reservation, Tourist y Experience.
