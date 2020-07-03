package com.createsapp.printpdforimage.Utils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

public class PDFUtils {
    public static void addNewItem(Document document, String text, int align, Font font) throws DocumentException {
        Chunk chunk = new Chunk(text, font);
        Paragraph p = new Paragraph(chunk);
        p.setAlignment(align);
        document.add(p);
    }

    public static void addLinesSpace(Document document) throws DocumentException {
        document.add(new Paragraph(""));
    }

    public static void addNewItemWithLeftAndRight(Document document, String leftText, String rightText,
                                                  Font leftFont, Font rightFont) throws DocumentException {
        Chunk leftTextChunk = new Chunk(leftText, leftFont);
        Chunk rightTextChunk = new Chunk(rightText, leftFont);

        Paragraph p = new Paragraph(leftTextChunk);
        p.add(new Chunk(new VerticalPositionMark()));
        p.add(rightTextChunk);
        document.add(p);

    }

    public static void addLinesSeperator(Document document) throws DocumentException {
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(new BaseColor(0,0,0,68));
        addLinesSpace(document);
        document.add(new Chunk(lineSeparator));
        addLinesSpace(document);
    }
}
