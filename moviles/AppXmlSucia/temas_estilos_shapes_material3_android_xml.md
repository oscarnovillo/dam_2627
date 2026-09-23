# Temas, estilos, colores y Shapes en Android XML

Material para 2.º DAM: creación y aplicación de temas en aplicaciones
Android con vistas XML.

## 1. Objetivos

Al finalizar la unidad, el alumno debe ser capaz de:

-   Crear una paleta de colores reutilizable.
-   Crear y aplicar un tema Android.
-   Diferenciar entre tema, estilo y drawable.
-   Utilizar atributos del tema mediante `?attr/...`.
-   Crear fondos personalizados con `shape`.
-   Utilizar `selector` para representar estados.
-   Preparar una aplicación para modo claro y oscuro.
-   Generar un tema Material 3 mediante Material Theme Builder.
-   Importar los archivos generados en un proyecto Android con XML.

------------------------------------------------------------------------

## 2. ¿Qué es un tema?

Un tema (`Theme`) es un conjunto de propiedades visuales que se aplica a
una aplicación o a una `Activity`.

Puede definir:

-   Colores.
-   Tipografías.
-   Fondos.
-   Apariencia de componentes.
-   Barra de estado y navegación.
-   Estilos de Material Design.
-   Comportamiento para modo claro y oscuro.

La ventaja principal es centralizar el diseño. Si se cambia el color
principal de la aplicación, no es necesario editar todas las vistas
individualmente.

### Ejemplo conceptual

Una aplicación tiene 20 botones. Todos utilizan el atributo:

``` xml
android:backgroundTint="?attr/colorPrimary"
```

Si se modifica `colorPrimary` en el tema, los botones que utilicen ese
atributo podrán adaptarse automáticamente.

------------------------------------------------------------------------

## 3. Organización de recursos

Una estructura habitual es:

``` text
app/
└── src/
    └── main/
        ├── AndroidManifest.xml
        └── res/
            ├── drawable/
            │   ├── bg_button.xml
            │   └── bg_card.xml
            ├── layout/
            │   └── activity_main.xml
            ├── values/
            │   ├── colors.xml
            │   ├── dimens.xml
            │   ├── strings.xml
            │   ├── styles.xml
            │   └── themes.xml
            └── values-night/
                ├── colors.xml
                └── themes.xml
```

  Carpeta           Uso
  ----------------- ------------------------------------------------
  `values/`         Colores, estilos, temas, dimensiones y textos.
  `values-night/`   Recursos alternativos para modo oscuro.
  `drawable/`       Shapes, selectores, vectores e imágenes.
  `layout/`         Pantallas XML.

Referencias habituales:

``` xml
@color/primary
@drawable/bg_button
@style/Theme.MiApp
@string/app_name
?attr/colorPrimary
```

------------------------------------------------------------------------

## 4. Crear una paleta de colores

Archivo:

``` text
res/values/colors.xml
```

Ejemplo:

``` xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <color name="primary">#6750A4</color>
    <color name="on_primary">#FFFFFF</color>

    <color name="secondary">#625B71</color>
    <color name="on_secondary">#FFFFFF</color>

    <color name="background">#FFFBFE</color>
    <color name="on_background">#1C1B1F</color>

    <color name="surface">#FFFBFE</color>
    <color name="on_surface">#1C1B1F</color>

</resources>
```

### Significado de `on_*`

Los colores `on_*` se utilizan para el contenido que aparece encima de
otro color:

-   `primary`: fondo principal.
-   `on_primary`: texto o icono sobre el fondo principal.
-   `surface`: color de una superficie o contenedor.
-   `on_surface`: contenido situado sobre la superficie.

Ejemplo:

``` text
primary    = fondo morado
on_primary = texto blanco
```

En Material 3 también son habituales los roles:

-   `primary`
-   `onPrimary`
-   `primaryContainer`
-   `onPrimaryContainer`
-   `secondary`
-   `surface`
-   `surfaceContainer`
-   `onSurface`
-   `outline`

La nomenclatura exacta depende de si se utilizan recursos propios,
atributos de Material Components o archivos exportados por Material
Theme Builder.

------------------------------------------------------------------------

## 5. Crear el tema XML

Archivo:

``` text
res/values/themes.xml
```

Ejemplo con Material 3:

``` xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.MiApp"
        parent="Theme.Material3.DayNight.NoActionBar">

        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnPrimary">@color/on_primary</item>

        <item name="colorSecondary">@color/secondary</item>
        <item name="colorOnSecondary">@color/on_secondary</item>

        <item name="android:windowBackground">
            @color/background
        </item>

    </style>

</resources>
```

### Explicación

``` xml
<style name="Theme.MiApp"
    parent="Theme.Material3.DayNight.NoActionBar">
```

-   `name`: nombre del tema.
-   `parent`: tema del que se hereda.
-   `Material3`: utiliza el sistema Material 3.
-   `DayNight`: permite trabajar con modo claro y oscuro.
-   `NoActionBar`: evita una barra de acción tradicional automática.

La aplicación debe incluir una versión compatible de Material Components
para Android.

------------------------------------------------------------------------

## 6. Aplicar el tema en el Manifest

En `AndroidManifest.xml`:

``` xml
<application
    android:theme="@style/Theme.MiApp"
    android:label="@string/app_name"
    android:allowBackup="true"
    android:supportsRtl="true">

    <activity
        android:name=".MainActivity"
        android:exported="true">

        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>

    </activity>

</application>
```

También se puede aplicar un tema a una única `Activity`:

``` xml
<activity
    android:name=".SettingsActivity"
    android:theme="@style/Theme.MiApp.Settings" />
```

------------------------------------------------------------------------

## 7. Recursos de color y atributos de tema

### Acceder a un color

``` xml
android:background="@color/primary"
```

Utiliza un recurso de color concreto.

### Acceder a un atributo del tema

``` xml
android:background="?attr/colorPrimary"
```

Consulta el valor de `colorPrimary` en el tema activo.

  Sintaxis                Significado
  ----------------------- -------------------------------------------
  `@color/primary`        Recurso de color concreto.
  `?attr/colorPrimary`    Atributo del tema actual.
  `@drawable/bg_button`   Drawable XML o imagen.
  `?attr/colorSurface`    Color de superficie definido por el tema.

Se recomienda utilizar `?attr/...` cuando se desea que el componente se
adapte al tema activo.

------------------------------------------------------------------------

## 8. Ejemplo de layout XML

``` xml
<?xml version="1.0" encoding="utf-8"?>

<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center"
    android:background="?attr/colorSurface">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Iniciar sesión"
        android:textSize="28sp"
        android:textStyle="bold"
        android:textColor="?attr/colorOnSurface"
        android:gravity="center"/>

    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:hint="Usuario"
        android:inputType="text"
        android:textColor="?attr/colorOnSurface"/>

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="Entrar"
        android:textColor="?attr/colorOnPrimary"
        android:backgroundTint="?attr/colorPrimary"/>

</LinearLayout>
```

------------------------------------------------------------------------

## 9. ¿Qué es un Shape Drawable?

Un `shape` es un drawable XML que permite crear formas reutilizables:

-   Fondos redondeados.
-   Bordes.
-   Botones.
-   Tarjetas.
-   Campos de texto.
-   Contenedores.

Se guarda en:

``` text
res/drawable/
```

### Elementos principales

  Elemento     Función
  ------------ -----------------------------
  `shape`      Define el tipo de forma.
  `solid`      Define el color de relleno.
  `corners`    Redondea las esquinas.
  `stroke`     Define un borde.
  `padding`    Define espacio interior.
  `gradient`   Define un degradado.

------------------------------------------------------------------------

## 10. Shape para un botón

Archivo:

``` text
res/drawable/bg_button.xml
```

``` xml
<?xml version="1.0" encoding="utf-8"?>

<shape
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <solid
        android:color="?attr/colorPrimary"/>

    <corners
        android:radius="16dp"/>

    <padding
        android:left="16dp"
        android:top="12dp"
        android:right="16dp"
        android:bottom="12dp"/>

</shape>
```

Aplicación:

``` xml
<Button
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Entrar"
    android:textColor="@color/on_primary"
    android:background="@drawable/bg_button"/>
```

### Nota

Los componentes tradicionales pueden aplicar su propio tintado o estilo.
En proyectos Material 3 suele ser preferible usar componentes Material,
como `MaterialButton`, y sus atributos específicos cuando se necesita
controlar estados, esquinas, stroke y otros aspectos.

------------------------------------------------------------------------

## 11. Shape con borde

``` xml
<?xml version="1.0" encoding="utf-8"?>

<shape
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">

    <solid android:color="@android:color/transparent"/>

    <stroke
        android:width="2dp"
        android:color="?attr/colorPrimary"/>

    <corners
        android:radius="12dp"/>

    <padding
        android:left="16dp"
        android:top="12dp"
        android:right="16dp"
        android:bottom="12dp"/>

</shape>
```

Se puede utilizar para botones secundarios, tarjetas y contenedores.

------------------------------------------------------------------------

## 12. Selector para estados

Un `selector` permite seleccionar un drawable según el estado de una
vista:

-   Normal.
-   Pulsado.
-   Deshabilitado.
-   Enfocado.

Archivo:

``` text
res/drawable/button_background.xml
```

``` xml
<?xml version="1.0" encoding="utf-8"?>

<selector
    xmlns:android="http://schemas.android.com/apk/res/android">

    <item
        android:state_enabled="false"
        android:drawable="@drawable/bg_button_disabled"/>

    <item
        android:state_pressed="true"
        android:drawable="@drawable/bg_button_pressed"/>

    <item
        android:drawable="@drawable/bg_button"/>

</selector>
```

Diferencias:

  Recurso        Función
  -------------- ----------------------------------------
  `shape`        Define una forma.
  `selector`     Selecciona recursos según el estado.
  `layer-list`   Combina varias capas.
  `ripple`       Añade un efecto de interacción táctil.

------------------------------------------------------------------------

## 13. Estilos frente a temas

### Tema

Define la apariencia general de la aplicación o de una `Activity`.

``` xml
<style name="Theme.MiApp"
    parent="Theme.Material3.DayNight.NoActionBar">

    <item name="colorPrimary">@color/primary</item>

</style>
```

### Estilo

Agrupa atributos reutilizables para un elemento.

Archivo:

``` text
res/values/styles.xml
```

``` xml
<resources>

    <style name="TituloPantalla">
        <item name="android:textSize">28sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:layout_width">match_parent</item>
        <item name="android:layout_height">wrap_content</item>
    </style>

</resources>
```

Uso:

``` xml
<TextView
    style="@style/TituloPantalla"
    android:text="Mi aplicación"/>
```

### Resumen

  -----------------------------------------------------------------------
  Tema                                Estilo
  ----------------------------------- -----------------------------------
  Configuración visual global.        Agrupación de atributos
                                      reutilizables.

  Se aplica a la aplicación o         Se aplica a vistas o componentes.
  Activity.                           

  Proporciona atributos de tema.      Puede utilizar atributos del tema.
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 14. Estilos que utilizan atributos del tema

Un estilo puede referenciar un atributo del tema:

``` xml
<style name="TituloPantalla">
    <item name="android:textColor">?attr/colorOnSurface</item>
    <item name="android:textSize">28sp</item>
    <item name="android:textStyle">bold</item>
</style>
```

De esta manera el estilo no depende de un color fijo. El color se
obtiene del tema activo.

------------------------------------------------------------------------

## 15. Modo oscuro con `values-night`

Se pueden crear recursos alternativos para el modo oscuro:

``` text
res/values-night/colors.xml
```

Ejemplo:

``` xml
<resources>

    <color name="background">#121212</color>
    <color name="on_background">#FFFFFF</color>

    <color name="surface">#121212</color>
    <color name="on_surface">#FFFFFF</color>

</resources>
```

Si el sistema está en modo oscuro, Android seleccionará los recursos
compatibles de `values-night`.

Es preferible evitar colores fijos en los layouts:

``` xml
android:textColor="#000000"
```

Y utilizar atributos del tema:

``` xml
android:textColor="?attr/colorOnSurface"
```

------------------------------------------------------------------------

# 16. Generar un tema con Material Theme Builder

## 16.1. ¿Qué es Material Theme Builder?

Material Theme Builder es una herramienta de Material Design que permite
crear un tema personalizado a partir de colores de marca, generar
esquemas claros y oscuros y exportar recursos para plataformas como
Android Views (XML).

Web oficial:

https://m3.material.io/theme-builder/

La herramienta permite:

-   Elegir un color principal.
-   Generar paletas tonales.
-   Crear roles de color de Material 3.
-   Personalizar colores secundarios y terciarios.
-   Configurar tipografías.
-   Visualizar el tema.
-   Exportar código para Android Views (XML).
-   Exportar código para Compose y otros entornos compatibles.

## 16.2. Procedimiento paso a paso

### Paso 1. Abrir la herramienta

Accede a:

https://m3.material.io/theme-builder/

También puede utilizarse el proyecto de Material Theme Builder
disponible en GitHub:

https://github.com/material-foundation/material-theme-builder

### Paso 2. Elegir el color principal

En la herramienta:

1.  Localiza el color principal o `Primary`.
2.  Introduce un color hexadecimal de marca.
3.  Comprueba la paleta tonal generada.
4.  Revisa las variantes claras y oscuras.

Ejemplo de color de marca:

``` text
#6750A4
```

No es necesario introducir manualmente todos los colores. La herramienta
deriva roles adicionales a partir de los colores principales.

### Paso 3. Revisar el esquema de color

Revisa los roles generados:

-   Primary.
-   On Primary.
-   Primary Container.
-   On Primary Container.
-   Secondary.
-   Tertiary.
-   Background.
-   Surface.
-   On Surface.
-   Outline.

Es importante comprobar el contraste entre el fondo y el contenido. La
herramienta ayuda a generar combinaciones coherentes, pero el diseño
debe revisarse en el contexto real de la aplicación.

### Paso 4. Personalizar colores adicionales

Según las opciones disponibles en la versión de la herramienta:

1.  Accede a la sección de colores personalizados o extendidos.
2.  Añade colores adicionales si la aplicación lo necesita.
3.  Cambia el nombre de los colores personalizados.
4.  Revisa si deben armonizarse con el esquema principal.

Ejemplos de colores adicionales:

-   Éxito.
-   Advertencia.
-   Error de negocio.
-   Estado informativo.

No conviene crear colores personalizados para cada componente. Es mejor
crear roles semánticos reutilizables.

### Paso 5. Configurar tipografía

En la sección de tipografía se pueden revisar:

-   Fuente principal.
-   Tamaños de texto.
-   Títulos.
-   Encabezados.
-   Etiquetas.
-   Texto de cuerpo.

La tipografía debe ser coherente con la jerarquía visual de la
aplicación.

### Paso 6. Previsualizar el tema

Comprueba:

-   Tema claro.
-   Tema oscuro.
-   Botones.
-   Campos de texto.
-   Tarjetas.
-   Estados seleccionados.
-   Estados deshabilitados.
-   Legibilidad del texto.
-   Contraste.

### Paso 7. Exportar

1.  Abre la opción `Export`.
2.  Selecciona la plataforma **Android Views (XML)**, si está
    disponible.
3.  Genera y descarga el paquete de recursos.
4.  Descomprime el archivo.
5.  Revisa su estructura.
6.  Copia los recursos a `app/src/main/res/`.

El paquete exportado puede incluir carpetas como:

``` text
values/
values-night/
```

Y archivos como:

``` text
colors.xml
themes.xml
styles.xml
attrs.xml
```

El contenido exacto depende de la versión de la herramienta y de las
opciones elegidas.

## 16.3. Importar el tema en Android Studio

Después de descomprimir el paquete:

1.  Abre el proyecto Android.
2.  Localiza `app/src/main/res/`.
3.  Copia o fusiona las carpetas `values` y `values-night`.
4.  Revisa los nombres de los recursos.
5.  Comprueba el nombre del tema generado.
6.  Cambia el `AndroidManifest.xml` para utilizar el tema correcto.
7.  Compila el proyecto.
8.  Ejecuta la aplicación en el emulador.

Ejemplo:

``` xml
<application
    android:theme="@style/AppTheme">
```

Si el tema generado se llama `AppTheme`, se puede utilizar ese nombre o
renombrarlo de forma coherente con el proyecto:

``` xml
<application
    android:theme="@style/Theme.MiApp">
```

Si se cambia el nombre, deben actualizarse las referencias
correspondientes en los archivos XML.

## 16.4. Revisar los archivos exportados

No se debe copiar el código generado sin comprenderlo. El alumno debe
identificar:

  -----------------------------------------------------------------------
  Archivo                             Qué revisar
  ----------------------------------- -----------------------------------
  `colors.xml`                        Colores base y recursos generados.

  `themes.xml`                        Temas claro y sus atributos.

  `values-night/themes.xml`           Tema oscuro.

  `attrs.xml`                         Atributos adicionales,
                                      especialmente colores
                                      personalizados.

  `styles.xml`                        Estilos y apariencias de
                                      componentes.
  -----------------------------------------------------------------------

Los archivos exportados pueden contener más roles de color que los
utilizados en el ejemplo manual. Esto es normal en Material 3.

## 16.5. Advertencias sobre la exportación

-   La herramienta puede cambiar con el tiempo.
-   Los nombres de archivos y roles pueden variar entre versiones.
-   Hay que seleccionar **Android Views (XML)**, no únicamente Compose.
-   El tema exportado puede requerir una versión compatible de Material
    Components.
-   Debe revisarse el nombre del tema en el `AndroidManifest.xml`.
-   Los recursos generados no sustituyen la revisión de accesibilidad.
-   Conviene hacer una copia de seguridad antes de fusionar archivos
    `values`.

------------------------------------------------------------------------

# 17. Diferencia entre XML Views y Compose

Material 3 se puede utilizar en dos enfoques diferentes:

## Android Views con XML

Utiliza:

-   `themes.xml`.
-   `colors.xml`.
-   `styles.xml`.
-   `shape`.
-   `selector`.
-   `MaterialButton`.
-   `TextInputLayout`.
-   `MaterialCardView`.

Los componentes reciben atributos del tema mediante XML.

Ejemplo:

``` xml
android:textColor="?attr/colorOnSurface"
```

## Jetpack Compose

Utiliza objetos Kotlin como:

-   `ColorScheme`.
-   `Typography`.
-   `Shapes`.
-   `MaterialTheme`.

Ejemplo conceptual:

``` kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = typography,
    shapes = shapes
) {
    // Interfaz Compose
}
```

El tema XML de Views y el tema de Compose no son exactamente el mismo
sistema. Si una aplicación combina ambos enfoques, hay que planificar
cómo se comparten o sincronizan los recursos.

------------------------------------------------------------------------

# 18. Práctica propuesta: Perfil de usuario

## Objetivo

Crear una pantalla de perfil utilizando recursos, temas, estilos y
shapes.

## Requisitos

-   [ ] Crear una paleta de colores en `colors.xml`.
-   [ ] Crear un tema personalizado.
-   [ ] Aplicar el tema a la aplicación.
-   [ ] Utilizar atributos de tema en los componentes.
-   [ ] Crear un `shape` para una tarjeta.
-   [ ] Crear un `shape` para un botón.
-   [ ] Crear un estilo reutilizable para títulos.
-   [ ] Crear recursos alternativos para modo oscuro.
-   [ ] Generar una variante del tema con Material Theme Builder.
-   [ ] Comparar el tema manual con el tema exportado.
-   [ ] Probar la aplicación en el emulador.

## Reto adicional

Crear un botón que cambie visualmente cuando:

-   Está habilitado.
-   Está deshabilitado.
-   Está pulsado.

## Entrega

El alumno debe entregar:

``` text
proyecto/
├── colors.xml
├── themes.xml
├── styles.xml
├── bg_button.xml
├── bg_card.xml
├── selector o ripple
└── capturas de pantalla
```

------------------------------------------------------------------------

# 19. Resumen

  Concepto                 Idea clave
  ------------------------ ----------------------------------------
  `colors.xml`             Centraliza los colores.
  `themes.xml`             Define el tema global.
  `styles.xml`             Agrupa atributos reutilizables.
  `?attr/...`              Consulta atributos del tema actual.
  `@color/...`             Accede a un recurso de color.
  `shape`                  Crea fondos y formas XML.
  `selector`               Cambia recursos según el estado.
  `values-night`           Permite recursos para modo oscuro.
  Material Theme Builder   Genera esquemas y recursos Material 3.

## Idea fundamental

Un buen diseño XML no consiste en introducir colores y dimensiones
directamente en cada vista. Consiste en construir un sistema
reutilizable de:

``` text
Paleta de colores
        ↓
Tema
        ↓
Estilos y atributos
        ↓
Shapes y estados
        ↓
Layouts XML
```

------------------------------------------------------------------------

## Fuentes y documentación

-   Material Theme Builder: https://m3.material.io/theme-builder/
-   Material Theme Builder en GitHub:
    https://github.com/material-foundation/material-theme-builder
-   Android Developers: Material 3 y tematización:
    https://developer.android.com/codelabs/m3-design-theming
-   Material Components for Android:
    https://github.com/material-components/material-components-android
-   Google Codelab sobre armonización de colores en Android Views:
    https://codelabs.developers.google.com/harmonize-color-android-views
