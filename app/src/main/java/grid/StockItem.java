package grid;

import android.content.ContentValues;
import android.util.Log;

import com.example.cs360_project.SQLiteDB;
import com.example.cs360_project.WarehouseApplication;

public class StockItem {
    private String name;
    private String sku;
    private double price; // didn't wind up using this field anywhere
    private String imageName;  // drawable resource, also a failed experiment allowing adding images

    private int stockCount;

    public static final int LOW_STOCK_THRESHOLD = 20;

    private Long dbId;

    public StockItem(
            String name,
            String sku,
            double price,
            String imageResName,
            int stockCount) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.imageName = imageResName;
        this.stockCount = stockCount;
    }

    // setters
    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public void setStockCount(int ct){
        boolean hasChanged = stockCount != ct;

        if(!hasChanged) return;

        stockCount = ct;

        // update db
        var res = SQLiteDB.getInstance().updateStockItemCount(this, stockCount);
        Log.d("update item call", res ? getName() + " count updated" : "something went wrong");

        // low stock?
        if(stockCount < LOW_STOCK_THRESHOLD){
            WarehouseApplication.getInstance().sendLowStockSMS(this);
        }
    }

    // getters
    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public double getPrice() {
        return price;
    }

    public String getImageName() {
        return imageName;
    }

    public Long getDbId() {
        return dbId;
    }

    public int getStockCount(){
        return stockCount;
    }

    public ContentValues getContentVals() {
        ContentValues vals = new ContentValues();
        vals.put("name", this.name);
        vals.put("sku", this.sku);
        vals.put("price", this.price);
        vals.put("imageName", this.imageName);
        vals.put("stockCount", this.stockCount);

        return vals;
    }
}
