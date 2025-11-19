package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.GrupoEntrenamiento;
import com.RafaelDiaz.ClubJudoColombia.modelo.Judoka;
import com.RafaelDiaz.ClubJudoColombia.servicio.TraduccionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Servicio centralizado para generación de reportes en PDF.
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Generar PDFs de grupos con judokas</li>
 *   <li>Formato tabular profesional</li>
 *   <li>Gestión de recursos PDFBox</li>
 *   <li>Logging y manejo de errores</li>
 * </ul>
 *
 * <p><b>Uso:</b>
 * <pre>
 * byte[] pdf = pdfExportService.generarPdfGrupo(grupo, columnas);
 * // Descargar con Vaadin DownloadHandler
 * </pre>
 *
 * @author RafaelDiaz
 * @version 1.0
 * @since 2025-11-19
 */
@Service
public class PdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);
    private final TraduccionService traduccionService;

    public PdfExportService(TraduccionService traduccionService) {
        this.traduccionService = traduccionService;
    }

    /**
     * Genera un PDF con la lista de judokas de un grupo.
     *
     * @param grupo Grupo de entrenamiento
     * @param columnasSeleccionadas Columnas a incluir (ej. "Nombre", "Edad", "Sexo")
     * @return Array de bytes del PDF generado
     * @throws RuntimeException si hay error en la generación
     */
    public byte[] generarPdfGrupo(GrupoEntrenamiento grupo, Set<String> columnasSeleccionadas) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no puede ser null");
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                dibujarEncabezado(contentStream, document, grupo);
                dibujarTabla(contentStream, document, grupo, columnasSeleccionadas);
            }

            document.save(baos);
            byte[] pdfBytes = baos.toByteArray();

            logger.info("PDF generado para grupo '{}': {} bytes", grupo.getNombre(), pdfBytes.length);
            return pdfBytes;

        } catch (IOException e) {
            logger.error("Error generando PDF para grupo {}", grupo.getNombre(), e);
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    /**
     * Dibuja el encabezado del documento.
     */
    private void dibujarEncabezado(PDPageContentStream contentStream, PDDocument document,
                                   GrupoEntrenamiento grupo) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("Club Judo Colombia - Lista de Judokas");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
        contentStream.newLineAtOffset(50, 720);
        contentStream.showText("Grupo: " + grupo.getNombre());
        contentStream.endText();
    }

    /**
     * Dibuja la tabla de judokas.
     */
    private void dibujarTabla(PDPageContentStream contentStream, PDDocument document,
                              GrupoEntrenamiento grupo, Set<String> columnas) throws IOException {
        float yStart = 700;
        float tableWidth = 500;
        float yPosition = yStart;
        float margin = 50;
        float rowHeight = 20;
        float colWidth = tableWidth / columnas.size();

        // Dibujar encabezado de tabla
        contentStream.setLineWidth(1f);
        contentStream.moveTo(margin, yStart);
        contentStream.lineTo(margin + tableWidth, yStart);
        contentStream.stroke();

        float xPosition = margin;
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.newLineAtOffset(xPosition, yPosition - 15);

        for (String col : columnas) {
            contentStream.showText(truncarTexto(col, 30));
            contentStream.newLineAtOffset(colWidth, 0);
        }
        contentStream.endText();
        yPosition -= rowHeight;
        contentStream.moveTo(margin, yPosition);
        contentStream.lineTo(margin + tableWidth, yPosition);
        contentStream.stroke();

        // Filas de datos
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        for (Judoka j : grupo.getJudokas()) {
            if (yPosition < 100) {
                // Nueva página si no hay espacio
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream.close();
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = 750;
            }

            xPosition = margin;
            contentStream.beginText();
            contentStream.newLineAtOffset(xPosition, yPosition - 15);

            Map<String, String> data = prepararDatosJudoka(j);
            for (String col : columnas) {
                String value = data.getOrDefault(col, "");
                contentStream.showText(truncarTexto(value, 30));
                contentStream.newLineAtOffset(colWidth, 0);
            }
            contentStream.endText();
            yPosition -= rowHeight;
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(margin + tableWidth, yPosition);
            contentStream.stroke();
        }
    }

    /**
     * Prepara los datos de un judoka según columnas disponibles.
     */
    private Map<String, String> prepararDatosJudoka(Judoka j) {
        Map<String, String> data = new HashMap<>();
        data.put("Nombre Completo", j.getUsuario().getNombre() + " " + j.getUsuario().getApellido());
        data.put("Edad", String.valueOf(j.getEdad()));
        data.put("Sexo", j.getSexo() != null ? j.getSexo().toString() : "");
        data.put("Grado", j.getGrado() != null ? j.getGrado().toString() : "");
        data.put("Peso", j.getPeso() != null ? j.getPeso().toString() : "");
        data.put("Estatura", j.getEstatura() != null ? j.getEstatura().toString() : "");
        data.put("Acudiente", j.esMenorDeEdad() ?
                (j.getNombreAcudiente() + " (" + j.getTelefonoAcudiente() + ")") : "");
        return data;
    }

    /**
     * Trunca texto largo para evitar desbordamiento en celdas.
     */
    private String truncarTexto(String texto, int maxLength) {
        if (texto == null) return "";
        return texto.length() > maxLength ? texto.substring(0, maxLength - 3) + "..." : texto;
    }
}