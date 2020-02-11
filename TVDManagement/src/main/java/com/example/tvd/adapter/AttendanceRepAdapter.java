package com.example.tvd.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tvd.R;
import com.example.tvd.model.AttendanceSummary;

import java.util.List;

public class AttendanceRepAdapter extends RecyclerView.Adapter<AttendanceRepAdapter.ApproveHolder> {
    private List<AttendanceSummary> arrayList;
    private Context context;

    public AttendanceRepAdapter(Context context, List<AttendanceSummary> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public AttendanceRepAdapter.ApproveHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attendance_summary, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new AttendanceRepAdapter.ApproveHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceRepAdapter.ApproveHolder approveHolder, int i) {
        AttendanceSummary attendanceSummary = arrayList.get(i);
        approveHolder.tv_id.setText(String.valueOf(i + 1));
        approveHolder.tv_name.setText(attendanceSummary.getUSERNAME());
        approveHolder.tv_subdiv.setText(attendanceSummary.getSUBDIVCODE());
        approveHolder.tv_date_time.setText(attendanceSummary.getDATETIME());
        approveHolder.tv_remark.setText(attendanceSummary.getREMARK());
        approveHolder.tv_location.setText(attendanceSummary.getADDRESS());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ApproveHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_subdiv, tv_date_time, tv_remark, tv_id,tv_location;

        public ApproveHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.txt_user_name);
            tv_subdiv = itemView.findViewById(R.id.txt_subdiv_code);
            tv_date_time = itemView.findViewById(R.id.txt_date_time);
            tv_remark = itemView.findViewById(R.id.txt_remark);
            tv_id = itemView.findViewById(R.id.txt_id);
            tv_location= itemView.findViewById(R.id.txt_location);
        }
    }
}
