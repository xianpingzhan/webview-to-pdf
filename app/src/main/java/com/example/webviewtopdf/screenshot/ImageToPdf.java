package com.example.webviewtopdf.screenshot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.webviewtopdf.screenshot.callback.PdfListener;
import com.example.webviewtopdf.screenshot.callback.ScreenshotListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ImageToPdf {
    private Screenshot screenshot;
    private Context context;
    private WebView webView;
    private LinearLayout box;
    private String webPath;
    private List<String> reportList;

    public ImageToPdf(Context context, View view, List<String> reportList) {
        this.context = context;
        this.reportList = reportList;
        this.box = (LinearLayout) view;
        initPermissions();
    }

    //调用此方法判断是否拥有权限
    private void initPermissions() {
        String[] needPermission = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        ArrayList<String> needRequestPermissions = new ArrayList<>();
        for (String s : needPermission) {
            if (ActivityCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissions.add(s);
            }
        }
        String[] permissions = new String[needRequestPermissions.size()];
        if (needRequestPermissions.size() != 0) {
            for (int i = 0; i < needRequestPermissions.size(); i++) {
                permissions[i] = needRequestPermissions.get(i);
            }
            ActivityCompat.requestPermissions((Activity) context, permissions, 999);
        } else {
            startPdf(reportList.remove(0));
        }
    }

    private final PdfListener pdfListener = new PdfListener() {
        @Override
        public void start() {

        }

        @Override
        public void success() {
            Toast.makeText(context, "报告生成成功", Toast.LENGTH_SHORT).show();
            if (reportList.size() > 0) {
                startPdf(reportList.remove(0));
            }
        }

        @Override
        public void fail() {

        }
    };

    private void startPdf(String path) {
        webPath = path;
        createWebView();
    }

    private void createWebView() {
        pdfListener.start();
        if (webView == null) {
            webView = new WebView(context);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDisplayZoomControls(false);
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    // 在页面加载完成后执行操作
                    // 在这里可以获取到当前页面的URL，并进行相应的处理
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            exportImage();
                        }
                    }, 1000);
                }
            });
            webView.setAlpha(0);
            box.addView(webView);

            ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            layoutParams.width = dm.widthPixels;
            layoutParams.height = dm.heightPixels;
            webView.setLayoutParams(layoutParams);
        }
        webView.scrollTo(0, 0);
        System.out.println(webPath);
        webView.loadUrl(webPath);
    }

    private void exportImage() {
        screenshot = new Screenshot.Builder(context)
                .setTarget(webView)
                .setScreenshotType(true)
                .setFilePath(new File(Environment.getExternalStorageDirectory(), "/zxp/1/aa" + getFileName(webPath) + ".jpg").getAbsolutePath())
                .setScreenshotListener(new ScreenshotListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap, boolean isLongScreenshot) {
                        Log.e("MainActivity", "onSuccess");
                        createPdf(bitmap);
                    }

                    @Override
                    public void onFail(int code, String errorInfo) {
                        Log.e("MainActivity", "onFail = " + errorInfo);
                    }

                    @Override
                    public void onPreStart() {
                        Log.e("MainActivity", "onPreStart");
                    }
                })
                .build();
        screenshot.start();
    }

    private void createPdf(Bitmap bitmap) {
        PdfDocument pdfDocument = new PdfDocument();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = (int) (1682f / 1190f * dm.widthPixels);
        int pageSize = webView.getContentHeight() / height;

        for (int i = 0; i < pageSize; i++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, i + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            canvas.drawBitmap(Bitmap.createBitmap(bitmap, 0, height * i, width, height), 0, 0, new Paint());
            //绘制
            webView.draw(canvas);
            pdfDocument.finishPage(page);
        }

        File path = new File(Environment.getExternalStorageDirectory(), "/zxp/1");
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path.getAbsolutePath(), "bb" + getFileName(webPath) + ".pdf");
        System.out.println(file.getAbsolutePath());
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            pdfListener.fail();
            e.printStackTrace();
        }
        pdfDocument.close();
        if (screenshot != null) {
            screenshot.destroy();
            screenshot = null;
        }
        if (reportList.size() <= 0) {
            box.removeView(webView);
            webView = null;
        }
        pdfListener.success();
    }

    private String getFileName(String page) {
        return page.substring((page.lastIndexOf('/') + 1));
    }
}
