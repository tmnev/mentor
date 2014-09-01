package uni.oulu.mentor;

import android.test.ActivityInstrumentationTestCase2;
import junit.framework.Assert;
//import junit.framework.TestCase;
import com.jayway.android.robotium.solo.Solo;

public class TestTeacherViewActivity extends ActivityInstrumentationTestCase2<TeacherViewActivity> {
	private Solo solo;
	public TestTeacherViewActivity() {
		super(TeacherViewActivity.class);
	}
	@Override
	protected void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
		super.setUp();
	}
	public void testMainEditTextsBlackBox() {
		/* Test course name */
		solo.clearEditText(0);
		solo.enterText(0, "");
		solo.clickOnButton("Start"); //button5
		Assert.assertTrue("Course name input test failed", solo.waitForText("Input to course name was invalid"));
		
		/* Test teacher name */
		solo.clearEditText(1);
		solo.enterText(1, "x");
		solo.clickOnButton("Start"); //button5
		Assert.assertTrue("Teacher name input test failed", solo.waitForText("If set, teacher name"));
		
		/* Test course subject */
		solo.clearEditText(2);
		solo.enterText(2, "x");
		solo.clickOnButton("Start"); //button5
		Assert.assertTrue("Course subject input test failed", solo.waitForText("If set, course subject"));

		/* Test lecture number */
		solo.clearEditText(3);
		solo.enterText(3, "9999");
		solo.clickOnButton("Start"); //button5
		Assert.assertTrue("Lecture number input test failed", solo.waitForText("If set, lecture number"));
		//solo.clearEditText(3);
		//solo.enterText(3, "0");
		//Assert.assertTrue("Lecture number input test failed", solo.waitForText("If set, lecture number"));
		
		/* Test lecture topic */
		solo.clearEditText(4);
		solo.enterText(4, "");
		solo.clickOnButton("Start"); //button5
		Assert.assertTrue("Lecture topic input test failed", solo.waitForText("Input to lecture topic was invalid"));
	}
	
	public void testPrivateSession() {
		/* Set some values to mandatory fields */
		solo.clearEditText(0); //course name
		solo.enterText(0, "tester");
		solo.clearEditText(4); //lecture topic
		solo.enterText(4, "tester");
		
		/* Test private session checkbox */
		boolean found = solo.searchText("Private session");
		Assert.assertEquals("Private session text not found", true, found);
		solo.clickOnCheckBox(2);
		boolean checked = solo.isCheckBoxChecked(2);
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
	
	@Override
	public void tearDown() throws Exception{
		solo.finishOpenedActivities();
		super.tearDown();
	}
}
