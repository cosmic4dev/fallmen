package cosmic.com.firstchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText password;
    private Button login;
    private Button signup;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        firebaseRemoteConfig=FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString( "splash_background" );
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        login=findViewById( R.id.loginActivity_button_login );
        signup=findViewById( R.id.loginActivity_button_join  );
        id=findViewById( R.id.loginActivity_edittext_id );
        password=findViewById( R.id.loginActivity_edittext_password );

        login.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEvent();
            }
        } );

        signup.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent( LoginActivity.this,SignupActivity.class ) );
            }
        } );

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                  FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user !=null){
                    Intent intent= new Intent(LoginActivity.this,SecondActivity.class);
                    startActivity( intent );
                    finish();
                }else{

                }
            }
        };
    }

    void loginEvent(){
        firebaseAuth.signInWithEmailAndPassword( id.getText().toString(),password.getText().toString() )
              .addOnCompleteListener( new OnCompleteListener<AuthResult>(){

                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {
                      if(!task.isSuccessful()){
                          Toast.makeText( LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT ).show();
                      }
                  }
              } );
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener( authStateListener );
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener( authStateListener );
    }
}
