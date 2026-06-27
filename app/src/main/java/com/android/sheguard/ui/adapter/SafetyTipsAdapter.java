package com.android.sheguard.ui.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sheguard.R;
import com.android.sheguard.model.SafetyTipModel;

import java.util.ArrayList;

public class SafetyTipsAdapter extends RecyclerView.Adapter<SafetyTipsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<SafetyTipModel> safetyTips;
    private final LifecycleOwner lifecycleOwner;

    public SafetyTipsAdapter(@NonNull Context context, ArrayList<SafetyTipModel> safetyTips, LifecycleOwner lifecycleOwner) {
        this.safetyTips = safetyTips;
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_safety_tips_list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SafetyTipModel tip = safetyTips.get(position);
        
        holder.videoContainer.setVisibility(View.GONE);
        holder.videoView.stopPlayback();
        
        if (tip.isVideo()) {
            holder.tips.setText(tip.getTitle());
            holder.tips.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.tips.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            holder.iconPlay.setVisibility(View.VISIBLE);
            
            holder.itemHeader.setOnClickListener(v -> {
                if (holder.videoContainer.getVisibility() == View.VISIBLE) {
                    holder.videoContainer.setVisibility(View.GONE);
                    if (holder.videoView.isPlaying()) {
                        holder.videoView.pause();
                    }
                } else {
                    holder.videoContainer.setVisibility(View.VISIBLE);
                    String videoPath = "android.resource://" + context.getPackageName() + "/" + tip.getVideoResId();
                    Uri uri = Uri.parse(videoPath);
                    holder.videoView.setVideoURI(uri);
                    
                    MediaController mediaController = new MediaController(context);
                    holder.videoView.setMediaController(mediaController);
                    mediaController.setAnchorView(holder.videoView);
                    
                    holder.videoView.requestFocus();
                    holder.videoView.start();
                }
            });
            holder.itemHeader.setOnLongClickListener(null);
        } else {
            holder.tips.setText(tip.getDescription());
            holder.tips.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.tips.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text)); // fallback black/grey
             
            holder.iconPlay.setVisibility(View.GONE);

            holder.itemHeader.setOnLongClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Safety Tip", tip.getDescription());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Tip copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            });
            holder.itemHeader.setOnClickListener(null); 
        }
    }

    @Override
    public int getItemCount() {
        return safetyTips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tips;
        ImageView iconPlay;
        View itemHeader;
        View videoContainer;
        VideoView videoView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tips = itemView.findViewById(R.id.tips);
            iconPlay = itemView.findViewById(R.id.icon_play);
            itemHeader = itemView.findViewById(R.id.item_header);
            videoContainer = itemView.findViewById(R.id.video_container);
            videoView = itemView.findViewById(R.id.video_view);
        }
    }
}
