package tw.dp103g3.itfood.address;

import java.io.Serializable;
import java.util.List;

public class City implements Serializable {

    private String name;
    private List<District> districts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }
}
