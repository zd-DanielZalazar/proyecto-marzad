package com.sga.marzad.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.Desktop;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class CertificadoRegularService {

    public static void generarCertificado(String nombre, String apellido, String dni) throws IOException, WriterException {
        // A4 Landscape
        PDRectangle pageSize = PDRectangle.A4;
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(pageSize);
        document.addPage(page);

        // Datos y fecha
        LocalDate hoy = LocalDate.now();
        String dia = String.valueOf(hoy.getDayOfMonth());
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String anio = String.valueOf(hoy.getYear());

        // Texto principal
        String texto =
                "Certificado de Alumno Regular\n\n" +
                        "Por medio de la presente, se deja constancia de que " + nombre + " " + apellido +
                        ", con DNI " + dni + ", es alumno/a regular de la carrera Analista de Sistemas en el Instituto Superior de Estudios Técnicos.\n\n" +
                        "A la fecha, el/la mencionado/a se encuentra cursando el tercer/último año de dicha carrera.\n\n" +
                        "El presente certificado se extiende a pedido del/de la interesado/a y para ser presentado ante quien corresponda, " +
                        "en la ciudad de Mar del Plata, a los " + dia + " días del mes de " + mes + " del año " + anio + ".";

        PDType1Font font = PDType1Font.HELVETICA;
        float fontSizeTitulo = 28f;
        float fontSizeTexto = 15f;

        // Crear QR como imagen
        BufferedImage qrImg = generarQR("VÁLIDO", 130, 130);
        File qrFile = File.createTempFile("qr_temp", ".png");
        ImageIO.write(qrImg, "PNG", qrFile);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Título centrado
            contentStream.setFont(font, fontSizeTitulo);
            float titleWidth = font.getStringWidth("Certificado de Alumno Regular") / 1000 * fontSizeTitulo;
            float titleX = (pageSize.getWidth() - titleWidth) / 2;
            float titleY = pageSize.getHeight() - 70;
            contentStream.beginText();
            contentStream.newLineAtOffset(titleX, titleY);
            contentStream.showText("Certificado de Alumno Regular");
            contentStream.endText();

            // Texto principal (con auto-wrap)
            contentStream.setFont(font, fontSizeTexto);
            float textX = 90;
            float textY = pageSize.getHeight() - 120;
            float maxLineWidth = pageSize.getWidth() - 2 * textX;

            List<String> lineas = wrapText(texto, font, fontSizeTexto, maxLineWidth);
            for (String linea : lineas) {
                contentStream.beginText();
                contentStream.newLineAtOffset(textX, textY);
                contentStream.showText(linea);
                contentStream.endText();
                textY -= 24;
            }

            // QR centrado mucho más abajo
            PDImageXObject qrImage = PDImageXObject.createFromFile(qrFile.getAbsolutePath(), document);
            float qrWidth = 130, qrHeight = 130;
            float qrX = (pageSize.getWidth() - qrWidth) / 2;
            float qrY = 60;
            contentStream.drawImage(qrImage, qrX, qrY, qrWidth, qrHeight);

            // Texto debajo del QR
            contentStream.setFont(font, 13f);
            float footerTextWidth = font.getStringWidth("Escanee para verificar autenticidad") / 1000 * 13f;
            float footerX = (pageSize.getWidth() - footerTextWidth) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(footerX, qrY - 24);
            contentStream.showText("Escanee para verificar autenticidad");
            contentStream.endText();
        }

        // Guardar PDF en Descargas/Downloads — busca nombre libre
        String userHome = System.getProperty("user.home");
        File downloads = new File(userHome, "Downloads");
        if (!downloads.exists()) {
            downloads = new File(userHome, "Descargas");
            if (!downloads.exists()) downloads.mkdirs();
        }
        File outFile = getUniqueFile(downloads, "certificado_regular", ".pdf");

        document.save(outFile);
        document.close();
        qrFile.delete();
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(outFile);
        }
    }

    // --- MÉTODO DE WRAP PARA PDFBox ---
    private static List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String originalLine : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : originalLine.split(" ")) {
                String testLine = line.length() == 0 ? word : line + " " + word;
                float size = font.getStringWidth(testLine) / 1000 * fontSize;
                if (size > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    if (line.length() > 0) line.append(" ");
                    line.append(word);
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }

    // --- QR GENERATOR ---
    private static BufferedImage generarQR(String texto, int ancho, int alto) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, ancho, alto);
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                imagen.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return imagen;
    }

    // --- Método para obtener un nombre de archivo disponible ---
    private static File getUniqueFile(File directory, String baseName, String extension) {
        File file = new File(directory, baseName + extension);
        int count = 1;
        while (file.exists()) {
            file = new File(directory, baseName + "(" + count + ")" + extension);
            count++;
        }
        return file;
    }
}
