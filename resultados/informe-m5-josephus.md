# M5 Guardias rotativas

Responsable: FEDE / Federico Villagra. Grupo 3. Fecha: 22 de noviembre de 2026.

## Comportamiento

Los técnicos se identifican de 1 a N. Cada asignado sale de la ronda; los
N-1 eliminados cubren los fines de semana en ese orden y el último es suplente.
N y M deben ser positivos. Con N=1, no hay asignaciones y el técnico 1 es suplente.
`CalendarioGuardias` copia la lista y la expone inmodificable.

M cuenta pasadas: se omiten M técnicos y se asigna al siguiente. Por eso
N=5,M=1 produce [2,4,1,5], suplente 3. La convención alternativa de contar
al primero como M=1 sería incorrecta para este enunciado.

Grupo 3, N=12,M=3: [4,8,12,5,10,3,11,7,6,9,2], suplente 1.
El calendario se muestra desde Main, opción 5; no depende del registro de estaciones.

## Estructuras propias y complejidades

- `JosephusLista`: LinkedList e Iterator; cuando se agota, se reinicia antes
  de next. Elimina mediante ese mismo iterador, sin invalidarlo por una baja
  externa. Construcción O(N), resolución O(N*M), memoria O(N).
- `ArbolOrdenEstadistico`: ABB propio, construido equilibrado por puntos
  medios en O(N). Cada nodo guarda el número de técnico y el tamaño de su
  subárbol. Seleccionar y eliminarK usan el tamaño izquierdo para encontrar
  el rango cero-based, y actualizan metadatos en el regreso recursivo.
- `JosephusArbol`: mantiene índice = (índice + (long) M) % restantes y elimina
  el elemento de ese rango. El long evita desbordar la suma con M grande.
  Es O(N log N), memoria O(N), incluida la salida.

El árbol no necesita rotaciones en este caso: se construye una sola vez y
después solo borra. Quitar hojas, promover un hijo o reemplazar por el sucesor
no aumenta la altura original O(log N). Esto garantiza el costo logarítmico
respecto del N inicial, aunque el árbol no sea AVL respecto de los restantes.
No ofrece inserciones posteriores: añadirlas requeriría revisar esa garantía.
No se reutiliza el AVL de estaciones porque tiene otro modelo y no tiene rangos estadísticos.

## Validación

`PruebaJosephus`: 34.745 verificaciones correctas. Comprueba el caso oficial,
el grupo, N=1, todas las combinaciones N=1..80 con M=1,2,3,7,1000 contra un
oráculo pequeño de ArrayList, y M=Integer.MAX_VALUE en el árbol. Verifica
todos los rangos y metadatos del ABB tras 257 bajas aleatorias con semilla
3026, entradas inválidas, estado vacío e inmutabilidad de resultados.

También pasan M1 lineal (15.205), cuadrática (96), ABB (57), comprobaciones
del AVL, reproducibilidad M6 y límites. Se probó el menú con calendario de
grupo, control N=5,M=1, parámetros inválidos, regreso y salida.

## Experimento obligatorio medido

Fuente: `experimento-m5.txt`. OpenJDK 23, Windows 11 amd64. System.nanoTime,
tres calentamientos por escenario y mediana de cinco corridas, alternando
qué estrategia va primero. Incluye construcción de la estructura y lista
de asignaciones; excluye impresión y comparación de calendarios. Se comparan
los calendarios completos en todas las rondas medidas. M=3 es el del grupo;
M=1000 muestra el costo de muchas pasadas.

| N | M | Lista ms | ABB ms | Lista / ABB |
|---:|---:|---:|---:|---:|
| 1.000 | 3 | 0,360 | 0,234 | 1,54 |
| 1.000 | 1000 | 7,132 | 0,217 | 32,84 |
| 10.000 | 3 | 2,923 | 1,574 | 1,86 |
| 10.000 | 1000 | 23,857 | 1,506 | 15,84 |
| 100.000 | 3 | 2,942 | 12,348 | 0,24 |
| 100.000 | 1000 | 375,516 | 22,088 | 17,00 |

Con M pequeño la lista es competitiva: para M constante su recorrido es
lineal en N, frente al trabajo de rangos del árbol. Con 100.000 y M=3, en
esta corrida gana la lista. Con muchas pasadas, la lista las recorre y el
árbol las resume con aritmética de rango; para 100.000 y M=1000 fue 17 veces
más rápido. No se promete que el árbol gane siempre. JIT, caché, GC y carga
del sistema afectan los tiempos y explican variaciones entre tamaños;
los resultados deterministas sí deben coincidir exactamente.

## Reproducir

Compilar como indica README y ejecutar:

```powershell
java -cp out redsens.guardias.PruebaJosephus
java -cp out redsens.guardias.ExperimentoJosephus
java -cp out redsens.Main
```

El experimento es la opción 3 del submenú M5. Para N y M grandes, la versión
simple tarda proporcionalmente a las pasadas, tal como se mide. Los tiempos
de una nueva corrida cambiarán; no se deben copiar como si fueran constantes.
