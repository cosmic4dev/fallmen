package cosmic.com.firstchat;

import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cosmic.com.firstchat.Model.ChatModel;
import cosmic.com.firstchat.Model.User;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
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
                            editText.setText( "" );
                        }
                    } );

                }


            }
        } );

        checkChatRoom();//여기에 안놔서 코멘트를 못담았음...

    }

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child( "chatrooms" )
                .orderByChild( "users/"+uid ).equalTo( true ).addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot item:dataSnapshot.getChildren()){
                    ChatModel chatModel=item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey( destinationUid )){
                        chatRoomUid=item.getKey();
                        button.setEnabled( true );

                        mLayoutManager = new LinearLayoutManager( MessageActivity.this );
                        recyclerView.setLayoutManager( mLayoutManager );
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
        finish();
        overridePendingTransition( R.anim.fromleft,R.anim.toright );
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments;
        //        List<User>users;
        User userModel;

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



        void getMessageList() {

            FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).child( chatRoomUid ).child( "comments" ).addValueEventListener( new ValueEventListener() {
//            FirebaseDatabase.getInstance().getReference().child( "chatrooms" ).child( "comments" ).child( chatRoomUid ).addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        comments.add( item.getValue( ChatModel.Comment.class ) );
                    }
                    notifyDataSetChanged();
                    recyclerView.scrollToPosition( comments.size() - 1 );
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
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


            public MessageViewHolder(@NonNull View itemView) {
                super( itemView );
                textView_message = itemView.findViewById( R.id.messageItem_textView_message );
                textview_name = itemView.findViewById( R.id.messageItem_tv_name );
                iv_profile = itemView.findViewById( R.id.messageItem_iv_profile );
                linearLayout_destination = itemView.findViewById( R.id.messageItem_linearLayout_destination );
                linearLayout_main = itemView.findViewById( R.id.messageItem_linearLayout_main );
                textView_timestamp=itemView.findViewById( R.id.messageItem_tv_timestamp );
            }
        }
    }
}
