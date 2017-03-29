package net.rdrei.android.dirchooser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DirectoryChooserFragmentTest {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void startFragment(@NonNull final Fragment fragment, @Nullable Class activityClass) {
        final Activity activity = (Activity) Robolectric
                .buildActivity(activityClass == null ? Activity.class : activityClass)
                .create()
                .start()
                .resume()
                .get();

        final FragmentManager fragmentManager = activity.getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main, fragment);
        fragmentTransaction.commit();

        fragmentManager.executePendingTransactions();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testWithDirectory() {
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(
                DirectoryChooserConfig.builder().newDirectoryName("mydir").build());

        startFragment(fragment, DirectoryChooserActivityMock.class);

        final View chooseBtn = fragment.getActivity().findViewById(R.id.btnConfirm);
        assertThat(chooseBtn).isEnabled();

        assertThat(chooseBtn.performClick()).isTrue();
        assertThat(((DirectoryChooserActivityMock) fragment.getActivity()).selectedDirectory)
                .isNotNull();
    }

    @Test
    public void testCreateDirectoryDialogAllowFolderNameModification() {
        final String directoryName = "mydir";
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(
                DirectoryChooserConfig.builder()
                        .newDirectoryName(directoryName)
                        .initialDirectory("")
                        .allowReadOnlyDirectory(false)
                        .allowNewDirectoryNameModification(true)
                        .build());

        startFragment(fragment, DirectoryChooserActivityMock.class);

        fragment.onOptionsItemSelected(new TestMenuItem() {
            @Override
            public int getItemId() {
                return R.id.new_folder_item;
            }
        });

        final AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        final ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(dialog);
        assertThat(shadowAlertDialog.getTitle()).isEqualTo("Create folder");
        assertThat(ShadowDialog.getShownDialogs()).contains(dialog);

        final TextView msgView = (TextView) dialog.findViewById(R.id.msgText);
        assertThat(msgView).hasText("Create new folder with name \"mydir\"?");

        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        assertThat(editText).isVisible();
        assertThat(editText).hasTextString(directoryName);
    }

    @Test
    public void testCreateDirectoryDialogDisallowFolderNameModification() {
        final String directoryName = "mydir";
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(
                DirectoryChooserConfig.builder()
                        .newDirectoryName(directoryName)
                        .initialDirectory("")
                        .allowReadOnlyDirectory(false)
                        .allowNewDirectoryNameModification(false)
                        .build());

        startFragment(fragment, DirectoryChooserActivityMock.class);

        fragment.onOptionsItemSelected(new TestMenuItem() {
            @Override
            public int getItemId() {
                return R.id.new_folder_item;
            }
        });

        final AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        final ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(dialog);
        assertThat(shadowAlertDialog.getTitle()).isEqualTo("Create folder");
        assertThat(ShadowDialog.getShownDialogs()).contains(dialog);

        final TextView msgView = (TextView) dialog.findViewById(R.id.msgText);
        assertThat(msgView).hasText("Create new folder with name \"mydir\"?");

        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        assertThat(editText).isGone();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testWithCustomListener() {
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(
                DirectoryChooserConfig.builder().newDirectoryName("mydir").build());

        startFragment(fragment, CustomDirectoryChooserActivity.class);
        final CustomDirectoryChooserListener listener = new CustomDirectoryChooserListener();
        fragment.setDirectoryChooserListener(listener);

        final View chooseBtn = fragment.getActivity().findViewById(R.id.btnConfirm);
        assertThat(chooseBtn).isEnabled();

        assertThat(chooseBtn.performClick()).isTrue();
        assertThat(listener.selectedDirectory).isNotNull();
    }

    private static class DirectoryChooserActivityMock extends Activity implements
            DirectoryChooserFragment.OnFragmentInteractionListener {
        public String selectedDirectory;

        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.directory_chooser_activity);
        }

        @Override
        public void onSelectDirectory(@NonNull final String path) {
            selectedDirectory = path;
        }

        @Override
        public void onCancelChooser() {

        }
    }

    private static class CustomDirectoryChooserActivity extends Activity {
        @Override
        protected void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.directory_chooser_activity);
        }
    }

    private static class CustomDirectoryChooserListener implements
            DirectoryChooserFragment.OnFragmentInteractionListener {
        public String selectedDirectory;

        @Override
        public void onSelectDirectory(@NonNull final String path) {
            selectedDirectory = path;
        }

        @Override
        public void onCancelChooser() {
            selectedDirectory = null;
        }
    }
}
