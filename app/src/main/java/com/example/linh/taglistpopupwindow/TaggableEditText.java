package com.example.linh.taglistpopupwindow;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

import static android.widget.MultiAutoCompleteTextView.Tokenizer;

/**
 * Created by linh on 20/06/2017.
 */

public class TaggableEditText extends AppCompatEditText implements TextWatcher, RealmTasksAdapter.ItemClickListener {
    private static final String TAG = "TaggableEditText";

    private int mThreshold = 1;
    private boolean mOpenBefore;
    private int mLastKeyCode = KeyEvent.KEYCODE_UNKNOWN;

    UserTagListPopUp mPopupWindow;
    private Tokenizer mTokenizer;
    Realm realm;
    RealmResults<FollowItemModel> items;

    public TaggableEditText(Context context) {
        super(context);
        constructor(context, null, 0);
    }

    public TaggableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructor(context, attrs, 0);
    }

    public TaggableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructor(context, attrs, defStyleAttr);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            dismissDropDown();
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        // Perform validation if the view is losing focus.
        if (!focused) {
            performValidation();
            dismissDropDown();
        }
        Timber.d("onFocusChanged %s", String.valueOf(focused));
    }

    @Override
    protected void onDetachedFromWindow() {
        dismissDropDown();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            dismissDropDown();
            return true;
        }
        mLastKeyCode = keyCode;
        boolean handled = super.onKeyDown(keyCode, event);
        mLastKeyCode = KeyEvent.KEYCODE_UNKNOWN;
        return handled;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        boolean consumed = mPopup.onKeyUp(keyCode, event);
//        if (consumed) {
//            switch (keyCode) {
//                // if the list accepts the key events and the key event
//                // was a click, the text view gets the selected item
//                // from the drop down as its content
//                case KeyEvent.KEYCODE_ENTER:
//                case KeyEvent.KEYCODE_DPAD_CENTER:
//                case KeyEvent.KEYCODE_TAB:
//                    if (event.hasNoModifiers()) {
//                        performCompletion();
//                    }
//                    return true;
//            }
//        }

        if (isPopupShowing() && keyCode == KeyEvent.KEYCODE_TAB && event.hasNoModifiers()) {
            performCompletion();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        doBeforeTextChanged();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        doAfterTextChanged();
    }

    @Override
    public void onClick(View caller, FollowItemModel task) {

    }

    private void constructor(Context context, AttributeSet attrs, int defStyleAttr){
        mTokenizer = new AtTokenizer();
        realm = Realm.getDefaultInstance();
        createDumpData();
        setupPopUpWindow();
        setCallback();
    }

    private void createDumpData(){
        items = realm.where(FollowItemModel.class).findAll();
        if (items.isEmpty()){
            generateItem();
            Timber.d("realm is empty");
        }
    }

    private void generateItem(){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i =0; i < 10; i++) {
                    FollowItemModel followItemModel = realm.createObject(FollowItemModel.class);
                    followItemModel.setDisplayName("name " + i);
                    followItemModel.setUserId(String.valueOf(i));
                    followItemModel.setUserName("username" + i);
                    realm.copyToRealm(followItemModel);
                }
            }
        });
    }

    private void setupPopUpWindow(){
        mPopupWindow = UserTagListPopUp.newInstance(getContext(), items, new OnClickListener() {
            @Override
            public void onClick(View v) {
                performCompletion();
            }
        });
    }

    private void setCallback(){
        addTextChangedListener(this);
    }

    private void showDropDown(){
        Layout layout = getLayout();
        int pos = getSelectionStart();
        int line = layout.getLineForOffset(pos);
        int maxLine = getMaxLines();
        if (line > maxLine){
            line = maxLine;
        }
        int baseline = layout.getLineBaseline(line);
        int lineTop = layout.getLineTop(line);
        int lineBottom = layout.getLineBottom(line);
        int bottom = getHeight();

        Timber.d("pos %d", pos);
        Timber.d("line %d", line);
        Timber.d("baseline %d", baseline);
        Timber.d("line top %d", lineTop);
        Timber.d("line bottom %d", lineBottom);
        Timber.d("bottom %d", bottom);

        if (isPopupShowing()) {
            if (line == maxLine) {
                mPopupWindow.dismiss();
                mPopupWindow.showAtLocation(this, Gravity.START | Gravity.TOP, 0, 0);
            }else{
                mPopupWindow.update(this, 0, lineBottom - bottom, this.getWidth(), 250);
            }

        }else{
            if (line == maxLine){
                mPopupWindow.showAtLocation(this, Gravity.START | Gravity.TOP, 0, 0);
            }else {
                mPopupWindow.showAsDropDown(this, 0, baseline - bottom, Gravity.NO_GRAVITY);
            }
        }
    }

    /**
     * <p>Returns the number of characters the user must type before the drop
     * down list is shown.</p>
     *
     * @return the minimum number of characters to type to show the drop down
     *
     * @see #setThreshold(int)
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_completionThreshold
     */
    public int getThreshold() {
        return mThreshold;
    }

    /**
     * <p>Specifies the minimum number of characters the user has to type in the
     * edit box before the drop down list is shown.</p>
     *
     * <p>When <code>threshold</code> is less than or equals 0, a threshold of
     * 1 is applied.</p>
     *
     * @param threshold the number of characters to type before the drop down
     *                  is shown
     *
     * @see #getThreshold()
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_completionThreshold
     */
    public void setThreshold(int threshold) {
        if (threshold <= 0) {
            threshold = 1;
        }

        mThreshold = threshold;
    }

    void doBeforeTextChanged() {
//        if (mBlockCompletion) return;

        // when text is changed, inserted or deleted, we attempt to show
        // the drop down
        mOpenBefore = isPopupShowing();
        Timber.v("before text changed: open=" + mOpenBefore);
    }

    void doAfterTextChanged() {
//        if (mBlockCompletion) return;

        // if the list was open before the keystroke, but closed afterwards,
        // then something in the keystroke processing (an input filter perhaps)
        // called performCompletion() and we shouldn't do any more processing.
        Timber.v("after text changed: openBefore=" + mOpenBefore + " open=" + isPopupShowing());
        if (mOpenBefore && !isPopupShowing()) {
            return;
        }

        // the drop down is shown only when a minimum number of characters
        // was typed in the text view
        if (enoughToFilter()) {
            performFiltering(getText(), mLastKeyCode);
        } else {
            // drop down is automatically dismissed when enough characters
            // are deleted from the text view
            if (isPopupShowing()){
                mPopupWindow.dismiss();
            }
        }
    }

    /**
     * Instead of validating the entire text, this subclass method validates
     * each token of the text individually.  Empty tokens are removed.
     */
    public void performValidation() {
        Editable e = getText();
        int i = getText().length();
        while (i > 0) {
            int start = mTokenizer.findTokenStart(e, i);
            int end = mTokenizer.findTokenEnd(e, start);

            CharSequence sub = e.subSequence(start, end);
            if (TextUtils.isEmpty(sub)) {
                e.replace(start, i, "");
            } else {
                e.replace(start, i, mTokenizer.terminateToken(sub));
            }

            i = start;
        }
    }

    private void performCompletion() {
        replaceText("linh");
        dismissDropDown();
    }

    /**
     * <p>Performs the text completion by replacing the range from
     * {@link Tokenizer#findTokenStart} to {@link #getSelectionEnd} by the
     * the result of passing <code>text</code> through
     * {@link Tokenizer#terminateToken}.
     * In addition, the replaced region will be marked as an AutoText
     * substition so that if the user immediately presses DEL, the
     * completion will be undone.
     * Subclasses may override this method to do some different
     * insertion of the content into the edit box.</p>
     *
     * @param text the selected suggestion in the drop down list
     */
    protected void replaceText(CharSequence text) {
        clearComposingText();

        int end = getSelectionEnd();
        int start = mTokenizer.findTokenStart(getText(), end);

        Editable editable = getText();
        String original = TextUtils.substring(editable, start, end);

        QwertyKeyListener.markAsReplaced(editable, start, end, original);
        editable.replace(start, end, mTokenizer.terminateToken(text));
    }

    /**
     * Instead of filtering on the entire contents of the edit box,
     * this subclass method filters on the range from
     * {@link Tokenizer#findTokenStart} to {@link #getSelectionEnd}
     * if the length of that range meets or exceeds {@link #getThreshold}.
     */
    protected void performFiltering(CharSequence text, int keyCode) {
        if (enoughToFilter()) {
            int end = getSelectionEnd();
            int start = mTokenizer.findTokenStart(text, end);

            performFiltering(text, start, end, keyCode);
        } else {
            if (isPopupShowing()) {
                mPopupWindow.dismiss();
            }
//            Filter f = getFilter();
//            if (f != null) {
//                f.filter(null);
//            }
        }
    }

    /**
     * <p>Starts filtering the content of the drop down list. The filtering
     * pattern is the specified range of text from the edit box. Subclasses may
     * override this method to filter with a different pattern, for
     * instance a smaller substring of <code>text</code>.</p>
     */
    protected void performFiltering(CharSequence text, int start, int end, int keyCode) {
        showDropDown();
    }

    /**
     * Instead of filtering whenever the total length of the text
     * exceeds the threshhold, this subclass filters only when the
     * length of the range from
     * {@link Tokenizer#findTokenStart} to {@link #getSelectionEnd}
     * meets or exceeds {@link #getThreshold}.
     */
    public boolean enoughToFilter() {
        Editable text = getText();

        int end = getSelectionEnd();
        if (end < 0 || mTokenizer == null) {
            return false;
        }

        int start = mTokenizer.findTokenStart(text, end);

        if (end - start >= getThreshold()) {
            return true;
        } else {
            return false;
        }
    }

    private void updateDropDownForFilter(int count) {
        // Not attached to window, don't update drop-down
        if (getWindowVisibility() == View.GONE) return;

        /*
         * This checks enoughToFilter() again because filtering requests
         * are asynchronous, so the result may come back after enough text
         * has since been deleted to make it no longer appropriate
         * to filter.
         */

        final boolean enoughToFilter = enoughToFilter();
        if ((count > 0) && enoughToFilter) {
            if (hasFocus() && hasWindowFocus()) {
                showDropDown();
            }
        } else if (isPopupShowing()) {
            dismissDropDown();
            // When the filter text is changed, the first update from the adapter may show an empty
            // count (when the query is being performed on the network). Future updates when some
            // content has been retrieved should still be able to update the list.
//            mPopupCanBeUpdated = true;
        }
    }

    /**
     * <p>Indicates whether the popup menu is showing.</p>
     *
     * @return true if the popup menu is showing, false otherwise
     */
    public boolean isPopupShowing() {
        Timber.d("is popup showing %s", String.valueOf(mPopupWindow.isShowing()));
        return mPopupWindow.isShowing();
    }

    /**
     * <p>Closes the drop down if present on screen.</p>
     */
    public void dismissDropDown() {
        mPopupWindow.dismiss();
    }

    private class AtTokenizer implements Tokenizer {
        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != '@') {
                i--;
            }

            //Check if token really started with @, else we don't have a valid token
            if (i < 1 || text.charAt(i - 1) != '@') {
                return cursor;
            }

            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        @Override
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
//                if (text instanceof Spanned) {
//                    SpannableString sp = new SpannableString(text + " ");
//                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
//                    return sp;
//                } else {
//                    return text + " ";
//                }
                SpannableString sp = new SpannableString(text + " ");
                sp.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return sp;
            }
        }
    }
}
