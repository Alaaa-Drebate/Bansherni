package com.example.bansherni.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.bansherni.AdminProblemDetailsActivity;
import com.example.bansherni.R;
import com.example.bansherni.api.Constants;
import com.example.bansherni.model.Problem;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminProblemsAdapter extends RecyclerView.Adapter<AdminProblemsAdapter.ViewHolder>{


    Context context;
    private List<Problem> problems;

    int chosenStatus = -1;


    // RecyclerView recyclerView;
    public AdminProblemsAdapter(Context context, ArrayList<Problem> reports) {
        this.context = context;
        this.problems = reports;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.admin_problem_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Problem problem = problems.get(position);

        holder.userName.setText(problem.getUser_name());
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

        holder.itemView.setOnClickListener(v -> {
            Intent details = new Intent(context, AdminProblemDetailsActivity.class);
            details.putExtra("problem_id", problem.getId());//
            details.putExtra("user_id", problem.getUser_id());//
            details.putExtra("user_name", problem.getUser_name());
            details.putExtra("phone", problem.getPhone_number());
            details.putExtra("details", problem.getDetails());
            details.putExtra("type", problem.getType());
            details.putExtra("status", problem.getStatus());
            details.putExtra("lat", problem.getLat());
            details.putExtra("lon", problem.getLon());
            details.putExtra("price", problem.getPrice());
            context.startActivity(details);
        });


        holder.itemView.setOnLongClickListener(v -> {
            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.status_chooser_dialog, null);
            final AlertDialog statusChooserDialog = new AlertDialog.Builder(context).create();
            statusChooserDialog.setView(view);

            TextView yes = view.findViewById(R.id.yes_btn);
            TextView no = view.findViewById(R.id.no_btn);
            RadioGroup group = view.findViewById(R.id.chooser_layout);
            chosenStatus = -1;

            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId){
                        case R.id.state_new:
                            chosenStatus = Constants.REQUEST_STATUS_NEW;
                            break;
                        case R.id.state_processing:
                            chosenStatus = Constants.REQUEST_STATUS_PROCESSING;
                            break;
                        case R.id.state_completed:
                            chosenStatus = Constants.REQUEST_STATUS_COMPLETE;
                            break;
                        case R.id.state_rejected:
                            chosenStatus = Constants.REQUEST_STATUS_REJECTED;
                            break;
                    }
                }
            });

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(chosenStatus == -1){
                        Toast.makeText(context, context.getResources().getString(R.string.chose_at_least_one_status), Toast.LENGTH_SHORT).show();
                    }else
                    if(chosenStatus == problem.getStatus()){
                        Toast.makeText(context, context.getResources().getString(R.string.must_chose_new_status), Toast.LENGTH_SHORT).show();
                    }else{
                        problem.setStatus(chosenStatus);
                        updateProblemStatus(problem, position);
                        statusChooserDialog.dismiss();
                    }



                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusChooserDialog.dismiss();
                }
            });
            statusChooserDialog.show();
            return true;
        });



    }

    private void updateProblemStatus(Problem problem, int index) {

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post("api/requests/update.php")
                .addBodyParameter("request_id", String.valueOf(problem.getId()))
                .addBodyParameter("status", String.valueOf(problem.getStatus()))
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
                                updateProblem(index, problem);

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

    private void updateProblem(int index, Problem problem) {
        problems.set(index, problem);
        notifyItemChanged(index);
    }



    @Override
    public int getItemCount() {
        return problems.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userName;
        public TextView details;
        public ImageView type ;
        public TextView status;
        public TextView price;

        public ViewHolder(View itemView) {
            super(itemView);
            this.userName = itemView.findViewById(R.id.user_name);
            this.details = itemView.findViewById(R.id.problem_details);
            this.type = itemView.findViewById(R.id.type_icon);
            this.status = itemView.findViewById(R.id.status_code);
            this.price = itemView.findViewById(R.id.price);
        }
    }





}