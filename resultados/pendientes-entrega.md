# Estado frente al enunciado

Revisión: 22 de noviembre de 2026. Base main a0a702f, más la entrega de M5
de codex/modulo-5-josephus. No implica que ese PR ya esté integrado.

## Código e integración

| Requisito | Evidencia y pendiente |
|---|---|
| Modelo, M1, M2 | En main, con pruebas y experimentos. Hash y AVL se cargan desde el menú; búsquedas, rangos, críticas y bajas están conectados. |
| M3 completo | Existe en rama feat/grafo-red, PR #11 abierto; falta revisar e integrar. Incluye matriz/lista, archivo de rutas, BFS/DFS, Dijkstra/Floyd, cortes y plan. Sus pruebas no se ejecutaron en esta auditoría de M5. |
| M4 completo | Existe en rama feat/huffman, PR #12 abierto; falta revisar e integrar. Incluye archivos comprimidos/restaurados y pruebas. Sus pruebas no se ejecutaron aquí. |
| M5 | Esta rama agrega las dos soluciones, control obligatorio, calendario y experimento de seis escenarios; falta revisión y merge de su PR. |
| M6 | Generación con semilla, frecuencias, mediciones y mediciones de tiempo existentes. Falta una opción de menú que ejecute/reúna la batería global M1+M2+M5; actualmente los experimentos M1/M2 son clases independientes y M5 tiene su opción propia. |
| Menú final | En main las opciones 3 y 4 son mensajes de pendiente. Después de integrar PR #11 y #12, resolver los cambios concurrentes de Main y comprobar el flujo completo. |

Se deben probar desde un mismo checkout integrado: cargar N estaciones en
hash y AVL, buscar/listar, críticas -> visitas con base San Francisco,
mediciones -> Huffman -> recuperación idéntica y calendario M5. Pasar pruebas
de módulos aislados no demuestra por sí solo esa integración.

## Documentación y entrega

1. Revisar e integrar PR #13 (docs/informe-readme): tiene informe conjunto
   M1/M2 y documentación de módulos, pero aún marca M5 pendiente. Incorporar
   los resultados reales de informe-m5-josephus.md y sus comandos.
2. Revisar e integrar la bitácora M2 del PR #10. Unificar las bitácoras en
   el documento final de 1 a 2 páginas, con usos reales, error corregido y
   aportes humanos verificables; no atribuir revisiones humanas no realizadas.
3. Exportar y revisar el informe técnico final PDF de máximo 15 páginas:
   seis módulos, decisiones, complejidades, tablas de M1/M2/M5, casos de
   verificación, frecuencias y conclusión sobre la compresión de mediciones.
   Los informes parciales actuales y el borrador Markdown no sustituyen ese PDF.
4. Actualizar README final con los cinco integrantes, sus roles y todos los
   comandos. Se corrigieron aquí dos descripciones viejas (ausencia de menú
   y cuadrática pendiente), pero la integración completa queda por reflejar.
5. Confirmar archivo oficial de rutas y muestras comprimidas/restauradas de
   M4 en la versión final integrada; ya hay archivos en las ramas pendientes.
6. Armar TPI_Grupo_03.zip con fuentes, README, PDF técnico, bitácora y datos,
   y verificar compilación/ejecución desde una extracción limpia.
7. Ensayar demostración en vivo y preguntas para cualquier integrante.

No hace falta reimplementar M3 o M4: hay trabajo entregado en los PR
abiertos. Tampoco se debe declarar el proyecto al 100% hasta integrar,
validar los flujos completos y reunir la entrega documental.
