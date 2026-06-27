package com.android.sheguard.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.sheguard.R;
import com.android.sheguard.databinding.FragmentSafetyTipsBinding;
import com.android.sheguard.ui.adapter.SafetyTipsAdapter;
import com.android.sheguard.model.SafetyTipModel;

import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class SafetyTipsFragment extends Fragment {

    private final ArrayList<SafetyTipModel> safetyTips = new ArrayList<>();
    private FragmentSafetyTipsBinding binding;
    private SafetyTipsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSafetyTipsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_safety_tips_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_safety_tips_desc));
        }

        // Safety Videos (Top for better visibility)
        safetyTips.add(new SafetyTipModel("Video: 5 Self Defense Moves Every Woman Should Know", "", R.raw.self_defense));
        safetyTips.add(new SafetyTipModel("Video: Understanding & Stopping Workplace Harassment", "", R.raw.workplace_harassment));
        safetyTips.add(new SafetyTipModel("Video: How to Deal with Street Harassment", "", R.raw.street_harassment));
        safetyTips.add(new SafetyTipModel("Video: Cyber Stalking & Online Safety", "", R.raw.cyber_stalking));
        
        // New Smart Tips
        safetyTips.add(new SafetyTipModel("Trust Your Instincts", "If a situation or person makes you feel uncomfortable, leave immediately. Your gut feeling is often right."));
        safetyTips.add(new SafetyTipModel("Share Your Live Location", "When traveling alone, especially in taxis or rideshares, share your live location with a trusted contact via WhatsApp or this app."));
        safetyTips.add(new SafetyTipModel("Be Loud", "If you are being harassed, do not be afraid to shout 'STOP' or 'HELP'. Drawing attention is your best defense in public."));
        safetyTips.add(new SafetyTipModel("Walk with Confidence", "Walk with your head up and aware of your surroundings. Predators often target those who look distracted or unsure."));
        safetyTips.add(new SafetyTipModel("The 'Crowded Place' Rule", "If you suspect you are being followed, never go home. Head straight for a crowded shop, cafe, or police station."));

        // Existing Tips
        safetyTips.add(new SafetyTipModel("General Tip", getString(R.string.safety_tips_tip_1)));
        safetyTips.add(new SafetyTipModel("Shopping Safety", getString(R.string.safety_tips_tip_2)));
        safetyTips.add(new SafetyTipModel("Awareness", getString(R.string.safety_tips_tip_3)));
        safetyTips.add(new SafetyTipModel("Purse Safety", getString(R.string.safety_tips_tip_4)));
        safetyTips.add(new SafetyTipModel("Car Safety", getString(R.string.safety_tips_tip_5)));
        safetyTips.add(new SafetyTipModel("Parking Safety", getString(R.string.safety_tips_tip_6)));
        safetyTips.add(new SafetyTipModel("Vehicle Approach", getString(R.string.safety_tips_tip_7)));
        safetyTips.add(new SafetyTipModel("Leaving Work", getString(R.string.safety_tips_tip_8)));
        safetyTips.add(new SafetyTipModel("Home Address Privacy", getString(R.string.safety_tips_tip_9)));
        safetyTips.add(new SafetyTipModel("Hotel Safety", getString(R.string.safety_tips_tip_10)));
        safetyTips.add(new SafetyTipModel("Hotel Hallways", getString(R.string.safety_tips_tip_11)));
        safetyTips.add(new SafetyTipModel("Lock Doors", getString(R.string.safety_tips_tip_12)));
        safetyTips.add(new SafetyTipModel("Room Service", getString(R.string.safety_tips_tip_13)));
        safetyTips.add(new SafetyTipModel("Asking Directions", getString(R.string.safety_tips_tip_14)));
        safetyTips.add(new SafetyTipModel("Drink Safety", getString(R.string.safety_tips_tip_15)));
        safetyTips.add(new SafetyTipModel("Designated Driver", getString(R.string.safety_tips_tip_16)));
        safetyTips.add(new SafetyTipModel("Watch Your Drink", getString(R.string.safety_tips_tip_17)));
        safetyTips.add(new SafetyTipModel("Stick Together", getString(R.string.safety_tips_tip_18)));
        safetyTips.add(new SafetyTipModel("Social Media Privacy", getString(R.string.safety_tips_tip_19)));
        safetyTips.add(new SafetyTipModel("Hidden Cameras", getString(R.string.safety_tips_tip_20)));
        safetyTips.add(new SafetyTipModel("Self Defense", getString(R.string.safety_tips_tip_21)));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SafetyTipsAdapter(requireContext(), safetyTips, getViewLifecycleOwner());
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        return view;
    }
}