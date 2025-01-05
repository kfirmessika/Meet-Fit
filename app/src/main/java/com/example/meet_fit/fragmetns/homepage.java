package com.example.meet_fit.fragmetns;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.adapters.RecyclerAdapter;
import com.example.meet_fit.models.Info;

import java.util.ArrayList;
import java.util.List;

public class homepage extends Fragment {

    private List<Info> infoList;
    private List<Info> originalList;
    private final List<String> selectedActivities = new ArrayList<>();
    private final List<String> selectedFitnessLevels = new ArrayList<>();

    private RecyclerAdapter recyclerAdapter;


    public homepage() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_homepage, container, false);

        // Initialize Buttons
        Button btnSettings = rootview.findViewById(R.id.btnSettings);
        Button btnFilter = rootview.findViewById(R.id.btnFilter);

        // Set click listeners for Buttons
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnFilter.setOnClickListener(view -> showFilterDialog());

        // Initialize RecyclerView
        RecyclerView recyclerView = rootview.findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list and adapter
        infoList = new ArrayList<>();
        originalList = new ArrayList<>();



        // Create Adapter with a click listener
        recyclerAdapter = new RecyclerAdapter(getContext(), infoList, phoneNumber -> {
            Log.d("Fragment", "Received phone number: " + phoneNumber);

            if (phoneNumber != null && !TextUtils.isEmpty(phoneNumber)) {
                // Launch the dialer
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            } else {
                // Log an error and show a toast if the phone number is missing
                Log.e("Fragment", "Phone number is missing!");
                Toast.makeText(getContext(), "Phone number is missing", Toast.LENGTH_SHORT).show();
            }
        });



        // Fetch data from Firebase
        MainActivity main = (MainActivity) getActivity();
        if (main != null) {
            main.fetchDataFromFirebase(recyclerAdapter, infoList, originalList);
        }
        recyclerView.setAdapter(recyclerAdapter);
        return rootview;
    }

    // Function to show Filter popup menu
    private void showFilterDialog(){
        // Inflate the filter dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.filter_dialog, null);

        preselectFilters( dialogView,selectedActivities, selectedFitnessLevels);
        // Initialize CheckBoxes
        CheckBox cbRun = dialogView.findViewById(R.id.cbRun);
        CheckBox cbBike = dialogView.findViewById(R.id.cbBike);
        CheckBox cbSwim = dialogView.findViewById(R.id.cbSwim);
        CheckBox cbHiking = dialogView.findViewById(R.id.cbHiking);
        CheckBox cbGym = dialogView.findViewById(R.id.cbGym);
        CheckBox cbOther = dialogView.findViewById(R.id.cbOther);

        CheckBox cbPro = dialogView.findViewById(R.id.cbPro);
        CheckBox cbGood = dialogView.findViewById(R.id.cbGood);
        CheckBox cbMid = dialogView.findViewById(R.id.cbMid);
        CheckBox cbBeginner = dialogView.findViewById(R.id.cbBeginner);

        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle("Filter Options")
                .setPositiveButton("Apply", (dialog, which) -> {
                    // Collect selected filters
                    selectedActivities.clear();
                    selectedFitnessLevels.clear();


                    if(cbRun.isChecked()) selectedActivities.add("Running");
                    if(cbBike.isChecked()) selectedActivities.add("Biking");
                    if(cbSwim.isChecked()) selectedActivities.add("Swim");
                    if(cbHiking.isChecked()) selectedActivities.add("Hiking");
                    if(cbGym.isChecked()) selectedActivities.add("Gym");
                    if(cbOther.isChecked()) selectedActivities.add("Other");


                    if(cbPro.isChecked()) selectedFitnessLevels.add("Professional");
                    if(cbGood.isChecked()) selectedFitnessLevels.add("Very Good");
                    if(cbMid.isChecked()) selectedFitnessLevels.add("Mediocre");
                    if(cbBeginner.isChecked()) selectedFitnessLevels.add("Beginner");

                    applyFilters(selectedActivities, selectedFitnessLevels);
                })
                .setNegativeButton("Cancel", null);

        builder.show();
    }

    // Function to apply filters based on selected activities and fitness levels
    @SuppressLint("NotifyDataSetChanged")
    private void applyFilters(List<String> selectedActivities, List<String> selectedFitnessLevels) {
        List<Info> filteredList = new ArrayList<>();

        for (Info info : originalList) {
            boolean matchesActivity = selectedActivities.isEmpty() ||
                    (info.getActivities() != null && !info.getActivities().isEmpty() &&
                            info.getActivities().stream().anyMatch(activity ->
                                    selectedActivities.stream().anyMatch(selected ->
                                            selected.equalsIgnoreCase(activity))));

            boolean matchesFitness = selectedFitnessLevels.isEmpty() ||
                    (info.getFitLevel() != null &&
                            selectedFitnessLevels.stream().anyMatch(selected ->
                                    selected.equalsIgnoreCase(info.getFitLevel())));

            if (matchesActivity && matchesFitness) {
                filteredList.add(info);
            }
        }

        infoList.clear();
        infoList.addAll(filteredList);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void preselectFilters(View dialogView, List<String> selectedActivities, List<String> selectedFitnessLevels) {


        // Find activity checkboxes
        CheckBox cbRun = dialogView.findViewById(R.id.cbRun);
        CheckBox cbBike = dialogView.findViewById(R.id.cbBike);
        CheckBox cbSwim = dialogView.findViewById(R.id.cbSwim);
        CheckBox cbHiking = dialogView.findViewById(R.id.cbHiking);
        CheckBox cbGym = dialogView.findViewById(R.id.cbGym);
        CheckBox cbOther = dialogView.findViewById(R.id.cbOther);

        // Find fitness level checkboxes
        CheckBox cbPro = dialogView.findViewById(R.id.cbPro);
        CheckBox cbGood = dialogView.findViewById(R.id.cbGood);
        CheckBox cbMid = dialogView.findViewById(R.id.cbMid);
        CheckBox cbBeginner = dialogView.findViewById(R.id.cbBeginner);

        // Preselect activity checkboxes
        if (selectedActivities.contains("Running")) cbRun.setChecked(true);
        if (selectedActivities.contains("Biking")) cbBike.setChecked(true);
        if (selectedActivities.contains("Swim")) cbSwim.setChecked(true);
        if (selectedActivities.contains("Hiking")) cbHiking.setChecked(true);
        if (selectedActivities.contains("Gym")) cbGym.setChecked(true);
        if (selectedActivities.contains("Other")) cbOther.setChecked(true);

        // Preselect fitness level checkboxes
        if (selectedFitnessLevels.contains("Professional")) cbPro.setChecked(true);
        if (selectedFitnessLevels.contains("Very Good")) cbGood.setChecked(true);
        if (selectedFitnessLevels.contains("Mediocre")) cbMid.setChecked(true);
        if (selectedFitnessLevels.contains("Beginner")) cbBeginner.setChecked(true);
    }

    public void showSettingsDialog() {

        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.settings_diaglog, null);


        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize buttons
        Button btnMyProfile = dialogView.findViewById(R.id.btn_my_profile);
        Button btnChangeSignInData = dialogView.findViewById(R.id.btn_change_signin_data);
        Button btnLogOut = dialogView.findViewById(R.id.btn_log_out);

        MainActivity main = (MainActivity) getActivity();
        assert main != null;

        // Set button actions
        btnMyProfile.setOnClickListener(v -> {
            // Handle My Profile click
            main.homepageToMyProfile();
            dialog.cancel();
        });

        btnChangeSignInData.setOnClickListener(v -> {
            // Handle Change Sign in Data click
            main.homepageToUpdateAuthentication();
            dialog.cancel();
        });

        btnLogOut.setOnClickListener(v -> {
            // Handle Log Out click
            main.homepageToLogin();
            dialog.cancel();
        });


    }

}
