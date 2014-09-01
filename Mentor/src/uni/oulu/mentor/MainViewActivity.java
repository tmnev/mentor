package uni.oulu.mentor;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
//import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainViewActivity extends ActionBarActivity {
	protected String webClientUri; //if Mentor web-based client is used
	public static boolean problemOccurred = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_view);
		String ipStr = getResources().getString(R.string.IP);
		webClientUri = "http://"+ipStr+"/mentor/client/";
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_view,
					container, false);
			return rootView;
		}
	}

	/**
	 * When "Exit" button clicked, finish activity
	 */
	public void loadExit(View view) {
		//if Mentor web-based client is used uncomment
		/*Uri uri = Uri.parse(webClientUri);
		Intent webClientIntent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(webClientIntent);*/
		this.finish();
		//if IOException occurred in StudentView or TeacherView, force exit
		//if(problemOccurred)
		System.exit(0);
	}
	
	/**
	 * When "Teacher" button clicked, load TeacherViewActivity
	 */
	public void loadTeacherView(View view) {
		Intent intent = new Intent(this, TeacherViewActivity.class);
		startActivity(intent);
	}
	
	/**
	 * When "Student" button clicked, load StudentViewActivity
	 */
	public void loadStudentView(View view) {
		Intent intent = new Intent(this, StudentViewActivity.class);
		startActivity(intent);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.finish();
		//if IOException occurred in StudentView or TeacherView, force exit
		//if(problemOccurred)
		//System.exit(0);
	}
}
