package com.android.sheguard.ui.adapter;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.sheguard.R;
import com.android.sheguard.model.HelplineModel;

import java.util.ArrayList;

public class HelplineAdapter extends RecyclerView.Adapter<HelplineAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<HelplineModel> helplines;

    public HelplineAdapter(@NonNull Context context, ArrayList<HelplineModel> helplines) {
        this.helplines = helplines;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_helpline_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelplineModel model = helplines.get(position);
        holder.name.setText(model.getName());
        holder.details.setText(model.getDetails());

        if ("OPEN MAP".equals(model.getNumber())) {
            holder.number.setText("Tap to find nearby");
            holder.number.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            holder.number.setOnClickListener(v -> {
                String query = model.getName().contains("Hospital") ? "hospital" : "police station";
                android.net.Uri gmmIntentUri = android.net.Uri.parse("geo:0,0?q=" + query);
                android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    context.startActivity(mapIntent);
                } catch (Exception e) {
                   // Fallback if maps not installed
                   try {
                       context.startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, 
                           android.net.Uri.parse("https://www.google.com/maps/search/" + query)));
                   } catch (Exception e2) {
                       // No browser either?
                   }
                }
            });
        } else {
            holder.number.setText(model.getNumber());
            holder.number.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            Linkify.addLinks(holder.number, Patterns.PHONE, "tel:", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter);
            holder.number.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public int getItemCount() {
        return helplines.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, number, details;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            details = itemView.findViewById(R.id.details);
        }
    }
}
