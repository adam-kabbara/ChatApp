package com.example.chatapp.ui.new_contact;

import static androidx.core.os.BundleKt.bundleOf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chatapp.ListViewAdapter;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentNewContactBinding;
import com.example.chatapp.Contact;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class NewContactFragment extends Fragment {

    private FragmentNewContactBinding binding;
    private Context context;
    private ListView listView;
    private ListViewAdapter adapter;
    private SearchView searchView;
    private MainActivity mainActivity;
    private DialogInterface.OnClickListener dialogClickListener;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNewContactBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity)requireContext();
        context = requireActivity().getApplicationContext();
        //assert getArguments() != null;
        mainActivity.db.collection("users").get() // this is bad cause it loads all documents
                .addOnCompleteListener(task -> { // todo add lazyloading
                    ArrayList<Contact> contactsList = new ArrayList<>();
                    if (task.isSuccessful()) {
                        // make the users in contact obj so use less code and not code for finding string
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            contactsList.add(new Contact(document.getId(), (String) data.get("email"),
                                    (String) data.get("name"), (String) data.get("pfp_url")));
                        }
                        initListView(contactsList);
                    }
                    else {
                        System.out.println("Error getting documents: "+ task.getException());
                    }
                });


       return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initListView(ArrayList<Contact> contactsArrayList){
        listView = binding.newContactListView;
        // Pass results to ListViewAdapter Class
        adapter = new ListViewAdapter(context, contactsArrayList, true); //todo get contacts form firbase users
        listView.setAdapter(adapter);
        searchView = binding.newContactSearchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return false; // true
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                adapter.filter(text);
                return false;
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) ->
                dialogBoxSetup(position, contactsArrayList));
    }

    private void dialogBoxSetup(int position, ArrayList<Contact> contactsArrayList){
        dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Bundle result = new Bundle();
                    result.putSerializable("newContactKey", contactsArrayList.get(position));
                    getParentFragmentManager().setFragmentResult("requestKey", result);                    mainActivity.displayView(R.id.nav_home);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Select yes to display toast message and no to dismiss the dialog ?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}