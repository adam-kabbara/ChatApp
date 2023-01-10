package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.chatapp.ui.chat.ChatFragment;
import com.example.chatapp.ui.home.HomeFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Context context;
    private String contactsFileName;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount signedInAccount;
    private String userDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        contactsFileName = getResources().getString(R.string.contacts_file_name);
        mAuth = FirebaseAuth.getInstance();
        createRequest(); // init mGoogleSignInClient
        signedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        userDirectory = String.valueOf(context.getFilesDir());
//        userDirectory = context.getFilesDir()+"/"+signedInAccount.getId();
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Creating New Contact", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    createNewContact();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        setNavigationViewListener();
        displayView(R.id.nav_home);
        // Set nav bar header info
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        TextView currentUserNameTextView = headerView.findViewById(R.id.currentUserName);
        TextView currentUserEmailTextView = headerView.findViewById(R.id.currentUserEmail);
        currentUserNameTextView.setText(signedInAccount.getDisplayName());
        currentUserEmailTextView.setText(signedInAccount.getEmail());
        //todo put pfp - when first login if no firebase accoutn already use google pfp as chatapp pfp
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Custom Methods
    private void createNewContact() throws JSONException, IOException { // todo fix this so add contact correctly
        File file = new File(userDirectory, contactsFileName);
        JSONArray data;
        if (file.exists())
            data = new JSONArray(Utils.loadJSONFromAsset(context, contactsFileName));
        else
            data = new JSONArray();
        Random rand = new Random(); // for filler data todo fix
        JSONObject contact = new JSONObject();
        contact.put("id", signedInAccount.getId());
        contact.put("email", signedInAccount.getEmail());
        contact.put("name", signedInAccount.getDisplayName());
        contact.put("pfp_url", "https://picsum.photos/"+(rand.nextInt(100)+200));
        data.put(contact);

        String userString = data.toString();
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

    // firebase related - mostly for sign out
    private void signOut(){
        mAuth.signOut();
        mGoogleSignInClient.signOut(); // this line and above remove the account so that it doesnt auto log in later
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(),GoogleLoginActivity.class);
        startActivity(intent);
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // for drawer routes and btns
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        System.out.println("fuckkkkkkk");
        if (item.getItemId() == R.id.nav_sign_out)
            signOut();
        else // handel navigation
            displayView(item.getItemId());
        return true;
    }
    public void displayView(int viewId) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (viewId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_chat:
                fragment = new ChatFragment();
                title = "Chat";
                break;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_content_main, fragment);
            ft.commit();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
}