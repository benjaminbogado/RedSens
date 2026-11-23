package redsens.compresion;

import java.io.IOException;
import java.nio.file.Path;

/** Interfaz sencilla para integrar M4 con el menú o con otros módulos. */
public interface TransmisionComprimida {
    ArchivoHuffman.ResultadoArchivo comprimir(Path origen, Path destino) throws IOException;

    byte[] descomprimir(Path comprimido) throws IOException;
}
