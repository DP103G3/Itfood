package tw.dp103g3.itfood;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonFragment extends Fragment {
    private Activity activity;
    private ListAdapter memberAdapter, guestAdapter;
    private int[] memberIcon, guestIcon;
    private String[] memberTitle, guestTitle;
    private int[] memberAction, guestAction;
    private List<Map<String, Object>> memberList, guestList;
    private ListView listView;
    private int memId;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        preferences =
                activity.getSharedPreferences(Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        memId = preferences.getInt("mem_id", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initListMap();
        listView = view.findViewById(R.id.listView);
        if (memId == 0) {
            guestAdapter = new SimpleAdapter(activity, guestList, R.layout.basic_list_item,
                    new String[]{"icon", "title"}, new int[]{R.id.ivIcon, R.id.tvTitle});
            listView.setAdapter(guestAdapter);
            listView.setOnItemClickListener(((parent, v, position, id) -> {
                Navigation.findNavController(v).navigate(guestAction[position]);
            }));
        } else {
            memberAdapter = new SimpleAdapter(activity, memberList, R.layout.basic_list_item,
                    new String[]{"icon", "title"}, new int[]{R.id.ivIcon, R.id.tvTitle});
            listView.setAdapter(memberAdapter);
            listView.setOnItemClickListener(((parent, v, position, id) -> {
                NavController navController = Navigation.findNavController(v);
                if (position != 3) {
                    navController.navigate(memberAction[position]);
                } else {
                    preferences.edit().putInt("mem_id", 0).commit();
                    navController.popBackStack(R.id.mainFragment, false);
                }
            }));
        }
    }

    private void initListMap() {
        memberList = new ArrayList<>();
        memberIcon = new int[]{R.drawable.person, R.drawable.payment, R.drawable.location, R.drawable.logout};
        memberTitle = new String[]{getString(R.string.textPersonInfo), getString(R.string.textPayment), getString(R.string.textSendLocation), getString(R.string.textLogout)};
        memberAction = new int[]{R.id.action_personFragment_to_personalInfoFragment,
                R.id.action_personFragment_to_paymentFragment,
                R.id.action_personFragment_to_locationFragment};
        guestList = new ArrayList<>();
        guestIcon = new int[]{R.drawable.login};
        guestTitle = new String[]{getString(R.string.textLogin)};
        guestAction = new int[]{R.id.action_personFragment_to_loginFragment};
        for (int i = 0; i < memberIcon.length; i++) {
            Map<String, Object> memberItem = new HashMap<>();
            memberItem.put("icon", memberIcon[i]);
            memberItem.put("title", memberTitle[i]);
            memberList.add(memberItem);
        }
        for (int i = 0; i < guestIcon.length; i++) {
            Map<String, Object> guestItem = new HashMap<>();
            guestItem.put("icon", guestIcon[i]);
            guestItem.put("title", guestTitle[i]);
            guestList.add(guestItem);
        }
    }
}
