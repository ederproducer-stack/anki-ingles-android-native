#!/usr/bin/env bash
# Build nativo em Kotlin/Compose usando o Android SDK do container.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IMAGE="${ANDROID_BUILD_IMAGE:-anki-ingles-android-builder}"
OUT="$ROOT/dist"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"

mkdir -p "$OUT"

# O SDK da imagem é somente-leitura para usuários normais; o build roda como root
# no container e os diretórios gerados são limpos no final.
docker run --rm \
  -e HOME=/tmp \
  -e GRADLE_USER_HOME=/tmp/gradle-home \
  -e ANDROID_USER_HOME=/tmp/android-user-home \
  -v anki-native-gradle-cache:/tmp/gradle-home \
  -v anki-native-android-cache:/tmp/android-user-home \
  -v "$ROOT:/workspace" \
  -w /workspace \
  "$IMAGE" \
  bash -lc '
    unset ANDROID_SDK_HOME
    export ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
    for candidate in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-21-openjdk /opt/java/openjdk; do
      if [ -x "$candidate/bin/java" ]; then JAVA_HOME="$candidate"; break; fi
    done
    export JAVA_HOME
    export PATH="$JAVA_HOME/bin:$PATH"
    java -version
    ./gradlew assembleDebug --no-daemon --stacktrace
  '

if [ ! -f "$APK" ]; then
  echo "APK não encontrado em $APK" >&2
  exit 1
fi

cp "$APK" "$OUT/anki-ingles-mobile.apk"

# Remove artefatos root-owned para o próximo build não travar por permissão.
docker run --rm -v "$ROOT:/workspace" -w /workspace "$IMAGE" \
  bash -c 'rm -rf build app/build .gradle'

echo "==> APK: $OUT/anki-ingles-mobile.apk"
