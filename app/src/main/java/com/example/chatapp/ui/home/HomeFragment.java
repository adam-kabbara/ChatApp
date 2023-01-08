package com.example.chatapp.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageHelper;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentHomeBinding;

import org.w3c.dom.Text;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

       // final TextView homeView = binding.textHome;
       // homeView.setText("this is the contact page");
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        for (int i=0; i<20;i++)
            addContactBox("tester"+i, R.mipmap.ic_launcher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Custom Methods
    public void addContactBox(String name, int pfpId){
        View contactView = getLayoutInflater().inflate(R.layout.contact_box, null);
        ImageView pfpView = contactView.findViewById(R.id.pfp);
        TextView nameView = contactView.findViewById(R.id.contactNameTextView);

        pfpView.setImageResource(pfpId); // placeholder img
        nameView.setText(name); // placeholder txt
        binding.contactScrollable.addView(contactView);
    }
}