package tw.dp103g3.itfood.person;


import android.accounts.Account;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class InformationFragment extends Fragment {
    private Toolbar toolbarInformation;
    private Activity activity;

    public InformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = getActivity();
        return inflater.inflate(R.layout.fragment_information, container, false);
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        toolbarInformation = view.findViewById(R.id.toolbarInformation);

        toolbarInformation.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

}
