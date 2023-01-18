package com.example.chatapp.ui.chat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Contact;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.databinding.FragmentChatBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private GoogleSignInAccount signedInAccount;
    private MainActivity mainActivity;
    private Context context;
    private Contact receiverContact;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity)requireContext();
        context = requireActivity().getApplicationContext();
        signedInAccount = GoogleSignIn.getLastSignedInAccount(context);
        ImageView sendButton = binding.sendButton;
        EditText editText = binding.plainTextInput;
        ScrollView scrollView = binding.scrollView;

        sendButton.setOnClickListener(view -> {
            String message = String.valueOf(editText.getText());
            addMessageBox(message, true);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
            sendMessageFire(message);
            editText.setText("");
        });

        initPage();
        return root;
    }

    @Override // not sure if needed
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initPage(){
        getParentFragmentManager().setFragmentResultListener
                ("requestKey2", this, (requestKey, bundle) -> {
                    receiverContact = (Contact) bundle.getSerializable("pressedContactKey");
                    mainActivity.getSupportActionBar().setTitle(receiverContact.getName());
                });
    }

    private void addMessageBox(String message, boolean isSender){
        View messageView = getLayoutInflater().inflate(R.layout.text_message, null);
        TextView textView = messageView.findViewById(R.id.textViewMessage);
        textView.setText(message);
        if (isSender){
            textView.setBackgroundColor(getResources().getColor(R.color.teal_200));
            //messageView.setGravity(Gravity.END); todo fix so that sender messages are at end
        }
        else{
            textView.setBackgroundColor(getResources().getColor(R.color.peach_200));
        }
        binding.linearLayout.addView(messageView);
    }

    private void sendMessageFire(String message){
        HashMap<String, Object> metaData = new HashMap<>();
        HashMap<String, Object> data = new HashMap<>();
        data.put("sender", signedInAccount.getId());
        data.put("message", message);
        data.put("time", FieldValue.serverTimestamp());
        metaData.put(UUID.randomUUID().toString(), data); // uuid not 100% unique
        mainActivity.db.collection("messages").document(receiverContact.getId())
                .update(metaData).addOnFailureListener(e -> mainActivity.db.collection("messages")
                         .document(receiverContact.getId()).set(metaData));
    }
}