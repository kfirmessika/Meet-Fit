package com.example.meet_fit.fragmetns;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;


public class login extends Fragment {

    public login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        TextView buttonSignup = rootView.findViewById(R.id.tvSignUp);
        Button buttonSigning = rootView.findViewById(R.id.btnSignIn);
        MainActivity main =(MainActivity) getActivity();
        assert main != null;
        buttonSignup.setOnClickListener(view -> main.loginToRegister(rootView));

        buttonSigning.setOnClickListener(view -> {
            String email = ((EditText)rootView.findViewById(R.id.etEmailSignIn)).getText().toString();
            String password = ((EditText)rootView.findViewById(R.id.etPasswordSignIn)).getText().toString();
           validateEmailAndPassword(email,password,main,rootView);

        });

        return rootView;
    }

    private void validateEmailAndPassword(String email, String password ,MainActivity main, View view ) {
        // Check if the email is empty
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(getContext(), "Email is required", Toast.LENGTH_SHORT).show();
            return  ;
        }

        // Check if the email format is valid
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            return  ;
        }

        // Check if the password is empty
        if (password == null || password.trim().isEmpty()) {
            Toast.makeText(getContext(), "Password is required", Toast.LENGTH_SHORT).show();
            return ;
        }


        main.login(email,password, isSuccess -> {
            if (isSuccess) {
                // Navigate to the next fragment only on success
                main.checkFullSignIn(isSuccess2 -> {
                    if (isSuccess2) {
                        // Navigate to the next fragment only on success
                        main.loginToHomepage(view);
                    } else {
                        // Handle failure case if needed
                        main.loginToInfo(view);
                        Toast.makeText(getActivity(), "You have to fill all the information first", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Handle failure case if needed
                Toast.makeText(getActivity(), "Failed login. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

    }

}