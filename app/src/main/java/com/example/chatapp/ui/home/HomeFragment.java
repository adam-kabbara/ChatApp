package com.example.chatapp.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.example.chatapp.R;
import com.example.chatapp.Utils;
import com.example.chatapp.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context context;
    private String contactsFileName;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        context = requireActivity().getApplicationContext();
        contactsFileName = getResources().getString(R.string.contacts_file_name);

       // final TextView homeView = binding.textHome;
       // homeView.setText("this is the contact page");
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            Contact[] contacts = readContacts();
            if (contacts != null) {
                for (Contact contact : contacts) {
                    addContactBox(contact.getName(), contact.getPfpUrl());
                }
            }
        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Custom Methods
    public void addContactBox(String name, String pfpUrl){
        View contactView = getLayoutInflater().inflate(R.layout.contact_box, null);
        ImageView pfpView = contactView.findViewById(R.id.pfp);
        TextView nameView = contactView.findViewById(R.id.contactNameTextView);
        // this is a cache image viewer thing todo make sure that the image updates if it changes in the server
        Glide.with(this)// https://stackoverflow.com/questions/33443146/remove-image-from-cache-in-glide-library
                .load(pfpUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_menu_gallery)
                .into(pfpView);

        nameView.setText(name);
        binding.contactScrollable.addView(contactView);
    }

    public Contact[] readContacts() throws FileNotFoundException, JSONException {
        System.out.println(context.getFilesDir()+contactsFileName);
        File file = new File(context.getFilesDir(), contactsFileName);
        if (file.exists()){
            JSONArray contactsJSON = new JSONArray(Utils.loadJSONFromAsset(context, contactsFileName));
            Contact[] contactsArray = new Contact[contactsJSON.length()];
            for (int i=0; i<contactsJSON.length(); i++){
                JSONObject c = contactsJSON.getJSONObject(i);
                contactsArray[i] = new Contact(c.getInt("id"), c.getString("email"), c.getString("name"), c.getString("pfp_url"));
            }
            return contactsArray;
        }
        return null;
    }
}