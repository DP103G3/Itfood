package tw.dp103g3.itfood.address;

import java.io.Serializable;

public class District implements Serializable {
    private String name;
    private int zip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
