package uni.oulu.mentor;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.RadioButton;
import junit.framework.Assert;
import com.jayway.android.robotium.solo.Solo;

public class TestStudentViewActivity extends ActivityInstrumentationTestCase2<StudentViewActivity> {
	private Solo solo;
	public TestStudentViewActivity() {
		super(StudentViewActivity.class);
	}

	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
		super.setUp();
	}

	public void testMainSvEditTextsBlackBox() {
		/* Test marker number */
		RadioButton m1Rb = (RadioButton) solo.getView(R.id.marker_one);
		solo.clickOnView(m1Rb);
		Assert.assertEquals("Marker number test failed", true, m1Rb.isChecked());
		/* Test student name */
		solo.clearEditText(0);
		solo.enterText(0, "x");
		solo.clickOnButton("Start");
		Assert.assertTrue("Student name input test failed", solo.waitForText("If set, student name"));
	}
	
	public void testPrivateSession() {
		
		/* Test private session checkbox */
		boolean found = solo.searchText("Private session");
		Assert.assertEquals("Private session text not found", true, found);
		solo.clickOnCheckBox(0);
		boolean checked = solo.isCheckBoxChecked(0);
		Assert.assertEquals("Private session checkbox not checked", true, checked);
		solo.clickOnButton("Start"); //button5
		
		/* Wait for dialog to open */
		solo.waitForDialogToOpen(2000);
		
		/* Test username */
		solo.clearEditText(0);
		solo.enterText(0, "");
		solo.clickOnButton("Send"); //button5
		Assert.assertTrue("Username input test failed", solo.waitForText("Input to username was too short"));
		
		/* Test password */
		solo.clearEditText(1);
		solo.enterText(1, "");
		solo.clickOnButton("Send"); //button5
		Assert.assertTrue("Password input test failed", solo.waitForText("Input to password was too short"));
		solo.goBack();
	}	
	
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
		super.tearDown();
	}
}
