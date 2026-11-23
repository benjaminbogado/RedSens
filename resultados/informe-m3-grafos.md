# RedSens · M3 — Red vial y planificación de recorridos

Grupo 3 · Base de la cuadrilla: **San Francisco** · Control: Córdoba · Java 21 · Integración M3/M4

## 1. Qué incluye

| Archivo | Contenido |
|---|---|
| `datos/red_vial.txt` | Grafo de referencia del enunciado: 12 localidades y 18 tramos (km del enunciado). |
| `red/GrafoLocalidades.java` | Grafo no dirigido y ponderado con lista y matriz de adyacencia sincronizadas; alta, corte de tramos y copia. |
| `red/Ruta.java` | Arista de la lista de adyacencia (destino, km). |
| `red/CargadorGrafo.java` | Carga el grafo desde el archivo de texto (no está escrito en el código). |
| `red/Recorridos.java` | BFS (orden y niveles) y DFS, con lista y con matriz; conexidad con DFS. |
| `red/CaminosMinimos.java` | Dijkstra (un origen) y Floyd (todos los pares), con reconstrucción de caminos. |
| `red/CorteRuta.java` | Corte de un tramo sobre una copia: conexidad (DFS), localidades aisladas y caminos que empeoran. |
| `red/PlanVisitas.java` | Vecino más cercano sobre la tabla de Floyd, desde la base y volviendo a ella. |
| `Main.java` | Opción 3 del menú. La opción 9 del submenú toma el reporte de críticas del AVL (M2). |
| `src/test/java/redsens/red/PruebaGrafo.java` | 88 verificaciones, todas OK (`resultados/verificacion-m3.txt`). |
| `src/test/java/redsens/red/ExperimentoGrafo.java` | Corrida de los seis puntos del enunciado (`resultados/resultados-m3.txt`). |

Restricción del enunciado para M3: sin `HashMap`, `TreeMap` ni librerías de grafos. Solo se usan `ArrayList`, `ArrayDeque` (cola del BFS) y arreglos. La traducción nombre → índice es una búsqueda secuencial O(V), y Dijkstra elige el mínimo con un recorrido lineal en lugar de una cola de prioridad.

```sh
java -cp out redsens.red.PruebaGrafo
java -cp out redsens.red.ExperimentoGrafo
```

## 2. Punto 1 — ¿Matriz o lista de adyacencia?

La matriz tiene 12 × 12 = 144 celdas y solo 36 tienen un tramo (cada tramo aparece dos veces porque el grafo no es dirigido). Son 18 tramos de 66 pares posibles: el grafo es **disperso** (≈ 27 %), como toda red vial, donde cada localidad se conecta con unas pocas vecinas.

**Conviene la lista de adyacencia** para este caso:

- BFS, DFS y Dijkstra recorren los vecinos de cada vértice. Con la lista, eso cuesta O(V + E) en total. Con la matriz hay que revisar la fila completa, sea o no vecina: O(V²).
- La lista ocupa O(V + E), y la matriz O(V²) aunque casi todo esté vacío.

La matriz se mantiene igual porque el enunciado la pide y porque es la entrada natural de **Floyd**, que trabaja con todos los pares y es O(V³) de todos modos. También sirve para responder en O(1) si existe un tramo directo. Con solo 12 localidades la diferencia de tiempo no se nota: el argumento es cómo escala cada representación. BFS y DFS están implementados con las dos y dan el mismo orden, porque en ambas se recorren los vecinos por índice ascendente.

## 3. Punto 2 — BFS por niveles desde San Francisco

| Tramos | Localidades |
|---|---|
| 1 | Córdoba, Villa María |
| 2 | Villa Carlos Paz, Alta Gracia, Jesús María, Río Tercero, Río Cuarto |
| 3 | Cosquín, Villa Dolores, Deán Funes |
| 4 | La Falda (fuera de los 3 tramos que pide el enunciado) |

El nivel de BFS cuenta tramos, no km. Por ejemplo, Río Cuarto está a 2 tramos, pero a 280 km.

## 4. Punto 3 — DFS, conexidad y corte de un tramo

- El DFS desde San Francisco visita las 12 localidades, así que **la red es conexa**.
- **Corte de Córdoba – San Francisco (205 km):** la red sigue conexa y ninguna localidad queda aislada. Lo que cambia es la distancia: casi todos los destinos empeoran 91 km, porque ahora hay que ir por Villa María (150 + 146 = 296 km hasta Córdoba). Alta Gracia empeora 61 km, porque le conviene pasar por Río Tercero.
- **Ningún corte de un único tramo desconecta la red.** Se probaron los 18 cortes uno por uno, con un DFS para cada uno. Todas las localidades están dentro de algún ciclo, así que ninguna arista es un "puente".
- Hace falta cortar **dos** tramos para aislar a alguien. Con Córdoba – San Francisco y Villa María – San Francisco cortados, la base queda aislada, porque San Francisco solo tiene esas dos salidas.

## 5. Punto 4 — Dijkstra

Control desde Córdoba (para verificar el algoritmo): Río Cuarto queda a 212 km pasando por Río Tercero, y La Falda a 80 km pasando por Villa Carlos Paz y Cosquín.

Desde San Francisco (resultado completo, con caminos, en `resultados-m3.txt`): Villa María 150, Córdoba 205, Río Tercero 220 (por Villa María), Río Cuarto 280 (por Villa María) y Villa Dolores 381 (Córdoba > Villa Carlos Paz > Villa Dolores), que es la más lejana.

Implementación: en cada paso se elige con búsqueda lineal el vértice no definitivo de menor distancia y se relajan sus tramos. Costo O(V² + E). El algoritmo es correcto porque todos los km son positivos: una vez que un vértice es definitivo, ninguna ruta posterior puede mejorarlo. Se guarda `previo[]` para reconstruir el camino completo.

## 6. Punto 5 — Floyd

`d[i][j]` arranca como la matriz de adyacencia. Para cada intermedio `k`, se cambia `d[i][j]` por `d[i][k] + d[k][j]` cuando ese camino es más corto. Costo O(V³) = 1.728 pasos para 12 localidades. Una matriz `siguiente` permite reconstruir los caminos. Una prueba verifica que coincide con Dijkstra ejecutado desde cada uno de los 12 orígenes. La matriz completa está en `resultados-m3.txt`.

## 7. Punto 6 — Plan de visitas a las estaciones críticas

Integración M2 → M3: `avl.listarCriticas()` devuelve el reporte de estaciones con batería menor a 20, ordenadas por número. De ahí salen las localidades a visitar, sin repetir. Desde la base se va siempre a la pendiente más cercana según la tabla de Floyd y al final se vuelve a la base. Para medir la heurística, el experimento calcula también el óptimo exacto (Held-Karp), solo como referencia.

| Estaciones generadas (semilla 3026) | Críticas | Localidades | Vecino más cercano | Óptimo | Exceso |
|---|---|---|---|---|---|
| 30 | 5 | 5 | 1123 km | 1051 km | 6,9 % |
| 1000 | 206 | 12 (todas) | 1354 km | 1215 km | 11,4 % |

**Por qué no garantiza el óptimo.** Es un criterio voraz: en cada paso elige la localidad más cercana a donde está, sin mirar cuánto le va a costar después llegar a las que deja atrás ni volver a la base. En el plan de 12 localidades, primero recorre las cercanas y deja para el final Villa Dolores y Río Cuarto, que están lejos de todo. Eso lo obliga a pagar Deán Funes → Villa Dolores (279 km) y Río Cuarto → San Francisco (280 km). Encontrar el recorrido óptimo es el problema del viajante, que no tiene solución eficiente conocida: Held-Karp es exponencial y solo sirve acá porque la red es chica. La heurística, en cambio, es O(V²) una vez calculado Floyd.

## 8. Complejidades

| Operación | Costo |
|---|---|
| Cargar el grafo | O(V² + E) (la matriz se agranda al sumar cada localidad) |
| BFS / DFS con lista · con matriz | O(V + E) · O(V²) |
| Dijkstra (selección lineal) | O(V² + E) |
| Floyd | O(V³) tiempo, O(V²) memoria |
| Corte de un tramo + DFS | O(V + E) más la copia del grafo |
| Probar los E cortes | O(E · (V + E)) |
| Vecino más cercano (con Floyd ya calculado) | O(V²) |
