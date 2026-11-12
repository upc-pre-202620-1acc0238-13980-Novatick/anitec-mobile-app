# ANITEC · NOVATICK

Aplicación Android para la gestión ganadera y la coordinación con veterinarios. El segmento **GANADERO** administra fincas, animales y observaciones; el segmento **VETERINARIO** consulta animales autorizados, gestiona vínculos e invitaciones y programa visitas en su agenda.

Esta entrega académica TB2 incluye las Fases 1–5. Es una versión de evaluación contra staging, no una distribución productiva. Requiere conexión a Internet y una clave de acceso entregada **privadamente** por el Team Leader, además de una cuenta ANITEC.

## Inicio rápido para evaluadores

GitHub conserva `main` como rama predeterminada. **Clonar main no incluye necesariamente esta entrega.** Mientras esta PR esté pendiente, usar `feature/kotlin-tb2-evaluation`; tras su integración aprobada, usar `develop`.

```sh
git clone --branch feature/kotlin-tb2-evaluation https://github.com/upc-pre-202620-1acc0238-13980-Novatick/anitec-mobile-app-kotlin.git
cd anitec-mobile-app-kotlin
```

1. Abrir la carpeta raíz en Android Studio, confiar en el proyecto y esperar la sincronización Gradle.
2. En SDK Manager, instalar la plataforma **Android API 37**, Build Tools **36.0.0**, Platform Tools y una imagen de emulador compatible. Aceptar las licencias del SDK. Android Studio configura `local.properties` localmente; no compartir ese archivo.
3. Configurar el **Gradle JDK 21** en los ajustes de Gradle. Utilizar una versión de Android Studio compatible con **AGP 9.4.1**; no cambiar las versiones del proyecto para sortear un IDE antiguo.
4. Crear o seleccionar un dispositivo Android **API 24 o superior** en Device Manager. La QA de referencia utiliza Pixel 4a **API 34**. Encenderlo manualmente.
5. En **Build → Select Build Variant** (o la ventana **Build Variants**), comprobar que `app` utiliza **alpha**. Seleccionar el módulo/configuración Android App `app`, elegir dispositivo y pulsar **Run**. Si no existe una configuración de ejecución, crear una Android App para el módulo `app` con lanzamiento de la actividad predeterminada.
6. En **Acceso privado de staging**, introducir la clave recibida por un canal privado y pulsar **Validar y continuar**. Después registrarse/verificar el correo o iniciar sesión con la cuenta autorizada.

**No se necesita Docker, PostgreSQL local ni ejecutar el backend para evaluar alpha.** El servidor es `https://anitec-staging.onrender.com/api/v1/`.

### Variante predeterminada y Run

`app/build.gradle.kts` declara `alpha.isDefault = true`; `debug` y `labAlpha` no son predeterminadas. Esta es la preferencia del modelo Gradle para una importación nueva, mediante la [API oficial de AGP](https://developer.android.com/reference/tools/gradle-api/9.4/com/android/build/api/dsl/ApplicationBuildType#isDefault()). Android Studio puede conservar una selección previa por equipo: **comprobar alpha antes de Run**, especialmente al abrir una copia ya utilizada. No se versionan preferencias del IDE para simular una selección compartida.

## Modos de ejecución

| Variante exacta | Nombre instalado | API y uso |
|---|---|---|
| `alpha` | ANITEC TB2 | Render HTTPS; evaluación remota; formulario privado de staging; sin banda Alpha ni fixtures |
| `debug` | ANITEC Dev local | `http://10.0.2.2:18080/api/v1/`; desarrollo local y QA con fixtures exclusivos de debug |
| `labAlpha` | ANITEC Lab | Misma API local del emulador; integración con laboratorio real, sin pantallas de demostración |
| `release` | AniTec | Render HTTPS; variante técnica de release, **no la entrega TB2**: no contiene el formulario/cabecera privados de alpha y no resuelve el acceso al staging protegido |

Seleccionar cada variante en Build Variants antes de Run. Sus paquetes son respectivamente `pe.novatick.anitec.alpha.linking`, `pe.novatick.anitec`, `pe.novatick.anitec.alpha.lab` y `pe.novatick.anitec`. Debug y release comparten paquete: una instalación puede reemplazar a la otra si sus firmas son compatibles. Alpha conserva su identificador histórico para actualizar sin borrar sesiones existentes.

El modo local requiere el backend aislado previamente preparado y su PostgreSQL; este repositorio no aprovisiona el laboratorio ni contiene sus claves/scripts privados. Solicitar al responsable su configuración y procedimiento de arranque. `10.0.2.2` corresponde al host del emulador Android; no es una dirección válida del ordenador desde un teléfono físico. No apuntar bases ni scripts locales a producción.

**QA local** utiliza repositorios reiniciables en `src/debug`: sus datos son ficticios, no prueban persistencia remota y no forman parte de alpha, labAlpha ni release. Las pruebas instrumentadas están en `src/androidTest`. Las pruebas de laboratorio que escriben datos requieren autorización expresa; no activarlas como comprobación rutinaria.

## Acceso y autenticación

- La clave staging autoriza el acceso al entorno; **no sustituye login ni JWT**. Se introduce exclusivamente en el formulario privado, se valida contra el servidor y permanece solo en memoria. Tras finalizar el proceso debe introducirse nuevamente.
- Para volver al formulario, cambiar/quitar la clave o actualizar la sesión, usar **Mi cuenta → Acceso privado de staging**. No hay banda superior sobre las pantallas funcionales.
- El registro permite GANADERO o VETERINARIO. Usar un correo propio/controlado, completar los campos y verificar los seis dígitos recibidos. El envío real depende del backend/servicio de correo; los fixtures no envían mensajes.
- Las contraseñas nuevas conservan las reglas de complejidad y tienen un máximo de **72 bytes UTF-8**, sin truncado ni normalización. No equivale a 72 caracteres. Esta regla no restringe el login de cuentas existentes.
- Login, restauración/renovación de sesión y logout utilizan la infraestructura JWT existente. La interfaz separa cuentas y roles. Cerrar sesión desde Mi cuenta; no compartir sesiones de otra persona para evaluar.
- Nunca introducir claves, contraseñas, JWT u OTP en código, BuildConfig, Gradle, README, Git, incidencias, capturas o logs. No usar la clave de Resend como clave staging.

## Funcionalidades disponibles y límites

| Segmento | Implementado |
|---|---|
| Ambos | Registro, verificación de correo, reenvío según restricciones del servidor, login/logout y sesiones |
| GANADERO | Fincas, catálogo de especies, inventario/filtros/capacidad, registro/ficha/edición/baja lógica de animales, observaciones y vinculación veterinaria |
| VETERINARIO | Invitaciones/vínculos autorizados, ganaderos vinculados, inventario/ficha y observaciones en lectura, programación desde Inicio o animal, agenda por fecha y detalle |

La agenda convierte fecha/hora local a UTC y muestra únicamente datos reales autorizados. Después de un envío incierto se debe consultar antes de intentar otra creación; no repetir Guardar suponiendo que un timeout significa que no se guardó.

**Pendiente para TF:** registrar atenciones, tratamientos, vacunas, historial clínico completo y suscripciones. Los accesos señalados Próximamente no ejecutan operaciones simuladas. No existe edición PUT/PATCH de observaciones ni agenda GANADERO respaldada por el contrato actual. Fotografías: URL existente o placeholder, sin subida de archivos. Recuperación de contraseña y opciones de perfil sin contrato operativo no deben considerarse flujos terminados.

## Tecnologías y versiones del repositorio

| Componente | Versión/configuración |
|---|---|
| Kotlin / plugin Compose | 2.2.10 |
| Android Gradle Plugin | 9.4.1 |
| Gradle Wrapper | 9.6.0; usar el wrapper, no un Gradle global |
| JDK de compilación validado | Temurin 21.0.13; bytecode Java 11 |
| Android | compileSdk/targetSdk 37; minSdk 24 |
| Compose | BOM 2026.02.01, Material3, tipografía Poppins |
| Navegación | Navigation Compose 2.10.2 |
| HTTP/JSON | Retrofit/converter Gson 3.0.0; OkHttp 4.12.0 |
| Concurrencia/estado | Coroutines 1.10.2, StateFlow, ViewModels y DI manual existente |
| Imágenes / pruebas | Coil 2.7.0; JUnit4, MockWebServer y Compose UI Test |

Las versiones fuente están en `gradle/libs.versions.toml`, `app/build.gradle.kts` y `gradle/wrapper/gradle-wrapper.properties`. No actualizar dependencias como parte de una evaluación.

## Compilación e instalación

Desde PowerShell, en la raíz, con SDK y JDK configurados:

```powershell
.\gradlew.bat :app:assembleAlpha
.\gradlew.bat :app:assembleLabAlpha
```

En macOS/Linux usar `./gradlew` en lugar de `.\gradlew.bat`. Los APK normales se generan en `app/build/outputs/apk/alpha/app-alpha.apk` y `app/build/outputs/apk/labAlpha/app-labAlpha.apk`. No se versionan ni se necesitan copias adicionales para Run.

Con Platform Tools en PATH y el dispositivo autorizado:

```powershell
adb devices
adb install -r app/build/outputs/apk/alpha/app-alpha.apk
adb shell am start -n pe.novatick.anitec.alpha.linking/pe.novatick.anitec.MainActivity
```

La evaluación alpha está firmada para desarrollo, no para publicación en tiendas. Si otro equipo firma con una clave debug distinta, la actualización puede fallar por firma incompatible: **no desinstalar ni borrar datos sin comprobar qué sesiones se perderían**. Para revisión de código, compilar/Run en un emulador propio evita afectar el dispositivo de otra persona.

Ejemplo de pruebas focalizadas del acceso remoto, sin credenciales operativas ni escrituras remotas:

```powershell
.\gradlew.bat :app:testAlphaUnitTest --tests pe.novatick.anitec.core.environment.StagingHeaderTest
```

Las evidencias previas de Fases 4/5 incluyen JVM/Compose y laboratorio real. No equivalen a una nueva ejecución de registro/OTP o escritura contra Render por cada clonación. La evaluación de datos remotos debe realizarse con cuentas y operaciones autorizadas.

## Solución de problemas

| Síntoma | Acción |
|---|---|
| Render tarda o aparece un timeout | Comprobar Internet, esperar y reintentar una lectura explícitamente. No repetir escrituras a ciegas. Un fallo temporal de lectura no significa logout |
| No se valida la clave / 403 staging | Revisar entorno, conexión y clave privada con el Team Leader. No modificar el filtro ni insertar secretos en la compilación |
| HTTP 401/403 en datos | Diferenciar sesión expirada de falta de autorización o vínculo; volver a iniciar sesión cuando corresponda. No intentar consultar recursos de otra cuenta |
| HTTP 400/409 u otro error | Revisar el mensaje y datos del formulario. No simular éxito ni ocultar el error |
| OTP no llega | Comprobar correo/spam, tiempos y límites de reenvío; consultar al responsable. No activar Resend ni crear cuentas repetidas para probar |
| Gradle no sincroniza | Comprobar JDK 21, Studio compatible, SDK API37/licencias y acceso a repositorios; usar el wrapper. No versionar rutas del SDK |
| Memoria insuficiente al compilar | Ajustar memoria/parallelismo localmente según el equipo; evitar varios builds concurrentes. No cambiar dependencias |
| Emulador no detectado | Encenderlo manualmente en Device Manager; comprobar `adb devices` y autorización USB en dispositivo físico |
| Se intenta conectar a 10.0.2.2 | Está seleccionada debug/labAlpha. Para evaluación remota seleccionar alpha |
| Backend local apagado | Es normal que debug/labAlpha fallen sin laboratorio. Solicitar su arranque controlado o seleccionar alpha para Render |

## Estructura real

```text
app/src/main/java/pe/novatick/anitec/
  MainActivity.kt y pantallas heredadas en el paquete raíz
  core/       DI, red, almacenamiento seguro y sesiones
  features/   auth, livestock y linking (dominio, infraestructura, presentación)
  data/       código de datos existente
  ui/         componentes, pantallas y tema
app/src/main/res/       recursos, idiomas ES/EN y vectores oficiales
app/src/alpha/          acceso privado a staging y nombre de evaluación
app/src/labAlpha/       configuración aislada de laboratorio
app/src/debug/          desarrollo y fixtures de revisión local
app/src/release/        configuración de release
app/src/test/           pruebas JVM compartidas
app/src/testAlpha/      pruebas JVM del acceso staging
app/src/androidTest/    pruebas Android/Compose
```

La estructura se conserva; no mover pantallas ni reorganizar paquetes para ejecutar la app. Los repositorios reutilizan Retrofit/AuthorizedRequests y la gestión de sesión existente.

## Colaboración y seguridad

- `main`: versiones estables aprobadas. `develop`: integración. Nuevas ramas `feature/...` parten de develop actualizado.
- Publicar cambios acotados mediante PR; describir comportamiento, pruebas realmente ejecutadas y limitaciones. Las PR Draft no son aprobación de merge.
- No realizar force push ni omitir protecciones/revisiones. No mezclar tareas o cambios locales del IDE con código funcional.
- No versionar `local.properties`, claves, sesiones, APK, capturas, informes locales ni `AGENTS.md`. No copiar secretos a ejemplos de documentación.
- El backend y la infraestructura requieren su propia autorización; esta rama no cambia Render, PostgreSQL, Docker ni Resend.

Antes de distribuir la copia académica definitiva se requiere aprobar e integrar esta PR y validar la rama indicada para evaluadores. Permanecen los riesgos previos documentados: ANR residual al cambiar idioma, ausencia de precondición atómica de versión en DELETE y riesgos de seguridad pendientes del backend. Esta preparación no los declara resueltos ni habilita Fase 6.
