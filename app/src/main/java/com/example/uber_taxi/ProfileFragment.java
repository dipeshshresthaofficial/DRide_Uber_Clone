package com.example.uber_taxi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    FirebaseUser user;
    TextView name;
    ProgressDialog dialog;
    Button changePassword,deleteAccount,updateProfile;
    FirebaseAuth auth;

    EditText newName,newAddress,nPhone,country,DOB;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_profile,container,false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        dialog = new ProgressDialog((Menu)getActivity());
        newName = (EditText)view.findViewById(R.id.name);
        newName.setText(user.getDisplayName());
        newAddress = (EditText)view.findViewById(R.id.address);
        nPhone = (EditText)view.findViewById(R.id.phoneNo);
        country = (EditText)view.findViewById(R.id.country);
        DOB = (EditText)view.findViewById(R.id.date);

        getData();

        changePassword =(Button)view.findViewById(R.id.change);
        deleteAccount =(Button)view.findViewById(R.id.delete);
        updateProfile =(Button)view.findViewById(R.id.updateProfile);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.textToSpeech.speak("Are you sure you want to delete this account permanently.", TextToSpeech.QUEUE_FLUSH,null);
                deactivate();
                //dothis();
            }
        });
        auth = FirebaseAuth.getInstance();
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change();
            }
        });

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateName();
            }
        });
        return view;
    }

    public void change(){

        dialog.setMessage("Changing password....");
        dialog.show();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Toast.makeText((Menu)getActivity(),"user name is "+user.getEmail(),Toast.LENGTH_SHORT).show();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = user.getEmail();

        //sending an mail to customer email for changing password
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText((Menu)getActivity(),"send verification mail to users",Toast.LENGTH_SHORT).show();
                            MainActivity.textToSpeech.speak("A verification mail is send to your email address.", TextToSpeech.QUEUE_FLUSH,null);
                        }

                        else {
                            dialog.dismiss();
                            Toast.makeText((Menu)getActivity(),"the email doesnot exists",Toast.LENGTH_SHORT).show();

                        }
                    }
                });


    }

    //function to show alert dialog
    public void deactivate(){

        final AlertDialog.Builder alert = new AlertDialog.Builder((Menu)getActivity());
        View mview = getLayoutInflater().inflate(R.layout.layout_dialog,null); //getting the layout dialog that we have created for dialog

        final TextView text = (TextView)mview.findViewById(R.id.texto);
        Button cancel = (Button)mview.findViewById(R.id.cancel);
        Button ok = (Button)mview.findViewById(R.id.fine);

        alert.setView(mview);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.textToSpeech.speak("you have cancelled the process.", TextToSpeech.QUEUE_FLUSH,null);
                alertDialog.dismiss();//removing the dialog

            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.textToSpeech.speak("Deleting your account.", TextToSpeech.QUEUE_FLUSH,null);
                //displayNotification();
                alertDialog.dismiss();//removing the dialog
                deleteAcc();
            }
        });
        alertDialog.show();
    }

    public void deleteAcc() {

        dialog.setMessage("Deactivating account please wait for some time....");
        dialog.show();
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Toast.makeText((Menu) getActivity(), "user name is " + user.getEmail(), Toast.LENGTH_SHORT).show();
        dialog.setMessage("Deletiing account please wait..");
        dialog.show();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText((Menu) getActivity(), "user account deleted", Toast.LENGTH_SHORT).show();
                            Intent myIntent = new Intent((MainActivity)getActivity(), UserLoginActivity.class);
                            startActivity(myIntent);

                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText((Menu) getActivity(), "user account not deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

        //updating the name of user
        public void updateName(){

//            FirebaseAuth auth = FirebaseAuth.getInstance();
//            FirebaseUser user = auth.getCurrentUser();
//
//            user.sendEmailVerification()
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                //Log.d(TAG, "Email sent.");
//                                Toast.makeText((Menu) getActivity(), "user email updated", Toast.LENGTH_SHORT).show();
//                                Intent myIntent = new Intent((Menu)getActivity(), UserLoginActivity.class);
//                                startActivity(myIntent);
//
//                            }
//                            else{
//                                Toast.makeText((Menu) getActivity(), "user email not updated", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });

            dialog.setMessage("Updating users details please wait for some time....");
            dialog.show();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName.getText().toString())
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText((Menu) getActivity(), "user name updated", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText((Menu) getActivity(), "user name not updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            String dphone = nPhone.getText().toString();
            String dname = newName.getText().toString();
            String daddr = newAddress.getText().toString();
            String ddob = DOB.getText().toString();
            String coun = country.getText().toString();

            if(!dname.equals("")&&!dphone.equals("")&&!daddr.equals("")&&!ddob.equals("")&& !coun.equals("")){


                //inserting into database
                FirebaseDatabase rootNode = FirebaseDatabase.getInstance();//include all the tables of the database
                DatabaseReference reference = rootNode.getReference("UserInfo");

                UserHelperClass m1 = new UserHelperClass(dphone,dname,daddr,coun,ddob);
                //stored in database
                //reference.setValue("First Data stored");
                reference.child(user.getUid()).setValue(m1);
                Toast.makeText((Menu) getActivity(), "user data is updated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                Intent myIntent = new Intent((Menu)getActivity(), UserMapActivity.class);
                startActivity(myIntent);
            }
            else{
                dialog.dismiss();
                MainActivity.textToSpeech.speak("Please enter all the fields given above.", TextToSpeech.QUEUE_FLUSH,null);
                Toast.makeText((Menu) getActivity(), "user data not updated", Toast.LENGTH_SHORT).show();
            }
        }

    public void getData(){

        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("UserInfo");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot data: dataSnapshot.getChildren()){
                    //data.getKey();
                    if(user.getUid().equals(data.getKey())){
                        UserHelperClass post = dataSnapshot.child(user.getUid()).getValue(UserHelperClass.class);
                        newAddress.setText(post.address.toString());
                        country.setText(post.country.toString());
                        nPhone.setText(post.mobile.toString());
                        DOB.setText(post.date.toString());

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        };
        myRef.addValueEventListener(postListener);

    }
}
