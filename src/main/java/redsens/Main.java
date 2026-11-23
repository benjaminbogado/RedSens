package redsens;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.IntFunction;

import redsens.indice.ArbolAVL;
import redsens.indice.ArbolBinarioBusqueda;
import redsens.compresion.DemostracionHuffman;
import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;
import redsens.red.CaminosMinimos;
import redsens.red.CargadorGrafo;
import redsens.red.CorteRuta;
import redsens.red.GrafoLocalidades;
import redsens.red.PlanVisitas;
import redsens.red.Recorridos;
import redsens.registro.EstrategiaExploracion;
import redsens.registro.TablaHash;
import redsens.simulacion.ComprobadorM6;
import redsens.simulacion.GeneradorDatos;
import redsens.simulacion.GeneradorMediciones;

/** Menú de demostración para las funcionalidades disponibles de RedSens. */
public class Main {
    private final Scanner entrada = new Scanner(System.in);
    private List<Estacion> estaciones = List.of();
    private TablaHash hashLineal = nuevaTabla(EstrategiaExploracion.LINEAL);
    private TablaHash hashCuadratica = nuevaTabla(EstrategiaExploracion.CUADRATICA);
    private ArbolBinarioBusqueda abb = new ArbolBinarioBusqueda();
    private ArbolAVL avl = new ArbolAVL();
    private GrafoLocalidades red;

    public static void main(String[] args) {
        new Main().ejecutar();
    }

    private void ejecutar() {
        System.out.println("RedSens iniciado");
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== RedSens | menú principal ===\n");
            System.out.println("0. Conjunto de estaciones");
            System.out.println("1. M1 - Registro hash");
            System.out.println("2. M2 - Índices ABB y AVL");
            System.out.println("3. M3 - Grafos y recorridos");
            System.out.println("4. M4 - Compresión de datos");
            System.out.println("5. M5 - Lista y orden estadístico");
            System.out.println("6. M6 - Generación y verificaciones");
            System.out.println("7. Salir");
            switch (leerEntero("Opción: ")) {
                case 0 -> menuEstaciones();
                case 1 -> menuM1();
                case 2 -> menuM2();
                case 3 -> menuM3();
                case 4 -> menuM4();
                case 5 -> System.out.println("Este módulo todavía no está disponible en esta versión.");
                case 6 -> menuM6();
                case 7 -> salir = true;
                default -> System.out.println("Opción inválida. Elegí un número del menú.");
            }
        }
        System.out.println("RedSens finalizado.");
    }

    private void menuEstaciones() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- Conjunto de estaciones (" + estaciones.size() + ") ---");
            System.out.println("1. Generar estaciones de prueba");
            System.out.println("2. Cargar estaciones desde archivo");
            System.out.println("3. Mostrar resumen del conjunto");
            System.out.println("4. Mostrar estaciones cargadas");
            System.out.println("0. Volver al menú principal");
            switch (leerEntero("Opción: ")) {
                case 1 -> generarEstaciones();
                case 2 -> cargarEstaciones();
                case 3 -> mostrarResumen();
                case 4 -> mostrarEstaciones();
                case 0 -> volver = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void generarEstaciones() {
        int cantidad = leerEntero("Cantidad (0 a " + GeneradorDatos.MAX_ESTACIONES + "): ");
        try {
            reemplazarEstaciones(GeneradorDatos.generarEstaciones(cantidad));
            System.out.println("Se generaron " + estaciones.size()
                    + " estaciones con la semilla del grupo " + GeneradorDatos.SEMILLA_GRUPO + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo generar el conjunto: " + e.getMessage());
        }
    }

    /** Formato: codigo;numero;nombre;localidad;tipo;bateria;diasSinVisita */
    private void cargarEstaciones() {
        System.out.print("Ruta del archivo UTF-8 (una estación por línea): ");
        String ruta = entrada.nextLine().trim();
        try {
            List<String> lineas = Files.readAllLines(Path.of(ruta), StandardCharsets.UTF_8);
            List<Estacion> cargadas = new ArrayList<>();
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.isEmpty() || linea.startsWith("#")
                        || linea.toLowerCase(Locale.ROOT).startsWith("codigo;")) {
                    continue;
                }
                String[] campos = linea.split(";", -1);
                if (campos.length != 7) {
                    throw new IllegalArgumentException("línea " + (i + 1) + ": se esperaban 7 campos separados por ';'");
                }
                try {
                    cargadas.add(new Estacion(campos[0].trim(), Integer.parseInt(campos[1].trim()),
                            campos[2].trim(), campos[3].trim(),
                            TipoEstacion.valueOf(campos[4].trim().toUpperCase(Locale.ROOT)),
                            Integer.parseInt(campos[5].trim()), Integer.parseInt(campos[6].trim())));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("línea " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
            reemplazarEstaciones(cargadas);
            System.out.println("Se cargaron " + estaciones.size() + " estaciones desde " + ruta + ".");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("No se pudo cargar el archivo: " + e.getMessage());
        }
    }

    private void mostrarResumen() {
        if (estaciones.isEmpty()) {
            System.out.println("No hay estaciones cargadas.");
            return;
        }
        System.out.println("Estaciones: " + estaciones.size());
        System.out.println("Hash lineal: " + hashLineal.tamanio() + " activas, capacidad " + hashLineal.capacidad());
        System.out.println("Hash cuadrática: " + hashCuadratica.tamanio() + " activas, capacidad " + hashCuadratica.capacidad());
        System.out.println("ABB: " + abb.cantidadNodos() + " nodos, altura " + abb.altura());
        System.out.println("AVL: " + avl.cantidadNodos() + " nodos, altura " + avl.altura());
    }

    private void mostrarEstaciones() {
        if (estaciones.isEmpty()) {
            System.out.println("No hay estaciones cargadas.");
            return;
        }

        String[] encabezados = {"Número", "Código", "Nombre", "Localidad", "Tipo", "Batería", "Días sin visita"};
        List<String[]> filas = estaciones.stream()
                .map(e -> new String[]{
                    Integer.toString(e.getNumero()), e.getCodigo(), e.getNombre(), e.getLocalidad(),
                    e.getTipo().toString(), e.getBateria() + "%", Integer.toString(e.getDiasSinVisita())
                })
                .toList();

        int[] anchos = new int[encabezados.length];
        for (int columna = 0; columna < encabezados.length; columna++) {
            anchos[columna] = encabezados[columna].length();
            for (String[] fila : filas) {
                anchos[columna] = Math.max(anchos[columna], fila[columna].length());
            }
        }

        imprimirFila(encabezados, anchos);
        String[] separadores = new String[anchos.length];
        for (int i = 0; i < anchos.length; i++) {
            separadores[i] = "-".repeat(anchos[i]);
        }
        imprimirFila(separadores, anchos);
        for (String[] fila : filas) {
            imprimirFila(fila, anchos);
        }
    }

    private static void imprimirFila(String[] celdas, int[] anchos) {
        for (int i = 0; i < celdas.length; i++) {
            if (i > 0) {
                System.out.print(" | ");
            }
            System.out.printf(Locale.ROOT, "%-" + anchos[i] + "s", celdas[i]);
        }
        System.out.println();
    }

    private void menuM1() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- M1: registro hash ---");
            System.out.println("1. Buscar por código usando sondeo lineal");
            System.out.println("2. Buscar por código usando sondeo cuadrático");
            System.out.println("3. Agregar estación a todos los índices");
            System.out.println("4. Eliminar estación de todos los índices");
            System.out.println("0. Volver al menú principal");
            switch (leerEntero("Opción: ")) {
                case 1 -> buscarCodigo(hashLineal);
                case 2 -> buscarCodigo(hashCuadratica);
                case 3 -> agregarEstacion();
                case 4 -> eliminarEstacion();
                case 0 -> volver = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void buscarCodigo(TablaHash tabla) {
        System.out.println("Estrategia utilizada: sondeo " + tabla.getEstrategia());
        System.out.print("Código (CBA-NNNN): ");
        String codigo = entrada.nextLine().trim();
        try {
            Estacion encontrada = tabla.buscar(codigo);
            if (encontrada == null) {
                System.out.println("No existe una estación con el código " + codigo + ".");
            } else {
                System.out.println("Estación encontrada:");
                mostrarEstacion(encontrada);
            }
            System.out.println("Sondeos en esta búsqueda: " + tabla.getSondeosUltimaOperacion());
            System.out.println("Colisiones en esta búsqueda: " + tabla.getColisionesUltimaOperacion());
        } catch (IllegalArgumentException e) {
            System.out.println("Código inválido: " + e.getMessage());
        }
    }

    private void agregarEstacion() {
        try {
            Estacion estacion = leerEstacion();
            agregarAIndices(estacion);
            System.out.println("Estación agregada a M1, ABB y AVL.");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo agregar la estación: " + e.getMessage());
        }
    }

    private void eliminarEstacion() {
        System.out.print("Código a eliminar: ");
        String codigo = entrada.nextLine().trim();
        try {
            Estacion estacion = hashLineal.buscar(codigo);
            if (estacion == null) {
                System.out.println("No existe una estación con el código " + codigo + ".");
                return;
            }
            hashLineal.eliminar(codigo);
            hashCuadratica.eliminar(codigo);
            abb.eliminar(estacion.getNumero());
            avl.eliminar(estacion.getNumero());
            estaciones = estaciones.stream().filter(e -> !e.getCodigo().equals(codigo)).toList();
            System.out.println("Estación eliminada de los cuatro índices.");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo eliminar: " + e.getMessage());
        }
    }

    private void menuM2() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- M2: ABB y AVL ---");
            System.out.println("1. Buscar por número en ABB");
            System.out.println("2. Buscar por número en AVL");
            System.out.println("3. Listar rango inclusivo (AVL)");
            System.out.println("4. Listar estaciones críticas (AVL)");
            System.out.println("5. Mostrar recorridos y métricas (AVL)");
            System.out.println("0. Volver al menú principal");
            switch (leerEntero("Opción: ")) {
                case 1 -> buscarNumero("ABB", abb::buscar);
                case 2 -> buscarNumero("AVL", avl::buscar);
                case 3 -> listarRango();
                case 4 -> mostrarLista(avl.listarCriticas(), "No hay estaciones críticas.");
                case 5 -> mostrarRecorridosYMetricas();
                case 0 -> volver = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void buscarNumero(String nombreArbol, IntFunction<Estacion> busqueda) {
        int numero = leerEntero("Número de estación: ");
        mostrarResultadoBusqueda(nombreArbol, busqueda.apply(numero));
    }

    private void listarRango() {
        int desde = leerEntero("Desde: ");
        int hasta = leerEntero("Hasta: ");
        try {
            mostrarLista(avl.listarPorRango(desde, hasta), "No hay estaciones en ese rango.");
        } catch (IllegalArgumentException e) {
            System.out.println("Rango inválido: " + e.getMessage());
        }
    }

    private void mostrarRecorridosYMetricas() {
        System.out.println("\n=== Resumen del AVL ===");
        System.out.println("Altura (en aristas; árbol vacío = -1): " + avl.altura());
        System.out.println("Cantidad de nodos: " + avl.cantidadNodos());
        System.out.println("Cantidad de hojas: " + avl.cantidadHojas());

        mostrarRecorrido("Inorden", "izquierda → raíz → derecha",
                "Visita las estaciones en orden ascendente por número.", avl.inorden());
        mostrarRecorrido("Preorden", "raíz → izquierda → derecha",
                "Muestra cada raíz antes de recorrer sus subárboles.", avl.preorden());
        mostrarRecorrido("Postorden", "izquierda → derecha → raíz",
                "Muestra cada raíz después de recorrer sus subárboles.", avl.postorden());
    }

    private void mostrarRecorrido(String nombre, String orden, String explicacion,
            List<Estacion> recorrido) {
        System.out.println("\n--- " + nombre + ": " + orden + " ---");
        System.out.println(explicacion);
        if (recorrido.isEmpty()) {
            System.out.println("(sin estaciones)");
            return;
        }
        for (int i = 0; i < recorrido.size(); i++) {
            Estacion estacion = recorrido.get(i);
            System.out.printf("%d. Número: %d | Código: %s%n",
                    i + 1, estacion.getNumero(), estacion.getCodigo());
        }
    }

    // ------------------------------------------------------------------
    // M3: grafos y recorridos
    // ------------------------------------------------------------------

    private static final String CONTROL_M3 = "Córdoba";

    private void menuM3() {
        if (red == null) {
            cargarRed(CargadorGrafo.ARCHIVO_POR_DEFECTO.toString());
        }
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- M3: red vial (base " + PlanVisitas.BASE_GRUPO + ") ---");
            System.out.println("1. Cargar red vial desde archivo");
            System.out.println("2. Mostrar lista y matriz de adyacencia");
            System.out.println("3. BFS: localidades alcanzables por niveles (tramos) desde la base");
            System.out.println("4. DFS: verificar si la red es conexa");
            System.out.println("5. Dijkstra desde la base: distancias y caminos");
            System.out.println("6. Dijkstra de control desde " + CONTROL_M3);
            System.out.println("7. Floyd: distancias mínimas entre todos los pares");
            System.out.println("8. Simular el corte de una ruta");
            System.out.println("9. Plan de visitas para las estaciones críticas (reporte M2)");
            System.out.println("0. Volver al menú principal");
            int opcion = leerEntero("Opción: ");
            if (red == null && opcion >= 2 && opcion <= 9) {
                System.out.println("Primero cargá una red vial (opción 1).");
                continue;
            }
            switch (opcion) {
                case 1 -> {
                    System.out.print("Ruta del archivo [" + CargadorGrafo.ARCHIVO_POR_DEFECTO + "]: ");
                    String ruta = entrada.nextLine().trim();
                    cargarRed(ruta.isEmpty() ? CargadorGrafo.ARCHIVO_POR_DEFECTO.toString() : ruta);
                }
                case 2 -> {
                    System.out.println("\nLista de adyacencia:");
                    System.out.print(red.textoListaAdyacencia());
                    System.out.println("\nMatriz de adyacencia (km, '-' = sin ruta directa):");
                    System.out.print(red.textoMatrizAdyacencia());
                }
                case 3 -> {
                    List<List<String>> niveles = Recorridos.nivelesBfs(red, PlanVisitas.BASE_GRUPO);
                    for (int k = 1; k < niveles.size(); k++) {
                        System.out.printf("A %d tramo%s: %s%n", k, k == 1 ? "" : "s",
                                String.join(", ", niveles.get(k)));
                    }
                }
                case 4 -> {
                    System.out.println("DFS: " + String.join(" > ", Recorridos.dfs(red, PlanVisitas.BASE_GRUPO)));
                    List<String> faltan = Recorridos.inalcanzables(red, PlanVisitas.BASE_GRUPO);
                    System.out.println(faltan.isEmpty() ? "La red es conexa: el DFS visitó las "
                            + red.cantidadLocalidades() + " localidades."
                            : "La red NO es conexa. Sin acceso desde la base: " + String.join(", ", faltan));
                }
                case 5 -> mostrarDijkstra(PlanVisitas.BASE_GRUPO);
                case 6 -> mostrarDijkstra(CONTROL_M3);
                case 7 -> {
                    for (int i = 0; i < red.cantidadLocalidades(); i++) {
                        System.out.printf("[%2d] %s%n", i, red.nombreDe(i));
                    }
                    System.out.print(CaminosMinimos.floyd(red).textoMatriz());
                }
                case 8 -> simularCorte();
                case 9 -> planCriticas();
                case 0 -> volver = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void cargarRed(String ruta) {
        try {
            GrafoLocalidades cargada = CargadorGrafo.cargar(Path.of(ruta));
            if (!cargada.existeLocalidad(PlanVisitas.BASE_GRUPO)) {
                throw new IllegalArgumentException("la red no incluye la base " + PlanVisitas.BASE_GRUPO);
            }
            red = cargada;
            System.out.println("Red cargada: " + red.cantidadLocalidades() + " localidades y "
                    + red.cantidadRutas() + " tramos.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("No se pudo cargar la red vial: " + e.getMessage());
        }
    }

    private void mostrarDijkstra(String origen) {
        if (!red.existeLocalidad(origen)) {
            System.out.println("La red cargada no incluye " + origen + ".");
            return;
        }
        CaminosMinimos.ResultadoDijkstra resultado = CaminosMinimos.dijkstra(red, origen);
        System.out.println("Desde " + origen + ":");
        for (String destino : red.getNombres()) {
            if (resultado.hayCamino(destino)) {
                System.out.printf("  %-17s %4d km  %s%n", destino, resultado.distanciaA(destino),
                        String.join(" > ", resultado.caminoA(destino)));
            } else {
                System.out.printf("  %-17s sin camino%n", destino);
            }
        }
    }

    /** Muestra las localidades numeradas y devuelve la elegida, o null. */
    private String elegirLocalidad(String mensaje) {
        for (int i = 0; i < red.cantidadLocalidades(); i++) {
            System.out.printf("%2d. %s%n", i, red.nombreDe(i));
        }
        int indice = leerEntero(mensaje);
        if (indice < 0 || indice >= red.cantidadLocalidades()) {
            System.out.println("Número de localidad inválido.");
            return null;
        }
        return red.nombreDe(indice);
    }

    private void simularCorte() {
        String origen = elegirLocalidad("Un extremo de la ruta: ");
        if (origen == null) {
            return;
        }
        String destino = elegirLocalidad("El otro extremo: ");
        if (destino == null) {
            return;
        }
        try {
            CorteRuta corte = CorteRuta.simular(red, PlanVisitas.BASE_GRUPO, origen, destino);
            System.out.print(corte.texto());
            System.out.println("(La red cargada no cambia: el corte se simula sobre una copia.)");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo simular: " + e.getMessage());
        }
    }

    /** Integración M2 → M3: el reporte de críticas del AVL define qué localidades visitar. */
    private void planCriticas() {
        if (estaciones.isEmpty()) {
            System.out.println("No hay estaciones cargadas (menú 0).");
            return;
        }
        List<Estacion> criticas = avl.listarCriticas();
        if (criticas.isEmpty()) {
            System.out.println("No hay estaciones críticas: no hace falta salir.");
            return;
        }
        List<String> localidades = new ArrayList<>();
        List<String> fueraDeRed = new ArrayList<>();
        for (String localidad : PlanVisitas.localidadesDe(criticas)) {
            (red.existeLocalidad(localidad) ? localidades : fueraDeRed).add(localidad);
        }
        System.out.println(criticas.size() + " estaciones críticas en " + localidades.size() + " localidades: "
                + String.join(", ", localidades));
        if (!fueraDeRed.isEmpty()) {
            System.out.println("Localidades del reporte que no están en la red: " + String.join(", ", fueraDeRed));
        }
        System.out.print(PlanVisitas.planificar(red, PlanVisitas.BASE_GRUPO, localidades).texto());
        System.out.println("Criterio: vecino más cercano sobre la tabla de Floyd. No garantiza el óptimo:");
        System.out.println("elige cada paso solo por cercanía y puede dejar localidades lejanas para el final.");
    }

    // ------------------------------------------------------------------
    // M4: compresión Huffman
    // ------------------------------------------------------------------

    private void menuM4() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- M4: transmisión comprimida Huffman ---");
            System.out.println("1. Ejecutar demostración");
            System.out.println("0. Volver al menú principal");
            switch (leerEntero("Opción: ")) {
                case 1 -> DemostracionHuffman.ejecutar();
                case 0 -> volver = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void menuM6() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- M6: generación y verificaciones ---");
            System.out.println("1. Ejecutar comprobación de reproducibilidad y frecuencias");
            System.out.println("2. Generar 5000 mediciones para las estaciones cargadas");
            System.out.println("0. Volver al menú principal");
            switch (leerEntero("Opción: ")) {
                case 1 -> ComprobadorM6.main(new String[0]);
                case 2 -> generarMediciones();
                case 0 -> volver = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void generarMediciones() {
        try {
            Path destino = Path.of("datos", "mediciones_menu.txt");
            GeneradorMediciones.generarArchivo(estaciones, 5000, LocalDate.now(),
                    GeneradorDatos.SEMILLA_GRUPO, destino);
            System.out.println("Se generaron 5000 mediciones en " + destino.toAbsolutePath());
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("No se pudieron generar mediciones: " + e.getMessage());
        }
    }

    private Estacion leerEstacion() {
        System.out.print("Código CBA-NNNN: ");
        String codigo = entrada.nextLine().trim();
        int numero = leerEntero("Número (debe coincidir con el código): ");
        System.out.print("Nombre: ");
        String nombre = entrada.nextLine().trim();
        System.out.print("Localidad: ");
        String localidad = entrada.nextLine().trim();
        System.out.print("Tipo (PLUVIOMETRICA, METEOROLOGICA, HIDROMETRICA): ");
        TipoEstacion tipo = TipoEstacion.valueOf(entrada.nextLine().trim().toUpperCase(Locale.ROOT));
        int bateria = leerEntero("Batería (0 a 100): ");
        int dias = leerEntero("Días sin visita (0 o más): ");
        return new Estacion(codigo, numero, nombre, localidad, tipo, bateria, dias);
    }

    private void reemplazarEstaciones(List<Estacion> nuevas) {
        TablaHash nuevoLineal = nuevaTabla(EstrategiaExploracion.LINEAL);
        TablaHash nuevaCuadratica = nuevaTabla(EstrategiaExploracion.CUADRATICA);
        ArbolBinarioBusqueda nuevoAbb = new ArbolBinarioBusqueda();
        ArbolAVL nuevoAvl = new ArbolAVL();
        for (Estacion estacion : nuevas) {
            nuevoLineal.insertar(estacion);
            nuevaCuadratica.insertar(estacion);
            nuevoAbb.insertar(estacion);
            nuevoAvl.insertar(estacion);
        }
        estaciones = List.copyOf(nuevas);
        hashLineal = nuevoLineal;
        hashCuadratica = nuevaCuadratica;
        abb = nuevoAbb;
        avl = nuevoAvl;
    }

    private void agregarAIndices(Estacion estacion) {
        for (Estacion existente : estaciones) {
            if (existente.getNumero() == estacion.getNumero()
                    || existente.getCodigo().equals(estacion.getCodigo())) {
                throw new IllegalArgumentException("Ya existe una estación con ese código o número.");
            }
        }
        hashLineal.insertar(estacion);
        hashCuadratica.insertar(estacion);
        abb.insertar(estacion);
        avl.insertar(estacion);
        List<Estacion> actualizadas = new ArrayList<>(estaciones);
        actualizadas.add(estacion);
        estaciones = List.copyOf(actualizadas);
    }

    private static TablaHash nuevaTabla(EstrategiaExploracion estrategia) {
        return TablaHash.ampliable(17, estrategia);
    }

    private int leerEntero(String mensaje) {
        System.out.print(mensaje);
        String valor = entrada.nextLine().trim();
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida: ingresá un número entero.");
            return Integer.MIN_VALUE;
        }
    }

    private static void mostrarResultadoBusqueda(String indice, Estacion estacion) {
        if (estacion == null) {
            System.out.println(indice + ": no se encontró ese número.");
        } else {
            System.out.print(indice + ": ");
            mostrarEstacion(estacion);
        }
    }

    private static void mostrarLista(List<Estacion> lista, String mensajeVacio) {
        if (lista.isEmpty()) {
            System.out.println(mensajeVacio);
            return;
        }
        for (Estacion estacion : lista) {
            mostrarEstacion(estacion);
        }
    }

    private static void mostrarEstacion(Estacion e) {
        System.out.printf("%s | %s | %s | %s | batería %d%% | %d días sin visita%s%n",
                e.getCodigo(), e.getNombre(), e.getLocalidad(), e.getTipo(), e.getBateria(),
                e.getDiasSinVisita(), e.esCritica() ? " | CRÍTICA" : "");
    }
}
