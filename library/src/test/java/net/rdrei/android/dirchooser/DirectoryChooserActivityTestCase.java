package net.rdrei.android.dirchooser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.Intent;

import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DirectoryChooserActivityTestCase {
	private DirectoryChooserActivity activity;
	private Intent launchIntent;

	@Before
	public void setup() {
		activity = Mockito.mock(DirectoryChooserActivity.class);
		// Robolectric doesn't support the ICS ActionBar.
		Mockito.doNothing().when(activity).setupActionBar();

		launchIntent = new Intent();
		Mockito.doReturn(launchIntent).when(activity).getIntent();
	}

	@Test
	public void testSmokeInit() {
		activity.onCreate(null);
	}

	@Test
	public void testSmokeInitWithExtras() {
		launchIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME,
				"my dir");
		activity.onCreate(null);
	}
}
