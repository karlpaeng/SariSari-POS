package com.simsdroid.app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchProductForPOS extends AppCompatActivity implements RecViewInterface{
    EditText searchBar;
    Button search, confirm;
    TextView tvProdName;
    Long prodId;
    RecyclerView recView;
    ArrayList<ModelProducts> prodList = new ArrayList<>();

    ModelProducts returnProd;
    String searchTerm;

    DBHelper dbHalp = new DBHelper(SearchProductForPOS.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setStatusBarColor(ContextCompat.getColor(SearchProductForPOS.this, R.color.mintish));
        getWindow().setNavigationBarColor(ContextCompat.getColor(SearchProductForPOS.this, R.color.mintish));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product_for_pos);
        searchBar = findViewById(R.id.etSearchProdPOS);
        confirm = findViewById(R.id.btnSelectProdToPOS);
        search = findViewById(R.id.btnSearchOnPOSNewActivity);
        tvProdName = findViewById(R.id.tvProdNameOnSearchPOS);
        //
        recView = findViewById(R.id.rec_view_on_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                hideKB();
                searchTerm = searchBar.getText().toString();
                if (checkIfStrValid(searchTerm)){
                    //
                    prodList = dbHalp.searchProductByName(searchTerm);
                    updateRecView();
                    if (prodList.isEmpty()){
                        Toast.makeText(SearchProductForPOS.this, "No results :/", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    //toast
                    Toast.makeText(SearchProductForPOS.this, "Pls enter a valid product name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                if (prodId != null) {
                    returnProd = dbHalp.searchProductsById(prodId);
                    alertDiaText();
                }else{
                    Toast.makeText(SearchProductForPOS.this, "No selected Product", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void updateRecView(){
        //
        RecAdaptInventory adapter = new RecAdaptInventory(prodList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SearchProductForPOS.this);
        recView.setLayoutManager(layoutManager);
        recView.setItemAnimator(new DefaultItemAnimator());
        recView.setAdapter(adapter);
    }
    private boolean checkIfStrValid(String str){
        if(str.equals("")) return false;
        Pattern ps = Pattern.compile("[a-zA-Z0-9\\s]+");
        Matcher ms = ps.matcher(str);
        boolean out = ms.matches();
        return out;
    }
    public void hideKB(){
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClickItem(int position) {
        prodId = prodList.get(position).id;
        tvProdName.setText(prodList.get(position).name);
        tvProdName.setTextColor(ContextCompat.getColor(SearchProductForPOS.this, R.color.darkblue));
    }

    private void alertDiaText(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchProductForPOS.this);
        View v = getLayoutInflater().inflate(R.layout.dialog_text, null);

        TextView top = v.findViewById(R.id.tvTopDiaText);
        EditText content = v.findViewById(R.id.etFieldDiaText);
        Button okBtn = v.findViewById(R.id.btnOkDiaText);
        Button cancBtn = v.findViewById(R.id.btnCancelDiaText);

        content.setInputType(InputType.TYPE_CLASS_NUMBER);

        top.setText("Specify amount");
        content.setText("1");
        content.setHint(returnProd.name);

        builder.setView(v);
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                if(content.getText().toString().equals("")) {
                    //
                    Toast.makeText(SearchProductForPOS.this, "Enter a valid amount", Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(SearchProductForPOS.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id_from_spec_amt", ""+returnProd.id);
                    intent.putExtra("amount_from_spec_amount", content.getText().toString());
                    intent.putExtra("frag", "pos");
                    //
                    alertDialog.dismiss();
                    startActivity(intent);
                }
            }
        });
        cancBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
}