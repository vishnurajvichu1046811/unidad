package com.utracx.reports;

import android.content.Context;

import androidx.annotation.NonNull;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.util.List;

public final class PDFUtility {
    private static final String TAG = PDFUtility.class.getSimpleName();
    private static Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static Font FONT_SUBTITLE = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    private static Font FONT_CELL = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static Font FONT_COLUMN = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL);

    public interface OnDocumentClose {
        void onPDFDocumentClose(File file);
    }

    static void createPdf(@NonNull Context mContext, OnDocumentClose mCallback, List<String[]> items, @NonNull String filePath, boolean isPortrait) {

    }

    private static void addEmptyLine(Document document, int number) {

    }

    private static void setMetaData(Document document) {

    }

    private static void addHeader(Context mContext, Document document) {

    }

    /*private static PdfPTable createDataTable(List<String[]> dataTable) {

    }

    private static PdfPTable createSignBox() throws DocumentException {

    }

    private static Image getImage(byte[] imageByte, boolean isTintingRequired) {

    }

    private static Image getBarcodeImage(PdfWriter pdfWriter, String barcodeText) {

    }*/
}
