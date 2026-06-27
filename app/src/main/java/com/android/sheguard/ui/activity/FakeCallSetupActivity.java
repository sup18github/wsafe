package com.android.sheguard.ui.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.util.TriggerManager;
import com.google.android.material.textfield.TextInputEditText;

public class FakeCallSetupActivity extends AppCompatActivity {

    private TextInputEditText etCallerName;
    private TextInputEditText etCallerNumber;
    private RadioGroup rgDelay;
    private Button btnScheduleCall;
    private Button btnCancelSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call_setup);

        etCallerName = findViewById(R.id.etCallerName);
        etCallerNumber = findViewById(R.id.etCallerNumber);
        rgDelay = findViewById(R.id.rgDelay);
        btnScheduleCall = findViewById(R.id.btnScheduleCall);
        btnCancelSchedule = findViewById(R.id.btnCancelSchedule);

        btnScheduleCall.setOnClickListener(v -> scheduleCall());
        btnCancelSchedule.setOnClickListener(v -> cancelCall());
    }

    private void scheduleCall() {
        String name = etCallerName.getText().toString().trim();
        String number = etCallerNumber.getText().toString().trim();

        if (name.isEmpty()) {
            name = "Home"; // Default
        }

        long delay = 0;
        int checkedId = rgDelay.getCheckedRadioButtonId();

        if (checkedId == R.id.rb10s) {
            delay = 10000;
        } else if (checkedId == R.id.rb30s) {
            delay = 30000;
        } else if (checkedId == R.id.rb1min) {
            delay = 60000;
        }

        TriggerManager.getInstance().scheduleCall(this, name, number, delay);

        if (delay == 0) {
            // Instant
             // Activity starts immediately, maybe we can finish this one or stay ?
             // Usually keeping setup open is fine.
        } else {
            Toast.makeText(this, "Fake call scheduled in " + (delay / 1000) + " seconds", Toast.LENGTH_SHORT).show();
            // Move task to back? Or just finish?
            // "Discreet escape" -> maybe user wants to put phone away.
            moveTaskToBack(true);
        }
    }

    private void cancelCall() {
        TriggerManager.getInstance().cancelScheduledCall(this);
        Toast.makeText(this, "Scheduled call cancelled", Toast.LENGTH_SHORT).show();
    }
}
