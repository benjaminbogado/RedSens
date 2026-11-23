# RedSens · Informe de rendimiento

Grupo 3 · Semilla 3026 · Capacidad inicial de la tabla hash 17 · Base San Francisco · M de Josephus 3

Responsable del informe: Tobias Zapiola. Reúne los tres experimentos obligatorios del enunciado
(M1, M2 y M5), que M6 pide juntar en un informe de rendimiento. Todos los números salen de
corridas reales guardadas en el repositorio.

| Experimento | Módulo | Estado |
|---|---|---|
| Exploración lineal vs. cuadrática con 10.000 estaciones | M1 | Medido |
| ABB vs. AVL con 1.000, 5.000 y 10.000 estaciones, en orden y al azar | M2 | Medido |
| Lista enlazada vs. ABB con k-ésimo para N = 1.000, 10.000 y 100.000 | M5 | Medido (código en revisión, PR #14) |

## 1. Método

- **Datos:** estaciones generadas por `GeneradorDatos` (M6) con `java.util.Random` y la semilla 3026.
  Los códigos `CBA-NNNN` son únicos, así que hay como máximo 10.000 estaciones.
- **Tiempos:** `System.nanoTime()`, con la generación de datos y las impresiones fuera de la medición.
- **Dos máquinas:** los experimentos se corrieron en una Mac (Java 21, macOS aarch64) y en PCs
  con Windows 11 (Java 24 para M1 y M2, Java 23 para M5, amd64). Los valores deterministas
  (colisiones, sondeos, rehashings, alturas y suplentes de Josephus) dan **idénticos** en las dos,
  como corresponde a una semilla y a parámetros fijos. Los tiempos cambian con la máquina; se
  informan los dos y se comentan las diferencias.
- **Verificación previa:** antes de medir, cada experimento comprueba que las estructuras tengan
  todas las estaciones y devuelvan lo esperado.

Salidas completas guardadas en el repositorio:

| Archivo | Contenido |
|---|---|
| `resultados/experimento-m1.txt` | M1 en Mac |
| `resultados/experimento-m1-luca.txt` | M1 en Windows |
| `resultados/experimento-m2.txt` | M2 en Mac |
| `resultados/experimento-m2-luca.txt` | M2 en Windows |
| `resultados/experimento-m5.txt` (PR #14) | M5 en Windows |
| Sección 4.1 de este informe | M5 en Mac |

## 2. M1 · Tabla hash: exploración lineal vs. cuadrática

10.000 estaciones insertadas en una tabla que arranca con capacidad 17 y crece sola. Umbral de cada
estrategia: lineal 0,75 y cuadrática 0,5. Clase `redsens.registro.ExperimentoTablaHash`.

### 2.1 Experimento obligatorio

| Estrategia | Umbral | Colisiones al insertar | Rehashings | Capacidad final | Sondeos prom. búsqueda exitosa | Sondeos prom. búsqueda no exitosa |
|---|---:|---:|---:|---:|---:|---:|
| Lineal | 0,75 | 99.993 | 10 | 21.911 | 2,296 | 4,577 |
| Cuadrática | 0,50 | 9.067 | 10 | 21.911 | 1,517 | 2,176 |

Búsqueda no exitosa: como con 10.000 estaciones no queda ningún código `CBA-NNNN` libre, se arma
otra tabla con 9.000 y se buscan las 1.000 que faltan. Con 1.000 estaciones borradas en forma
perezosa, en lugar de ausentes, la búsqueda no exitosa sube a 11,502 sondeos en la lineal y a 4,599
en la cuadrática.

### 2.2 Control: las dos estrategias con umbral 0,5

| Estrategia | Colisiones al insertar | Sondeos prom. búsqueda exitosa |
|---|---:|---:|
| Lineal | 20.187 | 2,296 |
| Cuadrática | 9.067 | 1,517 |

### 2.3 Barrido de factor de carga (capacidad fija 10.007, sin rehashing)

Sondeos promedio por búsqueda. Entre paréntesis, el valor teórico (Knuth para la lineal).

| λ | Lineal exitosa | Lineal no exitosa | Cuadrática exitosa | Cuadrática no exitosa |
|---:|---:|---:|---:|---:|
| 0,3 | 1,10 (1,21) | 1,31 (1,52) | 1,09 | 1,25 |
| 0,5 | 1,36 (1,50) | 2,80 (2,50) | 1,24 | 1,87 |
| 0,7 | 3,10 (2,17) | 19,30 (6,06) | 1,64 | 4,00 |
| 0,9 | 19,56 (5,50) | 199,09 (50,50) | 3,01 | 15,01 |

Para λ > 0,5 la cuadrática se corrió solo como prueba: con capacidad prima, su garantía de
encontrar un casillero libre vale hasta λ = 0,5.

### 2.4 Análisis

- **Agrupamiento primario.** La lineal tuvo 11 veces más colisiones. El control con el mismo umbral
  muestra que no es solo por el umbral: aun con 0,5 tiene más del doble (20.187 contra 9.067). En la
  lineal, las claves que chocan ocupan casilleros contiguos y forman bloques que crecen; cualquier
  clave que cae en el bloque lo recorre y lo agranda. La cuadrática salta 1, 4, 9… posiciones y sale
  rápido del bloque.
- **La búsqueda no exitosa es la que más sufre**, porque recorre el bloque entero hasta encontrar un
  casillero vacío. En el barrido, con λ = 0,9 la lineal necesita 199 sondeos y la cuadrática 15.
- **Costo del borrado perezoso.** Las marcas de borrado no cortan la búsqueda, por eso suben los
  sondeos de la búsqueda no exitosa. Por la misma razón las marcas cuentan para el umbral y el
  rehashing las descarta.
- **Rehashings.** Las dos estrategias hacen 10 (17 → 37 → 79 → … → 21.911): para 10.000 estaciones
  las dos tienen que superar los 10.949 casilleros, porque 10.000 / 10.949 = 0,91 supera ambos umbrales.
- **Función hash.** Con λ ≥ 0,6 la lineal da peor que la fórmula de Knuth. La función conserva el
  orden de los códigos (CBA-0347, 0348 y 0349 caen en casilleros seguidos), y la lineal, que también
  avanza de a uno, agrava ese agrupamiento.
- **Tiempo por búsqueda.** Fue de 899 ns (lineal) y 556 ns (cuadrática) en Mac, y de 1.062 y 871 ns
  en Windows. Gran parte de ese tiempo es la validación del código con una expresión regular en
  cada búsqueda, que no cambia sondeos ni colisiones. Por eso, para comparar estrategias, se usan
  los sondeos y no los nanosegundos.

## 3. M2 · ABB vs. AVL

Las estaciones del censo llegan ordenadas por número. Se insertan 1.000, 5.000 y 10.000 estaciones
en orden ascendente y en orden aleatorio, las mismas y en el mismo orden en los dos árboles. Se
buscan todas, con 3 rondas de calentamiento y 10 medidas. Altura en aristas (árbol vacío = −1).
Clase `redsens.indice.ExperimentoArboles`.

### 3.1 Resultados (Mac)

| Estaciones | Orden | Altura ABB | Altura AVL | Búsqueda prom. ABB (ns) | Búsqueda prom. AVL (ns) | ABB / AVL |
|---:|---|---:|---:|---:|---:|---:|
| 1.000 | ascendente | 999 | 9 | 1.698,8 | 46,4 | 36,6× |
| 1.000 | aleatorio | 18 | 11 | 27,7 | 32,4 | 0,85× |
| 5.000 | ascendente | 4.999 | 12 | 9.395,0 | 72,6 | 129,4× |
| 5.000 | aleatorio | 28 | 14 | 42,3 | 39,9 | 1,06× |
| 10.000 | ascendente | 9.999 | 13 | 18.634,2 | 71,4 | 261,0× |
| 10.000 | aleatorio | 30 | 15 | 74,2 | 63,8 | 1,16× |

Construcción con 10.000 ascendentes: ABB 210,7 ms, AVL 0,4 ms. Gráficos en
`resultados/grafico-alturas.svg` y `resultados/grafico-busqueda.svg`.

### 3.2 Comparación con Windows

Las alturas son idénticas en las dos máquinas. Los tiempos absolutos de Windows son mayores (con
10.000 ascendentes: ABB 48.713,8 ns y AVL 226,4 ns por búsqueda), pero la conclusión es la misma:
el AVL busca más de 200 veces más rápido cuando los datos llegan ordenados.

**Desborde de pila.** El ABB es recursivo, así que insertar 10.000 claves ordenadas encadena 10.000
llamadas. Con la pila por defecto de la JVM, en la Mac no desbordó, pero en Windows dio
`StackOverflowError`. Por eso el experimento corre en un hilo con 512 MB de pila. Es el mismo
problema que el enunciado advierte para Python, que limita la recursión a unas 1.000 llamadas.

### 3.3 Análisis

- **Orden ascendente, ABB.** Cada número nuevo es mayor que todos los anteriores y se cuelga a la
  derecha: el árbol degenera en una lista de altura n − 1. Buscar pasa a ser O(n): al multiplicar n
  por 10, el tiempo de búsqueda crece 11 veces. Construirlo es O(n²): de 2,05 ms a 210,7 ms (103 veces).
- **Orden ascendente, AVL.** Con el factor de equilibrio y las rotaciones, la altura queda en
  O(log n): 9, 12 y 13, por debajo de la cota teórica ≈ 1,44·log₂ n (≈ 19 para 10.000).
- **Orden aleatorio.** Los dos árboles quedan bajos y buscan casi igual de rápido. Las diferencias de
  pocos nanosegundos (en algún caso gana el ABB) están dentro del ruido de medición.
- **Decisión de diseño.** Como los datos del censo llegan ordenados, el índice del sistema es el AVL.
  El menú usa el AVL para el listado por rango y el reporte de críticas.

## 4. M5 · Josephus: lista enlazada vs. ABB con k-ésimo

N técnicos en ronda; se omiten M y el siguiente queda de guardia. Se comparan las dos soluciones
de la lectura: `JosephusLista` (`LinkedList` e `Iterator`, O(N·M)) y `JosephusArbol` (ABB propio que
guarda el tamaño de cada subárbol para obtener el k-ésimo, O(N log N)). Se usan M = 3 (el del grupo)
y M = 1.000 (muchas pasadas). Cada escenario tiene 3 rondas de calentamiento y se informa la mediana
de 5; el tiempo incluye construir la estructura y la lista de asignaciones. En cada ronda se
verifica que los dos calendarios sean idénticos. Implementación de Federico Villagra (PR #14), clase
`redsens.guardias.ExperimentoJosephus`.

**Casos de verificación:** N = 5 y M = 1 asigna 2, 4, 1, 5 y deja al 3 de suplente, como en la
lectura. Para el grupo (N = 12, M = 3): 4, 8, 12, 5, 10, 3, 11, 7, 6, 9, 2, con el técnico 1 de
suplente. `PruebaJosephus` hace 34.745 verificaciones, todas correctas.

### 4.1 Resultados

| N | M | Lista Mac (ms) | Árbol Mac (ms) | Lista / árbol Mac | Lista Windows (ms) | Árbol Windows (ms) | Lista / árbol Windows | Suplente |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 1.000 | 3 | 0,182 | 0,147 | 1,24 | 0,360 | 0,234 | 1,54 | 323 |
| 1.000 | 1.000 | 1,570 | 0,129 | 12,16 | 7,132 | 0,217 | 32,84 | 154 |
| 10.000 | 3 | 0,200 | 1,035 | 0,19 | 2,923 | 1,574 | 1,86 | 3.254 |
| 10.000 | 1.000 | 16,085 | 1,164 | 13,83 | 23,857 | 1,506 | 15,84 | 4.342 |
| 100.000 | 3 | 1,974 | 10,373 | 0,19 | 2,942 | 12,348 | 0,24 | 32.942 |
| 100.000 | 1.000 | 203,360 | 15,757 | 12,91 | 375,516 | 22,088 | 17,00 | 12.442 |

Mac: Java 21, macOS aarch64, corrida del 23/11. Windows: Java 23, Windows 11 amd64
(`resultados/experimento-m5.txt` del PR #14). "Lista / árbol" mayor que 1 significa que el árbol
fue más rápido. Los suplentes son idénticos en las dos máquinas.

### 4.2 Análisis

- **Con M grande gana el árbol, y por mucho.** Con M = 1.000 la lista tiene que avanzar 1.000
  posiciones por cada técnico que sale, mientras que el árbol calcula directamente la posición
  (`(índice + M) % restantes`) y baja hasta ella en O(log N). Con 100.000 técnicos el árbol fue
  13 veces más rápido en Mac y 17 en Windows.
- **Con M chico la lista es competitiva, o incluso mejor.** Con M = 3 la lista hace unas 3·N
  operaciones simples, mientras que el árbol hace unas N·log₂ N (≈ 17 por técnico con N = 100.000).
  Como 3 es menor que log₂ N, la lista gana con N grande: con 100.000 técnicos fue entre 4 y 5 veces
  más rápida en las dos máquinas.
- **Cuándo importa la diferencia.** La comparación real es M contra log₂ N: si M es mucho mayor que
  log₂ N, conviene el árbol; si M es chico, la lista alcanza. El parámetro del grupo (M = 3) cae del
  lado de la lista, pero el árbol garantiza un costo que no depende de M.
- **Variaciones.** Con N = 10.000 y M = 3, la lista ganó en la Mac y perdió en Windows. Con tiempos
  de 1 a 3 ms, el JIT, la caché y la carga del sistema pesan tanto como el algoritmo. Las tendencias
  con M = 1.000 y con N = 100.000 coinciden en las dos máquinas.

## 5. Cómo reproducir

Desde la raíz del repositorio, con el proyecto compilado (ver `README.md`):

```bash
java -cp out redsens.registro.ExperimentoTablaHash 3026
java -cp out redsens.indice.ExperimentoArboles 3026
java -cp out redsens.guardias.ExperimentoJosephus    # cuando se integre el PR #14
```

El experimento de M5 también está en el menú: opción `5` → `3`.

Los valores deterministas tienen que coincidir exactamente con este informe. Los tiempos dependen de
la máquina; en la Mac, una nueva corrida de M1 y M2 (21/11) dio diferencias de pocos puntos
porcentuales sin cambiar ninguna conclusión.

## 6. Conclusiones

- **M1:** con el mismo trabajo de rehashing, la cuadrática tiene muchas menos colisiones y sondeos que
  la lineal, por el agrupamiento primario. La lineal tolera un umbral más alto, pero sus costos crecen
  muy rápido cuando la tabla se llena.
- **M2:** el ABB depende del orden de llegada de los datos y, con el censo ordenado, degenera en una
  lista. El AVL garantiza O(log n) en cualquier orden, a cambio de un costo chico por guardar alturas
  y rotar.
- **M5:** la mejor solución depende de M. El ABB con k-ésimo garantiza O(N log N) sin importar M y
  gana claramente con muchas pasadas; con M chico, como el M = 3 del grupo, la lista enlazada es
  igual o más rápida.
