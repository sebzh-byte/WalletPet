# WalletPet 🐾

App de ahorro para estudiantes con una mascota virtual (un gatito). Creas metas de ahorro
(ej. "Audífonos"), registras lo que vas guardando y tu gatito reacciona: si ahorras seguido
y lo alimentas, está feliz; si lo descuidas, le da hambre y se pone triste.

Proyecto integrador · Desarrollo de Aplicaciones Móviles · Universidad Politécnica de Chiapas.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje / UI | Kotlin, Jetpack Compose, Material 3 |
| Arquitectura | MVVM (Screen → ViewModel → UseCase/Repository → Room / Retrofit) |
| Estado | `StateFlow` + `collectAsStateWithLifecycle` |
| Navegación | Navigation Compose (rutas en `navigation/Routes.kt`) |
| Persistencia local | Room (5 tablas) |
| API externa | Retrofit + Gson → https://open.er-api.com (tipo de cambio, sin clave) |
| Dependencias | Contenedor manual `di/AppContainer.kt` |
| Integraciones | Audio (MediaPlayer), PDF (PdfDocument + FileProvider), Biometría (BiometricPrompt) |
| Imágenes / GIF | Coil |

## Requisitos

- Android Studio reciente (con soporte para AGP 9.3) y JDK 17 o superior
- Emulador o teléfono con Android 7.0 (API 24) o superior
- Internet para la conversión de moneda (lo demás funciona sin red)

## Cómo ejecutar

1. Abrir la carpeta `WalletPet` en Android Studio.
2. Esperar el *Gradle Sync* (descarga las librerías la primera vez).
3. Elegir un emulador o teléfono y presionar **Run ▶**.
4. Para el APK: **Build → Generate App Bundles or APKs → Generate APKs**. Queda en
   `app/build/outputs/apk/debug/app-debug.apk`.

## Funcionalidades

- **Inicio:** el gato (grande) con su globo de texto según su humor. Arriba, un carrusel con
  tus metas: deslizas y registras ahorro en la meta que estás viendo. Si tocas al gato se
  abre su panel: barras de **hambre** y **ánimo**, porciones de croquetas y el botón
  **Alimentar**.
- **Croquetas:** se compran en la tienda (100 monedas) y cada bolsa trae **6 porciones**.
  Darle de comer gasta una porción, no monedas. Si ya está lleno o no hay croquetas, la app
  avisa y te lleva a la tienda.
- **Tienda:** objetos para el gato, bloqueados por nivel. Las monedas se ganan ahorrando
  (1 moneda por cada $10) y cada 10 ahorros subes de nivel.
- **Metas:** carrusel de metas (no se desborda aunque tengas muchas). Puedes crear, registrar
  ahorro y eliminar. Muestra tu ahorro total convertido a **USD o EUR con la API** (con
  estados de carga, sin datos, error y botón **Reintentar**).
- **Gastos:** registrar gastos con nombre y monto (categoría opcional) e ingresos. "Mes" muestra el resumen por categoría y "Todo" cada gasto con su nombre, fecha y opción de borrarlo.
- **Reportes:** uno por mes, generado con los datos guardados. Se descarga como **PDF**
  real, se puede abrir o compartir.
- **Huella / PIN** al abrir la app.

### Humor del gato

Las reglas están en `domain/model/ReglasMascota.kt`:

- La pancita se vacía sola en 48 horas; cada porción de croquetas la llena 35 %.
- El cumplimiento de metas baja si pasan días sin ahorrar (a los 7 días llega a 0).
- **Hambre** si la pancita está casi vacía, **triste** si no estás ahorrando,
  **feliz** si las dos cosas van bien.

Para la demostración, cambiar `VELOCIDAD = 60.0` (una hora pasa en un minuto) y regresarlo
a `1.0` antes de entregar.

Los GIFs del gato se conectan en `core/ui/components/PetView.kt`: se ponen los archivos en
`res/drawable` y se cambia solo el `when` de `imagen()`.

## Estructura MVVM

```
com.example.walletpet
├── MainActivity.kt            Actividad (FragmentActivity por la huella)
├── WalletPetApplication.kt    Inicia AppContainer y Coil (GIFs)
├── navigation/                Routes.kt (rutas) y AppNavGraph.kt (NavHost + barra inferior)
├── di/AppContainer.kt         Crea Room, Retrofit, repositorios y módulos del dispositivo
├── core/
│   ├── common/                UiState (carga/éxito/vacío/error) y reloj
│   └── ui/                    theme/ (colores del Figma) y components/ (botones, tarjetas, PetView)
├── domain/
│   ├── model/                 Meta, Mascota, Producto, reglas del gato, validaciones
│   ├── repository/            Contratos (interfaces) de los repositorios
│   └── usecase/               RegistrarAhorroUseCase (ahorro + monedas + nivel)
├── data/
│   ├── local/                 Room: entidades, DAOs y AppDatabase
│   ├── remote/api, dto/       Retrofit: ApiService, RetrofitClient, TipoCambioDto
│   └── repository/            Implementaciones que deciden qué fuente usar
├── integration/               audio/, pdf/, biometria/
└── presentation/              Una carpeta por módulo: Screen + ViewModel + UiState
    ├── inicio/  tienda/  metas/  gastos/  auth/  splash/
```

Flujo de datos (ejemplo con la API):
`MetasScreen` → `MetasViewModel.cargarTipoCambio()` → `TipoCambioRepository` →
`ApiService` (Retrofit) → el resultado vuelve como `UiState` en un `StateFlow` → la pantalla
lo observa y se redibuja.

Base de datos (`walletpet.db`): `metas`, `ahorros` (con llave foránea a metas),
`movimientos`, `mascota` (una fila con monedas, nivel, comida y croquetas) y `compras`.
Todo se conserva al cerrar y abrir la app.

## Probar las integraciones

- **Audio:** registrar un ahorro o alimentar al gato → maúlla (subir volumen multimedia).
- **PDF:** Gastos → Reporte → elegir un mes → Descargar PDF → Abrir PDF / Compartir.
- **Biometría:** en el emulador, *Settings → Security* y configurar PIN + huella; en
  *Extended controls → Fingerprint* se simula el toque. Si el dispositivo no tiene nada
  configurado, la app deja pasar.

## Versionado

Ramas `main` / `develop` / `feature/*`. Versión entregada: **v1.0.0** (`versionName` en
`app/build.gradle.kts`).

## Evidencias

Agregar aquí capturas de: inicio con el gato, panel de comida, tienda, carrusel de metas,
tipo de cambio, reporte PDF y pantalla de huella.
