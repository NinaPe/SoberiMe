package com.example.soberime_v3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KorisnikOpciiActivity extends AppCompatActivity {

    private EditText txtIme, txtTelefon;
    private Button btnPotvrdi, btnNazad;

    private ImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference korisnikDatabase;

    private String userId;
    private String ime = "";
    private String telefon = "";
    private Uri img;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_korisnik_opcii);

        txtIme = (EditText) findViewById(R.id.imeKorisnik);
        txtTelefon = (EditText) findViewById(R.id.telefonKorisnik);

        btnPotvrdi = (Button) findViewById(R.id.potvrda);
        btnNazad = (Button) findViewById(R.id.nazad);

        profileImage = (ImageView) findViewById(R.id.profileImage);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        korisnikDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Korisnici").child(userId);

        getUserInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        btnPotvrdi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfoKorisnici();
            }
        });

        btnNazad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

    }

    private void getUserInfo() {
        korisnikDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("name") != null) {
                        ime = map.get("name").toString();
                        txtIme.setText(ime);
                    }

                    if(map.get("phone") != null) {
                        telefon = map.get("phone").toString();
                        txtTelefon.setText(telefon);
                    }

                    if(map.get("profileImageUrl") != null) {
                        url = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(url).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveInfoKorisnici() {
        ime = txtIme.getText().toString();
        telefon = txtTelefon.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", ime);
        userInfo.put("phone", telefon);
        korisnikDatabase.updateChildren(userInfo);

        if(img != null) {
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), img);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> download = taskSnapshot.getStorage().getDownloadUrl();

                    while(!download.isComplete());

                    Uri url = download.getResult();

                    Map newImage = new HashMap();

                    newImage.put("profileImageUrl", url.toString());
                    korisnikDatabase.updateChildren(newImage);

                }
            });
        }

        finish();
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            img = imageUri;
            profileImage.setImageURI(img);
        }
    }
}
