package com.simsdroid.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context) {
        super(context, "SariSari.db", null, 2);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE products (" +
                "prod_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "prod_name TEXT, " +
                "barcode TEXT, " +
                "cost TEXT, " +
                "retail_price TEXT, " +
                "amount_stock INTEGER, " +
                "last_update TEXT)"
        );
        sqLiteDatabase.execSQL("CREATE TABLE orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_number INTEGER, " +
                "product_name TEXT, " +
                "product_id INTEGER, " +
                "retail_price TEXT, " +
                "amount INTEGER, " +
                "amountxprice TEXT)"
        );
        sqLiteDatabase.execSQL("CREATE TABLE debts (" +
                "debt_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_number INTEGER, " +
                "cust_name TEXT, " +
                "cust_contact TEXT, " +
                "date_checkout TEXT, " +
                "date_paid TEXT)"
        );
        sqLiteDatabase.execSQL("CREATE TABLE user_info (" +
                "id INTEGER PRIMARY KEY, " +
                "store_name TEXT, " +
                "pin TEXT)"
        );
        sqLiteDatabase.execSQL("CREATE TABLE order_numbers (" +
                "order_number INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "time TEXT, " +
                "total TEXT)");
        sqLiteDatabase.execSQL("CREATE TABLE temp_orders (" +
                "temp_order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_number INTEGER, " +
                "product_name TEXT, " +
                "product_id INTEGER, " +
                "retail_price TEXT, " +
                "amount INTEGER, " +
                "amountxprice TEXT)"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void xxxResetDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM products;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='products';");//reset primary key to 1
        db.execSQL("DELETE FROM orders;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='orders';");//reset primary key to 1
        db.execSQL("DELETE FROM debts;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='debts';");//reset primary key to 1
        db.execSQL("DELETE FROM user_info;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='user_info';");//reset primary key to 1
        db.execSQL("DELETE FROM order_numbers;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='order_numbers';");//reset primary key to 1
        db.execSQL("DELETE FROM temp_orders;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='temp_orders';");//reset primary key to 1
        db.close();
    }
//-------------------------------     USER INFO TABLE
    public void createUserInfo(String storeName, String PIN){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", 1);
        cv.put("store_name", storeName);
        cv.put("pin", PIN);

        long i = db.insert("user_info", null, cv);

        db.close();
    }

    public boolean checkExistingUserInfo(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM user_info WHERE id = 1;", null);
        boolean b = cursor.moveToFirst();
        cursor.close();
        db.close();
        return b;
    }

    public void updateStoreName(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user_info SET store_name = '" + name + "' WHERE id = 1;");
        db.close();
    }
    public void updatePIN(String pin){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE user_info SET pin = '" + pin + "' WHERE id = 1;");
        db.close();
    }
    public String getPIN(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT pin FROM user_info WHERE id = 1;", null);
        String ret = "";
        if(cursor.moveToFirst()) {
            ret = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return ret;
    }
    public String getStoreName(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT store_name FROM user_info WHERE id = 1;", null);
        String ret = "";
        if (cursor.moveToFirst()) {
            ret = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return ret;
    }

    //--------------------------------     PRODUCTS TABLE

    public long addProduct(ModelProducts product){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("prod_name", product.name);
        cv.put("barcode", product.barcode);
        cv.put("cost", String.valueOf(product.cost));
        cv.put("retail_price", String.valueOf(product.retailPrice));
        cv.put("amount_stock", product.amountStock);
        cv.put("last_update", product.lastUpdate);

        long i = db.insert("products", null, cv);

        db.close();
        return i;
    }

    public void updateProduct(long id, ModelProducts product){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE products " +
                "SET " +
                "prod_name = '" + product.name + "', " +
                "cost = '" + product.cost + "', " +
                "retail_price = '" + product.retailPrice + "', "+
                "amount_stock = " + product.amountStock + ", " +
                "last_update = '" + product.lastUpdate + "' " +
                "WHERE prod_id = " + id + ";");
        db.close();

    }
    public void clearAllProducts(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM products;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='products';");//reset primary key to 1
        db.close();
    }
    //products list, list search by name, by barcode, barcode exists
    public ArrayList<ModelProducts> allProductsInventory(int limit){
        String lim = (limit > 0 ? "LIMIT "+limit+";" : ";");
        ArrayList<ModelProducts> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products ORDER BY prod_name ASC " + lim, null);
        if (cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String barcode = cursor.getString(2);
                BigDecimal cost = new BigDecimal(cursor.getString(3));
                BigDecimal retail = new BigDecimal(cursor.getString(4));
                int amt = cursor.getInt(5);
                String update = cursor.getString(6);

                ModelProducts product = new ModelProducts(id, name, barcode, cost, retail, amt, update);

                products.add(product);
            }while (cursor.moveToNext());
        }

        db.close();
        cursor.close();
        return products;
    }

    public ArrayList<ModelProducts> searchProductByName(String prod_name){
        ArrayList<ModelProducts> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products WHERE prod_name LIKE '%" + prod_name + "%';", null);
        if (cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String barcode = cursor.getString(2);
                BigDecimal cost = new BigDecimal(cursor.getString(3));
                BigDecimal retail = new BigDecimal(cursor.getString(4));
                int amt = cursor.getInt(5);
                String update = cursor.getString(6);

                ModelProducts product = new ModelProducts(id, name, barcode, cost, retail, amt, update);

                products.add(product);
            }while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return products;
    }
    public ModelProducts searchProductByBarcode(String barcodestr){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products WHERE barcode = '" + barcodestr + "';", null);
        cursor.moveToFirst();

        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        String barcode = cursor.getString(2);
        BigDecimal cost = new BigDecimal(cursor.getString(3));
        BigDecimal retail = new BigDecimal(cursor.getString(4));
        int amt = cursor.getInt(5);
        String update = cursor.getString(6);

        ModelProducts product = new ModelProducts(id, name, barcode, cost, retail, amt, update);

        db.close();
        cursor.close();

        return product;
    }
    public ModelProducts searchProductsById(long prodid){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products WHERE prod_id = " + prodid + ";", null);
        cursor.moveToFirst();

        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        String barcode = cursor.getString(2);
        BigDecimal cost = new BigDecimal(cursor.getString(3));
        BigDecimal retail = new BigDecimal(cursor.getString(4));
        int amt = cursor.getInt(5);
        String update = cursor.getString(6);

        ModelProducts product = new ModelProducts(id, name, barcode, cost, retail, amt, update);

        db.close();
        cursor.close();

        return product;
    }
    public boolean barcodeExists(String bc){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT prod_id FROM products WHERE barcode = '" + bc + "';", null);
        boolean ret = cursor.moveToFirst();
        cursor.close();
        db.close();
        return ret;
    }
    //inv value and retail value, profit
    public BigDecimal computeInventoryValue(){
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal temp, temp2;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT cost, amount_stock FROM products;", null);
        if (cursor.moveToFirst()){
            do {
                temp = new BigDecimal(cursor.getString(0));
                temp2 = new BigDecimal(cursor.getInt(1));
                total = total.add(temp.multiply(temp2));
            }while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return total;
    }
    public BigDecimal computeRetailValue(){
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal temp, temp2;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT retail_price, amount_stock FROM products;", null);
        if (cursor.moveToFirst()){
            do {
                temp = new BigDecimal(cursor.getString(0));
                temp2 = new BigDecimal(cursor.getInt(1));
                total = total.add(temp.multiply(temp2));
            }while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return total;
    }
//------------ ?

    public void createOrder(ArrayList<ModelOrders> orders, String xdate, String xtime, long orderNumnum){ //subtract from amount in stock, create records in orders table
        SQLiteDatabase db = this.getWritableDatabase();
        BigDecimal total = new BigDecimal("0.0");
        for (ModelOrders order : orders) {
            ContentValues cv = new ContentValues();

            cv.put("order_number", orderNumnum);
            cv.put("product_name", order.productName);
            cv.put("product_id", order.productId);
            cv.put("retail_price", String.valueOf(order.retailPrice));
            cv.put("amount", order.amount);
            cv.put("amountxprice", String.valueOf(order.amountXprice));

            long i = db.insert("orders", null, cv);

            Cursor cursor = db.rawQuery("SELECT amount_stock FROM products WHERE prod_id = " + order.productId + ";", null);
            cursor.moveToFirst();
            int tempAmt = cursor.getInt(0);

            cursor.close();

            tempAmt = tempAmt - order.amount;

            if (tempAmt < 0) tempAmt = 0;

            db.execSQL("UPDATE products SET amount_stock = " + tempAmt + " WHERE prod_id = " + order.productId + ";");

            total = total.add(order.amountXprice);
        }
        //total
        db.execSQL("UPDATE order_numbers SET total = '" + total + "', date = '" + xdate + "', time = '" + xtime + "' WHERE order_number = " + orderNumnum + ";");
        db.close();
    }

    public long getOrderNumber(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("total", "");
        cv.put("date", "");
        cv.put("time", "");

        long i = db.insert("order_numbers", null, cv);

        Cursor cursor = db.rawQuery("SELECT order_number FROM order_numbers ORDER BY order_number DESC LIMIT 1;", null);
        cursor.moveToFirst();

        long returnLong = cursor.getLong(0);

        cursor.close();
        db.close();

        return returnLong;
    }

    public ArrayList<ModelOrders> getOrderInfo(long orderNumber){
        ArrayList<ModelOrders> ordersList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM orders WHERE order_number = " + orderNumber + ";", null);
        if (cursor.moveToFirst()){
            do{
                int orderNum = cursor.getInt(1);
                String productName = cursor.getString(2);
                int productId = cursor.getInt(3);
                BigDecimal retailPrice = new BigDecimal(cursor.getString(4));
                int amount = cursor.getInt(5);
                BigDecimal amountXprice = new BigDecimal(cursor.getString(6));

                ModelOrders order = new ModelOrders(
                        orderNum,
                        productName,
                        productId,
                        retailPrice,
                        amount,
                        amountXprice
                );

                ordersList.add(order);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ordersList;
    }

    public ModelOrderView getSimpleOrderInfo(long orderNum){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();
        Cursor cursor = db.rawQuery("SELECT * FROM order_numbers WHERE order_number = " + orderNum + ";", null);
        cursor.moveToFirst();

        String date = cursor.getString(1);
        String time = cursor.getString(2);

        BigDecimal returnBD = new BigDecimal(String.valueOf(cursor.getString(3)));

        ModelOrderView orderView = new ModelOrderView(orderNum, date, time, returnBD);
        cursor.close();
        db.close();

        return orderView;
    }
    public ArrayList<ModelOrderView> getOrderHistory(int limit){
        String lim = (limit > 0 ? "LIMIT "+limit+";" : ";");
        ArrayList<ModelOrderView> orderViews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM order_numbers WHERE NOT total = '' ORDER BY order_number DESC " +lim, null);
        if (cursor.moveToFirst()){
            do{
                int num = cursor.getInt(0);
                String date = cursor.getString(1);
                String time = cursor.getString(2);
                BigDecimal tot = new BigDecimal(cursor.getString(3));

                ModelOrderView order = new ModelOrderView(num, date, time, tot);
                orderViews.add(order);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orderViews;

    }
    public ArrayList<ModelOrderView> searchOrderNumByDate(String datestr){
        ArrayList<ModelOrderView> orderViews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM order_numbers WHERE date = '" + datestr + "' ORDER BY order_number DESC;", null);
        if (cursor.moveToFirst()){
            do{
                int num = cursor.getInt(0);
                String date = cursor.getString(1);
                String time = cursor.getString(2);
                BigDecimal tot = new BigDecimal(cursor.getString(3));

                ModelOrderView order = new ModelOrderView(num, date, time, tot);
                orderViews.add(order);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orderViews;
    }
    public ArrayList<ModelOrderView> searchOrderNumById(long idVal){
        ArrayList<ModelOrderView> orderViews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM order_numbers WHERE order_number = " + idVal + " AND NOT total = '' ORDER BY order_number DESC;", null);
        if (cursor.moveToFirst()){
            do{
                int num = cursor.getInt(0);
                String date = cursor.getString(1);
                String time = cursor.getString(2);
                String bdStr = "";
                if(cursor.getString(3).equals("")){
                    bdStr = "0.0";
                }else {
                    bdStr = cursor.getString(3);
                }
                BigDecimal tot = new BigDecimal(bdStr);

                ModelOrderView order = new ModelOrderView(num, date, time, tot);
                orderViews.add(order);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orderViews;
    }
    //for excel

    public ArrayList<ModelOrders> getAllOrdersForExcel(String date){
        String whereClause = (date.equals("all") ? "" : " WHERE order_number IN (SELECT order_number FROM order_numbers WHERE date = '" + date + "')");
        ArrayList<ModelOrders> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM orders" + whereClause + " ORDER BY order_number DESC;", null);
        if (cursor.moveToFirst()){
            do{
                //

                long orderNumber = cursor.getLong(1);
                String productName = cursor.getString(2);
                BigDecimal retailPrice = new BigDecimal(String.valueOf(cursor.getString(4)));
                int amount = cursor.getInt(5);
                BigDecimal amountXprice = new BigDecimal(String.valueOf(cursor.getString(6)));

                ModelOrders order = new ModelOrders(orderNumber,productName,-1,retailPrice,amount,amountXprice);
                orders.add(order);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orders;
    }
    //debts
    public void addDebt(ModelDebts debt){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("order_number", debt.orderNumber);
        cv.put("cust_name", debt.customerName);
        cv.put("cust_contact", debt.customerContact);
        cv.put("date_checkout", debt.dateCheckout);
        cv.put("date_paid", debt.datePaid);

        long i = db.insert("debts", null, cv);

        db.close();
    }

    public ArrayList<ModelDebts> getDebtList(String tag){
        ArrayList<ModelDebts> debtList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String whereClause = "";
        if (tag.equals("paid")){
            whereClause = "WHERE NOT date_paid = '-'";
        }else if (tag.equals("unpaid")){
            whereClause = "WHERE date_paid = '-'";
        }else if (tag.equals("all")){
            whereClause = "";
        }
        Cursor cursor = db.rawQuery("SELECT * FROM debts " + whereClause + " ORDER BY debt_id DESC;", null);
        if (cursor.moveToFirst()){
            do {
                long ordNum = cursor.getInt(1);
                String name = cursor.getString(2);
                String cont = cursor.getString(3);
                String check = cursor.getString(4);
                String paid = cursor.getString(5);

                ModelDebts debt = new ModelDebts(ordNum, name, cont, check, paid);
                debtList.add(debt);
            }while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return debtList;
    }
    public ModelDebts getDebtInfo(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM debts WHERE order_number = " + id + ";", null);
        ModelDebts modelDebts = new ModelDebts();
        if (cursor.moveToFirst()) {
            long ordNum = cursor.getInt(1);
            String name = cursor.getString(2);
            String cont = cursor.getString(3);
            String check = cursor.getString(4);
            String paid = cursor.getString(5);
            modelDebts = new ModelDebts(ordNum, name, cont, check, paid);
        }

        return modelDebts;
    }
    public void updateDebt(long orderNumber, String paidDate, String name, String contact){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE debts SET date_paid = '" + paidDate + "'," +
                " cust_name = '" + name + "', " +
                "cust_contact = '" + contact + "' " +
                "WHERE order_number = " + orderNumber + ";");
        db.close();
    }

    public void addToTempOrder(ModelOrders order){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("product_name", order.productName);
        cv.put("product_id", order.productId);
        cv.put("retail_price", String.valueOf(order.retailPrice));
        cv.put("amount", order.amount);
        cv.put("amountxprice", String.valueOf(order.amountXprice));

        long i = db.insert("temp_orders", null, cv);

        db.close();
    }

    public void removeFromTempOrder(long position){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM temp_orders WHERE temp_order_id = " + (position) + ";");
        db.close();
    }

    public void clearTempOrder(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM temp_orders;");
        db.execSQL("UPDATE sqlite_sequence SET seq=0 WHERE NAME='temp_orders';");//reset primary key to 1
        db.close();
    }

    public ArrayList<ModelOrders> getAllTempOrder(){
        ArrayList<ModelOrders> ordersList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM temp_orders;", null);
        if (cursor.moveToFirst()){
            do{
                int orderNum = cursor.getInt(0);
                String productName = cursor.getString(2);
                int productId = cursor.getInt(3);
                BigDecimal retailPrice = new BigDecimal(cursor.getString(4));
                int amount = cursor.getInt(5);
                BigDecimal amountXprice = new BigDecimal(cursor.getString(6));

                ModelOrders order = new ModelOrders(
                        orderNum,
                        productName,
                        productId,
                        retailPrice,
                        amount,
                        amountXprice
                );

                ordersList.add(order);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ordersList;
    }
    public BigDecimal getTotalTempOrder(){
        //
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal temp;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT amountxprice FROM temp_orders;", null);
        if (cursor.moveToFirst()){
            do {
                temp = new BigDecimal(cursor.getString(0));
                total = total.add(temp);
            }while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return total;
    }
}
