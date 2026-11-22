# Anki Inglês — Android nativo

**Criado por [Eder Oliveira](mailto:ederproducer@gmail.com)** ·
[ederproducer@gmail.com](mailto:ederproducer@gmail.com)

Refação do zero do Anki Inglês **somente para smartphone**, em **Kotlin + Jetpack Compose**.
Não é um WebView: é um app Android nativo, com a mesma proposta do app do laptop —
frases em inglês com voz americana, tradução em português e repetição espaçada.

Este projeto é separado e não modifica `anki-ingles` nem `anki-ingles-android`.

## O que tem (essencial bem feito)

- **Revisar:** cartão com animação de virada, voz automática em `en-US`, botão 🔊,
  "Mostrar resposta" e notas **De novo / Difícil / Bom / Fácil** com o intervalo
  calculado pelo SM-2 simplificado (mesmo algoritmo do webapp).
- **Cartões:** buscar, adicionar, editar, excluir e importar (`inglês :: português`
  ou um backup JSON).
- **Ajustes:** backup automático, voz automática, velocidade da voz, **Salvar agora**
  e **Restaurar**, configuração da voz do Android e rodapé de autoria.

Não há WebView, gerador de ZIP, gráficos, temas do Omarchy nem múltiplos decks.

## Backup

O app define sozinho a pasta:

```
Android/data/com.ederproducer.ankiingles.mobile/files/Anki/
└── anki-ingles-backup.json
```

O formato é o mesmo do webapp (`cards`, `settings`, `stats`), então um backup
antigo pode ser colado em **Ajustes → Restaurar**. **Nenhum MP3 é gravado**: a voz
usa o mecanismo de síntese de fala do Android, sem depender da rede.

Se o aparelho não tiver uma voz em inglês instalada, o app mostra o aviso e
oferece um atalho para as configurações de TTS do sistema.

## Como gerar o APK

O Android SDK fica no Docker, então não é preciso instalá-lo na máquina:

```bash
ANDROID_BUILD_IMAGE=anki-ingles-android-builder bash scripts/build-apk.sh
```

O APK sai em `dist/anki-ingles-mobile.apk`. Para instalar:

```bash
adb install -r dist/anki-ingles-mobile.apk
```

`scripts/build-apk.sh` usa a imagem `anki-ingles-android-builder` (JDK 21 +
Android SDK 36), construída pelo projeto `anki-ingles-android`. Se quiser criar
a imagem do zero:

```bash
cd ../anki-ingles-android && docker build -t anki-ingles-android-builder .
```

## Testando no emulador

O emulador headless do projeto Android serve para este app também:

```bash
docker exec anki-emu adb install -r /dist/anki-ingles-mobile.apk
docker exec anki-emu adb exec-out screencap -p > /tmp/screen.png
```

(Monte esta pasta em `/dist` no container, ou use `docker cp`.)

## Estrutura

```
app/src/main/java/.../model/     cartões, SRS e frases iniciais
app/src/main/java/.../data/      backup em JSON
app/src/main/java/.../tts/       voz nativa do Android
app/src/main/java/.../ui/        Compose: tema, revisão, cartões, ajustes
scripts/build-apk.sh             build via Docker
```

## Escopo

Não edite `~/Work/anki-ingles`, `~/Work/anki-ingles-android` nem configurações do
Omarchy a partir deste projeto. Toda mudança fica aqui.
