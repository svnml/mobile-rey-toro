package com.example.reytorodeli.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.reytorodeli.Adapter.OrdersAdapter;
import com.example.reytorodeli.R;
import com.example.reytorodeli.databinding.ActivityListOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ListOrdersActivity extends AppCompatActivity {

    private ActivityListOrdersBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private OrdersAdapter adapter;
    private ArrayList<HashMap<String, Object>> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.backBtnOrders.setOnClickListener(view -> navigateToMainActivity());

        loadOrders();
    }

    private void loadOrders() {
        binding.progressBarOrders.setVisibility(View.VISIBLE);
        ordersList = new ArrayList<>();

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference ordersRef = database.getReference("Orders");

        ordersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        try {
                            HashMap<String, Object> orderData = (HashMap<String, Object>) orderSnapshot.getValue();
                            if (orderData != null) {
                                ordersList.add(orderData);
                            }
                        } catch (ClassCastException e) {
                            Log.e("ListOrdersActivity", "Error al convertir el pedido: " + e.getMessage());
                        }
                    }
                    setupRecyclerView();
                } else {
                    Toast.makeText(ListOrdersActivity.this, "No tienes pedidos registrados.", Toast.LENGTH_SHORT).show();
                }
                binding.progressBarOrders.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarOrders.setVisibility(View.GONE);
                Toast.makeText(ListOrdersActivity.this, "Error al cargar los pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        binding.orderListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(ordersList, this);
        binding.orderListView.setAdapter(adapter);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(ListOrdersActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateToMainActivity();
    }
}

