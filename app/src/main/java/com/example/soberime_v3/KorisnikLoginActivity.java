package com.example.soberime_v3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class KorisnikLoginActivity extends AppCompatActivity {
    private TextView btnNajava,btnRegistarcija;
    private EditText txtEmail, txtPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_korisnik_login);
        mAuth =FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(KorisnikLoginActivity.this, KorisnikMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };


        btnNajava = (TextView) findViewById(R.id.najavaKorisnik);
        btnRegistarcija = (TextView) findViewById(R.id.registacijaKorisnik);
        txtEmail = (EditText) findViewById(R.id.emailKorisnik);
        txtPassword = (EditText) findViewById(R.id.passwordKorisnik);

        btnRegistarcija.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtEmail.getText().toString().equals("") && !txtPassword.getText().toString().equals("")) {
                    final String email = txtEmail.getText().toString();
                    final String pass = txtPassword.getText().toString();

                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(KorisnikLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(KorisnikLoginActivity.this, "Грешка при регистрација", Toast.LENGTH_SHORT).show();
                            } else {
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Korisnici").child(user_id);
                                current_user_db.setValue(true);
                            }
                        }
                    });
                }else{
                    Toast.makeText(KorisnikLoginActivity.this, "Грешка при регистрација", Toast.LENGTH_SHORT).show();

                }
            }
        });



        btnNajava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtEmail.getText().toString().equals("") && !txtPassword.getText().toString().equals("")) {
                    final String email = txtEmail.getText().toString();
                    final String pass = txtPassword.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(KorisnikLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(KorisnikLoginActivity.this, "Грешка при најава", Toast.LENGTH_SHORT).show();
                            } else {

                            }
                        }
                    });
                }else{
                    Toast.makeText(KorisnikLoginActivity.this, "Грешка при најава", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }

}
