# 📚 PROBLEMAS DE HILOS Y SINCRONIZACIÓN - 2º DAM

## 📋 **ÍNDICE DE PROBLEMAS**

1. [Problema 1: El Contador Compartido](#problema-1-el-contador-compartido) - *Nivel: Básico*
2. [Problema 2: El Banco Virtual](#problema-2-el-banco-virtual) - *Nivel: Intermedio*
3. [Problema 3: El Restaurante Concurrente](#problema-3-el-restaurante-concurrente) - *Nivel: Intermedio-Avanzado*
4. [Problema 4: El Parking Inteligente](#problema-4-el-parking-inteligente) - *Nivel: Avanzado*
5. [Problema 5: El Sistema de Descargas](#problema-5-el-sistema-de-descargas) - *Nivel: Avanzado*
6. [Problema Final: La Red Social](#problema-integrador-final-la-red-social) - *Nivel: Proyecto Final*

---

## 🎯 **PROBLEMA 1: El Contador Compartido**
**Nivel:** Básico | **Tiempo estimado:** 2-3 horas

### **Enunciado:**
Crea una aplicación que simule un **contador de visitas web**. Tu aplicación debe:

#### **Funcionalidades requeridas:**
- Una clase `ContadorVisitas` con un método `incrementarVisita()`
- Crear **1000 hilos** que representen usuarios visitando la página web
- Cada hilo debe incrementar el contador **una sola vez**
- Cada hilo debe esperar un tiempo aleatorio entre **50-150ms** antes de incrementar

#### **Implementaciones requeridas:**
1. **Sin sincronización** - Para mostrar el problema de condiciones de carrera
2. **Con `synchronized`** - Usando palabra clave synchronized
3. **Con `AtomicInteger`** - Usando clases atómicas

#### **Salida esperada:**
```
=== CONTADOR DE VISITAS WEB ===
Esperando 1000 visitantes...

--- SIN SINCRONIZACIÓN ---
Visitas esperadas: 1000
Visitas contadas: 847  ❌ INCORRECTO
Tiempo: 152ms

--- CON SYNCHRONIZED ---
Visitas esperadas: 1000
Visitas contadas: 1000 ✅ CORRECTO
Tiempo: 167ms

--- CON ATOMICINTEGER ---
Visitas esperadas: 1000
Visitas contadas: 1000 ✅ CORRECTO
Tiempo: 149ms
```

#### **Objetivos de aprendizaje:**
- Entender qué son las condiciones de carrera
- Aprender diferentes formas de sincronización
- Comparar rendimiento entre diferentes técnicas

---

## 🏦 **PROBLEMA 2: El Banco Virtual**
**Nivel:** Intermedio | **Tiempo estimado:** 4-5 horas

### **Enunciado:**
Simula un **sistema bancario** donde múltiples clientes realizan operaciones concurrentes sobre una cuenta bancaria.

#### **Especificaciones técnicas:**
- **Saldo inicial:** 10.000€
- **Clientes simultáneos:** 50 hilos
- **Operaciones por cliente:** 10 operaciones aleatorias cada uno

#### **Tipos de operaciones:**
- **Retiro (60% probabilidad):** Entre 1€ y 100€
- **Ingreso (40% probabilidad):** Entre 1€ y 50€
- **Tiempo por operación:** 100-300ms (simular latencia bancaria)

#### **Restricciones:**
- El saldo **nunca puede ser negativo**
- Si no hay fondos suficientes, la operación debe fallar
- Todas las operaciones deben quedar registradas con timestamp

#### **Implementaciones requeridas:**
1. **Con `ReentrantLock`** - Usando locks explícitos
2. **Con `synchronized`** - Usando sincronización implícita
3. **Con `volatile`** - Para demostrar que NO es suficiente

#### **Clase base sugerida:**
```java
public class CuentaBancaria {
    private double saldo;
    
    public boolean retirar(double cantidad);
    public void ingresar(double cantidad);
    public double consultarSaldo();
    public List<String> obtenerHistorial();
}
```

#### **Salida esperada:**
```
=== BANCO VIRTUAL ===
Saldo inicial: 10000.00€
50 clientes realizando 500 operaciones totales...

--- CON REENTRANTLOCK ---
Saldo final: 9847.32€
Operaciones exitosas: 487/500
Operaciones fallidas: 13 (fondos insuficientes)
Tiempo total: 2.3s ✅

--- CON SYNCHRONIZED ---
Saldo final: 9847.32€
Operaciones exitosas: 487/500
Tiempo total: 2.1s ✅

--- CON VOLATILE ---
Saldo final: 8923.45€ ❌ INCORRECTO
Operaciones perdidas detectadas!
```

#### **Objetivos de aprendizaje:**
- Manejo de recursos compartidos complejos
- Diferencias entre Lock y synchronized
- Limitaciones de volatile

---

## 🍽️ **PROBLEMA 3: El Restaurante Concurrente**
**Nivel:** Intermedio-Avanzado | **Tiempo estimado:** 6-8 horas

### **Enunciado:**
Simula un **restaurante completo** con el patrón Productor-Consumidor, donde camareros toman pedidos y cocineros los preparan.

#### **Actores del sistema:**
- **👨‍🍳 Cocineros:** 3 hilos que preparan platos
- **🍽️ Mesa de pedidos:** Buffer limitado de máximo 10 pedidos
- **👨‍💼 Camareros:** 5 hilos que toman pedidos
- **👥 Clientes:** 100 clientes que llegan aleatoriamente

#### **Reglas de negocio:**
- **Tiempo de cocina:** Entre 2-5 segundos por plato
- **Tiempo por pedido:** 1 segundo por camarero
- **Mesa llena:** Los camareros deben esperar
- **Sin pedidos:** Los cocineros deben esperar
- **Llegada de clientes:** Cada 500ms llega un cliente nuevo

#### **Tipos de platos:**
```java
enum TipoPlato {
    ENSALADA(2000),    // 2 segundos
    PASTA(3000),       // 3 segundos  
    PIZZA(4000),       // 4 segundos
    CARNE(5000);       // 5 segundos
    
    private final int tiempoMs;
}
```

#### **Implementación requerida:**
- Usar **`BlockingQueue`** para la mesa de pedidos
- Usar **hilos virtuales** para los clientes
- Generar estadísticas en tiempo real

#### **Salida esperada:**
```
=== RESTAURANTE CONCURRENTE ===
Iniciando servicio con 3 cocineros y 5 camareros...

[14:32:15] Cliente-001 pide PIZZA
[14:32:15] Camarero-2 toma pedido PIZZA
[14:32:16] Cliente-002 pide ENSALADA
[14:32:19] Cocinero-1 termina PIZZA para Cliente-001
[14:32:20] Cliente-003 pide CARNE

--- ESTADÍSTICAS FINALES ---
Clientes atendidos: 100/100 ✅
Platos servidos: 100
Tiempo promedio de espera: 3.2s
Mesa llena (veces): 12
Cocineros esperando (tiempo): 45s total
Eficiencia: 85%
```

#### **Objetivos de aprendizaje:**
- Patrón Productor-Consumidor
- BlockingQueue y sus implementaciones
- Coordinación entre múltiples tipos de hilos

---

## 🅿️ **PROBLEMA 4: El Parking Inteligente**
**Nivel:** Avanzado | **Tiempo estimado:** 8-10 horas

### **Enunciado:**
Desarrolla un **sistema de gestión de parking** con control de acceso, diferentes tipos de plazas y sistema de colas.

#### **Infraestructura del parking:**
- **🚗 Plazas normales:** 20 plazas
- **⭐ Plazas VIP:** 5 plazas (coches VIP pueden usar cualquiera)
- **🚪 Barrera entrada:** Procesa 1 coche cada 2 segundos
- **🚪 Barrera salida:** Procesa 1 coche cada 1 segundo
- **⏱️ Tiempo de estancia:** 10-30 segundos aleatorio

#### **Tipos de vehículos:**
```java
enum TipoVehiculo {
    NORMAL(1.0),  // 1€/minuto
    VIP(2.0);     // 2€/minuto
    
    private final double tarifaPorMinuto;
}
```

#### **Reglas de negocio:**
- **Clientes VIP:** Pueden usar cualquier plaza libre
- **Clientes normales:** Solo plazas normales
- **Cola de espera:** Máximo 10 coches esperando
- **Si parking + cola llenos:** Los coches se van

#### **Implementación técnica:**
- Usar **`Semaphore`** para controlar acceso a plazas
- Usar **hilos virtuales** para simular 200 coches
- **Dashboard en tiempo real** mostrando estado del parking

#### **Dashboard esperado:**
```
=== PARKING INTELIGENTE ===
🅿️  Estado actual: [15:45:23]
┌─────────────────────────────────┐
│ PLAZAS NORMALES: ████████░░ 16/20│
│ PLAZAS VIP:     █████       5/5 │
│ COLA DE ESPERA: ████░░░     4/10│
│ INGRESOS HOY:              245€ │
└─────────────────────────────────┘

Últimos eventos:
[15:45:20] 🚗 Coche-147 (NORMAL) entra - Plaza N-12
[15:45:22] ⭐ Coche-089 (VIP) sale - Plaza V-3 - Pagó: 4.50€
[15:45:23] 🚗 Coche-156 (NORMAL) esperando en cola
```

#### **Estadísticas finales:**
```
--- RESUMEN DEL DÍA ---
Vehículos procesados: 200
Vehículos atendidos: 187 (93.5%)
Vehículos rechazados: 13 (parking+cola llenos)
Tiempo promedio de estancia: 18.3s
Ingresos totales: 312.45€
Ocupación máxima: 25/25 plazas (100%)
```

#### **Objetivos de aprendizaje:**
- Uso avanzado de Semáforos
- Gestión de recursos limitados múltiples
- Interfaces de usuario en tiempo real
- Hilos virtuales a gran escala

---

## 📥 **PROBLEMA 5: El Sistema de Descargas**
**Nivel:** Avanzado | **Tiempo estimado:** 8-12 horas

### **Enunciado:**
Crea un **gestor de descargas multihilo** que simule un servidor con capacidad limitada atendiendo múltiples clientes concurrentes.

#### **Arquitectura del sistema:**
- **🖥️ Servidor:** Máximo 5 descargas simultáneas
- **📁 Archivos disponibles:** 100 archivos de diferentes tamaños
- **👥 Clientes:** 50 usuarios intentando descargar
- **⚡ Velocidad:** Simulada según tamaño del archivo

#### **Características de los archivos:**
```java
enum TipoArchivo {
    DOCUMENTO(10),    // 10MB  -> 1s descarga
    IMAGEN(50),       // 50MB  -> 5s descarga  
    VIDEO(500),       // 500MB -> 50s descarga
    JUEGO(2000);      // 2GB   -> 200s descarga
    
    private final int sizeMB;
}
```

#### **Funcionalidades avanzadas:**
- **Reintentos automáticos:** Máximo 3 intentos si falla
- **Barras de progreso** para cada descarga activa
- **Pausar/Reanudar** descargas
- **Límite de ancho de banda** por cliente
- **Sistema de prioridades** (usuarios premium)

#### **Implementación técnica:**
- **ThreadPool limitado** para el servidor (5 hilos máximo)
- **Hilos virtuales** para los clientes
- **`CompletableFuture`** para descargas asíncronas
- **`ScheduledExecutorService`** para actualizaciones de progreso

#### **Interfaz de usuario:**
```
=== GESTOR DE DESCARGAS ===
Servidor: 5/5 conexiones activas

🔄 Descargas activas:
┌──────────────────────────────────────────┐
│ Usuario-12 │ juego_2GB.zip  ████░░░  67% │
│ Usuario-08 │ video_HD.mp4   ███████░  89% │  
│ Usuario-31 │ doc_manual.pdf █████████ 100%│
│ Usuario-05 │ imagen_4K.jpg  ██░░░░░░  25% │
│ Usuario-22 │ app_mobile.apk █████░░░  71% │
└──────────────────────────────────────────┘

⏳ Cola de espera: 12 usuarios
❌ Fallos: 3 | ✅ Completadas: 28

[16:20:15] Usuario-31 completó: doc_manual.pdf
[16:20:16] Usuario-45 inicia: video_serie_S1E1.mkv
[16:20:17] Usuario-08 reintentando (2/3): video_HD.mp4
```

#### **Estadísticas finales:**
```
--- ESTADÍSTICAS DEL SERVIDOR ---
Tiempo total funcionamiento: 15m 32s
Archivos descargados: 47/50 solicitudes
Descargas exitosas: 44 (93.6%)
Descargas fallidas: 3 (reintentos agotados)
Datos transferidos: 12.3 GB
Velocidad promedio: 13.5 MB/s
Tiempo promedio por descarga: 8.7s
Usuarios premium atendidos: 15/15 (100%)
Usuarios normales atendidos: 29/32 (90.6%)
```

#### **Objetivos de aprendizaje:**
- ThreadPools y gestión de recursos del servidor
- Programación asíncrona con CompletableFuture
- Manejo de fallos y reintentos
- Interfaces de usuario complejas en consola
- Simulación de sistemas distribuidos

---

## 🌐 **PROBLEMA INTEGRADOR FINAL: La Red Social**
**Nivel:** Proyecto Final | **Tiempo estimado:** 15-20 horas

### **Enunciado:**
Desarrolla una **simulación completa de red social** que integre todos los conceptos de concurrencia aprendidos durante el curso.

#### **Arquitectura del sistema:**

##### **👥 Usuarios (1000 usuarios activos)**
- Cada usuario es un **hilo virtual** independiente
- Comportamiento aleatorio: publican, dan likes, comentan, siguen
- Diferentes tipos: usuarios normales, influencers, bots

##### **📱 Actividades disponibles:**
```java
enum TipoActividad {
    PUBLICAR_POST(2000),      // 2s para crear contenido
    DAR_LIKE(100),           // 100ms 
    COMENTAR(1500),          // 1.5s para escribir
    SEGUIR_USUARIO(200),     // 200ms
    COMPARTIR_POST(300);     // 300ms
    
    private final int tiempoMs;
}
```

##### **🔒 Restricciones y límites:**
- **Rate limiting:** Máximo 10 publicaciones por minuto por usuario
- **Base de datos:** 100ms latencia por operación + pool de 20 conexiones
- **Cache LRU:** Máximo 50 posts populares en memoria
- **Notificaciones:** Sistema asíncrono con cola

#### **Componentes técnicos requeridos:**

##### **1. Sistema de Posts**
```java
public class Post {
    private final String id;
    private final String autor;
    private final String contenido;
    private final AtomicLong likes;
    private final ConcurrentLinkedQueue<Comentario> comentarios;
    private final LocalDateTime timestamp;
}
```

##### **2. Feed personalizado**
- Usar **`ConcurrentHashMap`** para feeds por usuario
- Algoritmo de timeline basado en followers
- Cache de posts populares (más de 100 likes)

##### **3. Sistema de notificaciones**
```java
public class SistemaNotificaciones {
    private final BlockingQueue<Notificacion> colaNotificaciones;
    private final ExecutorService procesadorNotificaciones;
    
    public void enviarNotificacion(String usuario, TipoNotificacion tipo);
    public List<Notificacion> obtenerNotificaciones(String usuario);
}
```

##### **4. Base de datos simulada**
```java
public class BaseDatosSimulada {
    private final Semaphore poolConexiones; // Máximo 20 conexiones
    private final Map<String, Object> datos;
    
    public CompletableFuture<Post> guardarPost(Post post);
    public CompletableFuture<List<Post>> obtenerFeed(String usuario);
}
```

##### **5. Métricas en tiempo real**
```java
public class MetricasRedSocial {
    private final AtomicLong postsCreados;
    private final AtomicLong likesTotales;
    private final AtomicInteger usuariosActivos;
    private final ScheduledExecutorService actualizadorMetricas;
}
```

#### **Dashboard en tiempo real:**
```
=== RED SOCIAL - DASHBOARD EN VIVO ===
🕐 [18:45:30] | Uptime: 2h 15m 23s

📊 ACTIVIDAD EN TIEMPO REAL:
┌─────────────────────────────────────┐
│ 👥 Usuarios activos:     847/1000   │
│ 📝 Posts/minuto:           156      │
│ ❤️  Likes/segundo:          23      │  
│ 💬 Comentarios/minuto:      89      │
│ 🔄 Shares/minuto:           45      │
└─────────────────────────────────────┘

💾 SISTEMA:
┌─────────────────────────────────────┐
│ 🗄️  Pool BD:           18/20 en uso │
│ 🚀 Cache hits:              87.3%   │
│ 📱 Notificaciones cola:      156    │  
│ 🧠 Memoria usada:           2.1GB   │
│ ⚡ Threads virtuales:       1000    │
└─────────────────────────────────────┘

🔥 TRENDING NOW:
#JavaConcurrency (1.2K mentions)
#ViralVideo (856 likes)
@InfluencerTech (234 new followers)

📈 ÚLTIMOS EVENTOS:
[18:45:28] @Usuario_789 publicó: "Aprendiendo hilos virtuales! 🚀"
[18:45:29] Post viral alcanza 1000 likes: "Gatos programando Java"
[18:45:30] Rate limit activado para @SpamBot_001
```

#### **Casos de uso complejos:**

##### **Viralización de contenido:**
```java
// Cuando un post recibe más de 100 likes en 1 minuto
public void manejarViralizado(Post post) {
    // 1. Notificar a todos los followers del autor
    // 2. Añadir al trending topics
    // 3. Aumentar prioridad en feeds
    // 4. Activar cache especial
}
```

##### **Detección de spam:**
```java
// Si un usuario hace más de 10 acciones en 1 segundo
public void detectarSpam(String usuario) {
    // 1. Activar rate limiting agresivo
    // 2. Notificar a moderadores
    // 3. Filtrar contenido
}
```

#### **Entregables del proyecto:**

1. **📝 Código fuente completo**
   - Estructura de paquetes organizada
   - Código comentado siguiendo JavaDoc
   - Tests unitarios básicos

2. **📊 Análisis de rendimiento**
   - Comparación hilos tradicionales vs virtuales
   - Métricas de memoria y CPU
   - Gráficas de rendimiento bajo carga

3. **🐛 Informe de problemas encontrados**
   - Condiciones de carrera detectadas y solucionadas
   - Deadlocks evitados
   - Optimizaciones implementadas

4. **🎥 Demo en vivo**
   - Presentación de 10 minutos mostrando el sistema funcionando
   - Explicación de decisiones técnicas
   - Q&A sobre implementación

#### **Criterios de evaluación:**

| Criterio | Peso | Descripción |
|----------|------|-------------|
| **Funcionalidad** | 40% | El sistema hace lo que debe hacer sin errores |
| **Sincronización** | 25% | Uso correcto de hilos, no hay condiciones de carrera |
| **Rendimiento** | 20% | Uso eficiente de recursos, hilos virtuales |
| **Arquitectura** | 10% | Código limpio, patrones de diseño, organización |
| **Documentación** | 5% | README, comentarios, análisis de decisiones |

#### **Bonus (puntos extra):**
- **🎨 Interfaz gráfica** (+1 punto): JavaFX o web interface
- **🔍 Métricas avanzadas** (+0.5 puntos): JMX, Micrometer
- **🛡️ Seguridad** (+0.5 puntos): Autenticación, validación de datos
- **🌍 Distribuido** (+1 punto): Simular múltiples servidores

---

## 🎯 **CRITERIOS DE EVALUACIÓN GENERALES**

### **Rúbrica de calificación:**

#### **🟢 EXCELENTE (9-10 puntos)**
- ✅ Funcionalidad completa sin errores
- ✅ Sincronización perfecta, sin condiciones de carrera
- ✅ Uso óptimo de hilos virtuales y recursos
- ✅ Código limpio y bien documentado
- ✅ Manejo de errores robusto
- ✅ Métricas y logging implementado

#### **🟡 BIEN (7-8 puntos)**
- ✅ Funcionalidad mayormente completa
- ✅ Sincronización correcta con algún error menor
- ✅ Uso adecuado de hilos
- ✅ Código organizado
- ⚠️ Manejo básico de errores
- ⚠️ Documentación básica

#### **🟠 SUFICIENTE (5-6 puntos)**
- ⚠️ Funcionalidad básica implementada
- ⚠️ Algunos problemas de sincronización
- ⚠️ Uso correcto pero no óptimo de hilos
- ⚠️ Código funcional pero mejorable
- ❌ Manejo de errores limitado

#### **🔴 INSUFICIENTE (0-4 puntos)**
- ❌ Funcionalidad incompleta o con errores graves
- ❌ Problemas serios de concurrencia
- ❌ Mal uso de hilos y sincronización
- ❌ Código desorganizado o difícil de entender

### **📚 Recursos recomendados:**
- **Documentación oficial:** [Oracle Java Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- **Libro:** "Java Concurrency in Practice" - Brian Goetz
- **Hilos virtuales:** [JEP 444](https://openjdk.org/jeps/444)
- **Herramientas:** JVisualVM, JProfiler para análisis de rendimiento

---

**¡Buena suerte con los ejercicios! 🚀**

> **Nota para el profesor:** Estos problemas están diseñados para ser progresivos. Se recomienda resolver los primeros 2-3 problemas antes de intentar los más avanzados. Cada problema puede ser adaptado según el tiempo disponible y el nivel del grupo.
