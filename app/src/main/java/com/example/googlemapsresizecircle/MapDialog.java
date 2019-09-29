package com.example.googlemapsresizecircle;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.zip.Inflater;

public class MapDialog extends DialogFragment {
    Button ok_btn;
    Button cancel_btn;
    EditText radius_value;


    public MapDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        double radius = getArguments().getDouble("radius");
        Log.d("radius", radius + "");
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.my_dialog, null, false);
//        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.ThemeOverlay_AppCompat_Dialog).setView(view).create();

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.MyDialog).setView(view).create();
        initView(view);
        return alertDialog;
    }

    private void initView(View view) {
        ok_btn = view.findViewById(R.id.buttonOk);
        cancel_btn = view.findViewById(R.id.cancel);
        radius_value = view.findViewById(R.id.radiusInput);
        radius_value.setText(getArguments().getDouble("radius")+"");


        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String radius = radius_value.getText().toString();

                DialogListener dialogListener = (DialogListener) getActivity();
                dialogListener.onFinishEditDialog(Double.parseDouble(radius));
                dismiss();
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
    public interface DialogListener {
        void onFinishEditDialog(Double inputText);
    }

}
