package com.example.meet_fit.fragmetns;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.Info;
import com.example.meet_fit.models.dataAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link myProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class myProfile extends Fragment {
    private ImageView imgGallery;
    private Button btnGallery;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // 2) Get the image URI from the intent
                    Uri selectedImageUri = result.getData().getData();
                    // 3) Display the image in the ImageView
                    imgGallery.setImageURI(selectedImageUri);
                }
            });

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public myProfile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment myProfile.
     */
    // TODO: Rename and change types and number of parameters
    public static myProfile newInstance(String param1, String param2) {
        myProfile fragment = new myProfile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @SuppressLint("CutPasteId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);


        MainActivity main = (MainActivity) getActivity();
        assert main != null;
        main.fetchAndPopulateUserData(rootView);

        Button buttonSubmit = rootView.findViewById(R.id.btnSubmit);

        imgGallery = rootView.findViewById(R.id.ivProfilePicture);
        btnGallery = rootView.findViewById(R.id.upload_button);

        // Set click listener on the button
        btnGallery.setOnClickListener(view -> dataAdapter.openGallery(pickImageLauncher));
buttonSubmit.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String userName = ((TextView)rootView.findViewById(R.id.tvTitle)).getText().toString();
        String age = ((Spinner)rootView.findViewById(R.id.spAge)).getSelectedItem().toString();
        String fitLevel = ((Spinner)rootView.findViewById(R.id.spFitnessLevel)).getSelectedItem().toString();
        String aboutMe = ((EditText)rootView.findViewById(R.id.etAboutMe)).getText().toString();
        String location = ((Spinner)rootView.findViewById(R.id.etLocation)).getSelectedItem().toString();
        List<String> activities = getSelectedActivities(rootView);
        String digits = ((Spinner)rootView.findViewById(R.id.spPhonePrefix)).getSelectedItem().toString();
        String number = ((EditText)rootView.findViewById(R.id.etPhoneNumber)).getText().toString();
        String phoneNumber = digits.concat(number);
        ImageView photo = rootView.findViewById(R.id.ivProfilePicture);

        String image = dataAdapter.imageViewToBase64(photo);

        Info userInfo = new Info( userName ,activities,  age,  fitLevel, aboutMe,  location,  phoneNumber,  image);
        dataAdapter.validateInputs( userInfo, main,rootView);
    }
});


        return rootView;
    }

    public List<String> getSelectedActivities(View view) {
        List<String> activities = new ArrayList<>();

        // Find the checkboxes by their IDs
        CheckBox cbGym = view.findViewById(R.id.cbGym);
        CheckBox cbBiking = view.findViewById(R.id.cbBiking);
        CheckBox cbHiking = view.findViewById(R.id.cbHiking);
        CheckBox cbRunning = view.findViewById(R.id.cbRunning);
        CheckBox cbSwimming = view.findViewById(R.id.cbSwimming);
        CheckBox cbOther = view.findViewById(R.id.cbOther);

        // Check if each checkbox is checked, and if so, add it to the list
        if (cbGym.isChecked()) {
            activities.add("Gym");
        }
        if (cbBiking.isChecked()) {
            activities.add("Biking");
        }
        if (cbHiking.isChecked()) {
            activities.add("Hiking");
        }
        if (cbRunning.isChecked()) {
            activities.add("Running");
        }
        if (cbSwimming.isChecked()) {
            activities.add("Swimming");
        }
        if (cbOther.isChecked()) {
            activities.add("Other");
        }

        return activities;
    }




}