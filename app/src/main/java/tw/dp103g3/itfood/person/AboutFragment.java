package tw.dp103g3.itfood.person;



import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.main.Common;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {
    private Activity activity;
    private Toolbar toolbarAbout;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();

    }

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);

    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

        toolbarAbout = view.findViewById(R.id.toolbarAbout);

        toolbarAbout.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

}
