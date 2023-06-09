package com.simsdroid.app;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.simsdroid.app.databinding.ActivityMainBinding;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    TextView actionBar;

    ArrayList<ModelProducts> productListOfInventory = new ArrayList<>();
    BigDecimal invVal, retval, potProf;

    DBHelper dbHalp = new DBHelper(MainActivity.this);
    ExcelHelper xcHalp = new ExcelHelper();

    ArrayList<ModelOrders> orderListForPOS = new ArrayList<>();
    String barcodeStr;
    BigDecimal totalForPOS = new BigDecimal("0.0");

    long l;
    String date, time;

    ModelProducts prodForSpecAmt;

    int indx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setNavigationBarColor(ContextCompat.getColor(MainActivity.this, R.color.lightGr));
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,

        }, PackageManager.PERMISSION_GRANTED);
        //move this later

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        actionBar = findViewById(R.id.actionBar);
        orderListForPOS = dbHalp.getAllTempOrder();
        totalForPOS = dbHalp.getTotalTempOrder();
        String extraStr="";
        if (getIntent().getStringExtra("frag") == null) {

            replaceFragment(new Frag1POS());
            binding.bottomNavigationView.setSelectedItemId(R.id.pos);
            actionBar.setText("POS (Point of Sale)");

            if(getIntent().getStringExtra("new_user") != null && getIntent().getStringExtra("new_user").equals("true")){
                alertDiaTut("Skip");
            }
            //Toast.makeText(MainActivity.this, "null", Toast.LENGTH_LONG).show();
        }else {
            extraStr = getIntent().getStringExtra("frag");

            //Toast.makeText(MainActivity.this, extraStr, Toast.LENGTH_LONG).show();
            if (extraStr.equals("pos")){
                //
                //Toast.makeText(MainActivity.this, "null", Toast.LENGTH_LONG).show();
                totalForPOS = dbHalp.getTotalTempOrder();
                ModelProducts productForPOS = dbHalp.searchProductsById(Long.parseLong(getIntent().getStringExtra("id_from_spec_amt")));
                int amtForPOS = Integer.parseInt(getIntent().getStringExtra("amount_from_spec_amount"));
                BigDecimal amtXprice = productForPOS.retailPrice.multiply(BigDecimal.valueOf(amtForPOS));
                totalForPOS = totalForPOS.add(amtXprice);

                ModelOrders oneOrder = new ModelOrders(
                        1L,
                        productForPOS.name,
                        productForPOS.id,
                        productForPOS.retailPrice,
                        amtForPOS,
                        amtXprice
                );
                dbHalp.addToTempOrder(oneOrder);
                orderListForPOS = dbHalp.getAllTempOrder();

                replaceFragment(new Frag1POS());
                binding.bottomNavigationView.setSelectedItemId(R.id.pos);
                actionBar.setText("POS (Point of Sale)");

            }else if (extraStr.equals("mnge")){
                //
                updateMngeData();
                replaceFragment(new Frag2Manage());
                binding.bottomNavigationView.setSelectedItemId(R.id.mnge);
                actionBar.setText("Manage Inventory");
            }else if (extraStr.equals("hist")){
                //
            }else if (extraStr.equals("debt")){
                //
                replaceFragment(new Frag4Debt());
                binding.bottomNavigationView.setSelectedItemId(R.id.debt);
                actionBar.setText("Customer Debt");
            }else if (extraStr.equals("othr")){
                //
            }
        }




        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.pos:

                    replaceFragment(new Frag1POS());
                    //set text
                    actionBar.setText("POS (Point of Sale)");
                    break;
                case R.id.mnge:

                    replaceFragment(new Frag2Manage());
                    //set text
                    actionBar.setText("Manage Inventory");
                    break;
                case R.id.hist:

                    replaceFragment(new Frag3History());
                    //set text
                    actionBar.setText("View Order History");
                    break;
                case R.id.debt:

                    replaceFragment(new Frag4Debt());
                    //set text
                    actionBar.setText("Customer Debt");
                    break;
                case R.id.othr:

                    replaceFragment(new Frag5Other());
                    //set text
                    actionBar.setText("Settings");
                    break;

            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentLayout, fragment);
        fragmentTransaction.commit();

    }
    public void updateMngeData(){
        invVal = dbHalp.computeInventoryValue();
        retval = dbHalp.computeRetailValue();
        potProf = retval.subtract(invVal);
    }
    public void updateProductListOfInventory(int limit){
        productListOfInventory = dbHalp.allProductsInventory(limit);
    }
    public void searchProdsByName(String name){
        productListOfInventory = dbHalp.searchProductByName(name);
    }
    public void hideKB(){
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    public void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Press volume up button to turn on flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            //result.getContents();
            Toast.makeText(MainActivity.this, "" + result.getContents(), Toast.LENGTH_SHORT).show();
            barcodeStr = result.getContents();
            //
            if(dbHalp.barcodeExists(barcodeStr)){
                //
                prodForSpecAmt = dbHalp.searchProductByBarcode(barcodeStr);
                alertDiaText("Specify amount", prodForSpecAmt.name);

            }else{
                alertDia("UNKNOWN PRODUCT", "Scanned Barcode is not recognized");
            }


        }else{
            alertDia("Error", "Scan failed. Try again.");
        }
    });
    public void alertDia(String buildTitle, String buildMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View v = getLayoutInflater().inflate(R.layout.dialog_generic, null);

        TextView top = v.findViewById(R.id.tvTopDiaGen);
        TextView content = v.findViewById(R.id.tvContentDiaGen);
        Button okBtn = v.findViewById(R.id.btnOkDiaGen);

        top.setText(buildTitle);
        content.setText(buildMessage);
        builder.setView(v);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();

            }
        });

    }
    public void removeFromPosList(int position){
        Toast.makeText(MainActivity.this, "Removed: " + orderListForPOS.get(position).productName , Toast.LENGTH_SHORT).show();
        dbHalp.removeFromTempOrder(orderListForPOS.get(position).orderNumber);
        totalForPOS = totalForPOS.subtract(orderListForPOS.get(position).amountXprice);
        orderListForPOS = dbHalp.getAllTempOrder();

    }
    public void checkOutOrder(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MMM-dd");
        date = simpleDate.format(calendar.getTime());
        simpleDate = new SimpleDateFormat("hh:mm:ss a");
        time = simpleDate.format(calendar.getTime());
        l = dbHalp.getOrderNumber();
        dbHalp.createOrder(orderListForPOS, date, time, l);
        orderListForPOS.clear();
        dbHalp.clearTempOrder();
        totalForPOS = new BigDecimal("0.0");
        //goto receipt
        Toast.makeText(MainActivity.this, "Checked out " , Toast.LENGTH_SHORT).show();

    }
    public String ReadFromFile(String fileName){
        String line,line1 = "";
        File filePath = new File(MainActivity.this.getExternalFilesDir(null) + "/" + fileName);
        try{
            if(filePath.exists()) filePath.createNewFile();
            else filePath = new File(MainActivity.this.getExternalFilesDir(null) + "/" + fileName);

            InputStream instream = new FileInputStream(filePath);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                try {
                    while ((line = buffreader.readLine()) != null)
                        line1= line1 + line + "\n";
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            instream.close();
            //Log.e("TAG", "Update to file: "+fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return line1;
    }
    public void WriteToFile(String fileName, String content){
        File filePath = new File(MainActivity.this.getExternalFilesDir(null) + "/" + fileName);
        try{
            if(filePath.exists()) filePath.createNewFile();
            else filePath = new File(MainActivity.this.getExternalFilesDir(null) + "/" + fileName);

            FileOutputStream writer = new FileOutputStream(filePath);
            writer.write(content.getBytes());
            writer.flush();
            writer.close();
            //Log.e("TAG", "Wrote to file: "+fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void alertDiaText(String title, String name){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View v = getLayoutInflater().inflate(R.layout.dialog_text, null);

        TextView top = v.findViewById(R.id.tvTopDiaText);
        EditText content = v.findViewById(R.id.etFieldDiaText);
        Button okBtn = v.findViewById(R.id.btnOkDiaText);
        Button cancBtn = v.findViewById(R.id.btnCancelDiaText);

        content.setInputType(InputType.TYPE_CLASS_NUMBER);

        top.setText(title);
        content.setText("1");
        content.setHint(name);

        builder.setView(v);
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                totalForPOS = dbHalp.getTotalTempOrder();
//                ModelProducts productForPOS = dbHalp.searchProductsById(Long.parseLong(getIntent().getStringExtra("id_from_spec_amt")));
                int amtForPOS = Integer.parseInt(content.getText().toString());
                BigDecimal amtXprice = prodForSpecAmt.retailPrice.multiply(BigDecimal.valueOf(amtForPOS));
                totalForPOS = totalForPOS.add(amtXprice);

                ModelOrders oneOrder = new ModelOrders(
                        1L,
                        prodForSpecAmt.name,
                        prodForSpecAmt.id,
                        prodForSpecAmt.retailPrice,
                        amtForPOS,
                        amtXprice
                );
                dbHalp.addToTempOrder(oneOrder);
                orderListForPOS = dbHalp.getAllTempOrder();

                Frag1POS testFragment = new Frag1POS();

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentLayout, testFragment).commitNow();

                testFragment.updateRecView();
                testFragment.total.setText(String.valueOf(totalForPOS));

                alertDialog.dismiss();

            }
        });
        cancBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
    public void copyToClip(String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("link", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "Link copied to clipboard" , Toast.LENGTH_SHORT).show();

    }
    public void alertDiaTut(String tag){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View v = getLayoutInflater().inflate(R.layout.dialog_tutorial, null);
        TextView back = v.findViewById(R.id.tvBack);
        TextView forw = v.findViewById(R.id.tvForw);
        TextView skip = v.findViewById(R.id.tvSkip);
        ImageView imgs = v.findViewById(R.id.ivSlideImgTut);
        TextView descri = v.findViewById(R.id.tvDescriTutorial);

        skip.setText(tag);

        int[] imgID = {R.drawable.tut1, R.drawable.tut2, R.drawable.tut3, R.drawable.tut4, R.drawable.tut5, R.drawable.tut6};
        String[] descStr = {
                "Adding and updating product data",
                "Managing items in POS",
                "Adding items by scanning or searching",
                "Options for checking out order",
                "Viewing and searching order records and receipt",
                "Managing and updating customer debt info"
        };
        indx = 0;

        descri.setText(descStr[indx]);

        back.setText("<<<");
        forw.setText(">>>");

        back.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.lightGr));
        forw.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darkblue));


        imgs.setImageResource(imgID[indx]);
        builder.setView(v);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.show();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (indx > 0){
                    if (indx==1) back.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.lightGr));
                    forw.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darkblue));
                    indx--;
                    imgs.setImageResource(imgID[indx]);
                    descri.setText(descStr[indx]);
                }
            }
        });
        forw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(indx<5){
                    if (indx==4) {
                        forw.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.lightGr));
                        skip.setText("Close");
                    }
                    back.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darkblue));
                    indx++;
                    imgs.setImageResource(imgID[indx]);
                    descri.setText(descStr[indx]);


                }
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }



}