package com.example.chatapp.ui.new_contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentNewContactBinding;

public class NewContactFragment extends Fragment {

    private FragmentNewContactBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNewContactBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

       return root; // figure out the difference beteen this file and chatviewmodel
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}