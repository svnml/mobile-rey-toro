package com.example.reytorodeli.Helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.reytorodeli.Domain.Foods;
import java.util.ArrayList;

public class ManagmentCart {
    private Context context;
    private TinyDB tinyDB;
    private static final String CART_KEY = "CartList"; // Clave consistente

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
    }

    public void setCartList(ArrayList<Foods> list) {
        tinyDB.putListObject(CART_KEY, list);
        Log.d("ManagmentCart", "Cart list set with items: " + list.size());
    }

    public void insertFood(Foods food) {
        ArrayList<Foods> cartList = getListCart();
        boolean alreadyInCart = false;

        for (Foods item : cartList) {
            if (item.getId() == food.getId()) {
                item.setNumberInCart(item.getNumberInCart() + food.getNumberInCart());
                alreadyInCart = true;
                break;
            }
        }

        if (!alreadyInCart) {
            cartList.add(food);
        }

        tinyDB.putListObject(CART_KEY, cartList);

        Toast.makeText(context, "Producto agregado con éxito", Toast.LENGTH_SHORT).show();
    }
    public ArrayList<Foods> getListCart() {
        ArrayList<Foods> cartList = tinyDB.getListObject(CART_KEY);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        Log.d("ManagmentCart", "Cart list retrieved with items: " + cartList.size());
        return cartList;
    }

    public Double getTotalFee() {
        ArrayList<Foods> listItem = getListCart();
        double fee = 0;

        for (Foods item : listItem) {
            fee += item.getPrice() * item.getNumberInCart();
        }
        return fee;
    }

    public void minusNumberItem(ArrayList<Foods> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        if (listItem.get(position).getNumberInCart() == 1) {
            listItem.remove(position);
        } else {
            listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() - 1);
        }
        tinyDB.putListObject(CART_KEY, listItem); // Usa CART_KEY
        changeNumberItemsListener.change();
    }

    public void plusNumberItem(ArrayList<Foods> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() + 1);
        tinyDB.putListObject(CART_KEY, listItem); // Usa CART_KEY
        changeNumberItemsListener.change();
    }

    public void clearCart() {
        tinyDB.putListObject(CART_KEY, new ArrayList<>()); // Guarda una lista vacía
    }
}
