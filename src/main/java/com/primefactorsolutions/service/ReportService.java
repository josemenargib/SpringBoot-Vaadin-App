package com.primefactorsolutions.service;

import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.primefactorsolutions.repositories.HoursWorkedRepository;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Service
public class ReportService {
    private final HoursWorkedRepository hoursWorkedRepository;

    public ReportService(final HoursWorkedRepository hoursWorkedRepository) {
        this.hoursWorkedRepository = hoursWorkedRepository;
    }

    // Este método ahora solo crea el archivo Excel a partir de los datos que recibe.
    public byte[] writeAsExcel(final String reportName, final List<String> headers,
                               final List<Map<String, Object>> data, final String selectedTeam,
                               final int weekNumber, final int currentYear)
            throws IOException {
        return createExcelFile(reportName, headers, data, selectedTeam, weekNumber, currentYear);
    }

    private byte[] createExcelFile(final String reportName, final List<String> headers,
                                   final List<Map<String, Object>> data, final String selectedTeam,
                                   final int weekNumber, final int currentYear)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(reportName);

            // Crear encabezados
            // Crear una fila para el rótulo "Reporte por equipo"
            Row titleRow = sheet.createRow(0);  // Fila 0 para el rótulo
            Cell titleCell = titleRow.createCell(0);

            // Concatenar el nombre del equipo al rótulo
            String titleText = "Informe: " + weekNumber + "/" + currentYear;
            titleCell.setCellValue(titleText);

            // Estilo del rótulo
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14); // Tamaño de la fuente
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Fusionar celdas para el rótulo
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1)); // Ajusta el rango de celdas

            // Crear filas adicionales con la información solicitada
            Row asuntoRow = sheet.createRow(1); // Fila 1: Asunto
            asuntoRow.createCell(0).setCellValue("Asunto: Informe semanal de horas trabajadas");
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.size() - 1));

            Row semanaRow = sheet.createRow(2); // Fila 2: Semana
            semanaRow.createCell(0).setCellValue("Semana:  " + weekNumber); // Puedes insertar una fecha real aquí
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.size() - 1));

            Row horasCumplirRow = sheet.createRow(3); // Fila 3: Horas a cumplir
            horasCumplirRow.createCell(0).setCellValue("Horas a cumplir: 40 horas"); // Puedes agregar las horas reales
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, headers.size() - 1));

            Row teamLeadRow = sheet.createRow(4); // Fila 4: Team Lead
            teamLeadRow.createCell(0).setCellValue("Team Lead: "); // Solo texto
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, headers.size() - 1));

            // Crear encabezados (fila 5)
            Row headerRow = sheet.createRow(5); // Los encabezados empiezan en la fila 5
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Crear filas de datos (a partir de la fila 6)
            for (int i = 0; i < data.size(); i++) {
                Row dataRow = sheet.createRow(i + 6);  // Los datos empiezan después de la fila de encabezados
                Map<String, Object> rowData = data.get(i);
                int cellIndex = 0;
                for (String key : headers) {
                    Cell cell = dataRow.createCell(cellIndex++);
                    Object value = rowData.get(key);
                    if (value != null) {
                        if (value instanceof String) {
                            cell.setCellValue((String) value);
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        }
                    } else {
                        cell.setCellValue(""); // Manejo de valores nulos
                    }
                }
            }

            workbook.write(os);
            return os.toByteArray();
        } catch (IOException e) {
            System.err.println("Error al generar el archivo Excel: " + e.getMessage());
            throw e; // Propagar la excepción después de registrarla
        }
    }

    @SneakyThrows
    public byte[] writeAsPdf(final String reportName, final Object model) {
        try (var os = new ByteArrayOutputStream()) {
            writeAsPdf(reportName, model, os);

            return os.toByteArray();
        }
    }

    @SneakyThrows
    public void writeAsPdf(final String reportName, final Object model, final OutputStream out) {
        var in = getTemplate(reportName);
        final Configuration cfg = getConfiguration();
        final Reader reader = new InputStreamReader(in);
        final Template temp = new Template(reportName, reader, cfg);

        var wrapper = new DefaultObjectWrapper(Configuration.VERSION_2_3_32);
        ByteArrayOutputStream oo = new ByteArrayOutputStream();
        Writer outTemplate = new OutputStreamWriter(oo);

        temp.process(wrapper.wrap(model), outTemplate);

        var builder = new PdfRendererBuilder();
        builder.usePDDocument(new PDDocument(MemoryUsageSetting.setupMixed(1000000)));
        builder.withHtmlContent(oo.toString(StandardCharsets.UTF_8), "/test");
        builder.toStream(out);

        try (PdfBoxRenderer pdfBoxRenderer = builder.buildPdfRenderer()) {
            pdfBoxRenderer.layout();
            pdfBoxRenderer.createPDF();
        }
    }

    public static InputStream getTemplate(final String reportName) {
        return ReportService.class.getResourceAsStream(String.format("/reports/%s.html", reportName));
    }


    @NotNull
    private static Configuration getConfiguration() {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        return cfg;
    }
}