# Guía práctica: interfaces en Android con vistas XML (Views)

Guía para diseñar bien pantallas de una `Activity`/`Fragment` usando layouts XML, con foco en **ConstraintLayout**.

---

## Índice

1. [¿Siempre ConstraintLayout?](#1-siempre-constraintlayout)
2. [Cómo funcionan las constraints](#2-cómo-funcionan-las-constraints)
3. [¿Anclar los 4 lados o solo 2?](#3-anclar-los-4-lados-o-solo-2)
4. [Tamaños: wrap_content, 0dp y match_parent](#4-tamaños-wrap_content-0dp-y-match_parent)
5. [Guidelines: porcentaje vs medidas exactas](#5-guidelines-porcentaje-vs-medidas-exactas)
6. [Herramientas clave de ConstraintLayout](#6-herramientas-clave-de-constraintlayout)
7. [Unidades y dimensiones](#7-unidades-y-dimensiones)
8. [Diseño adaptable (móvil, tablet, horizontal)](#8-diseño-adaptable)
9. [Scroll, teclado y edge-to-edge](#9-scroll-teclado-y-edge-to-edge)
10. [Organización del proyecto](#10-organización-del-proyecto)
11. [Accesibilidad](#11-accesibilidad)
12. [Rendimiento](#12-rendimiento)
13. [Ejemplo completo](#13-ejemplo-completo-pantalla-de-login)
14. [Errores comunes](#14-errores-comunes)
15. [Checklist final](#15-checklist-final)

---

## 1. ¿Siempre ConstraintLayout?

**No siempre, pero es la opción por defecto recomendada** para pantallas con cierta complejidad. Es el layout que Google promueve, el que mejor soporta el editor visual de Android Studio y permite **jerarquías planas** (sin anidar muchos layouts).

### Cuándo usar cada layout

| Situación | Layout recomendado |
|---|---|
| Pantalla compleja con varios elementos relacionados entre sí | **ConstraintLayout** |
| Fila o columna simple de 2-3 elementos | `LinearLayout` (o ConstraintLayout con cadena) |
| Un solo elemento centrado, o superponer vistas | `FrameLayout` |
| Listas largas | `RecyclerView` (el item puede ser ConstraintLayout) |
| Contenido que puede no caber en pantalla | `NestedScrollView` + un hijo (LinearLayout o ConstraintLayout) |
| Toolbar colapsable, FAB, comportamientos de scroll | `CoordinatorLayout` + `AppBarLayout` |
| Animaciones complejas entre estados de la UI | `MotionLayout` (extiende ConstraintLayout) |
| Rejillas regulares | `RecyclerView` con `GridLayoutManager` |

> **Regla práctica:** si necesitas anidar más de 2 niveles de `LinearLayout` para conseguir el diseño, probablemente deberías usar ConstraintLayout.

> **Nota:** `RelativeLayout` está en desuso a efectos prácticos; ConstraintLayout hace todo lo que hace y más. `TableLayout` y `GridLayout` tampoco se recomiendan para código nuevo.

---

## 2. Cómo funcionan las constraints

Una constraint es una **conexión entre un lado de una vista y otra cosa** (el padre, otra vista, una guideline o una barrier).

Lados disponibles:

- Horizontal: `layout_constraintStart_toStartOf`, `layout_constraintStart_toEndOf`, `layout_constraintEnd_toEndOf`, `layout_constraintEnd_toStartOf`
- Vertical: `layout_constraintTop_toTopOf`, `layout_constraintTop_toBottomOf`, `layout_constraintBottom_toBottomOf`, `layout_constraintBottom_toTopOf`
- Texto: `layout_constraintBaseline_toBaselineOf`

### Regla de oro

> **Toda vista necesita como mínimo una constraint horizontal y una vertical.**

Sin ellas, la vista se coloca en la esquina superior izquierda (0,0) en tiempo de ejecución, aunque en el editor la veas bien colocada. Es el error nº 1 de quien empieza.

### `start`/`end` en lugar de `left`/`right`

Usa siempre **`Start`/`End`** en vez de `Left`/`Right`. Así la interfaz se adapta a idiomas de derecha a izquierda (árabe, hebreo). Lo mismo para `marginStart`/`marginEnd` y `paddingStart`/`paddingEnd`.

```xml
<!-- ✅ Bien -->
android:layout_marginStart="16dp"
app:layout_constraintStart_toStartOf="parent"

<!-- ❌ Evitar -->
android:layout_marginLeft="16dp"
app:layout_constraintLeft_toLeftOf="parent"
```

---

## 3. ¿Anclar los 4 lados o solo 2?

Depende de **qué comportamiento quieres**. No hay una respuesta única, pero sí reglas claras.

### Mínimo necesario: 2 constraints (1 horizontal + 1 vertical)

La vista mantiene su tamaño y se posiciona desde ese punto.

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### Dos constraints opuestas en el mismo eje: centrado (con bias)

Si anclas **Start y End** (o **Top y Bottom**), la vista queda **centrada** en ese eje. Con `layout_constraintHorizontal_bias` (0 a 1) puedes desplazarla.

```xml
<Button
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintHorizontal_bias="0.3"
    app:layout_constraintTop_toTopOf="parent" />
```

### Dos constraints opuestas + `0dp`: la vista se estira

Es la forma correcta de "ocupar todo el ancho disponible" respetando los márgenes.

```xml
<EditText
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### Resumen

| Constraints | Resultado |
|---|---|
| 1 por eje (ej. Start + Top) | Posición fija respecto a un punto, tamaño propio |
| 2 en un eje + `wrap_content` | Centrada en ese eje (ajustable con bias) |
| 2 en un eje + `0dp` | Se estira entre ambos anclajes |
| Los 4 lados + `wrap_content` | Centrada en ambos ejes |
| Los 4 lados + `0dp` | Ocupa todo el espacio entre anclajes |

### Consejos

- **No hace falta anclar los 4 lados a todo.** Añade solo las constraints que expresan una relación real de diseño.
- **Cada constraint extra es una relación que mantener.** Si cambias una vista, las demás pueden moverse. Mantén las relaciones lo más simples y lógicas posible.
- Ancla a **la vista con la que tiene relación visual**, no siempre al padre. Ejemplo: un botón "Guardar" debajo de un campo de texto → `Top_toBottomOf="@id/campo"`. Si mañana el campo se mueve, el botón le sigue.
- Evita **dependencias circulares** entre vistas (A depende de B y B depende de A).

---

## 4. Tamaños: wrap_content, 0dp y match_parent

En ConstraintLayout hay tres formas de definir tamaño:

| Valor | Significado | Cuándo usarlo |
|---|---|---|
| `wrap_content` | Tamaño según el contenido | Textos, iconos, botones |
| `0dp` (match_constraint) | Ocupa el espacio entre sus constraints | Campos de texto, imágenes, contenedores que deben estirarse |
| Valor fijo (`48dp`) | Tamaño exacto | Iconos, avatares, botones cuadrados |
| `match_parent` | ⚠️ **No recomendado** en hijos de ConstraintLayout | Solo para el propio ConstraintLayout dentro de su padre |

> ⚠️ **Usa `0dp` en lugar de `match_parent`** para hijos de ConstraintLayout. `match_parent` ignora las constraints y puede dar resultados inesperados.

### `wrap_content` con límites

Un texto largo con `wrap_content` puede empujar otras vistas. Para evitarlo:

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:layout_constrainedWidth="true"
    app:layout_constraintStart_toEndOf="@id/avatar"
    app:layout_constraintEnd_toStartOf="@id/btnMas"
    app:layout_constraintHorizontal_bias="0" />
```

`layout_constrainedWidth="true"` hace que el `wrap_content` respete las constraints y no se salga.

### Tamaño mínimo y máximo

```xml
app:layout_constraintWidth_min="100dp"
app:layout_constraintWidth_max="400dp"
app:layout_constraintHeight_min="48dp"
```

### Proporción (aspect ratio)

Muy útil para imágenes o tarjetas. Una de las dimensiones debe ser `0dp`:

```xml
<ImageView
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:layout_constraintDimensionRatio="16:9"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

---

## 5. Guidelines: porcentaje vs medidas exactas

Una `Guideline` es una **línea invisible** (horizontal o vertical) a la que anclar vistas. No se dibuja ni consume recursos significativos.

Se puede posicionar de tres formas:

```xml
<!-- Porcentaje del contenedor -->
<androidx.constraintlayout.widget.Guideline
    android:id="@+id/guidelineMitad"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    app:layout_constraintGuide_percent="0.5" />

<!-- Distancia fija desde el inicio (start / top) -->
<androidx.constraintlayout.widget.Guideline
    android:id="@+id/guidelineInicio"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    app:layout_constraintGuide_begin="24dp" />

<!-- Distancia fija desde el final (end / bottom) -->
<androidx.constraintlayout.widget.Guideline
    android:id="@+id/guidelineFin"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    app:layout_constraintGuide_end="24dp" />
```

### ¿Porcentaje o dp?

| Usa **porcentaje** (`guide_percent`) | Usa **medida fija** (`guide_begin` / `guide_end` en dp) |
|---|---|
| Dividir la pantalla en zonas proporcionales (40 % imagen / 60 % formulario) | Márgenes de contenido consistentes (24dp a cada lado) |
| Cabeceras o "hero" que deben ocupar una fracción de la altura | Alinear varias vistas a una misma línea con un margen concreto |
| Layouts donde la proporción importa más que el tamaño absoluto | Cuando el valor debe ser el mismo en móvil y tablet |
| Centrar o repartir elementos en pantallas de tamaño muy variable | Cuando el elemento debe respetar un tamaño mínimo (tocable, legible) |

### Recomendaciones

1. **Márgenes laterales del contenido → guidelines con dp**, no porcentaje. Un 5 % son 18dp en un móvil pequeño y 60dp en una tablet, lo cual rara vez es lo deseado.
2. **Divisiones de pantalla → porcentaje.** Ideal para una zona superior decorativa o para repartir columnas.
3. **No abuses.** Si solo necesitas un margen desde el padre, usa `layout_margin` directamente. Una guideline vale la pena cuando **varias vistas** comparten esa línea.
4. **No uses porcentajes para textos.** El texto no escala con el porcentaje y puede cortarse en pantallas pequeñas.
5. Para tamaño relativo de una vista (no una línea), existe también `layout_constraintWidth_percent` con `0dp`:

```xml
<View
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    app:layout_constraintWidth_percent="0.6"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

---

## 6. Herramientas clave de ConstraintLayout

### Chains (cadenas)

Reparten vistas en una fila o columna. Se crean anclando cada vista a la siguiente **en ambos sentidos**. La primera vista define el estilo con `layout_constraintHorizontal_chainStyle`:

| Estilo | Efecto |
|---|---|
| `spread` (por defecto) | Reparto equitativo con espacios entre vistas |
| `spread_inside` | Primera y última pegadas a los bordes, resto repartido |
| `packed` | Vistas agrupadas juntas (centradas, ajustable con bias) |

Con `0dp` y `layout_constraintHorizontal_weight` obtienes un comportamiento parecido a `layout_weight` de LinearLayout:

```xml
<Button
    android:id="@+id/btnA"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    app:layout_constraintHorizontal_weight="1"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toStartOf="@id/btnB" />

<Button
    android:id="@+id/btnB"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    app:layout_constraintHorizontal_weight="2"
    app:layout_constraintStart_toEndOf="@id/btnA"
    app:layout_constraintEnd_toEndOf="parent" />
```

### Barrier

Una línea invisible que **se coloca junto a la vista más grande de un grupo**. Perfecta cuando el contenido es dinámico (textos traducidos, longitud variable).

```xml
<androidx.constraintlayout.widget.Barrier
    android:id="@+id/barrierEtiquetas"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:barrierDirection="end"
    app:constraint_referenced_ids="labelNombre,labelEmail,labelTelefono" />
```

Después ancla los campos a `barrierEtiquetas` y todos quedarán alineados sin importar qué etiqueta sea la más larga.

### Baseline

Alinea el texto de dos vistas por su línea base (ej. un `TextView` junto a un `EditText`):

```xml
app:layout_constraintBaseline_toBaselineOf="@id/campo"
```

### Group

Controla la visibilidad de varias vistas a la vez:

```xml
<androidx.constraintlayout.widget.Group
    android:id="@+id/groupError"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:constraint_referenced_ids="iconoError,textoError" />
```

> ℹ️ En versiones recientes de ConstraintLayout, `Group` tiene algunas limitaciones si cambias la visibilidad de sus elementos individualmente después. Ten cuidado con eso.

### Flow

Reparte vistas en varias filas/columnas con salto automático (tipo chips). Útil para contenido dinámico sin RecyclerView.

### Bias

Desplaza una vista entre dos anclajes opuestos: `layout_constraintHorizontal_bias` / `layout_constraintVertical_bias` (0.0 a 1.0).

### `goneMargin`

Margen que se aplica **solo cuando la vista a la que estás anclado tiene `visibility="gone"`**. Muy útil para evitar saltos de diseño:

```xml
app:layout_goneMarginTop="0dp"
```

### `visibility="gone"` vs `invisible`

- `gone`: no ocupa espacio; las constraints que apuntan a ella se colapsan a un punto.
- `invisible`: no se ve, pero conserva su espacio.

---

## 7. Unidades y dimensiones

| Unidad | Uso |
|---|---|
| `dp` | Tamaños, márgenes, paddings, separaciones |
| `sp` | **Solo texto** (respeta el tamaño de fuente del usuario) |
| `px` | ❌ Nunca |

Buenas prácticas:

- **Nada de valores mágicos.** Define dimensiones repetidas en `res/values/dimens.xml`:

```xml
<resources>
    <dimen name="margin_screen">24dp</dimen>
    <dimen name="margin_small">8dp</dimen>
    <dimen name="margin_medium">16dp</dimen>
    <dimen name="icon_size">24dp</dimen>
    <dimen name="touch_target_min">48dp</dimen>
</resources>
```

- Usa **múltiplos de 4dp u 8dp** para márgenes y espaciados (sistema de cuadrícula de Material Design).
- **Evita alturas fijas en textos o botones**: si el usuario aumenta la fuente del sistema, se cortará. Usa `wrap_content` y `minHeight`.
- **Textos**: `android:textSize="16sp"`. Mejor aún, usa estilos de texto del tema (`?attr/textAppearanceBodyLarge`).

---

## 8. Diseño adaptable

No diseñes solo para tu móvil. Considera pantallas pequeñas, tablets, plegables y modo horizontal.

### Buenas prácticas

- **Prefiere constraints y `0dp`** a tamaños fijos grandes.
- **Limita el ancho máximo** del contenido en pantallas grandes:

```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    app:layout_constraintWidth_max="480dp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

- **Layouts alternativos** cuando la disposición cambia realmente:

```
res/
  layout/                → móvil vertical (por defecto)
  layout-land/           → horizontal
  layout-sw600dp/        → tablets (ancho ≥ 600dp)
  layout-sw600dp-land/   → tablets en horizontal
```

- Para apps modernas y plegables, usa **Window Size Classes** (`androidx.window`) y adapta la UI en función de `Compact` / `Medium` / `Expanded`.
- **Prueba** con distintos dispositivos en el editor (`Preview`), con **fuente grande** (Ajustes → Accesibilidad) y con idiomas largos (alemán) o RTL.

---

## 9. Scroll, teclado y edge-to-edge

### Scroll

Si el contenido puede no caber (formularios, pantallas de detalle), envuélvelo:

```xml
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <!-- contenido -->
    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.core.widget.NestedScrollView>
```

- `fillViewport="true"` hace que el contenido ocupe al menos toda la pantalla (útil si quieres anclar algo abajo).
- Dentro de un scroll, **el ConstraintLayout debe tener `wrap_content` de alto** y no puedes anclar vistas al bottom "del scroll" si el contenido es más grande que la pantalla.
- **Nunca** pongas un `RecyclerView` dentro de un `ScrollView` sin necesidad; provoca problemas de rendimiento y de medición.

### Teclado

En el `AndroidManifest.xml`:

```xml
<activity
    android:name=".LoginActivity"
    android:windowSoftInputMode="adjustResize" />
```

`adjustResize` redimensiona la ventana al aparecer el teclado; junto con un scroll evita que el teclado tape los campos.

### Edge-to-edge y window insets

En las versiones recientes de Android (a partir de targetSdk 35 / Android 15) la app se dibuja **de borde a borde** por defecto, por debajo de la barra de estado y la de navegación. Debes gestionar los *insets* para que tu contenido no quede tapado:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(binding.root)

    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}
```

---

## 10. Organización del proyecto

### Nombres de IDs y archivos

- Layouts: `activity_login.xml`, `fragment_home.xml`, `item_user.xml`, `dialog_confirm.xml`, `view_custom_header.xml`
- IDs en `camelCase` con prefijo de tipo si ayuda a leer: `btnLogin`, `etEmail`, `tvTitle`, `ivAvatar`, `rvUsers`. Elige un estilo y sé consistente en todo el proyecto.

### Recursos, nunca hardcodeados

```xml
<!-- ❌ Mal -->
<TextView android:text="Iniciar sesión" android:textColor="#FF5722" />

<!-- ✅ Bien -->
<TextView android:text="@string/login_title"
          android:textColor="?attr/colorPrimary" />
```

- **Textos** → `strings.xml` (con traducciones).
- **Colores** → `colors.xml` o, mejor, **atributos del tema** (`?attr/colorPrimary`, `?attr/colorOnSurface`) para que funcione el modo oscuro.
- **Estilos repetidos** → `styles.xml` / `themes.xml`.

### Componentes Material

Usa `com.google.android.material.*` (`MaterialButton`, `TextInputLayout`, `MaterialToolbar`, `MaterialCardView`...). Ya vienen con theming, estados y accesibilidad resueltos.

### Reutilización

- `<include layout="@layout/view_header" />` para reutilizar fragmentos de layout.
- `<merge>` como raíz de layouts incluidos para no añadir un nivel extra de jerarquía.
- **Vista personalizada** (`CustomView`) si un bloque se repite con lógica propia.

### Acceso a las vistas

Usa **ViewBinding** en vez de `findViewById`:

```kotlin
private lateinit var binding: ActivityLoginBinding

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.btnLogin.setOnClickListener { /* ... */ }
}
```

En `build.gradle.kts`:

```kotlin
android {
    buildFeatures { viewBinding = true }
}
```

### Atributos `tools:`

Sirven para ver contenido de ejemplo en el editor sin que llegue a la app:

```xml
tools:text="Nombre de ejemplo muy largo para probar"
tools:src="@tools:sample/avatars"
tools:visibility="visible"
```

Es muy recomendable probar con **textos largos** para detectar desbordamientos.

---

## 11. Accesibilidad

- **Área táctil mínima: 48dp × 48dp.** Si el icono es más pequeño, añade padding o `minWidth`/`minHeight`.
- **`contentDescription`** en imágenes e iconos con significado. Usa `android:importantForAccessibility="no"` en los decorativos.
- **Contraste** suficiente entre texto y fondo (mínimo 4.5:1 para texto normal).
- **No transmitas información solo por color.**
- Usa `sp` para textos y comprueba la interfaz con **fuente grande**.
- Asocia etiquetas y campos (`android:labelFor`) o usa `TextInputLayout`.
- Un orden lógico de foco: si el orden visual no coincide con el orden de lectura, ajusta con `accessibilityTraversalBefore/After`.
- Prueba con **TalkBack**.

---

## 12. Rendimiento

- **Jerarquía plana**: es la gran ventaja de ConstraintLayout. Evita anidar layouts sin necesidad.
- **Evita `LinearLayout` anidados con `layout_weight`**: se miden dos veces.
- **Infla menos, reutiliza más**: `RecyclerView` para listas, `ViewStub` para vistas que casi nunca se muestran.
- **Evita el sobredibujo (overdraw)**: no pongas fondos innecesarios en varios niveles. Quita el `windowBackground` si tu layout ya lo cubre por completo.
- Herramientas: **Layout Inspector**, **Layout Validation** y **Profile GPU Rendering** en Android Studio.

---

## 13. Ejemplo completo: pantalla de login

Combina guidelines con dp para márgenes, una zona superior proporcional, `0dp` para campos y scroll para el teclado.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    tools:context=".LoginActivity">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <!-- Márgenes laterales consistentes (dp) -->
        <androidx.constraintlayout.widget.Guideline
            android:id="@+id/guideStart"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            app:layout_constraintGuide_begin="@dimen/margin_screen" />

        <androidx.constraintlayout.widget.Guideline
            android:id="@+id/guideEnd"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            app:layout_constraintGuide_end="@dimen/margin_screen" />

        <!-- Zona del logo: proporcional a la altura (porcentaje) -->
        <androidx.constraintlayout.widget.Guideline
            android:id="@+id/guideLogoBottom"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            app:layout_constraintGuide_percent="0.28" />

        <ImageView
            android:id="@+id/ivLogo"
            android:layout_width="96dp"
            android:layout_height="96dp"
            android:contentDescription="@string/app_logo"
            android:src="@drawable/ic_logo"
            app:layout_constraintBottom_toBottomOf="@id/guideLogoBottom"
            app:layout_constraintEnd_toEndOf="@id/guideEnd"
            app:layout_constraintStart_toStartOf="@id/guideStart"
            app:layout_constraintTop_toTopOf="parent" />

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/login_title"
            android:textAlignment="center"
            android:textAppearance="?attr/textAppearanceHeadlineMedium"
            app:layout_constraintEnd_toEndOf="@id/guideEnd"
            app:layout_constraintStart_toStartOf="@id/guideStart"
            app:layout_constraintTop_toBottomOf="@id/guideLogoBottom" />

        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilEmail"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/margin_medium"
            android:hint="@string/email"
            app:layout_constraintEnd_toEndOf="@id/guideEnd"
            app:layout_constraintStart_toStartOf="@id/guideStart"
            app:layout_constraintTop_toBottomOf="@id/tvTitle"
            app:layout_constraintWidth_max="480dp">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etEmail"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textEmailAddress" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.textfield.TextInputLayout
            android:id="@+id/tilPassword"
            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/margin_small"
            android:hint="@string/password"
            app:endIconMode="password_toggle"
            app:layout_constraintEnd_toEndOf="@id/guideEnd"
            app:layout_constraintStart_toStartOf="@id/guideStart"
            app:layout_constraintTop_toBottomOf="@id/tilEmail"
            app:layout_constraintWidth_max="480dp">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPassword" />
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnLogin"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/margin_medium"
            android:text="@string/login"
            app:layout_constraintEnd_toEndOf="@id/guideEnd"
            app:layout_constraintStart_toStartOf="@id/guideStart"
            app:layout_constraintTop_toBottomOf="@id/tilPassword"
            app:layout_constraintWidth_max="480dp" />

        <TextView
            android:id="@+id/tvRegister"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:minHeight="@dimen/touch_target_min"
            android:gravity="center"
            android:text="@string/register_prompt"
            app:layout_constraintEnd_toEndOf="@id/guideEnd"
            app:layout_constraintStart_toStartOf="@id/guideStart"
            app:layout_constraintTop_toBottomOf="@id/btnLogin"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintVertical_bias="1"
            android:layout_marginTop="@dimen/margin_medium"
            android:layout_marginBottom="@dimen/margin_medium" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.core.widget.NestedScrollView>
```

Puntos a notar:

- Los **márgenes laterales** vienen de guidelines en `dp` (iguales en todos los dispositivos).
- La **zona del logo** usa un `percent` (se adapta a la altura).
- Los campos usan **`0dp`** anclados a las guidelines y tienen **ancho máximo** para tablets.
- Todo cuelga de relaciones lógicas (`Top_toBottomOf` la vista anterior).
- Todo va en un scroll por si aparece el teclado o la fuente es grande.

---

## 14. Errores comunes

| Error | Consecuencia | Solución |
|---|---|---|
| Vista sin constraints en un eje | Se pinta en (0,0) al ejecutar | Añadir 1 horizontal + 1 vertical como mínimo |
| `match_parent` en hijos de ConstraintLayout | Comportamiento impredecible | Usar `0dp` con constraints |
| `Left`/`Right` en vez de `Start`/`End` | Falla en RTL | Usar `Start`/`End` |
| Todo anclado al padre con márgenes fijos | Si cambia una vista, nada se reajusta | Anclar a la vista relacionada |
| Márgenes laterales en porcentaje | Márgenes enormes en tablet | Guidelines con dp |
| Altura fija en textos/botones | Texto cortado con fuente grande | `wrap_content` + `minHeight` |
| Textos, colores y dimens hardcodeados | Sin traducción ni modo oscuro | Recursos y atributos del tema |
| `RecyclerView` dentro de `ScrollView` | Rendimiento pobre, scroll raro | `RecyclerView` como elemento principal, o `ConcatAdapter` |
| Probar solo en un móvil | Fallos en otros tamaños | Preview multi-dispositivo, tablet, landscape, fuente grande |
| Ignorar los insets (edge-to-edge) | Contenido tapado por barras | Gestionar `WindowInsets` |
| Dependencias circulares | Layout inválido | Simplificar relaciones |
| Jerarquías profundas de LinearLayout | Medición lenta | Aplanar con ConstraintLayout |

---

## 15. Checklist final

- [ ] ¿Cada vista tiene al menos una constraint horizontal y una vertical?
- [ ] ¿Uso `start`/`end` en lugar de `left`/`right`?
- [ ] ¿Uso `0dp` en lugar de `match_parent` dentro de ConstraintLayout?
- [ ] ¿Las constraints reflejan relaciones reales entre vistas (no todo al padre)?
- [ ] ¿Guidelines en dp para márgenes y en porcentaje solo para proporciones?
- [ ] ¿Textos en `sp`, resto en `dp`, sin `px`?
- [ ] ¿Nada hardcodeado (strings, colores, dimens)?
- [ ] ¿Colores del tema (`?attr/...`) y probado en modo oscuro?
- [ ] ¿Ancho máximo para contenido en pantallas grandes?
- [ ] ¿Scroll donde el contenido pueda no caber?
- [ ] ¿Probado en horizontal, tablet, fuente grande y textos largos?
- [ ] ¿Áreas táctiles ≥ 48dp y `contentDescription` donde toca?
- [ ] ¿Insets gestionados (edge-to-edge) y teclado no tapa campos?
- [ ] ¿ViewBinding en lugar de `findViewById`?
- [ ] ¿Jerarquía plana, sin anidar de más?
- [ ] ¿Comprobado con TalkBack y Layout Inspector?

---

## Resumen rápido

1. **ConstraintLayout por defecto** para pantallas no triviales; layouts simples para casos simples.
2. **Mínimo 2 constraints** (una por eje). Pon más solo cuando expresen una relación de diseño real.
3. **`0dp` + dos anclajes opuestos** para estirar; **dos anclajes + `wrap_content`** para centrar.
4. **Guidelines**: dp para márgenes y alineaciones, porcentaje para repartir zonas.
5. **Piensa en adaptabilidad** desde el principio: anchos máximos, layouts alternativos, fuentes grandes.
6. **Recursos y temas** para todo; **ViewBinding** para acceder a las vistas.
7. **Nota**: para proyectos nuevos, Google recomienda **Jetpack Compose**, pero las vistas XML siguen siendo totalmente válidas y muy presentes en proyectos existentes.
