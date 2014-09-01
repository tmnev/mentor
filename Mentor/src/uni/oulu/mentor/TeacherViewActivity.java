package uni.oulu.mentor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import org.json.JSONObject;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

public class TeacherViewActivity extends ActionBarActivity  {
	private static final String TAG = "TeacherView";
	protected static final int CONNTIME = 30000;
	private static final String allowed = new String("0123456789abcdefghijklmnopqrstuvwxyzåäöABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ");
	private static final String numAllowed = new String("0123456789");
	private static final String userNameTooShort = new String("Input to username was too short. Input min 5 characters");
	private static final String passWordTooShort = new String("Input to password was too short. Input min 5 characters");
	private static final String userNameTooLong = new String("Input to username was too long. Input max 15 characters");
	private static final String connectingServer = new String("Please wait a moment, connecting to the server");
	private static final String wifiOff = new String("Network connection failed. Try to put your WIFI on");
	private static final String loggingIn = new String("Please wait a moment, logging in");
	private static final String passWordTooLong = new String("Input to password was too long. Input max 15 characters");
	private static final String courseSubjectInvalid = new String("If set, course subject must have 2 to 30 characters");
	private static final String teacherNameInvalid = new String("If set, teacher name must have 2 to 30 characters");	
	private static final String courseNameInvalid = new String("Input to course name was invalid. Input 2 to 30 characters");
	private static final String lectureNumberInvalid = new String("If set, lecture number must be a value between 1 and 1000");
	private static final String serverDown = new String("Server down, try again after few minutes or contact the administrator");
	private static final String lectureTopicInvalid = new String("Input to lecture topic was invalid. Input 2 to 30 characters");
	private static final String teacherExisted = new String("This username already exists in Mentor system. Please give another username");
	private static final String courseExisted = new String("This course existed in Mentor system. Please wait while checking permissions");
	private static final String noCourseAccess = new String("This course is registered to another username. Please use a correct username or create a new course");
	private static final String passwordIncorrect = new String("Password was incorrect. Please try again");
	private static final String noSuchTeacher = new String("No such user found from the Mentor system. Please try again");
	private static final String noVisitorAccountsAvailable = new String("All (3) visitor accounts are online. Please try creating a private session");
	private static final String eitherHasToBeChosen = new String("Either one, new user or existing user, has to be chosen");
	//this password should be secured better, for example encrypted into some other, safer location
	private static final String tvisitor1Pwd = "XXXXXXXX";
	private static final int MIN_LENGTH = 2;
	private static final int MAX_LENGTH = 30;
	private static final int MIN_CLENGTH = 5;
	private static final int MAX_CLENGTH = 15;
	private static final int MIN_VALUE = 1;
	private static final int MAX_VALUE = 999;
	private static ProgressBar progressBar;
	private static EditText courseNameEdit;
	private static EditText pwdEdit;
	private static EditText unameEdit;
	private static EditText teacherNameEdit;
	private static EditText lectureNumberEdit;
	private static EditText lectureTopicEdit;
	private static EditText courseSubjectEdit;
	private static String teacherNameText;
	private static String defCourseName = "";
	private static String defLectureTopic = "";
	private String ipStr = "";
	private String coursesUrl = "";
	private String usersUrl = "";
	private String lectureNumberText = "";
	private String lectureTopicText = "";
	private String courseSubjectText = "";
	private String courseNameText = "";
	private String privateCourse = "";
	private String pwdGot = "";
	protected String unameGot = "";
	private boolean courseExists = false;
	private boolean lectureExists = false;
	private boolean lectureTopicDefaulted = false;
	private boolean courseNameDefaulted = false;
	private boolean lectureChosen = false;
	private boolean exerciseChosen = false;
	private boolean permissionToContinue = false;
	private boolean newUserChosen = false;
	private boolean existingUserChosen = false;
	private boolean privateUser = false;
	private int lectureNumber = 0;
	protected int tvisitorNum = 0;
	protected int getCounter = 0;
	Teacher teacher;
	CredentialFragment credFragment;
	SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teacher_view);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        defCourseName = sp.getString("courseNamePref", "");
        defLectureTopic = sp.getString("lectureTopicPref", "");
		//ip is fetched from layout/values/strings.xml
		ipStr = getResources().getString(R.string.IP);
		teacher = new Teacher();
		coursesUrl = "http://"+ipStr+"/mentor/courses";
		usersUrl = "http://"+ipStr+"/mentor/users";
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.finish();
	}
	
	/**
	 * DialogFragment which creates (an alert) popUp dialog for receiving user credentials
	 */
	public static class CredentialFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View rootView = inflater.inflate(R.layout.fragment_credentials,null);
			builder.setView(rootView);
			pwdEdit = (EditText) rootView.findViewById(R.id.pwdEdit);
			unameEdit = (EditText) rootView.findViewById(R.id.unameEdit);
			InputFilter filter = new InputFilter() {
				@Override
				public CharSequence filter(CharSequence source, int start, int end,
						Spanned dest, int dstart, int dend) {
					for(int i=start; i < end; i++) {
						if(allowed.indexOf(source.charAt(i)) < 0)
							return "";
					}
					return null;
				}
			};
			pwdEdit.setFilters(new InputFilter[]{filter});
			unameEdit.setFilters(new InputFilter[]{filter});
			builder.setTitle(R.string.credentials_title)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//do nothing special, yet
					}
				});
			return builder.create();
		}
	};
	
	/**
	 * Main fragment containing the main UI of this view
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_teacher_view,
					container, false);
			courseNameEdit = (EditText) rootView.findViewById(R.id.course_name);
			teacherNameEdit = (EditText) rootView.findViewById(R.id.teacher_name);
			lectureNumberEdit = (EditText) rootView.findViewById(R.id.lecture_number);
			lectureTopicEdit = (EditText) rootView.findViewById(R.id.lecture_topic);
			courseSubjectEdit = (EditText) rootView.findViewById(R.id.course_subject);
			progressBar = (ProgressBar) rootView.findViewById(R.id.tvProgressBar);
			//progressbar is set to indeterminate mode to make it progress infinitely
			progressBar.setIndeterminate(true);
			//and set to invisible at the beginning
			progressBar.setVisibility(View.GONE);
			InputFilter filter = new InputFilter() {
				@Override
				public CharSequence filter(CharSequence source, int start, int end,
						Spanned dest, int dstart, int dend) {
					for(int i=start; i < end; i++) {
						if(allowed.indexOf(source.charAt(i)) < 0)
							return "";
					}
					return null;
				}
			};
			InputFilter filter2 = new InputFilter() {
				@Override
				public CharSequence filter(CharSequence source, int start, int end,
						Spanned dest, int dstart, int dend) {
					for(int i=start; i < end; i++) {
						if(numAllowed.indexOf(source.charAt(i)) < 0)
							return "";
					}
					return null;
				}
			};
			courseNameEdit.setFilters(new InputFilter[]{filter});
			teacherNameEdit.setFilters(new InputFilter[]{filter});
			lectureNumberEdit.setFilters(new InputFilter[]{filter2});
			lectureTopicEdit.setFilters(new InputFilter[]{filter});
			courseSubjectEdit.setFilters(new InputFilter[]{filter});
			if(!defCourseName.equals(""))
				courseNameEdit.setText(defCourseName);
			if(!defLectureTopic.equals(""))
				lectureTopicEdit.setText(defLectureTopic);
			return rootView;
		}
	}
	
	/**
	 * Handle all default clicks here
	 */
	public void defaultClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();
		switch(view.getId()) {
			case R.id.default_course:
				if(checked)
					courseNameDefaulted = true;
				else
					courseNameDefaulted = false;
				break;
			case R.id.default_lecture:
				if(checked)
					lectureTopicDefaulted = true;
				else
					lectureTopicDefaulted = false;
				break;
		}
	}

	/**
	 * Handle clicks to private course here
	 */
	public void privateClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();
		switch(view.getId()) {
			case R.id.private_checkbox:
				if(checked)
					privateCourse = "1"; //private = 1 means course private, all other values mean public
				else
					privateCourse = "0";
				break;
		}
	}	

	/**
	 * Handle "new user or existing user" radio button clicked
	 */
	public void onUserTypeClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch(view.getId()) {
			case R.id.newUser:
				if(checked)
					newUserChosen = true;
				else
					newUserChosen = false;
				break;
			case R.id.existingUser:
				if(checked)
					existingUserChosen = true;
				else
					existingUserChosen = false;
				break;
		}		
	}	
	
	/**
	 * Handle "lecture or exercise" radio button clicked
	 */
	public void onLectureRbClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch(view.getId()) {
			case R.id.lecture:
				if(checked)
					lectureChosen = true;
				else
					lectureChosen = false;
				break;
			case R.id.exercise:
				if(checked)
					exerciseChosen = true;
				else
					exerciseChosen = false;
				break;
		}		
	}

	
	/**
	 * Checks if the network connection is OK
	 */
	public boolean networkConnected() {
		ConnectivityManager connMan = (ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isConnectedOrConnecting())
			return true;
		else
			return false;
	}	
	
	/**
	 * Fetches the IP address of the user from user's WIFI connection
	 * Idea from: http://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
	 */
	public String getIpAddress() {
		String ipAddress;
		WifiManager wifiManager = (WifiManager)this.getApplicationContext().getSystemService(WIFI_SERVICE);
		int ipAddr = wifiManager.getConnectionInfo().getIpAddress();
		if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
			ipAddr = Integer.reverseBytes(ipAddr);
		byte[] ipBytes = BigInteger.valueOf(ipAddr).toByteArray();
		try {
			ipAddress = InetAddress.getByAddress(ipBytes).getHostAddress();
		}
		catch(UnknownHostException ex) {
			ipAddress = null;
		}
		return ipAddress;
	}

	/**
	 * Handle Send button clicked when user credentials were set
	 */
	public void sendCredentialsClicked(View view) {
		String unameText = unameEdit.getText().toString();
		String pwdText = pwdEdit.getText().toString();
		boolean unameOk = false;
		boolean pwdOk = false;
		//check that username input is in correct form
		if(unameText.trim().equals("") || unameText.length() < MIN_CLENGTH) {
			Toast.makeText(this, userNameTooShort, Toast.LENGTH_SHORT).show();
			unameOk = false;
		}
		else if(unameText.length() > MAX_CLENGTH) {
			Toast.makeText(this, userNameTooLong, Toast.LENGTH_SHORT).show();
			unameOk = false;
		}
		else {
			//username was OK, so save it to the global variable
			unameGot = unameText;
			unameOk = true;
		}
		//check that password input is in correct form
		if(pwdText.trim().equals("") || pwdText.length() < MIN_CLENGTH) {
			Toast.makeText(this, passWordTooShort, Toast.LENGTH_SHORT).show();
			pwdOk = false;
		}
		else if(pwdText.length() > MAX_CLENGTH) {
			Toast.makeText(this, passWordTooLong, Toast.LENGTH_SHORT).show();
			pwdOk = false;
		}
		else {
			//password was OK, so save it to the global variable
			pwdGot = pwdText;
			pwdOk = true;
		}
		if(unameOk == true && pwdOk == true) {
			privateUser = true;
			//new user chosen
			if(newUserChosen == true) {
				Toast.makeText(getApplicationContext(), connectingServer, Toast.LENGTH_SHORT).show();
				teacher.setUserName(unameGot);
				teacher.setTeacherPwd(pwdGot);
				new PostTeacherTask().execute("/"+unameText);
				newUserChosen = false;
			}
			//existing user chosen
			else if(existingUserChosen == true) {
				new GetCredUserTask().execute("/"+unameText);
				existingUserChosen = false;
			}
			//neither one was chosen
			else
				Toast.makeText(getApplicationContext(), eitherHasToBeChosen, Toast.LENGTH_SHORT).show();
			//dismiss the dialog
			if(credFragment != null) {
				if(credFragment.isAdded() == true || credFragment.isVisible())
					credFragment.dismiss();
			}
		}
	}
	
	/**
	 * When "Start" button clicked, start TeacherVisionActivity
	 */
	public void loadTeacherVision(View view) {
		permissionToContinue = true;
		//check first, that can the EditText input values be accepted
		if(!courseNameEdit.getText().toString().trim().equals("") && courseNameEdit.getText().toString().length() 
				>= MIN_LENGTH && courseNameEdit.getText().toString().length() <= MAX_LENGTH) {
			courseNameText = courseNameEdit.getText().toString();
		}
		else {
			Toast.makeText(this, courseNameInvalid, Toast.LENGTH_SHORT).show();
			permissionToContinue = false;
		}
		if(!lectureNumberEdit.getText().toString().trim().equals("") ) {
			if(Integer.parseInt(lectureNumberEdit.getText().toString()) <= MAX_VALUE && Integer.parseInt(lectureNumberEdit.getText().toString()) >= MIN_VALUE) {
				lectureNumberText = lectureNumberEdit.getText().toString();
				lectureNumber = Integer.parseInt(lectureNumberEdit.getText().toString());				
			}
			else {
				Toast.makeText(this, lectureNumberInvalid, Toast.LENGTH_SHORT).show();
				permissionToContinue = false;
			}
		}
		else {
			lectureNumberText = "";
			lectureNumber = 0;
		}
		if(!lectureTopicEdit.getText().toString().trim().equals("") && lectureTopicEdit.getText().toString().length() 
				>= MIN_LENGTH && lectureTopicEdit.getText().toString().length() <= MAX_LENGTH) {
			lectureTopicText = lectureTopicEdit.getText().toString();
		}
		else {
			Toast.makeText(this, lectureTopicInvalid, Toast.LENGTH_SHORT).show();
			permissionToContinue = false;
		}
		if(!teacherNameEdit.getText().toString().trim().equals("") ) {
			if(teacherNameEdit.getText().toString().length() >= MIN_LENGTH && teacherNameEdit.getText().toString().length() <= MAX_LENGTH) {
				teacherNameText = teacherNameEdit.getText().toString();			
			}
			else {
				Toast.makeText(this, teacherNameInvalid, Toast.LENGTH_SHORT).show();
				permissionToContinue = false;
			}
		}
		else {
			teacherNameText = "";
		}
		if(!courseSubjectEdit.getText().toString().trim().equals("") ) {
			if(courseSubjectEdit.getText().toString().length() >= MIN_LENGTH && courseSubjectEdit.getText().toString().length() <= MAX_LENGTH) {
				courseSubjectText = courseSubjectEdit.getText().toString();			
			}
			else {
				Toast.makeText(this, courseSubjectInvalid, Toast.LENGTH_SHORT).show();
				permissionToContinue = false;
			}
		}
		else {
			courseSubjectText = "";
		}
		//get IP-address of the user
		String ipAddr = getIpAddress();
		boolean ipAddrOk = true;
		// NOTE: if IP-address was not gained from WiFi-connection, we could try to get it elsewhere
		if(ipAddr != null) {
			if(ipAddr.equals(""))
				ipAddrOk = false;
		}
		else
			ipAddrOk = false;
		//if service is used, required ipAddrOk == true before passing this point
		if(permissionToContinue == true) {
			teacher.setCourseName(courseNameText);
			if(privateUser == true)
				teacher.setTeacherName(teacherNameText);
			if(ipAddrOk)
				teacher.setDeviceIp(ipAddr);
			teacher.setLectureNumber(lectureNumber);
			teacher.setLectureTopic(lectureTopicText);
			if(courseNameDefaulted) {
				teacher.setCourseNameDefaulted(1);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("courseNamePref", courseNameText);
				//editor.clear();
				editor.commit();
			}
			else
				teacher.setCourseNameDefaulted(0);
			if(lectureTopicDefaulted) {
				teacher.setLectureTopicDefaulted(1);
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("lectureTopicPref", lectureTopicText);
				editor.commit();
			}
			else
				teacher.setLectureTopicDefaulted(0);
			if(privateCourse.equals("1")) {
				//show credentials popup
				//dataflow if existing user: GetCredUser, PutCredUser, PostCourse, GetCourseId, PostLecture, GetLectureId
				//dataflow if new user: PostTeacher, PostCourse, GetCourseId, PostLecture, GetLectureId
				//if existing course, otherwise similar dataflow, except PutCourseOnline after GetCourseId and before PostLecture
				//if existing lecture, otherwise similar dataflow, except PutLectureOnline After GetLectureId
				if(credFragment == null) {
					credFragment = new CredentialFragment();
					if(credFragment.isAdded() == false)
						credFragment.show(getFragmentManager(), "popUp");
				}
				else {
					if(credFragment.isVisible() == false)
						credFragment.show(getFragmentManager(), "popUp");
				}
			}
			else {
				progressBar.setVisibility(View.VISIBLE);
				//dataflow: GetUser (max 3x), PostCourse, GetCourseId, PostLecture, GetLectureId, PutUser
				//if existing course, otherwise similar dataflow, except PutCourseOnline after GetCourseId and before PostLecture
				//if existing lecture, otherwise similar dataflow, except PutLectureOnline After GetLectureId and before PutUser
				Toast.makeText(getApplicationContext(), connectingServer, Toast.LENGTH_SHORT).show();
				new GetUserTask().execute("/tvisitor1");
			}
		}
	}
	
	/**
	 * This task is used to send max 3x HTTP GET requests to the server. These tasks search which tvisitor account 
	 * is offline and if offline tvisitor account found, then uses it. if all tvisitor accounts are online, inform user about it and stop the process flow
	 *  */
	private class GetUserTask extends AsyncTask<String, Integer, Double> {
		protected int getUserCode = 0;
		String results = "";
		boolean resOk = false;
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected()) {
				try {			
					tvisitorNum = Integer.parseInt(params[0].substring(9,10));
					URL urli = new URL(usersUrl+params[0]);
					HttpURLConnection httpCon = (HttpURLConnection)urli.openConnection();
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestMethod("GET");
					httpCon.setReadTimeout(CONNTIME);
					httpCon.setConnectTimeout(CONNTIME);
					//httpCon.connect();
					reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream(), "UTF-8"));
					StringBuilder sb = new StringBuilder();
					while(true) {
						String line = reader.readLine();
						if(line == null)
							break;
						sb.append(line).append("\n");
					}
					results = sb.toString();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "TVISITOR GET SUCCEEDED");
						resOk = true;
					}
					else {
						Log.e(TAG, "TVISITOR GET FAILED");
						resOk = false;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET USERS "+e);
					getUserCode = 5;
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET USERS "+e);
					getUserCode = 5;
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at get user "+e);
							getUserCode = 5;
						}
					}
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				getUserCode = 6;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			int online = 0;
			if(results.contains("online")) {
				String[] onlineStr = results.split("online");
				String onlineValue = onlineStr[1].substring(3, 4);
				online = Integer.parseInt(onlineValue);
				if(online == 1)
					getUserCode = 1;
				else if(online == 0) {
					online = 2;
					getUserCode = 2;
				}
			}
			if(online == 2) {
				if(results.contains(new String("user_id"))) {
					String [] uidStr = results.split("user_id");
					String [] uidStopParts = uidStr[1].split(",");
					int uidStop = (uidStopParts[0].length());
					String uidValue = uidStr[1].substring(3, uidStop);
					teacher.setUserId(Integer.parseInt(uidValue));
					if(tvisitorNum == 1) {
						unameGot = "tvisitor1";
						teacher.setUserName("tvisitor1");
					}
					else if(tvisitorNum == 2) {
						unameGot = "tvisitor2";
						teacher.setUserName("tvisitor2");
					}
					else if(tvisitorNum == 3) {
						unameGot = "tvisitor3";
						teacher.setUserName("tvisitor3");
					}
					teacher.setTeacherPwd(tvisitor1Pwd);
				}
			}
			if(resOk == true) {
				if(getUserCode == 2)
					getUserCode = 3;
				else if(getUserCode == 1)
					getUserCode = 4;
			}
			if(getUserCode == 3) {
				//tvisitorX was offline. continue
				if(tvisitorNum == 1) {
					unameGot = "tvisitor1";
					teacher.setUserName("tvisitor1");
				}
				else if(tvisitorNum == 2) {
					unameGot = "tvisitor2";
					teacher.setUserName("tvisitor2");
				}
				else if(tvisitorNum == 3) {
					unameGot = "tvisitor3";
					teacher.setUserName("tvisitor3");
				}
				new PostCourseTask().execute(courseNameText);
			}
			else if(getUserCode == 4 && getCounter == 0) {
				//tvisitor1 was online
				getCounter++;
				new GetUserTask().execute("/tvisitor2");
			}
			else if(getUserCode == 4 && getCounter == 1) {
				//tvisitor2 was online
				getCounter++;
				new GetUserTask().execute("/tvisitor3");
			}
			else if(getUserCode == 4 && getCounter == 2) {
				//tvisitor3 was online
				Toast.makeText(getApplicationContext(), noVisitorAccountsAvailable, Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.GONE);
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(getUserCode == 5) {
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
					MainViewActivity.problemOccurred = true;
				}
				else if(getUserCode == 6)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task creates a new teacher-typed user into the server's database.
	 *  */	
	private class PostTeacherTask extends AsyncTask<String, Integer, Double> {
		protected int creationCode = 0;
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("password", pwdGot);
						jsonObj.put("name", teacher.getUserName());
						jsonObj.put("type", "1");
						jsonObj.put("number", "0");
						jsonObj.put("email", "something@something.com");
						jsonObj.put("online", "1");
						jsonObj.put("mobile_user", "1");
						jsonObj.put("mobile_ip", teacher.getDeviceIp());
						if(exerciseChosen)
							jsonObj.put("is_exercise", 1);
						else if(lectureChosen)
							jsonObj.put("is_exercise", 0);
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(usersUrl+params[0]);
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Authorization", "admin");
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestMethod("POST");
					httpCon.setConnectTimeout(CONNTIME);
					httpCon.setReadTimeout(CONNTIME);
					writer = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
					writer.write(jsonStr);
					writer.flush();
					writer.close();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
						Log.i(TAG, "TEACHER CREATION SUCCEEDED");
						creationCode = 1;
					}
					else if(httpCon.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
						Log.e(TAG, "THIS TEACHER ALREADY EXISTS");
						creationCode = 2;
					}
					else {
						Log.e(TAG, "TEACHER CREATION FAILED "+httpCon.getResponseCode());
						creationCode = 3;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST TEACHER "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST TEACHER "+e);
					creationCode = 4;
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				creationCode = 5;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			//if creation succeeded, continue to PostCourseTask
			//if conflict, show credentials panel again and show toast that user already exists
			if(creationCode == 1) {
				teacher.setTeacherPwd(pwdGot);
				new PostCourseTask().execute(courseNameText);
			}
			else if(creationCode == 2) {
				Toast.makeText(getApplicationContext(), teacherExisted, Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.GONE);
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(creationCode == 4) {
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
					MainViewActivity.problemOccurred = true;
				}
				else if(creationCode == 5)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * This task is used to send HTTP GET request to the server.
	 * The task searches for user from the server, who gave credentials for private session.
	 *  */	
	private class GetCredUserTask extends AsyncTask<String, Integer, Double> {
		protected int getCode = 0;
		protected int okCode = 0;
		String results = "";
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected()) {
				try {
					URL urli = new URL(usersUrl+params[0]);
					HttpURLConnection httpCon = (HttpURLConnection)urli.openConnection();
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestMethod("GET");
					httpCon.setReadTimeout(CONNTIME);
					httpCon.setConnectTimeout(CONNTIME);
					reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream(), "UTF-8"));
					StringBuilder sb = new StringBuilder();
					while(true) {
						String line = reader.readLine();
						if(line == null)
							break;
						sb.append(line).append("\n");
					}
					results = sb.toString();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK)
						okCode = 1;
					else if(httpCon.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
						getCode = 2;
					else
						getCode = 1;
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET USERS "+e);
					getCode = 6;
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET USERS "+e);
					getCode = 7;
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at get cred user "+e);
							getCode = 7;
						}
					}
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				getCode = 8;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(results.contains("password")) {
				String[] pwdStr = results.split("password");
				String [] pwdStopParts = pwdStr[1].split(",");
				int pwdStop = (pwdStopParts[0].length());
				String pwdValue = pwdStr[1].substring(4, pwdStop-1);
				Log.e(TAG, "PWD IS "+pwdValue);
				if(pwdValue.equals(pwdGot)) {
					teacher.setTeacherPwd(pwdGot);
					getCode = 3;
				}
				else {
					okCode = 2;
				}
			}
			if(results.contains("user_id")) {
				String [] uidStr = results.split("user_id");
				String [] uidStopParts = uidStr[1].split(",");
				int uidStop = (uidStopParts[0].length());
				String uidValue = uidStr[1].substring(3, uidStop);
				Log.e(TAG, "UID IS "+uidValue);
				teacher.setUserId(Integer.parseInt(uidValue));
			}
			//if uname found and passwd was correct, then continue
			if(getCode == 3 && okCode == 1) {
				Toast.makeText(getApplicationContext(), loggingIn, Toast.LENGTH_SHORT).show();
				new PutCredUserTask().execute();
			}
			else if(getCode == 1 || getCode == 2) {
				if(getCode == 2) {
					//password was not correct
					Toast.makeText(getApplicationContext(), passwordIncorrect, Toast.LENGTH_SHORT).show();
					progressBar.setVisibility(View.GONE);
				}
				else if(getCode == 1) {
					//no such user existed
					Toast.makeText(getApplicationContext(), noSuchTeacher, Toast.LENGTH_SHORT).show();
					progressBar.setVisibility(View.GONE);
				}
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(getCode == 7) {
					Toast.makeText(getApplicationContext(), noSuchTeacher, Toast.LENGTH_SHORT).show();
					//MainViewActivity.problemOccurred = true;
				}
				else if(getCode == 8)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
				else if(okCode == 2)
					Toast.makeText(getApplicationContext(), passwordIncorrect,Toast.LENGTH_SHORT).show();
			}
		}
	}	
	
	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task simply puts user online who started a private session and who gave credentials correctly.
	 *  */	
	private class PutCredUserTask extends AsyncTask<String, Integer, Double> {
		protected int putCode = 0;
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("password", pwdGot);
						jsonObj.put("name", "x");
						jsonObj.put("email", "something@something.com");
						jsonObj.put("number", "123");
						jsonObj.put("online", "1");
						jsonObj.put("mobile_user", "1");
						jsonObj.put("mobile_ip", teacher.getDeviceIp());
						jsonObj.put("marker_number", "0");
						jsonObj.put("reg_to_course", "0");
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(usersUrl+"/"+unameGot);
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Authorization", "admin");
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestMethod("PUT");
					httpCon.setConnectTimeout(CONNTIME);
					httpCon.setReadTimeout(CONNTIME);
					writer = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
					writer.write(jsonStr);
					writer.flush();
					writer.close();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
						Log.i(TAG, "USER EDIT SUCCEEDED");
						putCode = 1;
					}
					else {
						Log.e(TAG, "USER EDIT FAILED "+httpCon.getResponseCode());
						putCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT PUT CRED USER "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT PUT CRED USER "+e);
					putCode = 3;
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				putCode = 4;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			//if succeeded, continue. else show toast
			if(putCode == 1) {
				teacher.setUserName(unameGot);
				new PostCourseTask().execute(courseNameText);
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(putCode == 3)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(putCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * This task is used to send HTTP GET request to the server.
	 * The task gets id of the course created
	 *  */	
	private class GetCourseIdTask extends AsyncTask<String, Integer, Double> {
		protected int getIdCode = 0;
		String results = "";
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected()) {
				try {
					URL urli = new URL(coursesUrl+"/"+teacher.getCourseName());
					HttpURLConnection httpCon = (HttpURLConnection)urli.openConnection();
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestMethod("GET");
					httpCon.setReadTimeout(CONNTIME);
					httpCon.setConnectTimeout(CONNTIME);
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "COURSE ID GET SUCCEEDED");
						getIdCode = 1;
					}
					else {
						Log.e(TAG, "COURSE ID GET FAILED");
						getIdCode = 2;
					}
					reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream(), "UTF-8"));
					StringBuilder sb = new StringBuilder();
					while(true) {
						String line = reader.readLine();
						if(line == null)
							break;
						sb.append(line).append("\n");
					}
					results = sb.toString();
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET USERS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET USERS "+e);
					getIdCode = 3;
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at get course id "+e);
							getIdCode = 3;
						}
					}
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				getIdCode = 4;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(getIdCode == 1) {
				if(results.contains("course_id")) {
					String [] courseIdPartsArr = results.split("course_id");
					String [] idStopParts = courseIdPartsArr[1].split(",");
					int idStop = (idStopParts[0].length());
					String courseIdStr = courseIdPartsArr[1].substring(3, idStop);
					int courseId = Integer.parseInt(courseIdStr);
					teacher.setCourseId(courseId);
				}
				if(courseExists == true) {
					//inspect also is it private
					if(results.contains("private")) {
						String[] privateStr = results.split("private");
						String privateValue = privateStr[1].substring(3, 4);
						int coursePrivate = 0;
						if(privateValue.contains("1"))
							coursePrivate = Integer.parseInt(privateValue);
						if(coursePrivate == 1) {
							//inspect also teacher_uname field
							if(results.contains("teacher_uname")) {
								String[] unameStrPartsArr = results.split("teacher_uname");
								String [] unameStopParts = unameStrPartsArr[1].split(",");
								int unameStop = (unameStopParts[0].length());
								String unameStr = unameStrPartsArr[1].substring(4, unameStop-1);
								//if course's teacher_uname matches teacher.getUserName(), let the user pass
								//Log.e(TAG, "UNAME "+unameStr +"TEACHER UNAME "+teacher.getUserName());
								if(unameStr.equals(teacher.getUserName())) {
									new PutCourseOnlineTask().execute();
								}
								else {
									progressBar.setVisibility(View.GONE);
									//show toast
									Toast.makeText(getApplicationContext(), noCourseAccess, Toast.LENGTH_SHORT).show();
								}
							}
						}
						else if(coursePrivate == 0) {
							//just let the user continue
							new PutCourseOnlineTask().execute();
						}					
					}
				}
				else {
					//Create lecture
					new PostLectureTask().execute();
				}
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(getIdCode == 3)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(getIdCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task simply puts user online who started a public session
	 *  */		
	public class PutUserTask extends AsyncTask<String, Integer, Double> {
		protected int putUserCode = 0;
		@Override
		protected void onPreExecute() {
			//do something
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					//int tvisitorNum = Integer.parseInt(params[0].substring(9,10));
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("password", tvisitor1Pwd);
						jsonObj.put("name", "tvisitor"+teacher.getTvisitorChosen());
						jsonObj.put("email", "something@something.com");
						jsonObj.put("number", "123");
						jsonObj.put("online", "1");
						jsonObj.put("mobile_user", "1");
						jsonObj.put("mobile_ip", teacher.getDeviceIp());
						jsonObj.put("marker_number", "0");
						jsonObj.put("reg_to_course", "0");
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(usersUrl+"/"+teacher.getUserName());
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Authorization", "admin");
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestMethod("PUT");
					httpCon.setConnectTimeout(CONNTIME);
					httpCon.setReadTimeout(CONNTIME);
					writer = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
					writer.write(jsonStr);
					writer.flush();
					writer.close();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
						Log.i(TAG, "USER EDIT SUCCEEDED");
						putUserCode = 1;
					}
					else {
						Log.e(TAG, "USER EDIT FAILED "+httpCon.getResponseCode());
						putUserCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST COURSE "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST COURSE "+e);
					putUserCode = 3;
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				putUserCode = 4;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			progressBar.setVisibility(View.GONE);
			if(putUserCode == 1) {
				Intent intent = new Intent(getApplicationContext(), TeacherVisionActivity.class);
				//need to pass created teacher object to the next activity
				intent.putExtra("teacherObj", teacher);
				startActivity(intent);
			}
			else {
				//do something
				progressBar.setVisibility(View.GONE);
				if(putUserCode == 3)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(putUserCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task is used to create new course by courseName given by the user.
	 *  */	
	private class PostCourseTask extends AsyncTask<String, Integer, Double> {
		protected int postCourseCode = 0;
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("name", teacher.getCourseName());
						jsonObj.put("subject", courseSubjectText);
						jsonObj.put("max_students", "44");
						jsonObj.put("reg_students", "4");
						jsonObj.put("teacher_id", teacher.getUserId()); //add teacherId variable here after fetched
						jsonObj.put("teacher_uname", teacher.getUserName());
						jsonObj.put("private", privateCourse);
						jsonObj.put("online", "1");
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl);
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Authorization", "admin");
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestMethod("POST");
					httpCon.setConnectTimeout(CONNTIME);
					writer = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
					writer.write(jsonStr);
					writer.flush();
					writer.close();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
						Log.i(TAG, "COURSE CREATION SUCCEEDED");
						postCourseCode = 1;
					}
					else if(httpCon.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
						Log.e(TAG, "THIS COURSE ALREADY EXISTS");
						postCourseCode = 2;
					}
					else {
						Log.e(TAG, "COURSE CREATION FAILED "+httpCon.getResponseCode());
						postCourseCode = 3;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST COURSE "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST COURSE "+e);
					postCourseCode = 4;
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				postCourseCode = 5;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			//Get id of the created course
			if(postCourseCode == 1) {
				new GetCourseIdTask().execute();
			}
			else if(postCourseCode == 2) {
				Toast.makeText(getApplicationContext(), courseExisted, Toast.LENGTH_SHORT).show();
				courseExists = true;
				new GetCourseIdTask().execute();
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(postCourseCode == 4)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(postCourseCode == 5)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task puts the course online if existing (and private session) was used.
	 *  */		
	private class PutCourseOnlineTask extends AsyncTask<String, Integer, Double> {
		protected int courseOnlineCode = 0;
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						//update other data at the same time aswell
						jsonObj.put("name", teacher.getCourseName());
						jsonObj.put("subject", courseSubjectText);
						jsonObj.put("max_students", "44");
						jsonObj.put("reg_students", "4");
						jsonObj.put("teacher_id", teacher.getUserId()); //add teacherId variable here after fetched
						jsonObj.put("teacher_uname", teacher.getUserName());
						jsonObj.put("private", privateCourse);
						jsonObj.put("online", "1");
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+teacher.getCourseId());
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Authorization", "admin");
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestMethod("PUT");
					httpCon.setConnectTimeout(CONNTIME);
					httpCon.setReadTimeout(CONNTIME);
					writer = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
					writer.write(jsonStr);
					writer.flush();
					writer.close();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
						Log.i(TAG, "COURSE ONLINE SUCCEEDED");
						courseOnlineCode = 1;
					}
					else {
						Log.e(TAG, "COURSE ONLINE FAILED "+httpCon.getResponseCode());
						courseOnlineCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT COURSE OFFLINE"+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT COURSE OFFLINE "+e);
					courseOnlineCode = 3;
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				courseOnlineCode = 4;
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(courseOnlineCode == 1) {
				new PostLectureTask().execute();
			}
			else {
				//handle failure
				progressBar.setVisibility(View.GONE);
				if(courseOnlineCode == 3)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(courseOnlineCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}	
	
	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task creates a new lecture into the server, by using lectureTopic, given by the user
	 *  */		
	public class PostLectureTask extends AsyncTask<String, Integer, Double> {
		protected int postLectureCode = 0;
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("topic", lectureTopicText);
						jsonObj.put("number", lectureNumberText);
						jsonObj.put("online", "1");
						if(exerciseChosen)
							jsonObj.put("is_exercise", "1");
						else if(lectureChosen)
							jsonObj.put("is_exercise", "0");
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					String courseIdStr = Integer.toString(teacher.getCourseId());
					URL url = new URL(coursesUrl+"/"+courseIdStr+"/lectures");
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Authorization", "admin");
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestMethod("POST");
					httpCon.setConnectTimeout(CONNTIME);
					writer = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
					writer.write(jsonStr);
					writer.flush();
					writer.close();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
						Log.i(TAG, "LECTURE CREATION SUCCEEDED");
						postLectureCode = 1;
					}
					else if(httpCon.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
						Log.e(TAG, "THIS LECTURE ALREADY EXISTS");
						postLectureCode = 2;
					}
					else {
						Log.e(TAG, "LECTURE CREATION FAILED "+httpCon.getResponseCode());
						postLectureCode = 3;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST COURSE "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST COURSE "+e);
					postLectureCode = 4;
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				postLectureCode = 5;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(postLectureCode == 1 || postLectureCode == 2) {
				teacher.setLectureTopic(lectureTopicText);
				if(postLectureCode == 2)
					lectureExists = true;
				new GetLectureIdTask().execute();
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(postLectureCode == 4)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(postLectureCode == 5)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}	

	/**
	 * This task is used to send HTTP GET request to the server.
	 * The task gets the id of lecture created.
	 *  */		
	public class GetLectureIdTask extends AsyncTask<String, Integer, Double> {
		protected int getLecturesCode = 0;
		String results = "";
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected()) {
				try {
					URL urli = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures");
					HttpURLConnection httpCon = (HttpURLConnection)urli.openConnection();
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestMethod("GET");
					httpCon.setReadTimeout(CONNTIME);
					httpCon.setConnectTimeout(CONNTIME);
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "LECTURES GET SUCCEEDED");
						getLecturesCode = 1;
					}
					else {
						Log.e(TAG, "LECTURES GET FAILED");
						getLecturesCode = 2;
					}
					reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream(), "UTF-8"));
					StringBuilder sb = new StringBuilder();
					while(true) {
						String line = reader.readLine();
						if(line == null)
							break;
						sb.append(line).append("\n");
					}
					results = sb.toString();
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET LECTURES "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET LECTURES "+e);
					getLecturesCode = 3;
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at get lecture id "+e);
							getLecturesCode = 3;
						}
					}
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				getLecturesCode = 4;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(getLecturesCode == 1) {
				if(teacher.getLectureTopic() != null) {
					if(results.contains(teacher.getLectureTopic())) {
						String [] lecturePartsArr = results.split(teacher.getLectureTopic());
						String [] afterTopicPartsArr = lecturePartsArr[1].split("lecture_id");
						String [] idStopParts = afterTopicPartsArr[1].split("[}]");
						int idStop = (idStopParts[0].length());
						String lectureIdStr = afterTopicPartsArr[1].substring(3, idStop); 
						int lectureId = Integer.parseInt(lectureIdStr); 
						teacher.setLectureId(lectureId);
					}
				}
				if(lectureExists == true) {
					new PutLectureOnlineTask().execute();
				}
				else {
					if(privateUser == true)
						progressBar.setVisibility(View.GONE);
					//Put user online
					if(privateUser == false) {
						new PutUserTask().execute("/"+unameGot);
					}
					else if(privateUser == true) {				
						if(credFragment != null) {
							credFragment.dismiss();
						}
						Intent intent = new Intent(getApplicationContext(), TeacherVisionActivity.class);
						//need to pass created teacher object to the next activity
						intent.putExtra("teacherObj", teacher);
						startActivity(intent);
					}
				}
			}
			else {
				//get lectures failure
				progressBar.setVisibility(View.GONE);
				if(getLecturesCode == 3)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(getLecturesCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}	
	
	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task puts existing lecture online
	 *  */		
	private class PutLectureOnlineTask extends AsyncTask<String, Integer, Double> {
		protected int lectureOnlineCode = 0;
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("topic", lectureTopicText);
						jsonObj.put("number", lectureNumberText);
						jsonObj.put("online", "1");
						if(exerciseChosen)
							jsonObj.put("is_exercise", "1");
						else if(lectureChosen)
							jsonObj.put("is_exercise", "0");
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId());
					HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Authorization", "admin");
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestProperty("Content-type", "application/json");
					httpCon.setRequestMethod("PUT");
					httpCon.setConnectTimeout(CONNTIME);
					httpCon.setReadTimeout(CONNTIME);
					writer = new BufferedWriter(new OutputStreamWriter(httpCon.getOutputStream(), "UTF-8"));
					writer.write(jsonStr);
					writer.flush();
					writer.close();
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
						Log.i(TAG, "LECTURE ONLINE SUCCEEDED");
						lectureOnlineCode = 1;
					}
					else {
						Log.e(TAG, "LECTURE ONLINE FAILED "+httpCon.getResponseCode());
						lectureOnlineCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT LECTURE ONLINE"+e);
				}
				catch(IOException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT LECTURE ONLINE "+e);
					lectureOnlineCode = 3;
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				lectureOnlineCode = 4;
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(lectureOnlineCode == 1) {
				if(privateUser == true)
					progressBar.setVisibility(View.GONE);
				//Put user online
				if(privateUser == false) {
					new PutUserTask().execute("/"+unameGot);
				}
				else if(privateUser == true) {				
					if(credFragment != null) {
						credFragment.dismiss();
					}
					Intent intent = new Intent(getApplicationContext(), TeacherVisionActivity.class);
					//need to pass created teacher object to the next activity
					intent.putExtra("teacherObj", teacher);
					startActivity(intent);
				}
			}
			else {
				//handle failure
				progressBar.setVisibility(View.GONE);
				if(lectureOnlineCode == 3)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(lectureOnlineCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}	
}
