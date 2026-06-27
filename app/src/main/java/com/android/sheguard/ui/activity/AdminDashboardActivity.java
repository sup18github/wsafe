package com.android.sheguard.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import android.graphics.Color;
import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotal, tvLowRisk, tvModerateRisk, tvHighRisk, tvSevereRisk;
    private ProgressBar progressBar;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotal = findViewById(R.id.tvTotal);
        tvLowRisk = findViewById(R.id.tvLowRisk);
        tvModerateRisk = findViewById(R.id.tvModerateRisk);
        tvHighRisk = findViewById(R.id.tvHighRisk);
        tvSevereRisk = findViewById(R.id.tvSevereRisk);
        progressBar = findViewById(R.id.progressBar);
        pieChart = findViewById(R.id.pieChart);

        setupPieChartBasic();
        loadStatistics();
    }

    private void loadStatistics() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("quiz_assessments")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        int total = 0;
                        int low = 0;
                        int moderate = 0;
                        int high = 0;
                        int severe = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            total++;
                            if (document.contains("risk_level")) {
                                String riskLevel = document.getString("risk_level");
                                if ("Low Risk".equals(riskLevel)) low++;
                                else if ("Moderate Risk".equals(riskLevel)) moderate++;
                                else if ("High Risk".equals(riskLevel)) high++;
                                else if ("Severe Risk".equals(riskLevel)) severe++;
                            }
                        }

                        tvTotal.setText("Total Assessments: " + total);
                        tvLowRisk.setText("Low Risk: " + low);
                        tvModerateRisk.setText("Moderate Risk: " + moderate);
                        tvHighRisk.setText("High Risk: " + high);
                        tvSevereRisk.setText("Severe Risk: " + severe);

                        updatePieChart(low, moderate, high, severe);

                    } else {
                        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupPieChartBasic() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Risk Levels");
        pieChart.setCenterTextColor(getResources().getColor(android.R.color.darker_gray, null));
        pieChart.getLegend().setEnabled(false);
    }

    private void updatePieChart(int low, int mod, int high, int sev) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (low > 0) entries.add(new PieEntry(low, "Low"));
        if (mod > 0) entries.add(new PieEntry(mod, "Moderate"));
        if (high > 0) entries.add(new PieEntry(high, "High"));
        if (sev > 0) entries.add(new PieEntry(sev, "Severe"));

        PieDataSet dataSet = new PieDataSet(entries, "Risk Validation");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(android.R.color.holo_green_dark, null));
        colors.add(getResources().getColor(android.R.color.holo_orange_light, null));
        colors.add(getResources().getColor(android.R.color.holo_orange_dark, null));
        colors.add(getResources().getColor(android.R.color.holo_red_dark, null));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}
