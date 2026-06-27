package com.android.sheguard.wear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class WearMainActivity extends Activity {

    private static final String TAG = "WearMainActivity";
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final String SOS_ALERT_PATH = "/sheguard/sos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        Button btnVoice = findViewById(R.id.btn_voice_sos);
        Button btnManual = findViewById(R.id.btn_manual_sos);

        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition();
            }
        });

        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSosAlert();
            }
        });
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0).toLowerCase();
                Log.d(TAG, "Spoken text: " + spokenText);
                if (spokenText.contains("help")) {
                    sendSosAlert();
                } else {
                    Toast.makeText(this, "Say 'Help' to trigger SOS", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendSosAlert() {
        Wearable.getNodeClient(this).getConnectedNodes().addOnSuccessListener(nodes -> {
            boolean messageSent = false;
            for (Node node : nodes) {
                Wearable.getMessageClient(this).sendMessage(
                        node.getId(),
                        SOS_ALERT_PATH,
                        "SOS".getBytes()
                ).addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "SOS Alert sent successfully to node: " + node.getId());
                    Toast.makeText(WearMainActivity.this, "SOS Triggered!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send SOS alert", e);
                });
                messageSent = true;
            }
            if (!messageSent) {
                Toast.makeText(this, "No phone connected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
