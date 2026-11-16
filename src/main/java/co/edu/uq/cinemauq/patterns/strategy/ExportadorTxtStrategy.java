package co.edu.uq.cinemauq.patterns.strategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * para guardar el reporte en formato de texto plano (.txt).
 */
public class ExportadorTxtStrategy implements ExportadorReporte {

    @Override
    public void exportar(String contenido, Path destino) throws IOException {
        if (contenido == null || destino == null) {
            throw new IllegalArgumentException("El contenido y la ruta de destino son obligatorios");
        }
        // Escribe el texto en el archivo usando UTF-8 para mantener tildes y eñes
        Files.writeString(destino, contenido, StandardCharsets.UTF_8);
    }

}
