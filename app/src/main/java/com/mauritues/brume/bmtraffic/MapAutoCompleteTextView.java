package com.mauritues.brume.bmtraffic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.util.HashMap;


@SuppressLint("AppCompatCustomView")
public class MapAutoCompleteTextView extends AutoCompleteTextView {

    public MapAutoCompleteTextView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get("description");
    }


}
