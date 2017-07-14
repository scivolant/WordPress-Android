package org.wordpress.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.wordpress.android.R;
import org.wordpress.android.util.ViewUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Compound view composed of an icon and an EditText
 */
public class WPLoginInputRow extends RelativeLayout {

    public interface OnEditorCommitListener {
        void OnEditorCommit();
    }

    private ImageView mIcon;
    private TextInputLayout mTextInputLayout;
    private EditText mEditText;

    private List<Integer> mNewIds;

    public ImageView getIcon() {
        return mIcon;
    }

    public TextInputLayout getTextInputLayout() {
        return mTextInputLayout;
    }

    public EditText getEditText() {
        return mEditText;
    }

    public WPLoginInputRow(Context context) {
        super(context);
        init(context, null);
    }

    public WPLoginInputRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WPLoginInputRow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.login_input_row, this);

        mIcon = (ImageView) findViewById(R.id.icon);
        mTextInputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        mEditText = (EditText) findViewById(R.id.input);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.wpLoginInputRow, 0, 0);

            try {
                mIcon.setContentDescription(a.getString(R.styleable.wpLoginInputRow_wpIconContentDescription));
                mIcon.setImageResource(a.getResourceId(R.styleable.wpLoginInputRow_wpIconDrawable, 0));

                mTextInputLayout.setHint(a.getString(R.styleable.wpLoginInputRow_android_hint));
                mTextInputLayout.setPasswordVisibilityToggleEnabled(
                        a.getBoolean(R.styleable.wpLoginInputRow_passwordToggleEnabled, false));
                mTextInputLayout.setPasswordVisibilityToggleTintList(
                        a.getColorStateList(R.styleable.wpLoginInputRow_passwordToggleTint));

                mEditText.setInputType(a.getInteger(R.styleable.wpLoginInputRow_android_inputType, 0));
            } finally {
                a.recycle();
            }
        }

        mNewIds = Arrays.asList(ViewUtils.generateViewId(), ViewUtils.generateViewId(), ViewUtils.generateViewId());

        reassignIds();
    }

    /**
     * Assign new IDs to the Views so multiple instances of the compound View can exist in the same layout and
     *  auto-save of the Views state can be performed
     */
    private void reassignIds() {
        RelativeLayout.LayoutParams iconLayoutParams = (LayoutParams) mIcon.getLayoutParams();
        int[] rules = iconLayoutParams.getRules();
        for (int i = 0; i < rules.length; i++) {
            if (rules[i] == mTextInputLayout.getId()) {
                rules[i] = mNewIds.get(1);
            }
        }
        mIcon.setLayoutParams(iconLayoutParams);

        RelativeLayout.LayoutParams editTextLayoutParams = (LayoutParams) mTextInputLayout.getLayoutParams();
        rules = editTextLayoutParams.getRules();
        for (int i = 0; i < rules.length; i++) {
            if (rules[i] == mIcon.getId()) {
                rules[i] = mNewIds.get(0);
            }
        }
        mTextInputLayout.setLayoutParams(editTextLayoutParams);

        mIcon.setId(mNewIds.get(0));
        mTextInputLayout.setId(mNewIds.get(1));
        mEditText.setId(mNewIds.get(2));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState, mNewIds);

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mNewIds = savedState.mIds;

        reassignIds();
    }

    public void addTextChangedListener(TextWatcher watcher) {
        mEditText.addTextChangedListener(watcher);
    }

    public void setOnEditorCommitListener(final OnEditorCommitListener listener) {
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null
                        && event.getAction() == KeyEvent.ACTION_UP
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    listener.OnEditorCommit();
                }

                // always consume the event so the focus stays in the EditText
                return true;
            }
        });
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener l) {
        mEditText.setOnEditorActionListener(l);
    }

    public final void setText(CharSequence text) {
        mEditText.setText(text);
    }

    public void setError(@Nullable final CharSequence error) {
        mTextInputLayout.setError(error);
    }

    public static class SavedState extends BaseSavedState {
        private List<Integer> mIds;

        SavedState(Parcelable superState, List<Integer> ids) {
            super(superState);
            mIds = ids;
        }

        private SavedState(Parcel in) {
            super(in);

            in.readList(mIds, List.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(mIds);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
