package tw.dp103g3.itfood.shopping_cart;

import java.io.Serializable;
import java.util.List;

import tw.dp103g3.itfood.address.Address;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.payment.Payment;
import tw.dp103g3.itfood.shop.Dish;

public class Cart implements Serializable {
    private List<Dish> dishes;
    private Member member;
    private List<Payment> payments;
    private List<Address> addresses;

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Cart() {
    }

    public Cart(List<Dish> dishes, Member member, List<Payment> payments, List<Address> addresses) {
        super();
        this.dishes = dishes;
        this.member = member;
        this.payments = payments;
        this.addresses = addresses;
    }

}
