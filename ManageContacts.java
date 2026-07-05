package com.example.hershield;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ManageContacts extends Fragment {

    private RecyclerView recyclerViewContacts;
    private ContactsAdapter contactsAdapter;
    private List<Contact> contactList;
    private ContactDBHelper dbHelper;
    Button btnAddContact;
    Button btnWhatsAppBroadcast;

    public ManageContacts() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manage_contacts, container, false);

        recyclerViewContacts = root.findViewById(R.id.recyclerContacts);
        btnAddContact = root.findViewById(R.id.btnAddContact);
        btnWhatsAppBroadcast = root.findViewById(R.id.btnWhatsAppBroadcast);

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new ContactDBHelper(requireContext());
        contactList = new ArrayList<>();

        loadContactsFromDB();  // ✅ Load contacts here

        contactsAdapter = new ContactsAdapter(requireContext(), contactList);
        recyclerViewContacts.setAdapter(contactsAdapter);

        btnAddContact.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), AddContactActivity.class);
            startActivity(i);
        });

        // WhatsApp Broadcast button
        btnWhatsAppBroadcast.setOnClickListener(v -> {
            if (contactList == null || contactList.isEmpty()) {
                Toast.makeText(requireContext(),
                        "No contacts added! Please add emergency contacts first.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            WhatsAppHelper whatsAppHelper = new WhatsAppHelper(requireContext());

            if (!whatsAppHelper.isWhatsAppInstalled()) {
                Toast.makeText(requireContext(),
                        "WhatsApp is not installed on this device!",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Send to all contacts via WhatsApp
            whatsAppHelper.createWhatsAppBroadcast(contactList);
        });

        return root;
    }

    private void loadContactsFromDB() {
        contactList.clear();
        Cursor cursor = dbHelper.getAllContacts();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactDBHelper.COLUMN_ID));
                String fname = cursor.getString(cursor.getColumnIndexOrThrow(ContactDBHelper.COLUMN_FNAME));
                String lname = cursor.getString(cursor.getColumnIndexOrThrow(ContactDBHelper.COLUMN_LNAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactDBHelper.COLUMN_CONTACT));

                contactList.add(new Contact(id, fname, lname, phone));

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContactsFromDB();
        if (contactsAdapter != null) {
            contactsAdapter.notifyDataSetChanged();
        }
    }
}
