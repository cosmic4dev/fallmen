package cosmic.com.firstchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import cosmic.com.firstchat.Model.User;

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
                            public void onComplete(@NonNull final Task<AuthResult> task) {
//
                                final String uid= task.getResult().getUser().getUid();

                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();

                                task.getResult().getUser().updateProfile(userProfileChangeRequest);

                                    FirebaseStorage.getInstance().getReference()
                                            .child( "userImages" ).child( uid ).putFile(imageUri )
                                          .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                              @Override
                                              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                  //사용법이 바뀜 그리고 업로드 경로와 다운로드 경로는 다르다.
                                                  //The getDownloadUrl method has now been depreciated in the new firebase update. Instead use the following method.

                                                  FirebaseStorage.getInstance().getReference().child( "userImages" ).child( uid )
                                                          .getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                                                      @Override
                                                      public void onSuccess(Uri uri) {
                                                          String imageUrl = uri.toString();
                                                          Log.d( "TAG","이미지 Url: "+ imageUrl );
//
                                                          User user=new User();
                                                          user.userName=name.getText().toString();
                                                          user.profileImageUrl=imageUrl;
                                                          user.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                          FirebaseDatabase.getInstance().getReference().child( "users" ).child(uid).setValue( user );
                                                          SignupActivity.this.finish();

                                                      }
                                                  } );


                                              }
                                          } );


                            }
                        } );

            }
        } );



    }
}
