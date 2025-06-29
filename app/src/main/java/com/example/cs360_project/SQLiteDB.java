package com.example.cs360_project;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import grid.StockItem;

public class SQLiteDB extends SQLiteOpenHelper {
    private static SQLiteDB instance; // singleton
    static final String FILENAME = "database.db";
    static final String USERS_TABLE_NAME = "users";
    static final String STOCK_TABLE_NAME = "stock";

    public static SQLiteDB getInstance() {
        if (instance == null) {
            instance = new SQLiteDB();
        }
        return instance;
    }

    public SQLiteDB() {
        super(WarehouseApplication.getInstance().getAppCtx(), FILENAME, null, 1);
    }

    //region setting up database
    @Override
    public void onCreate(SQLiteDatabase db) {
        // onCreate is called if no database exists, then creates one, so define schema here.

        String rolesCheckArgs = "'" + UserRoles.USER_ROLE + "','" +
                UserRoles.MANAGER_ROLE + "','" +
                UserRoles.ADMIN_ROLE + "'";

        // USERS TABLE
        String createUserTable = "CREATE TABLE users ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL CHECK(role IN (" +
                rolesCheckArgs +
                ")));";

        db.execSQL(createUserTable);

        insertUsers(db);

        // STOCK TABLE (Items)
        String createStockTable = "CREATE TABLE stock (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "sku TEXT," +
                "price REAL," +
                "imageName TEXT," +
                "stockCount INTEGER);";

        db.execSQL(createStockTable);

        insertItems(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // populating DB...
    private void insertItems(SQLiteDatabase db) {
        List<String> textData = new ArrayList<>();
        List<StockItem> stockItems = new ArrayList<>();
        var appCtx = WarehouseApplication.getInstance().getAppCtx();

        // get items txt
        try {
            //https://stackoverflow.com/questions/5868369/how-can-i-read-a-large-text-file-line-by-line-using-java
            InputStream stream = appCtx.getResources().openRawResource(R.raw.test_items);

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            List<String> lines = new ArrayList<>();

            String currentLine;

            reader.readLine(); //skip first line

            while ((currentLine = reader.readLine()) != null) {
                lines.add(currentLine);
            }

            textData = lines;

        } catch (Exception e) {
            String msg = e.getMessage(); // exception msg has possible null return? java quirk I guess...
            Log.d("Item insert error", msg == null ? "C# > Java" : msg);
        }

        // create item objects with data
        for (var line : textData) {
            // text data format:
            //      name, sku, price, imageName, stockCount
            var cols = line.split(",");

            var name = cols[0].trim();
            var sku = cols[1].trim();
            var price = Double.parseDouble(cols[2].trim());
            var imageName = cols[3].trim();
            var stockCount = Integer.parseInt(cols[4].trim());

            var item = new StockItem(name, sku, price, imageName, stockCount);
            stockItems.add(item);
        }

        for (var item : stockItems) {
            // insert to db
            Long id = db.insert(STOCK_TABLE_NAME, null, item.getContentVals());
            item.setDbId(id);
        }
    }

    private void insertUsers(SQLiteDatabase db) {
        // creates two test users that can be used without having to register
        ContentValues userValues = new ContentValues();
        userValues.put("username", "testUser");
        userValues.put("password", "weakPassword");
        userValues.put("role", UserRoles.USER_ROLE);
        db.insert(USERS_TABLE_NAME, null, userValues);

        ContentValues managerValues = new ContentValues();
        managerValues.put("username", "testManager");
        managerValues.put("password", "weakPassword");
        managerValues.put("role", UserRoles.MANAGER_ROLE);
        db.insert(USERS_TABLE_NAME, null, managerValues);
    }

    //endregion

    //region querying/reading
    public boolean tryLogin(String user, String pass) {
        // serves as onclick for login button.
        // take input vals and compare with db.
        var db = this.getReadableDatabase();

        String[] cols = {"id"};
        String selector = "username = ? AND password = ?";
        String[] filters = {
                user,
                pass
        };

        // try query
        boolean match = false;
        try (Cursor c = db.query(
                USERS_TABLE_NAME,
                cols,
                selector,
                filters,
                null, null, null
        )) {
            if (c.getCount() != 0) match = true; //username is unique so should only ever get 1 result max
        }

        return match;
    }

    public boolean isUsernameUnique(String username) {
        var db = this.getReadableDatabase();

        var cursor = db.query("users", new String[]{"id"}, "username = ?", new String[]{username}, null, null, null);
        boolean isUnique = cursor.getCount() == 0;
        cursor.close();

        return isUnique;
    }

    public boolean isSkuUnique(String sku){
        var db = this.getReadableDatabase();

        var cursor = db.query(STOCK_TABLE_NAME, new String[]{"id"}, "sku = ?", new String[]{sku}, null, null, null);
        boolean isUnique = cursor.getCount() == 0;
        cursor.close();

        return isUnique;
    }

    public List<StockItem> getStockItems() {
        var db = instance.getReadableDatabase();
        List<StockItem> retList = new ArrayList<>();
        // get all item entries in "stock" table
        var cursor = db.query(STOCK_TABLE_NAME, null, null, null, null, null, null);

        final int NAME_COL = cursor.getColumnIndex("name");
        final int ID_COL = cursor.getColumnIndex("id");
        final int SKU_COL = cursor.getColumnIndex("sku");
        final int PRICE_COL = cursor.getColumnIndex("price");
        final int IMG_COL = cursor.getColumnIndex("imageName");
        final int STOCK_COL = cursor.getColumnIndex("stockCount");

        while (cursor.moveToNext()) {
            // create StockItem objects and add to list
            var item = new StockItem(
                    cursor.getString(NAME_COL),
                    cursor.getString(SKU_COL),
                    cursor.getDouble(PRICE_COL),
                    cursor.getString(IMG_COL),
                    cursor.getInt(STOCK_COL)
            );

            item.setDbId(cursor.getLong(ID_COL));

            retList.add(item);
        }

        cursor.close();
        return retList;
    }

    public String getUserRole(String username) {
        var db = this.getReadableDatabase();
        var c = db.query(
                USERS_TABLE_NAME,
                new String[]{"role"},
                "username = ?",
                new String[]{username},
                null, null, null
        );

        String role = null;
        final int ROLE_COL = 0;

        if (c.getCount() == 1) {
            c.moveToFirst();
            role = c.getString(ROLE_COL);
        }
        c.close();
        return role;
    }
    //endregion

    //region inserting
    public boolean addUser(String user, String pass) {
        var db = this.getReadableDatabase();
        var vals = new ContentValues();
        var role = UserRoles.MANAGER_ROLE; // having all added users be managers, so prof can delete items if he tests with newly registered acct

        vals.put("username", user);
        vals.put("password", pass);
        vals.put("role", role);

        var res = db.insert("users", null, vals);

        // db.insert returns "the row ID of the newly inserted row, or -1 if an error occurred"
        // so if res != -1, the user was added to db.
        return res != -1;
    }

    public boolean addItem(String name, String sku, int count){
        // I don't have time to figure out the image naming and resource additions, so all added items just get default image
        final String IMG_NAME = "product_image.png";
        StockItem toAdd = new StockItem(name, sku, 5, IMG_NAME, count);

        var db = this.getReadableDatabase();
        var res = db.insert(STOCK_TABLE_NAME, null, toAdd.getContentVals());

        return res != -1;
    }
    //endregion

    //region updating
    public boolean updateStockItemCount(StockItem item, int newCount) {
        var db = this.getReadableDatabase();

        var contentVals = item.getContentVals();

        contentVals.put("stockCount", newCount);

        var updated = db.update(
                STOCK_TABLE_NAME,
                contentVals,
                "sku = ?",
                new String[]{item.getSku()}
        );

        return updated > 0;
    }
    //endregion

    //region deleting
    public int deleteItems(StockItem[] items) {
        var db = this.getReadableDatabase();

        int removedCt = 0;

        for (var item : items) {
            var res = db.delete(
                    STOCK_TABLE_NAME,
                    "id = ?",
                    new String[]{String.valueOf(item.getDbId())}
            );
            removedCt += res;
        }

        return removedCt;
    }
    //endregion
}
