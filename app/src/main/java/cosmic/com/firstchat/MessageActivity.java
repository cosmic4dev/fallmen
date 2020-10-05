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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;
//    private RecyclerView.LayoutManager mLayoutManager;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private User userModel;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    int countPeople=0;

    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_message );

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내 아이디
        destinationUid = getIntent().getStringExtra( "destinationUid" );//상대방 아이디
        button = findViewById( R.id.message_button );
        editText=findViewById( R.id.message_editText );
        recyclerView=findViewById( R.id.messageActivity_recyclerView);
        recyclerView.setHasFixedSize( true );

        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel= new ChatModel();
                chatModel.users.put(uid,true);
                chatModel.users.put( destinationUid,true );

                if(chatRoomUid ==null){
                    button.setEnabled( false );
                    FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).push().setValue(chatModel).addOnSuccessListener( new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();
                            Log.d( "TAG","메시지 푸쉬실패 " );
                        }
                    } );
                }else{

                    ChatModel.Comment comment=new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    comment.timestamp = ServerValue.TIMESTAMP;
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener( new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d( "TAG","메시지 푸쉬확인" );
                            sendFcm();
                            editText.setText( "" );
                        }
                    } );

                }


            }
        } );

        checkChatRoom();//여기에 안놔서 코멘트를 못담았음...

    }

    void sendFcm(){
        Gson gson = new Gson();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            Log.d( "TAG","유저네임 :"+firebaseUser.getDisplayName() );
            Log.d( "TAG","유저정보:"+firebaseUser );
        }else{
            Log.d( "TAG","유저널" );
        }
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Log.d( "TAG","username: "+userName );
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = userModel.pushToken;
        notificationModel.notification.title = userName;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.title = userName;
        notificationModel.data.text = editText.getText().toString();

        Log.d("TAG","확인 노티 메시지: "+notificationModel.notification.text+"-보낸이 : "+userName);
//여기까지됨.

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

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


    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child( "chatrooms" )
                .orderByChild( "users/"+uid ).equalTo( true ).addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot item:dataSnapshot.getChildren()){
                    ChatModel chatModel=item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey( destinationUid )&&chatModel.users.size()==2){
                        chatRoomUid=item.getKey();
                        button.setEnabled( true );
                        recyclerView.setLayoutManager( new LinearLayoutManager( MessageActivity.this ) );
                        recyclerView.setAdapter( new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }



    @Override
    public void onBackPressed() {
        if(valueEventListener !=null) {
            databaseReference.removeEventListener( valueEventListener );
        }
        finish();
        overridePendingTransition( R.anim.fromleft,R.anim.toright );
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();


            FirebaseDatabase.getInstance().getReference().child( "users" ).child( destinationUid ).addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue( User.class );
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );


        }

        void setReadCounter(final int position, final TextView textView){
            if(countPeople ==0) {
                //읽은 사람 있는지 서버에 물어보고 없으면..
                FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).child( chatRoomUid ).child( "users" ).addListenerForSingleValueEvent( new ValueEventListener() {
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



        void getMessageList() {

            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_motify = item.getValue(ChatModel.Comment.class);
                        comment_motify.readUsers.put(uid, true);

                        readUsersMap.put(key, comment_motify);
                        comments.add(comment_origin);
                    }


                    //아래 if 코드는 첫 채팅창이 존재하지않으면 버그를 발생시킨다...
                    if(comments.size()!=0) {//그래서 리스트 사이즈가 0이면 통과 시켜서 해결함.
                        if (!comments.get( comments.size() - 1 ).readUsers.containsKey( uid )) {


                            FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).child( chatRoomUid ).child( "comments" )
                                    .updateChildren( readUsersMap ).addOnCompleteListener( (task) -> {
                                notifyDataSetChanged();
                                recyclerView.scrollToPosition( comments.size() - 1 );
                            } );
                        } else {
                            notifyDataSetChanged();
                            recyclerView.scrollToPosition( comments.size() - 1 );
                        }
                    }
                    //메세지가 갱신


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_message, parent, false );

            return new MessageViewHolder( view );
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
               MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);
            
            if (comments.get( position ).uid.equals( uid )) {//내가보낸메시지
                messageViewHolder.textView_message.setText( comments.get( position ).message );
                messageViewHolder.textView_message.setBackgroundResource( R.drawable.rightbubble );
                messageViewHolder.linearLayout_destination.setVisibility( View.INVISIBLE );
                messageViewHolder.textView_message.setTextSize( 25 );
                messageViewHolder.linearLayout_main.setGravity( Gravity.RIGHT );
                setReadCounter( position,messageViewHolder.textView_readCounter_left );
            } else {//상대방이 보낸 메시지
                Glide.with( messageViewHolder.itemView.getContext() )
                        .load( userModel.profileImageUrl )
                        .apply( new RequestOptions().circleCrop() )
                        .into( messageViewHolder.iv_profile );
                messageViewHolder.textview_name.setText( userModel.userName );
                messageViewHolder.linearLayout_destination.setVisibility( View.VISIBLE );
                messageViewHolder.textView_message.setBackgroundResource( R.drawable.leftbubble );
                messageViewHolder.textView_message.setText( comments.get( position ).message );
                messageViewHolder.textView_message.setTextSize( 25 );
                messageViewHolder.linearLayout_main.setGravity( Gravity.LEFT );
                setReadCounter( position,messageViewHolder.textView_readCounter_right );
            }
            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);

        }


        @Override
        public int getItemCount() {
            return comments.size();

        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textview_name;
            public ImageView iv_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;


            public MessageViewHolder(@NonNull View itemView) {
                super( itemView );
                textView_message = itemView.findViewById( R.id.messageItem_textView_message );
                textview_name = itemView.findViewById( R.id.messageItem_tv_name );
                iv_profile = itemView.findViewById( R.id.messageItem_iv_profile );
                linearLayout_destination = itemView.findViewById( R.id.messageItem_linearLayout_destination );
                linearLayout_main = itemView.findViewById( R.id.messageItem_linearLayout_main );
                textView_timestamp=itemView.findViewById( R.id.messageItem_tv_timestamp );
                textView_readCounter_left = itemView.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right = itemView.findViewById(R.id.messageItem_textview_readCounter_right);
            }
        }
    }
}
