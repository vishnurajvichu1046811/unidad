package com.utracx.util.helper;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.google.android.material.textfield.TextInputLayout;

public class TextInputAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public TextInputAutoCompleteTextView(Context context) {
        super(context);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            performFiltering(getText(), 0);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            final ViewParent parent = getParent();
            if (parent instanceof TextInputLayout) {
                outAttrs.hintText = ((TextInputLayout) parent).getHint();
            }
        }
        return ic;
    }
}