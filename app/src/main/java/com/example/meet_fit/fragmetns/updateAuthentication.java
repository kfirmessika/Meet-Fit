package com.example.meet_fit.fragmetns;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meet_fit.R;
import com.example.meet_fit.activities.MainActivity;
import com.example.meet_fit.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link updateAuthentication#newInstance} factory method to
 * create an instance of this fragment.
 */
public class updateAuthentication extends Fragment {


    private Button btnUpdateCredentials;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public updateAuthentication() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment updateAuthentication.
     */
    // TODO: Rename and change types and number of parameters
    public static updateAuthentication newInstance(String param1, String param2) {
        updateAuthentication fragment = new updateAuthentication();
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
        View rootView = inflater.inflate(R.layout.fragment_update_authentication, container, false);
        btnUpdateCredentials = rootView.findViewById(R.id.btnUpdateCredentials);

        MainActivity main = (MainActivity) getActivity();

        btnUpdateCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String etOldEmail = ((EditText)rootView.findViewById(R.id.etOldEmail)).getText().toString();
                String etOldPassword = ((EditText)rootView.findViewById(R.id.etOldPassword)).getText().toString();
                String etNewEmail = ((EditText)rootView.findViewById(R.id.etNewEmail)).getText().toString();
                String etNewPassword = ((EditText)rootView.findViewById(R.id.etNewPassword)).getText().toString();



                if(etNewEmail == null ||etNewEmail.isEmpty() )
                { etNewEmail = etOldEmail;}

                if(etNewPassword == null ||etNewPassword.isEmpty() )
                { etNewPassword = etOldPassword;}

                if((etNewPassword == null || etNewPassword.isEmpty()) && (etNewEmail == null || etNewEmail.isEmpty()))
                {
                    Toast.makeText(getContext(),"Please enter new email or password or both",Toast.LENGTH_LONG).show();
                    return ;
                }
                User newUser = new User(etNewEmail,etNewPassword);
                User oldUser = new User(etOldEmail,etOldPassword);

                assert main != null;
                main.updateCredentials(newUser,oldUser);
            }
        });
        return rootView;
    }

}