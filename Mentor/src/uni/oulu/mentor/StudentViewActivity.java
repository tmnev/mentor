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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;
import android.text.InputFilter;
import android.text.Spanned;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class StudentViewActivity extends Activity {
	private static final String TAG = "StudentView";
	protected static final int CONNTIME = 30000;
	private static final String userNameTooShort = new String("Input to username was too short. Input min 5 characters");
	private static final String passWordTooShort = new String("Input to password was too short. Input min 5 characters");
	private static final String userNameTooLong = new String("Input to username was too long. Input max 15 characters");
	private static final String passWordTooLong = new String("Input to password was too long. Input max 15 characters");
	private static final String studentNameInvalid = new String("If set, student name must have 2 to 30 characters");
	private static final String wifiOff = new String("Network connection failed. Try to put your WIFI on");
	private static final String allowed = new String("0123456789abcdefghijklmnopqrstuvwxyzåäöABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ");
	private static final String noVisitorAccountsAvailable = new String("All (3) visitor accounts are online. Please try creating a private session");
	private static final String svisitorPwd = "XXXXXXXX";
	private static final String studentExisted = new String("This username already exists in Mentor system. Please give another username");
	private static final String passwordIncorrect = new String("Password was incorrect. Please try again");
	private static final String noSuchStudent = new String("No such user found from the Mentor system. Please try again");
	private static final String serverDown = new String("Server down, try again after few minutes or contact the administrator");
	private static final String connectingServer = new String("Please wait a moment, connecting to the server");
	private static final String loggingIn = new String("Please wait a moment, logging in");
	private static final int MIN_LENGTH = 2;
	private static final int MAX_LENGTH = 30;
	private static final int MIN_CLENGTH = 5;
	private static final int MAX_CLENGTH = 15;
	private static EditText pwdEdit;
	private static EditText unameEdit;
	private EditText studentNameEdit;
	private static String studentNameText;
	private String privateSession = "";
	private String ipStr;
	private String coursesUrl;
	private String usersUrl;
	private String pwdGot;
	protected String unameGot;
	private boolean newUserChosen = false;
	private boolean existingUserChosen = false;
	protected boolean privateUser = false;
	private int courseCounter = 0;
	private int numOfTasks = 0;
	private int svisitorNum = 0;
	private int getCounter = 0;
	List<String> parentList;
	HashMap<String, List<String>> childList;
	static List<Course> courseList;
	static List<Lecture> lecList;
	static ProgressBar progressBar;
	Student student;
	CredentialFragment credFragment;
	SvExpandableListAdapter svExpListAdapter;
	ExpandableListView svExpListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_view);
		ipStr = getResources().getString(R.string.IP);
		coursesUrl = "http://"+ipStr+"/mentor/courses";
		usersUrl = "http://"+ipStr+"/mentor/users";
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linLayout = (LinearLayout)inflater.inflate(R.layout.fragment_student_view, null);
		addContentView(linLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		//instantiate expandable list view
		svExpListView = (ExpandableListView)linLayout.findViewById(R.id.sv_exp_list_view);
		//instantiate lists and hash map
		parentList = new ArrayList<String>();
		courseList = new ArrayList<Course>();
		lecList = new ArrayList<Lecture>();
		childList = new HashMap<String, List<String>>();
		courseCounter = 0;
		//begin to fetch courses and lectures from the server instantly
		new GetCoursesTask(linLayout.getContext()).execute();
		studentNameEdit = (EditText) findViewById(R.id.student_name);
		//this user's data is stored into this instance
		student = new Student();
		//set progress bar to indeterminate mode and set it visible
		progressBar = (ProgressBar) findViewById(R.id.svProgressBar);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.VISIBLE);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.finish();
	}
	@Override
	protected void onResume() {
		courseCounter = 0;
		super.onResume();
	}
	
	@Override
	protected void onStart() {
		courseCounter = 0;
		super.onStart();
	}
	
	/** increment the number of tasks **/
	public void addTask() {
		numOfTasks++;
	}
	
	/** decrease the number of tasks **/
	public void removeTask() {
		numOfTasks--;
	}

	/** show the courses and lectures fetched from the server only when the data transfer process is finished **/
	public void tasksCompleted(Context context) {
		if(numOfTasks == 0) {
			progressBar.setVisibility(View.GONE);
			svExpListAdapter = new SvExpandableListAdapter(context, parentList, childList);
			svExpListView.setAdapter(svExpListAdapter);
			svExpListView.setOnGroupClickListener(onParentClickListener);
			svExpListView.setOnGroupCollapseListener(onParentCollapseListener);
			svExpListView.setOnGroupExpandListener(onParentExpandListener);
			svExpListView.setOnChildClickListener(onChildClickListener);
		}
	}

	OnGroupClickListener onParentClickListener = new OnGroupClickListener() {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
			//do something
			Log.i("PARENT CLICKED ", "BY NUMBER "+groupPosition);
			return false;
		}
	};
	
	OnGroupExpandListener onParentExpandListener = new OnGroupExpandListener() {
		@Override
		public void onGroupExpand(int groupPosition) {
			//do something
		}
	};
	
	OnGroupCollapseListener onParentCollapseListener = new OnGroupCollapseListener() {
		@Override
		public void onGroupCollapse(int groupPosition) {
			//do something
		}
	};
	
	/**
	 * Overriding the functionality of OnChildClickListener-class and it's onChildClick-method
	 */
	OnChildClickListener onChildClickListener = new OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
			Log.i("CHILD CLICKED ", "BY NUMBER "+childPosition);
			/* Idea of getting parent index from: http://vardhan-justlikethat.blogspot.fi/2013/10/android-highlighting-selected-item-in.html */
			int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition,childPosition));
			parent.setItemChecked(index, true);
			//set this lecture topic chosen for the user
			student.setLectureTopicChosen(childList.get(parentList.get(groupPosition)).get(childPosition).toString());
			//fetch from the lecList the lecture, which has this similar topic ...
			Iterator<Lecture> iter = lecList.iterator();
			while(iter.hasNext()) {
				Lecture lecture = iter.next();
				if(lecture.getLectureTopic().equals(student.getLectureTopicChosen())) {
					//... and store to student the lecture's data which is considered important
					student.setLectureIdChosen(lecture.getLectureId());
					student.setCourseIdChosen(lecture.getCourseId());
				}
			}
			return false;
		}
	};


	
	/**
	 * DialogFragment which creates (an alert) popUp dialog for receiving user credentials
	 */
	public static class CredentialFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View rootView = inflater.inflate(R.layout.fragment_svcredentials,null);
			builder.setView(rootView);
			pwdEdit = (EditText) rootView.findViewById(R.id.pwdEdit);
			unameEdit = (EditText) rootView.findViewById(R.id.unameEdit);
			unameEdit.setText(studentNameText);
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
						//do nothing for now
					}
				});
			return builder.create();
		}
	};
	
	/* Basic knowledge of using ExpandableListView from: http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/  */
	public class SvExpandableListAdapter extends BaseExpandableListAdapter {
		Context context;
		List<String> parent;
		HashMap<String,List<String>> child;
		
		public SvExpandableListAdapter(Context context, List<String> parent, HashMap<String,List<String>> child) {
			super();
			this.context = context;
			this.parent = parent;
			this.child = child;
		}
		
		@Override
		public int getGroupCount() {
			return this.parent.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return this.child.get(this.parent.get(groupPosition)).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return this.parent.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return this.child.get(this.parent.get(groupPosition)).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			String tvText = (String)getGroup(groupPosition);
			if(convertView == null) {
				LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.list_parent, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.listParent);
			tv.setText(tvText);
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			String tvText = (String)getChild(groupPosition, childPosition);
			if(convertView == null) {
				LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.list_child, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.listChild);
			tv.setText(tvText);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}
	};
	
	/**
	 * Handle radio button clicked
	 */
	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch(view.getId()) {
			case R.id.marker_one:
				if(checked) {
					student.setMarkerNumber(1);
				}
				break;
			case R.id.marker_two:
				if(checked) {
					student.setMarkerNumber(2);
				}
				break;
			case R.id.marker_three:
				if(checked) {
					student.setMarkerNumber(3);
				}
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
	 * Fetches the IP address of the user from user's WiFi-connection
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
	 * Handle Send button clicked when user credentials added
	 */
	public void sendSvCredentialsClicked(View view) {
		String unameText = unameEdit.getText().toString();
		String pwdText = pwdEdit.getText().toString();
		boolean unameOk = false;
		boolean pwdOk = false;
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
			if(newUserChosen == true) {
				student.setUserName(unameText);
				Toast.makeText(getApplicationContext(), connectingServer, Toast.LENGTH_SHORT).show();
				new PostStudentTask().execute("/"+unameText);				
			}
			else if(existingUserChosen == true) {
				new GetCredStudentTask().execute("/"+unameText);
			}
			if(credFragment != null) {
				if(credFragment.isAdded()== true || credFragment.isVisible())
					credFragment.dismiss();
			}
		}
	}

	/**
	 * Handle "new user or existing user" radio button clicked
	 */
	public void onSvUserTypeClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch(view.getId()) {
			case R.id.newSvUser:
				if(checked)
					newUserChosen = true;
				else
					newUserChosen = false;
				break;
			case R.id.existingSvUser:
				if(checked)
					existingUserChosen = true;
				else
					existingUserChosen = false;
				break;
		}		
	}	
	
	/**
	 * Handle clicks to private session here
	 */
	public void privateSvClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();
		switch(view.getId()) {
			case R.id.private_sv_checkbox:
				if(checked)
					privateSession = "1";
				else
					privateSession = "0";
				break;
		}
	}
	
	/**
	 * Start StudentVisionActivity
	 */
	public void loadStudentVision(View view) {
		String ipAddr = getIpAddress();
		boolean permissionToContinue = true;
		//boolean markerOk = false;
		int mNum = 0;
		mNum = student.getMarkerNumber();
		if(mNum == 0)
			student.setMarkerNumber(1);
		boolean ipAddrOk = true;
		/* NOTE: make some more tests for IP address */
		if(ipAddr != null) {
			if(ipAddr.equals(""))
				ipAddrOk = false;
		}
		else {
			ipAddrOk = false;
		}
		studentNameText = "";
		if(!studentNameEdit.getText().toString().trim().equals("") ) {
			if(studentNameEdit.getText().toString().length() >= MIN_LENGTH && studentNameEdit.getText().toString().length() <= MAX_LENGTH) {
				studentNameText = studentNameEdit.getText().toString();			
			}
			else {
				Toast.makeText(this, studentNameInvalid, Toast.LENGTH_SHORT).show();
				permissionToContinue = false;
			}
		}
		//if service is used, required ipAddrOk == true before passing this point
		if(permissionToContinue == true) {
			if(ipAddrOk)
				student.setDeviceIp(ipAddr);
			student.setStudentName(studentNameText);
			if(privateSession.equals("1")) {
				//show credentials popup
				//dataflow if existing user: GetCredStudent, PutStudent
				//dataflow if new user: PostStudent
				if(credFragment == null) {
					credFragment = new CredentialFragment();
					if(credFragment.isAdded() == false)
						credFragment.show(getFragmentManager(), "svPopUp");
				}
				else {
					if(credFragment.isVisible() == false)
						credFragment.show(getFragmentManager(), "svPopUp");
				}
			}
			else {
				//dataflow: GetStudent (max 3x), PutStudent
				Toast.makeText(getApplicationContext(), connectingServer, Toast.LENGTH_SHORT).show();
				new GetStudentTask().execute("/svisitor1");
			}
		}
	}

	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task creates a new student-typed user into the server's database.
	 *  */		
	private class PostStudentTask extends AsyncTask<String, Integer, Double> {
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
						//jsonObj.put("password", student.getStudentPwd());
						jsonObj.put("password", pwdGot);
						jsonObj.put("name", student.getStudentName());
						jsonObj.put("type", "0");
						jsonObj.put("number", "0");
						jsonObj.put("email", "something@something.com");
						jsonObj.put("online", "1");
						jsonObj.put("mobile_user", "1");
						jsonObj.put("mobile_ip", student.getDeviceIp());
						jsonObj.put("marker_number", student.getMarkerNumber());
						jsonObj.put("lec_top_chosen", student.getLectureTopicChosen());
						jsonObj.put("reg_to_course", student.getCourseIdChosen());
						jsonObj.put("reg_to_lecture", student.getLectureIdChosen());
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
						Log.i(TAG, "STUDENT CREATION SUCCEEDED");
						creationCode = 1;
					}
					else if(httpCon.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
						Log.e(TAG, "THIS STUDENT ALREADY EXISTS");
						creationCode = 2;
					}
					else {
						Log.e(TAG, "STUDENT CREATION FAILED "+httpCon.getResponseCode());
						creationCode = 3;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST COURSE "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST COURSE "+e);
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
			progressBar.setVisibility(View.GONE);
			//if creation succeeded, continue to StudentVision
			//if conflict, show credentials panel again and show toast that user already exists
			if(creationCode == 1) {
				//CONTINUE TO STUDENT VISION
				Intent intent = new Intent(getApplicationContext(), StudentVisionActivity.class);
				//need to pass student object to the next activity
				intent.putExtra("studentObj", student);
				startActivity(intent);
			}
			else if(creationCode == 2) {
				Toast.makeText(getApplicationContext(), studentExisted, Toast.LENGTH_SHORT).show();			
			}
			else {
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
	 * The task searches for user  from the server, who gave credentials for private session.
	 *  */		
	private class GetCredStudentTask extends AsyncTask<String, Integer, Double> {
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
						okCode = 1;
					}
					else if(httpCon.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
						getCode = 2;
					}
					else
						getCode = 1;
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET USERS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET USERS "+e);
					getCode = 8;
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at close get cred user "+e);
							getCode = 8;
						}
					}
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				getCode = 9;
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
				if(pwdValue.equals(pwdGot)) {
					student.setStudentPwd(pwdGot);
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
				student.setUserId(Integer.parseInt(uidValue));
			}
			//if uname found and passwd was correct, then continue
			if(getCode == 3 && okCode == 1) {
				student.setUserName(unameGot);
				Toast.makeText(getApplicationContext(), loggingIn, Toast.LENGTH_SHORT).show();
				new PutStudentTask().execute();
			}
			else if(getCode == 1 || getCode == 2 || getCode == 6) {
				if(getCode == 2) {
					//password was not correct
					Toast.makeText(getApplicationContext(), passwordIncorrect, Toast.LENGTH_SHORT).show();
					progressBar.setVisibility(View.GONE);
				}
				else if(getCode == 1 || getCode == 6) {
					//no such user existed
					Toast.makeText(getApplicationContext(), noSuchStudent, Toast.LENGTH_SHORT).show();
					progressBar.setVisibility(View.GONE);
				}
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(getCode == 8) {
					Toast.makeText(getApplicationContext(), noSuchStudent, Toast.LENGTH_SHORT).show();
					MainViewActivity.problemOccurred = true;
				}
				else if(getCode == 9)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
				else if(okCode == 2)
					Toast.makeText(getApplicationContext(), passwordIncorrect, Toast.LENGTH_SHORT).show();
			}
		}
	}	

	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task simply puts user online who started a public session
	 *  */			
	private class PutStudentTask extends AsyncTask<String, Integer, Double> {
		protected int putCode = 0;
		@Override
		protected void onPreExecute() {
			//do something
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected()) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("password", student.getStudentPwd());
						jsonObj.put("name", student.getStudentName());
						jsonObj.put("email", "something@something.com");
						jsonObj.put("number", "0");
						jsonObj.put("online", "1");
						jsonObj.put("mobile_user", "1");
						jsonObj.put("type", "0");
						jsonObj.put("mobile_ip", student.getDeviceIp());
						jsonObj.put("marker_number", student.getMarkerNumber());
						jsonObj.put("lec_top_chosen", student.getLectureTopicChosen());
						jsonObj.put("reg_to_course", student.getCourseIdChosen());
						jsonObj.put("reg_to_lecture", student.getLectureIdChosen());					
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(usersUrl+"/"+student.getUserName()); 
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
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST COURSE "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST COURSE "+e);
					putCode = 3;
				}				
			}
			else {
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
			progressBar.setVisibility(View.GONE);
			//if succeeded, continue. else show toast
			if(putCode == 1) {
				//SUCCESS, GOTO STUDENT VISION
				Intent intent = new Intent(getApplicationContext(), StudentVisionActivity.class);
				//need to pass student object to the next activity
				intent.putExtra("studentObj", student);
				startActivity(intent);
			}
			else {
				if(putCode == 3) 
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(putCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * This task is used to send max 3x HTTP GET requests to the server. These tasks search which svisitor account 
	 * is offline and if offline svisitor account found, then uses it. if all svisitor accounts are online, inform user about it and stop the process flow
	 *  */	
	private class GetStudentTask extends AsyncTask<String, Integer, Double> {
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
					svisitorNum = Integer.parseInt(params[0].substring(9,10));
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
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "SVISITOR GET SUCCEEDED");
						resOk = true;
					}
					else {
						Log.e(TAG, "SVISITOR GET FAILED");
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
							Log.e(TAG, "IOException at get student "+e);
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
				if(online == 1) {
					getUserCode = 1;
				}
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
					student.setUserId(Integer.parseInt(uidValue));
					if(svisitorNum == 1)
						student.setUserName("svisitor1");
					else if(svisitorNum == 2)
						student.setUserName("svisitor2");
					else if(svisitorNum == 3)
						student.setUserName("svisitor3");
					student.setStudentPwd(svisitorPwd);
				}
			}
			if(resOk == true) {
				if(getUserCode == 2)
					getUserCode = 3;
				else if(getUserCode == 1)
					getUserCode = 4;
			}
			if(getUserCode == 3) {
				//svisitorX was offline. continue
				new PutStudentTask().execute();
			}
			else if(getUserCode == 4 && getCounter == 0) {
				//tvisitor1 was online
				getCounter++;
				new GetStudentTask().execute("/svisitor2");
			}
			else if(getUserCode == 4 && getCounter == 1) {
				//tvisitor2 was online
				getCounter++;
				new GetStudentTask().execute("/svisitor3");
			}
			else if(getUserCode == 4 && getCounter == 2) {
				//tvisitor3 was online
				Toast.makeText(getApplicationContext(), noVisitorAccountsAvailable, Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.GONE);
			}
			else if(getUserCode == 5) {
				Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.GONE);
				MainViewActivity.problemOccurred = true;
			}
			else if(getUserCode == 6) {
				Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
				progressBar.setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * This task starts a data transfer process to the server. In this process, HTTP GET request are sent to server.
	 * This task gets all courses from server, and within onPostExecute-method of this task, single courses are separated from all courses.
	 * For each course found from the results, a new GetLectures task is executed.
	 */
	public class GetCoursesTask extends AsyncTask<String, Integer, Double> {
		protected int getCoursesCode = 0;
		String results = "";
		private Context mContext;
		public GetCoursesTask(Context context) {
			mContext = context;
		}
		@Override
		protected void onPreExecute() {
			addTask();
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected()) {
				try {
					URL urli = new URL(coursesUrl);
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
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "COURSES GET SUCCEEDED");
						getCoursesCode = 1;
					}
					else {
						Log.e(TAG, "COURSES GET FAILED");
						getCoursesCode = 2;
					}
				}
				catch(IOException e) {
					Log.e(TAG, "IOException was "+e);
					getCoursesCode = 3;
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at get course "+e);
							getCoursesCode = 3;
						}
					}
				}
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");
				getCoursesCode = 4;
				progressBar.setVisibility(View.GONE);
			}
			return null;
		}
		@Override
		protected void onPostExecute(Double result) {
			String commaStr = ",";
			String onlineStr = "online";
			String idStr = "course_id";
			String [] coursesArr = null;
			if(results.contains("}"))
				coursesArr = results.split("[}]");
			if(coursesArr != null) {
				for(int i=0; i < coursesArr.length; i++) {
					if(coursesArr[i].contains(onlineStr)) {
						String [] courseOnlineParts = coursesArr[i].split(onlineStr);
						String courseOnline = courseOnlineParts[1].substring(3, 4);
						int courseOn = 0;
						if(!courseOnline.equals("n"))
							courseOn = Integer.parseInt(courseOnline);
						if(courseOn == 1) {
							//GET NAME AND ID. STORE THESE TO LIST. GET DATA FROM coursesArr[i]
							//Course name
							String [] courseNameParts = coursesArr[i].split("name");
							//neglect course if it does not have a name
							if(courseNameParts.length > 1) {
								String [] nameStopParts = courseNameParts[1].split(commaStr); 
								int nameStop = (nameStopParts[0].length());
								String courseName = courseNameParts[1].substring(4, nameStop-1);
								//Course id
								String [] courseIdParts = coursesArr[i].split(idStr);
								String [] idStopParts = courseIdParts[1].split(commaStr);
								int idStop = (idStopParts[0].length());
								String courseIdStr = courseIdParts[1].substring(3, idStop);
								int courseId = Integer.parseInt(courseIdStr); //has ""1""
								Course course = new Course();
								course.setCourseId(courseId);
								course.setCourseName(courseName);
								parentList.add(courseName);
								courseList.add(course);
							}
						}		
					}
				}
			}
			if(getCoursesCode == 1) {
				//continue
				//FOREACH COURSE IN THE LIST, GET ALL LECTURES
				Iterator<Course> iter = courseList.iterator(); //if not null
				while(iter.hasNext()) {
					Course course = iter.next();
					String lecturesUrl = "http://"+ipStr+"/mentor/courses/"+course.getCourseId()+"/lectures";
					new GetLecturesTask(mContext).execute(lecturesUrl);
				}
			}
			else {
				//get courses failure
				progressBar.setVisibility(View.GONE);
				if(getCoursesCode == 3)
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				else if(getCoursesCode == 4)
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
			}
			removeTask();
			tasksCompleted(mContext);
		}
	}
	
	/**
	 * This task is used to send HTTP GEST request to the server.
	 * The task fetches all lectures within one course, from the server.
	 *  */	
	public class GetLecturesTask extends AsyncTask<String, Integer, Double> {
		protected int getLecturesCode = 0;
		String results = "";
		private Context mContext;
		public GetLecturesTask(Context context) {
			mContext = context;
		}
		@Override
		protected void onPreExecute() {
			addTask();
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressBar.setProgress(progress[0]);
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected()) {
				try {	
					URL urli = new URL(params[0]);
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
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "LECTURES GET SUCCEEDED");
						getLecturesCode = 1;
					}
					else {
						Log.e(TAG, "LECTURES GET FAILED");
						getLecturesCode = 2;
					}
				}
				catch(IOException e) {
					Log.e(TAG, "IOException was "+e);
					getLecturesCode = 3;
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at get lectures "+e);
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
		protected void onPostExecute(Double result) {
			if(getLecturesCode == 1) {
				List<String> childHeaders = new ArrayList<String>();
				String commaStr = ",";
				String [] lecturesArr = null;
				String onlineStr = "online";
				String idStr = "lecture_id";
				//String numberStr = "number";
				//String exerciseStr = "is_exercise";
				String courseIdStr = "course_id";
				String topicStr = "topic";
				if(results.contains("}")) {
					lecturesArr = results.split("[}]");
				}
				if(lecturesArr != null) {
					for(int i=0; i < lecturesArr.length; i++) {
						if(lecturesArr[i].contains(onlineStr)) {
							String [] lectureOnlineParts = lecturesArr[i].split(onlineStr);
							String lectureOnline = lectureOnlineParts[1].substring(3, 4);
							int lectureOn = 0;
							if(!lectureOnline.equals("n"))
								lectureOn = Integer.parseInt(lectureOnline);
							if(lectureOn == 1) {
								//GET COURSE_ID, NUMBER, IS_EXERCISE, TOPIC AND ID. STORE THESE TO LIST. GET DATA FROM lecturesArr[i]
								//Lecture id
								String [] lectureIdParts = lecturesArr[i].split(idStr);
								if(lectureIdParts.length > 1) {
									String [] idStopParts = lectureIdParts[1].split(commaStr); //java.lang.ArrayIndexOutOfBoundsException: length=1
									int idStop = (idStopParts[0].length());
									String lectureIdStr = lectureIdParts[1].substring(3, idStop);
									int lectureId = Integer.parseInt(lectureIdStr);
									//Lecture topic
									String [] lectureTopicParts = lecturesArr[i].split(topicStr);
									//neglect lectures which dont have a topic
									if(lectureTopicParts.length > 1) {
										String [] topicStopParts = lectureTopicParts[1].split(commaStr); //ArrayIndexOutOfBounds
										int topicStop = (topicStopParts[0].length());
										String lectureTopic = lectureTopicParts[1].substring(4, topicStop-1);
										//Course id
										String [] courseIdParts = lecturesArr[i].split(courseIdStr);
										String [] cidStopParts = courseIdParts[1].split(commaStr);
										int cidStop = (cidStopParts[0].length());
										String courseIdString = courseIdParts[1].substring(4, cidStop-1);
										int courseId = Integer.parseInt(courseIdString);
										//Lecture/Exercise number
										//do this only if set!
										/*int number = 0;
										String [] numberParts = lecturesArr[i].split(numberStr);
										if(numberParts.length > 0) {
											String [] numStopParts = numberParts[1].split(commaStr);
											int numStop = (numStopParts[0].length());
											String numberString = numberParts[1].substring(3, numStop);
											if(!numberString.equals("null"))
												number = Integer.parseInt(numberString);
										}*/
										//Lecture or exercise information
										//do this only if set!
										/*int isExercise = 0;
										String [] isExerciseParts = lecturesArr[i].split(exerciseStr);
										if(isExerciseParts.length > 0) {
											String isExerciseStr = isExerciseParts[1].substring(3, 4);
											if(!isExerciseStr.equals("n"))
												isExercise = Integer.parseInt(isExerciseStr);
										}*/
										Lecture lecture = new Lecture();
										lecture.setLectureId(lectureId);
										lecture.setLectureTopic(lectureTopic);
										lecture.setCourseId(courseId);
										/*if(number != 0)
											lecture.setNumber(number);
										if(isExercise != 0)
											lecture.setIsExercise(isExercise);*/
										childHeaders.add(lectureTopic);
										lecList.add(lecture);
									}
								}
							}		
						}
					}

				}

				//if(childHeaders.isEmpty())
				//	childHeaders.add("no lectures found");
				childList.put(parentList.get(courseCounter), childHeaders);
				courseCounter++;
			}
			else {
				progressBar.setVisibility(View.GONE);
				if(getLecturesCode == 3) {
					Toast.makeText(getApplicationContext(), serverDown, Toast.LENGTH_SHORT).show();
				}
				else if(getLecturesCode == 4) {
					Toast.makeText(getApplicationContext(), wifiOff, Toast.LENGTH_SHORT).show();
				}
			}
			removeTask();
			tasksCompleted(mContext);
		}
	}	
}
