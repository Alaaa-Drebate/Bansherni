package com.example.bansherni.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.bansherni.AddOrderActivity;
import com.example.bansherni.R;
import com.example.bansherni.api.Constants;
import com.example.bansherni.model.Problem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserProblemsAdapter extends RecyclerView.Adapter<UserProblemsAdapter.ViewHolder>{


    Context context;
    private List<Problem> problems;

    // RecyclerView recyclerView;
    public UserProblemsAdapter(Context context, ArrayList<Problem> reports) {
        this.context = context;
        this.problems = reports;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.user_problem_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Problem problem = problems.get(position);

        holder.details.setText(problem.getDetails());

        holder.price.setText(String.valueOf(problem.getPrice()) + context.getResources().getString(R.string.price_unit));

        switch (problem.getType()){
            case Constants.TYPE_BATTERY :
                holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_car_battery));
                break;
            case Constants.TYPE_ENGINE :
                holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_engine));
                break;
            case Constants.TYPE_FUEL :
                holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fuel));
                break;
            case Constants.TYPE_TIRES :
                holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_tire));
                break;
        }

        switch (problem.getStatus()){
            case Constants.REQUEST_STATUS_NEW :
                holder.status.setTextColor(context.getResources().getColor(R.color.status_new));
                holder.status.setText(context.getResources().getString(R.string.status_new));
                break;
            case Constants.REQUEST_STATUS_PROCESSING:
                holder.status.setTextColor(context.getResources().getColor(R.color.status_processing));
                holder.status.setText(context.getResources().getString(R.string.status_processing));
                break;
            case Constants.REQUEST_STATUS_COMPLETE :
                holder.status.setTextColor(context.getResources().getColor(R.color.status_completed));
                holder.status.setText(context.getResources().getString(R.string.status_completed));
                break;
            case Constants.REQUEST_STATUS_REJECTED :
                holder.status.setTextColor(context.getResources().getColor(R.color.status_rejected));
                holder.status.setText(context.getResources().getString(R.string.status_rejected));
                break;
        }


        holder.itemView.setOnLongClickListener(v -> {
            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.delete_confirmation_dialog, null);
            final AlertDialog deleteProductDialog = new AlertDialog.Builder(context).create();
            deleteProductDialog.setView(view);

            TextView yes = view.findViewById(R.id.yes_btn);
            TextView no = view.findViewById(R.id.no_btn);


            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteProblemRequest(position, problem.getId());

                    deleteProductDialog.dismiss();

                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteProductDialog.dismiss();
                }
            });
            deleteProductDialog.show();
            return true;
        });



    }

    private void deleteProblemRequest(int position, int id) {

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post("api/requests/delete.php")
                .addBodyParameter("id", String.valueOf(id))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (obj.getInt("status") == 1) {

                                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                removeProblemRequest(position);

                            } else if(obj.getInt("status") == -1){
                                Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        pDialog.dismiss();
                        Toast.makeText(context, anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void removeProblemRequest(int index) {
        problems.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView details;
        public ImageView type ;
        public TextView status;
        public TextView price;

        public ViewHolder(View itemView) {
            super(itemView);
            this.details = itemView.findViewById(R.id.problem_details);
            this.type = itemView.findViewById(R.id.type_icon);
            this.status = itemView.findViewById(R.id.status_code);
            this.price = itemView.findViewById(R.id.price);
        }
    }





}