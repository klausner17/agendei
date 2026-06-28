# Login por email/senha + Rate Limiting — Design

**Data:** 2026-06-28
**Branch:** feat/authorization

## Objetivo

Remover a autenticação via Google OAuth e substituir por login com **email + senha**,
usando hash **PBKDF2** (nativo do JDK, sem dependências novas). Adicionar **rate limiting**
em todas as rotas (com limite mais estrito nas rotas de autenticação) para mitigar brute
force. O JWT de aplicação permanece inalterado.

## Decisões

| Tópico | Decisão |
|--------|---------|
| Algoritmo de hash | PBKDF2-HMAC-SHA256 (JDK `javax.crypto`), sem libs novas |
| Identificador de login | Email (reaproveita campo existente) |
| Criação de usuário | Endpoint público de registro `POST /api/v1/auth/register` |
| Campo `name` | Coletado no registro (`{email, name, password}`) |
| Validação de senha | Mínimo 8 caracteres |
| Migração da tabela | Recriar `users` (banco de dev descartável `data.db`) |
| Rate limit | Global 100 req/min em todas as rotas + 5 req/min nas rotas de auth, chave por IP |

## Hash de senha — `PasswordHasher` (novo service)

- **Algoritmo:** PBKDF2-HMAC-SHA256.
- **Parâmetros (OWASP):** salt aleatório de 16 bytes por usuário, 210.000 iterações,
  comprimento de chave de 256 bits.
- **Encoding** (string única gravada em `password_hash`):
  `pbkdf2_sha256$<iterations>$<saltBase64>$<hashBase64>`.
- **API:**
  - `hash(password: String): String` — gera salt, deriva, retorna a string codificada.
  - `verify(password: String, stored: String): Boolean` — faz parse da string,
    re-deriva com o mesmo salt/iterations e compara em tempo constante
    (`MessageDigest.isEqual`).

## Domain `User` (simplificado)

```kotlin
data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val name: String,
    val passwordHash: String,
) : Domain
```

Remove os campos: `picture`, `provider`, `googleId`, `emailVerified`.

## Tabela `users` (recriada)

```kotlin
object UserTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255)
    val passwordHash = varchar("password_hash", 255)
    override val primaryKey = PrimaryKey(id)
    fun toDomain(row: ResultRow): User { ... }
}
```

Remove as colunas do Google. Banco de dev descartável — assume-se recriação de `data.db`.

## Repository `IUserRepository`

- Mantém `findByEmail(email): Result<User?>`.
- **Remove** `findByGoogleId`.
- `create` passa a gravar `passwordHash`.

## Use cases (substituem `GoogleAuthUseCase`)

### `RegisterUserUseCase`
- **Input:** `{ email: String, name: String, password: String }`
- **Fluxo:**
  1. Valida: email não-vazio, senha com ≥ 8 caracteres → senão `IllegalArgumentException` (400).
  2. Rejeita email já cadastrado (`findByEmail`) → `IllegalArgumentException` (400).
  3. `passwordHasher.hash(password)`.
  4. `userRepository.create(...)`.
  5. Gera JWT.
- **Output:** `{ token: String, user: UserData{ id, email, name } }`

### `LoginUseCase`
- **Input:** `{ email: String, password: String }`
- **Fluxo:**
  1. `findByEmail`; se nulo → `InvalidCredentialsException` (401).
  2. `passwordHasher.verify`; se falso → `InvalidCredentialsException` (401).
  3. Gera JWT.
- **Output:** `{ token: String, user: UserData{ id, email, name } }`

## Rotas (`LoginRoutes.kt`)

- **Remove:** `POST /api/v1/auth/google`.
- **Adiciona (públicas, fora do bloco `authenticate`):**
  - `POST /api/v1/auth/register` → `{email, name, password}`
  - `POST /api/v1/auth/login` → `{email, password}`
- Ambas envoltas no limiter estrito de auth.

## Rate Limiting (`Main.kt`)

- Dependência: `io.ktor:ktor-server-rate-limit:$ktorVersion`.
- `install(RateLimit)`:
  - **Global:** `limit = 100`, `refillPeriod = 60.seconds` — aplicado automaticamente a todas as rotas.
  - **Nomeado `RateLimitName("auth")`:** `limit = 5`, `refillPeriod = 60.seconds`.
  - Chave por IP: `requestKey { call.request.origin.remoteHost }`.
- Rotas de auth envoltas em `rateLimit(RateLimitName("auth")) { ... }`.
- Excedente retorna **429 Too Many Requests** (comportamento padrão do plugin).

## Mudanças de suporte

- `statusFor` (ResultExtesions.kt): adicionar `is InvalidCredentialsException -> HttpStatusCode.Unauthorized` (401).
- `InvalidCredentialsException`: nova exceção em `usecases/auth/`.
- DI (`infraestructure/DI.kt`): remover `GoogleAuthService` e `GoogleAuthUseCase`;
  adicionar `PasswordHasher`, `RegisterUserUseCase`, `LoginUseCase`.
- `build.gradle.kts`: remover `com.google.api-client:google-api-client`; adicionar `ktor-server-rate-limit`.
- Deletar: `GoogleAuthService.kt`, `GoogleAuthUseCase.kt`, `GoogleAuthUseCaseTest.kt`.
- `docs/openapi.yaml`: remover path `/api/v1/auth/google`; adicionar `/register` e `/login`;
  documentar resposta 429; atualizar schema de `User` (remover picture/provider/etc.).

## Impacto em JWT e autorização

A camada de JWT e autorização (foco da branch `feat/authorization`) permanece intacta.
Só muda a **origem** do token; o formato e a validação são os mesmos.

**Permanece igual:**
- `JwtService.generateToken(user)` — `subject` = `user.id` (UUID), claims `email`/`name`,
  expiração 7 dias, HMAC256 com `JWT_SECRET`.
- Validação `jwt-auth` (`LoginRoutes.kt:config()`) — verifica assinatura + subject não-nulo → `JWTPrincipal`.
- `principalUserId()` (`PrincipalExt.kt`) — lê `payload.subject` como UUID.
- Checks de ownership nos use cases — `professional.userId == requesterId`, com
  `requesterId = principalUserId()`. Como o `subject` continua sendo `user.id`, funcionam sem alteração.
- Bloco `authenticate("jwt-auth")` em `Main.kt` — todas as rotas `/api/v1` seguem protegidas.

**Muda apenas a origem do token:**
- Antes: `POST /api/v1/auth/google { credential } → { token, user }`.
- Agora: `POST /api/v1/auth/register { email, name, password }` e
  `POST /api/v1/auth/login { email, password }`, ambos retornando o mesmo `{ token, user{id,email,name} }`.

**Fluxo:**
```
1. register/login  → JwtService.generateToken(user) → { token, user }
2. cliente         → Authorization: Bearer <token>
3. jwt-auth valida → JWTPrincipal(subject = user.id)
4. principalUserId() → UUID
5. use case        → professional.userId == requesterId
```

**Ordem dos plugins:** rate limit roda antes da autenticação. Rotas protegidas pegam o
limite global (100/min); `/login` e `/register` pegam o limite estrito de auth (5/min).

## Fora de escopo

- **Frontend** (`../agendei-frontend`): usa o fluxo Google e consome `picture`/`provider`
  na resposta. Precisará de ajuste posterior, mas é repositório separado e **não** faz
  parte deste plano.

## Testes (TDD)

- **`PasswordHasherTest`:**
  - hash difere da senha em texto puro;
  - `verify` retorna `true` para a senha correta;
  - `verify` retorna `false` para senha errada;
  - dois hashes da mesma senha diferem (salts aleatórios distintos).
- **`RegisterUserUseCaseTest`:**
  - registra com sucesso (cria usuário + retorna token);
  - falha com email já cadastrado;
  - falha com senha menor que 8 caracteres.
- **`LoginUseCaseTest`:**
  - login com sucesso retorna token;
  - falha quando usuário não existe;
  - falha quando a senha está errada.
