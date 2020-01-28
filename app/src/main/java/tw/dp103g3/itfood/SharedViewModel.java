package tw.dp103g3.itfood;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import tw.dp103g3.itfood.address.City;
import tw.dp103g3.itfood.address.District;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<City> citySelected = new MutableLiveData<City>();
    private final MutableLiveData<District> districtSelected = new MutableLiveData<District>();

    public void selectDistrict(District district) {
        districtSelected.setValue(district);
    }

    public void selectCity(City city) {
        citySelected.setValue(city);
    }

    public LiveData<City> getSelectedCity() {
        return citySelected;
    }

    public LiveData<District> getSelectedDistrict() {
        return districtSelected;
    }
}


