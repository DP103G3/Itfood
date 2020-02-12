package tw.dp103g3.itfood.payment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.task.CommonTask;

import static android.view.View.GONE;
import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;


public class PaymentFragment extends Fragment {
    @BindView(R.id.toolbarPayment)
    Toolbar toolbarPayment;
    @BindView(R.id.rvPayments)
    RecyclerView rvPayments;
    private NavController navController;
    private Activity activity;
    private CommonTask getPaymentTask;
    private SharedPreferences pref;
    private int mem_id;


    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        navController = Navigation.findNavController(view);

        toolbarPayment.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    private List<Payment> getPayments(int mem_id) {
        List<Payment> payments = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/PaymentServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
            jsonObject.addProperty("action", "getByMemberId");
            jsonObject.addProperty("mem_id", mem_id);
            jsonObject.addProperty("state", 1);
            String jsonOut = jsonObject.toString();
            getPaymentTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getPaymentTask.execute().get();
                Type listType = new TypeToken<List<Payment>>() {
                }.getType();
                payments = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return payments;
    }

    private void ShowPayments(List<Payment> payments) {
        PaymentFragment.PaymentAdapter paymentAdapter = (PaymentFragment.PaymentAdapter) rvPayments.getAdapter();
        if (paymentAdapter == null) {
            rvPayments.setAdapter(new PaymentFragment.PaymentAdapter(activity, payments));
        } else {
            paymentAdapter.setPayments(payments);
            paymentAdapter.notifyDataSetChanged();
        }
    }

    private class PaymentAdapter extends RecyclerView.Adapter<PaymentFragment.PaymentAdapter.MyViewHolder> {
        private Context context;
        private List<Payment> payments;

        public PaymentAdapter(Context context, List<Payment> payments) {
            this.payments = payments;
            this.context = context;
        }

        void setPayments(List<Payment> payments) {
            this.payments = payments;
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivCardType;
            TextView tvCardNum;
            ImageView ivChevRight;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCardType = itemView.findViewById(R.id.ivCardType);
                tvCardNum = itemView.findViewById(R.id.tvCardNum);
                ivChevRight = itemView.findViewById(R.id.ivChevRight);
            }
        }

        @Override
        public int getItemCount() {
            if (payments.isEmpty()) {
                return 1;
            } else {
                return payments.size() + 1;
            }
        }

        @NonNull
        @Override
        public PaymentFragment.PaymentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.payment_item_view, parent, false);
            return new PaymentFragment.PaymentAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PaymentFragment.PaymentAdapter.MyViewHolder holder, int position) {
            if (position == payments.size()) {
                Drawable add = getResources().getDrawable(R.drawable.add, activity.getTheme());
                holder.ivCardType.setImageDrawable(add);
                holder.tvCardNum.setText(R.string.addPayment);
                holder.itemView.setOnClickListener(v -> {
                    navController.navigate(R.id.action_paymentFragment_to_addPaymentFragment);
                });
                holder.ivChevRight.setVisibility(GONE);
            } else {
                final Payment payment = payments.get(position);
                String cardNum = Common.formatCardNum(payment.getPay_cardnum());

                holder.tvCardNum.setText(cardNum);
                if (cardNum.startsWith("4")) {
                    holder.ivCardType.setImageResource(R.drawable.visa);
                } else if (cardNum.startsWith("5")) {
                    holder.ivCardType.setImageResource(R.drawable.mastercard);
                } else {
                    holder.ivCardType.setVisibility(View.INVISIBLE);
                }
                holder.itemView.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("payment", payment);
                    navController.navigate(R.id.action_paymentFragment_to_paymentDetailFragment, bundle);
                });

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        rvPayments.setLayoutManager(new LinearLayoutManager(activity));
        List<Payment> payments = getPayments(mem_id);
        ShowPayments(payments);
    }
}
