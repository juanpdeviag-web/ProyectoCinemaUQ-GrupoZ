package co.edu.uq.cinemauq.patterns.strategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * para exportar el reporte en formato PDF.
 */
public class ExportadorPdfStrategy implements ExportadorReporte {

    private static final int LINEAS_POR_PAGINA = 48;

    @Override
    public void exportar(String contenido, Path destino) throws IOException {
        if (contenido == null || destino == null) {
            throw new IllegalArgumentException("El contenido y la ruta de destino son obligatorios");
        }

        List<String> lineas = prepararLineas(contenido);
        List<List<String>> paginas = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i += LINEAS_POR_PAGINA) {
            paginas.add(lineas.subList(i, Math.min(i + LINEAS_POR_PAGINA, lineas.size())));
        }
        if (paginas.isEmpty()) {
            paginas.add(List.of("Reporte sin registros."));
        }

        byte[] pdfBytes = construirDocumentoPdf(paginas);
        Files.write(destino, pdfBytes);
    }

    // Divide el reporte en líneas cortas para que no se salgan del borde de la hoja
    private List<String> prepararLineas(String contenido) {
        List<String> resultado = new ArrayList<>();
        for (String linea : contenido.replace("\r", "").split("\n")) {
            String limpia = limpiarTexto(linea);
            while (limpia.length() > 95) {
                resultado.add(limpia.substring(0, 95));
                limpia = "  " + limpia.substring(95);
            }
            resultado.add(limpia);
        }
        return resultado;
    }

    // Ajusta caracteres especiales para el formato estándar PDF (WinAnsi)
    private String limpiarTexto(String s) {
        return s.replace("·", "-")
                .replace("→", "->")
                .replace("×", "x")
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    // Arma la estructura interna del PDF (páginas, fuente Helvetica y texto)
    private byte[] construirDocumentoPdf(List<List<String>> paginas) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        escribir(out, "%PDF-1.4\n");

        int totalPaginas = paginas.size();
        int idFuente = 3 + (totalPaginas * 2);

        // Objeto 1: Catálogo principal
        offsets.add(out.size());
        escribir(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Objeto 2: Árbol de páginas
        StringBuilder kids = new StringBuilder("[");
        for (int i = 0; i < totalPaginas; i++) {
            kids.append(3 + (i * 2)).append(" 0 R ");
        }
        kids.append("]");
        offsets.add(out.size());
        escribir(out, "2 0 obj\n<< /Type /Pages /Kids " + kids + " /Count " + totalPaginas + " >>\nendobj\n");

        // Objetos de cada página y su texto
        for (int i = 0; i < totalPaginas; i++) {
            int idPag = 3 + (i * 2);
            int idCont = idPag + 1;

            StringBuilder flujo = new StringBuilder("BT\n/F1 10 Tf\n14 TL\n45 750 Td\n");
            for (String linea : paginas.get(i)) {
                flujo.append("(").append(linea).append(") Tj\nT*\n");
            }
            flujo.append("ET\n");
            byte[] bytesFlujo = flujo.toString().getBytes(StandardCharsets.ISO_8859_1);

            offsets.add(out.size());
            escribir(out, idPag + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                    "/Resources << /Font << /F1 " + idFuente + " 0 R >> >> /Contents " + idCont + " 0 R >>\nendobj\n");

            offsets.add(out.size());
            escribir(out, idCont + " 0 obj\n<< /Length " + bytesFlujo.length + " >>\nstream\n");
            out.write(bytesFlujo);
            escribir(out, "\nendstream\nendobj\n");
        }

        // Objeto de la fuente (Helvetica con soporte de tildes WinAnsiEncoding)
        offsets.add(out.size());
        escribir(out, idFuente + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

        // Tabla de referencias (xref) y cierre del archivo PDF
        int posXref = out.size();
        int totalObjetos = offsets.size() + 1;
        escribir(out, "xref\n0 " + totalObjetos + "\n0000000000 65535 f \n");
        for (int offset : offsets) {
            escribir(out, String.format("%010d 00000 n \n", offset));
        }
        escribir(out, "trailer\n<< /Size " + totalObjetos + " /Root 1 0 R >>\nstartxref\n" + posXref + "\n%%EOF");

        return out.toByteArray();
    }

    private void escribir(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.ISO_8859_1));
    }
}
