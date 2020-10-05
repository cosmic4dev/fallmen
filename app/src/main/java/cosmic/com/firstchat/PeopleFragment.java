package cosmic.com.firstchat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cosmic.com.firstchat.Model.User;

public class PeopleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_people,container,false );
        RecyclerView recyclerView = (RecyclerView)view.findViewById( R.id.recyclerview );
        recyclerView.setLayoutManager( new LinearLayoutManager( inflater.getContext() ) );
        recyclerView.setAdapter(new PeopleAdapter() );

        FloatingActionButton floatingActionButton  = view.findViewById( R.id.peoplefragment_floatingButton );
        floatingActionButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),SelectFriendActivity.class);
                startActivity( intent );
            }
        } );
        return view;
    }

    public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.CostumViewHolder> {

        List<User> users;

        public PeopleAdapter() {
            users=new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child( "users" ).addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    users.clear();//

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        User user = snapshot.getValue(User.class);

                        if(user.uid.equals( myUid )){
                            continue;
                        }
                        users.add( user );
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            } );
        }

        @NonNull
        @Override
        public CostumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_friend,parent,false );
            return new CostumViewHolder( view );
        }


        @Override
        public void onBindViewHolder(@NonNull final CostumViewHolder holder, final int position) {
            Glide.with( holder.imageView.getContext()).load( users.get(position).profileImageUrl )
                    .apply( new RequestOptions().circleCrop() ).into( (ImageView) holder.imageView );

            holder.textView.setText( users.get( position ).userName );

            holder.imageView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),MessageActivity.class);
                    intent.putExtra( "destinationUid",users.get( position ).uid );
                    Log.d("TAG","ddid: "+users.get( position ).uid);
                    ActivityOptions activityOptions=ActivityOptions.makeCustomAnimation( v.getContext(),R.anim.fromright,R.anim.toleft );
                    startActivity( intent, activityOptions.toBundle());
//                    startActivity( intent );

                    if(users.get( position ).comment!=null) {
                        holder.textView_comment.setText( users.get( position ).comment );
                    }
                }
            } );
        }




        @Override
        public int getItemCount() {
            return users.size();
        }

        public class CostumViewHolder extends RecyclerView.ViewHolder{

            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;

            public CostumViewHolder(@NonNull View itemView) {
                super( itemView );
                imageView= itemView.findViewById( R.id.frienditem_imageview );
                textView=itemView.findViewById( R.id.friend_textview );
                textView_comment = itemView.findViewById( R.id.frienditem_textview_comment );
            }
        }
    }


}
