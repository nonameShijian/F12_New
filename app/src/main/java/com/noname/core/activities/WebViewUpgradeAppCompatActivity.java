package com.noname.core.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.noname.core.utils.WebViewUpgradeUtils;

public class WebViewUpgradeAppCompatActivity extends AppCompatActivity {
    private static final String TAG = "WebViewUpgradeAppCompatActivity";

    protected void ActivityOnCreate(Bundle extras) {
        // 很难想象最新的控制台要多新的特性
        // 还需要注入一个core.js
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("onCreate", String.valueOf(savedInstanceState));
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        WebViewUpgradeUtils webViewUpgradeUtils = new WebViewUpgradeUtils();
        webViewUpgradeUtils.upgrade(this, new Thread(() -> {
            ActivityOnCreate(extras);
        }));
    }
}
