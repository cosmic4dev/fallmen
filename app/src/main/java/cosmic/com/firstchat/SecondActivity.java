package cosmic.com.firstchat;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_second );

        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN ,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        BottomNavigationView bottomNavigationView = findViewById( R.id.bottomnavview );
        getSupportFragmentManager().beginTransaction().replace( R.id.frame,new PeopleFragment() ).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_people:
                        getSupportFragmentManager().beginTransaction().replace( R.id.frame,new PeopleFragment() ).commit();
                        return true;
                    case R.id.action_chat:
                        getSupportFragmentManager().beginTransaction().replace( R.id.frame,new ChatFragment() ).commit();
                        return true;
                    case R.id.action_account:
                        getSupportFragmentManager().beginTransaction().replace( R.id.frame,new AccountFragment() ).commit();
                        return true;
                }
                return false;
            }
        } );

        passPushTokenToServer();
    }

    void passPushTokenToServer(){


//        String token = FirebaseInstanceId.getInstance().getToken(  );   //deprecated

//test
        //test1
<<<<<<< HEAD
=======
        //test2
>>>>>>> secondman
        //so new it is.
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "getInstanceId failed", task.getException());
                            return;
                        }
                        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Map<String,Object> map = new HashMap<>();
                        map.put("pushToken",token);

                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);

                        Log.d("TAG","토큰푸쉬: " +token);
                    }
                });




    }
}
