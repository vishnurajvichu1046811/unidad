package com.utracx.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.utracx.BuildConfig;
import com.utracx.R;

import java.io.File;

import static com.utracx.util.helper.NavigationHelper.KEY_NAVIGATION_DATA_BUNDLE;

public class ReportViewer extends BaseRefreshActivity {
    public static final String REPORT_DOCUMENT_PATH = "report_data_path";
    File file;
    private PDFView pdfView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports_viewer);
        setAutoRefresh(false);
        initView();
        initData();

    }

    private void initView() {
        pdfView = (PDFView) findViewById(R.id.pdfView);
        findViewById(R.id.backView).setOnClickListener(backView -> finish());
        findViewById(R.id.share).setOnClickListener(v -> {
            if (file != null) {
                shareFile(file);
            }
        });
    }

    private void loadPdfView(File file) {
        if (file == null)
            return;

        pdfView.recycle();
        pdfView.fromFile(file)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .swipeHorizontal(true)
                .enableSwipe(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(5) // in dp
                .load();
    }

    private void initData() {
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            if (dataBundle.containsKey(KEY_NAVIGATION_DATA_BUNDLE)
                    && dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE) != null) {
                Bundle innerBundle = dataBundle.getBundle(KEY_NAVIGATION_DATA_BUNDLE);
                if (innerBundle != null && innerBundle.containsKey(REPORT_DOCUMENT_PATH)) {
                    String scanDocumentPath = innerBundle.getString(REPORT_DOCUMENT_PATH);
                    file = new File(scanDocumentPath);
                    loadPdfView(file);
                }
            }
        }
    }

    private void shareFile(File file) {
        if (file != null) {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            if (file.exists()) {
                intentShareFile.setType("application/pdf");

                Uri fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri);

                intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                        "Share file");
                intentShareFile.putExtra(Intent.EXTRA_TEXT, "Report");

                startActivity(Intent.createChooser(intentShareFile, "Share report"));
            }
        }
    }
}
