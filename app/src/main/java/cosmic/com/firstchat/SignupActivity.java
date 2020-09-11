package cosmic.com.firstchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignupActivity extends AppCompatActivity {

    private EditText email;
    private EditText name;
    private EditText password;
    private Button signupbtn;
    private FirebaseAuth mAuth;
    private ImageView profile;
    private Uri imageUri;
    public static final int PICK_FROM_ALBUM=10;
    private StorageReference mStorageRef;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==PICK_FROM_ALBUM &&resultCode==RESULT_OK){
            profile.setImageURI( data.getData() );
            imageUri=data.getData();//이미지 경로 원본
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_signup );

        email=findViewById( R.id.signup_edittext_email );
        name=findViewById( R.id.signup_edittext_name );
        password=findViewById( R.id.signup_edittext_password );
        signupbtn=findViewById( R.id.submit_btn );
        profile=findViewById( R.id.iv_profile );

        profile.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType( MediaStore.Images.Media.CONTENT_TYPE );
                startActivityForResult( intent,PICK_FROM_ALBUM );
            }
        } );

        signupbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(email.getText().toString()==null
                ||name.getText().toString()==null||password.getText().toString()==null||imageUri==null){
                    return;
                }

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword( email.getText().toString(),password.getText().toString() )
                        .addOnCompleteListener( SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    Toast.makeText( getApplicationContext(), "성공", Toast.LENGTH_SHORT ).show();


                                    final String uid= task.getResult().getUser().getUid();
                                    FirebaseStorage.getInstance().getReference().child( "userImages" ).child( uid ).putFile(imageUri ).addOnCompleteListener( new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            @SuppressWarnings( "VisibleForTests" )
                                            String imageUrl = task.getResult().getTask().toString();

                                            User user=new User();
                                            user.userName=name.getText().toString();
                                            user.profileImageUrl=imageUrl;

                                            FirebaseDatabase.getInstance().getReference().child( "users" ).child(uid).setValue( user );
                                        }
                                    } );



                                    SignupActivity.this.finish();


//                                }else{
//                                    Log.w("TAG", "signInWithEmail:failure", task.getException());
//                                    Toast.makeText(getApplicationContext(), "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
//                                }
                            }
                        } );

            }
        } );



    }
}
