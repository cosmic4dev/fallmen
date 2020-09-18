package cosmic.com.firstchat;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

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
//                    case R.id.action_account:
//                        getSupportFragmentManager().beginTransaction().replace( R.id.frame,new () ).commit();
//                        return true;
                }
                return false;
            }
        } );

        passPushTokenToServer();
    }

    void passPushTokenToServer(){
        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener( new OnCompleteListener<Instand>() )
        String token = FirebaseInstanceId.getInstance().getToken(  );
        Map<String,Object>map = new HashMap<>();
        map.put("pushToken",token);

        FirebaseDatabase.getInstance().getReference().child( "users" ).child( uid ).updateChildren( map );

    }
}
