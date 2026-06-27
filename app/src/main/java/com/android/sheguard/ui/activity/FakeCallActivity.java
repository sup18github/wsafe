package com.android.sheguard.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.util.RingtonePlayer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FakeCallActivity extends AppCompatActivity {

    private RingtonePlayer ringtonePlayer;
    private LinearLayout layoutIncoming;
    private LinearLayout layoutConnected;
    private TextView tvDuration;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0L;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = SystemClock.uptimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            tvDuration.setText(String.format("%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        TextView tvCallerName = findViewById(R.id.tvCallerName);
        TextView tvCallerNumber = findViewById(R.id.tvCallerNumber);
        layoutIncoming = findViewById(R.id.fabAnswer).getParent().getParent() instanceof LinearLayout 
                ? (LinearLayout) findViewById(R.id.fabAnswer).getParent().getParent() 
                : findViewById(R.id.fabAnswer).getParent() instanceof LinearLayout // Handling the structure from XML
                ? (LinearLayout) findViewById(R.id.fabAnswer).getParent() // Actually in XML it's nested in a LinearLayout (weight 1) -> Horizontal Linear Layout
                : null; // Let's correct binding based on XML structure.

        // Re-reading XML structure:
        // Root RelativeLayout
        //   Top LinearLayout
        //   Bottom Horizontal LinearLayout (id not set? It's the one with alignParentBottom)
        //      Decline Layout
        //      Answer Layout
        //   Connected Layout (id=layoutConnected)
        
        // I need to find the Bottom Horizontal LinearLayout to hide it.
        // I didn't give it an ID involved in previous step.
        // Let's assume I can find it by finding the parent of the buttons.
        
        View fabAnswer = findViewById(R.id.fabAnswer);
        View fabDecline = findViewById(R.id.fabDecline);
        
        // The buttons are inside vertical linear layouts, which are inside the horizontal one.
        View parentAnswer = (View) fabAnswer.getParent();
        View containerActions = (View) parentAnswer.getParent();
        
        layoutIncoming = (LinearLayout) containerActions;
        layoutConnected = findViewById(R.id.layoutConnected);
        tvDuration = findViewById(R.id.tvDuration);
        FloatingActionButton fabEndCall = findViewById(R.id.fabEndCall);

        String name = getIntent().getStringExtra("CALLER_NAME");
        String number = getIntent().getStringExtra("CALLER_NUMBER");

        if (name != null) tvCallerName.setText(name);
        if (number != null && !number.isEmpty()) {
            tvCallerNumber.setText(number);
        } else {
            tvCallerNumber.setVisibility(View.GONE);
        }

        ringtonePlayer = new RingtonePlayer();
        ringtonePlayer.play(this);

        fabAnswer.setOnClickListener(v -> answerCall());
        fabDecline.setOnClickListener(v -> rejectCall());
        fabEndCall.setOnClickListener(v -> finish());
    }

    private void answerCall() {
        ringtonePlayer.stop();
        layoutIncoming.setVisibility(View.GONE);
        layoutConnected.setVisibility(View.VISIBLE);
        
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void rejectCall() {
        ringtonePlayer.stop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ringtonePlayer.stop();
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    @Override
    public void onBackPressed() {
        // Prevent accidental closing during "ringing"
        // But allow if connected? or just block?
        // Standard behavior: Power button silences, standard back might reject.
        // For safety app, let's make back reject.
        rejectCall();
    }
}
