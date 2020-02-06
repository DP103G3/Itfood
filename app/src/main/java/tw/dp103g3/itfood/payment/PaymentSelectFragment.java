package tw.dp103g3.itfood.payment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.main.SharedViewModel;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;
import static tw.dp103g3.itfood.Common.formatCardNum;


public class PaymentSelectFragment extends Fragment {
    private Toolbar toolbarPaymentSelect;
    private RecyclerView rvPaymentRadioButton;
    private Activity activity;
    private CommonTask getPaymentTask;
    private SharedViewModel model;
    private Payment selectedPayment;
    private View view;
    private SharedPreferences pref;
    private int mem_id;
    private CardView cardViewCheck;
    private List<Payment> payments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        mem_id = pref.getInt("mem_id", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_select, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        model.getSelectedPayment().observe(getViewLifecycleOwner(), payment -> {
            selectedPayment = payment;
        });

        toolbarPaymentSelect = view.findViewById(R.id.toolbarPaymentSelect);
        rvPaymentRadioButton = view.findViewById(R.id.rvPaymentRadioButton);
        cardViewCheck = view.findViewById(R.id.cardViewCheck);

        rvPaymentRadioButton.setLayoutManager(new LinearLayoutManager(activity));

        toolbarPaymentSelect.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        cardViewCheck.setOnClickListener(v -> {
            model.selectPayment(selectedPayment);
            Navigation.findNavController(v).popBackStack();
        });

        payments = getPayments(mem_id);
        ShowPayments(payments);

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
        if (payments == null || payments.isEmpty()) {
            if (Common.networkConnected(activity)) {
                Common.showToast(activity, "no payment found");
                Navigation.findNavController(view).popBackStack();
            }
        }
        PaymentSelectFragment.PaymentAdapter paymentAdapter = (PaymentSelectFragment.PaymentAdapter) rvPaymentRadioButton.getAdapter();
        if (paymentAdapter == null) {
            rvPaymentRadioButton.setAdapter(new PaymentSelectFragment.PaymentAdapter(activity, payments));
        } else {
            paymentAdapter.setPayments(payments);
            paymentAdapter.notifyDataSetChanged();
        }
    }

    private class PaymentAdapter extends RecyclerView.Adapter<PaymentSelectFragment.PaymentAdapter.MyViewHolder> {
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
            RadioButton radioButton;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                radioButton = itemView.findViewById(R.id.radioButton);
            }
        }

        @Override
        public int getItemCount() {
            return payments.size() + 1;
        }

        @NonNull
        @Override
        public PaymentSelectFragment.PaymentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.select_radio_button_item_view, parent, false);
            return new PaymentSelectFragment.PaymentAdapter.MyViewHolder(itemView);
        }

        private RadioButton lastChecked = null;

        @Override
        public void onBindViewHolder(@NonNull PaymentSelectFragment.PaymentAdapter.MyViewHolder holder, int position) {
            Drawable visa = getResources().getDrawable(R.drawable.visa, activity.getTheme());
            visa.setBounds(0, 0, 72, 72);
            Drawable master = getResources().getDrawable(R.drawable.mastercard, activity.getTheme());
            master.setBounds(0, 0, 72, 72);
            Drawable checkedIcon = getResources().getDrawable(R.drawable.round_check_circle, activity.getTheme());
            int height = checkedIcon.getIntrinsicHeight();
            int width = checkedIcon.getIntrinsicWidth();
            checkedIcon.setBounds(0, 0, width, height);
            ColorStateList tint = getResources().getColorStateList(R.color.radio_button_custom_button, activity.getTheme());
            checkedIcon.setTintList(tint);
            if (position == payments.size()) {
                Drawable add = getResources().getDrawable(R.drawable.add, activity.getTheme());
                holder.radioButton.setCompoundDrawablesWithIntrinsicBounds(add, null, null, null);
                holder.radioButton.setText(R.string.addPayment);
                holder.radioButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_paymentSelectFragment_to_addPaymentFragment));
            } else {
                final Payment payment = payments.get(position);
                holder.radioButton.setText(formatCardNum(payment.getPay_cardnum()));
                holder.radioButton.setTag(position);
                holder.radioButton.setCompoundDrawables(checkedIcon, null, null, null);

                if ((selectedPayment != null) && (payment.getPay_id() == selectedPayment.getPay_id())) {
                    holder.radioButton.setChecked(true);
                    lastChecked = holder.radioButton;
                    selectedPayment = payments.get(position);
                } else {
                    holder.radioButton.setChecked(false);
                }

                holder.radioButton.setOnClickListener(v -> {
                    RadioButton rb = (RadioButton) v;
                    if (rb.isChecked()) {
                        if (lastChecked != null && lastChecked != rb) {
                            lastChecked.setChecked(false);
                        }
                        lastChecked = rb;
                    }
                    selectedPayment = payment;
                });

                if (holder.radioButton.getText().toString().startsWith("4")) {
                    holder.radioButton.setCompoundDrawables(checkedIcon, null, visa, null);
                } else if (holder.radioButton.getText().toString().startsWith("5")) {
                    holder.radioButton.setCompoundDrawables(checkedIcon, null, master, null);
                }


            }
        }
    }


}
