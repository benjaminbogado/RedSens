# RedSens · M1 — Exploración cuadrática, rehashing y comparación de estrategias

Grupo 3 · Integrante 4 (Franco) · Semilla 3026 · Capacidad inicial 17 · Java 21 · Datos: `GeneradorDatos` (M6)

## 1. Qué incluye esta rama (`feat/tabla-hash-cuadratica`)

La rama parte de `main`, que ya incluye la tabla lineal de FEDE (PR #5) y el generador de LUCA (PR #6), y agrega la segunda parte de M1 sobre su `TablaHash`, sin cambiar su comportamiento de capacidad fija.

| Archivo | Cambio |
|---|---|
| `registro/EstrategiaExploracion.java` | Se agrega `CUADRATICA` (h + i²) debajo de `LINEAL`. |
| `registro/Primos.java` | Nuevo: `esPrimo` y `siguientePrimo`. |
| `registro/TablaHash.java` | Modo ampliable con umbral, rehashing al siguiente primo del doble, limpieza de marcas de borrado, contador de borradas, `factorOcupacion()`, `getRehashings()`, `getLimpiezas()` y `umbralPorDefecto()`. Solo se agregan cosas; lo de FEDE no se modifica, salvo lo indispensable para que el arreglo pueda crecer. |
| `test/.../registro/PruebaTablaHashCuadratica.java` | 96 verificaciones (todas OK). |
| `test/.../registro/ExperimentoTablaHash.java` | Experimento obligatorio + control + barrido de factor de carga. |
| `resultados/experimento-m1.txt` | Salida completa de la corrida. |

Las pruebas de FEDE (`PruebaTablaHash`, 15.205 verificaciones), el ABB y el AVL siguen pasando sin cambios.

```powershell
java -cp out redsens.registro.PruebaTablaHash
java -cp out redsens.registro.PruebaTablaHashCuadratica
java -cp out redsens.registro.ExperimentoTablaHash        # semilla 3026 por defecto
```

## 2. Cómo se usa

```java
// Capacidad fija (lo de FEDE, sin cambios): si se llena, insertar lanza IllegalStateException.
TablaHash fija = new TablaHash(17, EstrategiaExploracion.LINEAL);

// Ampliable con el umbral por defecto (0,75 lineal, 0,5 cuadrática). Es la que usa el sistema.
TablaHash registro = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);

// Ampliable con umbral explícito (para experimentos o el cambio en vivo de la exposición).
TablaHash prueba = new TablaHash(17, EstrategiaExploracion.LINEAL, 0.5);

registro.factorCarga();       // activas / capacidad
registro.factorOcupacion();   // (activas + borradas) / capacidad  <- decide la ampliación
registro.getRehashings();     // veces que creció (no se reinicia con reiniciarContadores)
registro.getLimpiezas();      // reconstrucciones sin crecer para descartar marcas
```

Se mantienen todas las reglas del contrato de FEDE: duplicados y eliminaciones inexistentes lanzan `IllegalArgumentException`, buscar un código ausente devuelve `null`, y sondeos y colisiones se cuentan igual. Las reinserciones internas del rehashing no se suman a los contadores, como pedía el punto 5 de su contrato.

## 3. Decisiones de diseño

**Exploración cuadrática.** Intento *i* en `(h + i²) mod m`. Con *m* primo, las ⌈m/2⌉ primeras posiciones son todas distintas (verificado para todos los *h* en m = 17, 37, … 1361). Con *m* = 16, en cambio, desde 0 solo se alcanzan 4 casillas: por eso la capacidad siempre es prima.

**Umbral cuadrática = 0,5.** Es el límite del teorema anterior: si a lo sumo la mitad de la tabla está ocupada, siempre hay una casilla libre alcanzable. Por encima no está garantizado.

**Umbral lineal = 0,75.** La lineal siempre encuentra lugar si queda alguna casilla, así que el umbral solo controla el costo. Con λ = 0,75 una búsqueda fallida cuesta en teoría 8,5 sondeos; con λ = 0,9 ya son 50,5.

**Rehashing.** Antes de insertar, si `(activas + borradas + 1)` superaría `umbral · m`, la capacidad pasa al primer primo mayor o igual a `2·m` y se reinsertan solo las activas (las marcas de borrado se descartan). Desde 17: 37, 79, 163, 331, 673, 1361, 2729, 5471, 10949, 21911.

**Las borradas cuentan para el umbral.** Una casilla borrada no corta la búsqueda, así que ocupa lugar en la secuencia de exploración igual que una ocupada. Si no se contaran, la cuadrática podría superar 0,5 de ocupación real y perder la garantía. Prueba: 4 ocupadas + 4 borradas en m = 17 → la 9.ª inserción dispara rehashing aunque haya solo 5 estaciones.

**Limpieza sin crecer.** Si al llegar al umbral casi todo lo ocupado son marcas (las activas + 1 no superan la mitad del umbral), se reconstruye con la misma capacidad en lugar de duplicar. Responde al punto 4 del contrato de FEDE ("decidir cuándo limpiar marcas sin crecer"). Prueba: 1 activa + 7 borradas en m = 17 → se limpia y sigue en 17.

**Inserción con borradas.** Se recorre hasta una casilla vacía (para asegurar que el código no exista más adelante) y se inserta en la primera borrada encontrada, si la hubo.

## 4. Resultados (10.000 estaciones, semilla 3026, capacidad inicial 17)

### 4.1 Experimento obligatorio

| Estrategia | Umbral | Colisiones al insertar | Rehashings | Capacidad final | λ final | Sondeos prom. búsqueda exitosa | Sondeos prom. búsqueda fallida (con 1.000 borradas) | Sondeos prom. búsqueda fallida (sin borradas) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Lineal | 0,75 | 99.993 | 10 | 21.911 | 0,456 | 2,296 | 11,502 | 4,577 |
| Cuadrática | 0,50 | 9.067 | 10 | 21.911 | 0,456 | 1,517 | 4,599 | 2,176 |

*Búsqueda fallida.* Siguiendo la propuesta del contrato de FEDE: con los 10.000 códigos CBA-NNNN cargados no queda ningún código válido ausente. Se midió de dos formas: (a) en la tabla completa se borran perezosamente 1.000 al azar y se buscan; (b) se arma otra tabla con 9.000 y se buscan las 1.000 que faltan.

### 4.2 Control: ambas con umbral 0,5

| Estrategia | Colisiones al insertar | Sondeos exitosa | Fallida c/borradas | Fallida s/borradas |
|---|---:|---:|---:|---:|
| Lineal | 20.187 | 2,296 | 11,502 | 4,577 |
| Cuadrática | 9.067 | 1,517 | 4,599 | 2,176 |

Con el mismo umbral la lineal sigue con más del doble de colisiones: la diferencia no es solo por el umbral, es el agrupamiento primario. Las búsquedas dan igual que en 4.1 porque ambas configuraciones terminan reconstruidas en m = 21.911 con los mismos datos.

### 4.3 Barrido de factor de carga (m = 10.007 fijo, sin rehashing)

| λ | Lineal exitosa (teoría) | Lineal fallida (teoría) | Cuadrática exitosa | Cuadrática fallida |
|---:|---:|---:|---:|---:|
| 0,3 | 1,10 (1,21) | 1,31 (1,52) | 1,09 | 1,25 |
| 0,5 | 1,36 (1,50) | 2,80 (2,50) | 1,24 | 1,87 |
| 0,7 | 3,10 (2,17) | 19,30 (6,06) | 1,64 | 4,00 |
| 0,9 | 19,56 (5,50) | 199,09 (50,50) | 3,01 | 15,01 |

(La tabla completa está en `experimento-m1.txt`. Para λ > 0,5 la cuadrática se corrió con umbral 0,95 solo como prueba; ahí ya no tiene garantía.)

## 5. Explicación

1. **Agrupamiento primario.** En la lineal, todas las claves que caen en cualquier punto de un bloque ocupado terminan alargando ese mismo bloque: los bloques crecen y se fusionan. La cuadrática salta 1, 4, 9, 16… posiciones y sale rápido del bloque, así que las claves que colisionan se dispersan. Por eso, con el mismo λ, la lineal hace más sondeos, y la diferencia crece mucho con λ (a 0,9: 19 contra 3 sondeos en búsqueda exitosa).
2. **La búsqueda fallida es la que más sufre.** Tiene que recorrer el bloque completo hasta una casilla vacía; en la lineal ese bloque es largo. Es la fila con mayor diferencia entre estrategias.
3. **Costo del borrado perezoso.** Las 1.000 casillas borradas no cortan la búsqueda, así que las búsquedas fallidas pasan de 4,2 a 11,4 sondeos en la lineal y de 2,1 a 4,6 en la cuadrática. Por eso las borradas cuentan para el umbral y el rehashing las elimina.
4. **Rehashings.** Ambas hacen 10 y terminan en 21.911 porque el umbral solo decide *cuándo* se duplica; para 10.000 claves las dos necesitan pasar 10.949 (10.000 / 10.949 = 0,91 supera ambos umbrales).
5. **Hallazgo sobre la función hash.** La lineal da peor que la fórmula de Knuth a partir de λ ≈ 0,6 (a 0,9: 199 contra 50,5 sondeos en búsqueda fallida). Causa: el polinomio de Horner en base 31 conserva el orden de los códigos, porque CBA-0347 y CBA-0348 caen en casillas consecutivas. Las claves ya llegan "agrupadas" y la lineal, que también avanza de a una casilla, empeora el agrupamiento. En una prueba aparte (con datos propios, antes de tener el generador de M6, y sin cambiar el código del repo) agregué una mezcla multiplicativa al final del hash: la lineal pasó a coincidir con la teoría (a 0,9: 46,9 contra 50,5) y las colisiones al insertar bajaron de 111.154 a 23.715. La cuadrática casi no se ve afectada. Esto es un buen tema para la exposición: la función hash "justificada" también tiene que romper los patrones de las claves. Lo dejo planteado; decidir si se cambia le corresponde a FEDE.

## 6. Notas para el equipo

- **Por qué la ampliación es opcional.** Las pruebas de FEDE verifican el comportamiento con capacidad fija (tabla de 5 llena, capacidad 1). Para no romperlas, sus dos constructores siguen igual, y la ampliación se activa con `TablaHash.ampliable(...)` o pasando el umbral. El menú y M6 deben crear el registro con `TablaHash.ampliable(17, ...)`.
- **Umbral de la lineal.** El contrato de FEDE proponía evaluar 0,7. Se eligió 0,75 por lo explicado en la sección 3. Se puede cambiar en `TablaHash.umbralPorDefecto` sin tocar nada más.
- **Duplicados.** Un insert que va a ser rechazado por duplicado no dispara la ampliación.
- **Tiempo de búsqueda.** `buscar` y `eliminar` validan el código con `String.matches`, que compila la expresión regular en cada llamada. Eso sube el tiempo por búsqueda de ~100 ns a ~850 ns. No cambia sondeos ni colisiones, que son lo que pide el experimento. Si se quiere, se resuelve con un `Pattern` estático precompilado.
- **M6.** El experimento usa `GeneradorDatos.generarEstaciones(10000, 3026)` de LUCA, sin modificarlo.
