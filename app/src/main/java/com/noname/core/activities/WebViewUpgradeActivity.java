package com.noname.core.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.noname.core.utils.WebViewUpgradeUtils;

public class WebViewUpgradeActivity extends Activity {
    private static final String TAG = "WebViewUpgradeActivity";

    protected void ActivityOnCreate(Bundle extras) {

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
