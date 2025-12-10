package com.RafaelDiaz.ClubJudoColombia.servicio;

import com.RafaelDiaz.ClubJudoColombia.modelo.ConfiguracionSistema;
import com.RafaelDiaz.ClubJudoColombia.modelo.MovimientoCaja;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReciboService {

    private final ConfiguracionService configuracionService;

    public PdfReciboService(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    public byte[] generarReciboPdf(MovimientoCaja movimiento) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A6); // Tamaño pequeño tipo recibo
            PdfWriter.getInstance(document, out);
            document.open();

            ConfiguracionSistema config = configuracionService.obtenerConfiguracion();

            // 1. Encabezado
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph titulo = new Paragraph(config.getNombreOrganizacion(), fontBold);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("NIT/ID: 123456789", fontNormal)); // Dato quemado o de config
            document.add(new Paragraph("RECIBO DE CAJA N° " + movimiento.getId(), fontBold));
            document.add(new Paragraph("Fecha: " + movimiento.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal));

            document.add(Chunk.NEWLINE);

            // 2. Datos del Pago
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            agregarFilaTabla(table, "Pagado por:", movimiento.getJudoka() != null
                    ? movimiento.getJudoka().getUsuario().getNombre() + " " + movimiento.getJudoka().getUsuario().getApellido()
                    : "Anónimo / General");

            agregarFilaTabla(table, "Concepto:", movimiento.getConcepto().getNombre());
            agregarFilaTabla(table, "Método:", movimiento.getMetodoPago().getNombre());

            if (movimiento.getObservacion() != null && !movimiento.getObservacion().isEmpty()) {
                agregarFilaTabla(table, "Nota:", movimiento.getObservacion());
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // 3. Total
            Paragraph total = new Paragraph("TOTAL: $ " + movimiento.getMonto().toString(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // 4. Pie de página
            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Gracias por su apoyo al deporte.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }

    private void agregarFilaTabla(PdfPTable table, String label, String valor) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c2);
    }
}