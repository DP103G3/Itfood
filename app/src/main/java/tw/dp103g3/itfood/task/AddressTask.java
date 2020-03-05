package tw.dp103g3.itfood.task;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.person.ShopRegisterFragment;

public class AddressTask extends AsyncTask<String, Integer, Address> {
    private static final String TAG = "TAG_AddressTask";
    private WeakReference<Context> contextWeakReference;
    private String locationName;
    private WeakReference<ShopRegisterFragment> shopRegisterFragmentWeakReference;

    public AddressTask(Context context, String locationName, ShopRegisterFragment shopRegisterFragment) {
        this.contextWeakReference = new WeakReference<>(context);
        this.locationName = locationName;
        this.shopRegisterFragmentWeakReference = new WeakReference<>(shopRegisterFragment);
    }

    @Override
    protected Address doInBackground(String... strings) {
        Context context = contextWeakReference.get();
        if (context == null) {
            return null;
        }
        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(locationName, 1);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (addressList == null || addressList.isEmpty()) {
            return null;
        } else {
            return addressList.get(0);
        }
    }

    @Override
    protected void onPostExecute(Address address) {
        Context context = contextWeakReference.get();
        ShopRegisterFragment shopRegisterFragment = shopRegisterFragmentWeakReference.get();
        if (isCancelled() || context == null || shopRegisterFragment == null) {
            return;
        }
        View view = shopRegisterFragment.getView();
        if (view == null) {
            return;
        }
        EditText editText = view.findViewById(R.id.etAddress);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        if (address == null) {
            progressBar.setVisibility(View.GONE);
            editText.setError(context.getString(R.string.textNoThisAddress));
        } else {
            progressBar.setVisibility(View.GONE);
            shopRegisterFragment.setAddressCheck(true);
            shopRegisterFragment.setAddress(address);
        }
    }
}
