package net.rdrei.android.dirchooser;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import javax.annotation.Nonnull;

/**
 * Let's the user choose a directory on the storage device. The selected folder
 * will be sent back to the starting activity as an activity result.
 */
public class DirectoryChooserActivity extends ActionBarActivity implements
        DirectoryChooserFragment.OnFragmentInteractionListener {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.directory_chooser_activity);

        final String newDirName = getIntent().getStringExtra(EXTRA_NEW_DIR_NAME);
        final String initialDir = getIntent().getStringExtra(EXTRA_INITIAL_DIRECTORY);

        if (newDirName == null) {
            throw new IllegalArgumentException(
                    "You must provide EXTRA_NEW_DIR_NAME when starting the DirectoryChooserActivity.");
        }

        if (savedInstanceState == null) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(newDirName, initialDir);
            fragmentManager.beginTransaction()
                    .add(R.id.main, fragment)
                    .commit();
        }
    }

    /* package */void setupActionBar() {
        // there might not be an ActionBar, for example when started in Theme.Holo.Dialog.NoActionBar theme
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectDirectory(@Nonnull String path) {
        final Intent intent = new Intent();
        intent.putExtra(RESULT_SELECTED_DIR, path);
        setResult(RESULT_CODE_DIR_SELECTED, intent);
        finish();
    }

    @Override
    public void onCancelChooser() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
