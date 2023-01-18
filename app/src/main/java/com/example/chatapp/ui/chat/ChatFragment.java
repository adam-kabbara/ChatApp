package com.example.chatapp.ui.chat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatapp.Contact;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentChatBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;


public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private GoogleSignInAccount signedInAccount;
    private MainActivity mainActivity;
    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity)requireContext();
        context = requireActivity().getApplicationContext();
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context);

        getParentFragmentManager().setFragmentResultListener
                ("requestKey2", this, (requestKey, bundle) -> {
                    Contact newContact = (Contact) bundle.getSerializable("pressedContactKey");
                    binding.textView3.setText(newContact+"");
                });

        return root;
    }

    @Override // not sure if needed
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}