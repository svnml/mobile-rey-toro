package com.example.reytorodeli.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reytorodeli.Adapter.CartAdapter;
import com.example.reytorodeli.Domain.Foods;
import com.example.reytorodeli.Helper.ManagmentCart;
import com.example.reytorodeli.R;
import com.example.reytorodeli.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.reytorodeli.Helper.ManagmentCart;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;


import java.util.ArrayList;
import java.util.HashMap;
import android.content.Intent;

public class CartActivity extends BaseActivity {

    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double iva;
    private String editingOrderId = null;
    private ArrayList<Foods> orderItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));

        managmentCart= new ManagmentCart(this);

        loadOrderData();
        initList();
        calculateCart();
        setVariable();
    }

    private void initList() {
        ArrayList<Foods> cartList = managmentCart.getListCart();

        if (cartList.isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollviewCart.setVisibility(View.GONE);
            binding.constraintLayout3.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollviewCart.setVisibility(View.VISIBLE);
            binding.constraintLayout3.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.cardView.setLayoutManager(linearLayoutManager);

        adapter = new CartAdapter(cartList, this, () -> calculateCart());
        binding.cardView.setAdapter(adapter);

        Log.d("CartActivity", "Adapter initialized with items: " + cartList.size());

        binding.addItemsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(CartActivity.this, ListFoodsActivity.class);
            intent.putExtra("listAll", true);
            startActivity(intent);
        });
    }

    private void loadOrderData() {
        Intent intent = getIntent();
        ArrayList<Foods> items = (ArrayList<Foods>) intent.getSerializableExtra("foodsList");
        editingOrderId = intent.getStringExtra("orderId");

        Log.d("CartActivity", "Received orderId: " + editingOrderId);
        Log.d("CartActivity", "Received foodsList: " + items);

        try {
            if (items != null && !items.isEmpty()) {
                for (Foods food : items) {
                    Log.d("CartActivity", "Food item: " + food.getTitle() + ", Quantity: " + food.getNumberInCart());
                }
                managmentCart.setCartList(items);
                Log.d("CartActivity", "Lista del carrito actualizada: " + items.size());
            } else {
                Log.d("CartActivity", "No se reciben productos");
            }
        } catch (Exception e) {
            Log.e("CartActivity", "Error cargando los datos del pedido: ", e);
            Toast.makeText(this, "Error al cargar los datos del pedido.", Toast.LENGTH_SHORT).show();
        }
    }




    private void calculateCart() {
        double percentIva=0.04;
        double delivery=1000;

        iva = Math.round(managmentCart.getTotalFee() * percentIva * 100) / 100;

        double total= Math.round((managmentCart.getTotalFee() + iva + delivery) * 100) / 100;
        double itemTotal= Math.round(managmentCart.getTotalFee() * 100) / 100;

        binding.totalFeeTxt.setText("CLP$"+itemTotal);
        binding.ivaTxt.setText("CLP$"+iva);
        binding.deliveryTxt.setText("CLP$"+delivery);
        binding.totalTxt.setText("CLP$"+total);
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(view -> CartActivity.this.finish());

        binding.button2.setOnClickListener(view -> confirmOrder());
    }

    private void confirmOrder() {
        ArrayList<Foods> cartItems = managmentCart.getListCart();

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para confirmar el pedido.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingOrderId != null) {
            DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(editingOrderId);

            HashMap<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", editingOrderId);
            orderData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            orderData.put("deliveryFee", 1000);
            orderData.put("iva", Math.round(managmentCart.getTotalFee() * 0.04 * 100) / 100.0);
            orderData.put("totalFee", managmentCart.getTotalFee() + 1000);

            ArrayList<HashMap<String, Object>> items = new ArrayList<>();
            for (Foods item : cartItems) {
                HashMap<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getId());
                itemData.put("title", item.getTitle());
                itemData.put("price", item.getPrice());
                itemData.put("quantity", item.getNumberInCart());
                itemData.put("imagePath", item.getImagePath());
                items.add(itemData);
            }
            orderData.put("items", items);

            orderRef.setValue(orderData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Pedido actualizado con éxito.", Toast.LENGTH_SHORT).show();
                    managmentCart.clearCart();

                    Intent intent = new Intent(CartActivity.this, ListOrdersActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Error al actualizar el pedido: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Id Incremental
            DatabaseReference counterRef = FirebaseDatabase.getInstance().getReference("orderCounter");

            counterRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Long currentCount = currentData.getValue(Long.class);
                    if (currentCount == null) {
                        currentData.setValue(1);
                        return Transaction.success(currentData);
                    } else {
                        currentData.setValue(currentCount + 1);
                        return Transaction.success(currentData);
                    }
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (committed && currentData != null) {
                        Long newOrderIdLong = currentData.getValue(Long.class);
                        if (newOrderIdLong == null) {
                            Toast.makeText(CartActivity.this, "Error al generar el ID del pedido.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String newOrderId = String.valueOf(newOrderIdLong);

                        DatabaseReference newOrderRef = FirebaseDatabase.getInstance().getReference("Orders").child(newOrderId);


                        HashMap<String, Object> orderData = new HashMap<>();
                        orderData.put("orderId", newOrderId);
                        orderData.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        orderData.put("deliveryFee", 1000);
                        orderData.put("iva", Math.round(managmentCart.getTotalFee() * 0.04 * 100) / 100.0);
                        orderData.put("totalFee", managmentCart.getTotalFee() + 1000);

                        ArrayList<HashMap<String, Object>> items = new ArrayList<>();
                        for (Foods item : cartItems) {
                            HashMap<String, Object> itemData = new HashMap<>();
                            itemData.put("id", item.getId());
                            itemData.put("title", item.getTitle());
                            itemData.put("price", item.getPrice());
                            itemData.put("quantity", item.getNumberInCart());
                            itemData.put("imagePath", item.getImagePath());
                            items.add(itemData);
                        }
                        orderData.put("items", items);

                        newOrderRef.setValue(orderData).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(CartActivity.this, "Pedido confirmado con éxito.", Toast.LENGTH_SHORT).show();
                                managmentCart.clearCart();

                                Intent intent = new Intent(CartActivity.this, ListOrdersActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(CartActivity.this, "Error al confirmar el pedido: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(CartActivity.this, "Error al generar el ID del pedido.", Toast.LENGTH_SHORT).show();
                        Log.e("CartActivity", "Transaccion fallida: " + error);
                    }
                }
            });
        }
    }
}