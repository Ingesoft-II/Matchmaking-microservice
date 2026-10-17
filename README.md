# Matchmaking-microservice

Microservicio **Matchmaking** de *Killer Protocol*: encuentra y agrupa
jugadores para formar partidas 5 vs 5. Recibe solicitudes de búsqueda, las pone
en cola por modo de juego, ejecuta un ciclo de emparejamiento cada 2 segundos,
pide un servidor de juego al orquestador y publica un evento en el broker.

## Requisitos

- **JDK 21** (LTS) o superior
- **Maven** — el proyecto incluye el *Maven Wrapper* (`mvnw` / `mvnw.cmd`),
  así que basta con el JDK; no es necesario instalar Maven aparte.

## Cómo ejecutar

**Perfil Entregable** (por defecto; sin Docker ni servicios externos):

```bash
# Compilar y correr todas las pruebas (unitarias + arquitectura ArchUnit)
./mvnw verify

# Arrancar el servicio con los servidores simulados (perfil demo, hito H10)
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

- Servicio: `http://localhost:8082`
- Documentación de la API (OpenAPI): `http://localhost:8082/swagger-ui.html`
- Salud: `http://localhost:8082/actuator/health` · Métricas: `http://localhost:8082/actuator/prometheus`

## Estructura del proyecto (arquitectura hexagonal)

Regla de dependencias: `adapter → application → domain` (nunca al revés).

```
src/main/java/com/killerprotocol/matchmaking/
├── MatchmakingApplication.java
├── domain/                          # Núcleo: sin Spring, sin infraestructura
│   ├── model/                       #   Ticket, Party, Partida, Jugador, ServerInfo,
│   │                                #   ReglasAntiCheat, GameMode, IDs
│   ├── state/                       #   State: TicketState, Waiting, Matched, Cancelled, Expired
│   ├── policy/                      #   Strategy: RatingPolicy, RankedPolicy, ArcadePolicy
│   │                                #   Factory Method: RatingPolicyFactory
│   ├── builder/                     #   Builder: PartidaBuilder, PartidaClasificatoriaBuilder,
│   │                                #   PartidaArcadeBuilder
│   └── exception/
├── application/
│   ├── port/in/                     #   FindMatchUseCase, CancelUseCase, ReconnectUseCase,
│   │                                #   RunCycleUseCase, GetTicketUseCase
│   ├── port/out/                    #   MatchmakingStore, ServerAllocator, EventPublisher,
│   │                                #   RatingProvider
│   ├── service/                     #   FindMatchService, CancelService, ReconnectService,
│   │                                #   RunCycleService (clases Java normales, sin @Service)
│   └── event/                       #   PartidaCreadaEvent, TicketCanceladoEvent, TicketExpiradoEvent
├── adapter/
│   ├── in/web/                      #   MatchmakingController, dto/*, ApiExceptionHandler
│   ├── in/scheduler/                #   CycleScheduler (@Scheduled cada 2 s)
│   ├── out/redis/                   #   RedisStore (Adapter), RedisClient (Adaptee), InMemoryRedisClient
│   ├── out/orchestrator/            #   OrchestratorHttp
│   ├── out/broker/                  #   BrokerPublisher, BrokerClient, InMemoryBrokerClient
│   └── out/progress/                #   ProgressClient, CachedRatingProvider (Decorator)
├── config/                          #   MatchmakingConfig (raíz de composición),
│                                    #   MatchmakingProperties, HttpClientsConfig
└── demo/                            #   StubServers (Progreso y Orquestador simulados, perfil demo)
```

## Tabla patrón → archivo

Ruta base: `src/main/java/com/killerprotocol/matchmaking/`

| Familia | Patrón | Rol | Archivo / clase |
|---|---|---|---|
| Creacional | Builder | Product | `domain/model/Partida.java` |
| | | Builder (interfaz) | `domain/builder/PartidaBuilder.java` |
| | | Concrete Builders | `domain/builder/PartidaClasificatoriaBuilder.java`, `domain/builder/PartidaArcadeBuilder.java` |
| | | Director / Client | `application/service/RunCycleService.java` |
| Creacional | Factory Method | Creator | `domain/policy/RatingPolicyFactory.java` |
| | | Product | `domain/policy/RatingPolicy.java` |
| Estructural | Adapter | Target | `application/port/out/MatchmakingStore.java` |
| | | Adapter | `adapter/out/redis/RedisStore.java` |
| | | Adaptee | `adapter/out/redis/RedisClient.java` (+ `InMemoryRedisClient.java`) |
| | | Client | `application/service/FindMatchService.java`, `CancelService.java`, `ReconnectService.java`, `RunCycleService.java` |
| Estructural | Decorator | Component | `application/port/out/RatingProvider.java` |
| | | ConcreteComponent | `adapter/out/progress/ProgressClient.java` |
| | | ConcreteDecorator | `adapter/out/progress/CachedRatingProvider.java` |
| | | Client | `application/service/RunCycleService.java` |
| Comportamiento | Strategy | Strategy | `domain/policy/RatingPolicy.java` |
| | | Concrete Strategies | `domain/policy/RankedPolicy.java`, `domain/policy/ArcadePolicy.java` |
| | | Context | `application/service/RunCycleService.java` |
| Comportamiento | State | Context | `domain/model/Ticket.java` |
| | | State | `domain/state/TicketState.java` |
| | | Concrete States | `domain/state/Waiting.java`, `Matched.java`, `Cancelled.java`, `Expired.java` |



