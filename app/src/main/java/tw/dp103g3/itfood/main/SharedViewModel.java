package tw.dp103g3.itfood.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.address.City;
import tw.dp103g3.itfood.address.District;
import tw.dp103g3.itfood.payment.Payment;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<City> citySelected = new MutableLiveData<>();
    private final MutableLiveData<District> districtSelected = new MutableLiveData<>();
    private final MutableLiveData<Date> deliveryTimeSelected = new MutableLiveData<>();
    private final MutableLiveData<Payment> paymentSelected = new MutableLiveData<>();
    private final MutableLiveData<Address> addressSelected = new MutableLiveData<>();

    public void selectDistrict(District district) {
        districtSelected.setValue(district);
    }

    public void selectCity(City city) {
        citySelected.setValue(city);
    }

    public void selectDeliveryTime(Date date) {
        deliveryTimeSelected.setValue(date);
    }

    public void selectPayment(Payment payment) {
        paymentSelected.setValue(payment);
    }

    public void selectAddress(Address address) {
        addressSelected.setValue(address);
    }


    public LiveData<City> getSelectedCity() {
        return citySelected;
    }

    public LiveData<District> getSelectedDistrict() {
        return districtSelected;
    }

    public LiveData<Date> getSelectedDeliveryTime() {
        return deliveryTimeSelected;
    }

    public LiveData<Payment> getSelectedPayment() {
        return paymentSelected;
    }

    public LiveData<Address> getSelectedAddress() {
        return addressSelected;
    }


}


