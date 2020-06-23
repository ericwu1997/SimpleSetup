package com.example.simplesetup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BarcodeActivity extends AppCompatActivity {

    LinearLayout logContainer;
    int count;
    int MAX_LOG_COUNT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Button btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });

        Button btn_prev = findViewById(R.id.btn_prev);
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        logContainer = findViewById(R.id.log_container);

        registerReceiver(new BroadcastReceiver() {
             @Override
             public void onReceive(Context c, Intent intent) {
                 String action  = intent.getAction() ;
                 if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
                     SupplicantState supl_state=((SupplicantState)intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE));
                     switch(supl_state){
                         case ASSOCIATED:
                             writeLog("ASSOCIATED");
                             break;
                         case ASSOCIATING:
                             writeLog("ASSOCIATING");
                             break;
                         case AUTHENTICATING:
                             writeLog("Authenticating...");
                             break;
                         case COMPLETED:
                             writeLog("Connected");
                             Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://macsX:8889"));
                             startActivity(browserIntent);
                             break;
                         case DISCONNECTED:
                             writeLog("Disconnected");
                             break;
                         case DORMANT:
                             writeLog("DORMANT");
                             break;
                         case FOUR_WAY_HANDSHAKE:
                             writeLog("FOUR_WAY_HANDSHAKE");
                             break;
                         case GROUP_HANDSHAKE:
                             writeLog("GROUP_HANDSHAKE");
                             break;
                         case INACTIVE:
                             writeLog("INACTIVE");
                             break;
                         case INTERFACE_DISABLED:
                             writeLog("INTERFACE_DISABLED");
                             break;
                         case INVALID:
                             writeLog("INVALID");
                             break;
//                         case SCANNING:
//                             writeLog("SCANNING");
//                             break;
                         case UNINITIALIZED:
                             writeLog("UNINITIALIZED");
                             break;
                         default:
//                             writeLog("Unknown");
                             break;

                     }
                     int supl_error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                     if(supl_error==WifiManager.ERROR_AUTHENTICATING){
                         writeLog("ERROR_AUTHENTICATING");
                     }
                 }
             }
        }
        , new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
    }

    private void connect(String ssid, String pass){
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", pass);

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }

        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        writeLog("Connecting...");
    }

    private void writeLog(String log) {
        if(count >= MAX_LOG_COUNT) {
            logContainer.removeAllViews();
        }
        TextView textView = new TextView(this);
        textView.setText(log);
        logContainer.addView(textView);
        count++;
    }

    private void scanCode(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning code");
        integrator.initiateScan();
    }

    private String[] parseData(String data){
        String ssid_regex = "WIFI:S:(.*?)(?<!\\\\);";
        String pass_regex = "P:(.*?)(?<!\\\\);;";

        String[] output = new String[2];

        Matcher matcher = Pattern.compile(ssid_regex).matcher(data);
        if(matcher.find()) {
            output[0] = matcher.group(1);
            writeLog("SSID: " + matcher.group(1));
        }

        matcher = Pattern.compile(pass_regex).matcher(data);
        if(matcher.find()) {
            output[1] = matcher.group(1);
            writeLog("PASS: " + matcher.group(1));
        }
        return output;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final String[] output;
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() != null) {
                final AlertDialog.Builder  builder = new AlertDialog.Builder(this);
                builder.setTitle("Scanning Result");
                output = parseData(result.getContents());
                if(output[0] != null && output[1] != null) {
                    builder.setMessage(result.getContents());
                    builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            scanCode();
                        }
                    }).setNegativeButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            connect(output[0], output[1]);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Toast.makeText(this, "Invalid QR code", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "No Results", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}