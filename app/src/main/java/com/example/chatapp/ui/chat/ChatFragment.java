package com.example.chatapp.ui.chat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private GoogleSignInAccount signedInAccount;
    private MainActivity mainActivity;
    private Context context;
    private Contact receiverContact;
    private String messageFileName;

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
        initPage();

        sendButton.setOnClickListener(view -> {
            String message = String.valueOf(editText.getText());
            addMessageBox(message, true);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
            sendMessageFire(message);
            try {
                saveMessageLocally(message, true);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            editText.setText("");
        });

        mainActivity.db.collection("messages").document(Objects.requireNonNull(signedInAccount.getId()))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.out.println("Listen failed."+ e);
                            return;
                        }

                        String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                                ? "Local" : "Server";

                        if (snapshot != null && snapshot.exists()) {
                            Map<String, Object> data = Objects.requireNonNull(snapshot.getData());
                            System.out.println(source + " data: " + data);
                            for (Object messageData: data.values()){
                                System.out.println("jjj: "+messageData); // todo finish here
                            }
                            //addMessageBox(receivedMessage, false);
                          //  try {
                           //     saveMessageLocally(receivedMessage,false);
                          //  } catch (IOException | JSONException ex) {
                           //     ex.printStackTrace();
                           // }
                            // todo delete message from db after saved locally
                        } else {
                            System.out.println(source + " data: null");
                        }
                    }
                });

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
                    messageFileName = signedInAccount.getId()+"-"+receiverContact.getId()+"-"+getResources().getString(R.string.messages_file_name);
                    try {
                        JSONArray data = new JSONArray(mainActivity.loadJSONFromAsset(context, messageFileName));
                        for (int i=0; i<data.length(); i++){
                            JSONObject msgObj = data.getJSONObject(i);
                            addMessageBox(msgObj.getString("message"), msgObj.getBoolean("is_sender"));
                        }
                    } catch (JSONException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
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

    private void saveMessageLocally(String message, boolean isSender) throws IOException, JSONException {
        File file = new File(context.getFilesDir(), messageFileName);
        JSONArray data;
        if (file.exists())
            data = new JSONArray(mainActivity.loadJSONFromAsset(context, messageFileName));
        else
            data = new JSONArray();
        JSONObject messageJson = new JSONObject();
        messageJson.put("time", FieldValue.serverTimestamp());
        messageJson.put("message", message);
        messageJson.put("is_sender", isSender);
        data.put(messageJson);

        String userString = data.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

}