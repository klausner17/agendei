# Login por email/senha + Rate Limiting — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Substituir o login Google OAuth por autenticação email/senha com hash PBKDF2 (nativo do JDK) e adicionar rate limiting em todas as rotas, mantendo o JWT e a camada de autorização intactos.

**Architecture:** Um novo service `PasswordHasher` (PBKDF2-HMAC-SHA256) é injetado em dois use cases (`RegisterUserUseCase`, `LoginUseCase`) que emitem o mesmo JWT de hoje (`subject = user.id`). As rotas públicas `/auth/register` e `/auth/login` substituem `/auth/google`. O plugin Ktor `RateLimit` aplica um limite global a todas as rotas e um limite estrito às rotas de auth.

**Tech Stack:** Kotlin 2.1, Ktor 3.1, Exposed 0.59, Koin 4.1, SQLite, Auth0 java-jwt, MockK (testes), `javax.crypto` (PBKDF2), `io.ktor:ktor-server-rate-limit`.

## Global Constraints

- Hash de senha: **PBKDF2-HMAC-SHA256**, salt 16 bytes, **210000 iterações**, chave **256 bits**. Sem dependências de hash externas.
- Encoding da senha gravado em `password_hash`: `pbkdf2_sha256$<iterations>$<saltBase64>$<hashBase64>`.
- Identificador de login: **email**. Validação de senha no registro: **mínimo 8 caracteres**.
- Rate limit: **global 100 req/min** em todas as rotas + **5 req/min** nas rotas de auth, chave por IP (`call.request.origin.remoteHost`).
- JWT inalterado: `subject = user.id`, claims `email`/`name`, HMAC256 com `JWT_SECRET`, expiração 7 dias.
- Versões: `ktorVersion = "3.1.0"` (use `$ktorVersion`).
- Código em inglês; rotas REST em inglês. Object Calisthenics (métodos ≤10 linhas, um nível de indentação, sem `else`, sem abreviações).
- Rodar `./gradlew ktlintCheck` antes de cada commit; mensagens de commit terminam com a linha de co-autoria.
- Banco de dev descartável: a tabela `users` é recriada (sem migração de dados).

---

### Task 1: Service `PasswordHasher` (PBKDF2)

**Files:**
- Create: `src/main/kotlin/com/klausner/services/PasswordHasher.kt`
- Test: `src/test/kotlin/com/klausner/services/PasswordHasherTest.kt`
- Modify: `src/main/kotlin/com/klausner/infraestructure/DI.kt`

**Interfaces:**
- Consumes: nada (service autônomo).
- Produces: `class PasswordHasher` com `fun hash(password: String): String` e `fun verify(password: String, stored: String): Boolean`.

- [ ] **Step 1: Escrever o teste que falha**

Create `src/test/kotlin/com/klausner/services/PasswordHasherTest.kt`:

```kotlin
package com.klausner.services

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {
    private val passwordHasher = PasswordHasher()

    @Test
    fun `should produce a hash different from the plain password`() {
        // when
        val hash = passwordHasher.hash("my-secret-password")

        // then
        assertNotEquals("my-secret-password", hash)
    }

    @Test
    fun `should verify a correct password`() {
        // given
        val hash = passwordHasher.hash("my-secret-password")

        // when
        val matches = passwordHasher.verify("my-secret-password", hash)

        // then
        assertTrue(matches)
    }

    @Test
    fun `should reject a wrong password`() {
        // given
        val hash = passwordHasher.hash("my-secret-password")

        // when
        val matches = passwordHasher.verify("wrong-password", hash)

        // then
        assertFalse(matches)
    }

    @Test
    fun `should produce different hashes for the same password`() {
        // when
        val first = passwordHasher.hash("my-secret-password")
        val second = passwordHasher.hash("my-secret-password")

        // then
        assertNotEquals(first, second)
    }
}
```

- [ ] **Step 2: Rodar o teste e confirmar que falha**

Run: `./gradlew test --tests "com.klausner.services.PasswordHasherTest"`
Expected: FAIL na compilação — `unresolved reference: PasswordHasher`.

- [ ] **Step 3: Implementar o `PasswordHasher`**

Create `src/main/kotlin/com/klausner/services/PasswordHasher.kt`:

```kotlin
package com.klausner.services

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordHasher {
    private val random = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }
        val derived = deriveKey(password, salt, ITERATIONS)
        return "$PREFIX\$$ITERATIONS\$${encode(salt)}\$${encode(derived)}"
    }

    fun verify(
        password: String,
        stored: String,
    ): Boolean {
        val parts = stored.split("$")
        if (parts.size != 4 || parts[0] != PREFIX) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val derived = deriveKey(password, decode(parts[2]), iterations)
        return MessageDigest.isEqual(decode(parts[3]), derived)
    }

    private fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int,
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(value: String): ByteArray = Base64.getDecoder().decode(value)

    companion object {
        private const val PREFIX = "pbkdf2_sha256"
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 210_000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
    }
}
```

- [ ] **Step 4: Rodar o teste e confirmar que passa**

Run: `./gradlew test --tests "com.klausner.services.PasswordHasherTest"`
Expected: PASS (4 testes).

- [ ] **Step 5: Registrar no DI**

In `src/main/kotlin/com/klausner/infraestructure/DI.kt`, add the import (junto aos outros `import com.klausner.services.*`):

```kotlin
import com.klausner.services.PasswordHasher
```

E adicione o single no bloco `// services` (logo abaixo do `JwtService { ... }`):

```kotlin
        single { PasswordHasher() }
```

- [ ] **Step 6: ktlint + commit**

Run: `./gradlew ktlintCheck`
Expected: PASS.

```bash
git add src/main/kotlin/com/klausner/services/PasswordHasher.kt \
        src/test/kotlin/com/klausner/services/PasswordHasherTest.kt \
        src/main/kotlin/com/klausner/infraestructure/DI.kt
git commit -m "feat: adicionar PasswordHasher com PBKDF2

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 2: Remover código do Google OAuth

Remove o login Google por completo (service, use case, teste, DI, dependência Gradle e a rota). Deixa `loginRoutes()` vazio temporariamente — `/auth/register` e `/auth/login` entram na Task 6. O `User`/`UserRepository` ainda mantêm campos do Google neste ponto (removidos na Task 3); compilam normalmente porque ninguém mais os referencia.

**Files:**
- Delete: `src/main/kotlin/com/klausner/services/GoogleAuthService.kt`
- Delete: `src/main/kotlin/com/klausner/usecases/auth/GoogleAuthUseCase.kt`
- Delete: `src/test/kotlin/com/klausner/usecases/auth/GoogleAuthUseCaseTest.kt`
- Modify: `src/main/kotlin/com/klausner/infraestructure/DI.kt`
- Modify: `src/main/kotlin/com/klausner/routes/LoginRoutes.kt`
- Modify: `build.gradle.kts`

**Interfaces:**
- Consumes: nada.
- Produces: `fun Route.loginRoutes()` (corpo vazio) e `fun AuthenticationConfig.config()` (inalterado) permanecem disponíveis para o `Main.kt`.

- [ ] **Step 1: Deletar os arquivos do Google**

```bash
git rm src/main/kotlin/com/klausner/services/GoogleAuthService.kt \
       src/main/kotlin/com/klausner/usecases/auth/GoogleAuthUseCase.kt \
       src/test/kotlin/com/klausner/usecases/auth/GoogleAuthUseCaseTest.kt
```

- [ ] **Step 2: Remover do DI**

In `src/main/kotlin/com/klausner/infraestructure/DI.kt`:

Remova os imports:
```kotlin
import com.klausner.services.GoogleAuthService
import com.klausner.usecases.auth.GoogleAuthUseCase
```

Remova o bloco do service:
```kotlin
        single {
            GoogleAuthService(
                clientId = "47481749184-lv7j2hj1v1id47jsfq8c3sf77rthjf3g.apps.googleusercontent.com",
            )
        }
```

Remova a linha do use case:
```kotlin
        single { GoogleAuthUseCase(get(), get(), get()) }
```

- [ ] **Step 3: Esvaziar o `LoginRoutes.kt`**

Replace o conteúdo inteiro de `src/main/kotlin/com/klausner/routes/LoginRoutes.kt` por:

```kotlin
package com.klausner.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.routing.Route

fun AuthenticationConfig.config() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "your-super-secret-jwt-key-change-in-production"

    jwt("jwt-auth") {
        realm = "agendei"
        verifier(
            JWT
                .require(Algorithm.HMAC256(jwtSecret))
                .build(),
        )
        validate { credential ->
            if (credential.payload.subject == null) {
                null
            } else {
                JWTPrincipal(credential.payload)
            }
        }
    }
}

fun Route.loginRoutes() {
}
```

- [ ] **Step 4: Remover a dependência do Google no Gradle**

In `build.gradle.kts`, remova a linha:
```kotlin
    implementation("com.google.api-client:google-api-client:2.7.2")
```

- [ ] **Step 5: Confirmar que nenhuma referência ao Google sobrou**

Run: `grep -rn -i "google" src/main src/test build.gradle.kts`
Expected: nenhuma ocorrência (saída vazia).

- [ ] **Step 6: Compilar e rodar todos os testes**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (compila e passa testes restantes; ktlint inclusa).

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: remover autenticação Google OAuth

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 3: Simplificar `User`, `UserTable` e `UserRepository`

Reduz o domínio a `{id, email, name, passwordHash}`, recria a tabela com `password_hash` e `email` único, e ajusta o repositório (grava `passwordHash`, remove `findByGoogleId`).

**Files:**
- Modify: `src/main/kotlin/com/klausner/domains/User.kt`
- Modify: `src/main/kotlin/com/klausner/database/tables/UserTable.kt`
- Modify: `src/main/kotlin/com/klausner/repositories/user/IUserRepository.kt`
- Modify: `src/main/kotlin/com/klausner/repositories/user/UserRepository.kt`

**Interfaces:**
- Consumes: nada.
- Produces:
  - `data class User(id: UUID, email: String, name: String, passwordHash: String)`.
  - `interface IUserRepository : BasicCrud<User> { fun findByEmail(email: String): Result<User?> }`.

- [ ] **Step 1: Reescrever o domain `User`**

Replace o conteúdo de `src/main/kotlin/com/klausner/domains/User.kt` por:

```kotlin
package com.klausner.domains

import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val name: String,
    val passwordHash: String,
) : Domain
```

- [ ] **Step 2: Reescrever a `UserTable`**

Replace o conteúdo de `src/main/kotlin/com/klausner/database/tables/UserTable.kt` por:

```kotlin
package com.klausner.database.tables

import com.klausner.domains.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object UserTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255)
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)

    fun toDomain(row: ResultRow): User {
        return User(
            id = row[id],
            email = row[email],
            name = row[name],
            passwordHash = row[passwordHash],
        )
    }
}
```

- [ ] **Step 3: Atualizar a interface `IUserRepository`**

Replace o conteúdo de `src/main/kotlin/com/klausner/repositories/user/IUserRepository.kt` por:

```kotlin
package com.klausner.repositories.user

import com.klausner.domains.User
import com.klausner.repositories.interfaces.BasicCrud

interface IUserRepository : BasicCrud<User> {
    fun findByEmail(email: String): Result<User?>
}
```

- [ ] **Step 4: Atualizar a `UserRepository`**

In `src/main/kotlin/com/klausner/repositories/user/UserRepository.kt`, substitua o método `create` por (note as colunas: só `id`, `email`, `name`, `passwordHash`):

```kotlin
    override fun create(obj: User): Result<User> =
        runCatching {
            transaction(database) {
                UserTable.insert {
                    it[id] = obj.id
                    it[email] = obj.email
                    it[name] = obj.name
                    it[passwordHash] = obj.passwordHash
                }

                UserTable
                    .selectAll()
                    .where { UserTable.id eq obj.id }
                    .map { row -> UserTable.toDomain(row) }
                    .singleOrNull()!!
            }
        }
```

E **remova** o método `findByGoogleId` inteiro:

```kotlin
    override fun findByGoogleId(googleId: String): Result<User?> =
        runCatching {
            transaction(database) {
                UserTable
                    .selectAll()
                    .where { UserTable.googleId eq googleId }
                    .map { row -> UserTable.toDomain(row) }
                    .singleOrNull()
            }
        }
```

(Mantenha `findByEmail`, `delete`, `find`, `update` como estão.)

- [ ] **Step 5: Compilar e rodar todos os testes**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: ktlint + commit**

Run: `./gradlew ktlintCheck`
Expected: PASS.

```bash
git add src/main/kotlin/com/klausner/domains/User.kt \
        src/main/kotlin/com/klausner/database/tables/UserTable.kt \
        src/main/kotlin/com/klausner/repositories/user/IUserRepository.kt \
        src/main/kotlin/com/klausner/repositories/user/UserRepository.kt
git commit -m "refactor: simplificar User para email/senha

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 4: `RegisterUserUseCase`

Use case de registro: valida email/senha, rejeita email duplicado, hasheia, cria usuário e emite JWT.

**Files:**
- Create: `src/main/kotlin/com/klausner/usecases/auth/RegisterUserUseCase.kt`
- Test: `src/test/kotlin/com/klausner/usecases/auth/RegisterUserUseCaseTest.kt`
- Modify: `src/main/kotlin/com/klausner/infraestructure/DI.kt`

**Interfaces:**
- Consumes: `PasswordHasher.hash(password)` (Task 1); `IUserRepository.findByEmail`/`create` (Task 3); `JwtService.generateToken(user)`; `User(id, email, name, passwordHash)`.
- Produces:
  - `class RegisterUserUseCase(userRepository: IUserRepository, passwordHasher: PasswordHasher, jwtService: JwtService)`
  - `RegisterUserUseCase.Input(email: String, name: String, password: String)`
  - `RegisterUserUseCase.Output(token: String, user: UserData)`
  - `RegisterUserUseCase.UserData(id: String, email: String, name: String)`

- [ ] **Step 1: Escrever os testes que falham**

Create `src/test/kotlin/com/klausner/usecases/auth/RegisterUserUseCaseTest.kt`:

```kotlin
package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterUserUseCaseTest {
    private val userRepository = mockk<IUserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val jwtService = mockk<JwtService>()
    private val useCase = RegisterUserUseCase(userRepository, passwordHasher, jwtService)

    private val user =
        User(id = UUID.randomUUID(), email = "ana@email.com", name = "Ana", passwordHash = "hashed")

    @Test
    fun `should register user successfully`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(null)
        every { passwordHasher.hash("password123") } returns "hashed"
        every { userRepository.create(any()) } returns Result.success(user)
        every { jwtService.generateToken(user) } returns "jwt-token"

        // when
        val result = useCase.execute(RegisterUserUseCase.Input("ana@email.com", "Ana", "password123"))

        // then
        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrThrow().token)
        assertEquals("ana@email.com", result.getOrThrow().user.email)
        verify(exactly = 1) { userRepository.create(any()) }
    }

    @Test
    fun `should fail when email already registered`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(user)

        // when
        val result = useCase.execute(RegisterUserUseCase.Input("ana@email.com", "Ana", "password123"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun `should fail when password is shorter than 8 characters`() {
        // when
        val result = useCase.execute(RegisterUserUseCase.Input("ana@email.com", "Ana", "short"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(exactly = 0) { userRepository.create(any()) }
    }
}
```

- [ ] **Step 2: Rodar os testes e confirmar que falham**

Run: `./gradlew test --tests "com.klausner.usecases.auth.RegisterUserUseCaseTest"`
Expected: FAIL na compilação — `unresolved reference: RegisterUserUseCase`.

- [ ] **Step 3: Implementar o `RegisterUserUseCase`**

Create `src/main/kotlin/com/klausner/usecases/auth/RegisterUserUseCase.kt`:

```kotlin
package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import com.klausner.usecases.UseCase
import java.util.UUID

class RegisterUserUseCase(
    private val userRepository: IUserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) : UseCase<RegisterUserUseCase.Input, RegisterUserUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        runCatching {
            validate(input)
            ensureEmailIsAvailable(input.email)
            val user = createUser(input)
            Output(token = jwtService.generateToken(user), user = toUserData(user))
        }

    private fun validate(input: Input) {
        require(input.email.isNotBlank()) { "Email is required" }
        require(input.password.length >= MINIMUM_PASSWORD_LENGTH) {
            "Password must have at least $MINIMUM_PASSWORD_LENGTH characters"
        }
    }

    private fun ensureEmailIsAvailable(email: String) {
        val existing = userRepository.findByEmail(email).getOrThrow()
        require(existing == null) { "Email already registered" }
    }

    private fun createUser(input: Input): User {
        val user =
            User(
                id = UUID.randomUUID(),
                email = input.email,
                name = input.name,
                passwordHash = passwordHasher.hash(input.password),
            )
        return userRepository.create(user).getOrThrow()
    }

    private fun toUserData(user: User) =
        UserData(id = user.id.toString(), email = user.email, name = user.name)

    data class Input(
        val email: String,
        val name: String,
        val password: String,
    )

    data class Output(
        val token: String,
        val user: UserData,
    )

    data class UserData(
        val id: String,
        val email: String,
        val name: String,
    )

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH = 8
    }
}
```

- [ ] **Step 4: Rodar os testes e confirmar que passam**

Run: `./gradlew test --tests "com.klausner.usecases.auth.RegisterUserUseCaseTest"`
Expected: PASS (3 testes).

- [ ] **Step 5: Registrar no DI**

In `src/main/kotlin/com/klausner/infraestructure/DI.kt`, add o import:

```kotlin
import com.klausner.usecases.auth.RegisterUserUseCase
```

E no bloco `// use cases`:

```kotlin
        single { RegisterUserUseCase(get(), get(), get()) }
```

- [ ] **Step 6: ktlint + commit**

Run: `./gradlew ktlintCheck`
Expected: PASS.

```bash
git add src/main/kotlin/com/klausner/usecases/auth/RegisterUserUseCase.kt \
        src/test/kotlin/com/klausner/usecases/auth/RegisterUserUseCaseTest.kt \
        src/main/kotlin/com/klausner/infraestructure/DI.kt
git commit -m "feat: adicionar RegisterUserUseCase

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 5: `LoginUseCase` + `InvalidCredentialsException` (401)

Use case de login + nova exceção mapeada para 401 em `statusFor`.

**Files:**
- Create: `src/main/kotlin/com/klausner/usecases/auth/InvalidCredentialsException.kt`
- Create: `src/main/kotlin/com/klausner/usecases/auth/LoginUseCase.kt`
- Test: `src/test/kotlin/com/klausner/usecases/auth/LoginUseCaseTest.kt`
- Modify: `src/main/kotlin/com/klausner/infraestructure/ResultExtesions.kt`
- Modify: `src/main/kotlin/com/klausner/infraestructure/DI.kt`

**Interfaces:**
- Consumes: `PasswordHasher.verify(password, stored)` (Task 1); `IUserRepository.findByEmail` (Task 3); `JwtService.generateToken(user)`; `User`.
- Produces:
  - `class InvalidCredentialsException : RuntimeException`
  - `class LoginUseCase(userRepository: IUserRepository, passwordHasher: PasswordHasher, jwtService: JwtService)`
  - `LoginUseCase.Input(email: String, password: String)`
  - `LoginUseCase.Output(token: String, user: UserData)`
  - `LoginUseCase.UserData(id: String, email: String, name: String)`

- [ ] **Step 1: Escrever os testes que falham**

Create `src/test/kotlin/com/klausner/usecases/auth/LoginUseCaseTest.kt`:

```kotlin
package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {
    private val userRepository = mockk<IUserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val jwtService = mockk<JwtService>()
    private val useCase = LoginUseCase(userRepository, passwordHasher, jwtService)

    private val user =
        User(id = UUID.randomUUID(), email = "ana@email.com", name = "Ana", passwordHash = "stored-hash")

    @Test
    fun `should login successfully`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(user)
        every { passwordHasher.verify("password123", "stored-hash") } returns true
        every { jwtService.generateToken(user) } returns "jwt-token"

        // when
        val result = useCase.execute(LoginUseCase.Input("ana@email.com", "password123"))

        // then
        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrThrow().token)
    }

    @Test
    fun `should fail when user does not exist`() {
        // given
        every { userRepository.findByEmail("ghost@email.com") } returns Result.success(null)

        // when
        val result = useCase.execute(LoginUseCase.Input("ghost@email.com", "password123"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }

    @Test
    fun `should fail when password is wrong`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(user)
        every { passwordHasher.verify("wrong", "stored-hash") } returns false

        // when
        val result = useCase.execute(LoginUseCase.Input("ana@email.com", "wrong"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }
}
```

- [ ] **Step 2: Rodar os testes e confirmar que falham**

Run: `./gradlew test --tests "com.klausner.usecases.auth.LoginUseCaseTest"`
Expected: FAIL na compilação — `unresolved reference: LoginUseCase`.

- [ ] **Step 3: Criar a exceção**

Create `src/main/kotlin/com/klausner/usecases/auth/InvalidCredentialsException.kt`:

```kotlin
package com.klausner.usecases.auth

class InvalidCredentialsException : RuntimeException("Invalid email or password")
```

- [ ] **Step 4: Implementar o `LoginUseCase`**

Create `src/main/kotlin/com/klausner/usecases/auth/LoginUseCase.kt`:

```kotlin
package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import com.klausner.usecases.UseCase

class LoginUseCase(
    private val userRepository: IUserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) : UseCase<LoginUseCase.Input, LoginUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        runCatching {
            val user = authenticate(input)
            Output(token = jwtService.generateToken(user), user = toUserData(user))
        }

    private fun authenticate(input: Input): User {
        val user =
            userRepository.findByEmail(input.email).getOrThrow()
                ?: throw InvalidCredentialsException()
        if (passwordHasher.verify(input.password, user.passwordHash)) return user
        throw InvalidCredentialsException()
    }

    private fun toUserData(user: User) =
        UserData(id = user.id.toString(), email = user.email, name = user.name)

    data class Input(
        val email: String,
        val password: String,
    )

    data class Output(
        val token: String,
        val user: UserData,
    )

    data class UserData(
        val id: String,
        val email: String,
        val name: String,
    )
}
```

- [ ] **Step 5: Rodar os testes e confirmar que passam**

Run: `./gradlew test --tests "com.klausner.usecases.auth.LoginUseCaseTest"`
Expected: PASS (3 testes).

- [ ] **Step 6: Mapear a exceção para 401 em `statusFor`**

In `src/main/kotlin/com/klausner/infraestructure/ResultExtesions.kt`, add o import:

```kotlin
import com.klausner.usecases.auth.InvalidCredentialsException
```

E adicione o ramo no `when` de `statusFor` (antes do `else`):

```kotlin
        is InvalidCredentialsException -> HttpStatusCode.Unauthorized
```

O `when` fica:

```kotlin
fun statusFor(exception: Throwable): HttpStatusCode =
    when (exception) {
        is NoSuchElementException -> HttpStatusCode.NotFound
        is IllegalArgumentException -> HttpStatusCode.BadRequest
        is IllegalStateException -> HttpStatusCode.UnprocessableEntity
        is SecurityException -> HttpStatusCode.Forbidden
        is InvalidCredentialsException -> HttpStatusCode.Unauthorized
        else -> HttpStatusCode.InternalServerError
    }
```

- [ ] **Step 7: Registrar no DI**

In `src/main/kotlin/com/klausner/infraestructure/DI.kt`, add o import:

```kotlin
import com.klausner.usecases.auth.LoginUseCase
```

E no bloco `// use cases`:

```kotlin
        single { LoginUseCase(get(), get(), get()) }
```

- [ ] **Step 8: Build + ktlint + commit**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL.

```bash
git add src/main/kotlin/com/klausner/usecases/auth/InvalidCredentialsException.kt \
        src/main/kotlin/com/klausner/usecases/auth/LoginUseCase.kt \
        src/test/kotlin/com/klausner/usecases/auth/LoginUseCaseTest.kt \
        src/main/kotlin/com/klausner/infraestructure/ResultExtesions.kt \
        src/main/kotlin/com/klausner/infraestructure/DI.kt
git commit -m "feat: adicionar LoginUseCase com 401 para credenciais inválidas

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 6: Rotas `/auth/register` e `/auth/login`

Preenche `loginRoutes()` com os dois endpoints públicos. (Rate limit é adicionado na Task 7; aqui as rotas ficam sem o wrapper ainda.)

**Files:**
- Modify: `src/main/kotlin/com/klausner/routes/LoginRoutes.kt`

**Interfaces:**
- Consumes: `RegisterUserUseCase` + `RegisterUserUseCase.Input` (Task 4); `LoginUseCase` + `LoginUseCase.Input` (Task 5); `foldAndRespond` (`infraestructure`).
- Produces: `POST /api/v1/auth/register` e `POST /api/v1/auth/login`; DTOs `RegisterRequest(email, name, password)` e `LoginRequest(email, password)`.

- [ ] **Step 1: Reescrever o `LoginRoutes.kt`**

Replace o conteúdo inteiro de `src/main/kotlin/com/klausner/routes/LoginRoutes.kt` por:

```kotlin
package com.klausner.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.auth.LoginUseCase
import com.klausner.usecases.auth.RegisterUserUseCase
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.java.KoinJavaComponent.getKoin

fun AuthenticationConfig.config() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "your-super-secret-jwt-key-change-in-production"

    jwt("jwt-auth") {
        realm = "agendei"
        verifier(
            JWT
                .require(Algorithm.HMAC256(jwtSecret))
                .build(),
        )
        validate { credential ->
            if (credential.payload.subject == null) {
                null
            } else {
                JWTPrincipal(credential.payload)
            }
        }
    }
}

fun Route.loginRoutes() {
    val registerUserUseCase: RegisterUserUseCase by getKoin().inject()
    val loginUseCase: LoginUseCase by getKoin().inject()

    post("/api/v1/auth/register") {
        val request = call.receive<RegisterRequest>()
        val input = RegisterUserUseCase.Input(request.email, request.name, request.password)
        foldAndRespond(registerUserUseCase.execute(input))
    }

    post("/api/v1/auth/login") {
        val request = call.receive<LoginRequest>()
        val input = LoginUseCase.Input(request.email, request.password)
        foldAndRespond(loginUseCase.execute(input))
    }
}

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)
```

- [ ] **Step 2: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Verificação manual (smoke test)**

Apague o banco de dev e suba a aplicação:
```bash
rm -f data.db
./gradlew run
```
Em outro terminal:
```bash
# registro → espera 200 com {token, user}
curl -s -X POST localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@email.com","name":"Ana","password":"password123"}'

# login correto → espera 200 com {token, user}
curl -s -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@email.com","password":"password123"}'

# login errado → espera HTTP 401
curl -s -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@email.com","password":"wrong"}'

# senha curta no registro → espera HTTP 400
curl -s -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"b@email.com","name":"B","password":"short"}'
```
Expected: 200, 200, 401, 400. Encerre a app (`Ctrl+C`).

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/klausner/routes/LoginRoutes.kt
git commit -m "feat: adicionar rotas de registro e login

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 7: Rate limiting (global + auth)

Adiciona o plugin Ktor `RateLimit`: limite global em todas as rotas + limite estrito nas rotas de auth.

**Files:**
- Modify: `build.gradle.kts`
- Modify: `src/main/kotlin/com/klausner/Main.kt`
- Modify: `src/main/kotlin/com/klausner/routes/LoginRoutes.kt`

**Interfaces:**
- Consumes: `loginRoutes()` (Task 6).
- Produces: `RateLimitName("auth")` aplicado a `/auth/register` e `/auth/login`; limite global em todas as rotas.

- [ ] **Step 1: Adicionar a dependência Gradle**

In `build.gradle.kts`, no bloco `dependencies`, adicione (junto aos outros `io.ktor:ktor-server-*`):

```kotlin
    implementation("io.ktor:ktor-server-rate-limit:$ktorVersion")
```

- [ ] **Step 2: Instalar o plugin no `Main.kt`**

In `src/main/kotlin/com/klausner/Main.kt`, adicione os imports:

```kotlin
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.seconds
```

E adicione o bloco de instalação logo após `install(Authentication) { config() }`:

```kotlin
        install(RateLimit) {
            global {
                rateLimiter(limit = 100, refillPeriod = 60.seconds)
                requestKey { call -> call.request.origin.remoteHost }
            }
            register(RateLimitName("auth")) {
                rateLimiter(limit = 5, refillPeriod = 60.seconds)
                requestKey { call -> call.request.origin.remoteHost }
            }
        }
```

- [ ] **Step 3: Envolver as rotas de auth com o limiter estrito**

In `src/main/kotlin/com/klausner/routes/LoginRoutes.kt`, adicione os imports:

```kotlin
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
```

E envolva os dois `post` dentro de `rateLimit(RateLimitName("auth")) { ... }`. A função `loginRoutes()` fica:

```kotlin
fun Route.loginRoutes() {
    val registerUserUseCase: RegisterUserUseCase by getKoin().inject()
    val loginUseCase: LoginUseCase by getKoin().inject()

    rateLimit(RateLimitName("auth")) {
        post("/api/v1/auth/register") {
            val request = call.receive<RegisterRequest>()
            val input = RegisterUserUseCase.Input(request.email, request.name, request.password)
            foldAndRespond(registerUserUseCase.execute(input))
        }

        post("/api/v1/auth/login") {
            val request = call.receive<LoginRequest>()
            val input = LoginUseCase.Input(request.email, request.password)
            foldAndRespond(loginUseCase.execute(input))
        }
    }
}
```

- [ ] **Step 4: Build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Verificação manual do rate limit**

```bash
rm -f data.db
./gradlew run
```
Em outro terminal, dispare 7 logins seguidos (limite de auth = 5/min) e observe os códigos:
```bash
for i in $(seq 1 7); do
  curl -s -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/api/v1/auth/login \
    -H 'Content-Type: application/json' \
    -d '{"email":"x@email.com","password":"whatever1"}'
done
```
Expected: as primeiras respostas são `401` (credenciais inválidas) e, após exceder o limite, passa a `429`. Encerre a app (`Ctrl+C`).

- [ ] **Step 6: Commit**

```bash
git add build.gradle.kts \
        src/main/kotlin/com/klausner/Main.kt \
        src/main/kotlin/com/klausner/routes/LoginRoutes.kt
git commit -m "feat: adicionar rate limiting global e nas rotas de auth

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 8: Atualizar `docs/openapi.yaml`

Remove o path do Google e documenta `/register`, `/login` e a resposta 429. Satisfaz a diretiva do CLAUDE.md de manter o OpenAPI sincronizado com `routes/`.

**Files:**
- Modify: `docs/openapi.yaml`

**Interfaces:**
- Consumes: rotas finais (`/api/v1/auth/register`, `/api/v1/auth/login`) da Task 6/7.
- Produces: spec OpenAPI atualizado.

- [ ] **Step 1: Substituir o bloco do Google pelos novos paths**

In `docs/openapi.yaml`, na seção `# ─── Auth ───`, substitua todo o bloco `/api/v1/auth/google:` (do `/api/v1/auth/google:` até logo antes de `# ─── Professionals ───`) por:

```yaml
  /api/v1/auth/register:
    post:
      summary: Registrar usuário
      description: Cria um usuário com email/senha e retorna um JWT da aplicação.
      tags: [Auth]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [email, name, password]
              properties:
                email:
                  type: string
                name:
                  type: string
                password:
                  type: string
                  minLength: 8
      responses:
        "200":
          description: Registro bem-sucedido
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/AuthResponse"
        "400":
          description: Dados inválidos ou email já cadastrado
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
        "429":
          description: Limite de requisições excedido
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"

  /api/v1/auth/login:
    post:
      summary: Login com email/senha
      description: Valida as credenciais e retorna um JWT da aplicação.
      tags: [Auth]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [email, password]
              properties:
                email:
                  type: string
                password:
                  type: string
      responses:
        "200":
          description: Login bem-sucedido
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/AuthResponse"
        "401":
          description: Credenciais inválidas
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
        "429":
          description: Limite de requisições excedido
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
```

- [ ] **Step 2: Adicionar o schema `AuthResponse` em `components/schemas`**

In `docs/openapi.yaml`, dentro de `components: schemas:` (mesma indentação dos outros schemas, ex.: ao lado de `ErrorResponse`), adicione:

```yaml
    AuthResponse:
      type: object
      properties:
        token:
          type: string
          description: JWT da aplicação
        user:
          type: object
          properties:
            id:
              type: string
            email:
              type: string
            name:
              type: string
```

- [ ] **Step 3: Verificar que não há referências ao Google no spec**

Run: `grep -n -i "google\|credential\|picture\|provider" docs/openapi.yaml`
Expected: nenhuma ocorrência relacionada a auth (saída vazia, ou apenas ocorrências não relacionadas a este fluxo).

- [ ] **Step 4: Commit**

```bash
git add docs/openapi.yaml
git commit -m "docs: atualizar OpenAPI para login email/senha

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 9: Verificação final

**Files:** nenhum (apenas verificação).

- [ ] **Step 1: Build completo limpo**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL; todos os testes passam (incl. `PasswordHasherTest`, `RegisterUserUseCaseTest`, `LoginUseCaseTest`).

- [ ] **Step 2: Confirmar ausência total de Google no código e specs**

Run: `grep -rn -i "google" src/ build.gradle.kts docs/openapi.yaml`
Expected: saída vazia.

- [ ] **Step 3: ktlint**

Run: `./gradlew ktlintCheck`
Expected: PASS.

---

## Notas para o executor

- **Frontend fora de escopo:** `../agendei-frontend` ainda usa o fluxo Google e consome `picture`/`provider`. Não faz parte deste plano (repo separado); precisará de ajuste posterior.
- **Camada de autorização intacta:** `JwtService`, `jwt-auth` (`config()`), `principalUserId()` e os checks `professional.userId == requesterId` não mudam — o `subject` do JWT continua sendo `user.id`. Só muda a origem do token.
- **Banco de dev:** como a tabela `users` foi recriada com novas colunas, apague `data.db` antes de subir a app (já incluído nos smoke tests).
