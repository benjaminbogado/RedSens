# RedSens
Trabajo Práctico Integrador - AED II

Sistema de consola en Java para gestionar la red provincial de estaciones de medición de DATA
Investigaciones: registro por código (tabla hash), índice ordenado (ABB y AVL), red vial (grafos),
transmisión comprimida (Huffman), guardias (Josephus) y simulación con datos reproducibles.

## Grupo 3

| Parámetro | Valor |
|---|---|
| Semilla `Random` | `3026` |
| Capacidad inicial de la tabla hash (M1) | `17` |
| Base de la cuadrilla (M3) | San Francisco |
| M de Josephus (M5) | `3` |

| Integrante | Rol principal |
|---|---|
| Benjamín Bogado | Coordinación e integración, modelo `Estacion`, árbol AVL y menú principal |
| Federico Villagra | M1: tabla hash, exploración lineal y borrado perezoso · M5: Josephus |
| Franco Bossio | M1: exploración cuadrática y rehashing · M3: red vial |
| Luca Agustín Pepino | M6: generación de datos y verificaciones · M4: Huffman |
| Tobias Zapiola | M2: árbol binario de búsqueda y experimento ABB vs AVL · informe y documentación |

## Estado de los módulos

| Módulo | Paquete | Opción del menú | Estado |
|---|---|---|---|
| Modelo | `redsens.modelo` | 0 | En `main` |
| M1 · Registro (tabla hash) | `redsens.registro` | 1 | En `main` |
| M2 · Índice (ABB y AVL) | `redsens.indice` | 2 | En `main` |
| M3 · Red vial | `redsens.red` | 3 | En `main` |
| M4 · Transmisión (Huffman) | `redsens.compresion` | 4 | En `main` |
| M5 · Guardias (Josephus) | `redsens.guardias` | 5 | Implementado, en revisión (PR #14) |
| M6 · Simulación | `redsens.simulacion` | 6 | En `main` |

## Compilar y ejecutar

Requiere un JDK 17 o superior con `javac` y `java` disponibles en el PATH (verificar con
`java -version`). Todos los comandos se ejecutan **desde la raíz del repositorio**, porque el menú
lee archivos de `datos/`.

**Windows (PowerShell):**

```powershell
$fuentes = @(Get-ChildItem src/main/java,src/test/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -encoding UTF-8 --release 17 -d out @fuentes
java -cp out redsens.Main
```

**macOS o Linux:**

```sh
mkdir -p out
find src/main/java src/test/java -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -d out
java -cp out redsens.Main
```

Si `javac` no se reconoce, agregar la carpeta `bin` del JDK al PATH o invocar ambos ejecutables
mediante su ruta completa. Si la consola de Windows muestra mal los acentos, ejecutar antes
`chcp 65001`. Después de cambiar el código hay que volver a compilar.

## Cómo probar las funcionalidades desde el menú

El menú principal tiene un submenú por módulo. Recorrido sugerido:

1. **Cargar estaciones:** `0` (Conjunto de estaciones) → `1` (Generar) → `200`. Genera 200
   estaciones con la semilla 3026 y las inserta a la vez en las dos tablas hash, el ABB y el AVL.
   Con `3` se ve el resumen de cada estructura y con `4` la lista de estaciones.
2. **M1:** `1` (Registro hash) → `1` o `2` → un código de la lista, por ejemplo `CBA-1423`. Busca con
   sondeo lineal o cuadrático y muestra los sondeos y colisiones de esa búsqueda. Las opciones `3` y
   `4` agregan o eliminan una estación en todos los índices.
3. **M2:** `2` (Índices ABB y AVL) → `1` o `2` para buscar por número, `3` para listar un rango
   (por ejemplo, de 1000 a 2000), `4` para el reporte de estaciones críticas (batería menor a 20) y
   `5` para los recorridos y métricas del AVL.
4. **M3:** `3` (Grafos y recorridos). Al entrar carga `datos/red_vial.txt` (12 localidades y
   18 tramos). Después: `3` BFS por niveles desde San Francisco, `4` conexidad con DFS, `5` Dijkstra
   desde la base, `7` Floyd, `8` corte de una ruta y `9` plan de visitas a las localidades con
   estaciones críticas del paso 1.
5. **M4:** `4` (Compresión de datos) → `1`. Reproduce el caso `WWXWYZWX` (64 → 14 bits) y comprime y
   descomprime el archivo de 5.000 mediciones, comprobando que el resultado sea idéntico.
6. **M5** (cuando se integre el PR #14): `5` → `1` calendario del grupo (N = 12, M = 3), `2` calendario
   con otros parámetros y `3` experimento de rendimiento.
7. **M6:** `6` → `1` comprueba la reproducibilidad de la semilla y la uniformidad de las baterías.

También se pueden cargar estaciones desde un archivo UTF-8 (opción `0` → `2`) con una línea por
estación en el formato `codigo;numero;nombre;localidad;tipo;bateria;diasSinVisita`. Las líneas que
empiezan con `#` o con el encabezado `codigo;` se ignoran.

Tener en cuenta:
- La opción `6` → `2` escribe `datos/mediciones_menu.txt`, y la demostración de M4 guarda sus
  archivos en `output/m4-demo/`.
- Con conjuntos grandes, la opción `2` → `5` imprime todas las estaciones.
- El ABB es recursivo: cargar varios miles de estaciones ya ordenadas puede desbordar la pila en
  algunas máquinas (ver el informe de rendimiento).

## Pruebas ejecutables

Después de compilar:

```sh
java -cp out redsens.registro.PruebaTablaHash
java -cp out redsens.registro.PruebaTablaHashCuadratica
java -cp out redsens.indice.PruebaABB
java -cp out redsens.indice.ComprobadorArbolAVL
java -cp out redsens.red.PruebaGrafo
java -cp out redsens.compresion.PruebaHuffman
java -cp out redsens.simulacion.ComprobadorM6
java -cp out redsens.simulacion.ComprobadorLimites
```

| Prueba | Resultado esperado |
|---|---|
| `PruebaTablaHash` | 15.205 verificaciones OK |
| `PruebaTablaHashCuadratica` | 96 verificaciones OK, 0 fallas |
| `PruebaABB` | 57 verificaciones OK, 0 fallas |
| `ComprobadorArbolAVL` | Todas las comprobaciones del AVL pasaron |
| `PruebaGrafo` | 88 verificaciones OK, 0 fallas |
| `PruebaHuffman` | Pruebas M4: OK |
| `ComprobadorM6` | Misma semilla, mismos datos; semillas distintas, datos distintos |
| `ComprobadorLimites` | Todas las pruebas de límites pasaron |
| `PruebaJosephus` (PR #14) | 34.745 verificaciones OK |

`ComprobadorMediciones` (M6) escribe `datos/mediciones_grupo3.txt` y
`datos/mediciones_verificacion.txt` en el directorio de trabajo. Para conservar los archivos de datos
del proyecto, ejecutarlo desde un directorio temporal:

```sh
raiz_proyecto="$(pwd)"
directorio_temporal="$(mktemp -d)"
(cd "$directorio_temporal" && java -cp "$raiz_proyecto/out" redsens.simulacion.ComprobadorMediciones)
```

`ExperimentoAVL` (M6) también reescribe un archivo de `datos/`.

## Experimentos de rendimiento

```sh
java -cp out redsens.registro.ExperimentoTablaHash 3026
java -cp out redsens.indice.ExperimentoArboles 3026
java -cp out redsens.red.ExperimentoGrafo
java -cp out redsens.guardias.ExperimentoJosephus    # cuando se integre el PR #14
```

Resultados y análisis de M1, M2 y M5 en [el informe de rendimiento](resultados/informe-rendimiento.md).

## Documentación

| Documento | Contenido |
|---|---|
| [Informe de rendimiento](resultados/informe-rendimiento.md) | Experimentos de M1, M2 y M5 |
| [Documentación de M3, M4 y M5](resultados/documentacion-m3-m4-m5.md) | Resumen de los módulos de la segunda etapa |
| [Informe de M1](resultados/informe-m1-tabla-hash.md) | Exploración cuadrática, rehashing y comparación |
| [Contrato de M1](resultados/contrato-m1-lineal.md) | Operaciones y decisiones de la tabla hash lineal |
| [Informe de M2](resultados/informe-rendimiento-m2.md) | Experimento ABB vs AVL en detalle |
| [Informe de M3](resultados/informe-m3-grafos.md) | Red vial: decisiones, resultados y complejidades |
| [README de M4](README_M4.md) | Huffman: clases, formato RHUF y comandos |
| [README de M6](README_M6.md) | Generación de datos, verificaciones y mediciones |
| Bitácoras de IA | [M1 cuadrática](BITACORA_IA_M1_CUADRATICA.md) · [M4](BITACORA_IA_M4.md) · [M6](BITACORA_IA_M6.md) · M2 (PR #10) · M5 (PR #14) |

## Notas por módulo

### M1 registro hash

`redsens.registro.TablaHash` implementa exploración lineal y cuadrática, inserción, búsqueda,
eliminación perezosa, métricas de búsqueda y tablas ampliables con rehashing. El menú M1 permite
elegir una estrategia para buscar por código; las operaciones de alta y baja actualizan ambas
tablas junto con los árboles. Ejemplo de construcción:

```java
TablaHash registro = new TablaHash(17, EstrategiaExploracion.LINEAL);
registro.insertar(estacion);
Estacion encontrada = registro.buscar("CBA-0347");
registro.eliminar("CBA-0347");
```

Las clases pertenecen a `redsens.registro` y usan el modelo `Estacion` común. Los umbrales por
defecto son 0,75 para lineal y 0,5 para cuadrática. Al superarlos, la tabla ampliable crece al
primer primo mayor o igual al doble. El contrato y la explicación de la tabla lineal están en
[el contrato de M1](resultados/contrato-m1-lineal.md).

#### Práctica manual de M1

Después de compilar, abrir JShell (incluido en el JDK):

```sh
jshell --class-path out
```

Copiar las líneas de [la práctica manual](ejemplos/m1-manual.jsh) una por una para observar
inserciones con colisiones, búsquedas, borrados y contadores. También se puede ejecutar la
demostración completa:

```sh
jshell --class-path out ejemplos/m1-manual.jsh
```

La sesión queda abierta para experimentar. Escribir `/exit` para salir. La
[guía de estudio en PDF](output/pdf/guia-estudio-m1-lineal.pdf) explica el código línea por línea,
las decisiones y la integración con el equipo.

Para usar explícitamente una tabla ampliable con exploración cuadrática:

```java
TablaHash registro = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
```

Los constructores de capacidad fija también están disponibles. El experimento comparativo está en
`src/test/java/redsens/registro/ExperimentoTablaHash.java`; sus resultados y explicación están en
[el informe de M1](resultados/informe-m1-tabla-hash.md).

### M2 árbol binario de búsqueda y AVL

`ArbolBinarioBusqueda` y `ArbolAVL` (paquete `redsens.indice`) indexan las estaciones por `numero`,
con inserción, búsqueda, eliminación (hoja, un hijo y dos hijos), recorridos inorden, preorden y
postorden, altura, cantidad de nodos y de hojas (recursivos, con el caso base marcado), listado por
rango y reporte de críticas. Con datos ordenados el ABB degenera en una lista y el AVL se mantiene
balanceado con rotaciones; por eso el menú usa el AVL para el rango y las críticas.

### M3 grafos y recorridos

M3 implementa la red vial en `redsens.red` sin `HashMap` ni librerías de grafos: lista y matriz de
adyacencia, BFS por niveles, DFS y conexidad, Dijkstra (base San Francisco y control desde
Córdoba), Floyd, corte de un tramo y plan de visitas por vecino más cercano. El grafo del enunciado
(12 localidades, 18 tramos) se carga desde `datos/red_vial.txt`. Está en la opción 3 del menú; la
opción 9 de ese submenú arma el plan con el reporte de críticas del AVL (M2). Las decisiones y los
resultados están en [el informe de M3](resultados/informe-m3-grafos.md).

### M4 compresión Huffman

El módulo M4 se encuentra en `redsens.compresion` y está disponible desde la opción 4 del menú
principal. Su diseño, formato RHUF1, pruebas y comandos están documentados en
[README_M4.md](README_M4.md). La demostración usa `datos/mediciones_grupo3.txt` como entrada de solo
lectura cuando contiene al menos 5.000 mediciones; si necesita generar una entrada, la guarda bajo
`output/m4-demo/`. Los archivos comprimidos y recuperados de la demostración también se guardan en
esa carpeta. El comprobador usa una carpeta temporal.

### M5 guardias rotativas

En revisión en el PR #14. `redsens.guardias` resuelve Josephus con dos estrategias:
`JosephusLista` (`LinkedList` e `Iterator`, O(N·M)) y `JosephusArbol` (ABB propio con acceso al
k-ésimo, O(N log N)). El calendario del grupo (N = 12, M = 3) asigna las guardias en el orden 4, 8,
12, 5, 10, 3, 11, 7, 6, 9, 2, con el técnico 1 como suplente.
