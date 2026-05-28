# Implementation Plan: Payments History Endpoint

## Overview

Implementar el endpoint `GET /api/v1/payments/me` que retorna el historial de pagos del turista autenticado. La implementación sigue la arquitectura de capas existente: se añade un método JPQL con JOIN FETCH en `PaymentRepository`, un método de servicio en `PaymentService`, y un nuevo endpoint GET en `PaymentController`. Se reutiliza el `PaymentMapper` existente para la conversión entidad→DTO.

## Tasks

- [x] 1. Añadir método de consulta en PaymentRepository
  - [x] 1.1 Implementar método JPQL con JOIN FETCH en PaymentRepository
    - Añadir método `findByTouristIdWithDetails(UUID touristId)` con anotación `@Query`
    - La query JPQL debe usar `JOIN FETCH p.reservation r`, `JOIN FETCH r.tourist`, `JOIN FETCH r.experience`
    - Filtrar por `r.tourist.id = :touristId`
    - Ordenar por `p.createdAt DESC`
    - Añadir anotación `@Param("touristId")` al parámetro
    - Retornar `List<Payment>`
    - _Requirements: 4.1, 4.2, 4.3_

- [x] 2. Añadir método de servicio en PaymentService
  - [x] 2.1 Implementar método getPaymentsByTourist en PaymentService
    - Añadir método público `getPaymentsByTourist(UUID touristId)` con `@Transactional(readOnly = true)`
    - Invocar `paymentRepository.findByTouristIdWithDetails(touristId)`
    - Mapear cada entidad Payment a PaymentResponse usando `paymentMapper::toResponse`
    - Retornar `List<PaymentResponse>`
    - _Requirements: 3.1, 3.3, 5.1_

  - [x]* 2.2 Write property test: Aislamiento de datos por turista
    - **Property 1: Aislamiento de datos por turista**
    - Generar múltiples tourist IDs aleatorios y listas de Payment asociadas a diferentes turistas
    - Mockear el repositorio para retornar solo los pagos del turista consultado
    - Verificar que el servicio retorna únicamente pagos cuya reserva pertenece al turista solicitado
    - Usar `@Property(tries = 100)` con tag `Feature: payments-history-endpoint, Property 1: Aislamiento de datos por turista`
    - **Validates: Requirements 1.1, 3.1, 3.3**

  - [x]* 2.3 Write property test: Ordenamiento descendente por fecha de creación
    - **Property 2: Ordenamiento descendente por fecha de creación**
    - Generar listas de Payment con `createdAt` aleatorios, configurar mock del repositorio para retornarlos ordenados
    - Verificar que cada `createdAt` en la lista resultante es >= al siguiente (orden descendente)
    - Usar `@Property(tries = 100)` con tag `Feature: payments-history-endpoint, Property 2: Ordenamiento descendente por fecha de creación`
    - **Validates: Requirements 1.3, 4.2**

- [x] 3. Añadir endpoint GET /me en PaymentController
  - [x] 3.1 Implementar endpoint getMyPayments en PaymentController
    - Añadir método `getMyPayments(Authentication authentication)` con `@GetMapping("/me")`
    - Añadir anotación `@Operation(summary = "Obtener historial de pagos del turista autenticado")`
    - Extraer `touristId` usando el método privado `extractUserIdFromAuthentication` existente
    - Invocar `paymentService.getPaymentsByTourist(touristId)`
    - Retornar `ResponseEntity.ok(payments)` con tipo `ResponseEntity<List<PaymentResponse>>`
    - Añadir import de `GetMapping` y `List`
    - _Requirements: 1.1, 1.2, 1.3, 2.3_

  - [x]* 3.2 Write unit tests para el endpoint /me
    - Test: Tourist con pagos → HTTP 200 + lista con elementos
    - Test: Tourist sin pagos → HTTP 200 + lista vacía `[]`
    - Mockear PaymentService y Authentication
    - _Requirements: 1.1, 1.2_

- [x] 4. Checkpoint - Verificar compilación e integración
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Property test del mapper
  - [x]* 5.1 Write property test: Correctitud del mapeo entidad→DTO
    - **Property 3: Correctitud del mapeo entidad→DTO**
    - Generar entidades Payment con Reservation, Tourist y Experience con valores aleatorios (UUIDs, strings, BigDecimal, LocalDateTime, enums)
    - Invocar el mapper real (MapStruct) sobre cada entidad generada
    - Verificar que cada campo aplanado del DTO coincide con su fuente en el grafo de entidades
    - Usar `@Property(tries = 100)` con tag `Feature: payments-history-endpoint, Property 3: Correctitud del mapeo entidad→DTO`
    - **Validates: Requirements 5.2, 5.3**

- [x] 6. Final checkpoint - Verificar todos los tests
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik (already configured, v1.8.4)
- Unit tests validate specific examples and edge cases
- La seguridad 401/403 es manejada automáticamente por Spring Security (JwtAuthenticationFilter + @PreAuthorize a nivel de clase), no requiere implementación adicional
- El PaymentMapper existente ya tiene todos los @Mapping necesarios, solo se reutiliza

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["2.2", "2.3", "3.1"] },
    { "id": 3, "tasks": ["3.2", "5.1"] }
  ]
}
```
