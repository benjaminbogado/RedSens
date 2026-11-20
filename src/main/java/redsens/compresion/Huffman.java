package redsens.compresion;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

/** Construcción, codificación y decodificación Huffman. */
public class Huffman {

    public static final class ResultadoCodificacion {
        private final String bits;
        private final Map<Byte, String> tabla;
        private final Nodo raiz;
        private final int caracteresOriginales;
        private final int bytesOriginales;

        public ResultadoCodificacion(String bits, Map<Byte, String> tabla, Nodo raiz,
                                     int caracteresOriginales, int bytesOriginales) {
            this.bits = bits;
            this.tabla = tabla;
            this.raiz = raiz;
            this.caracteresOriginales = caracteresOriginales;
            this.bytesOriginales = bytesOriginales;
        }

        public String getBits() {
            return bits;
        }

        public Map<Byte, String> getTabla() {
            return tabla;
        }

        public Nodo getRaiz() {
            return raiz;
        }

        public int getCaracteresOriginales() {
            return caracteresOriginales;
        }

        public int getBytesOriginales() {
            return bytesOriginales;
        }

        public long getBitsOriginalesPorCaracter() {
            return (long) caracteresOriginales * 8L;
        }

        public long getBitsOriginalesPorByte() {
            return (long) bytesOriginales * 8L;
        }
    }

    public Nodo construirArbolHuffman(byte[] datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos.");
        }
        long[] frecuencias = contarFrecuencias(datos);
        PriorityQueue<Nodo> cola = new PriorityQueue<>(comparadorHuffman());
        for (int i = 0; i < frecuencias.length; i++) {
            if (frecuencias[i] > 0) {
                cola.add(new Nodo(i, frecuencias[i]));
            }
        }
        if (cola.isEmpty()) {
            return null;
        }
        while (cola.size() > 1) {
            Nodo izquierda = cola.remove();
            Nodo derecha = cola.remove();
            cola.add(new Nodo(izquierda.getFrecuencia() + derecha.getFrecuencia(),
                    izquierda, derecha));
        }
        return cola.remove();
    }

    public ResultadoCodificacion codificar(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El texto no puede ser nulo.");
        }
        byte[] datos = texto.getBytes(StandardCharsets.UTF_8);
        Nodo raiz = construirArbolHuffman(datos);
        if (raiz == null) {
            return new ResultadoCodificacion("", new LinkedHashMap<>(), null,
                    texto.length(), 0);
        }
        Map<Byte, String> tabla = construirTablaCodigos(raiz);
        StringBuilder bits = new StringBuilder();
        for (byte valor : datos) {
            String codigo = tabla.get(valor);
            if (codigo == null) {
                throw new IllegalStateException("No existe código para un byte de entrada.");
            }
            bits.append(codigo);
        }
        return new ResultadoCodificacion(bits.toString(), tabla, raiz,
                texto.length(), datos.length);
    }

    public ResultadoCodificacion codificar(byte[] datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos.");
        }
        Nodo raiz = construirArbolHuffman(datos);
        if (raiz == null) {
            return new ResultadoCodificacion("", new LinkedHashMap<>(), null, 0, 0);
        }
        Map<Byte, String> tabla = construirTablaCodigos(raiz);
        StringBuilder bits = new StringBuilder();
        for (byte valor : datos) {
            String codigo = tabla.get(valor);
            if (codigo == null) {
                throw new IllegalStateException("No existe código para un byte de entrada.");
            }
            bits.append(codigo);
        }
        return new ResultadoCodificacion(bits.toString(), tabla, raiz, 0, datos.length);
    }

    public Map<Byte, String> construirTablaCodigos(Nodo raiz) {
        Map<Byte, String> tabla = new LinkedHashMap<>();
        if (raiz != null) {
            recorrerCodigos(raiz, "", tabla);
        }
        if (!sinPrefijos(tabla)) {
            throw new IllegalStateException("Se encontró un código que es prefijo de otro.");
        }
        return tabla;
    }

    public String decodificar(String bitsCodificados, Nodo raiz) {
        byte[] datos = decodificarBits(bitsCodificados, raiz);
        return new String(datos, StandardCharsets.UTF_8);
    }

    public byte[] decodificarBits(String bitsCodificados, Nodo raiz) {
        if (bitsCodificados == null) {
            throw new IllegalArgumentException("Los bits no pueden ser nulos.");
        }
        if (bitsCodificados.isEmpty()) {
            return new byte[0];
        }
        if (raiz == null) {
            throw new IllegalArgumentException("No existe árbol para decodificar.");
        }
        if (raiz.esHoja()) {
            java.io.ByteArrayOutputStream unico = new java.io.ByteArrayOutputStream();
            for (int i = 0; i < bitsCodificados.length(); i++) {
                if (bitsCodificados.charAt(i) != '0') {
                    throw new IllegalArgumentException("El código de un solo símbolo debe usar solo ceros.");
                }
                unico.write(raiz.getSimbolo());
            }
            return unico.toByteArray();
        }
        java.io.ByteArrayOutputStream salida = new java.io.ByteArrayOutputStream();
        Nodo actual = raiz;
        for (int i = 0; i < bitsCodificados.length(); i++) {
            char bit = bitsCodificados.charAt(i);
            if (bit != '0' && bit != '1') {
                throw new IllegalArgumentException("La cadena contiene caracteres que no son bits.");
            }
            actual = bit == '0' ? actual.getIzquierdo() : actual.getDerecho();
            if (actual == null) {
                throw new IllegalArgumentException("Secuencia inválida para el árbol Huffman.");
            }
            if (actual.esHoja()) {
                salida.write(actual.getSimbolo());
                actual = raiz;
            }
        }
        if (actual != raiz) {
            throw new IllegalArgumentException("Los bits terminan en un nodo interno.");
        }
        return salida.toByteArray();
    }

    public boolean sinPrefijos(Map<Byte, String> tabla) {
        if (tabla == null) {
            return false;
        }
        for (Map.Entry<Byte, String> uno : tabla.entrySet()) {
            for (Map.Entry<Byte, String> otro : tabla.entrySet()) {
                if (uno.getKey().equals(otro.getKey())) {
                    continue;
                }
                if (uno.getValue().startsWith(otro.getValue())
                        || otro.getValue().startsWith(uno.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    public long[] contarFrecuencias(byte[] datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos no pueden ser nulos.");
        }
        long[] frecuencias = new long[256];
        for (byte dato : datos) {
            frecuencias[dato & 0xff]++;
        }
        return frecuencias;
    }

    public static String representarSimbolo(int simbolo) {
        if (simbolo == 32) {
            return "ESP";
        }
        if (simbolo == 10) {
            return "LF";
        }
        if (simbolo == 13) {
            return "CR";
        }
        if (simbolo == 9) {
            return "TAB";
        }
        if (simbolo >= 32 && simbolo <= 126) {
            return "'" + (char) simbolo + "'";
        }
        return String.format("0x%02X", simbolo);
    }

    private Comparator<Nodo> comparadorHuffman() {
        return (uno, otro) -> {
            int frecuencia = Long.compare(uno.getFrecuencia(), otro.getFrecuencia());
            if (frecuencia != 0) {
                return frecuencia;
            }
            return Integer.compare(uno.getSimboloMinimo(), otro.getSimboloMinimo());
        };
    }

    private void recorrerCodigos(Nodo nodo, String prefijo, Map<Byte, String> tabla) {
        if (nodo.esHoja()) {
            if (tabla.isEmpty() && prefijo.isEmpty()) {
                tabla.put((byte) nodo.getSimbolo().intValue(), "0");
            } else {
                tabla.put((byte) nodo.getSimbolo().intValue(), prefijo);
            }
            return;
        }
        recorrerCodigos(nodo.getIzquierdo(), prefijo + "0", tabla);
        recorrerCodigos(nodo.getDerecho(), prefijo + "1", tabla);
    }
}
