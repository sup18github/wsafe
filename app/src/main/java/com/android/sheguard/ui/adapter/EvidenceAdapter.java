package com.android.sheguard.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sheguard.R;
import com.android.sheguard.model.EvidenceModel;

import java.util.ArrayList;

public class EvidenceAdapter extends RecyclerView.Adapter<EvidenceAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<EvidenceModel> evidenceList;
    private final OnEvidenceClickListener listener;

    public interface OnEvidenceClickListener {
        void onEvidenceClick(EvidenceModel evidence);
    }

    public EvidenceAdapter(Context context, ArrayList<EvidenceModel> evidenceList, OnEvidenceClickListener listener) {
        this.context = context;
        this.evidenceList = evidenceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_evidence_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EvidenceModel evidence = evidenceList.get(position);

        holder.evidenceDate.setText(evidence.getFormattedDate());
        holder.evidenceLocation.setText(evidence.getLocationString());
        holder.evidenceDuration.setText(evidence.getFormattedDuration());

        // Set type badge
        String type = evidence.getType();
        boolean isVideo = "video".equals(type);
        holder.evidenceType.setText(isVideo ? "VIDEO" : "AUDIO");
        holder.evidenceType.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                context.getColor(isVideo ? R.color.colorVideoEvidence : R.color.colorAudioEvidence)
        ));

        // Set icon based on type
        if (isVideo) {
            holder.evidenceIcon.setImageResource(R.drawable.ic_play_arrow);
            holder.evidenceIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    context.getColor(R.color.colorVideoEvidence)
            ));
        } else {
            holder.evidenceIcon.setImageResource(R.drawable.ic_mic);
            holder.evidenceIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    context.getColor(R.color.colorAudioEvidence)
            ));
        }

        if (evidence.isUploaded()) {
            holder.uploadStatus.setImageResource(R.drawable.ic_cloud_done);
            holder.uploadStatus.setColorFilter(context.getColor(R.color.green_active));
        } else {
            holder.uploadStatus.setImageResource(R.drawable.ic_cloud_upload);
            holder.uploadStatus.setColorFilter(context.getColor(R.color.text_color_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEvidenceClick(evidence);
            }
        });
    }

    @Override
    public int getItemCount() {
        return evidenceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView evidenceDate, evidenceLocation, evidenceDuration, evidenceType;
        ImageView uploadStatus, evidenceIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            evidenceDate = itemView.findViewById(R.id.evidenceDate);
            evidenceLocation = itemView.findViewById(R.id.evidenceLocation);
            evidenceDuration = itemView.findViewById(R.id.evidenceDuration);
            evidenceType = itemView.findViewById(R.id.evidenceType);
            uploadStatus = itemView.findViewById(R.id.uploadStatus);
            evidenceIcon = itemView.findViewById(R.id.evidenceIcon);
        }
    }
}