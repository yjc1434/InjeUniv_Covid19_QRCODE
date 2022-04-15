package com.injeuniv.yjc.qrcode;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.webkit.WebSettingsCompat;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WebViewActivity extends AppCompatActivity {
    private boolean isDark;
    private LinearLayout ll;
    private LinearLayout.LayoutParams params;
    private boolean isFirst;

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        checkPermission(); //camera check

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ll = (LinearLayout) inflater.inflate(R.layout.activity_webview_loading, null);
        ll.setBackgroundColor(Color.parseColor("#FFFFFF"));

        isFirst = true;
        Charles.showProgressDialog(WebViewActivity.this,1);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        isDark = false;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            isDark = true;
            WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON);
            webView.setBackgroundColor(Color.parseColor("#080808"));
            ll.setBackgroundColor(Color.parseColor("#080808"));
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(!isFirst) {
                    Charles.showProgressDialog(WebViewActivity.this,1);
                }
                super.onPageStarted(view, url, favicon);
                params = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                addContentView(ll, params);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);//페이지 불러오는게 완료되면 아래의 자바스크립트를 실행함
                try {
                    if (isDark) {
                        view.loadUrl("javascript:$('body').css('backgroundColor', '#080808');" +
                                "$('.titleLabel').css('color', '#fff');" +
                                "$('.pageHead').hide();$('#ubtn동선확인_결과').hide();" +
                                "document.head.appendChild(document.createElement('style'));" +
                                "var styleSheets = document.styleSheets;" +
                                "styleSheets[styleSheets.length-1].addRule('.resultCircle.tCircle, .resultCircle.tCircle > div','border : 3px solid #545454;');" +
                                "styleSheets[styleSheets.length-1].addRule('.btnBox7 dd input', 'background-color:#002172; color:#fff;');" +
                                "styleSheets[styleSheets.length-1].addRule('.tCircle.cBG_Monday, .tCircle.cBG_Monday > div','background-color:#3c0046;');" +
                                "styleSheets[styleSheets.length-1].addRule('.tCircle.cBG_Tuesday, .tCircle.cBG_Tuesday > div','background-color:#05015f;');" +
                                "styleSheets[styleSheets.length-1].addRule('.tCircle.cBG_Wednesday, .tCircle.cBG_Wednesday > div','background-color:#004300;');" +
                                "styleSheets[styleSheets.length-1].addRule('.tCircle.cBG_Thursday, .tCircle.cBG_Thursday > div','background-color:#013879;');" +
                                "styleSheets[styleSheets.length-1].addRule('.tCircle.cBG_Friday, .tCircle.cBG_Friday > div','background-color:#7c0000;');" +
                                "styleSheets[styleSheets.length-1].addRule('.tCircle.cBG_Saturday, .tCircle.cBG_Saturday > div','background-color:#00342d;');" +
                                "styleSheets[styleSheets.length-1].addRule('.tCircle.cBG_Sunday, .tCircle.cBG_Sunday > div','background-color:#750013;');");
                    } else {
                        view.loadUrl("javascript:$('.pageHead').hide();$('#ubtn동선확인_결과').hide()");
                    }
                    if(isFirst)
                        isFirst = false;
                    Thread.sleep(1500);
                    Charles.cancelProgressDialog();
                    ((ViewManager) ll.getParent()).removeView(ll);
                } catch (Exception ex) {

                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            /*@TargetApi(Build.VERSION_CO   DES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }*/
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    request.grant(request.getResources());
                }
            }

            @Override
            public Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            }
        });

        webView.setHorizontalScrollBarEnabled(false);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setDefaultTextEncodingName("UTF-8");

        SharedPreferences sPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String viewstate = sPreferences.getString("__VIEWSTATE", null);
        String viewstategggenerator = sPreferences.getString("__VIEWSTATEGENERATOR", null);
        String eventvalidaton = sPreferences.getString("__EVENTVALIDATION", null);

        try {
            String postData = "__EVENTTARGET=" +
                    "&__EVENTARGUMENT=" +
                    "&__VIEWSTATE=" + URLEncoder.encode(viewstate, "UTF-8") +
                    "&__VIEWSTATEGENERATOR=" + URLEncoder.encode(viewstategggenerator, "UTF-8") +
                    "&__EVENTVALIDATION=" + URLEncoder.encode(eventvalidaton, "UTF-8") +
                    "&" + URLEncoder.encode("ubtnQR리더", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");

            webView.postUrl("https://ijis.inje.ac.kr/covid19/main.aspx", postData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    void checkPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermission == PackageManager.PERMISSION_GRANTED) { // 카메라 실행 로직
            logic();
        } else {
            requestPermission();
        }
    }

    void requestPermission() {
        String[] permissions = {Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permissions, 321);
    }

    void logic() {
       Toast.makeText(this, "카메라 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 카메라 실행 로직
                logic();
            } else {
                finish();
            }
        }
    }
}
