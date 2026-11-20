package redsens.compresion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import redsens.modelo.Estacion;
import redsens.simulacion.GeneradorDatos;
import redsens.simulacion.GeneradorMediciones;

/** Ejecutable sencillo para demostrar M4 desde VS Code o el menú principal. */
public final class DemostracionHuffman {
    private DemostracionHuffman() {
    }

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        try {
            hacerDemostracion();
        } catch (Exception e) {
            System.out.println("M4 no pudo completarse: " + e.getMessage());
        }
    }

    private static void hacerDemostracion() throws IOException {
        Huffman huffman = new Huffman();
        String ww = "WWXWYZWX";
        Huffman.ResultadoCodificacion resultado = huffman.codificar(ww);
        String recuperado = huffman.decodificar(resultado.getBits(), resultado.getRaiz());
        comprobar(resultado.getBitsOriginalesPorCaracter() == 64,
                "WWXWYZWX debe ocupar 64 bits originales.");
        comprobar(resultado.getBits().length() == 14,
                "WWXWYZWX debe producir 14 bits Huffman útiles.");

        System.out.println("=== M4 - Huffman ===");
        System.out.println("Caso: " + ww);
        imprimirTabla(resultado.getTabla(), huffman.contarFrecuencias(
                ww.getBytes(StandardCharsets.UTF_8)));
        System.out.println("Bits originales (8 por carácter): "
                + resultado.getBitsOriginalesPorCaracter());
        System.out.println("Bits Huffman útiles: " + resultado.getBits().length());
        System.out.println("Recuperado: " + recuperado);
        System.out.println("Resultado idéntico: " + ww.equals(recuperado));
        System.out.println("Sin prefijos: " + huffman.sinPrefijos(resultado.getTabla()));

        Path mediciones = prepararMediciones();
        ArchivoHuffman archivo = new ArchivoHuffman();
        Path comprimido = Path.of("resultados", "mediciones-m4.huff");
        ArchivoHuffman.ResultadoArchivo metadatos =
                archivo.comprimir(mediciones, comprimido);
        byte[] recuperados = archivo.descomprimir(comprimido);
        byte[] originales = Files.readAllBytes(mediciones);
        boolean iguales = Arrays.equals(originales, recuperados);
        comprobar(iguales, "El archivo recuperado difiere del original.");
        Path muestraRecuperada = Path.of("resultados", "mediciones-m4-descomprimidas.txt");
        Files.write(muestraRecuperada, recuperados);
        comprobar(Arrays.equals(originales, Files.readAllBytes(muestraRecuperada)),
                "La muestra descomprimida guardada difiere del original.");
        long lineas = Files.readAllLines(mediciones, StandardCharsets.UTF_8).size();
        Huffman.ResultadoCodificacion estadisticas = huffman.codificar(originales);
        boolean sinPrefijos = huffman.sinPrefijos(estadisticas.getTabla());
        comprobar(sinPrefijos, "La tabla de mediciones contiene códigos prefijo.");
        double ahorro = (1.0 - (double) metadatos.getBytesReal() / originales.length) * 100.0;
        System.out.println("\nArchivo: " + mediciones);
        System.out.println("Líneas: " + lineas);
        System.out.println("Tabla de códigos de las mediciones:");
        imprimirTabla(estadisticas.getTabla(), huffman.contarFrecuencias(originales));
        System.out.println("Sin prefijos: " + sinPrefijos);
        System.out.println("Bytes originales: " + originales.length);
        System.out.println("Bits útiles: " + metadatos.getBitsUtiles());
        System.out.println("Relleno último byte: " + metadatos.getBitsRelleno());
        System.out.println("Cabecera: " + metadatos.getBytesCabecera() + " bytes");
        System.out.println("Archivo real: " + metadatos.getBytesReal() + " bytes");
        System.out.printf(Locale.ROOT, "Porcentaje de ahorro real: %.2f%%%n", ahorro);
        System.out.println("Igualdad byte por byte del recuperado: " + iguales);
        System.out.println("Muestra descomprimida: " + muestraRecuperada);
    }

    private static Path prepararMediciones() throws IOException {
        Path mediciones = Path.of("datos", "mediciones_grupo3.txt");
        if (Files.isRegularFile(mediciones)) {
            long lineas = Files.readAllLines(mediciones, StandardCharsets.UTF_8).size();
            if (lineas >= 5000) {
                return mediciones;
            }
        }
        List<Estacion> estaciones =
                GeneradorDatos.generarEstaciones(1000, GeneradorDatos.SEMILLA_GRUPO);
        GeneradorMediciones.generarArchivo(estaciones, 5000, LocalDate.of(2026, 10, 13),
                3026L, mediciones);
        return mediciones;
    }

    public static void imprimirTabla(Map<Byte, String> tabla, long[] frecuencias) {
        List<Map.Entry<Byte, String>> entradas =
                new ArrayList<>(tabla.entrySet());
        entradas.sort(Comparator.comparingInt(entrada -> entrada.getKey() & 0xff));
        System.out.println("Símbolo | Frecuencia | Código");
        for (Map.Entry<Byte, String> entrada : entradas) {
            int simbolo = entrada.getKey() & 0xff;
            System.out.printf(Locale.ROOT, "%s | %d | %s%n",
                    Huffman.representarSimbolo(simbolo), frecuencias[simbolo],
                    entrada.getValue());
        }
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}
