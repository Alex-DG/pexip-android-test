package com.gearedapp.ajentapexip;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.pexip.android.wrapper.PexView;
import com.tbruyelle.rxpermissions2.RxPermissions;


public class MainActivity extends AppCompatActivity {

    private static final String TAG_PERM = "PERM";
    private static final String TAG_INIT_PEX = "INIT_PEX";

    // Permissions
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private RxPermissions rxPermissions; // where this is an Activity instance

    // Layouts
    private RelativeLayout layout;
    private PexView pexView;
    private WebView selfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handlePermissions();
    }

    private void handlePermissions() {
        if (rxPermissions == null) {
            rxPermissions = new RxPermissions(this);
        }

        rxPermissions.request(PERMISSIONS).subscribe(granted -> {
            if (granted) {
                Log.i(TAG_PERM, "All permissions granted");
                initPexip();
            } else {
                Log.e(TAG_PERM, "At least one permission is not granted");
            }
        });
    }

    private void initPexip() {
        if (pexView == null) {
            pexView = new PexView(this);
        }

        layout = findViewById(R.id.pex_layout);
        layout.addView(pexView);

        selfView = findViewById(R.id.pex_self_layout);
        pexView.setSelfView(selfView);

        pexView.setEvent("onSetup", pexView.new PexEvent() {
            @Override
            public void onEvent(String[] strings) {
                Log.i(TAG_INIT_PEX, "onSetup...");
                pexView.setSelfViewVideo(strings[0]);
                pexView.evaluateFunction("connect");
            }
        });

        pexView.setEvent("onConnect", pexView.new PexEvent() {
            @Override
            public void onEvent(String[] strings) {
                Log.i(TAG_INIT_PEX, "onConnect...");
                if (strings.length > 0 && strings[0] != null) {
                    pexView.setVideo(strings[0]);
                }

            }
        });

        pexView.addPageLoadedCallback(pexView.new PexCallback() {
            @Override
            public void callback(String args) {
                Log.d(TAG_INIT_PEX, "addPageLoadedCallback...");
                // make a call
                pexView.evaluateFunction("makeCall", "pex-pool.vscene.net", "john_vmr", "Alex-droid");
            }
        });

        pexView.load();
    }
}
