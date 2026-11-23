# RedSens · Documentación de M3, M4 y M5

Resumen breve de los módulos de la segunda etapa, armado a partir de lo que entregó cada
responsable. El detalle completo está en los documentos de cada módulo.

| Módulo | Responsable | Estado (23/11) | Detalle |
|---|---|---|---|
| M3 · Red vial | Franco Bossio | En `main` (integrado con el PR #15) | [informe de M3](informe-m3-grafos.md) |
| M4 · Transmisión | Luca Agustín Pepino | En `main` (integrado con el PR #15) | [README de M4](../README_M4.md) |
| M5 · Guardias | Federico Villagra | Implementado, en revisión: PR #14 (`codex/modulo-5-josephus`) | `resultados/informe-m5-josephus.md` (en el PR) |

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

**Resultados:**
- `PruebaGrafo`: 88 verificaciones, todas correctas.
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

**Resultados** (`resultados/ejecucion-m4.txt`):
- Caso de verificación `WWXWYZWX`: 64 bits originales y **14 bits** con Huffman, sin prefijos repetidos.
- Archivo de 5.000 mediciones (`datos/mediciones_grupo3.txt`): 257.001 bytes originales y
  137.376 bytes comprimidos (cabecera incluida), un **ahorro de 46,55 %**.
- El archivo recuperado es idéntico byte por byte al original (mismo SHA-256).
- Una versión anterior del archivo de mediciones tenía 262.001 bytes porque usaba saltos de línea
  de Windows (CR + LF); su corrida quedó en `resultados/historico-m4-262001-bytes/`.

**¿Por qué no comprime más?** El archivo usa pocos símbolos, pero los dígitos aparecen con
frecuencias parecidas (entre unas 6.600 y 22.300 veces cada uno) y ninguno domina, así que sus
códigos no pueden ser muy cortos (de 3 a 5 bits). Un texto con un carácter muy repetido comprime
mucho más.

## M5 · Guardias rotativas (Josephus)

**Problema:** asignar las guardias de fin de semana de forma justa y verificable.

**Qué hace:** N técnicos en ronda; se omiten M y el siguiente queda de guardia y sale de la ronda.
El orden de salida es el calendario y el último que queda es el suplente.

| Clase (`redsens.guardias`) | Responsabilidad |
|---|---|
| `JosephusLista` | Solución simple con `LinkedList` e `Iterator`, O(N·M) |
| `ArbolOrdenEstadistico` | ABB propio que guarda el tamaño de cada subárbol para seleccionar y eliminar el k-ésimo |
| `JosephusArbol` | Solución eficiente sobre ese árbol, O(N log N) |
| `CalendarioGuardias` | Resultado: asignaciones en orden y suplente |
| `ExperimentoJosephus` | Experimento obligatorio de rendimiento |

**Decisiones:** el árbol se construye ya balanceado (tomando el punto medio) y después solo borra,
así que su altura nunca supera la inicial, O(log N), sin necesidad de rotaciones. No se reutiliza el
AVL de estaciones porque guarda otros datos y no sabe contar posiciones.

**Integración:** opción 5 del menú (con el PR #14): calendario del grupo, calendario con otros
parámetros y experimento.

**Resultados:**
- Caso de la lectura: N = 5 y M = 1 asigna 2, 4, 1, 5 y deja al **3** de suplente.
- Calendario del grupo (N = 12, M = 3): 4, 8, 12, 5, 10, 3, 11, 7, 6, 9, 2, con el técnico **1** de suplente.
- `PruebaJosephus`: 34.745 verificaciones correctas.
- Experimento: con M = 1.000 el árbol es entre 12 y 33 veces más rápido; con M = 3 y N grande la
  lista es más rápida. Tablas y análisis en la sección 4 del
  [informe de rendimiento](informe-rendimiento.md).
