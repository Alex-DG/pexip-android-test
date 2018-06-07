package com.gearedapp.ajentapexip;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.pexip.android.wrapper.PexView;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_PERM = "PERM";
    private static final String TAG_INIT_PEX = "INIT_PEX";
    private static final String TAG_BTN = "ON_CLICK_BTN";
    private static final String TAG_FINISH = "FINISH_CALLBACK";

    // Permissions
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private RxPermissions rxPermissions;

    // Layouts
    private RelativeLayout layout;
    private PexView pexView;
    private WebView selfView;
    private RelativeLayout.LayoutParams pexLayoutParams;

    private Button disconnectBtn;
    private Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handlePermissions();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pexView != null) {
            pexView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pexView != null) {
            pexView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pexView != null) {
            pexView.destroy();
        }
    }

    private void handlePermissions() {
        if (rxPermissions == null) {
            rxPermissions = new RxPermissions(this);
        }

        rxPermissions.request(PERMISSIONS).subscribe(granted -> {
            if (granted) {
                Log.i(TAG_PERM, "All permissions granted");
                start();
            } else {
                Log.e(TAG_PERM, "At least one permission is not granted");
            }
        });
    }

    private void start() {
        if (pexView == null) {
            pexView = new PexView(this);
        }

        if (pexLayoutParams == null) {
            pexLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        disconnectBtn = findViewById(R.id.btn_disconnect);
        connectBtn = findViewById(R.id.btn_connect);

        disconnectBtn.setOnClickListener(onDisconnect);
        connectBtn.setOnClickListener(onConnect);

        layout = findViewById(R.id.pex_layout);
        layout.addView(pexView, pexLayoutParams);

        selfView = findViewById(R.id.pex_self_layout);
        pexView.setSelfView(selfView);

        initPexip();
    }

    private void initPexip() {
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
            public void runOnUI(String returnValue) {
                super.runOnUI(returnValue);
                Log.d(TAG_INIT_PEX, "runOnUI.... " + returnValue);
            }

            @Override
            public void callback(String args) {
                Log.d(TAG_INIT_PEX, "addPageLoadedCallback... " + args);
                // make a call
                pexView.evaluateFunction("makeCall", "pex-pool.vscene.net", "john_vmr", "Alex-french-droid");
            }

        });


        pexView.load();
    }

    private View.OnClickListener onDisconnect = ((View v) -> {
        Log.d(TAG_BTN, "Disconnect");
        if (pexView != null) {
            pexView.evaluateFunction("disconnect");
        }
    });

    private View.OnClickListener onConnect = ((View v) -> {
        Log.d(TAG_BTN, "Connect");
        if (pexView != null) {
            initPexip();
        }

    });
}
