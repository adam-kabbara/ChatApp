package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.chatapp.ui.home.Contact;
import com.example.chatapp.ui.new_contact.NewContactFragment;
import com.example.chatapp.ui.home.HomeFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Context context;
    private String contactsFileName;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount signedInAccount;
    private NavigationView navigationView;
    private boolean viewIsAtHome = true;
    public ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        createRequest(); // init mGoogleSignInClient
        signedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        contactsFileName = signedInAccount.getId()+"-"+getResources().getString(R.string.contacts_file_name);
        navigationView = (NavigationView) findViewById(R.id.nav_view);


        setSupportActionBar(binding.appBarMain.toolbar);

        navigationView.setNavigationItemSelectedListener(this);
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
        if (item.getItemId() == R.id.nav_sign_out)
            signOut();
        else // handel navigation
            displayView(item.getItemId());
        return true;
    }
    public void displayView(int viewId) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, binding.appBarMain.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState(); // animate hamburger btn

        switch (viewId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                viewIsAtHome = true;
                break;
            case R.id.nav_new_contact:
                fragment = new NewContactFragment();
                viewIsAtHome = false;
                title = "New Contact";
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
        navigationView.setCheckedItem(viewId);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (!viewIsAtHome) { //if the current view is not the home fragment
            displayView(R.id.nav_home); //display the home fragment
        } else {
            moveTaskToBack(true);  //If view is in home fragment, exit application
        }
    }

    // shared methods
    public String loadJSONFromAsset(Context context, String fileName) throws FileNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e); // Error occurred when opening raw file for reading.
        }
        return stringBuilder.toString();
    }
}