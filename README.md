# Pong — Java Swing y concurrencia

Proyecto didáctico que implementa una versión sencilla de **Pong para un jugador**. Una pelota se desplaza y rebota dentro de un tablero; el usuario mueve una raqueta horizontal para interceptarla. El ejemplo combina **Swing**, **Java 2D**, manejo del teclado y **dos hilos de ejecución**.

> **Estado:** prototipo funcional con limitaciones. No implementa puntuación, vidas, adversario ni condición de victoria o derrota. El rebote en el borde inferior ocurre incluso cuando la pelota no toca la raqueta.

## Funcionalidades

- Dibujo de pelota roja (`Ellipse2D.Double`) y raqueta negra (`Rectangle2D.Double`).
- Movimiento automático de la pelota con rebotes en bordes y detección de intersección con la raqueta.
- Movimiento discreto de la raqueta mediante flechas.
- Pausa y reanudación mediante teclado.
- Ventana Swing de tamaño preferido **640 × 480 píxeles**; la lógica de colisiones usa límites fijos de 640 × 480.

## Requisitos y herramientas

- JDK compatible con el proyecto; **compilación comprobada con OpenJDK 21**.
- Gradle Wrapper incluido, configurado para **Gradle 9.8.2** (`gradle/wrapper/gradle-wrapper.properties`). Para usar ese wrapper, se requiere una versión de Java compatible con Gradle 9.8.2.
- Entorno gráfico de escritorio para ejecutar Swing.
- Dependencias de pruebas declaradas en `build.gradle`: **JUnit Jupiter 6.0.0** mediante BOM y `junit-platform-launcher`. No se encontraron pruebas en `src/test`.

No se necesitan bibliotecas externas para ejecutar la aplicación principal.

## Estructura

```text
pong_project/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
└── src/main/java/mx/unison/pong/
    ├── Main.java
    ├── Board.java
    ├── BallRunner.java
    └── Paddle.java
```

## Clases y responsabilidades

### `Main.java`

Punto de entrada (`main`). Usa `SwingUtilities.invokeLater(...)` para crear la interfaz en el **Event Dispatch Thread (EDT)**. `createAndShowGUI()` crea un `JFrame` titulado *Pong*, inserta un `Board` dentro de un `JPanel` con `BorderLayout`, registra el `KeyListener` del tablero en la ventana, ajusta el tamaño con `pack()` y muestra la ventana. El cierre utiliza `JFrame.EXIT_ON_CLOSE`.

### `Board.java`

Componente gráfico `JComponent` que implementa `Runnable` y `KeyListener`.

- El constructor crea la pelota (`Ellipse2D.Double` de 20 × 20), una instancia de `Paddle` y un `BallRunner`. Inicia **dos hilos**: el de movimiento de la pelota y otro que ejecuta `Board.run()`.
- `paintComponent(Graphics)` borra el fondo, muestra el texto `Paused` cuando corresponde y dibuja la pelota y la raqueta con `Graphics2D`.
- `run()` solicita `repaint()` cada 100 ms, sin condición de salida.
- `keyPressed(KeyEvent)` interpreta flechas izquierda/derecha, espacio y `R`.
- `getPreferredSize()` devuelve 640 × 480 mientras `preferredSize` sea `null`; de otro modo delega a la superclase.

### `BallRunner.java`

Implementa `Runnable` y actualiza la posición de la pelota. Sus constantes principales son `DX = 20`, `DY = 20`, `MAX_X = 640`, `MAX_Y = 480` y `SIGN = -1`.

- El constructor recibe una figura `Ellipse2D.Double` y una `Paddle`, posicionando inicialmente la pelota en `(320, 240)`.
- `run()` modifica la posición según las direcciones horizontal y vertical, comprueba la intersección con la raqueta y los límites del tablero y, normalmente, duerme 150 ms por iteración.
- `pause()` y `resume()` modifican el estado de pausa.
- `stop()` solicita terminar el ciclo, pero **no se invoca desde el código existente**.

La implementación contiene un `continue` al detectar colisión con la raqueta: esa ruta **omite el `sleep(150)`**. Durante la pausa se llama a `Thread.yield()` repetidamente, sin espera bloqueante.

### `Paddle.java`

Encapsula una raqueta rectangular de **80 × 30 píxeles**, ubicada inicialmente en `(320, 450)`.

- `moveLeft()` y `moveRight()` desplazan la raqueta entre ocho posiciones discretas, separadas 80 píxeles, con métodos `synchronized`.
- `getPaddle()` devuelve la referencia al rectángulo mutable.
- `check(int, int)` comprueba si un punto pertenece al rectángulo.
- `intersects(RectangularShape)` comprueba si otra figura rectangular se superpone a la raqueta, utilizando sus límites.

## Controles

| Tecla | Acción |
|---|---|
| `←` | Mover la raqueta 80 px a la izquierda |
| `→` | Mover la raqueta 80 px a la derecha |
| `Espacio` | Pausar el movimiento |
| `R` | Reanudar el movimiento |

Los controles dependen del foco de teclado de la ventana. No hay botones de inicio, reinicio o salida en la interfaz.

## Compilar y ejecutar

Desde la raíz del proyecto:

**Linux/macOS**

```bash
./gradlew build
```

**Windows**

```powershell
.\gradlew.bat build
```

El proyecto aplica el plugin Gradle `java`, pero **no declara el plugin `application` ni una tarea `run`**. Para ejecutar el programa sin modificar `build.gradle`, una vez compilado:

**Linux/macOS**

```bash
java -cp build/classes/java/main mx.unison.pong.Main
```

**Windows**

```powershell
java -cp build\classes\java\main mx.unison.pong.Main
```

También puede abrirse el proyecto en IntelliJ IDEA y ejecutarse el método `main` de `mx.unison.pong.Main`.

### Compilación directa con `javac`

Desde la raíz del repositorio:

```bash
mkdir -p out
javac -d out src/main/java/mx/unison/pong/*.java
java -cp out mx.unison.pong.Main
```

En Windows puede compilarse desde IntelliJ IDEA o adaptar el patrón de archivos al shell utilizado.

## Flujo de ejecución

```text
Main.main()
  └─ SwingUtilities.invokeLater(...)
      └─ createAndShowGUI()
          ├─ JFrame + Board
          └─ Board()
              ├─ Paddle + Ellipse2D
              ├─ Thread(BallRunner).start()
              │    └─ actualiza posición y detecta colisiones
              └─ Thread(Board).start()
                   └─ solicita repaint() periódicamente

EDT ──> Board.paintComponent(...) ──> dibuja pelota y raqueta
Teclado ──> Board.keyPressed(...) ──> mueve raqueta / pausa
```

## Limitaciones 

1. No hay sistema de puntuación ni detección de pérdida de pelota; el borde inferior actúa como pared.
2. Las dimensiones de colisión están fijadas a 640 × 480 y no se ajustan al área interior real del componente.
3. La pelota y la raqueta son objetos mutables compartidos entre hilos; el renderizado no obtiene instantáneas sincronizadas.
4. Los estados `isPaused` y `stop` de `BallRunner` no son `volatile` ni están protegidos por un mecanismo de sincronización.
5. La pausa puede provocar consumo innecesario de CPU y la colisión con la raqueta puede desencadenar iteraciones sin retardo.
6. No se dispone de un mecanismo completo para detener y liberar los hilos al finalizar el juego.
7. No hay pruebas automatizadas en el proyecto proporcionado.

## Posibles extensiones didácticas

- Sustituir los dos hilos por un `javax.swing.Timer` y comparar modelos de concurrencia.
- Implementar una máquina de estados (`RUNNING`, `PAUSED`, `GAME_OVER`).
- Añadir puntuación, vidas y reinicio.
- Calcular las colisiones usando las dimensiones reales del tablero y velocidades continuas.
- Crear pruebas JUnit para movimiento, rebotes y límites de la raqueta.
