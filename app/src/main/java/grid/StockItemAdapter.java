package grid;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cs360_project.R;
import com.example.cs360_project.SQLiteDB;
import com.example.cs360_project.UserRoles;
import com.example.cs360_project.WarehouseApplication;

import java.util.List;

public class StockItemAdapter extends BaseAdapter {
    Context ctx;
    List<StockItem> stockItems;

    public StockItemAdapter(Context ctx, List<StockItem> items) {
        this.ctx = ctx;
        this.stockItems = items;
    }

    //region overrides
    @Override
    public int getCount() {
        return stockItems.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < stockItems.size()) {
            return stockItems.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < stockItems.size()) {
            return stockItems.get(position).getDbId();
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            convertView = inflater.inflate(R.layout.grid_item, parent, false); // I don't get when attaching to root is appropriate
        }

        var anonItem = getItem(position);

        if (!(anonItem instanceof StockItem)) {
            Log.e("StockItemAdapter casting process", "cannot cast item at position " + position);
            return null;
        }

        StockItem item = (StockItem) getItem(position); // method signature won't allow for anything other than Object, so casting. Not sure how to work around.

        if (item == null) return null;

        // set component view values...
        TextView itemTitle = convertView.findViewById(R.id.itemTitle);
        itemTitle.setText(item.getName());

        ImageView imageView = convertView.findViewById(R.id.itemImage);
        // try load image
        imageView.setImageResource(R.drawable.product_image);

        TextView itemSku = convertView.findViewById(R.id.itemSKU);
        String skuString = "SKU: " + item.getSku(); // avoid string concat in setText.
        itemSku.setText(skuString);

        TextView itemStockCount = convertView.findViewById(R.id.itemStockCount);
        String stockString = "In Stock: " + String.valueOf(item.getStockCount());
        itemStockCount.setText(stockString);

        Button updateCountBtn = convertView.findViewById(R.id.btnUpdateCount);
        updateCountBtn.setOnClickListener(v -> showUpdateDialog(item));


        return convertView;
    }
    //endregion

    private void showUpdateDialog(StockItem item) {
        //https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Edit Item Stock");

        // admin + manager roles can delete, plain "user" cannot
        boolean userCanDelete = !UserRoles.USER_ROLE.equals(WarehouseApplication.getInstance().getRole());

        final EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.getStockCount()));

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                int countVal = 0;

                // parse int
                try {
                    countVal = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    String msg = e.getMessage();
                    Log.e("parse int error", msg == null ? "..." : msg);
                }

                item.setStockCount(Integer.parseInt(text));

                // refresh state of component (re-render)
                notifyDataSetChanged();
            }
        });

        // present delete option if role appropriate
        if (userCanDelete) {
            builder.setNeutralButton(
                    "DELETE",
                    (dialog, which) -> {
                        SQLiteDB.getInstance().deleteItems(new StockItem[]{item});

                        var res = stockItems.remove(item);

                        Log.d("item deletion", item.getName() + (res ? "was" : "was not") + " removed from stockItems list");

                        notifyDataSetChanged();
                    }
            );
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void refresh(){
        stockItems = SQLiteDB.getInstance().getStockItems();

        notifyDataSetChanged();
    }
}
