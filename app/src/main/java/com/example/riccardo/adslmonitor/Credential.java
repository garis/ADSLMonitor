package com.example.riccardo.adslmonitor;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

    abstract class CredentialDialog extends DialogFragment{
        String address;
        String user;
        String password;

        public abstract void setAddress(String str);
        public abstract void setUser(String str);
        public abstract void setPassword(String str);
        public abstract String getAddress();
        public abstract String getUser();
        public abstract String getPassword();
    }

    public class Credential extends CredentialDialog {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.credential, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            final EditText etAddress = (EditText) getDialog().findViewById(R.id.address);
                            final EditText etUser = (EditText) getDialog().findViewById(R.id.username);
                            final EditText etPassword = (EditText) getDialog().findViewById(R.id.password);
                            address = etAddress.getText().toString();
                            user = etUser.getText().toString();
                            password = etPassword.getText().toString();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            address = "null";
                            user = "null";
                            password = "null";
                            dialog.dismiss();
                        }
                    });

            View content = inflater.inflate(R.layout.credential, null);
            builder.setView(content);
            final EditText etAddress = (EditText) content.findViewById(R.id.address);
            final EditText etUser = (EditText) content.findViewById(R.id.username);
            final EditText etPassword = (EditText) content.findViewById(R.id.password);
            etAddress.setText(address);
            etUser.setText(user);
            etPassword.setText(password);

            return builder.create();
        }

        public void setAddress(String str){
            address=str;
        }

        public void setUser(String str){
            user=str;
        }

        public void setPassword(String str){
            password=str;
        }

        public String getAddress(){
            return address;
        }

        public String getUser(){
            return user;
        }

        public String getPassword(){
            return password;
        }
}