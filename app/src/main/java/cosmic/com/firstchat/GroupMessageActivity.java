package cosmic.com.firstchat;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import cosmic.com.firstchat.Model.ChatModel;
import cosmic.com.firstchat.Model.NotificationModel;
import cosmic.com.firstchat.Model.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupMessageActivity extends AppCompatActivity {

    Map<String, User>users = new HashMap<>();
    String destinationRoom;
    String uid;
    EditText editText;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private RecyclerView recyclerView;

    int countPeople = 0;
    List<ChatModel.Comment>comments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_group_message );

        destinationRoom =getIntent().getStringExtra( "destinationRoom" );
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText=findViewById( R.id.groupMessageActivity_editText );
        FirebaseDatabase.getInstance().getReference().child( "users" ).child( uid ).addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    users.put( item.getKey(),item.getValue(User.class) );
                }

                init();
                recyclerView = (RecyclerView) findViewById(R.id.groupMessageActivity_recyclerview);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        recyclerView= findViewById( R.id.groupMessageActivity_recyclerview );
        recyclerView.setAdapter( new GroupMessageRecyclerViewAdapter() );

    }

    private void init() {
        Button button = findViewById( R.id.groupMessageActivity_button );
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel.Comment comment=new ChatModel.Comment();
                comment.uid=uid;
                comment.message=editText.getText().toString();
                comment.timestamp= ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).child( destinationRoom ).child("comments" )
                        .push().setValue( comment ).addOnCompleteListener( new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseDatabase.getInstance().getReference().child( "chatrooms").child( destinationRoom)
                                .child( "users" ).addListenerForSingleValueEvent( new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String ,Boolean>map = (Map<String, Boolean>)dataSnapshot.getValue();

                                for(String item:map.keySet()){
                                    if(item.equals( uid )){
                                        continue;
                                    }
                                    sendFcm( users.get( item ).pushToken );
                                }
                                editText.setText( "" );
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        } );

                    }
                } );
            }
        } );

    }

    void sendFcm(String pushToken){
        Gson gson = new Gson();


        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = pushToken;
        notificationModel.notification.title = userName;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.title = userName;
        notificationModel.data.text = editText.getText().toString();

        Log.d("TAG","확인 노티 메시지: "+notificationModel.notification.text+"-보낸이 : "+userName);
//여기까지됨.

        RequestBody requestBody = RequestBody.create( MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type","application/json")
                .addHeader("Authorization","key=AIzaSyBU6NSdxTDZWPQOS8UGIoBIYcbJvcf3SJ8")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall( request ).enqueue( new Callback()  {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        } );
    }
    private class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public GroupMessageRecyclerViewAdapter() {
            getMessageList();
        }

        private void getMessageList() {
            databaseReference = FirebaseDatabase.getInstance().getReference().child( "chatrooms").child( destinationRoom ).child( "comments" );
            valueEventListener=databaseReference.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap =new HashMap<>();
                    for(DataSnapshot item :dataSnapshot.getChildren()){
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);

                        readUsersMap.put( key,comment_modify );
                        comments.add( comment_origin );
                    }

                    if(!comments.get( comments.size() - 1 ).readUsers.containsKey( uid )){
                        FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).child( destinationRoom ).child( "comments" )
                                .updateChildren( readUsersMap ).addOnCompleteListener( new OnCompleteListener<Void>() {


                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyDataSetChanged();
                                recyclerView.scrollToPosition( comments.size()-1 );
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_message,parent,false );

            return new GroupMessageViewHodler( view );

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GroupMessageViewHodler messageViewHolder = ((GroupMessageViewHodler)holder);

            if (comments.get( position ).uid.equals( uid )) {//내가보낸메시지
                messageViewHolder.textView_message.setText( comments.get( position ).message );
                messageViewHolder.textView_message.setBackgroundResource( R.drawable.rightbubble );
                messageViewHolder.linearLayout_destination.setVisibility( View.INVISIBLE );
                messageViewHolder.textView_message.setTextSize( 25 );
                messageViewHolder.linearLayout_main.setGravity( Gravity.RIGHT );
                setReadCounter( position,messageViewHolder.textView_readCounter_left );
            } else {//상대방이 보낸 메시지
                Glide.with( messageViewHolder.itemView.getContext() )
                        .load( users.get( comments.get( position ).uid ).profileImageUrl )
                        .apply( new RequestOptions().circleCrop() )
                        .into( messageViewHolder.imageView_profile );
                messageViewHolder.textview_name.setText( users.get( comments.get( position ).uid).userName );
                messageViewHolder.linearLayout_destination.setVisibility( View.VISIBLE );
                messageViewHolder.textView_message.setBackgroundResource( R.drawable.leftbubble );
                messageViewHolder.textView_message.setText( comments.get( position ).message );
                messageViewHolder.textView_message.setTextSize( 25 );
                messageViewHolder.linearLayout_main.setGravity( Gravity.LEFT );
                setReadCounter( position,messageViewHolder.textView_readCounter_right );
            }

            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone( TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }



        private class GroupMessageViewHodler extends RecyclerView.ViewHolder {

            public TextView textView_message;
            public TextView textview_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public GroupMessageViewHodler(View view) {
                super(view);

                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textview_name = (TextView) view.findViewById(R.id.messageItem_tv_name);
                imageView_profile = (ImageView) view.findViewById(R.id.messageItem_iv_profile);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearLayout_destination);
                linearLayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearLayout_destination);
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_tv_timestamp);
                textView_readCounter_left = (TextView) view.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right = (TextView) view.findViewById(R.id.messageItem_textview_readCounter_right);
            }
        }
    }
    void setReadCounter(final int position, final TextView textView){
        if(countPeople ==0) {
            //읽은 사람 있는지 서버에 물어보고 없으면..
            FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).child( destinationRoom ).child( "users" ).addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                    countPeople = users.size();
                    int count = countPeople - comments.get( position ).readUsers.size();
                    if (count > 0) {
                        textView.setVisibility( View.VISIBLE );
                        textView.setText( String.valueOf( count ) );
                    } else {
                        textView.setVisibility( View.INVISIBLE );
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
        }else{
            //읽은 사람 있으면 서버에 묻지않고 다이렉트 처리 (비용절감)
            int count = countPeople - comments.get( position ).readUsers.size();
            if (count > 0) {
                textView.setVisibility( View.VISIBLE );
                textView.setText( String.valueOf( count ) );
            } else {
                textView.setVisibility( View.INVISIBLE );
            }
        }
    }

}
