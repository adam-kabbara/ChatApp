package com.example.chatapp.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chatapp.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textChat;
        textView.setText("this is the chat fragment");//getText().observe(getViewLifecycleOwner(), textView::setText);
        return root; // figure out the difference beteen this file and chatviewmodel
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}