package com.example.reytorodeli.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reytorodeli.Activity.CartActivity;
import com.example.reytorodeli.Domain.Foods;
import com.example.reytorodeli.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private ArrayList<HashMap<String, Object>> ordersList;
    private Context context;

    public OrdersAdapter(ArrayList<HashMap<String, Object>> ordersList, Context context) {
        this.ordersList = ordersList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_list_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, Object> order = ordersList.get(position);


        Object orderIdObj = order.get("orderId");
        final String orderId;

        if (orderIdObj instanceof Long) {
            orderId = String.valueOf(orderIdObj);
        } else if (orderIdObj instanceof String) {
            orderId = (String) orderIdObj;
        } else {
            orderId = "";
        }

        holder.orderTitleTxt.setText("Pedido #" + orderId);


        Object totalFeeObj = order.get("totalFee");
        final double totalFee;

        if (totalFeeObj instanceof Long) {
            totalFee = ((Long) totalFeeObj).doubleValue();
        } else if (totalFeeObj instanceof Double) {
            totalFee = (Double) totalFeeObj;
        } else {
            totalFee = 0.0; // Valor por defecto
        }

        holder.orderTotalTxt.setText("Total: CLP$" + totalFee);

        holder.editOrderBtn.setOnClickListener(v -> {

            String currentOrderId = (String) order.get("orderId");

            ArrayList<HashMap<String, Object>> orderItems = (ArrayList<HashMap<String, Object>>) order.get("items");

            ArrayList<Foods> foodsList = new ArrayList<>();
            try {
                if (orderItems != null) {
                    for (HashMap<String, Object> item : orderItems) {
                        Log.d("OrdersAdapter", "Procesando el dato: " + item);
                        Foods food = new Foods();

                        Object idObj = item.get("id");
                        if (idObj != null) {
                            food.setId(((Number) idObj).intValue());
                        }

                        Object titleObj = item.get("title");
                        if (titleObj != null) {
                            food.setTitle((String) titleObj);
                        }

                        Object priceObj = item.get("price");
                        if (priceObj != null) {
                            food.setPrice(((Number) priceObj).doubleValue());
                        }

                        Object quantityObj = item.get("quantity");
                        if (quantityObj != null) {
                            food.setNumberInCart(((Number) quantityObj).intValue());
                        }

                        Object imagePathObj = item.get("imagePath");
                        if (imagePathObj != null) {
                            food.setImagePath((String) imagePathObj);
                        }

                        foodsList.add(food);
                    }
                }

                Log.d("OrdersAdapter", "Datos del foodList parceados: " + foodsList);

                Intent intent = new Intent(context, CartActivity.class);
                intent.putExtra("foodsList", foodsList);
                intent.putExtra("orderId", currentOrderId);
                context.startActivity(intent);

            } catch (Exception e) {
                Log.e("OrdersAdapter", "Error parsing order items or starting CartActivity: ", e);
                Toast.makeText(context, "Error al editar el pedido. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteOrderBtn.setOnClickListener(view -> {
            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
            ordersRef.child(orderId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("OrdersAdapter", "Order deleted successfully: " + orderId);
                    ordersList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, ordersList.size());
                } else {
                    Log.e("OrdersAdapter", "Error deleting order: " + task.getException());
                    Toast.makeText(context, "Error al eliminar el pedido.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderTitleTxt, orderTotalTxt;
        ImageView editOrderBtn, deleteOrderBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTitleTxt = itemView.findViewById(R.id.orderTitleTxt);
            orderTotalTxt = itemView.findViewById(R.id.orderTotalTxt);
            editOrderBtn = itemView.findViewById(R.id.editOrderBtn);
            deleteOrderBtn = itemView.findViewById(R.id.deleteOrderBtn);
        }
    }
}
