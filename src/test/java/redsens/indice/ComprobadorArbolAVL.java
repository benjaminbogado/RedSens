package redsens.indice;

import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

/** Comprobador manual de las cuatro rotaciones AVL, sin dependencias externas. */
public final class ComprobadorArbolAVL {
    private ComprobadorArbolAVL() {
    }

    public static void main(String[] args) {
        comprobarRotacion("LL", new int[]{30, 20, 10});
        comprobarRotacion("RR", new int[]{10, 20, 30});
        comprobarRotacion("LR", new int[]{30, 10, 20});
        comprobarRotacion("RL", new int[]{10, 30, 20});
        comprobarRecorridos();
        comprobarRecorridosArbolVacio();
        comprobarMetricasDelArbol();
        comprobarListarPorRango();
        comprobarListarCriticas();
        comprobarBusqueda();
        comprobarRechazoDeDuplicados();
        comprobarRechazoDeNulos();
        comprobarEliminacionDeHoja();
        comprobarEliminacionDeNodoConUnHijo();
        comprobarEliminacionDeNodoConDosHijos();
        comprobarEliminacionDeRaiz();
        comprobarEliminacionInexistente();
        comprobarReequilibradosPorEliminacion();
        comprobarBuscarEnArbolVacio();
        comprobarEliminarRaizUnica();
        comprobarSucesorConHijoDerecho();
        System.out.println("Todas las comprobaciones del AVL pasaron.");
    }

    private static void comprobarRecorridos() {
        ArbolAVL arbol = crearArbol(20, 10, 30);
        String estructuraAntes = AccesoInterno.estructura(arbol);

        List<Estacion> inorden = arbol.inorden();
        List<Estacion> preorden = arbol.preorden();
        List<Estacion> postorden = arbol.postorden();
        exigir(numeros(inorden).equals(List.of(10, 20, 30)),
                "Inorden debe ser 10, 20, 30");
        exigir(numeros(preorden).equals(List.of(20, 10, 30)),
                "Preorden debe ser 20, 10, 30");
        exigir(numeros(postorden).equals(List.of(10, 30, 20)),
                "Postorden debe ser 10, 30, 20");
        exigir(inorden != arbol.inorden()
                        && preorden != arbol.preorden()
                        && postorden != arbol.postorden(),
                "Cada llamada debe devolver una lista nueva");
        exigir(AccesoInterno.estructura(arbol).equals(estructuraAntes),
                "Los recorridos no deben modificar la estructura ni las alturas");
        System.out.println("PASÓ inorden, preorden y postorden");
    }

    private static void comprobarRecorridosArbolVacio() {
        ArbolAVL arbol = new ArbolAVL();
        exigir(arbol.inorden() != null && arbol.inorden().isEmpty(),
                "Inorden debe devolver una lista vacía para un árbol vacío");
        exigir(arbol.preorden() != null && arbol.preorden().isEmpty(),
                "Preorden debe devolver una lista vacía para un árbol vacío");
        exigir(arbol.postorden() != null && arbol.postorden().isEmpty(),
                "Postorden debe devolver una lista vacía para un árbol vacío");
        System.out.println("PASÓ recorridos de árbol vacío");
    }

    private static List<Integer> numeros(List<Estacion> estaciones) {
        return estaciones.stream().map(Estacion::getNumero).toList();
    }

    private static void comprobarMetricasDelArbol() {
        ArbolAVL vacio = new ArbolAVL();
        exigirMetricas(vacio, -1, 0, 0, "árbol vacío");
        System.out.println("PASÓ métricas de árbol vacío");

        ArbolAVL unNodo = crearArbol(20);
        exigirMetricas(unNodo, 0, 1, 1, "árbol con un nodo");
        System.out.println("PASÓ métricas de árbol con un nodo");

        ArbolAVL tresNodos = crearArbol(20, 10, 30);
        exigirMetricas(tresNodos, 1, 3, 2, "árbol con tres nodos");
        System.out.println("PASÓ métricas de árbol con tres nodos");

        tresNodos.eliminar(10);
        exigirMetricas(tresNodos, 1, 2, 1, "árbol después de eliminar una estación");
        System.out.println("PASÓ métricas después de eliminar una estación");
    }

    private static void exigirMetricas(ArbolAVL arbol, int altura, int nodos,
                                       int hojas, String caso) {
        exigir(arbol.altura() == altura, caso + ": altura incorrecta");
        exigir(arbol.cantidadNodos() == nodos, caso + ": cantidad de nodos incorrecta");
        exigir(arbol.cantidadHojas() == hojas, caso + ": cantidad de hojas incorrecta");
    }

    private static void comprobarListarPorRango() {
        ArbolAVL arbol = crearArbolVerificado("rango", 40, 20, 60, 10, 30, 50, 70);
        String estructuraAntes = AccesoInterno.estructura(arbol);

        exigir(numeros(arbol.listarPorRango(25, 55)).equals(List.of(30, 40, 50)),
                "El rango 25..55 debe devolver 30, 40, 50 en orden");
        exigir(numeros(arbol.listarPorRango(20, 60)).equals(List.of(20, 30, 40, 50, 60)),
                "El rango 20..60 debe incluir ambos extremos existentes");
        exigir(arbol.listarPorRango(80, 90).isEmpty(),
                "Un rango sin coincidencias debe devolver una lista vacía");
        exigir(AccesoInterno.estructura(arbol).equals(estructuraAntes),
                "Las consultas por rango no deben alterar el árbol");
        System.out.println("PASÓ rangos con coincidencias, sin coincidencias y sin mutación");

        try {
            arbol.listarPorRango(55, 25);
            throw new AssertionError("Un rango invertido debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperado) {
            exigir(AccesoInterno.estructura(arbol).equals(estructuraAntes),
                    "Un rango inválido no debe alterar el árbol");
            System.out.println("PASÓ rechazo de rango invertido");
        }
    }

    private static void comprobarListarCriticas() {
        ArbolAVL arbol = new ArbolAVL();
        arbol.insertar(crearEstacion(40, 19));
        arbol.insertar(crearEstacion(20, 20));
        arbol.insertar(crearEstacion(60, 0));
        arbol.insertar(crearEstacion(10, 18));
        arbol.insertar(crearEstacion(30, 99));
        arbol.insertar(crearEstacion(50, 19));
        arbol.insertar(crearEstacion(70, 20));
        String estructuraAntes = AccesoInterno.estructura(arbol);

        List<Estacion> criticas = arbol.listarCriticas();
        exigir(numeros(criticas).equals(List.of(10, 40, 50, 60)),
                "Deben listarse solo las estaciones críticas en orden ascendente");
        exigir(criticas.stream().allMatch(Estacion::esCritica),
                "Cada estación devuelta debe ser crítica");
        exigir(AccesoInterno.estructura(arbol).equals(estructuraAntes),
                "Listar críticas no debe alterar el árbol");
        System.out.println("PASÓ listado de estaciones críticas ordenado y sin mutación");

        ArbolAVL sinCriticas = new ArbolAVL();
        sinCriticas.insertar(crearEstacion(20, 20));
        sinCriticas.insertar(crearEstacion(10, 100));
        exigir(sinCriticas.listarCriticas().isEmpty(),
                "Sin estaciones críticas debe devolver una lista vacía");
        System.out.println("PASÓ listado vacío cuando no hay estaciones críticas");
    }

    private static void comprobarRotacion(String caso, int[] numeros) {
        ArbolAVL arbol = new ArbolAVL();
        for (int numero : numeros) {
            arbol.insertar(crearEstacion(numero));
        }

        Object raiz = AccesoInterno.raiz(arbol);
        exigir(AccesoInterno.numero(raiz) == 20, caso + ": la raíz debe ser 20");

        Object izquierdo = AccesoInterno.hijoIzquierdo(raiz);
        Object derecho = AccesoInterno.hijoDerecho(raiz);
        exigir(AccesoInterno.numero(izquierdo) == 10, caso + ": el hijo izquierdo debe ser 10");
        exigir(AccesoInterno.numero(derecho) == 30, caso + ": el hijo derecho debe ser 30");
        exigir(AccesoInterno.altura(raiz) == 1, caso + ": la altura de la raíz debe ser 1");
        exigir(AccesoInterno.altura(izquierdo) == 0, caso + ": la altura del hijo izquierdo debe ser 0");
        exigir(AccesoInterno.altura(derecho) == 0, caso + ": la altura del hijo derecho debe ser 0");

        AccesoInterno.comprobarAlturasYEquilibrio(raiz, caso);
        System.out.println("PASÓ " + caso);
    }

    private static void comprobarBusqueda() {
        ArbolAVL arbol = new ArbolAVL();
        Estacion existente = crearEstacion(50);
        int[] numeros = {40, 20, 60, 10, 30, 50, 70};
        for (int numero : numeros) {
            arbol.insertar(numero == 50 ? existente : crearEstacion(numero));
        }

        String antesDeBuscarExistente = AccesoInterno.estructura(arbol);
        exigir(arbol.buscar(50) == existente,
                "buscar(50) debe devolver la misma estación almacenada");
        exigir(AccesoInterno.estructura(arbol).equals(antesDeBuscarExistente),
                "buscar(50) no debe cambiar la estructura ni las alturas");
        System.out.println("PASÓ búsqueda existente y estructura/alturas intactas");

        String antesDeBuscarInexistente = AccesoInterno.estructura(arbol);
        exigir(arbol.buscar(99) == null, "buscar(99) debe devolver null");
        exigir(AccesoInterno.estructura(arbol).equals(antesDeBuscarInexistente),
                "buscar(99) no debe cambiar la estructura ni las alturas");
        System.out.println("PASÓ búsqueda inexistente y estructura/alturas intactas");
    }

    private static void comprobarRechazoDeDuplicados() {
        ArbolAVL arbol = new ArbolAVL();
        arbol.insertar(crearEstacion(25));
        try {
            arbol.insertar(crearEstacion(25));
            throw new AssertionError("Insertar un número duplicado debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperado) {
            System.out.println("PASÓ rechazo de número duplicado");
        }
    }

    private static void comprobarRechazoDeNulos() {
        ArbolAVL arbol = new ArbolAVL();
        try {
            arbol.insertar(null);
            throw new AssertionError("Insertar null debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperado) {
            System.out.println("PASÓ rechazo de estación null");
        }
    }

    private static void comprobarEliminacionDeHoja() {
        ArbolAVL arbol = crearArbol(40, 20, 60, 10, 30, 50, 70);
        Object nodo = AccesoInterno.nodo(arbol, 10);
        exigir(AccesoInterno.hijoIzquierdo(nodo) == null
                        && AccesoInterno.hijoDerecho(nodo) == null,
                "La prueba de hoja debe seleccionar un nodo sin hijos");

        arbol.eliminar(10);
        exigir(arbol.buscar(10) == null, "La hoja eliminada no debe seguir en el árbol");
        exigir(arbol.buscar(20) != null && arbol.buscar(40) != null,
                "Los nodos no eliminados deben permanecer en el árbol");
        comprobarEstadoAVL(arbol, "eliminación de hoja");
        System.out.println("PASÓ eliminación de hoja");
    }

    private static void comprobarEliminacionDeNodoConUnHijo() {
        ArbolAVL arbol = crearArbol(40, 20, 60, 10);
        Object nodo = AccesoInterno.nodo(arbol, 20);
        exigir((AccesoInterno.hijoIzquierdo(nodo) == null)
                        != (AccesoInterno.hijoDerecho(nodo) == null),
                "La prueba debe seleccionar un nodo con un único hijo");

        arbol.eliminar(20);
        exigir(arbol.buscar(20) == null, "El nodo eliminado no debe seguir en el árbol");
        exigir(arbol.buscar(10) != null && arbol.buscar(40) != null,
                "El hijo del nodo eliminado y los demás nodos deben permanecer");
        comprobarEstadoAVL(arbol, "eliminación de nodo con un hijo");
        System.out.println("PASÓ eliminación de nodo con un hijo");
    }

    private static void comprobarEliminacionDeNodoConDosHijos() {
        ArbolAVL arbol = crearArbol(40, 20, 60, 10, 30, 50, 70);
        Object nodo = AccesoInterno.nodo(arbol, 20);
        exigir(AccesoInterno.hijoIzquierdo(nodo) != null
                        && AccesoInterno.hijoDerecho(nodo) != null,
                "La prueba debe seleccionar un nodo con dos hijos");

        arbol.eliminar(20);
        exigir(arbol.buscar(20) == null, "El nodo eliminado no debe seguir en el árbol");
        exigir(arbol.buscar(10) != null && arbol.buscar(30) != null
                        && arbol.buscar(40) != null,
                "Los otros nodos deben permanecer tras usar el sucesor");
        comprobarEstadoAVL(arbol, "eliminación de nodo con dos hijos");
        System.out.println("PASÓ eliminación de nodo con dos hijos");
    }

    private static void comprobarEliminacionDeRaiz() {
        ArbolAVL arbol = crearArbol(40, 20, 60, 10, 30, 50, 70);
        exigir(AccesoInterno.numero(AccesoInterno.raiz(arbol)) == 40,
                "La prueba debe eliminar la raíz inicial");

        arbol.eliminar(40);
        exigir(arbol.buscar(40) == null, "La raíz eliminada no debe seguir en el árbol");
        for (int numero : new int[]{10, 20, 30, 50, 60, 70}) {
            exigir(arbol.buscar(numero) != null, "Debe permanecer el nodo " + numero);
        }
        comprobarEstadoAVL(arbol, "eliminación de raíz");
        System.out.println("PASÓ eliminación de raíz");
    }

    private static void comprobarEliminacionInexistente() {
        ArbolAVL arbol = crearArbol(40, 20, 60);
        String antes = AccesoInterno.estructura(arbol);
        try {
            arbol.eliminar(99);
            throw new AssertionError("Eliminar un número inexistente debía lanzar IllegalArgumentException");
        } catch (IllegalArgumentException esperado) {
            exigir(AccesoInterno.estructura(arbol).equals(antes),
                    "Un intento fallido no debe cambiar el árbol");
            comprobarEstadoAVL(arbol, "eliminación de número inexistente");
            System.out.println("PASÓ rechazo de número inexistente");
        }
    }

    private static void comprobarReequilibradosPorEliminacion() {
        comprobarEliminacionConRotacion("simple derecha", 20, 50,
                new int[]{40, 20, 60, 10, 30, 50, 5, 15}, -1, 20, -1);
        comprobarEliminacionConRotacion("simple izquierda", 60, 30,
                new int[]{40, 60, 20, 50, 70, 30, 65, 75}, 1, 60, 1);
        comprobarEliminacionConRotacion("doble izquierda-derecha", 30, 50,
                new int[]{40, 20, 60, 10, 30, 50, 25, 35}, -1, 20, 1);
        comprobarEliminacionConRotacion("doble derecha-izquierda", 50, 10,
                new int[]{40, 60, 20, 50, 70, 10, 45, 55}, 1, 60, -1);
    }

    private static void comprobarEliminacionConRotacion(String caso, int raizEsperada,
                                                         int numeroAEliminar, int[] numeros,
                                                         int factorRaizAntes, int numeroHijoDesequilibrado,
                                                         int factorHijoAntes) {
        ArbolAVL arbol = crearArbolVerificado("inserción para rotación " + caso, numeros);
        Object raizAntes = AccesoInterno.raiz(arbol);
        exigir(AccesoInterno.numero(raizAntes) == 40,
                caso + ": la configuración previa debe tener raíz 40");
        exigir(AccesoInterno.factorEquilibrio(raizAntes) == factorRaizAntes,
                caso + ": factor previo de la raíz inesperado");
        exigir(AccesoInterno.factorEquilibrio(
                        AccesoInterno.nodo(arbol, numeroHijoDesequilibrado)) == factorHijoAntes,
                caso + ": factor previo del hijo desequilibrado inesperado");

        arbol.eliminar(numeroAEliminar);
        comprobarEstadoAVL(arbol, "eliminación con rotación " + caso);

        exigir(arbol.buscar(numeroAEliminar) == null,
                caso + ": el número eliminado no debe seguir en el árbol");
        exigir(AccesoInterno.numero(AccesoInterno.raiz(arbol)) == raizEsperada,
                caso + ": la rotación debe dejar la raíz esperada " + raizEsperada);
        exigir(arbol.cantidadNodos() == numeros.length - 1,
                caso + ": debe quedar un nodo menos");
        System.out.println("PASÓ reequilibrado por eliminación con rotación " + caso);
    }

    private static void comprobarBuscarEnArbolVacio() {
        ArbolAVL arbol = new ArbolAVL();
        exigir(arbol.buscar(20) == null, "Buscar en un árbol vacío debe devolver null");
        comprobarEstadoAVL(arbol, "búsqueda en árbol vacío");
        System.out.println("PASÓ búsqueda en árbol vacío");
    }

    private static void comprobarEliminarRaizUnica() {
        ArbolAVL arbol = crearArbolVerificado("inserción de raíz única", 42);
        arbol.eliminar(42);
        comprobarEstadoAVL(arbol, "eliminación de raíz única");

        exigir(arbol.buscar(42) == null, "La raíz eliminada no debe seguir en el árbol");
        exigir(AccesoInterno.estructura(arbol).equals("#"),
                "Eliminar la única raíz debe dejar el árbol vacío");
        exigir(arbol.altura() == -1 && arbol.cantidadNodos() == 0 && arbol.cantidadHojas() == 0,
                "Las métricas deben indicar que el árbol quedó vacío");
        System.out.println("PASÓ eliminación de la única raíz");
    }

    private static void comprobarSucesorConHijoDerecho() {
        ArbolAVL arbol = crearArbolVerificado("inserción para sucesor con hijo derecho",
                40, 20, 60, 10, 30, 50, 70, 35);
        Object nodoAEliminar = AccesoInterno.nodo(arbol, 20);
        Object sucesor = AccesoInterno.hijoDerecho(nodoAEliminar);
        exigir(AccesoInterno.numero(sucesor) == 30,
                "El sucesor en orden del nodo 20 debe ser 30");
        exigir(AccesoInterno.hijoIzquierdo(sucesor) == null
                        && AccesoInterno.numero(AccesoInterno.hijoDerecho(sucesor)) == 35,
                "El sucesor debe tener un hijo derecho 35");

        arbol.eliminar(20);
        comprobarEstadoAVL(arbol, "eliminación con sucesor que tiene hijo derecho");
        exigir(arbol.buscar(20) == null && arbol.buscar(30) != null && arbol.buscar(35) != null,
                "El sucesor y su hijo derecho deben permanecer tras la eliminación");
        exigir(numeros(arbol.inorden()).equals(List.of(10, 30, 35, 40, 50, 60, 70)),
                "El orden del ABB debe conservarse al mover el sucesor con su hijo derecho");
        System.out.println("PASÓ eliminación con sucesor que tiene hijo derecho");
    }

    private static ArbolAVL crearArbol(int... numeros) {
        ArbolAVL arbol = new ArbolAVL();
        for (int numero : numeros) {
            arbol.insertar(crearEstacion(numero));
        }
        return arbol;
    }

    private static ArbolAVL crearArbolVerificado(String caso, int... numeros) {
        ArbolAVL arbol = new ArbolAVL();
        for (int numero : numeros) {
            arbol.insertar(crearEstacion(numero));
            comprobarEstadoAVL(arbol, caso + " tras insertar " + numero);
        }
        return arbol;
    }

    private static void comprobarEstadoAVL(ArbolAVL arbol, String caso) {
        AccesoInterno.comprobarAlturasYEquilibrio(AccesoInterno.raiz(arbol), caso);
    }

    private static Estacion crearEstacion(int numero) {
        return crearEstacion(numero, 80);
    }

    private static Estacion crearEstacion(int numero, int bateria) {
        String codigo = String.format(Locale.ROOT, "CBA-%04d", numero);
        return new Estacion(codigo, numero, "Estación " + numero, "Córdoba",
                TipoEstacion.METEOROLOGICA, bateria, 0);
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    /** Mantiene todo acceso reflectivo a la representación privada dentro del comprobador. */
    private static final class AccesoInterno {
        private static Object raiz(ArbolAVL arbol) {
            return leer(campo(ArbolAVL.class, "raiz"), arbol);
        }

        private static Object hijoIzquierdo(Object nodo) {
            return leer(campo(nodo.getClass(), "izquierdo"), nodo);
        }

        private static Object hijoDerecho(Object nodo) {
            return leer(campo(nodo.getClass(), "derecho"), nodo);
        }

        private static int numero(Object nodo) {
            Estacion estacion = (Estacion) leer(campo(nodo.getClass(), "estacion"), nodo);
            return estacion.getNumero();
        }

        private static int altura(Object nodo) {
            if (nodo == null) {
                return -1;
            }
            return (int) leer(campo(nodo.getClass(), "altura"), nodo);
        }

        private static int factorEquilibrio(Object nodo) {
            return altura(hijoDerecho(nodo)) - altura(hijoIzquierdo(nodo));
        }

        private static Object nodo(ArbolAVL arbol, int numero) {
            Object actual = raiz(arbol);
            while (actual != null) {
                int numeroActual = numero(actual);
                if (numero < numeroActual) {
                    actual = hijoIzquierdo(actual);
                } else if (numero > numeroActual) {
                    actual = hijoDerecho(actual);
                } else {
                    return actual;
                }
            }
            throw new AssertionError("No se encontró el nodo de prueba " + numero);
        }

        private static String estructura(ArbolAVL arbol) {
            return estructura(raiz(arbol));
        }

        private static String estructura(Object nodo) {
            if (nodo == null) {
                return "#";
            }
            return "(" + numero(nodo) + ":" + altura(nodo)
                    + estructura(hijoIzquierdo(nodo))
                    + estructura(hijoDerecho(nodo)) + ")";
        }

        private static int comprobarAlturasYEquilibrio(Object nodo, String caso) {
            return comprobarAlturasYEquilibrio(nodo, caso, null, null);
        }

        private static int comprobarAlturasYEquilibrio(Object nodo, String caso,
                                                       Integer minimo, Integer maximo) {
            if (nodo == null) {
                return -1;
            }

            int clave = numero(nodo);
            exigir(minimo == null || clave > minimo,
                    caso + ": el nodo " + clave + " debe ser mayor que el límite mínimo " + minimo);
            exigir(maximo == null || clave < maximo,
                    caso + ": el nodo " + clave + " debe ser menor que el límite máximo " + maximo);

            int alturaIzquierda = comprobarAlturasYEquilibrio(
                    hijoIzquierdo(nodo), caso, minimo, clave);
            int alturaDerecha = comprobarAlturasYEquilibrio(
                    hijoDerecho(nodo), caso, clave, maximo);
            int alturaEsperada = 1 + Math.max(alturaIzquierda, alturaDerecha);
            int alturaAlmacenada = altura(nodo);
            exigir(alturaAlmacenada == alturaEsperada,
                    caso + ": altura almacenada incorrecta para el nodo " + numero(nodo));

            int factor = alturaDerecha - alturaIzquierda;
            exigir(factor >= -1 && factor <= 1,
                    caso + ": factor de equilibrio fuera de [-1, 1] para el nodo " + numero(nodo));
            return alturaEsperada;
        }

        private static Field campo(Class<?> clase, String nombre) {
            try {
                Field campo = clase.getDeclaredField(nombre);
                campo.setAccessible(true);
                return campo;
            } catch (ReflectiveOperationException error) {
                throw new AssertionError("No se pudo acceder al campo " + nombre, error);
            }
        }

        private static Object leer(Field campo, Object objeto) {
            try {
                return campo.get(objeto);
            } catch (IllegalAccessException error) {
                throw new AssertionError("No se pudo leer el campo " + campo.getName(), error);
            }
        }
    }
}
