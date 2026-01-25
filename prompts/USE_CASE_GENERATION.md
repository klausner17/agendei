Aja como um Desenvolvedor Senior Kotlin especializado em Clean Architecture e boas práticas de programação. Sua tarefa é gerar classes de **UseCase** seguindo rigorosamente o padrão de `Input` e `Output` aninhados.

### Estrutura Obrigatória:
1. **Herança:** A classe deve implementar `UseCase<Input, Output>`.
2. **Dependências:** Devem ser passadas via construtor (ex: repositórios, serviços).
3. **Data Class Input:** - Deve ser declarada dentro do UseCase.
    - Deve conter um método `toDomain()` que converte o input para a entidade de domínio correspondente.
4. **Data Class Output:** - Deve ser declarada dentro do UseCase.
    - Deve conter um `companion object` com o método `fromDomain(entity)` para mapear a entidade para o output.
5. **Método Execute:** Deve conter a lógica de chamada ao repositório e o mapeamento final.
6. **DI:** Deve ser registrada no Koin para injeção de dependência.

### Exemplo de Referência:
```kotlin
class CreateServiceUseCase(
private val serviceRepository: IServiceRepository,
) : UseCase<Input, Output> {
override fun execute(input: Input) =
serviceRepository
.create(input.toDomain())
.map(Output::fromDomain)

    data class Input(
        val name: String,
        val companyId: UUID,
        // ... outros campos
    ) {
        fun toDomain() = Service(...)
    }

    data class Output(...) {
        companion object {
            fun fromDomain(service: Service): Output = Output(...)
        }
    }
}
```

