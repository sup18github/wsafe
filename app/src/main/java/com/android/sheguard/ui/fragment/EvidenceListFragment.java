package com.android.sheguard.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.sheguard.R;
import com.android.sheguard.databinding.FragmentEvidenceListBinding;
import com.android.sheguard.model.EvidenceModel;
import com.android.sheguard.ui.adapter.EvidenceAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

@SuppressLint({"StaticFieldLeak"})
public class EvidenceListFragment extends Fragment {

    public static ArrayList<EvidenceModel> evidenceList;
    public static View tvEmptyList;
    public static EvidenceAdapter adapter;
    private FragmentEvidenceListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEvidenceListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_evidence_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_evidence_desc));
        }

        tvEmptyList = view.findViewById(R.id.tv_empty_list);

        evidenceList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EvidenceAdapter(requireContext(), evidenceList, evidence -> {
            showEvidenceOptions(evidence);
        });
        binding.recyclerView.setAdapter(adapter);

        loadEvidenceData();

        return view;
    }

    private void loadEvidenceData() {
        evidenceList.clear();
        evidenceList.addAll(getLocalEvidence());
        adapter.notifyDataSetChanged();
        tvEmptyList.setVisibility(evidenceList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private ArrayList<EvidenceModel> getLocalEvidence() {
        ArrayList<EvidenceModel> localEvidence = new ArrayList<>();
        
        File evidenceDir = new File(requireContext().getFilesDir(), "evidence");
        if (evidenceDir.exists() && evidenceDir.isDirectory()) {
            File[] files = evidenceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().endsWith(".mp4")) continue;
                    
                    EvidenceModel evidence = new EvidenceModel();
                    String fileName = file.getName();
                    
                    // Determine type based on filename prefix
                    String type;
                    String sosId;
                    if (fileName.startsWith("video_")) {
                        type = "video";
                        sosId = fileName.replace("video_", "").replace(".mp4", "");
                    } else {
                        type = "audio";
                        sosId = fileName.replace("evidence_", "").replace(".mp4", "");
                    }
                    
                    evidence.setId(sosId + "_" + type);
                    evidence.setSosEventId(sosId);
                    evidence.setFileName(fileName);
                    evidence.setFileSize(file.length());
                    evidence.setTimestamp(file.lastModified());
                    evidence.setType(type);
                    evidence.setRecordingDuration(120000);
                    evidence.setUploaded(false);
                    localEvidence.add(evidence);
                }
            }
        }
        
        Collections.sort(localEvidence, (e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        
        return localEvidence;
    }

    private void showEvidenceOptions(EvidenceModel evidence) {
        boolean isVideo = "video".equals(evidence.getType());
        String playLabel = isVideo ? "Play Video" : "Play Audio";
        String[] options = {playLabel, "Share", "Delete"};
        
        new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialComponents_MaterialAlertDialog)
                .setTitle("Evidence Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            playEvidence(evidence);
                            break;
                        case 1:
                            shareEvidence(evidence);
                            break;
                        case 2:
                            deleteEvidence(evidence);
                            break;
                    }
                })
                .show();
    }

    private void playEvidence(EvidenceModel evidence) {
        File evidenceDir = new File(requireContext().getFilesDir(), "evidence");
        File evidenceFile = new File(evidenceDir, evidence.getFileName());
        
        if (evidenceFile.exists()) {
            Uri fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    evidenceFile
            );
            
            boolean isVideo = "video".equals(evidence.getType());
            String mimeType = isVideo ? "video/mp4" : "audio/mp4";
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            try {
                startActivity(intent);
            } catch (Exception e) {
                Snackbar.make(binding.getRoot(), "No app found to play this file", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(binding.getRoot(), "File not found", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void shareEvidence(EvidenceModel evidence) {
        File evidenceDir = new File(requireContext().getFilesDir(), "evidence");
        File evidenceFile = new File(evidenceDir, evidence.getFileName());
        
        if (evidenceFile.exists()) {
            Uri fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    evidenceFile
            );
            
            boolean isVideo = "video".equals(evidence.getType());
            String mimeType = isVideo ? "video/mp4" : "audio/mp4";
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    "Evidence (" + evidence.getType().toUpperCase() + ") recorded at: " + evidence.getFormattedDate() + 
                    "\nLocation: " + evidence.getLocationString());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Evidence"));
        }
    }

    private void deleteEvidence(EvidenceModel evidence) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialComponents_MaterialAlertDialog)
                .setMessage("Are you sure you want to delete this evidence?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    File evidenceDir = new File(requireContext().getFilesDir(), "evidence");
                    File evidenceFile = new File(evidenceDir, evidence.getFileName());
                    
                    if (evidenceFile.exists() && evidenceFile.delete()) {
                        evidenceList.remove(evidence);
                        adapter.notifyDataSetChanged();
                        tvEmptyList.setVisibility(evidenceList.isEmpty() ? View.VISIBLE : View.GONE);
                        Snackbar.make(binding.getRoot(), "Evidence deleted", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && evidenceList != null) {
            loadEvidenceData();
        }
    }
}