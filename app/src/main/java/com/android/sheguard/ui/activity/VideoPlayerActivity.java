package com.android.sheguard.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    private WebView webView;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        // Extract YouTube Video ID to embed cleanly
        String videoId = extractYTId(videoUrl);
        if (videoId != null) {
            String embedHtml = "<html><body style=\"margin:0;padding:0;background-color:#000;\">"
                    + "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"
                    + videoId + "?autoplay=1&fs=1&modestbranding=1\" frameborder=\"0\" "
                    + "allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen>"
                    + "</iframe></body></html>";
            webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null);
        } else {
            // Fallback
            webView.loadUrl(videoUrl);
        }
    }

    private String extractYTId(String url) {
        // Very basic extraction for standard v= youtube links
        if (url != null && url.contains("v=")) {
            String[] parts = url.split("v=");
            if (parts.length > 1) {
                String id = parts[1];
                int end = id.indexOf("&");
                if (end != -1) {
                    id = id.substring(0, end);
                }
                return id;
            }
        }
        return null; // For youtu.be links or non-YouTube
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
