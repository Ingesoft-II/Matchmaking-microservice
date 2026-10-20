# Matchmaking-microservice

Microservicio de emparejamiento de **Killer Protocol** (shooter competitivo 5 vs 5). Recibe grupos de hasta cinco jugadores, los encola como tickets y forma partidas de diez jugadores en dos equipos de cinco cada dos segundos. Ranked usa una política de rating (margen que crece con la espera); Arcade aplica un límite de latencia.

Java 21 · Spring Boot 3.5 · Sub-arquitectura **hexagonal** (puertos y adaptadores). No hay base de datos real ni infraestructura externa: todos los servicios (almacenamiento, orquestador, broker, rating) se resuelven con **adaptadores en memoria**.

## Ejecutar

```bash
./mvnw verify          # compila y ejecuta todas las pruebas (JUnit 5 + ArchUnit)
./mvnw spring-boot:run # arranca el servicio
```

En Windows usa `mvnw.cmd` en lugar de `./mvnw`.

- Servicio: `http://localhost:8082`
- API interactiva (Springdoc): `http://localhost:8082/swagger-ui.html`
- Salud: `/actuator/health` · Métricas Prometheus: `/actuator/prometheus`

## API mínima

Prefijo `/api/matchmaking`.

| Método | Ruta | Caso de uso | Respuesta |
|---|---|---|---|
| `POST` | `/tickets` | `FindMatchUseCase` — encola un grupo de 1 a 5 jugadores | 201 + `TicketView` |
| `GET` | `/tickets/{id}` | `GetTicketUseCase` — consulta el estado del ticket | 200 + `TicketView` |
| `DELETE` | `/tickets/{id}` | `CancelUseCase` — cancela un ticket que sigue esperando | 200 + `TicketView` |
| `GET` | `/reconnect/{playerId}` | `ReconnectUseCase` — partida activa del jugador | 200 + `Partida` (404 si no existe) |
| `POST` | `/cycle` | `RunCycleUseCase` — ejecuta un ciclo manual de emparejamiento | 200 + `List<Partida>` |

`POST /tickets` acepta (requiere el encabezado `Content-Type: application/json`; sin él el endpoint responde 415):

```json
{
  "playerIds": ["player-1", "player-2"],
  "mode": "RANKED",
  "region": "sa-east",
  "latencyMs": 38
}
```

Errores (ver `ApiExceptionHandler`): validación → 400 · ticket o partida inexistente → 404 · transición de estado inválida (p. ej. cancelar un ticket ya emparejado) → 409.

Además del endpoint manual, el scheduler (`CycleScheduler`) ejecuta un ciclo cada 2 segundos. Para probar el emparejamiento, encola tickets cuyos grupos sumen **exactamente diez jugadores** y compartan modo y región; la respuesta del ciclo incluye las partidas creadas, con dos equipos de cinco y una dirección de servidor simulada (`memory://…`). Los partidos Ranked reciben el rating que entrega el adaptador de ratings en memoria (1000 por jugador, configurable en `application.yml`).

## Estructura

La dependencia sigue `adapter → application → domain`, y se verifica automáticamente con ArchUnit (`HexagonalArchitectureTest`).

```text
src/main/java/com/killerprotocol/matchmaking/
├── domain/                  # Núcleo de negocio, sin Spring
│   ├── model/               # Ticket, Party, Partida, Team, GameMode, IDs (records)
│   ├── state/               # State: TicketState + Waiting, Matched, Cancelled, Expired
│   ├── policy/              # Strategy: RatingPolicy + Ranked/ArcadePolicy; factories
│   ├── builder/             # Builder: PartidaBuilder + Ranked/ArcadePartidaBuilder; PartidaBuilderFactory
│   └── exception/           # TransicionInvalidaException
├── application/
│   ├── port/in/             # Puertos de entrada: FindMatch, GetTicket, Cancel, Reconnect, RunCycle UseCase
│   ├── port/out/            # Puertos de salida: MatchmakingStore, ServerAllocator, EventPublisher, RatingProvider
│   └── service/             # Servicios de aplicación + MatchBuilder (búsqueda con backtracking 5 vs 5)
├── adapter/
│   ├── in/web/              # MatchmakingController + ApiExceptionHandler
│   ├── in/scheduler/        # CycleScheduler (@Scheduled cada 2 s)
│   ├── out/memory/          # Adapter: InMemoryMatchmakingStore, InMemoryServerAllocator, InMemoryEventPublisher
│   └── out/progress/        # Adapter: InMemoryRatingProvider + Decorator CachedRatingProvider (caché con TTL)
└── config/                  # MatchmakingConfig: raíz de composición (beans del núcleo)
```

## Patrones de diseño

Ruta base de los archivos: `src/main/java/com/killerprotocol/matchmaking/`.

| Patrón | Archivo y clase (rol) |
|---|---|
| Strategy | `domain/policy/RatingPolicy.java` (estrategia), `RankedPolicy.java` y `ArcadePolicy.java` (implementaciones); contexto: `application/service/MatchBuilder.java`, que invoca `canMatch` al armar el grupo (llamado por `RunCycleService`) |
| State | `domain/model/Ticket.java` (contexto), `domain/state/TicketState.java` (estado abstracto), `Waiting.java`, `Matched.java`, `Cancelled.java` y `Expired.java` (estados concretos) |
| Factory Method | `domain/policy/RatingPolicyCreator.java` + `ConfiguredRatingPolicyFactory.java` (crean `RatingPolicy`); `domain/builder/PartidaBuilderFactory.java` + `DefaultPartidaBuilderFactory.java` (crean `PartidaBuilder` por modo) |
| Builder | `domain/builder/PartidaBuilder.java` (interfaz), `RankedPartidaBuilder.java` y `ArcadePartidaBuilder.java` (builders por etapas); director: `application/service/RunCycleService.java` |
| Adapter | Puerto `application/port/out/MatchmakingStore.java` → adaptador `adapter/out/memory/InMemoryMatchmakingStore.java`; igual para `ServerAllocator` → `InMemoryServerAllocator` y `EventPublisher` → `InMemoryEventPublisher` |
| Decorator | Componente `application/port/out/RatingProvider.java`; `adapter/out/progress/CachedRatingProvider.java` (caché con TTL y último valor conocido si el proveedor falla) decorando `InMemoryRatingProvider.java` |

## Configuración

Todo lo relevante vive en `src/main/resources/application.yml` (cambia sin recompilar):

| Clave | Efecto |
|---|---|
| `matchmaking.cycle-delay-ms` | Frecuencia del ciclo automático (por defecto 2000 ms) |
| `matchmaking.maps.ranked` / `matchmaking.maps.arcade` | Mapas elegidos por round-robin según el modo |
| `matchmaking.rating.default` | Rating que entrega `InMemoryRatingProvider` (por defecto 1000) |
| `matchmaking.rating.cache-ttl-sec` | TTL de la caché de `CachedRatingProvider` (por defecto 60 s) |
| `matchmaking.policy.ranked.base-gap` / `growth-per-sec` | Margen de rating: `|Δrating| ≤ baseGap + growth·espera` |
| `matchmaking.policy.arcade.max-latency-ms` | Latencia máxima para la compatibilidad arcade |

## Pruebas

`./mvnw verify` ejecuta más de 60 pruebas (JUnit 5, AssertJ y ArchUnit):

- **Dominio**: `TicketTest`, `PartyTest`, `TicketStateTransitionsTest` (tabla completa de estados y transiciones inválidas), `RankedPolicyTest`, `ArcadePolicyTest`, `RatingPolicyFactoryTest`.
- **Ciclo de emparejamiento**: `RunCycleServiceTest` — arma 5 vs 5 con diez jugadores, no crea partida con menos de diez, y demuestra el **principio Abierto/Cerrado**: registra una estrategia nueva (`seasonalPolicy`) sin modificar `RunCycleService`.
- **Servicios**: `MatchBuilderTest` (reparto 5 vs 5) y `ReconnectServiceTest` (partida activa por jugador).
- **Adaptadores**: `CachedRatingProviderTest` (caché, expiración y tolerancia a fallo) e `InMemoryRatingProviderTest`.
- **Arquitectura**: `HexagonalArchitectureTest` (ArchUnit) garantiza que `domain` y `application` no dependen de `adapter` ni de Spring, que `adapter/in` no depende de `adapter/out` y que los servicios del núcleo no llevan anotaciones de Spring.

## Contribuidores

- Oscar Leonardo Riveros Perez (https://github.com/ShryhakAkizuki)
- Jader Camilo Rodriguez Arboleda (https://github.com/JaderCamiloUNAL)
- Juan Luis Arteaga Mantovani (https://github.com/ioannis-rz)
- Daniel Oñate Hernandez (https://github.com/donateh)
