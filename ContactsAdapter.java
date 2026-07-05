package com.example.hershield;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contact> contactList;
    private Context context;

    public ContactsAdapter(Context context, List<Contact> contactList) {
        this.context=context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        holder.tvFname.setText(contact.getFname());
        holder.tvLname.setText(contact.getLname());
        holder.tvPhone.setText(contact.getPhone());

        holder.editContact.setOnClickListener(v -> {
            Intent i = new Intent(context, EditContactActivity.class);
            i.putExtra("id",contact.getId());
            i.putExtra("fname", contact.getFname());
            i.putExtra("lname", contact.getLname());
            i.putExtra("phone", contact.getPhone());
            context.startActivity(i);
        });

        holder.itemView.setOnLongClickListener(v -> {
            showContextMenu(contact, holder.getAdapterPosition());
            return true;
        });
    }

    private void showContextMenu(Contact contact, int position) {
        String[] options = {"Call Now", "Delete Contact"};
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(contact.getFname() + " " + contact.getLname())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Call Now
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(android.net.Uri.parse("tel:" + contact.getPhone()));
                            context.startActivity(callIntent);
                            break;

                        case 1: // Delete Contact
                            deleteContact(contact, position);
                            break;
                    }
                })
                .show();
    }
    private void deleteContact(Contact contact, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Contact");

        // Set message with red color
        TextView message = new TextView(context);
        message.setText("Are you sure you want to delete " + contact.getFname() + " " + contact.getLname() + "?");
        message.setTextColor(android.graphics.Color.RED);
        message.setPadding(50, 40, 50, 40);
        message.setTextSize(16f);
        builder.setView(message);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Delete contact
            ContactDBHelper dbHelper = new ContactDBHelper(context);
            boolean deleted = dbHelper.deleteContact(contact.getId());
            if (deleted) {
                contactList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, contactList.size());
                Toast.makeText(context, "Contact Deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete contact", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Make buttons bold
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, android.graphics.Typeface.BOLD);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.RED);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, android.graphics.Typeface.BOLD);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.RED);
    }



    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvFname, tvLname, tvPhone;
        ImageButton editContact;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFname = itemView.findViewById(R.id.tvFname);
            tvLname = itemView.findViewById(R.id.tvLname);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            editContact = itemView.findViewById(R.id.editContact);
        }
    }
}