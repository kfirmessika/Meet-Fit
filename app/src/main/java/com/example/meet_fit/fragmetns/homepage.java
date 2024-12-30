package com.example.meet_fit.fragmetns;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.adapters.RecyclerAdapter;
import com.example.meet_fit.models.Info;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class homepage extends Fragment {

    private List<Info> infoList;
    private List<Info> originalList;

    private RecyclerAdapter recyclerAdapter;
    private DatabaseReference databaseReference;

    public homepage() {
        // Required empty public constructor
    }

    public static homepage newInstance(String param1, String param2) {
        homepage fragment = new homepage();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Handle fragment arguments if needed
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main layout for this fragment
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);
        Button btnSettings = view.findViewById(R.id.btnSettings);
        Button btnFilter = view.findViewById(R.id.btnFilter);

        // Set click listeners for buttons
        btnSettings.setOnClickListener(v -> showSettingsPopup(v));
        btnFilter.setOnClickListener(v -> showFilterDialog());
        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list and adapter
        infoList = new ArrayList<>();
        originalList = new ArrayList<>();

        recyclerAdapter = new RecyclerAdapter(getContext(), infoList);
        recyclerView.setAdapter(recyclerAdapter);
        // Initialize Firebase Database reference


        // Fetch data from Firebase
        MainActivity main = (MainActivity)getActivity();
        assert main != null;
        main.fetchDataFromFirebase(recyclerAdapter, infoList,originalList);




        return view;
    }

    private void showSettingsPopup(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.settings_menu, popup.getMenu());



        popup.show();
    }

    // Function to show Filter popup menu
    private void showFilterDialog(){
        // Inflate the filter dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.filter_dialog, null);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView)
                .setTitle("Filter Options")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        // Collect selected filters
                        List<String> selectedActivities = new ArrayList<>();
                        if(cbRun.isChecked()) selectedActivities.add("Running");
                        if(cbBike.isChecked()) selectedActivities.add("Biking");
                        if(cbSwim.isChecked()) selectedActivities.add("Swimming");
                        if(cbHiking.isChecked()) selectedActivities.add("Hiking");
                        if(cbGym.isChecked()) selectedActivities.add("Gym");
                        if(cbOther.isChecked()) selectedActivities.add("Other");

                        List<String> selectedFitnessLevels = new ArrayList<>();
                        if(cbPro.isChecked()) selectedFitnessLevels.add("Professional");
                        if(cbGood.isChecked()) selectedFitnessLevels.add("Very Good");
                        if(cbMid.isChecked()) selectedFitnessLevels.add("Mediocre");
                        if(cbBeginner.isChecked()) selectedFitnessLevels.add("Beginner");

                        applyFilters(selectedActivities, selectedFitnessLevels);
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.show();
    }

    // Function to apply filters based on selected activities and fitness levels
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




}
