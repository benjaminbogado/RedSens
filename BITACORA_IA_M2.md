# Bitácora de uso de inteligencia artificial — M2 (ABB)

Proyecto: RedSens.
Materia: Taller de Algoritmos y Estructuras de Datos II.
Responsable de este registro: Tobias Zapiola.
Grupo: 3.
Semilla asignada: 3026.
Herramienta utilizada: Claude Code (Anthropic).

Este documento registra la asistencia correspondiente al árbol binario
de búsqueda del M2 y su experimento. Debe integrarse con los registros
de los demás integrantes para elaborar la bitácora general del grupo.

## 1. Herramienta

Se utilizó Claude Code, un asistente de programación que trabaja desde
la terminal sobre el repositorio del proyecto.

## 2. Tareas en las que se usó

| Tarea | Qué hizo la IA | Qué hicimos nosotros |
|---|---|---|
| Implementación del ABB (`ArbolBinarioBusqueda`) | Generó insertar, buscar, eliminar (3 casos), recorridos, altura, nodos, hojas, rango y críticas. | Revisamos que respetara el contrato común con el AVL (altura en aristas, sin duplicados, `null` si no existe) y lo pedido en el enunciado. |
| Pruebas (`PruebaABB`) | Generó las 57 verificaciones: árbol de ejemplo, eliminación, datos ordenados y aleatorios. | Ejecutamos las pruebas y revisamos que los valores esperados correspondan a lo que pide el enunciado. |
| Experimento M2 (`ExperimentoArboles`) e informe de rendimiento | Generó el experimento ABB vs AVL y el borrador del informe con tablas y gráficos. | Corrimos el experimento con la semilla del grupo y verificamos que las alturas coincidan con la teoría (ABB ascendente = n − 1, AVL ≤ 1,44·log₂ n). |
| Preparación de la exposición | Explicó los conceptos (recursión, O grande, rotaciones, tabla hash), armó resúmenes, el speech y una práctica en JShell. | Hicimos preguntas hasta entender cada punto, practicamos la ejecución en la terminal y ajustamos el speech al tiempo y a lo que pide la exposición 1. |

## 3. Errores de la asistencia y corrección

### 3.1 Uso de `TreeMap`, prohibido en M2

En `PruebaABB`, la IA usó un `TreeMap` como referencia para comprobar
los resultados del árbol. El enunciado prohíbe `HashMap`, `TreeMap` y
librerías externas en M1, M2 y M3. Lo detecté al revisar el código
contra el enunciado.

Corrección: se reemplazó por un arreglo de 10.000 posiciones indexado
por número de estación. Recorrerlo de principio a fin da las estaciones
ya ordenadas, así que sirve como referencia sin estructuras prohibidas.
Las 57 pruebas siguieron pasando (commit `73ecade`).

### 3.2 Semilla de otro grupo

La prueba con datos aleatorios usaba la semilla `2026`, que según la
tabla de la sección 3 del enunciado corresponde al grupo 2. La del
grupo 3 es `3026`. Lo detecté en la misma revisión.

Corrección: se cambió a `3026`, con un comentario que indica de dónde
sale (commit `73ecade`).

### 3.3 Casos base sin identificar

El enunciado pide identificar en el código el caso base de cada método
recursivo. El código generado funcionaba, pero no lo indicaba. Lo
detecté en la misma revisión.

Corrección: se agregó un comentario `// Caso base: …` en cada método
recursivo del ABB (commit `73ecade`).

### 3.4 Instrucciones de JShell que fallaban al pegarlas juntas

Para practicar la demo, la IA dio un bloque de instrucciones de JShell
con expresiones que no terminaban en `;`. Al pegar el bloque completo,
JShell respondía `';' expected` y no ejecutaba el resto. Lo detecté al
probarlo en la terminal.

Corrección: se reescribió la práctica con todas las instrucciones
terminadas en `;` e imprimiendo cada resultado.

## 4. Partes escritas o modificadas por nosotros

- Las correcciones de los puntos 3.1 a 3.3: referencia sin `TreeMap`,
  semilla del grupo y marcas de caso base.
- La decisión de diseño de usar el AVL como índice del sistema, a partir
  del experimento (con 10.000 estaciones ordenadas: altura 9.999 del ABB
  contra 13 del AVL).
- El speech de la exposición, reescrito con nuestras palabras y ajustado
  a 2:30.

## 5. Control de lo entregado

No se aceptó código sin ejecutarlo: se corrieron `PruebaABB`
(57 verificaciones OK), `ComprobadorArbolAVL` y el experimento con la
semilla 3026, y se compararon los resultados con la teoría de las
lecturas. El código se revisó contra el enunciado punto por punto, y
así aparecieron los errores 3.1 a 3.3.
