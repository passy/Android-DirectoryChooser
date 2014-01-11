package net.rdrei.android.dirchooser.sample;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

public class DirChooserSample extends ActionBarActivity {
    private static final int REQUEST_DIRECTORY = 0;
    private static final String TAG = "DirChooserSample";

    private TextView mDirectoryTextView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDirectoryTextView = (TextView) findViewById(R.id.textDirectory);

        // Set up click handler for "Choose Directory" button
        findViewById(R.id.btnChoose)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final Intent chooserIntent = new Intent(
                                DirChooserSample.this,
                                DirectoryChooserActivity.class);

                        chooserIntent.putExtra(
                                DirectoryChooserActivity.EXTRA_NEW_DIR_NAME,
                                "DirChooserSample");

                        startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY) {
            Log.i(TAG, String.format("Return from DirChooser with result %d",
                    resultCode));

            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                mDirectoryTextView
                        .setText(data
                                .getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
            } else {
                mDirectoryTextView.setText("nothing selected");
            }
        }
    }
}
