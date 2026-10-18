# Matchmaking-microservice

Servicio de ejemplo para Killer Protocol. Recibe grupos de hasta cinco jugadores, mantiene tickets en memoria y busca formar partidas de diez jugadores en dos equipos de cinco. Ranked usa una política de rating; Arcade aplica un límite de latencia. El proyecto usa Java 21 y Spring Boot.

La persistencia, la asignación de servidores y los eventos usan adaptadores en memoria para esta entrega. No hay conexión a Redis, broker, servicio de progreso ni servidores de juego reales.

## Ejecutar

```bash
./mvnw verify
./mvnw spring-boot:run
```

En Windows puedes usar `mvnw.cmd` en lugar de `./mvnw`. El servicio escucha en `http://localhost:8082`. La API documentada por Springdoc está en `/swagger-ui.html`; salud en `/actuator/health`.

## API mínima

`POST /api/matchmaking/tickets` registra un grupo y devuelve su ticket:

```json
{
  "playerIds": ["player-1", "player-2"],
  "mode": "RANKED",
  "region": "sa-east",
  "latencyMs": 38
}
```

`GET /api/matchmaking/tickets/{id}` consulta el estado y `DELETE /api/matchmaking/tickets/{id}` cancela un ticket que sigue esperando. `POST /api/matchmaking/cycle` ejecuta un ciclo manual; además, el scheduler ejecuta uno cada dos segundos. La respuesta del ciclo incluye las partidas creadas, con dos equipos de cinco y una dirección de servidor simulada.

Para probar el emparejamiento, registra tickets cuyos grupos sumen exactamente diez jugadores y comparte modo y región. Los partidos Ranked reciben el rating promedio que entrega el adaptador de ratings en memoria (1000 por jugador, configurable en `application.yml`). Las políticas, mapas, límites y frecuencia del ciclo también están en `application.yml`.

## Estructura

La dependencia sigue `adapter → application → domain`.

```text
src/main/java/com/killerprotocol/matchmaking/
├── domain/       # Ticket, Party, Partida, estados, políticas y builders
├── application/  # Casos de uso, puertos y servicios de aplicación
├── adapter/in/   # API REST y scheduler
├── adapter/out/  # Almacenamiento, asignador, eventos y rating en memoria
└── config/       # Composición de servicios y políticas
```

## Patrones de diseño

Ruta base: `src/main/java/com/killerprotocol/matchmaking/`.

| Patrón | Participantes principales |
|---|---|
| Strategy | `domain/policy/RatingPolicy`, `RankedPolicy`, `ArcadePolicy`; el contexto es `application/service/RunCycleService` |
| State | `domain/model/Ticket`, `domain/state/TicketState`, `Waiting`, `Matched`, `Cancelled`, `Expired` |
| Factory Method | `domain/policy/RatingPolicyCreator`, `ConfiguredRatingPolicyFactory`, `FixedRatingPolicyFactory`; el producto es `RatingPolicy` |
| Builder | `domain/model/Partida`, `domain/builder/PartidaBuilder`, `RankedPartidaBuilder`, `ArcadePartidaBuilder`; el servicio de ciclo dirige la construcción |
| Adapter | Puerto `application/port/out/MatchmakingStore`; adaptador `adapter/out/memory/InMemoryMatchmakingStore` |
| Decorator | Componente `application/port/out/RatingProvider`; `InMemoryRatingProvider` y `CachedRatingProvider` en `adapter/out/progress` |

## Pruebas

`mvnw verify` ejecuta pruebas de dominio, ciclo de emparejamiento y reglas ArchUnit. `RunCycleServiceTest` prueba el armado 5 vs. 5, que no se cree una partida con menos de diez jugadores y que una nueva Strategy se pueda registrar sin cambiar el servicio de ciclo.
