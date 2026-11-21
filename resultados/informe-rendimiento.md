# RedSens · Informe de rendimiento

Grupo 3 · Semilla 3026 · Capacidad inicial de la tabla hash 17 · Base San Francisco · M de Josephus 3

Responsable del informe: Tobias Zapiola. Reúne los experimentos obligatorios del enunciado que ya
están implementados en `main` (M1 y M2) y deja preparado el lugar del experimento de M5. No se
incluyen resultados de módulos que todavía no estén implementados.

| Experimento | Módulo | Estado |
|---|---|---|
| Exploración lineal vs. cuadrática con 10.000 estaciones | M1 | Medido |
| ABB vs. AVL con 1.000, 5.000 y 10.000 estaciones, en orden y al azar | M2 | Medido |
| Lista enlazada vs. ABB con k-ésimo para N = 1.000, 10.000 y 100.000 | M5 | Pendiente: M5 todavía no está implementado |

## 1. Método

- **Datos:** estaciones generadas por `GeneradorDatos` (M6) con `java.util.Random` y la semilla 3026.
  Los códigos `CBA-NNNN` son únicos, así que hay como máximo 10.000 estaciones.
- **Tiempos:** `System.nanoTime()`, con la generación de datos y las impresiones fuera de la medición.
- **Dos máquinas:** los experimentos se corrieron en una Mac (Java 21, macOS aarch64) y en una PC
  con Windows 11 (Java 24, amd64). Los valores deterministas (colisiones, sondeos, rehashings y
  alturas) dan **idénticos** en las dos, como corresponde a una semilla fija. Los tiempos cambian con
  la máquina; se informan los de la Mac y se comentan las diferencias.
- **Verificación previa:** antes de medir, cada experimento comprueba que las estructuras tengan
  todas las estaciones y devuelvan lo esperado.

Salidas completas guardadas en el repositorio:

| Archivo | Contenido |
|---|---|
| `resultados/experimento-m1.txt` | M1 en Mac |
| `resultados/experimento-m1-luca.txt` | M1 en Windows |
| `resultados/experimento-m2.txt` | M2 en Mac |
| `resultados/experimento-m2-luca.txt` | M2 en Windows |

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

## 4. M5 · Josephus: lista enlazada vs. ABB con k-ésimo (pendiente)

El enunciado pide medir las dos implementaciones con N = 1.000, 10.000 y 100.000 y al menos dos
valores de M, y explicar en qué casos la diferencia se vuelve importante. M5 todavía no está
implementado, así que esta sección queda para completar con la salida real del experimento:

| N | M | Lista enlazada (ms) | ABB con k-ésimo (ms) |
|---:|---:|---:|---:|
| 1.000 | 3 | — | — |
| 1.000 | _otro_ | — | — |
| 10.000 | 3 | — | — |
| 10.000 | _otro_ | — | — |
| 100.000 | 3 | — | — |
| 100.000 | _otro_ | — | — |

Complejidad esperada según la lectura: O(N·M) para la lista enlazada y O(N log N) para el ABB.

## 5. Cómo reproducir

Desde la raíz del repositorio, con el proyecto compilado (ver `README.md`):

```bash
java -cp out redsens.registro.ExperimentoTablaHash 3026
java -cp out redsens.indice.ExperimentoArboles 3026
```

Los valores deterministas tienen que coincidir exactamente con este informe. Los tiempos dependen de
la máquina; en la Mac, una nueva corrida (21/11) dio diferencias de pocos puntos porcentuales sin
cambiar ninguna conclusión.

## 6. Conclusiones

- **M1:** con el mismo trabajo de rehashing, la cuadrática tiene muchas menos colisiones y sondeos que
  la lineal, por el agrupamiento primario. La lineal tolera un umbral más alto, pero sus costos crecen
  muy rápido cuando la tabla se llena.
- **M2:** el ABB depende del orden de llegada de los datos y, con el censo ordenado, degenera en una
  lista. El AVL garantiza O(log n) en cualquier orden, a cambio de un costo chico por guardar alturas
  y rotar.
- **M5:** pendiente de implementación.
