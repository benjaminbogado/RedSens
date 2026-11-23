# M4 - Transmisión comprimida mediante Huffman

Grupo 3. Requiere Java 21 y no utiliza dependencias externas.

## Clases

- `redsens.compresion.Nodo`: nodo hoja o interno del árbol Huffman.
- `redsens.compresion.Huffman`: frecuencias, árbol, tabla de códigos,
  comprobación de prefijos y codificación/decodificación.
- `redsens.compresion.ArchivoHuffman`: empaqueta y descomprime archivos.
- `redsens.compresion.TransmisionComprimida`: interfaz sencilla de integración.
- `redsens.compresion.DemostracionHuffman`: demostración ejecutable.
- `redsens.compresion.PruebaHuffman`: pruebas ejecutables sin `-ea`.

No se utilizan estructuras prohibidas ni se duplican paquetes existentes.

## Empates y reproducibilidad

Cada nodo guarda el símbolo mínimo de su subárbol. La cola de prioridad
compara frecuencia y luego ese mínimo en tiempo O(1), de forma determinista.

## Casos probados

El caso `WWXWYZWX` genera 64 bits originales y 14 bits Huffman útiles.
También se prueban entrada vacía, un único símbolo repetido, frecuencias
empatadas, tildes y saltos de línea.

## Archivo binario

El formato se identifica por `RHUF` y contiene:

- versión;
- longitud original en bytes;
- cantidad de bits útiles;
- bits de relleno del último byte;
- tabla de símbolos y códigos;
- payload Huffman empaquetado.

Cada código se guarda como una cadena de ceros y unos, pero la cadena
de longitud variable se empaqueta en bits dentro del contenedor real.
El archivo no contiene caracteres ASCII `"0"` y `"1"` como payload.

El relleno de los últimos bytes no forma parte de los bits útiles.
La cabecera incluye la tabla y se mide por separado del payload.

El contenido original se conserva byte por byte. Para mediciones ASCII,
un carácter suele ocupar un byte y ambas magnitudes coinciden. En texto
UTF-8, bytes y caracteres pueden diferir; por eso el tamaño original del
archivo se calcula por bytes. El caso pequeño usa caracteres ASCII y
muestra explícitamente 8 bits por carácter.
Los símbolos no ASCII se identifican como bytes `0xNN`, no como puntos
de código Unicode.

La demostración del menú lee `datos/mediciones_grupo3.txt` sin modificarlo
cuando tiene al menos 5.000 líneas. Si ese archivo falta o tiene menos líneas,
genera su entrada de demostración en `output/m4-demo/mediciones-generadas.txt`.
Los archivos comprimido y recuperado se identifican en `output/m4-demo/`.
Las pruebas escriben sus entradas y salidas en carpetas temporales.

Los tres archivos canónicos `resultados/ejecucion-m4.txt`,
`resultados/mediciones-m4.huff` y
`resultados/mediciones-m4-descomprimidas.txt` se regeneran desde la misma
entrada versionada. La corrida anterior usó una entrada de 262.001 bytes; se
conservó como evidencia separada en
`resultados/historico-m4-262001-bytes/`, donde el informe, el contenedor y la
salida recuperada corresponden entre sí. El contenedor histórico se decodificó
para completar ese conjunto y validar su SHA-256 contra el informe anterior.

Un archivo truncado, con magic o versión inválidos, tabla inconsistente,
código duplicado, relleno inválido o longitud incorrecta se rechaza con
una excepción `IOException` clara.

## Compresión

Para bytes originales `B`:

`bits originales = B * 8`

Para caracteres ASCII:

`bits originales = cantidad de caracteres * 8`

`bits Huffman útiles = suma(frecuenciaSímbolo * longitudCódigoSímbolo)`

`tasa de ahorro = (1 - tamañoComprimidoReal / tamañoOriginal) * 100`

El tamaño real incluye cabecera, tabla y relleno; la tasa para los bits
útiles se informa por separado.

## Complejidad

Sea `B` la cantidad de bytes de entrada, `s` la cantidad de símbolos
distintos, `L` la longitud máxima de código y `U` la cantidad de bits
codificados útiles:

- conteo de frecuencias: `O(B + 256)` tiempo y `O(256)` espacio;
- construcción del árbol con PriorityQueue: `O(s log s)` tiempo y `O(s)`
  espacio; el comparador usa frecuencia y símbolo mínimo en `O(1)`;
- generación de la tabla: `O(s * L)` debido a los prefijos de código;
- comprobación de prefijos: `O(s² * L)` por comparar pares de códigos;
- codificación: `O(B * L)` tiempo, incluida la concatenación de cada código;
- decodificación del contenedor: `O(U * L + B)` tiempo por recorrer los
  bits, consultar los prefijos y producir los `B` bytes;
- memoria de codificación: `O(B + U + s * L)` para bytes de entrada,
  texto de bits y tabla.

## Por qué comprime las mediciones

Las mediciones pueden comprimirse bastante porque repiten dígitos,
separadores, etiquetas, fechas y ciertos patrones de texto. Pero no se
comprimen tanto como un texto dominado por un único símbolo: la variedad
de caracteres tiene frecuencias menos desbalanceadas, por lo que algunos
códigos tienen que ser largos y la entropía efectiva es mayor.

Esta conclusión se obtiene de las frecuencias reales; no se fuerza
ninguna tasa ni ningún resultado.

## Compilación y ejecución

Desde la raíz del repositorio:

```sh
mkdir -p out
find src/main/java src/test/java -name '*.java' -print0 | xargs -0 javac --release 21 -encoding UTF-8 -d out
java -cp out redsens.compresion.PruebaHuffman
java -cp out redsens.compresion.DemostracionHuffman
java -cp out redsens.compresion.PruebaHuffman descomprimir output/m4-demo/mediciones-m4.huff
java -cp out redsens.Main
```

En VS Code, abrir `PruebaHuffman.java` o `DemostracionHuffman.java` y
ejecutar su método `main` con `Run Java`.

## Integración con el menú

La opción `4` del menú principal está conectada a
`DemostracionHuffman.ejecutar()`. El módulo también se puede usar directamente
desde otras clases:

```java
TransmisionComprimida servicio = new ArchivoHuffman();
ArchivoHuffman.ResultadoArchivo resultado =
    servicio.comprimir(Path.of("entrada"), Path.of("salida.huff"));
byte[] recuperados = servicio.descomprimir(Path.of("salida.huff"));
```
