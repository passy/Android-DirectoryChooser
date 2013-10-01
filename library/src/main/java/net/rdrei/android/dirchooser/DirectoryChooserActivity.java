package net.rdrei.android.dirchooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Let's the user choose a directory on the storage device. The selected folder
 * will be sent back to the starting activity as an activity result.
 */
public class DirectoryChooserActivity extends SherlockActivity {
    public static final String EXTRA_NEW_DIR_NAME = "directory_name";
    
	/**
	 * Extra to define the path of the directory that will be shown first. 
	 * If it is not sent or if path denotes a non readable/writable directory
	 * or it is not a directory, it defaults to 
	 * {@link android.os.Environment#getExternalStorageDirectory()}
	 */
	public static final String EXTRA_INITIAL_DIRECTORY = "initial_directory";
	
    public static final String RESULT_SELECTED_DIR = "selected_dir";
    public static final int RESULT_CODE_DIR_SELECTED = 1;

    private static final String TAG = "DirectoryChooser";

    private Button mBtnConfirm;
    private Button mBtnCancel;
    private ImageButton mBtnNavUp;
    private TextView mTxtvSelectedFolder;
    private ListView mListDirectories;

    private ArrayAdapter<String> mListDirectoriesAdapter;
    private ArrayList<String> mFilenames;
    /** The directory that is currently being shown. */
    private File mSelectedDir;
    private File[] mFilesInDir;
    private FileObserver mFileObserver;

    /**
     * Extra injected from the calling activity.
     */
    private String mNewDirectoryName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.directory_chooser);

        mNewDirectoryName = getIntent().getStringExtra(EXTRA_NEW_DIR_NAME);

        mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
        mBtnCancel = (Button) findViewById(R.id.btnCancel);
        mBtnNavUp = (ImageButton) findViewById(R.id.btnNavUp);
        mTxtvSelectedFolder = (TextView) findViewById(R.id.txtvSelectedFolder);
        mListDirectories = (ListView) findViewById(R.id.directoryList);

        mBtnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isValidFile(mSelectedDir)) {
                    returnSelectedFolder();
                }
            }
        });

        mBtnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        mListDirectories.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                    int position, long id) {
                debug("Selected index: %d", position);
                if (mFilesInDir != null && position >= 0
                        && position < mFilesInDir.length) {
                    changeDirectory(mFilesInDir[position]);
                }
            }
        });

        mBtnNavUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                File parent = null;
                if (mSelectedDir != null
                        && (parent = mSelectedDir.getParentFile()) != null) {
                    changeDirectory(parent);
                }
            }
        });

        mFilenames = new ArrayList<String>();
        mListDirectoriesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mFilenames);
        mListDirectories.setAdapter(mListDirectoriesAdapter);
        
        String initialDirectoryPath = getIntent().getStringExtra(EXTRA_INITIAL_DIRECTORY);
        File initialDir = (initialDirectoryPath != null && isValidFile(new File(initialDirectoryPath))) ? new File(initialDirectoryPath) : Environment.getExternalStorageDirectory();
        
        changeDirectory(initialDir);
    }

    /* package */void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void debug(String message, Object... args) {
        Log.d(TAG, String.format(message, args));
    }

    /**
     * Finishes the activity and returns the selected folder as a result. The
     * selected folder can also be null.
     */
    private void returnSelectedFolder() {
        if (mSelectedDir != null) {
            debug("Returning %s as result", mSelectedDir.getAbsolutePath());
        }
        Intent resultData = new Intent();
        if (mSelectedDir != null) {
            resultData.putExtra(RESULT_SELECTED_DIR,
                    mSelectedDir.getAbsolutePath());
        }
        setResult(RESULT_CODE_DIR_SELECTED, resultData);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFileObserver != null) {
            mFileObserver.startWatching();
        }
    }

    /**
     * Change the directory that is currently being displayed.
     *
     * @param dir
     *            The file the activity should switch to. This File must be
     *            non-null and a directory, otherwise the displayed directory
     *            will not be changed
     */
    private void changeDirectory(File dir) {
        if (dir == null) {
            debug("Could not change folder: dir was null");
        } else if (!dir.isDirectory()) {
            debug("Could not change folder: dir is no directory");
        } else {
            File[] contents = dir.listFiles();
            if (contents != null) {
                int numDirectories = 0;
                for (File f : contents) {
                    if (f.isDirectory()) {
                        numDirectories++;
                    }
                }
                mFilesInDir = new File[numDirectories];
                mFilenames.clear();
                for (int i = 0, counter = 0; i < numDirectories; counter++) {
                    if (contents[counter].isDirectory()) {
                        mFilesInDir[i] = contents[counter];
                        mFilenames.add(contents[counter].getName());
                        i++;
                    }
                }
                Arrays.sort(mFilesInDir);
                Collections.sort(mFilenames);
                mSelectedDir = dir;
                mTxtvSelectedFolder.setText(dir.getAbsolutePath());
                mListDirectoriesAdapter.notifyDataSetChanged();
                mFileObserver = createFileObserver(dir.getAbsolutePath());
                mFileObserver.startWatching();
                debug("Changed directory to %s", dir.getAbsolutePath());
            } else {
                debug("Could not change folder: contents of dir were null");
            }
        }
        refreshButtonState();
    }

    /**
     * Changes the state of the buttons depending on the currently selected file
     * or folder.
     */
    private void refreshButtonState() {
        if (mSelectedDir != null) {
            mBtnConfirm.setEnabled(isValidFile(mSelectedDir));
            supportInvalidateOptionsMenu();
        }
    }

    /** Refresh the contents of the directory that is currently shown. */
    private void refreshDirectory() {
        if (mSelectedDir != null) {
            changeDirectory(mSelectedDir);
        }
    }

    /** Sets up a FileObserver to watch the current directory. */
    private FileObserver createFileObserver(String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

            @Override
            public void onEvent(int event, String path) {
                debug("FileObserver received event %d", event);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        refreshDirectory();
                    }
                });
            }
        };
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.new_folder_item).setVisible(
                isValidFile(mSelectedDir) && mNewDirectoryName != null);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.directory_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.new_folder_item) {
            openNewFolderDialog();
            return true;
        }

        return false;
    }

    /**
     * Shows a confirmation dialog that asks the user if he wants to create a
     * new folder.
     */
    private void openNewFolderDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.create_folder_label)
                .setMessage(
                        String.format(getString(R.string.create_folder_msg),
                                mNewDirectoryName))
                .setNegativeButton(R.string.cancel_label,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(R.string.confirm_label,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                                int msg = createFolder();
                                Toast t = Toast.makeText(
                                        DirectoryChooserActivity.this, msg,
                                        Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }).create().show();
    }

    /**
     * Creates a new folder in the current directory with the name
     * CREATE_DIRECTORY_NAME.
     */
    private int createFolder() {
        if (mNewDirectoryName != null && mSelectedDir != null
                && mSelectedDir.canWrite()) {
            File newDir = new File(mSelectedDir, mNewDirectoryName);
            if (!newDir.exists()) {
                boolean result = newDir.mkdir();
                if (result) {
                    return R.string.create_folder_success;
                } else {
                    return R.string.create_folder_error;
                }
            } else {
                return R.string.create_folder_error_already_exists;
            }
        } else if (mSelectedDir.canWrite() == false) {
            return R.string.create_folder_error_no_write_access;
        } else {
            return R.string.create_folder_error;
        }
    }

    /** Returns true if the selected file or directory would be valid selection. */
    private boolean isValidFile(File file) {
        return (file != null && file.isDirectory() && file.canRead() && file
                .canWrite());
    }
}
