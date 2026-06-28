# Como Configurar o WhatsApp Business API (Meta)

## Pré-requisitos

- Conta no [Meta for Developers](https://developers.facebook.com)
- Número de telefone que **não** esteja cadastrado em nenhuma conta WhatsApp
  (pessoal ou Business) — o Meta toma conta do número
- URL pública com HTTPS para o webhook (produção) ou ngrok (desenvolvimento)

---

## 1. Criar o App no Meta for Developers

1. Acesse [developers.facebook.com](https://developers.facebook.com) e faça login
2. Clique em **My Apps → Create App**
3. Selecione o tipo: **Business**
4. Preencha:
   - App name: `agendei` (ou o nome que preferir)
   - App contact email: seu email
   - Business portfolio: selecione ou crie um
5. Clique em **Create App**

---

## 2. Adicionar o Produto WhatsApp

1. No painel do app, vá em **Add Products to Your App**
2. Encontre **WhatsApp** e clique em **Set Up**
3. Você será direcionado para **WhatsApp → Getting Started**

---

## 3. Número de Telefone de Teste (Desenvolvimento)

A Meta fornece um número de teste gratuito com **1.000 conversas/mês**.

Em **WhatsApp → Getting Started**:

- **From** → número de teste fornecido pela Meta (algo como `+1 555 XXX XXXX`)
- **To** → adicione seu número pessoal como destinatário de teste

> O número de teste só pode enviar mensagens para até 5 números cadastrados
> como "Recipient phone numbers". Adicione os números da equipe aqui.

Anote:
- `Phone Number ID` (ex: `123456789012345`) — usado nas chamadas de API
- `WhatsApp Business Account ID` (WABA ID)

---

## 4. Gerar o Access Token

Em **WhatsApp → Getting Started**, há um campo **Temporary access token**.

> Este token expira em **24 horas**. Para produção, gere um token permanente
> via **System Users** (passo 4.1 abaixo).

### 4.1 Token Permanente (Produção)

1. Acesse **Business Settings → System Users**
2. Clique em **Add → System User**
   - Name: `agendei-bot`
   - Role: **Employee**
3. Clique em **Generate New Token**:
   - App: selecione o app criado
   - Permissões necessárias: `whatsapp_business_messaging`, `whatsapp_business_management`
4. Copie e salve o token — ele não é exibido novamente

---

## 5. Configurar o Webhook

O webhook recebe todos os eventos do WhatsApp (mensagens recebidas, status de entrega, etc.).

### 5.1 URL do Webhook

O endpoint já existe no projeto: `GET /api/v1/whatsapp` (verificação) e `POST /api/v1/whatsapp` (eventos).

Para desenvolvimento, exponha localmente com ngrok:

```bash
ngrok http 8080
# Anote a URL: https://xxxx.ngrok.io
```

URL do webhook: `https://xxxx.ngrok.io/api/v1/whatsapp`

### 5.2 Registrar no Painel

1. Em **WhatsApp → Configuration → Webhook**
2. Clique em **Edit**
3. Preencha:
   - **Callback URL**: `https://sua-url/api/v1/whatsapp`
   - **Verify Token**: uma string aleatória que você define (ex: `meu-token-secreto-123`)
4. Clique em **Verify and Save**

A Meta vai fazer um `GET` no seu endpoint com `hub.verify_token`. O servidor responde com `hub.challenge` se o token bater — isso já está implementado em `WhatsAppRoutes.kt`.

### 5.3 Assinar os Eventos

Após salvar, clique em **Manage** (ao lado de Webhook Fields) e ative:
- `messages` — mensagens recebidas dos clientes
- `message_deliveries` — confirmação de entrega (opcional)
- `message_reads` — confirmação de leitura (opcional)

---

## 6. Variáveis de Ambiente

Adicione ao ambiente de execução (`.env`, sistema operacional, ou secrets do CI):

```bash
# Token de acesso (gerado no passo 4)
WHATSAPP_ACCESS_TOKEN=EAAxxxxxxxxxxxxxx

# ID do número de telefone (passo 3)
WHATSAPP_PHONE_NUMBER_ID=123456789012345

# Token de verificação do webhook (passo 5.2 — você define)
WHATSAPP_VERIFY_TOKEN=meu-token-secreto-123
```

O `WHATSAPP_VERIFY_TOKEN` já é lido em `WhatsAppRoutes.kt`:
```kotlin
val verifyToken = System.getenv("WHATSAPP_VERIFY_TOKEN")
```

---

## 7. Enviar uma Mensagem de Teste

Com as variáveis configuradas, teste enviando uma mensagem via curl:

```bash
curl -X POST \
  "https://graph.facebook.com/v21.0/${WHATSAPP_PHONE_NUMBER_ID}/messages" \
  -H "Authorization: Bearer ${WHATSAPP_ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "messaging_product": "whatsapp",
    "to": "5511999999999",
    "type": "text",
    "text": { "body": "Olá! Teste do agendei." }
  }'
```

Substitua `5511999999999` pelo seu número cadastrado no passo 3.

---

## 8. Adicionar Número Real (Produção)

Quando sair do ambiente de teste:

1. Em **WhatsApp → Phone Numbers → Add Phone Number**
2. Informe o número real (precisa ser um número que você controla)
3. Verificação via SMS ou ligação
4. Aguarde aprovação da Meta (pode levar dias úteis)

> O número real substitui o de teste. Anote o novo `Phone Number ID`.

---

## 9. Aprovação do App (Produção com número real)

Para enviar mensagens a qualquer usuário (não só os cadastrados no teste):

1. Em **App Review → Permissions and Features**
2. Solicite: `whatsapp_business_messaging`
3. Preencha os casos de uso e screenshots do fluxo
4. Aguarde aprovação da Meta (geralmente 2–5 dias úteis)

Enquanto não aprovado, o app funciona apenas para os números cadastrados no passo 3.

---

## 10. Templates de Mensagem (Notificações Proativas)

Para mensagens fora da janela de 24h (notificações iniciadas pelo sistema):

1. Em **WhatsApp → Message Templates → Create Template**
2. Categorias relevantes:
   - `UTILITY` → confirmações e lembretes de agendamento
   - `MARKETING` → promoções (exige opt-in explícito)
3. Preencha o conteúdo com variáveis no formato `{{1}}`, `{{2}}`
4. Submeta para aprovação (geralmente horas a dias)

Exemplo de template `appointment_reminder`:
```
Lembrete: você tem um agendamento amanhã, {{1}}, às {{2}}, com {{3}}.
Responda *confirmar* para confirmar ou *cancelar* para cancelar.
```

---

## Referências

- [WhatsApp Business Platform Docs](https://developers.facebook.com/docs/whatsapp)
- [Cloud API Reference](https://developers.facebook.com/docs/whatsapp/cloud-api)
- [Webhook Events](https://developers.facebook.com/docs/whatsapp/cloud-api/webhooks)
- [Message Templates](https://developers.facebook.com/docs/whatsapp/message-templates)
- [Rate Limits](https://developers.facebook.com/docs/whatsapp/cloud-api/overview/rate-limits)
