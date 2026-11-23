# RedSens
Trabajo Práctico Integrador - AED II

Grupo 3: semilla Random `3026`, capacidad inicial de M1 `17`, base de la
cuadrilla San Francisco y M de Josephus `3`.

## Compilar y ejecutar

Requiere Java 21 con `javac` y `java` disponibles en el PATH. Desde la raíz
del repositorio:

```sh
mkdir -p out
find src/main/java src/test/java -name '*.java' -print0 | xargs -0 javac --release 21 -encoding UTF-8 -d out
java -cp out redsens.Main
```

El menú principal conecta el conjunto de estaciones, M1, M2, M3, M4 y M6.
M5 todavía no está disponible.

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
java -cp out redsens.simulacion.ComprobadorMediciones
```

`ComprobadorMediciones` escribe `datos/mediciones_grupo3.txt` y
`datos/mediciones_verificacion.txt` en el directorio de trabajo. Ejecutalo
desde un directorio temporal, no desde la raíz del proyecto, para conservar
los archivos de datos existentes. Desde la raíz del proyecto, después de
compilar, podés ejecutarlo así:

```sh
raiz_proyecto="$(pwd)"
directorio_temporal="$(mktemp -d)"
(cd "$directorio_temporal" && java -cp "$raiz_proyecto/out" redsens.simulacion.ComprobadorMediciones)
```

El experimento independiente de M2 se ejecuta con:

```sh
java -cp out redsens.indice.ExperimentoArboles 3026
```

El experimento de M1 (lineal contra cuadrática) se ejecuta con:

```sh
java -cp out redsens.registro.ExperimentoTablaHash 3026
```

## M1 registro hash

`redsens.registro.TablaHash` implementa exploración lineal y cuadrática,
inserción, búsqueda, eliminación perezosa, métricas de búsqueda y tablas
ampliables con rehashing. El menú M1 permite elegir una estrategia para buscar
por código; las operaciones de alta y baja actualizan ambas tablas junto con
los árboles. Ejemplo de construcción:

```java
TablaHash registro = new TablaHash(17, EstrategiaExploracion.LINEAL);
registro.insertar(estacion);
Estacion encontrada = registro.buscar("CBA-0347");
registro.eliminar("CBA-0347");
```

Las clases pertenecen a `redsens.registro` y usan el modelo `Estacion` común.
Los umbrales por defecto son 0,75 para lineal y 0,5 para cuadrática. Al
superarlos, la tabla ampliable crece al siguiente primo adecuado. El contrato
y la explicación de la tabla lineal están en
[el contrato de M1](resultados/contrato-m1-lineal.md).

### Práctica manual de M1

Después de compilar, abrir JShell (incluido en el JDK):

```sh
jshell --class-path out
```

Copiar las líneas de [la práctica manual](ejemplos/m1-manual.jsh) una por una
para observar inserciones con colisiones, búsquedas, borrados y contadores.
También se puede ejecutar la demostración completa:

```sh
jshell --class-path out ejemplos/m1-manual.jsh
```

La sesión queda abierta para experimentar. Escribir `/exit` para salir.
La [guía de estudio en PDF](output/pdf/guia-estudio-m1-lineal.pdf) explica
el código línea por línea, las decisiones y la integración con el equipo.

Para usar explícitamente una tabla ampliable con exploración cuadrática:

```java
TablaHash registro = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
```

Los constructores de capacidad fija también están disponibles. El experimento
comparativo ejecutable está en
`src/test/java/redsens/registro/ExperimentoTablaHash.java`; sus resultados y
explicación están en
[el informe de M1](resultados/informe-m1-tabla-hash.md).

## M3 grafos y recorridos

M3 implementa la red vial en `redsens.red` sin `HashMap` ni librerías de
grafos: lista y matriz de adyacencia, BFS por niveles, DFS y conexidad,
Dijkstra (base San Francisco y control desde Córdoba), Floyd, corte de un tramo
y plan de visitas por vecino más cercano. El grafo del enunciado (12
localidades, 18 tramos) se carga desde `datos/red_vial.txt`, por eso hay que
ejecutar desde la raíz del repositorio. Está en la opción 3 del menú; la opción
9 de ese submenú arma el plan con el reporte de críticas del AVL (M2).

```sh
java -cp out redsens.red.ExperimentoGrafo
```

Si la consola muestra mal los acentos, ejecutar antes `chcp 65001`. Las
decisiones y los resultados están en [el informe de M3](resultados/informe-m3-grafos.md).

## M4 compresión Huffman

El módulo M4 se encuentra en `redsens.compresion` y está disponible desde la
opción 4 del menú principal. Su diseño, formato RHUF1, pruebas y comandos están
documentados en [README_M4.md](README_M4.md). La demostración usa
`datos/mediciones_grupo3.txt` como entrada de solo lectura cuando contiene al
menos 5.000 mediciones; si necesita generar una entrada, la guarda bajo
`output/m4-demo/`. Los archivos comprimidos y recuperados de la demostración
también se guardan en esa carpeta. El comprobador usa una carpeta temporal.
