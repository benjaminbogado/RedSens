# RedSens
Trabajo Práctico Integrador - AED II

Grupo 3: semilla Random `3026`, capacidad inicial de M1 `17`, base de la
cuadrilla San Francisco y M de Josephus `3`.

## Compilar y ejecutar

Requiere un JDK 17 o superior con `javac` y `java` disponibles en el PATH.
Desde la raíz del repositorio, en PowerShell:

```powershell
$fuentes = @(Get-ChildItem src/main/java,src/test/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -encoding UTF-8 --release 17 -d out @fuentes
java -cp out redsens.Main
```

Si `javac` no se reconoce, agregar la carpeta `bin` del JDK al PATH o invocar
ambos ejecutables mediante su ruta completa. `Main` por ahora muestra el
mensaje de inicio; el menú integrado está pendiente.

## Pruebas ejecutables

Después de compilar:

```powershell
java -cp out redsens.registro.PruebaTablaHash
java -cp out redsens.registro.PruebaTablaHashCuadratica
java -cp out redsens.indice.PruebaABB
java -cp out redsens.indice.ComprobadorArbolAVL
```

El experimento independiente de M2 se ejecuta con:

```powershell
java -cp out redsens.indice.ExperimentoArboles 3026
```

El experimento de M1 (lineal contra cuadrática) se ejecuta con:

```powershell
java -cp out redsens.registro.ExperimentoTablaHash 3026
```

## M1 lineal

FEDE desarrolla la base de `TablaHash`, la exploración lineal, el borrado
perezoso y los contadores. Ejemplo de construcción:

```java
TablaHash registro = new TablaHash(17, EstrategiaExploracion.LINEAL);
registro.insertar(estacion);
Estacion encontrada = registro.buscar("CBA-0347");
registro.eliminar("CBA-0347");
```

Las clases pertenecen a `redsens.registro` y usan el modelo `Estacion` común.
Esta entrega tiene capacidad fija: el integrante de M1 cuadrática deberá
agregar esa estrategia, los umbrales de carga y el rehashing. El experimento
comparativo de M1 también está pendiente. El contrato, las decisiones y la
guía para continuar están en [el contrato de M1](resultados/contrato-m1-lineal.md).

### Práctica manual de M1

Después de compilar, abrir JShell (incluido en el JDK):

```powershell
jshell --class-path out
```

Copiar las líneas de [la práctica manual](ejemplos/m1-manual.jsh) una por una
para observar inserciones con colisiones, búsquedas, borrados y contadores.
También se puede ejecutar la demostración completa:

```powershell
jshell --class-path out ejemplos/m1-manual.jsh
```

La sesión queda abierta para experimentar. Escribir `/exit` para salir.
La [guía de estudio en PDF](output/pdf/guia-estudio-m1-lineal.pdf) explica
el código línea por línea, las decisiones y la integración con el equipo.

## M1 cuadrática y rehashing

FRANCO agrega la exploración cuadrática, el umbral de carga, el rehashing y
la comparación de estrategias sobre la `TablaHash` de FEDE. Los constructores
de capacidad fija no cambian; para la tabla que crece sola:

```java
TablaHash registro = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
```

Umbrales por defecto: 0,75 para lineal y 0,5 para cuadrática. Al superarlo,
la tabla crece al primer primo mayor o igual al doble. Las decisiones, los
resultados del experimento y su explicación están en
[el informe de M1](resultados/informe-m1-tabla-hash.md).
