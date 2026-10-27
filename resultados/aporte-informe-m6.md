# Aporte de M6 al informe técnico — Simulación y pruebas

Responsable: Luca Agustín Pepino.
Grupo: 3. Semilla: 3026.
Entorno de ejecución: Java 24.0.2, Windows 11 amd64.

## 1. Objetivo

M6 proporciona datos sintéticos reproducibles y herramientas
para verificar el comportamiento y medir el rendimiento
de las estructuras de RedSens.

Se implementaron la generación de estaciones, la generación
de mediciones, las comprobaciones de reproducibilidad y límites,
y una herramienta reutilizable de medición de tiempos.

## 2. Generación de estaciones

GeneradorDatos utiliza java.util.Random con semilla explícita.
La misma cantidad y semilla producen estaciones con los mismos
atributos y en el mismo orden, manteniendo la implementación.

Los códigos CBA-NNNN se obtienen seleccionando números de
0 a 9999 sin repetición mediante Fisher–Yates parcial.
Esto garantiza códigos únicos dentro de cada lista generada
y permite generar hasta 10.000 estaciones.

Se utilizan las 12 localidades del enunciado y los tipos
definidos por el modelo. La batería se genera entre 0 y 100
inclusive. Para los días sin visita se eligió un rango
de simulación de 0 a 365 inclusive.

El costo temporal es O(U + N) y el espacio es O(U + N),
donde U = 10.000 es el universo de códigos y N es la cantidad
de estaciones solicitadas.

## 3. Verificaciones y resultados

Se verificaron los siguientes casos:

| Comprobación | Resultado |
|---|---|
| Generación de 10.000 códigos únicos | Correcto |
| Dos generaciones con semilla 3026 | Mismos datos y orden |
| Comparación de semillas 3026 y 3027 | Secuencias diferentes |
| Cantidades 0, 1 y 10.000 | Aceptadas |
| Cantidades -1 y 10.001 | Rechazadas |
| Archivo de 5.000 mediciones | Cantidad correcta |
| Formato de todas las mediciones | Correcto |
| Dos archivos con los mismos parámetros | Idénticos byte a byte |

Estas verificaciones cubren los casos indicados y no sustituyen
las pruebas de integración del sistema completo.

## 4. Distribución de baterías

La batería se obtiene con nextInt(101), por lo que cada entero
de 0 a 100 tiene la misma probabilidad de ser seleccionado.

Para 10.000 estaciones, cada intervalo de diez valores tiene
frecuencia esperada 10.000 × 10 / 101 ≈ 990,10.
El valor 100 se presenta separado y tiene frecuencia esperada
10.000 / 101 ≈ 99,01.

| Batería | Frecuencia observada | Frecuencia esperada |
|---|---:|---:|
| 0–9 | 1009 | 990,10 |
| 10–19 | 973 | 990,10 |
| 20–29 | 979 | 990,10 |
| 30–39 | 955 | 990,10 |
| 40–49 | 1005 | 990,10 |
| 50–59 | 1006 | 990,10 |
| 60–69 | 1028 | 990,10 |
| 70–79 | 1012 | 990,10 |
| 80–89 | 980 | 990,10 |
| 90–99 | 961 | 990,10 |
| 100 | 92 | 99,01 |

Las frecuencias observadas son próximas a las esperadas.
No se exige igualdad exacta. El análisis es descriptivo;
no se realizó una prueba estadística formal de uniformidad.

## 5. Archivo de mediciones

Se generaron 5.000 registros asociados a 1.000 estaciones,
con fecha fija 2026-10-13 y semilla 3026.

Cada registro contiene código, fecha, hora, temperatura,
humedad, precipitación y batería. Se utiliza UTF-8,
punto decimal mediante Locale.ROOT y salto de línea LF.

Los registros se distribuyen entre estaciones y a lo largo
del mismo día. Son datos sintéticos para pruebas;
no representan un modelo meteorológico realista.

## 6. Comparación ABB/AVL

Se ejecutó el experimento comparativo desarrollado por
el equipo para 1.000, 5.000 y 10.000 estaciones,
con inserción ascendente y aleatoria.

Los seis escenarios superaron las verificaciones del programa.

| Estaciones | Orden | Altura ABB | Altura AVL |
|---|---|---:|---:|
| 1.000 | Ascendente | 999 | 9 |
| 1.000 | Aleatorio | 18 | 11 |
| 5.000 | Ascendente | 4999 | 12 |
| 5.000 | Aleatorio | 28 | 14 |
| 10.000 | Ascendente | 9999 | 13 |
| 10.000 | Aleatorio | 30 | 15 |

La altura se cuenta en aristas: una hoja tiene altura 0.

Con inserción ascendente, el ABB degenera en una cadena:
su altura es N − 1 y la búsqueda tiene costo O(N).
El AVL conserva una altura O(log N) mediante rebalanceo.

Con inserción aleatoria, el ABB obtuvo alturas mucho menores.
Este comportamiento es compatible con su costo esperado
logarítmico bajo inserciones en orden aleatorio, aunque
no ofrece la garantía de equilibrio del AVL.

El experimento separa construcción y búsqueda.
Para búsquedas utiliza tres rondas de calentamiento y diez
rondas medidas, alternando qué árbol se mide primero.
Los tiempos se obtienen con System.nanoTime().

La salida completa, incluidos los tiempos de esta computadora,
se conserva en resultados/experimento-m2-luca.txt.
Las diferencias pequeñas de tiempo no permiten establecer
superioridad sin analizar la variabilidad de las mediciones.

## 7. Experimentos pendientes

Esta sección debe completarse con resultados reales antes
de integrar la versión final del informe.

M1: comparar hash lineal y cuadrática con 10.000 estaciones,
capacidad inicial 17, colisiones, sondeos de búsquedas
exitosas y fallidas, y cantidad de rehashes.

Para búsquedas fallidas deben prepararse claves válidas
ausentes. Como los 10.000 códigos agotan el universo CBA-NNNN,
será necesario definir y documentar un escenario adicional,
por ejemplo, eliminando una muestra antes de buscarlas.

M5: comparar las dos implementaciones de Josephus con
N = 1.000, 10.000 y 100.000, utilizando al menos dos valores
de M e incluyendo M = 3, asignado al grupo.

En ambos experimentos se comprobará la corrección antes
de interpretar los tiempos, se usarán entradas equivalentes
y se documentará qué operaciones incluye cada medición.

## 8. Evidencias y limitaciones

Las evidencias están en:

- resultados/verificacion-m6.txt
- resultados/limites-m6.txt
- resultados/mediciones-m6.txt
- resultados/experimento-m2-luca.txt

Los tiempos dependen del equipo, la JVM y la actividad
del sistema operativo. Las mediciones son orientativas.

Quedan pendientes los experimentos completos de M1 y M5,
la integración desde el menú principal y la revisión
del procesamiento de las mediciones con su módulo consumidor.