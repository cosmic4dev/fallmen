package cosmic.com.firstchat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.CostumViewHolder> {

    List<User>users;

    public PeopleAdapter() {
        users=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child( "users" ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();//

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    users.add( snapshot.getValue(User.class) );
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
    public void onBindViewHolder(@NonNull CostumViewHolder holder, int position) {

        Glide.with( holder.imageView.getContext()).load( users.get(position).profileImageUrl )
                .apply( new RequestOptions().circleCrop() ).into( (ImageView) holder.imageView );


        holder.textView.setText( users.get( position ).userName );

        Log.d("TAG","데이터확인: "+users.get( position ).userName);
        Log.d("TAG","데이터확인: "+users.get( position ).profileImageUrl);
    }



    @Override
    public int getItemCount() {
        return users.size();
    }

    public class CostumViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView textView;

        public CostumViewHolder(@NonNull View itemView) {
            super( itemView );
            imageView= itemView.findViewById( R.id.image_friend );
            textView=itemView.findViewById( R.id.friend_textview );
        }
    }
}
