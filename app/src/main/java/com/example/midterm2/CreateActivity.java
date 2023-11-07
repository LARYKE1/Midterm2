package com.example.midterm2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateActivity extends AppCompatActivity {
    private EditText etName;
    private EditText etPhone;
    private Button btnAdd;
    private Button btnBack;
    private ContactDao contactDao;
    private final Executor executor= Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        variables(); //Initialize the UI elements
        getDataToEdit(); // Get data to edit or add new contact
        //Button to get back to Main Activity
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(CreateActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
    /**
     * Initializes variables, including the database and UI elements.
     */
    protected void variables() {
        AppDatabase appDatabase = AppDatabase.getInstance(this);
        contactDao = appDatabase.contactDao();
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
    }

    /**
     * Attempts to add a new contact to the database with validation.
     * @return True if the contact was added successfully; false otherwise.
     */
    private boolean addContact() {
        try {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            if (name.isEmpty() || phone.isEmpty()) {
                throw new IllegalArgumentException("Please add a name and a phone number!");
            }

            if (!name.matches("^[a-zA-Z]+$")) {
                throw new IllegalArgumentException("Only letters for the Name");
            }
            if (!phone.matches("^[0-9]+$") || phone.length() != 10) {
                throw new IllegalArgumentException("Please add a valid 10-digit number");
            }

            contactDao.insertData(new Contact(name, formatPhoneNumber(phone)));
            return true;
        } catch (IllegalArgumentException e) {
            runOnUiThread(() -> {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
        return false;
    }

    /**
     * Retrieves data to edit an existing contact or create a new one.
     */
    private void getDataToEdit() {
        //get the contact selected from main activity
        Contact contact = (Contact) getIntent().getSerializableExtra("Contact");
        //check if the contact is null, if it is null that means it needs to add a new contact
        if (contact == null) {
            btnAdd.setOnClickListener(view -> {
                executor.execute(() -> {
                    if (addContact()) {
                        Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            });
        } else {
            etName.setText(contact.getName());
            etPhone.setText(contact.getPhone());
            btnAdd.setOnClickListener(view -> {
                // Update the contact with the new values
                contact.setName(etName.getText().toString());
                contact.setPhone(etPhone.getText().toString());
                // Update the contact in the database
                executor.execute(() -> {
                    contactDao.update(contact);
                });
                Intent intent = new Intent(CreateActivity.this, MainActivity.class);
                intent.putExtra("Contact", contact);
                setResult(RESULT_OK, intent);
                finish();
            });
        }
    }

    /**
     * Formats the phone number to the pattern "000-000-0000".
     * @param phoneNumber The input phone number.
     * @return The formatted phone number.
     */
    private String formatPhoneNumber(String phoneNumber) {
        String cleanPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

        if (cleanPhoneNumber.length() == 10) {
            return cleanPhoneNumber.substring(0, 3) + "-" +
                    cleanPhoneNumber.substring(3, 6) + "-" +
                    cleanPhoneNumber.substring(6);
        } else {
            return phoneNumber;
        }
    }
}