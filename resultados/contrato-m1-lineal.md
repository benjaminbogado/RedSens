# M1 lineal y contrato de TablaHash

Responsable: FEDE. Grupo 3. Rama de trabajo: `codex/tabla-hash-lineal`.

Esta entrega resuelve la base del registro y la exploración lineal. Usa el
modelo común `Estacion`, sin `HashMap`, `TreeMap` ni bibliotecas externas.
La exploración cuadrática, el rehashing y la comparación de estrategias
corresponden al integrante 4. Este contrato es la propuesta de integración
para revisar con ese integrante.

## Operaciones disponibles

| Operación | Resultado o comportamiento |
|---|---|
| `new TablaHash(capacidad)` | Crea una tabla lineal vacía de capacidad positiva. |
| `new TablaHash(capacidad, estrategia)` | Selecciona la estrategia al construir. Por ahora solo existe `LINEAL`. |
| `insertar(Estacion)` | Almacena la misma instancia; rechaza null y códigos duplicados con `IllegalArgumentException`. |
| `buscar(String codigo)` | Devuelve la estación o `null` si no existe. |
| `eliminar(String codigo)` | Marca como borrado; lanza `IllegalArgumentException` si no existe. |
| `tamanio()`, `estaVacia()`, `capacidad()` | Informan el estado sin modificar mediciones. |
| `factorCarga()` | Estaciones activas divididas por capacidad; no cuenta marcas de borrado. |
| `getSondeosUltimaOperacion()`, `getColisionesUltimaOperacion()` | Medición de la última inserción, búsqueda o eliminación. |
| `getSondeosTotales()`, `getColisionesTotales()` | Acumulados desde la creación o el último reinicio. |
| `reiniciarContadores()` | Pone los cuatro contadores en cero y conserva los datos. |

Buscar y eliminar validan el mismo formato que `Estacion`: `CBA-` seguido
de cuatro dígitos ASCII. Los argumentos inválidos lanzan
`IllegalArgumentException` sin sondear casillas. Las operaciones rechazadas
por duplicado, ausencia o capacidad conservan los sondeos que sí realizaron.
El comportamiento de duplicados y eliminaciones inexistentes coincide con
el de los árboles del equipo.

## Hash y exploración

La función acumula los caracteres de la cadena mediante
`h = (31 * h + caracter) % capacidad`. Procesa toda la clave con una base
pequeña, conserva la influencia del orden de sus dígitos y reduce en cada
paso. El cálculo intermedio usa `long` para evitar desbordar un `int`.
El prefijo común no agrega variedad; la dispersión depende de los cuatro
dígitos y de la capacidad. No se afirma que evite colisiones.

La estrategia lineal visita `(h + i) % capacidad`, con `i` desde cero hasta
`capacidad - 1`. Por eso recorre todas las casillas y termina incluso si
la tabla está llena o todas las casillas fueron borradas. La función hash
cuesta O(longitud del código), que aquí es fija. Insertar, buscar y eliminar
son O(1) en promedio bajo una carga controlada y una dispersión adecuada,
y O(capacidad) en el peor caso. El almacenamiento es O(capacidad).

## Estados y borrado perezoso

- Una casilla `null` nunca estuvo ocupada y permite terminar una búsqueda.
- Una casilla existente con estación guarda un registro activo.
- Una casilla existente con estación `null` es una marca de borrado.

Buscar y eliminar atraviesan las marcas. Insertar recuerda la primera marca,
pero sigue buscando para descartar que el código ya exista más adelante.
Cuando encuentra una casilla nunca ocupada, o completa el ciclo, reutiliza
la primera marca disponible. Borrar libera la referencia al objeto sin
perder el estado lógico de la casilla.

## Convención de medición

Un sondeo es cada lectura de una casilla durante una operación, incluidas
las casillas vacías y borradas. Una colisión es encontrar una casilla activa
con un código diferente al consultado o insertado. Una coincidencia y una
marca de borrado no son colisiones. La escritura en una posición ya
examinada no añade otro sondeo. Estas reglas se aplican a las tres operaciones.

Para promediar búsquedas, reiniciar los contadores antes de cada lote y
dividir `getSondeosTotales()` por la cantidad de búsquedas realizadas.
Separar búsquedas exitosas y no exitosas en lotes distintos.

## Qué debe continuar el integrante 4

1. Agregar `CUADRATICA` en `EstrategiaExploracion`, conservando las mismas
   operaciones, estados y reglas de medición.
2. Definir la fórmula cuadrática y justificar su cobertura. Con capacidad
   prima impar y desplazamiento `i * i`, no se visitan todas las casillas;
   mantener una carga inferior a 0,5 permite garantizar una posición libre.
   Calcular los productos con `long`.
3. Agregar un umbral justificado para cada estrategia. Para lineal se puede
   evaluar 0,7 y para cuadrática un límite estricto inferior a 0,5, según la
   fórmula que se acuerde. Son propuestas, todavía no reglas implementadas.
4. Hacer ampliable el arreglo y reinsertar las estaciones activas al crecer
   al primer primo mayor o igual al doble de la capacidad actual. Descartar
   marcas de borrado y decidir también cuándo limpiarlas sin crecer.
5. Contar rehashings y separar sus sondeos internos de los de las operaciones
   del usuario, para no contaminar la comparación.
6. Agregar pruebas de crecimiento, conservación de datos y ambas estrategias;
   luego ejecutar el experimento con las estaciones generadas por M6.

Hasta esa ampliación, una inserción sin casilla disponible lanza
`IllegalStateException` y conserva los datos. La capacidad inicial del grupo
es 17; no se pueden insertar 10.000 estaciones en esa capacidad fija.
La prueba de integridad usa 20.011 casillas para aislar esta primera parte.

Los códigos válidos abarcan exactamente `CBA-0000` a `CBA-9999`. Después de
insertar los 10.000 no quedan códigos válidos ausentes. Para medir búsquedas
no exitosas habrá que acordar y documentar un escenario adicional, por
ejemplo borrar una muestra después de medir el estado completo y buscar
esos códigos. No usar códigos inválidos: se rechazan antes de sondear.

## Validación realizada

Se compiló todo el código de producción y pruebas con JDK 23 y
`--release 17`, sin dependencias adicionales. `PruebaTablaHash` pasó 15.205
verificaciones, incluidos contadores exactos con colisiones conocidas,
exploración circular, duplicados detrás de borrados, capacidad agotada,
todas las casillas borradas, capacidad uno, entradas inválidas, 3.000
operaciones aleatorias contra un arreglo de referencia y almacenamiento y
búsqueda de los 10.000 códigos válidos. Semilla de las pruebas: 3026.

Antes de cambiar código también pasaron las 57 verificaciones del ABB y
todas las comprobaciones existentes del AVL. Sus implementaciones no se
modificaron. Las pruebas construyen datos propios para aislar M1; todavía
no se integró el generador M6 de LUCA ni se midió rendimiento comparativo.

## Registro para la bitácora de IA

Codex asistió en la lectura del enunciado y el reparto, la implementación
de M1 lineal, las pruebas y esta documentación. El equipo debe revisar y
poder explicar el código antes de presentarlo. No se atribuyen aquí partes
escritas manualmente por el integrante ni revisiones que aún no ocurrieron.
Durante la preparación de la guía de estudio, Codex indicó en un borrador
que los códigos CBA-0000, CBA-0013 y CBA-0026 tenían hash inicial 16 módulo
17. La comprobación independiente de la fórmula dio 12 para los tres.
Se corrigieron las posiciones de la traza a 12, 13 y 14, antes de publicar
la guía; el algoritmo Java y sus resultados de sondeos no requirieron
cambios. Es un error real de documentación detectado en la validación
asistida, no una revisión humana que se atribuya al integrante.
Queda pendiente completar la bitácora final con la revisión humana y
las modificaciones propias reales.
