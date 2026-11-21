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
| Federico Villagra | M1: tabla hash, exploración lineal y borrado perezoso |
| Franco Bossio | M1: exploración cuadrática y rehashing · M3: red vial |
| Luca Agustín Pepino | M6: generación de datos y verificaciones · M4: Huffman |
| Tobias Zapiola | M2: árbol binario de búsqueda y experimento ABB vs AVL · informe y documentación |

## Estado de los módulos

| Módulo | Paquete | Estado |
|---|---|---|
| Modelo | `redsens.modelo` | En `main` |
| M1 · Registro (tabla hash) | `redsens.registro` | En `main` |
| M2 · Índice (ABB y AVL) | `redsens.indice` | En `main` |
| M3 · Red vial | `redsens.red` | En revisión (PR #11) |
| M4 · Transmisión (Huffman) | `redsens.compresion` | En revisión (PR #12) |
| M5 · Guardias (Josephus) | — | Pendiente |
| M6 · Simulación | `redsens.simulacion` | En `main` |
| Menú integrado | `redsens.Main` | En `main` (opciones 0, 1, 2 y 6) |

## Compilar y ejecutar

Requiere un JDK 17 o superior con `javac` y `java` disponibles en el PATH (verificar con
`java -version`). Todos los comandos se ejecutan **desde la raíz del repositorio**.

**Windows (PowerShell):**

```powershell
$fuentes = @(Get-ChildItem src/main/java,src/test/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -encoding UTF-8 --release 17 -d out @fuentes
java -cp out redsens.Main
```

**macOS o Linux:**

```bash
javac -encoding UTF-8 --release 17 -d out $(find src -name "*.java")
java -cp out redsens.Main
```

Si `javac` no se reconoce, agregar la carpeta `bin` del JDK al PATH o invocar ambos ejecutables
mediante su ruta completa. Si la consola de Windows muestra mal los acentos, ejecutar antes
`chcp 65001`. Después de cambiar el código hay que volver a compilar.

## Cómo probar las funcionalidades desde el menú

El menú principal tiene un submenú por módulo. Recorrido sugerido:

1. **Cargar datos:** `0` (Conjunto de estaciones) → `1` (Generar) → `20`. Genera 20 estaciones con la
   semilla 3026 y las inserta a la vez en las dos tablas hash, el ABB y el AVL. Con `3` se ve el
   resumen de cada estructura y con `4` la lista de estaciones.
2. **M1:** `1` (Registro hash) → `1` o `2` → un código de la lista, por ejemplo `CBA-1423`. Busca con
   sondeo lineal o cuadrático y muestra los sondeos y colisiones de esa búsqueda. Las opciones `3` y
   `4` agregan o eliminan una estación en todos los índices.
3. **M2:** `2` (Índices ABB y AVL) → `1` o `2` para buscar por número, `3` para listar un rango
   (por ejemplo, de 1000 a 2000), `4` para el reporte de estaciones críticas (batería menor a 20) y
   `5` para los recorridos y métricas del AVL.
4. **M6:** `6` → `1` comprueba la reproducibilidad de la semilla y la uniformidad de las baterías.

También se pueden cargar estaciones desde un archivo UTF-8 (opción `0` → `2`) con una línea por
estación en el formato `codigo;numero;nombre;localidad;tipo;bateria;diasSinVisita`. Las líneas que
empiezan con `#` o con el encabezado `codigo;` se ignoran.

Cuidado: la opción `6` → `2` escribe `datos/mediciones_menu.txt`. Con conjuntos grandes, la opción
`2` → `5` imprime todas las estaciones. El ABB es recursivo: cargar varios miles de estaciones ya
ordenadas puede desbordar la pila en algunas máquinas (ver el informe de rendimiento).

## Pruebas ejecutables

Después de compilar:

```powershell
java -cp out redsens.registro.PruebaTablaHash
java -cp out redsens.registro.PruebaTablaHashCuadratica
java -cp out redsens.indice.PruebaABB
java -cp out redsens.indice.ComprobadorArbolAVL
java -cp out redsens.simulacion.ComprobadorM6
java -cp out redsens.simulacion.ComprobadorLimites
```

| Prueba | Resultado esperado |
|---|---|
| `PruebaTablaHash` | 15.205 verificaciones OK |
| `PruebaTablaHashCuadratica` | 96 verificaciones OK, 0 fallas |
| `PruebaABB` | 57 verificaciones OK, 0 fallas |
| `ComprobadorArbolAVL` | Todas las comprobaciones del AVL pasaron |
| `ComprobadorM6` | Misma semilla, mismos datos; semillas distintas, datos distintos |
| `ComprobadorLimites` | Todas las pruebas de límites pasaron |

`ComprobadorMediciones` y `ExperimentoAVL` (M6) también se pueden ejecutar, pero reescriben archivos
de la carpeta `datos/`.

## Experimentos de rendimiento

```powershell
java -cp out redsens.registro.ExperimentoTablaHash 3026
java -cp out redsens.indice.ExperimentoArboles 3026
```

Resultados y análisis en [el informe de rendimiento](resultados/informe-rendimiento.md). La
documentación breve de M3, M4 y M5 está en
[documentación de M3, M4 y M5](resultados/documentacion-m3-m4-m5.md).

## Documentación

| Documento | Contenido |
|---|---|
| [Informe de rendimiento](resultados/informe-rendimiento.md) | Experimentos de M1 y M2 (M5 pendiente) |
| [Documentación de M3, M4 y M5](resultados/documentacion-m3-m4-m5.md) | Resumen de los módulos de la segunda etapa |
| [Informe de M1](resultados/informe-m1-tabla-hash.md) | Exploración cuadrática, rehashing y comparación |
| [Contrato de M1](resultados/contrato-m1-lineal.md) | Operaciones y decisiones de la tabla hash lineal |
| [Informe de M2](resultados/informe-rendimiento-m2.md) | Experimento ABB vs AVL en detalle |
| [README de M6](README_M6.md) | Generación de datos, verificaciones y mediciones |
| Bitácoras de IA | [M1 cuadrática](BITACORA_IA_M1_CUADRATICA.md) · [M6](BITACORA_IA_M6.md) |

## Notas por módulo

### M1 lineal

FEDE desarrolló la base de `TablaHash`, la exploración lineal, el borrado perezoso y los contadores.
Ejemplo de construcción con capacidad fija:

```java
TablaHash registro = new TablaHash(17, EstrategiaExploracion.LINEAL);
registro.insertar(estacion);
Estacion encontrada = registro.buscar("CBA-0347");
registro.eliminar("CBA-0347");
```

Las clases pertenecen a `redsens.registro` y usan el modelo `Estacion` común. El contrato, las
decisiones y la guía de integración están en [el contrato de M1](resultados/contrato-m1-lineal.md).

#### Práctica manual de M1

Después de compilar, abrir JShell (incluido en el JDK):

```powershell
jshell --class-path out
```

Copiar las líneas de [la práctica manual](ejemplos/m1-manual.jsh) una por una para observar
inserciones con colisiones, búsquedas, borrados y contadores. También se puede ejecutar la
demostración completa:

```powershell
jshell --class-path out ejemplos/m1-manual.jsh
```

La sesión queda abierta para experimentar. Escribir `/exit` para salir. La
[guía de estudio en PDF](output/pdf/guia-estudio-m1-lineal.pdf) explica el código línea por línea,
las decisiones y la integración con el equipo.

### M1 cuadrática y rehashing

FRANCO agregó la exploración cuadrática, el umbral de carga, el rehashing y la comparación de
estrategias sobre la `TablaHash` de FEDE. Los constructores de capacidad fija no cambian; para la
tabla que crece sola (la que usa el menú):

```java
TablaHash registro = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
```

Umbrales por defecto: 0,75 para lineal y 0,5 para cuadrática. Al superarlo, la tabla crece al primer
primo mayor o igual al doble. Las decisiones, los resultados del experimento y su explicación están
en [el informe de M1](resultados/informe-m1-tabla-hash.md).

### M2 árbol binario de búsqueda y AVL

`ArbolBinarioBusqueda` y `ArbolAVL` (paquete `redsens.indice`) indexan las estaciones por `numero`,
con inserción, búsqueda, eliminación (hoja, un hijo y dos hijos), recorridos inorden, preorden y
postorden, altura, cantidad de nodos y de hojas (recursivos, con el caso base marcado), listado por
rango y reporte de críticas. Con datos ordenados el ABB degenera en una lista y el AVL se mantiene
balanceado con rotaciones; por eso el menú usa el AVL para el rango y las críticas.
