package redsens.compresion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Contenedor binario RHUF1 con tabla, tamaño útil y relleno. */
public class ArchivoHuffman implements TransmisionComprimida {
    private static final byte[] MAGIC = {'R', 'H', 'U', 'F'};
    private static final byte VERSION = 1;

    public static final class ResultadoArchivo {
        private final long bitsUtiles;
        private final int bitsRelleno;
        private final int bytesCabecera;
        private final int bytesReal;
        private final int bytesPayload;

        public ResultadoArchivo(long bitsUtiles, int bitsRelleno, int bytesCabecera,
                                int bytesReal, int bytesPayload) {
            this.bitsUtiles = bitsUtiles;
            this.bitsRelleno = bitsRelleno;
            this.bytesCabecera = bytesCabecera;
            this.bytesReal = bytesReal;
            this.bytesPayload = bytesPayload;
        }

        public long getBitsUtiles() {
            return bitsUtiles;
        }

        public int getBitsRelleno() {
            return bitsRelleno;
        }

        public int getBytesCabecera() {
            return bytesCabecera;
        }

        public int getBytesReal() {
            return bytesReal;
        }

        public int getBytesPayload() {
            return bytesPayload;
        }
    }

    private final Huffman huffman = new Huffman();

    @Override
    public ResultadoArchivo comprimir(Path origen, Path destino) throws IOException {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Las rutas no pueden ser nulas.");
        }
        if (!Files.isRegularFile(origen)) {
            throw new IOException("Archivo de entrada inexistente: " + origen);
        }
        byte[] datos = Files.readAllBytes(origen);
        Huffman.ResultadoCodificacion resultado = huffman.codificar(datos);
        byte[] contenedor = construirContenedor(datos.length, resultado.getBits(), resultado.getTabla());
        ResultadoArchivo metadatos = calcularMetadatos(contenedor.length,
                resultado.getBits().length(), resultado.getTabla());
        Files.createDirectories(destino.toAbsolutePath().getParent());
        Files.write(destino, contenedor);
        return metadatos;
    }

    @Override
    public byte[] descomprimir(Path comprimido) throws IOException {
        if (comprimido == null) {
            throw new IllegalArgumentException("La ruta no puede ser nula.");
        }
        if (!Files.isRegularFile(comprimido)) {
            throw new IOException("Archivo comprimido inexistente: " + comprimido);
        }
        return leerContenedor(Files.readAllBytes(comprimido));
    }

    public void descomprimir(Path comprimido, Path destino) throws IOException {
        byte[] datos = descomprimir(comprimido);
        Files.createDirectories(destino.toAbsolutePath().getParent());
        Files.write(destino, datos);
    }

    public String decodificarTexto(byte[] contenedor) throws IOException {
        return new String(leerContenedor(contenedor), StandardCharsets.UTF_8);
    }

    private byte[] construirContenedor(long longitudOriginal, String bits,
                                       Map<Byte, String> tabla) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        salida.write(MAGIC, 0, MAGIC.length);
        salida.write(VERSION);
        salida.write(longToBytes(longitudOriginal), 0, 8);
        salida.write(longToBytes(bits.length()), 0, 8);
        int relleno = (8 - (bits.length() % 8)) % 8;
        salida.write(relleno);
        salida.write((shortTabla(tabla.size())), 0, 2);

        for (Map.Entry<Byte, String> entrada : tabla.entrySet()) {
            salida.write(entrada.getKey());
            String codigo = entrada.getValue();
            salida.write(shortTabla(codigo.length()), 0, 2);
            byte[] codigoBytes = empaquetar(codigo);
            salida.write(codigoBytes, 0, codigoBytes.length);
        }

        byte[] payload = empaquetar(bits);
        salida.write(payload, 0, payload.length);
        return salida.toByteArray();
    }

    private byte[] leerContenedor(byte[] archivo) throws IOException {
        int minimo = 24;
        if (archivo.length < minimo) {
            throw new IOException("Formato inválido: cabecera truncada.");
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (archivo[i] != MAGIC[i]) {
                throw new IOException("Formato inválido: magic RHUF no encontrado.");
            }
        }
        if (archivo[4] != VERSION) {
            throw new IOException("Formato inválido: versión no soportada.");
        }
        long longitudOriginal = bytesToLong(archivo, 5);
        long bitsUtiles = bytesToLong(archivo, 13);
        int relleno = archivo[21] & 0xff;
        int tablaTamano = unsignedShort(archivo, 22);
        if (relleno > 7) {
            throw new IOException("Formato inválido: relleno de último byte mayor que 7.");
        }
        if (longitudOriginal < 0 || bitsUtiles < 0) {
            throw new IOException("Formato inválido: tamaños negativos.");
        }

        int posicion = minimo;
        Map<String, Integer> inversa = new HashMap<>();
        Map<Byte, String> tabla = new LinkedHashMap<>();
        for (int i = 0; i < tablaTamano; i++) {
            if (posicion + 3 > archivo.length) {
                throw new IOException("Formato inválido: entrada de tabla truncada.");
            }
            int simbolo = archivo[posicion++] & 0xff;
            int largoCodigo = unsignedShort(archivo, posicion);
            posicion += 2;
            if (largoCodigo < 1 || largoCodigo > 255) {
                throw new IOException("Formato inválido: longitud de código fuera de rango.");
            }
            int codigoBytes = (largoCodigo + 7) / 8;
            if (posicion + codigoBytes > archivo.length) {
                throw new IOException("Formato inválido: bytes del código truncados.");
            }
            String codigo = descomponerCodigo(archivo, posicion, codigoBytes, largoCodigo);
            posicion += codigoBytes;
            Byte simboloByte = (byte) simbolo;
            if (tabla.putIfAbsent(simboloByte, codigo) != null) {
                throw new IOException("Formato inválido: símbolo duplicado en la tabla.");
            }
            if (inversa.putIfAbsent(codigo, simbolo) != null) {
                throw new IOException("Formato inválido: códigos duplicados.");
            }
        }
        if (!huffman.sinPrefijos(tabla)) {
            throw new IOException("Formato inválido: existe un código prefijo de otro.");
        }

        long payloadEsperado = (bitsUtiles + 7L) / 8L;
        if (archivo.length - posicion != payloadEsperado) {
            throw new IOException("Formato inválido: longitud de payload inconsistente.");
        }
        if (relleno != (bitsUtiles == 0 ? 0 : (int) ((8 - (bitsUtiles % 8)) % 8))) {
            throw new IOException("Formato inválido: relleno inconsistente.");
        }
        if (tablaTamano == 0) {
            if (longitudOriginal != 0 || bitsUtiles != 0) {
                throw new IOException("Formato inválido: tabla vacía con contenido.");
            }
            return new byte[0];
        }

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        StringBuilder actual = new StringBuilder();
        long procesados = 0;
        for (int i = posicion; i < archivo.length && procesados < bitsUtiles; i++) {
            int valor = archivo[i] & 0xff;
            for (int bit = 7; bit >= 0 && procesados < bitsUtiles; bit--, procesados++) {
                actual.append(((valor >>> bit) & 1) == 0 ? '0' : '1');
                Integer simbolo = inversa.get(actual.toString());
                if (simbolo != null) {
                    salida.write(simbolo);
                    actual.setLength(0);
                }
                if (actual.length() > 255) {
                    throw new IOException("Formato inválido: secuencia sin código válido.");
                }
            }
        }
        if (actual.length() != 0) {
            throw new IOException("Formato inválido: bits útiles terminan en un código incompleto.");
        }
        if (salida.size() != longitudOriginal) {
            throw new IOException("Formato inválido: tamaño de contenido recuperado inconsistente.");
        }
        return salida.toByteArray();
    }

    private ResultadoArchivo calcularMetadatos(int real, int utiles, Map<Byte, String> tabla) {
        int relleno = (8 - (utiles % 8)) % 8;
        int cabecera = 24;
        for (Map.Entry<Byte, String> entrada : tabla.entrySet()) {
            cabecera += 3 + (entrada.getValue().length() + 7) / 8;
        }
        int payload = (utiles + 7) / 8;
        return new ResultadoArchivo(utiles, relleno, cabecera, real,
                payload);
    }

    private byte[] empaquetar(String bits) {
        int longitud = (bits.length() + 7) / 8;
        byte[] salida = new byte[longitud];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                salida[i / 8] |= (byte) (1 << (7 - (i % 8)));
            }
        }
        return salida;
    }

    private String descomponerCodigo(byte[] archivo, int posicion, int bytes,
                                     int largoCodigo) {
        StringBuilder codigo = new StringBuilder(largoCodigo);
        for (int i = 0; i < largoCodigo; i++) {
            int bytePosicion = i / 8;
            int bitPosicion = 7 - (i % 8);
            int valor = (archivo[posicion + bytePosicion] >>> bitPosicion) & 1;
            codigo.append(valor);
        }
        return codigo.toString();
    }

    private byte[] shortTabla(int valor) {
        return new byte[]{(byte) ((valor >>> 8) & 0xff), (byte) (valor & 0xff)};
    }

    private int unsignedShort(byte[] datos, int posicion) {
        return ((datos[posicion] & 0xff) << 8) | (datos[posicion + 1] & 0xff);
    }

    private byte[] longToBytes(long valor) {
        byte[] datos = new byte[8];
        for (int i = 7; i >= 0; i--) {
            datos[i] = (byte) valor;
            valor >>>= 8;
        }
        return datos;
    }

    private long bytesToLong(byte[] datos, int posicion) {
        long valor = 0;
        for (int i = 0; i < 8; i++) {
            valor = (valor << 8) | (datos[posicion + i] & 0xffL);
        }
        return valor;
    }
}
