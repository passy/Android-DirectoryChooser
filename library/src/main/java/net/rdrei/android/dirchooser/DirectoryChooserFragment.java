package net.rdrei.android.dirchooser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gu.option.Option;
import com.gu.option.UnitFunction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectoryChooserFragment extends DialogFragment {
    public static final String KEY_CURRENT_DIRECTORY = "CURRENT_DIRECTORY";
    private static final String ARG_CONFIG = "CONFIG";
    private static final String TAG = DirectoryChooserFragment.class.getSimpleName();
    private String mNewDirectoryName;
    private String mInitialDirectory;

    private Option<OnFragmentInteractionListener> mListener = Option.none();

    private Button mBtnConfirm;
    private Button mBtnCancel;
    private ImageButton mBtnNavUp;
    private ImageButton mBtnCreateFolder;
    private TextView mTxtvSelectedFolder;
    private ListView mListDirectories;

    private ArrayAdapter<String> mListDirectoriesAdapter;
    private List<String> mFilenames;
    /**
     * The directory that is currently being shown.
     */
    private File mSelectedDir;
    private File[] mFilesInDir;
    private FileObserver mFileObserver;
    private DirectoryChooserConfig mConfig;

    public DirectoryChooserFragment() {
        // Required empty public constructor
    }

    /**
     * To create the config, make use of the provided
     * {@link DirectoryChooserConfig#builder()}.
     *
     * @return A new instance of DirectoryChooserFragment.
     */
    public static DirectoryChooserFragment newInstance(@NonNull final DirectoryChooserConfig config) {
        final DirectoryChooserFragment fragment = new DirectoryChooserFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_CONFIG, config);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedDir != null) {
            outState.putString(KEY_CURRENT_DIRECTORY, mSelectedDir.getAbsolutePath());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException(
                    "You must create DirectoryChooserFragment via newInstance().");
        }
        mConfig = getArguments().getParcelable(ARG_CONFIG);

        if (mConfig == null) {
            throw new NullPointerException("No ARG_CONFIG provided for DirectoryChooserFragment " +
                    "creation.");
        }

        mNewDirectoryName = mConfig.newDirectoryName();
        mInitialDirectory = mConfig.initialDirectory();

        if (savedInstanceState != null) {
            mInitialDirectory = savedInstanceState.getString(KEY_CURRENT_DIRECTORY);
        }

        if (getShowsDialog()) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        } else {
            setHasOptionsMenu(true);
        }

        if (!mConfig.allowNewDirectoryNameModification() && TextUtils.isEmpty(mNewDirectoryName)) {
            throw new IllegalArgumentException("New directory name must have a strictly positive " +
                    "length (not zero) when user is not allowed to modify it.");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {

        assert getActivity() != null;
        final View view = inflater.inflate(R.layout.directory_chooser, container, false);

        mBtnConfirm = (Button) view.findViewById(R.id.btnConfirm);
        mBtnCancel = (Button) view.findViewById(R.id.btnCancel);
        mBtnNavUp = (ImageButton) view.findViewById(R.id.btnNavUp);
        mBtnCreateFolder = (ImageButton) view.findViewById(R.id.btnCreateFolder);
        mTxtvSelectedFolder = (TextView) view.findViewById(R.id.txtvSelectedFolder);
        mListDirectories = (ListView) view.findViewById(R.id.directoryList);

        mBtnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                if (isValidFile(mSelectedDir)) {
                    returnSelectedFolder();
                }
            }
        });

        mBtnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                mListener.foreach(new UnitFunction<OnFragmentInteractionListener>() {
                    @Override
                    public void apply(final OnFragmentInteractionListener listener) {
                        listener.onCancelChooser();
                    }
                });
            }
        });

        mListDirectories.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                debug("Selected index: %d", position);
                if (mFilesInDir != null && position >= 0
                        && position < mFilesInDir.length) {
                    changeDirectory(mFilesInDir[position]);
                }
            }
        });

        mBtnNavUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                final File parent;
                if (mSelectedDir != null
                        && (parent = mSelectedDir.getParentFile()) != null) {
                    changeDirectory(parent);
                }
            }
        });

        mBtnCreateFolder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                openNewFolderDialog();
            }
        });

        if (!getShowsDialog()) {
            mBtnCreateFolder.setVisibility(View.GONE);
        }

        adjustResourceLightness();

        mFilenames = new ArrayList<>();
        mListDirectoriesAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mFilenames);
        mListDirectories.setAdapter(mListDirectoriesAdapter);

        final File initialDir;
        if (!TextUtils.isEmpty(mInitialDirectory) && isValidFile(new File(mInitialDirectory))) {
            initialDir = new File(mInitialDirectory);
        } else {
            initialDir = Environment.getExternalStorageDirectory();
        }

        changeDirectory(initialDir);

        return view;
    }

    private void adjustResourceLightness() {
        // change up button to light version if using dark theme
        int color = 0xFFFFFF;
        final Resources.Theme theme = getActivity().getTheme();

        if (theme != null) {
            final TypedArray backgroundAttributes = theme.obtainStyledAttributes(
                    new int[]{android.R.attr.colorBackground});

            if (backgroundAttributes != null) {
                color = backgroundAttributes.getColor(0, 0xFFFFFF);
                backgroundAttributes.recycle();
            }
        }

        // convert to greyscale and check if < 128
        if (color != 0xFFFFFF && 0.21 * Color.red(color) +
                0.72 * Color.green(color) +
                0.07 * Color.blue(color) < 128) {
            mBtnNavUp.setImageResource(R.drawable.navigation_up_light);
            mBtnCreateFolder.setImageResource(R.drawable.ic_action_create_light);
        }
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnFragmentInteractionListener) {
            mListener = Option.some((OnFragmentInteractionListener) activity);
        } else {
            Fragment owner = getTargetFragment();
            if (owner instanceof OnFragmentInteractionListener) {
                mListener = Option.some((OnFragmentInteractionListener) owner);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFileObserver != null) {
            mFileObserver.startWatching();
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.directory_chooser, menu);

        final MenuItem menuItem = menu.findItem(R.id.new_folder_item);

        if (menuItem == null) {
            return;
        }

        menuItem.setVisible(isValidFile(mSelectedDir) && mNewDirectoryName != null);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == R.id.new_folder_item) {
            openNewFolderDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a confirmation dialog that asks the user if he wants to create a
     * new folder. User can modify provided name, if it was not disallowed.
     */
    private void openNewFolderDialog() {
        @SuppressLint("InflateParams")
        final View dialogView = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_new_folder, null);
        final TextView msgView = (TextView) dialogView.findViewById(R.id.msgText);
        final EditText editText = (EditText) dialogView.findViewById(R.id.editText);
        editText.setText(mNewDirectoryName);
        msgView.setText(getString(R.string.create_folder_msg, mNewDirectoryName));

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_folder_label)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel_label,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(R.string.confirm_label,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                dialog.dismiss();
                                mNewDirectoryName = editText.getText().toString();
                                final int msg = createFolder();
                                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                            }
                        })
                .show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(editText.getText().length() != 0);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i2, final int i3) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i2, final int i3) {
                final boolean textNotEmpty = charSequence.length() != 0;
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(textNotEmpty);
                msgView.setText(getString(R.string.create_folder_msg, charSequence.toString()));
            }

            @Override
            public void afterTextChanged(final Editable editable) {

            }
        });

        editText.setVisibility(mConfig.allowNewDirectoryNameModification()
                ? View.VISIBLE : View.GONE);
    }

    private static void debug(final String message, final Object... args) {
        Log.d(TAG, String.format(message, args));
    }

    /**
     * Change the directory that is currently being displayed.
     *
     * @param dir The file the activity should switch to. This File must be
     *            non-null and a directory, otherwise the displayed directory
     *            will not be changed
     */
    private void changeDirectory(final File dir) {
        if (dir == null) {
            debug("Could not change folder: dir was null");
        } else if (!dir.isDirectory()) {
            debug("Could not change folder: dir is no directory");
        } else {
            final File[] contents = dir.listFiles();
            if (contents != null) {
                int numDirectories = 0;
                for (final File f : contents) {
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
        final Activity activity = getActivity();
        if (activity != null && mSelectedDir != null) {
            mBtnConfirm.setEnabled(isValidFile(mSelectedDir));
            getActivity().invalidateOptionsMenu();
        }
    }

    /**
     * Refresh the contents of the directory that is currently shown.
     */
    private void refreshDirectory() {
        if (mSelectedDir != null) {
            changeDirectory(mSelectedDir);
        }
    }

    /**
     * Sets up a FileObserver to watch the current directory.
     */
    private FileObserver createFileObserver(final String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

            @Override
            public void onEvent(final int event, final String path) {
                debug("FileObserver received event %d", event);
                final Activity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshDirectory();
                        }
                    });
                }
            }
        };
    }

    /**
     * Returns the selected folder as a result to the activity the fragment's attached to. The
     * selected folder can also be null.
     */
    private void returnSelectedFolder() {
        if (mSelectedDir != null) {
            debug("Returning %s as result", mSelectedDir.getAbsolutePath());
            mListener.foreach(new UnitFunction<OnFragmentInteractionListener>() {
                @Override
                public void apply(final OnFragmentInteractionListener f) {
                    f.onSelectDirectory(mSelectedDir.getAbsolutePath());
                }
            });
        } else {
            mListener.foreach(new UnitFunction<OnFragmentInteractionListener>() {
                @Override
                public void apply(final OnFragmentInteractionListener f) {
                    f.onCancelChooser();
                }
            });
        }

    }

    /**
     * Creates a new folder in the current directory with the name
     * CREATE_DIRECTORY_NAME.
     */
    private int createFolder() {
        if (mNewDirectoryName != null && mSelectedDir != null
                && mSelectedDir.canWrite()) {
            final File newDir = new File(mSelectedDir, mNewDirectoryName);
            if (newDir.exists()) {
                return R.string.create_folder_error_already_exists;
            } else {
                final boolean result = newDir.mkdir();
                if (result) {
                    return R.string.create_folder_success;
                } else {
                    return R.string.create_folder_error;
                }
            }
        } else if (mSelectedDir != null && !mSelectedDir.canWrite()) {
            return R.string.create_folder_error_no_write_access;
        } else {
            return R.string.create_folder_error;
        }
    }

    /**
     * Returns true if the selected file or directory would be valid selection.
     */
    private boolean isValidFile(final File file) {
        return (file != null && file.isDirectory() && file.canRead() &&
                (mConfig.allowNewDirectoryNameModification() || file.canWrite()));
    }

    @Nullable
    public OnFragmentInteractionListener getDirectoryChooserListener() {
        return mListener.get();
    }

    public void setDirectoryChooserListener(@Nullable final OnFragmentInteractionListener listener) {
        mListener = Option.option(listener);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * Triggered when the user successfully selected their destination directory.
         */
        void onSelectDirectory(@NonNull String path);

        /**
         * Advices the activity to remove the current fragment.
         */
        void onCancelChooser();
    }

}
