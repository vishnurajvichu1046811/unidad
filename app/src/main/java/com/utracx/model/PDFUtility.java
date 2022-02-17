package com.utracx.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.utracx.reports.PageNumeration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public final class PDFUtility {
    private static final String TAG = PDFUtility.class.getSimpleName();
    private static Font FONT_TITLE = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font FONT_SUBTITLE = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

    private static Font FONT_CELL = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static Font FONT_COLUMN = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL);
    private static String registrationNumber;
    private static String reportDate;
    private static String totalDistance;

    public interface OnDocumentClose {
        void onPDFDocumentClose(File file);
    }

    public static void createPdf(@NonNull Context mContext, OnDocumentClose mCallback, String regNumber, String date, String tDistance, List<String[]> items, @NonNull String filePath, boolean isPortrait) throws Exception {
        registrationNumber = regNumber;
        reportDate = date;
        totalDistance = tDistance;
        if (filePath.equals("")) {
            throw new NullPointerException("PDF File Name can't be null or blank. PDF File can't be created");
        }

        File file = new File(filePath);

        if (file.exists()) {
            file.delete();
            Thread.sleep(50);
        }

        Document document = new Document();
        document.setMargins(24f, 24f, 32f, 32f);
        document.setPageSize(PageSize.A4.rotate());

        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        pdfWriter.setFullCompression();
        pdfWriter.setPageEvent(new PageNumeration());

        document.open();

        setMetaData(document);

        addHeader(mContext, document);
        document.add(createSubtitle());
        addEmptyLine(document, 1);
        document.add(createDataTable(items));


        document.close();

        try {
            pdfWriter.close();
        } catch (Exception ex) {
            Log.e(TAG, "Error While Closing pdfWriter : " + ex.toString());
        }

        if (mCallback != null) {
            mCallback.onPDFDocumentClose(file);
        }
    }

    private static void addEmptyLine(Document document, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            document.add(new Paragraph(" "));
        }
    }

    private static void setMetaData(Document document) {
        document.addCreationDate();
        //document.add(new Meta("",""));
        document.addAuthor("Priyan");
        document.addCreator("Infinicom");
        document.addHeader("Utrack", "Unidad");
    }

    private static void addHeader(Context mContext, Document document) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{7});
        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell;

        {
            /*MIDDLE TEXT*/
            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(8f);
            cell.setUseAscender(true);

            Paragraph temp = new Paragraph("UTRACK ADMIN CONSOLE", FONT_TITLE);
            temp.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(temp);
            table.addCell(cell);
        }
        document.add(table);
    }

    private static PdfPTable createDataTable(List<String[]> dataTable) throws DocumentException {
        PdfPTable table1 = new PdfPTable(8);
        table1.setWidthPercentage(100);
        table1.setWidths(new float[]{0.1f, .3f, .3f, .3f, .3f, .3f, .3f, .3f});
        table1.setHeaderRows(1);
        table1.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table1.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell;
        {
            table1.addCell(getHeadingCell("SL", Boolean.FALSE));
            table1.addCell(getHeadingCell("Start Time", Boolean.FALSE));
            table1.addCell(getHeadingCell("Start Point", Boolean.FALSE));
            table1.addCell(getHeadingCell("End Time", Boolean.FALSE));
            table1.addCell(getHeadingCell("End Point", Boolean.FALSE));
            table1.addCell(getHeadingCell("Duration", Boolean.FALSE));
            table1.addCell(getHeadingCell("Average Speed", Boolean.FALSE));
            table1.addCell(getHeadingCell("Distance", Boolean.FALSE));
        }

        float top_bottom_Padding = 8f;
        float left_right_Padding = 4f;
        boolean alternate = false;

        BaseColor lt_gray = new BaseColor(221, 221, 221); //#DDDDDD
        BaseColor cell_color;

        int size = dataTable.size();

        for (int i = 0; i < size; i++) {
            cell_color = alternate ? lt_gray : BaseColor.WHITE;
            String[] temp = dataTable.get(i);
            cell = new PdfPCell(new Phrase(String.valueOf(i + 1), FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp[0], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp[1], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp[2], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp[3], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp[4], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp[5], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase(temp[6], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            /*cell = new PdfPCell(new Phrase(temp[7], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);*/

            alternate = !alternate;
        }

        //total distance cell
        {
            table1.addCell(getHeadingCell("", Boolean.TRUE));
            table1.addCell(getHeadingCell("", Boolean.TRUE));
            table1.addCell(getHeadingCell("", Boolean.TRUE));
            table1.addCell(getHeadingCell("", Boolean.TRUE));
            table1.addCell(getHeadingCell("", Boolean.TRUE));
            table1.addCell(getHeadingCell("Total Distance", Boolean.TRUE));
            table1.addCell(getHeadingCell(totalDistance, Boolean.TRUE));
        }
        return table1;
    }

    private static PdfPCell getHeadingCell(String columnName, boolean isNoBoarder) {
        BaseColor blueColorCode = new BaseColor(152, 137, 249);
        PdfPCell cell = new PdfPCell(new Phrase(columnName, FONT_COLUMN));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setNoWrap(false);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        if (isNoBoarder) {
            cell.setBorder(PdfPCell.NO_BORDER);
        } else {
            cell.setBackgroundColor(blueColorCode);
        }

        return cell;
    }

    private static PdfPTable createSubtitle() throws DocumentException {
        PdfPTable outerTable = new PdfPTable(1);
        outerTable.setWidthPercentage(100);
        outerTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

        PdfPTable innerTable = new PdfPTable(2);
        {
            innerTable.setWidthPercentage(100);
            innerTable.setWidths(new float[]{1, 1});
            innerTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

            PdfPCell cell = null;

            cell = new PdfPCell();
            Paragraph temp = new Paragraph(new Phrase(String.format("Vehicle Registration: %s", registrationNumber, FONT_SUBTITLE)));
            temp.setPaddingTop(4f);
            temp.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(temp);
            cell.setBorder(PdfPCell.NO_BORDER);
            innerTable.addCell(cell);

            //ROW-4 : Content Right Aligned
            cell = new PdfPCell(new Phrase(String.format("Date: %s", reportDate.replaceAll(" ", ""), FONT_SUBTITLE)));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(4f);
            innerTable.addCell(cell);
        }

        PdfPCell signRow = new PdfPCell(innerTable);
        signRow.setHorizontalAlignment(Element.ALIGN_LEFT);
        signRow.setBorder(PdfPCell.NO_BORDER);
        signRow.setPadding(4f);

        outerTable.addCell(signRow);

        return outerTable;
    }

    private static Image getImage(byte[] imageByte, boolean isTintingRequired) throws Exception {
        Paint paint = new Paint();
        if (isTintingRequired) {
            paint.setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN));
        }
        Bitmap input = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(input, 0, 0, paint);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        output.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image image = Image.getInstance(stream.toByteArray());
        image.setWidthPercentage(80);
        return image;
    }

    private static Image getBarcodeImage(PdfWriter pdfWriter, String barcodeText) {
        Barcode128 barcode = new Barcode128();
        //barcode.setBaseline(-1); //BARCODE TEXT ABOVE
        barcode.setFont(null);
        barcode.setCode(barcodeText);
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setTextAlignment(Element.ALIGN_BASELINE);
        return barcode.createImageWithBarcode(pdfWriter.getDirectContent(), BaseColor.BLACK, null);
    }
}