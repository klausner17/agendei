# Deploy automático (CI/CD)

Este projeto faz **build/teste em todo push e PR** e **deploy automático na VPS ao mergear na `main`**, via GitHub Actions.

## Visão geral dos workflows

| Workflow | Arquivo | Quando roda | O que faz |
|----------|---------|-------------|-----------|
| CI | `.github/workflows/ci.yaml` | push e PR em qualquer branch | `ktlintCheck` + `test` |
| Deploy | `.github/workflows/deploy.yaml` | push na `main` (inclui merge de PR) | lint + testes (gate) → `shadowJar` → `scp` do JAR pra VPS → `./deploy.sh` → health check |

O job de deploy roda lint e testes antes de enviar qualquer coisa: **se falharem, o deploy é abortado**.

---

## Passos manuais

A pipeline depende de configuração que **não** fica no repositório. Faça uma vez.

### 1. Variáveis e secret no GitHub

Em **Settings → Secrets and variables → Actions** do repositório:

**Secrets** (aba *Secrets*):

| Nome | Valor |
|------|-------|
| `SSH_PRIVATE_KEY` | Chave **privada** SSH (conteúdo completo, com `-----BEGIN ... KEY-----`) usada para acessar a VPS |

**Variables** (aba *Variables*):

| Nome | Exemplo | Descrição |
|------|---------|-----------|
| `VPS_HOST` | `203.0.113.10` ou `app.seudominio.com` | Host/IP da VPS |
| `VPS_USER` | `deploy` | Usuário SSH usado no deploy |
| `VPS_TARGET_DIR` | `/opt/agendei` | Diretório na VPS onde o `app.jar` e o `deploy.sh` ficam |

> O host é validado via `ssh-keyscan` a cada execução (TOFU). Se a VPS trocar de chave de host, o passo de SSH vai falhar até a nova chave ser aceita.

### 2. Chave SSH

No seu computador (ou onde preferir gerar):

```bash
ssh-keygen -t ed25519 -C "github-actions-deploy" -f deploy_key
```

- Cole o conteúdo de `deploy_key` (privada) no secret `SSH_PRIVATE_KEY`.
- Adicione o conteúdo de `deploy_key.pub` (pública) na VPS, em `~/.ssh/authorized_keys` do usuário `VPS_USER`:

```bash
# na VPS, como VPS_USER
mkdir -p ~/.ssh && chmod 700 ~/.ssh
echo "<conteúdo de deploy_key.pub>" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

Teste localmente: `ssh -i deploy_key VPS_USER@VPS_HOST` deve logar sem senha.

---

## Contrato esperado na VPS

A VPS já está configurada e funcionando. Esta seção documenta o que a pipeline **assume** — use para conferir/recriar se necessário.

### O que a pipeline faz na VPS

1. `scp` do JAR para `${VPS_TARGET_DIR}/app.jar`.
2. `ssh ... "cd ${VPS_TARGET_DIR} && ./deploy.sh"` — espera-se que o `deploy.sh` reinicie a aplicação com o novo JAR.
3. Health check: `curl http://localhost:8080/` na VPS, esperando qualquer resposta HTTP.

### Pré-requisitos na VPS

- **Java 17** (Temurin/OpenJDK) instalado e no `PATH`.
- Diretório `${VPS_TARGET_DIR}` (ex.: `/opt/agendei`) com permissão de escrita pelo `VPS_USER`.
- `curl` instalado (para o health check).
- A aplicação escuta na **porta 8080** (definida em `Main.kt`).

### `deploy.sh` (referência)

Script que mora em `${VPS_TARGET_DIR}/deploy.sh` na VPS. Exemplo equivalente ao esperado:

```bash
#!/usr/bin/env bash
set -euo pipefail

# Reinicia o serviço systemd que executa o app.jar recém-enviado.
sudo systemctl restart agendei
```

> Se o `VPS_USER` reinicia o serviço via `sudo`, garanta uma regra `sudoers` sem senha apenas para esse comando, ex.:
> `deploy ALL=(root) NOPASSWD: /usr/bin/systemctl restart agendei`

### Serviço systemd (referência)

`/etc/systemd/system/agendei.service`:

```ini
[Unit]
Description=Agendei API
After=network.target

[Service]
Type=simple
User=deploy
WorkingDirectory=/opt/agendei
# Mantém segredos/config fora do repositório:
Environment=JWT_SECRET=troque-por-um-segredo-forte
ExecStart=/usr/bin/java -jar /opt/agendei/app.jar
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Ativar:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now agendei
```

### Pontos de atenção

- **`JWT_SECRET`**: defina via `Environment=` no systemd (como acima). Sem isso, o app usa o segredo default inseguro do código. **Não** coloque o `JWT_SECRET` no GitHub — o build não precisa dele; ele é só de runtime na VPS.
- **Banco de dados (`data.db`)**: o app usa SQLite gravando `./data.db` relativo ao `WorkingDirectory`. O deploy só substitui o `app.jar`, então o `data.db` **persiste** entre deploys — desde que o `WorkingDirectory` do serviço seja sempre o mesmo (`${VPS_TARGET_DIR}`). Faça backup periódico desse arquivo.

---

## Melhoria recomendada (opcional)

O health check hoje aceita qualquer resposta HTTP como "app no ar". Um endpoint dedicado `GET /health` (público, retornando `200`) deixaria o check mais preciso e permitiria trocar o `curl` por `curl -fsS http://localhost:8080/health`. Não é obrigatório.
