import redsens.registro.TablaHash;
import redsens.registro.EstrategiaExploracion;
import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

TablaHash tabla = new TablaHash(17, EstrategiaExploracion.LINEAL);
Estacion e0 = new Estacion("CBA-0000", 0, "Estación cero", "San Francisco", TipoEstacion.METEOROLOGICA, 80, 0);
Estacion e13 = new Estacion("CBA-0013", 13, "Estación trece", "San Francisco", TipoEstacion.METEOROLOGICA, 15, 4);
Estacion e26 = new Estacion("CBA-0026", 26, "Estación veintiséis", "San Francisco", TipoEstacion.HIDROMETRICA, 60, 2);

tabla.insertar(e0);
tabla.insertar(e13);
tabla.insertar(e26);
System.out.println("Inserción de 0026: " + tabla.getSondeosUltimaOperacion() + " sondeos, " + tabla.getColisionesUltimaOperacion() + " colisiones");
System.out.println("Acumulados: " + tabla.getSondeosTotales() + " sondeos, " + tabla.getColisionesTotales() + " colisiones");
System.out.println("Tamaño: " + tabla.tamanio() + "; capacidad: " + tabla.capacidad());

tabla.eliminar("CBA-0000");
System.out.println("Buscar 0026 tras borrar 0000: " + tabla.buscar("CBA-0026").getNombre());
System.out.println("Búsqueda de 0026: " + tabla.getSondeosUltimaOperacion() + " sondeos, " + tabla.getColisionesUltimaOperacion() + " colisiones");
System.out.println("Buscar 0000 borrada: " + tabla.buscar("CBA-0000"));

tabla.insertar(e0);
System.out.println("Reinsertar 0000: " + tabla.getSondeosUltimaOperacion() + " sondeos, " + tabla.getColisionesUltimaOperacion() + " colisiones");
System.out.println("Factor de carga: " + tabla.factorCarga());
tabla.reiniciarContadores();
System.out.println("Sondeos tras reiniciar: " + tabla.getSondeosTotales());

// La sesión sigue abierta para probar otras operaciones. Salir con /exit.
