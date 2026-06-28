# Spec: Fluxo de Cliente via WhatsApp

## Visão Geral

O cliente final interage com o sistema exclusivamente via WhatsApp. O número de telefone
do WhatsApp é a identidade do cliente — não há senha, cadastro prévio ou login separado.

Quando uma mensagem chega pelo webhook, o sistema:
1. Extrai o número `from` da mensagem
2. Busca ou cria um `Customer` com esse número
3. Recupera o estado da conversa desse número
4. Processa o input e avança o estado
5. Responde via API do Meta

---

## Modelo de Identidade

| Campo | Fonte | Observação |
|-------|-------|------------|
| `phone.countryCode` | prefixo do `from` (ex: `55`) | extraído do número E.164 |
| `phone.areaCode` | próximos 2 dígitos | ex: `11` |
| `phone.number` | resto | ex: `999999999` |
| `phone.isWhatsApp` | sempre `true` | origem é o WhatsApp |

O campo `from` chega em formato E.164 sem `+`: `5511999999999`.

Se o `Customer` não existir para esse número → cria automaticamente na primeira mensagem.
Nome inicial: número formatado. Pode ser atualizado pelo cliente durante o fluxo.

---

## Gerenciamento de Estado

Cada conversa tem um estado persistido na tabela `conversation_states`:

```
conversation_states
  from_number  VARCHAR(20)  PK
  step         VARCHAR(50)       -- estado atual do cliente
  context      TEXT              -- JSON com dados parciais (ex: professionalId escolhido)
  updated_at   DATETIME
```

O estado expira após 30 minutos de inatividade — o cliente volta ao `MENU`.

---

## Máquina de Estados

```
                    ┌─────────────────────────────────┐
                    │                                 │
             ┌──────▼──────┐                         │
   [início]  │    MENU     │◄────────────────────────┤
  ──────────►│             │                    (qualquer /menu)
             └──┬──┬──┬───┘
                │  │  │
           "1"  │  │  │ "3"
                │  │"2"│
                │  │   └─────────────────────────────────────┐
                │  │                                         │
         ┌──────▼──┴──────┐                    ┌────────────▼──────────┐
         │ LIST_SCHEDULES │                    │  CANCEL_LIST          │
         │                │                    │                       │
         │ mostra lista   │                    │ mostra agendamentos   │
         │ ou "nenhum"    │                    │ ativos numerados      │
         └──────┬─────────┘                    └────────────┬──────────┘
                │                                           │ "N" (número)
                │ [volta ao menu]               ┌───────────▼──────────┐
                │                               │  CANCEL_CONFIRM      │
                │                               │                      │
                │                               │ "Cancelar [X]? 1/2"  │
                │                               └───────────┬──────────┘
                │                                           │
                │                               "1" ────────┘ (cancela e volta)
                │                               "2" ────────► (volta ao menu)
                │
         ┌──────▼──────────────────────────────────────────┐
         │              BOOKING FLOW                        │
         │                                                  │
         │  BOOK_PROFESSIONAL → BOOK_DATE → BOOK_SLOT      │
         │       │                  │            │          │
         │  lista       "dd/mm/aa"      lista    │          │
         │  profissionais              horários  │          │
         │                                       │          │
         │                               BOOK_CONFIRM       │
         │                              "Confirmar? 1/2"    │
         └──────────────────────────────────────────────────┘
```

---

## Descrição dos Estados

### `MENU`

Mensagem de boas-vindas com opções:

```
Olá! O que você deseja fazer?

1️⃣ Ver meus agendamentos
2️⃣ Fazer um agendamento
3️⃣ Cancelar um agendamento

Responda com o número da opção.
```

Gatilho: primeira mensagem, timeout de 30 min, ou cliente digita `/menu`.

---

### `LIST_SCHEDULES`

Busca `schedules` pelo `customerId`. Exibe:

```
Seus agendamentos:

📅 01/07/2026 às 09:00 — Dr. Ana Lima
   Status: Confirmado

📅 08/07/2026 às 10:30 — Dr. Ana Lima
   Status: Confirmado

Responda /menu para voltar.
```

Se não houver agendamentos:
```
Você não tem agendamentos futuros.

Responda /menu para voltar.
```

---

### `BOOK_PROFESSIONAL`

```
Com qual profissional você quer agendar?

1. Dr. Ana Lima
2. Carlos Souza

Responda com o número.
```

Salva `professionalId` no `context`. Avança para `BOOK_DATE`.

---

### `BOOK_DATE`

```
Qual data você prefere?
Responda no formato DD/MM/AAAA.

Exemplo: 15/07/2026
```

Valida que a data é futura. Salva `date` no `context`. Avança para `BOOK_SLOT`.

---

### `BOOK_SLOT`

Busca slots disponíveis (`status = AVAILABLE`) para o profissional na data informada.

```
Horários disponíveis para 15/07/2026:

1. 09:00 — 10:00
2. 14:00 — 15:00
3. 16:00 — 17:00

Responda com o número.
```

Se não houver slots:
```
Não há horários disponíveis nessa data.
Tente outra data (DD/MM/AAAA) ou responda /menu.
```

Salva `slotId` no `context`. Avança para `BOOK_CONFIRM`.

---

### `BOOK_CONFIRM`

```
Confirmar agendamento?

👨‍⚕️ Dr. Ana Lima
📅 15/07/2026 às 09:00

1️⃣ Sim, confirmar
2️⃣ Não, cancelar

Responda com 1 ou 2.
```

"1" → cria agendamento → confirma e volta ao `MENU`
"2" → descarta e volta ao `MENU`

Mensagem de confirmação:
```
✅ Agendamento confirmado!

Dr. Ana Lima
15/07/2026 às 09:00

Até logo!
```

---

### `CANCEL_LIST`

```
Selecione o agendamento para cancelar:

1. 01/07/2026 às 09:00 — Dr. Ana Lima
2. 08/07/2026 às 10:30 — Dr. Ana Lima

Responda com o número ou /menu para voltar.
```

Salva `scheduleId` no `context`. Avança para `CANCEL_CONFIRM`.

---

### `CANCEL_CONFIRM`

```
Cancelar este agendamento?

📅 01/07/2026 às 09:00 — Dr. Ana Lima

1️⃣ Sim, cancelar
2️⃣ Não, manter

Responda com 1 ou 2.
```

"1" → atualiza `schedule.status = CANCELLED` → volta ao `MENU`

---

## Tratamento de Erros

| Situação | Resposta |
|----------|----------|
| Input inválido (opção fora do range) | "Opção inválida. " + repete a pergunta atual |
| Data no passado | "Informe uma data futura." + repete `BOOK_DATE` |
| Data em formato errado | "Formato inválido. Use DD/MM/AAAA." |
| Timeout (30 min) | Volta ao `MENU` na próxima mensagem, sem aviso |
| Erro interno | "Desculpe, ocorreu um erro. Tente novamente ou responda /menu." |

---

## Notificações Proativas (Templates)

Templates precisam de aprovação prévia da Meta. Casos de uso:

| Template | Gatilho | Exemplo |
|----------|---------|---------|
| `appointment_reminder` | 24h antes do agendamento | "Lembrete: amanhã às 09:00 com Dr. Ana" |
| `appointment_confirmed` | Ao criar agendamento (pelo profissional) | "Seu agendamento foi confirmado" |
| `appointment_cancelled` | Ao cancelar (pelo profissional) | "Seu agendamento foi cancelado" |

Templates são enviados apenas para clientes que iniciaram conversa nos últimos 24h
(janela de serviço) ou via aprovação da Meta (fora da janela).

---

## Escopo da Implementação (MVP)

**Incluído:**
- Identificação automática pelo número do WhatsApp
- Ver meus agendamentos
- Cancelar agendamento

**Fora do MVP:**
- Fluxo de novo agendamento (depende de slots disponíveis e seleção de profissional)
- Templates proativos (requer aprovação Meta + número verificado)
- Alteração de nome do cliente
