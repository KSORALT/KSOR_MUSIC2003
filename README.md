# KSOR_MUSIC2003 📼
Reproductor Android local, estética Y2K. Código fuente Java + Android Gradle Plugin.

## Funciones en esta versión 0.1
- Escanea audio local mediante MediaStore con permisos Android 8–15.
- Lista título y artista, reproduce, pausa, cambia de canción y permite buscar posición.
- No usa internet para reproducir música.

## Compilar SIN computadora, desde Android
1. Crea una cuenta gratuita en https://github.com (si no tienes).
2. Crea un repositorio nuevo llamado `KSOR_MUSIC2003`.
3. Sube **el contenido de esta carpeta** al repositorio, conservando rutas como `.github/workflows/build.yml`. Desde el navegador del teléfono puedes usar la interfaz web de GitHub o una app cliente Git.
4. En GitHub abre **Actions** > **Build Android APK** > **Run workflow** (o espera la ejecución automática tras subir a `main`).
5. Cuando termine correctamente, abre la ejecución y descarga el artefacto `KSOR_MUSIC2003-debug-apk`.
6. Descomprime el ZIP del artefacto, abre `app-debug.apk` en tu Android y autoriza la instalación desde esa fuente si el sistema lo solicita.
7. Abre KSOR_MUSIC2003 y concede acceso a audio.

**Nota:** el ZIP de este proyecto es código fuente, no un APK. El APK se genera al ejecutar GitHub Actions. No se ha compilado ni probado aquí en un teléfono real.

## Limitaciones conocidas
- La reproducción usa MediaPlayer dentro de la Activity: puede detenerse cuando Android destruye la pantalla o proceso. No hay servicio multimedia de segundo plano, controles en notificaciones, playlists, aleatorio, repetición ni portadas todavía.
- Esta es una primera versión funcional, no una app final de producción.
- La compilación puede requerir ajustes según actualizaciones de Android/Gradle.
- El APK debug es para pruebas personales; para distribución se necesita firma de release.
