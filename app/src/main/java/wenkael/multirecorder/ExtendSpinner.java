package wenkael.multirecorder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Created by wenkael™ on 23.01.2016.
 */
public class ExtendSpinner extends Spinner {

    public ExtendSpinner(Context context) {
        super(context);
    }

    public ExtendSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void
    setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection ( int position){

        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
                // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
                getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }


}
