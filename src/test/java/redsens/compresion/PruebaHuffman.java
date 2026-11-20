package redsens.compresion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import redsens.modelo.Estacion;
import redsens.simulacion.GeneradorDatos;
import redsens.simulacion.GeneradorMediciones;

/** Pruebas ejecutables de M4 sin dependencia de -ea. */
public final class PruebaHuffman {
    private PruebaHuffman() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("descomprimir")) {
            throw new IllegalArgumentException(
                    "Uso: PruebaHuffman descomprimir <archivo.huff>");
        }
        if (args.length == 2 && args[0].equals("descomprimir")) {
            ArchivoHuffman archivo = new ArchivoHuffman();
            byte[] recuperados = archivo.descomprimir(Path.of(args[1]));
            System.out.println("DECOMPRESS_OK");
            System.out.println("BYTES=" + recuperados.length);
            System.out.println("SHA256=" + sha256(recuperados));
            return;
        }
        ejecutarTodas();
    }

    private static void ejecutarTodas() throws Exception {
        StringBuilder informe = new StringBuilder();
        Huffman huffman = new Huffman();

        String caso = "WWXWYZWX";
        Huffman.ResultadoCodificacion ww = huffman.codificar(caso);
        comprobar(ww.getBits().length() == 14,
                "WWXWYZWX debe producir 14 bits útiles");
        comprobar(ww.getBitsOriginalesPorCaracter() == 64,
                "WWXWYZWX debe ocupar 64 bits originales");
        comprobar(huffman.sinPrefijos(ww.getTabla()),
                "Los códigos WWXWYZWX deben ser libres de prefijos");
        comprobar(caso.equals(huffman.decodificar(ww.getBits(), ww.getRaiz())),
                "WWXWYZWX no se recuperó idénticamente");

        informar(informe, "Caso WWXWYZWX");
        informar(informe, "Bits originales: " + ww.getBitsOriginalesPorCaracter());
        informar(informe, "Bits Huffman útiles: " + ww.getBits().length());
        informar(informe, "Sin prefijos: OK");
        agregarTabla(informe, ww.getTabla(),
                huffman.contarFrecuencias(caso.getBytes(StandardCharsets.UTF_8)));

        Huffman.ResultadoCodificacion vacio = huffman.codificar("");
        comprobar(vacio.getBits().isEmpty() && vacio.getRaiz() == null,
                "La entrada vacía debe quedar sin árbol ni bits");
        probarArchivoCaso("vacio", new byte[0]);

        Huffman.ResultadoCodificacion unico = huffman.codificar("aaaa");
        comprobar("0000".equals(unico.getBits()),
                "Un solo símbolo repetido debe codificarse con ceros");
        comprobar("aaaa".equals(huffman.decodificar(unico.getBits(), unico.getRaiz())),
                "No se recuperó correctamente un solo símbolo");
        probarArchivoCaso("unico", "aaaa".getBytes(StandardCharsets.UTF_8));

        Huffman.ResultadoCodificacion empate = huffman.codificar("dcba");
        comprobar(huffman.sinPrefijos(empate.getTabla()),
                "Los frecuencias empatadas deben conservar ausencia de prefijos");
        comprobar("dcba".equals(huffman.decodificar(empate.getBits(), empate.getRaiz())),
                "No se recuperaron frecuencias empatadas");
        comprobar(empate.getBits().equals(huffman.codificar("dcba").getBits()),
                "Los empates deben producir resultados deterministas");

        String unicode = "áé í\núltima";
        Huffman.ResultadoCodificacion resultadoUnicode = huffman.codificar(unicode);
        comprobar(unicode.equals(huffman.decodificar(resultadoUnicode.getBits(),
                resultadoUnicode.getRaiz())),
                "Tildes y saltos de línea no se recuperaron exactamente");
        comprobar(huffman.sinPrefijos(resultadoUnicode.getTabla()),
                "Los códigos Unicode deben ser libres de prefijos");
        byte[] bytesUnicode = unicode.getBytes(StandardCharsets.UTF_8);
        probarArchivoCaso("utf8", bytesUnicode);
        informar(informe, "ArchivoHuffman vacío: compresión/descompresión byte por byte OK");
        informar(informe, "ArchivoHuffman un símbolo: compresión/descompresión byte por byte OK");
        informar(informe, "ArchivoHuffman UTF-8: compresión/descompresión byte por byte OK");

        Path mediciones = prepararMediciones();
        byte[] originales = Files.readAllBytes(mediciones);
        long lineas = Files.readAllLines(mediciones, StandardCharsets.UTF_8).size();
        comprobar(lineas >= 5000,
                "El archivo de mediciones debe tener al menos 5000 líneas");

        ArchivoHuffman archivo = new ArchivoHuffman();
        Path comprimido = Path.of("resultados", "mediciones-m4.huff");
        ArchivoHuffman.ResultadoArchivo metadatos =
                archivo.comprimir(mediciones, comprimido);
        byte[] recuperados = archivo.descomprimir(comprimido);
        comprobar(Arrays.equals(originales, recuperados),
                "El archivo M6 no se recuperó byte por byte");
        Path muestraRecuperada = Path.of("resultados", "mediciones-m4-descomprimidas.txt");
        Files.write(muestraRecuperada, recuperados);
        comprobar(Arrays.equals(originales, Files.readAllBytes(muestraRecuperada)),
                "La muestra descomprimida guardada difiere del original");

        long originalesBits = (long) originales.length * 8L;
        double tasa = (1.0 - (double) metadatos.getBytesReal()
                / (double) originales.length) * 100.0;
        informar(informe, "Archivo de mediciones");
        informar(informe, "Líneas: " + lineas);
        informar(informe, "Bytes originales: " + originales.length);
        informar(informe, "Bits originales (8 por byte): " + originalesBits);
        informar(informe, "Bits útiles Huffman: " + metadatos.getBitsUtiles());
        informar(informe, "Relleno último byte: " + metadatos.getBitsRelleno());
        informar(informe, "Cabecera: " + metadatos.getBytesCabecera() + " bytes");
        informar(informe, "Payload: " + metadatos.getBytesPayload() + " bytes");
        informar(informe, "Archivo comprimido real: " + metadatos.getBytesReal() + " bytes");
        informar(informe, String.format(Locale.ROOT,
                "Porcentaje de ahorro real: %.2f%%", tasa));
        informar(informe, "Igualdad byte por byte: OK");
        Huffman.ResultadoCodificacion tablaMediciones = huffman.codificar(originales);
        agregarTabla(informe, tablaMediciones.getTabla(),
                huffman.contarFrecuencias(originales));

        probarFormatosInvalidos(comprimido);
        informar(informe, "Cabecera inválida rechazada: OK");
        informar(informe, "Payload truncado rechazado: OK");
        String independiente = probarDescompresionSeparada(comprimido, originales);
        informar(informe, "Descompresión en proceso Java independiente: OK");
        informar(informe, independiente);

        Files.createDirectories(Path.of("resultados").toAbsolutePath());
        Files.writeString(Path.of("resultados", "ejecucion-m4.txt"),
                informe.toString(), StandardCharsets.UTF_8);

        System.out.println(informe);
        System.out.println("Pruebas M4: OK");
    }

    private static Path prepararMediciones() throws IOException {
        Path mediciones = Path.of("datos", "mediciones_grupo3.txt");
        if (Files.isRegularFile(mediciones)
                && Files.readAllLines(mediciones, StandardCharsets.UTF_8).size() >= 5000) {
            return mediciones;
        }
        List<Estacion> estaciones =
                GeneradorDatos.generarEstaciones(1000, GeneradorDatos.SEMILLA_GRUPO);
        GeneradorMediciones.generarArchivo(estaciones, 5000, LocalDate.of(2026, 10, 13),
                3026L, mediciones);
        return mediciones;
    }

    private static void probarArchivoCaso(String nombre, byte[] original) throws IOException {
        Path carpeta = Path.of("out", "m4-casos");
        Files.createDirectories(carpeta);
        Path entrada = carpeta.resolve(nombre + ".bin");
        Path comprimido = carpeta.resolve(nombre + ".huff");
        Files.write(entrada, original);
        ArchivoHuffman archivo = new ArchivoHuffman();
        archivo.comprimir(entrada, comprimido);
        byte[] recuperados = archivo.descomprimir(comprimido);
        comprobar(Arrays.equals(original, recuperados),
                "ArchivoHuffman no recuperó el caso " + nombre);
        Files.deleteIfExists(entrada);
        Files.deleteIfExists(comprimido);
    }

    private static void probarFormatosInvalidos(Path comprimido) throws IOException {
        Path carpeta = Path.of("out", "m4-invalidos");
        Files.createDirectories(carpeta);
        byte[] correcto = Files.readAllBytes(comprimido);
        Path cabeceraInvalida = carpeta.resolve("cabecera-invalida.huff");
        byte[] magicIncorrecto = correcto.clone();
        magicIncorrecto[0] = 'X';
        Files.write(cabeceraInvalida, magicIncorrecto);
        try {
            new ArchivoHuffman().descomprimir(cabeceraInvalida);
            throw new IllegalStateException("Una cabecera inválida no fue rechazada");
        } catch (IOException e) {
            if (!e.getMessage().contains("magic")) {
                throw e;
            }
        }
        Files.deleteIfExists(cabeceraInvalida);

        Path payloadTruncado = carpeta.resolve("payload-truncado.huff");
        comprobar(correcto.length > 24, "El archivo de prueba debe tener payload.");
        Files.write(payloadTruncado, Arrays.copyOf(correcto, correcto.length - 1));
        try {
            new ArchivoHuffman().descomprimir(payloadTruncado);
            throw new IllegalStateException("Un payload truncado no fue rechazado");
        } catch (IOException e) {
            if (!e.getMessage().contains("payload")) {
                throw e;
            }
        }
        Files.deleteIfExists(payloadTruncado);
    }

    private static String probarDescompresionSeparada(Path comprimido, byte[] originales)
            throws Exception {
        String ejecutable = System.getProperty("os.name").toLowerCase(Locale.ROOT)
                .contains("win") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", ejecutable);
        comprobar(Files.isRegularFile(java),
                "No se encontró el ejecutable Java de java.home: " + java);
        String classpath = System.getProperty("java.class.path");
        comprobar(classpath != null && !classpath.isEmpty(),
                "java.class.path está vacío.");
        Process proceso = new ProcessBuilder(
                java.toString(), "-cp", classpath, PruebaHuffman.class.getName(),
                "descomprimir", comprimido.toAbsolutePath().toString())
                .redirectErrorStream(true)
                .start();
        StringBuilder salida = new StringBuilder();
        try (BufferedReader lector =
                     new BufferedReader(new InputStreamReader(
                             proceso.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                salida.append(linea).append(System.lineSeparator());
            }
        }
        int codigo = proceso.waitFor();
        comprobar(codigo == 0, "La descompresión separada terminó con error");
        String bytes = valorSalida(salida.toString(), "BYTES=");
        String hash = valorSalida(salida.toString(), "SHA256=");
        comprobar(salida.toString().contains("DECOMPRESS_OK"),
                "La descompresión separada no confirmó el resultado");
        comprobar(Integer.parseInt(bytes) == originales.length,
                "La descompresión separada recuperó una cantidad de bytes distinta.");
        comprobar(sha256(originales).equals(hash),
                "El SHA-256 de la descompresión separada no coincide.");
        return "Proceso independiente: bytes=" + bytes + ", SHA-256=" + hash;
    }

    private static String valorSalida(String salida, String prefijo) {
        for (String linea : salida.split("\\R")) {
            if (linea.startsWith(prefijo)) {
                return linea.substring(prefijo.length());
            }
        }
        throw new IllegalStateException("El proceso no informó " + prefijo);
    }

    private static String sha256(byte[] datos) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(datos));
    }

    private static void agregarTabla(StringBuilder informe, Map<Byte, String> tabla,
                                     long[] frecuencias) {
        informe.append("Tabla de códigos:\n");
        informe.append("Símbolo | Frecuencia | Código\n");
        List<Map.Entry<Byte, String>> entradas = new ArrayList<>(tabla.entrySet());
        entradas.sort(Comparator.comparingInt(entrada -> entrada.getKey() & 0xff));
        for (Map.Entry<Byte, String> entrada : entradas) {
            int simbolo = entrada.getKey() & 0xff;
            informe.append(String.format(Locale.ROOT, "%s | %d | %s%n",
                    Huffman.representarSimbolo(simbolo), frecuencias[simbolo],
                    entrada.getValue()));
        }
    }

    private static void informar(StringBuilder informe, String texto) {
        System.out.println(texto);
        informe.append(texto).append(System.lineSeparator());
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException("ERROR: " + mensaje);
        }
    }
}
