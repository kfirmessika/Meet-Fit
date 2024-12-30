package com.example.meet_fit.fragmetns;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link register#newInstance} factory method to
 * create an instance of this fragment.
 */
public class register extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public register() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment register.
     */
    // TODO: Rename and change types and number of parameters
    public static register newInstance(String param1, String param2) {
        register fragment = new register();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_register, container, false);
        Button buttonContinue = rootview.findViewById(R.id.btnContinue);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity main = (MainActivity) getActivity();
                String username = ((EditText)rootview.findViewById(R.id.etUsername)).getText().toString();
                String email = ((EditText)rootview.findViewById(R.id.etEmail)).getText().toString();
                String password = ((EditText)rootview.findViewById(R.id.etPassword)).getText().toString();
                String passwordVerification = ((EditText)rootview.findViewById(R.id.etConfirmPassword)).getText().toString();
                assert main != null;
                User user = new User(username,  email, password);
              ValidateSignUpInputs(user,passwordVerification, rootview, main);





            }
        });

        return rootview;
    }
    private boolean EmailValidation(String email) {
        // Regular expression for basic email validation
        String emailRegex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // 2. Password Validation
    private boolean PasswordValidation(String password) {
        // Password must be at least 8 characters, containing at least one uppercase letter,
        // one lowercase letter, one digit, and one special character.
        if (password.length() < 8) {
            return false;
        }
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // 3. Password Verification Validation (check if passwords match)
    private boolean PasswordVerificationValidation(String password, String PasswordVerification) {
        // Check if both passwords match
        return password.equals(PasswordVerification);
    }

    private void ValidateSignUpInputs(User user, String passwordVerification, View view , MainActivity main) {
        // Check email validation
        if (user.getUser().isEmpty()) {
            Toast.makeText(requireContext(), "Make sure the user is not empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!EmailValidation(user.getEmail())) {
            Toast.makeText(requireContext(), "Invalid email format. Please enter a valid email.", Toast.LENGTH_SHORT).show();
            return ;
        }

        // Check password validation
        if (!PasswordValidation(user.getPassword())) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character.", Toast.LENGTH_LONG).show();
            return ;
        }

        // Check password verification
        if (!PasswordVerificationValidation(user.getPassword(), passwordVerification)) {
            Toast.makeText(requireContext(), "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
            return ;
        }
        CheckBox terms = view.findViewById(R.id.cbAgree);
        if(!terms.isChecked())
        {
            Toast.makeText(requireContext(), "Please agree to terms and conditions", Toast.LENGTH_SHORT).show();
            return ;
        }

        // All validations passed, proceed to register the user
        Toast.makeText(requireContext(), "All inputs are valid. Proceeding to register...", Toast.LENGTH_SHORT).show();
        main.register(user, isSuccess -> {
            if (isSuccess) {
                // Navigate to the next fragment only on success
                main.registerToUser(view);
            } else {
                // Handle failure case if needed
                Toast.makeText(getActivity(), "Failed to save info. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }); // Call the registrati/ Call the registration method

    }
}