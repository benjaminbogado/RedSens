# Bitácora de IA de M5

Responsable: FEDE / Federico Villagra. Fecha: 22 de noviembre de 2026.

Codex asistió en contrastar el enunciado, main y el Docs, implementar
Josephus con LinkedList/Iterator y ABB con k-ésimo, conectar el menú,
preparar pruebas y ejecutar el experimento. El código se compiló con
OpenJDK 23 y --release 17. Los resultados medidos están en resultados/.

Decisión revisada durante el desarrollo: el diseño local anterior proponía
un AVL de técnicos. Se eligió un ABB construido equilibrado con solo bajas:
esa restricción permite demostrar altura acotada por la inicial y evita
añadir rotaciones innecesarias. No se generaliza esta garantía a inserciones.

Error que se evita con un caso concreto: interpretar M como contar al
primero haría que M=1 eliminara al técnico 1. Se fijó la convención mediante
el caso oficial 2,4,1,5 y suplente 3, y se contrastaron ambas soluciones con
un oráculo independiente. No se afirma haber encontrado ese error en el
código publicado ni que lo haya detectado una persona.

Queda para la bitácora final que el integrante registre su revisión,
las modificaciones manuales reales y un error o sugerencia incorrecta
efectivamente detectada por el equipo. Este texto no inventa aportes humanos.
