# Bitácora de uso de inteligencia artificial — M1 cuadrática

Proyecto: RedSens.
Materia: Taller de Algoritmos y Estructuras de Datos II.
Responsable de este registro: Franco Bossio (integrante 4).
Grupo: 3.
Semilla asignada: 3026. Capacidad inicial de la tabla: 17.
Herramienta utilizada: Claude (Anthropic).

## 1. Objetivo y alcance

Se utilizó inteligencia artificial como asistencia técnica para la segunda
parte de M1: exploración cuadrática, control del factor de carga, rehashing
y comparación de las dos estrategias.

Se le entregaron a la herramienta el enunciado, el roadmap del equipo y el
repositorio. La herramienta propuso el código Java, las pruebas, el
experimento y el informe. El código se compiló y ejecutó para comprobar su
comportamiento, y se integró sobre la tabla hash lineal de FEDE (PR #5)
respetando su contrato.

Este documento registra la asistencia correspondiente a M1 cuadrática.
Debe integrarse con los registros de los demás integrantes para elaborar
la bitácora general del grupo.

## 2. Tareas asistidas

- Lectura del enunciado, del roadmap y del contrato de M1 lineal para
  identificar qué correspondía al integrante 4.
- Implementación de `CUADRATICA` en `EstrategiaExploracion` (h + i²).
- Clase `Primos` para calcular el siguiente primo.
- Modo ampliable de `TablaHash`: umbral por estrategia, rehashing al primer
  primo mayor o igual al doble, marcas de borrado contadas para el umbral y
  limpieza sin crecer.
- Justificación de los umbrales: 0,5 para cuadrática (con capacidad prima,
  las primeras ⌈m/2⌉ posiciones de la secuencia son distintas) y 0,75 para
  lineal (control del costo por agrupamiento primario).
- Pruebas (`PruebaTablaHashCuadratica`, 96 verificaciones) y experimento
  obligatorio con 10.000 estaciones de `GeneradorDatos` (M6).
- Informe de resultados y explicación del agrupamiento primario.
- Uso de Git: creación de la rama, aplicación de los cambios y Pull Request.

## 3. Errores y sugerencias incorrectas detectadas

**3.1 Tabla hash duplicada.** En la primera versión, como la rama de FEDE
todavía no estaba publicada, la herramienta construyó una `TablaHash`
completa propia, con la lineal incluida. Eso duplicaba el trabajo del
integrante 3. Al publicarse la rama de FEDE se descartó esa versión y se
rehízo la parte cuadrática sobre su clase. Al adaptarla aparecieron
diferencias de interfaz: el orden de los parámetros del constructor era
distinto y `eliminar` no devuelve la estación. La prueba que esperaba un
valor de retorno no compiló y se corrigió.

**3.2 Modificaciones innecesarias al código de otro integrante.** En la
primera integración, la herramienta cambió comentarios y el README de
FEDE, puso los umbrales dentro de su enum y cambió la visibilidad de su
función hash para usarla en las pruebas. Se pidió no modificar el trabajo
de otros integrantes. Se revirtió todo y la versión final solo agrega
código. De lo existente cambia únicamente `final` en el arreglo de
casillas, indispensable para que la tabla pueda crecer.

**3.3 Hallazgo verificado con mediciones.** Al comparar los sondeos medidos
con las fórmulas teóricas, la exploración lineal resultó mucho peor que lo
esperado con carga alta (λ = 0,9: 199 sondeos en búsqueda fallida contra
50,5 de la teoría). La causa es que el hash de Horner ubica códigos
consecutivos en casillas consecutivas. No se modificó el hash, porque
corresponde a M1 lineal: se dejó planteado en el Pull Request para
decidirlo con el integrante 3.

## 4. Partes escritas o modificadas por el integrante

El código, las pruebas y el informe fueron propuestos por la herramienta.
El trabajo propio consistió en dirigir y controlar esa asistencia:

- Delimitación del alcance: se le indicó a la herramienta qué tarea
  correspondía al integrante 4 según el reparto del equipo, y se le pidió
  verificar que lo entregado no excediera esa parte.
- Decisión de no modificar el trabajo de otros integrantes. A partir de
  ese pedido se revirtieron los cambios sobre el código y el README de
  FEDE (punto 3.2).
- Coordinación con el integrante 3: se acordó con FEDE crear la rama de
  M1 cuadrática a partir de su trabajo, y se esperó a que publicara su
  Pull Request antes de integrar la parte cuadrática.
- Control de cumplimiento: se le pidió a la herramienta contrastar el
  resultado con el enunciado, el roadmap y el contrato de M1 lineal.
- Trabajo con Git: clonado del repositorio, creación de la rama
  `feat/tabla-hash-cuadratica`, aplicación de los cambios, verificación
  del historial con `git log`, gestión del acceso al repositorio con el
  coordinador, publicación de la rama y apertura del Pull Request.

Pendiente antes de la exposición: repasar el código línea por línea y
practicar a mano una traza de exploración cuadrática y de rehashing.

## 5. Verificación

- `PruebaTablaHashCuadratica`: 96 verificaciones OK.
- `PruebaTablaHash` (M1 lineal), `PruebaABB`, `ComprobadorArbolAVL` y
  `ComprobadorM6`: sin fallas después de integrar los cambios.
- Compilación con `--release 17`, como el resto del proyecto.
