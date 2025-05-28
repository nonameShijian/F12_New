package io.alcatraz.f12.activities;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.webkit.WebViewAssetLoader;

import org.apache.cordova.CordovaDialogsHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.alcatraz.f12.R;
import io.alcatraz.f12.extended.CompatWithPipeActivity;

public class InDebuggerActivity extends CompatWithPipeActivity {
    public static final String KEY_DEBUGGER_URL = "key_alc_debugger_url";

    Toolbar toolbar;
    WebView window_webview;

    LinearLayout in_debugger_load_layer;
    ProgressBar in_deugger_progress;

    LinearLayout err_layer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_debugger);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        initViews();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void findViews() {
        toolbar = findViewById(R.id.in_debugger_toolbar);
        window_webview = findViewById(R.id.in_debugger_webview);
        in_debugger_load_layer = findViewById(R.id.in_debugger_load_layer);
        in_deugger_progress = findViewById(R.id.in_debugger_load_progress);
        err_layer = findViewById(R.id.in_debugger_err_layer);

        AssetManager assetManager = getAssets();

        // 初始化WebViewAssetLoader
        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .setDomain("localhost")
                .setHttpAllowed(true)
                .addPathHandler("/", path -> {
                    try {
                        if (path.isEmpty()) {
                            path = "index.html";
                        }
                        if ("/json".equals(path) || "/json/".equals(path)) {
                            JSONObject data = new JSONObject();
                            JSONArray targets = new JSONArray();
                            data.put("targets", targets);
                            // 将JSONObject转换为字符串
                            String jsonString = data.toString();
                            // 将字符串转换为字节数组
                            byte[] byteArray = jsonString.getBytes(StandardCharsets.UTF_8);
                            // 使用字节数组创建InputStream
                            InputStream inputStream = new ByteArrayInputStream(byteArray);
                            return new WebResourceResponse("application/json", null, inputStream);
                        }
                        InputStream is = assetManager.open("www/" + path, AssetManager.ACCESS_STREAMING);
                        String mimeType = "text/html";
                        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
                        if (extension != null) {
                            if (path.endsWith(".js") || path.endsWith(".mjs")) {
                                // Make sure JS files get the proper mimetype to support ES modules
                                mimeType = "application/javascript";
                            } else if (path.endsWith(".wasm")) {
                                mimeType = "application/wasm";
                            } else {
                                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            }
                        }
                        return new WebResourceResponse(mimeType, null, is);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .build();

        CordovaDialogsHelper dialogsHelper = new CordovaDialogsHelper(this);

        WebChromeClient wcc = new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                dialogsHelper.showAlert(message, new CordovaDialogsHelper.Result() {
                    @Override public void gotResult(boolean success, String value) {
                        if (success) {
                            result.confirm();
                        } else {
                            result.cancel();
                        }
                    }
                });
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                dialogsHelper.showConfirm(message, new CordovaDialogsHelper.Result() {
                    @Override
                    public void gotResult(boolean success, String value) {
                        if (success) {
                            result.confirm();
                        } else {
                            result.cancel();
                        }
                    }
                });
                return true;
            }
            @Override
            public boolean onJsPrompt(WebView view, String origin, String message, String defaultValue, final JsPromptResult result) {
                dialogsHelper.showPrompt(message, defaultValue, new CordovaDialogsHelper.Result() {
                    @Override
                    public void gotResult(boolean success, String value) {
                        if (success) {
                            result.confirm(value);
                        } else {
                            result.cancel();
                        }
                    }
                });
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                in_deugger_progress.setProgress(newProgress);
                if (newProgress == 100) {
                    in_debugger_load_layer.setVisibility(View.GONE);
                }
            }
        };

        WebViewClient wvc = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 本地页面就不判断这个了

//                Log.e("onReceivedError", String.valueOf(error.getErrorCode()));
//                view.loadUrl("about:blank");
//                err_layer.setVisibility(View.VISIBLE);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        };

        WebView.setWebContentsDebuggingEnabled(false);

        window_webview.getSettings().setJavaScriptEnabled(true);
        window_webview.getSettings().setSupportZoom(true);
        window_webview.getSettings().setDisplayZoomControls(false);
        window_webview.getSettings().setBuiltInZoomControls(true);
        window_webview.setWebChromeClient(wcc);
        window_webview.setWebViewClient(wvc);

        window_webview.getSettings().setUseWideViewPort(true);
        window_webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        window_webview.getSettings().setDomStorageEnabled(true);
        window_webview.getSettings().setDatabaseEnabled(true);

        window_webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        window_webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }

    private void initViews() {
        findViews();
        setSupportActionBar(toolbar);
        String url = getIntent().getStringExtra(KEY_DEBUGGER_URL);
        window_webview.loadUrl(url);
    }
}
