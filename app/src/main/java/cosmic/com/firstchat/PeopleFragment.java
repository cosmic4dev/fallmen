package cosmic.com.firstchat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PeopleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_people,container,false );
        RecyclerView recyclerView = (RecyclerView)view.findViewById( R.id.recyclerview );
        recyclerView.setLayoutManager( new LinearLayoutManager( inflater.getContext() ) );
        recyclerView.setAdapter(new PeopleAdapter() );
        return view;
    }


}
