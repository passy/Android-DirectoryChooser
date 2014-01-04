package net.rdrei.android.dirchooser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class DirectoryChooserFragmentTest {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void startFragment(@Nonnull Fragment fragment, @Nullable Class activityClass) {
        if (activityClass == null) {
            activityClass = Activity.class;
        }

        Activity activity = Robolectric.buildActivity(activityClass)
                .create()
                .start()
                .resume()
                .get();

        FragmentManager fragmentManager = activity.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main, fragment);
        fragmentTransaction.commit();

        fragmentManager.executePendingTransactions();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testWithDirectory() {
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance("mydir",
                null);

        startFragment(fragment, DirectoryChooserActivityMock.class);

        final View chooseBtn = fragment.getActivity().findViewById(R.id.btnConfirm);
        assertThat(chooseBtn).isEnabled();

        assert chooseBtn.performClick();

        assertNotNull(((DirectoryChooserActivityMock) fragment.getActivity()).selectedDirectory);
    }

    @Test
    public void testCreateDirectoryDialog() {
        final String directoryName = "mydir";
        final DirectoryChooserFragment fragment = DirectoryChooserFragment.newInstance(
                directoryName, null);

        startFragment(fragment, DirectoryChooserActivityMock.class);

        fragment.onOptionsItemSelected(new TestMenuItem() {
            @Override
            public int getItemId() {
                return R.id.new_folder_item;
            }
        });

        final AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        final ShadowAlertDialog shadowAlertDialog = Robolectric.shadowOf(dialog);
        assertEquals(shadowAlertDialog.getTitle(), "Create folder");
        assertEquals(shadowAlertDialog.getMessage(), "Create new folder with name \"mydir\"?");
    }

    static final private class DirectoryChooserActivityMock extends Activity implements
            DirectoryChooserFragment.OnFragmentInteractionListener {
        public String selectedDirectory;

        @Override
        public void onCreate(@Nullable Bundle bundle) {
            setContentView(R.layout.directory_chooser_activity);
        }

        @Override
        public void onSelectDirectory(@Nonnull String path) {
            this.selectedDirectory = path;
        }

        @Override
        public void onCancelChooser() {

        }
    }
}
