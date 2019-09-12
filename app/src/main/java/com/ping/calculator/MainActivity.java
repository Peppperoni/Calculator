package com.ping.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;

import com.ping.calculate.Calculate;

public class MainActivity extends AppCompatActivity {
    private GridView mGridView = null;
    private EditText mEditInput = null;
    private ArrayAdapter mAdapter = null;

    private Calculate mCalculate;

    private String mPreStr = "";
    private String mLastStr = "";
    private Object mResult = null;
    private boolean mIsExecuteNow = false;

    private final String newLine = "<br>";

    private final String[] mTextBtns = new String[]{
            "Back", "(", ")", "CE",
            "sin","cos","tan","π",
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "+",
            "0", ".", "=", "-",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCalculate = new Calculate();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mEditInput = (EditText) findViewById(R.id.edit_input);
        mEditInput.setKeyListener(null);

        mGridView = (GridView) findViewById(R.id.grid_buttons);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mTextBtns);
        mGridView.setAdapter(mAdapter);

        //listener
        mGridView.setOnItemClickListener(new OnButtonItemClickListener());
    }

    private void setText() {
        final String[] tags = new String[]{
                "<font color='#858585'>",
                "<font color='#CD2626'>",
                "</font> "
        };
        StringBuilder builder = new StringBuilder();
        builder.append(tags[0]).append(mPreStr).append(tags[2]);
        builder.append(tags[1]).append(mLastStr).append(tags[2]);
        mEditInput.setText(Html.fromHtml(builder.toString()));
        mEditInput.setSelection(mEditInput.getText().length());
    }

    private void executeExpression() {
        try {
            mResult = mCalculate.execute(mLastStr);
        } catch (Exception e) {
            mIsExecuteNow = false;
            return;
        }
        mLastStr += "="+ mResult;
        setText();
        mIsExecuteNow = true;
    }

    private class OnButtonItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String text = (String) parent.getAdapter().getItem(position);
            if (text.equals("=")) {
                executeExpression();
            } else if (text.equals("Back")) {
                if (mLastStr.length() == 0) {
                    if (mPreStr.length() != 0) {
                        mPreStr = mPreStr.substring(0, mPreStr.length() - newLine.length());
                        int index = mPreStr.lastIndexOf(newLine);
                        if (index == -1) {
                            mLastStr = mPreStr;
                            mPreStr = "";
                        } else {
                            mLastStr = mPreStr.substring(index + newLine.length(), mPreStr.length());
                            mPreStr = mPreStr.substring(0, index + newLine.length());
                        }
                        mIsExecuteNow = true;
                    }
                } else {
                    mLastStr = mLastStr.substring(0, mLastStr.length() - 1);
                }
                setText();
            } else if (text.equals("CE")) {
                mPreStr = "";
                mLastStr = "";
                mIsExecuteNow = false;
                mEditInput.setText("");
            } else {
                if (mIsExecuteNow) {
                    mPreStr += mLastStr + newLine;
                    mIsExecuteNow = false;
                    mLastStr = text;
                } else {
                    mLastStr += text;
                }
                setText();
            }
        }
    }

}