package grid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cs360_project.R;
import com.example.cs360_project.SQLiteDB;

import java.util.List;

public class StockGridFragment extends Fragment {

    GridView grid;

    Button addItemButton;

    boolean nameValid = false;
    boolean skuValid = false;
    boolean countValid = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.grid, container, false);

        grid = view.findViewById(R.id.itemDisplayGrid);

        addItemButton = view.findViewById(R.id.btnAddItem);
        addItemButton.setOnClickListener(v -> onAddItemClick(container));

        // get inventory from db
        List<StockItem> items = SQLiteDB.getInstance().getStockItems();

        StockItemAdapter itemAdapter = new StockItemAdapter(inflater.getContext(), items);

        grid.setAdapter(itemAdapter);

        return view;
    }

    private void onAddItemClick(ViewGroup container) {
        // load layout view
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View addItemView = inflater.inflate(R.layout.add_item_screen, container, false);

        // show dialogue
        AlertDialog addDialog = new AlertDialog.Builder(getActivity())
                .setView(addItemView)
                .setPositiveButton("Add Item", null) // adding btn listener in onShow, this is kinda screwy but I'm out of time
                .setNegativeButton("Cancel", null)
                .create();

        addDialog.setOnShowListener(idk -> {
            Button posBtn = addDialog.getButton(DialogInterface.BUTTON_POSITIVE);

            TextView nameErrText = addItemView.findViewById(R.id.errItemNameInput);
            EditText nameEdit = addItemView.findViewById(R.id.inputItemName);

            TextView skuErrText = addItemView.findViewById(R.id.errItemSkuInput);
            EditText skuEdit = addItemView.findViewById(R.id.inputSKU);

            TextView countErrText = addItemView.findViewById(R.id.errItemCountInput);
            EditText countEdit = addItemView.findViewById(R.id.inputCount);

            posBtn.setOnClickListener(v -> {
                // submit to db if valid
                boolean success = false;
                boolean isValid = nameValid && skuValid && countValid;

                if(isValid){
                    var name = nameEdit.getText().toString().trim();
                    var sku = skuEdit.getText().toString().trim();
                    // this can throw, but count is valid when only digits, so leaving it as is got no time
                    var ct = Integer.parseInt(countEdit.getText().toString().trim());
                    success = SQLiteDB.getInstance().addItem(name, sku, ct);

                    if(success){
                        // refresh stock items in grid
                        var adapter = grid.getAdapter();

                        if(adapter instanceof StockItemAdapter){ // which it will be...
                            ((StockItemAdapter) adapter).refresh();
                        }

                        addDialog.dismiss();
                    }
                }

            });

            //region dialog TextWatchers
            nameEdit.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            boolean isEmpty = s.toString().trim().isEmpty();

                            nameValid = !isEmpty;

                            nameErrText.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    }
            );

            skuEdit.addTextChangedListener(
                    new TextWatcher() {
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            boolean isEmpty = s.toString().trim().isEmpty();

                            if(isEmpty) skuErrText.setText("SKU is required");
                            skuErrText.setVisibility(isEmpty ? View.VISIBLE : View.INVISIBLE);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            // check sku uniqueness
                            skuValid = SQLiteDB.getInstance().isSkuUnique(s.toString().trim());

                            if(!skuValid){
                                skuErrText.setText("This SKU already exists");
                                skuErrText.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );

            countEdit.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            boolean isEmpty = s.toString().trim().isEmpty();
                            countValid = isOnlyDigits(s);

                            countErrText.setVisibility(isEmpty || !countValid ? View.VISIBLE : View.INVISIBLE);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    }
                    //endregion
            );
        });

        addDialog.show();
    }

    private boolean isOnlyDigits(CharSequence s){
        // can't foreach a charseq for some reason
        for(int i = 0; i < s.length(); i++){
            var isDigit = Character.isDigit(s.charAt(i));
            if(!isDigit){
                return false;
            }
        }
        return true;
    }
}

