# Taskly · Agenda de empleados (Kotlin Multiplatform + Compose Multiplatform)
Autora: Nerea Monferrer Navarro · 2DAM

Abrir la carpeta con **Android Studio** (Ladybug o superior, JDK 17) y esperar al Gradle Sync.

| Plataforma | Cómo ejecutar |
|---|---|
| Android | Run ▶ con la configuración `composeApp` en emulador/móvil |
| Desktop | `./gradlew :composeApp:run` |
| Web (Wasm) | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |

Código de la interfaz: `composeApp/src/commonMain/kotlin/es/taskly/agenda/App.kt`
Contraseña válida: 8+ caracteres, 1 número y 1 símbolo (p. ej. `Taskly#2025`).
Si Android Studio no encuentra `gradlew`, usa File > Sync o genera el wrapper con `gradle wrapper --gradle-version 8.9`.
