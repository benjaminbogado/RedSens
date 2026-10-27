# Bitácora de uso de inteligencia artificial — M6

Proyecto: RedSens.
Materia: Taller de Algoritmos y Estructuras de Datos II.
Responsable de este registro: Luca Agustín Pepino.
Grupo: 3.
Semilla asignada: 3026.
Herramienta utilizada: ChatGPT.

## 1. Objetivo y alcance

Se utilizó inteligencia artificial como asistencia técnica para
desarrollar el módulo de simulación y pruebas de RedSens.

Las consultas se centraron en la generación reproducible de datos,
la obtención de códigos únicos, el diseño de comprobaciones y
la metodología de medición de rendimiento.

La herramienta propuso código Java y documentación. El código
se incorporó al proyecto y se ejecutó localmente para comprobar
su comportamiento. Las salidas obtenidas permitieron revisar
las propuestas y corregir los problemas encontrados.

Este documento registra la asistencia correspondiente a M6.
Debe integrarse con los registros de los demás integrantes
para elaborar la bitácora general del grupo.

## 2. Generación reproducible de estaciones

Se solicitó una propuesta para generar estaciones válidas
utilizando java.util.Random y la semilla 3026.

Una decisión central fue generar códigos únicos sin depender
de intentos aleatorios repetidos. Para ello, se utilizó un
arreglo con los números de 0 a 9999 y una mezcla parcial
de Fisher–Yates.

En cada paso se selecciona un número entre las posiciones
todavía disponibles y se intercambia con la posición actual.
Así, cada número se utiliza como máximo una vez.

Los códigos se construyen con el formato CBA-NNNN.
El universo disponible permite generar hasta 10.000 estaciones.

La asistencia también abarcó la generación de los atributos
del modelo: localidad, tipo de estación, batería y días
sin visita.

Se mantuvo una secuencia fija de llamadas al generador
pseudoaleatorio. Con el mismo código, la misma cantidad
y la misma semilla se reproduce la secuencia de estaciones.

## 3. Comprobaciones del generador

Se prepararon comprobaciones para verificar:

- Generación de 10.000 códigos sin duplicados.
- Igualdad de las estaciones generadas en dos ejecuciones
  con la semilla 3026.
- Diferencias entre las secuencias obtenidas con las semillas
  3026 y 3027.
- Aceptación de las cantidades 0, 1 y 10.000.
- Rechazo de las cantidades -1 y 10.001.

La comparación de reproducibilidad considera los atributos
de cada estación y su posición en la secuencia. No se basa
solamente en comparar las referencias de los objetos.

Las comprobaciones ejecutadas dieron resultados correctos.
Sus salidas se conservaron en resultados/verificacion-m6.txt
y resultados/limites-m6.txt.

La comparación entre dos semillas es una comprobación concreta
del comportamiento observado; no demuestra que todas las
semillas posibles produzcan secuencias diferentes.

## 4. Distribución de los niveles de batería

Se solicitó asistencia para analizar las frecuencias de batería
generadas mediante nextInt(101), cuyo rango comprende los
enteros de 0 a 100 inclusive.

Para 10.000 estaciones, la frecuencia esperada de cada valor
es 10.000 / 101, aproximadamente 99,01.

Los intervalos 0–9, 10–19 y los restantes hasta 90–99 contienen
diez valores cada uno. Por eso, su frecuencia esperada es
aproximadamente 990,10. El valor 100 se presenta por separado,
con frecuencia esperada de 99,01.

En la ejecución se observaron entre 955 y 1.028 estaciones
por intervalo de diez valores, y 92 estaciones con batería 100.

Las frecuencias observadas resultaron próximas a las esperadas.
El análisis es descriptivo: no se realizó una prueba estadística
formal ni se exigió igualdad exacta entre frecuencias.

## 5. Generación y verificación de mediciones

La IA propuso código para generar un archivo de 5.000 mediciones
asociadas a estaciones del modelo.

Se utilizó una fecha fija, una semilla explícita y un formato
estable para los campos de código, fecha, hora, temperatura,
humedad, precipitación y batería.

Se empleó Locale.ROOT para mantener el punto decimal
independientemente de la configuración regional del equipo.
La escritura del archivo se realizó con codificación UTF-8.

Se ejecutaron comprobaciones de:

- Cantidad total de líneas.
- Formato de todas las líneas.
- Igualdad byte a byte entre dos archivos generados con
  los mismos parámetros.

Las verificaciones dieron resultados correctos.
El archivo de datos se encuentra en datos/mediciones_grupo3.txt
y la evidencia en resultados/mediciones-m6.txt.

La comprobación de formato no reemplaza una prueba de integración
con el módulo que procesará las mediciones.

## 6. Medición de rendimiento

Se recibió asistencia para preparar mediciones con
System.nanoTime() y separar la generación de datos de las
operaciones cronometradas.

En el experimento inicial de AVL se incorporaron rondas
de calentamiento, repeticiones medidas y uso de la mediana
de los tiempos por lote. También se conservó un resultado
de control derivado de las búsquedas.

Posteriormente se ejecutó el experimento comparativo ABB/AVL
desarrollado por otro integrante del grupo e incorporado
desde la rama main.

Ese experimento comprende 1.000, 5.000 y 10.000 estaciones,
con inserción ascendente y aleatoria. Los seis escenarios
superaron las verificaciones incluidas en el programa.

Se comprobó el comportamiento esperado en el caso ascendente:
con 10.000 estaciones, el ABB alcanzó altura 9999 y el AVL,
altura 13, contando la altura en aristas.

Los resultados de esta ejecución se conservaron en
resultados/experimento-m2-luca.txt.

Las metodologías de ambos experimentos son distintas:
el inicial de AVL informa medianas y el comparativo del equipo
informa promedios. Sus tiempos deben interpretarse según
la metodología y el entorno de cada ejecución.

## 7. Error de la asistencia y corrección

Durante la conservación de resultados, la IA propuso un comando
con el parámetro -Dfile.encoding=UTF-8 sin comillas.

En PowerShell, el argumento se interpretó incorrectamente.
Java informó que no encontraba la clase .encoding=UTF-8
y la salida esperada no se generó.

Se corrigió el comando colocando el parámetro entre comillas.
Sin embargo, esto no resolvió completamente la representación
de los acentos en la salida capturada.

Se configuró la codificación de salida de la consola,
se agregó el parámetro "-Dstdout.encoding=UTF-8" y se guardó
el texto mediante Out-File -Encoding utf8.

La corrección se verificó leyendo nuevamente el archivo:
los textos “Códigos únicos” y “Distribución de baterías”
se mostraron correctamente y las comprobaciones mantuvieron
sus resultados OK.

Este incidente mostró que una recomendación de la IA puede
ser incompleta para un entorno concreto y debe validarse
mediante ejecución y revisión del resultado.

## 8. Evaluación y estado del trabajo

La asistencia permitió obtener propuestas de implementación
y comprobaciones para aspectos específicos de M6.
La aceptación de esas propuestas se apoyó en compilaciones,
ejecuciones y revisión de las evidencias obtenidas.

Las pruebas realizadas verifican los casos documentados;
no constituyen una demostración de ausencia de errores
en todo el sistema.

Al momento de este registro quedan pendientes los experimentos
completos de hash y Josephus, las pruebas de integración
correspondientes y la incorporación de sus resultados
al informe final.

No se presentan resultados de esos experimentos mientras
no hayan sido ejecutados.