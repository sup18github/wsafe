package com.android.sheguard.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.databinding.FragmentEditProfileBinding;
import com.android.sheguard.model.UserModel;
import com.android.sheguard.ui.view.LoadingDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_edit_profile_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_edit_profile_desc));
        }
        
        // Force Firestore to try and connect
        FirebaseFirestore.getInstance().enableNetwork().addOnFailureListener(e -> 
            Log.e("EditProfileFragment", "Error restarting network", e));

        loadingDialog = new LoadingDialog(getContext());

        binding.btnSave.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(getContext(), "No internet connection. Changes will be saved locally and synced later.", Toast.LENGTH_LONG).show();
            }
            loadingDialog.show(null);
            saveDetailsInDatabase();
        });
        
        loadCurrentDetails();

        return view;
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager 
              = (android.net.ConnectivityManager) requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadCurrentDetails() {
         // Load from Prefs first (Offline support)
         String savedName = com.android.sheguard.config.Prefs.getString(Constants.PREFS_USER_NAME, "");
         String savedPhone = com.android.sheguard.config.Prefs.getString(Constants.PREFS_USER_PHONE, "");
         
         if (!savedName.isEmpty()) binding.etNewName.setText(savedName);
         if (!savedPhone.isEmpty()) binding.etNewPhone.setText(savedPhone);

         // Also try to fetch latest from Firestore if online (optional sync)
         FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
         if (currentUser != null && isNetworkAvailable()) {
             FirebaseFirestore.getInstance()
                    .collection(Constants.FIRESTORE_COLLECTION_USERLIST)
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                             String name = documentSnapshot.getString("name");
                             String phone = documentSnapshot.getString("phone");
                             // Only update UI if user hasn't typed anything yet or if it matches
                             if (binding.etNewName.getText().toString().isEmpty() && name != null) binding.etNewName.setText(name);
                             if (binding.etNewPhone.getText().toString().isEmpty() && phone != null) binding.etNewPhone.setText(phone);
                             
                             // Update Prefs to keep in sync
                             if (name != null) com.android.sheguard.config.Prefs.putString(Constants.PREFS_USER_NAME, name);
                             if (phone != null) com.android.sheguard.config.Prefs.putString(Constants.PREFS_USER_PHONE, phone);
                        }
                    });
         }
    }

    private void saveDetailsInDatabase() {
        String name = binding.etNewName.getText() != null ? binding.etNewName.getText().toString().trim() : "";
        String phone = binding.etNewPhone.getText() != null ? binding.etNewPhone.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty()) {
            loadingDialog.hide();
            Toast.makeText(getContext(), "Please enter name and phone", Toast.LENGTH_SHORT).show();
            return;
        }

        // SAVE LOCALLY (OFFLINE FIRST RESPONSE)
        com.android.sheguard.config.Prefs.putString(Constants.PREFS_USER_NAME, name);
        com.android.sheguard.config.Prefs.putString(Constants.PREFS_USER_PHONE, phone);
        
        // Notify user immediately
        loadingDialog.hide();
        Snackbar.make(binding.getRoot(), getString(R.string.details_saved_successfully), Snackbar.LENGTH_SHORT).show();

        // SAVE TO FIRESTORE (BACKGROUND SYNC)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            CollectionReference UserList = firestore.collection(Constants.FIRESTORE_COLLECTION_USERLIST);
            
            // We need existing email to save full object, try to get from auth or doc
            String email = currentUser.getEmail(); 
            
            // Just update the fields we have; set merge=true if possible or just use update()
            // Since UserModel might require full constructor, we'll do a simple map update for robustness
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("name", name);
            updates.put("phone", phone);
            if (email != null) updates.put("email", email);

            UserList.document(currentUser.getUid())
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnFailureListener(e -> {
                    // Log error but don't disturb user since local save worked
                    Log.e("EditProfileFragment", "Background Sync Failed", e);
                });
        }
    }

    private void logError(Exception e) {
        if (e != null) {
            Log.e("EditProfileFragment", "Firestore Error", e);
        }
    }

    @Override
    public void onDestroyView() {
        if (loadingDialog != null) {
            loadingDialog.hide();
        }
        super.onDestroyView();
    }
}