# Agendei - Sistema de Agendamento por Slots

## Comandos

- **Build**: `./gradlew build`
- **Run**: `./gradlew run` (porta 8080) — usa plugin `application` com mainClass `com.klausner.MainKt`
- **Test**: `./gradlew test`
- **Shadow JAR**: `./gradlew shadowJar` → `build/libs/agendei.jar`
- **Frontend**: `cd ../agendei-frontend && npm run dev` (porta 3000)

## Stack

- **Backend**: Kotlin 2.1 + Ktor 3.1 + Exposed 0.59 + Koin 4.1 + SQLite
- **Frontend**: Vue 3 + Pinia + TypeScript + Vite (repo separado em `../agendei-frontend`)
- **Auth**: Google OAuth2 + JWT (Auth0 java-jwt)
- **Serialization**: Jackson com LowerCamelCaseStrategy

## Arquitetura

```
src/main/kotlin/com/klausner/
├── domains/              # Entidades e value objects (DDD)
│   └── valueobjects/     # Phone, Address, Interval, Money
├── usecases/             # Casos de uso (lógica de negócio)
│   ├── UseCase.kt        # Interface base: UseCase<I, O> → Result<O>
│   ├── auth/
│   ├── professional/
│   └── service/
├── repositories/         # Acesso a dados (Exposed)
│   ├── interfaces/       # Crud.kt: Creator, Finder, Updater, Deleter, BasicCrud
│   └── {domain}/         # Interface + Implementação por domínio
├── routes/               # Endpoints Ktor (extension functions em Route)
├── services/             # Serviços externos (Google Auth, JWT)
├── infraestructure/      # DI (Koin), ObjectMapper, Result extensions
└── database/             # Tables (Exposed) e custom columns
```

## Padrões para Novas Features

### 1. Criar o Domain (`domains/`)
```kotlin
data class NovoDominio(
    val id: UUID,
    val campo: String,
) : Domain
```

### 2. Criar a Table (`database/tables/`)
```kotlin
object NovoDominioTable : Table("novo_dominio") {
    val id = varchar("id", 36)
    val campo = varchar("campo", 255)
    override val primaryKey = PrimaryKey(id)

    fun toDomain(row: ResultRow) = NovoDominio(
        id = UUID.fromString(row[id]),
        campo = row[campo],
    )
}
```
Registrar em `DatabaseMigration.kt` no `SchemaUtils.create(...)`.

### 3. Criar o Repository (`repositories/{domain}/`)
Interface:
```kotlin
interface INovoDominioRepository : BasicCrud<NovoDominio> {
    // métodos específicos
}
```
Implementação usa `transaction(database) { ... }` + `runCatching { }`.

### 4. Criar o UseCase (`usecases/{domain}/`)
```kotlin
class CreateNovoDominioUseCase(
    private val repository: INovoDominioRepository,
) : UseCase<Input, Output> {
    override fun execute(input: Input): Result<Output> =
        repository.create(inputToDomain(input)).map { domainToOutput(it) }

    data class Input(...)
    data class Output(...)
}
```
- Sempre `UseCase<I, O>` com `execute(input: I): Result<O>`
- `UseCaseWithoutOutput<O>` quando não há input
- Input/Output são data classes internas ao use case

### 5. Criar as Routes (`routes/`)
```kotlin
fun Route.novoDominioRoutes() {
    val createUseCase: CreateNovoDominioUseCase by getKoin().inject()

    route("/novo-dominio") {
        post {
            val input = call.receive<CreateNovoDominioUseCase.Input>()
            foldAndRespond(createUseCase.execute(input))
        }
    }
}
```
Registrar em `Main.kt` dentro do bloco `authenticate("jwt-auth")`.

### 6. Registrar no DI (`infraestructure/DI.kt`)
```kotlin
// repository
single { NovoDominioRepository(get()) } bind INovoDominioRepository::class
// use case
single { CreateNovoDominioUseCase(get()) }
```

## Convenções

- **Error handling**: Kotlin `Result<T>` em toda a camada de negócio. Repositórios usam `runCatching {}`.
- **Responses**: `foldAndRespond()` converte Result → HTTP (200/204/500).
- **IDs**: `UUID` como tipo, armazenado como `varchar(36)` no SQLite.
- **Naming**: Código em inglês, rotas REST em inglês.
- **Rotas protegidas**: Tudo sob `authenticate("jwt-auth")` em `Main.kt`.
- **Rotas públicas**: Definidas fora do bloco `authenticate` (ex: loginRoutes).
