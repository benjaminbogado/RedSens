# RedSens · Documentación de M3, M4 y M5

Resumen breve de los módulos de la segunda etapa, armado a partir de lo que entregó cada
responsable. El detalle completo está en los documentos de cada módulo.

| Módulo | Responsable | Estado (21/11) | Detalle |
|---|---|---|---|
| M3 · Red vial | Franco Bossio | Implementado, en revisión: PR #11 (`feat/grafo-red`) | `resultados/informe-m3-grafos.md` (en la rama) |
| M4 · Transmisión | Luca Agustín Pepino | Implementado, en revisión: PR #12 (`feat/huffman`) | `README_M4.md` (en la rama) |
| M5 · Guardias | A definir | Sin implementar | — |

Mientras los PR no se integren, M3 y M4 no están en `main`. En el menú, las opciones 3 y 4 siguen
diciendo "todavía no está disponible".

## M3 · Red vial y planificación de recorridos

**Problema:** las cuadrillas salen a cambiar baterías sin criterio de recorrido.

**Qué hace:** representa las 12 localidades y los 18 tramos del enunciado como un grafo no dirigido
y ponderado en km. El grafo se carga desde `datos/red_vial.txt`, no está escrito en el código.

| Clase (`redsens.red`) | Responsabilidad |
|---|---|
| `GrafoLocalidades`, `Ruta` | Grafo con matriz y lista de adyacencia sincronizadas |
| `CargadorGrafo` | Lectura del archivo, con errores que indican la línea |
| `Recorridos` | BFS por niveles y DFS, con lista y con matriz; conexidad |
| `CaminosMinimos` | Dijkstra con camino completo y Floyd para todos los pares |
| `CorteRuta` | Simula el corte de un tramo e informa localidades aisladas y caminos que empeoran |
| `PlanVisitas` | Vecino más cercano sobre la tabla de Floyd, desde la base y volviendo a ella |

**Decisiones:** la lista de adyacencia conviene porque el grafo es disperso (18 tramos de 66 pares
posibles); la matriz se mantiene porque es la entrada natural de Floyd. No usa `HashMap`, `TreeMap`
ni librerías de grafos.

**Integración:** opción 3 del menú. La opción 9 de ese submenú arma el plan de visitas con el
reporte de estaciones críticas del AVL (M2), como pide el enunciado.

**Resultados informados por el responsable:**
- 82 verificaciones en `PruebaGrafo`, todas correctas.
- BFS desde San Francisco: a 1 tramo, Córdoba y Villa María.
- Dijkstra desde San Francisco: Villa Dolores es la más lejana, a 381 km.
- La red es conexa y ningún corte de un solo tramo la desconecta. Para aislar San Francisco hay que
  cortar sus dos salidas.
- Plan con las 12 localidades: 1.354 km con vecino más cercano contra 1.215 km del óptimo (11,4 % más).
  Muestra por qué el criterio voraz no garantiza el recorrido óptimo.

## M4 · Transmisión comprimida (Huffman)

**Problema:** el archivo diario de mediciones viaja sin comprimir por enlaces lentos.

**Qué hace:** comprime y descomprime archivos sin perder información, con el algoritmo de Huffman.

| Clase (`redsens.compresion`) | Responsabilidad |
|---|---|
| `Nodo` | Nodo hoja o interno del árbol |
| `Huffman` | Frecuencias, árbol, tabla de códigos, verificación de prefijos, codificar y decodificar |
| `ArchivoHuffman` | Formato binario comprimido (`RHUF`) y descompresión, con rechazo de archivos inválidos |
| `TransmisionComprimida` | Interfaz de integración |
| `DemostracionHuffman` | Demostración que usa el menú |

**Decisiones:** cola de prioridad (`PriorityQueue`) y tabla de códigos en `HashMap`, ambos
permitidos por el enunciado para M4. Los empates se resuelven en forma determinista, así que el
árbol sale siempre igual.

**Integración:** opción 4 del menú.

**Resultados informados por el responsable:**
- Caso de verificación `WWXWYZWX`: 64 bits originales y **14 bits** con Huffman, sin prefijos repetidos.
- Archivo de 5.000 mediciones: 262.001 bytes originales y 141.755 bytes comprimidos (cabecera
  incluida), un **ahorro de 45,90 %**.
- El archivo recuperado es idéntico byte por byte al original, incluso descomprimido en otro proceso.

**¿Por qué no comprime más?** El archivo usa pocos símbolos, pero los dígitos aparecen con
frecuencias parecidas (entre unas 6.600 y 22.300 veces cada uno) y ninguno domina, así que sus
códigos no pueden ser muy cortos (de 3 a 5 bits). Un texto con un carácter muy repetido comprime
mucho más.

## M5 · Guardias rotativas (Josephus)

**Estado:** sin implementar. Esto es lo que pide el enunciado y lo que hay que entregar:

- Solución simple con lista enlazada e iterador, O(N·M).
- Solución eficiente con un ABB que permita obtener el k-ésimo elemento, O(N log N).
- Calendario de guardias para N = 12 técnicos con M = 3 (parámetro del grupo); el último que queda
  es el suplente.
- Caso de verificación de la lectura: con N = 5 y M = 1 salen 2, 4, 1, 5 y queda el 3.
- Experimento: tiempos de las dos versiones para N = 1.000, 10.000 y 100.000 con al menos dos
  valores de M. Sus resultados van en la sección 4 de `resultados/informe-rendimiento.md`.
