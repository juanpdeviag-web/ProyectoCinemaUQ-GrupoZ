package co.edu.uq.cinemauq.patterns.strategy;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interfaz Strategy para exportar los reportes del sistema en formatos (TXT, PDF)
 */
public interface ExportadorReporte {

    // Recibe el texto del reporte ya generado y la ruta donde se guardará el archivo
    void exportar(String contenido, Path destino) throws IOException;

}
