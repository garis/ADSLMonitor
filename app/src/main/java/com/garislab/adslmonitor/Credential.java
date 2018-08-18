package com.garislab.adslmonitor;



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
        String userSSH;
        String passwordSSH;
        String userSH;
        String passwordSH;

        public abstract void setAddress(String str);
        public abstract void setUserSSH(String str);
        public abstract void setPasswordSSH(String str);
        public abstract void setUserSH(String str);
        public abstract void setPasswordSH(String str);

        public abstract String getAddress();
        public abstract String getUserSSH();
        public abstract String getPasswordSSH();
        public abstract String getUserSH();
        public abstract String getPasswordSH();
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
                            final EditText EdTxtAddress = (EditText) getDialog().findViewById(R.id.address);
                            final EditText EdTxtUserSSH = (EditText) getDialog().findViewById(R.id.username_ssh);
                            final EditText EdTxtPasswordSSH = (EditText) getDialog().findViewById(R.id.password_ssh);
                            final EditText EdTxtUserSH = (EditText) getDialog().findViewById(R.id.username_sh);
                            final EditText EdTxtPasswordSH = (EditText) getDialog().findViewById(R.id.password_sh);
                            address = EdTxtAddress.getText().toString();
                            userSSH = EdTxtUserSSH.getText().toString();
                            passwordSSH = EdTxtPasswordSSH.getText().toString();
                            userSH = EdTxtUserSH.getText().toString();
                            passwordSH = EdTxtPasswordSH.getText().toString();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            address = "null";
                            userSSH = "null";
                            passwordSSH = "null";
                            userSH = "null";
                            passwordSH = "null";
                            dialog.dismiss();
                        }
                    });

            View content = inflater.inflate(R.layout.credential, null);
            builder.setView(content);
            final EditText EdTxtaddress = (EditText) content.findViewById(R.id.address);
            final EditText EdTxtuserSSH = (EditText) content.findViewById(R.id.username_ssh);
            final EditText EdTxtpasswordSSH = (EditText) content.findViewById(R.id.password_ssh);
            final EditText EdTxtuserSH = (EditText) content.findViewById(R.id.username_sh);
            final EditText EdTxtpasswordSH = (EditText) content.findViewById(R.id.password_ssh);

            EdTxtaddress.setText(address);
            EdTxtuserSSH.setText(userSSH);
            EdTxtpasswordSSH.setText(passwordSSH);
            EdTxtuserSSH.setText(userSH);
            EdTxtpasswordSSH.setText(passwordSH);

            return builder.create();
        }

        public void setAddress(String str){
            address=str;
        }

        public void setUserSSH(String str){
            userSSH=str;
        }

        public void setPasswordSSH(String str){
            passwordSSH=str;
        }

        public void setUserSH(String str){
            userSH=str;
        }

        public void setPasswordSH(String str){
            passwordSH=str;
        }

        public String getAddress(){
            return address;
        }

        public String getUserSSH(){
            return userSSH;
        }

        public String getPasswordSSH(){
            return passwordSSH;
        }

        public String getUserSH(){
            return userSH;
        }

        public String getPasswordSH(){
            return passwordSH;
        }
}