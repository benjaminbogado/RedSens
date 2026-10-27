# M6 — Simulación y pruebas

Responsable: Luca Agustín Pepino.
Grupo: 3.
Semilla: 3026.
Requiere JDK 17 o superior.

## Objetivo

Generar datos reproducibles para probar los módulos de RedSens,
verificar la generación y medir el rendimiento de las estructuras.

## Archivos y ejecución

En VS Code, abrir la carpeta RedSens y ejecutar con Run:

- ComprobadorM6.java:
  verifica 10.000 códigos únicos, reproducibilidad y frecuencias de batería.

- ComprobadorLimites.java:
  prueba cantidades 0, 1 y 10.000, y rechazo de -1 y 10.001.

- ComprobadorMediciones.java:
  genera 5.000 líneas y comprueba formato y reproducibilidad.

- ExperimentoAVL.java:
  mide búsquedas exitosas con 1.000, 5.000 y 10.000 estaciones,
  con inserción ascendente y aleatoria.

GeneradorDatos, GeneradorMediciones y MedidorRendimiento son
clases reutilizables. No se ejecutan directamente.

## Generación de estaciones

Ejemplo de uso desde otros módulos:

    List<Estacion> estaciones =
        GeneradorDatos.generarEstaciones(10000, 3026L);

Importaciones necesarias:

    import java.util.List;
    import redsens.modelo.Estacion;
    import redsens.simulacion.GeneradorDatos;

Devuelve una lista con códigos de formato CBA-NNNN.
Los números posibles van de 0000 a 9999.
Admite entre 0 y 10.000 estaciones.

Se utiliza Fisher-Yates parcial para seleccionar números sin repetición.
La unicidad está garantizada dentro de cada lista generada,
no entre llamadas independientes.

La misma cantidad y semilla producen los mismos datos en el mismo orden.
Para comparar estructuras se deben reutilizar las mismas estaciones.

Las localidades corresponden a las 12 del grafo del enunciado:

- Córdoba.
- Villa Carlos Paz.
- Alta Gracia.
- Jesús María.
- Río Tercero.
- Villa María.
- San Francisco.
- Cosquín.
- La Falda.
- Río Cuarto.
- Deán Funes.
- Villa Dolores.

Batería: de 0 a 100 inclusive.
Días sin visita: de 0 a 365 inclusive, rango elegido para la simulación.
Tipos: los valores definidos en TipoEstacion.

El costo de generación es O(U + N), donde U = 10.000 códigos
posibles y N es la cantidad de estaciones solicitadas.
El espacio utilizado es O(U + N).

## Reproducibilidad y uniformidad

Se comparan todos los campos de dos listas generadas con semilla 3026.
También se verifica que las secuencias probadas con semillas
3026 y 3027 sean diferentes.

Para examinar la uniformidad se cuentan las frecuencias de batería:

- Cada valor tiene frecuencia esperada N / 101.
- Cada intervalo de diez valores tiene frecuencia esperada N * 10 / 101.
- El valor 100 se informa por separado.

Las frecuencias observadas no tienen que ser exactamente iguales
a las esperadas.

Esta comparación es descriptiva y no constituye una prueba
estadística formal.

## Generación de mediciones

Archivo de salida:

    datos/mediciones_grupo3.txt

El comprobador genera 5.000 registros de 1.000 estaciones
para la fecha fija 2026-10-13.

Formato de cada registro:

    CBA-0347;2026-10-13;08:00;T=18.4;H=62;P=0.0;BAT=18

Decisiones de simulación:

- Temperatura: de -5 a 45 grados, con un decimal.
- Humedad: de 0 a 100.
- Precipitación: de 0 a 50, con un decimal.
- Batería: se conserva el valor de la estación.
- Registros distribuidos entre estaciones y a lo largo del mismo día.
- Codificación UTF-8, punto decimal y salto de línea LF.

Son datos sintéticos para pruebas, no un modelo meteorológico realista.

Se comprueba que el archivo tenga 5.000 líneas, que todas cumplan
el formato y que dos generaciones con los mismos parámetros
produzcan archivos idénticos.

El archivo de salida se reemplaza al ejecutar nuevamente
el comprobador.

## Medición de rendimiento

MedidorRendimiento utiliza System.nanoTime() para medir
el tiempo transcurrido de una operación.

Devuelve el resultado de la tarea y su duración en nanosegundos.
También permite calcular la mediana de varias duraciones.

La preparación de los datos debe realizarse fuera del intervalo
que se desea medir.

## Experimento del AVL

Archivo de salida:

    datos/rendimiento_avl.csv

Se prueban 1.000, 5.000 y 10.000 estaciones con dos órdenes
de inserción: ascendente y aleatorio.

Para cada tamaño se utilizan las mismas estaciones y las mismas
consultas en ambos árboles.

La generación, la inserción y la validación quedan fuera del
tiempo medido. Se mide el lote de búsquedas exitosas.

Procedimiento:

1. Construir el árbol.
2. Verificar que todas las estaciones puedan encontrarse.
3. Ejecutar cinco lotes de calentamiento.
4. Ejecutar siete lotes medidos.
5. Calcular la mediana del tiempo de los lotes.
6. Dividir esa mediana por la cantidad de búsquedas.

Las mediciones incluyen el recorrido del lote y su suma de control.
Son orientativas y pueden variar según la JVM, el equipo y
los procesos en ejecución.

Alturas observadas:

| Estaciones | Ascendente | Aleatorio |
|------------|------------|-----------|
| 1.000      | 9          | 11        |
| 5.000      | 12         | 14        |
| 10.000     | 13         | 15        |

Estas alturas son compatibles con el crecimiento logarítmico
de un AVL balanceado.

Los tiempos de la última ejecución se guardan en el CSV.
Este experimento cubre solamente la parte AVL de la comparación M2.

## Estado y pendientes de M6

Implementado y probado:

- Generación de estaciones únicas.
- Reproducibilidad con semilla.
- Comparación descriptiva de uniformidad.
- Casos límite de cantidad de estaciones.
- Generación y verificación de 5.000 mediciones.
- Herramienta de medición de tiempos.
- Experimento del AVL.

Pendiente a cargo de M6, al disponer de las clases del equipo:

- Experimento M1: comparar hash lineal y cuadrática con
  10.000 estaciones, colisiones, sondeos de búsquedas
  exitosas y fallidas, y cantidad de rehashings.
- Experimento M2 completo: comparar ABB y AVL con datos
  ascendentes y aleatorios.
- Experimento M5: comparar ambas versiones de Josephus
  para N = 1.000, 10.000 y 100.000, con al menos dos valores de M.
- Reunir tablas, metodología y conclusiones en el informe
  técnico del grupo.
- Coordinar la ejecución de las pruebas y experimentos
  desde el menú principal.

Las estructuras de M1, M2 y M5 las implementan sus responsables.
M6 utiliza esas implementaciones para ejecutar los experimentos.