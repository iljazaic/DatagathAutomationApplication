package com.example.datagath.service;

import java.io.ByteArrayOutputStream;

import com.example.datagath.model.CollectionTable;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfWriter;

public class FileGenerationService {

    public FileGenerationService() {
    }

public static byte[] generatePDFReportOnCollectionTable(CollectionTable collectionTable)
        throws DocumentException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document();
    PdfWriter.getInstance(document, baos);

    document.open();
    Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
    Chunk chunk = new Chunk("Collection Table report for %s".formatted("TABLENAMEGOESHERE"), font);
    document.add(chunk);
    document.close();

    return baos.toByteArray();
}
}
