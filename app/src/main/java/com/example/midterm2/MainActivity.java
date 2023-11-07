package com.example.midterm2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ContactListener{

    private EditText etSearch;
    //Instance used to execute background tasks, it will be used for the database because it cannot be executed on main thread.
    private final Executor executor= Executors.newSingleThreadExecutor();
    private ContactDao contactDao;
    private ImageView addButton;
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private final List<Contact> contactList=new ArrayList<>();
    private static final int REQUEST_CODE=1;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        variables(); //initialize the UI elements
        //Set up the adapter
        contactAdapter = new ContactAdapter(getApplicationContext(), contactList, this);
        recyclerView.setAdapter(contactAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        //Retrieve and display all contacts using a background thread
        executor.execute(this::getAllData);
        //HHandle the "Add" button to navigate to the CreateActivity
        addButton.setOnClickListener(view -> {
            intent = new Intent(getApplicationContext(), CreateActivity.class);
            //the method explained down
            startForResult.launch(intent);
        });
        //Set up the text watcher for live search filtering
        //I do have a problem with this one, the adapter gets notificated for the new list
        //sometimes will not update the old list, so this caused to not refresh the adapter and the
        //list will not be updated in the case of edit or add of a new contact
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchFilter(editable.toString());
            }
        });

    }

    /**
     * Menu bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_context,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //Add contact, it does the same thing like the addButton
            case R.id.addNewContactMenu:
                intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivityIfNeeded(intent, REQUEST_CODE);
                return true;
            //Goes to the splash activity, the one it used then the program starts
            case R.id.Splash:
                intent = new Intent(getApplicationContext(), SplashScreen.class);
                startActivity(intent);
                return true;
            case R.id.Help:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setMessage("ARE YOU SURE YOU WANT TO CONTINUE?");
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    finish();
                });
                builder.setNegativeButton("NOOOO",((dialogInterface, i) -> {
                    intent=new Intent(MainActivity.this,SplashScreen.class);
                    startActivity(intent);
                }));
                AlertDialog dialog=builder.create();
                dialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Filter the contact list based on the user's search query and update the adapter
     *
     */
    private void searchFilter(String text) {
        List<Contact> newList = new ArrayList<>();
        //get every item from contactList and search if it contains the letters we used inside edit text
        for (Contact item : contactList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                //send to the new list
                newList.add(item);
            }
        }
        //adapter will be set to the new filter list and if it is empty will get redisplay the old one
        contactAdapter.filterList(newList);
        if (newList.isEmpty()) {
            executor.execute(() -> MainActivity.this.getAllData());
        }

    }

    protected void variables() {
        AppDatabase midtermDb = AppDatabase.getInstance(getApplicationContext());
        contactDao = midtermDb.contactDao();

        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.searchName);
        addButton = findViewById(R.id.addButtonMain);
    }

    /**
     * Retrieve all contacts from the database and update the contactList.
     */
    private void getAllData() {
        contactList.clear();
        contactList.addAll(contactDao.getAll());
        runOnUiThread(() -> {
            contactAdapter.notifyDataSetChanged();
        });
    }
    @Override
    public void receiveContact(Contact contact) {
        intent = new Intent(MainActivity.this, CreateActivity.class);
        intent.putExtra("Contact", contact);
        startForResult.launch(intent);
    }
    /**
     *
     * Delete the contact
     * First it will be displayed a dialog to make sure you want to delete the selected item
     */
    @Override
    public void deleteContact(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            executor.execute(() -> {
                contactDao.delete(position);
                getAllData();
            });

        });
        builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    /**
     * Handle the result from CreateActivity, and refreshing the contact list.
     */
    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), o -> {
                if (o != null && o.getResultCode() == RESULT_OK) {
                    executor.execute(this::getAllData);
                }
            });

}