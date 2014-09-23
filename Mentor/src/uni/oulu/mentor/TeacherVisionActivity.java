/*
* Client side mobile application of Mentor, which is a real-time feedback system for education
*
* Copyright (C) 2014 Tom Nevala
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package uni.oulu.mentor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DialogFragment;
/*Maybe useful imports later*/
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import uni.oulu.mentor.TeacherVisionService.TeacherVisionServiceBinder;
//import android.os.IBinder;
//import android.content.ComponentName;
//import android.content.ServiceConnection;
/*AndAR imports*/
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;

//add implements TeacherVisionListener, if comet-approach is used later
public class TeacherVisionActivity extends AndARActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = "TeacherVision";
	protected static final int CONNTIME = 30000;
	protected static final int POLLSPAN = 4000;
	private static final String allowed = new String("0123456789abcdefghijklmnopqrstuvwxyzåäöABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ ?");
	private static final String sliderPanelOpen = new String("Slider panel is open. Please close it before sending personal message");
	private static final String noRegStudent = new String("No registered student for this marker found");
	private static final int MIN_LENGTH = 2;
	private static final int MAX_LENGTH = 30;
	protected static final int NUM_ITEMS = 3;
	protected static EditText questionAllEdit;
	protected static EditText answerAllEdit;
	protected static EditText feedbackAllEdit;
	protected static EditText customTextEdit;
	private boolean beepOn = true;
	private boolean notificationOn = true;
	private static boolean listItemAnswer = false;
	protected boolean listenerCreated = false;
	private boolean closeDirSat = false;
	private boolean closePosSat = false;
	private boolean openDirSat = false;
	private boolean openPosSat = false;
	private boolean pollUsersCancelled = false;
	private boolean pollFeedbacksCancelled = false;
	private boolean pollQuestionsCancelled = false;
	private boolean pollAnswersCancelled = false;
	private static String questionAllContent = "";
	protected static String answerAllContent = "";
	private static String feedbackAllContent = "";
	private static String questionContent = "";
	private static String message = "";
	private static String coursesUrl;
	private String usersUrl;
	private String ipStr;
	private static int listItemQuid = 0;
	private static int chosenMarker = 0;
	private int translationX = 400;
	private int screenType = 0;
	protected int timeSpan = 1;
	protected int flareTime = 5;
	private int alertThreshold = 1;
	protected int immediate = 0;
	private int personalChosen = 0;
	private int speedUpColor = 1; //1 means green, 2 yellow, 3 red, 4 cyan, 5 blue, 6 magenta
	private int slowDownColor = 3;
	private int moreExamplesColor = 2;
	private int repeatColor = 4;
	private int questionColor = 5;
	private int answerColor = 6;
	private int maxPosOpenX = 1050; //default values
	private int minPosOpenX = 950;
	private int maxPosCloseX = 650;
	private int minPosCloseX = 550;
	private int maxPosY = 600;
	private int minPosY = 300;
	protected int timespan;
	private float screenWidth;
	private float screenHeight;
	//private int icon;
	//private static CharSequence contentTitle = "Mentor feedback message received";
	//private boolean bound = false;
	private AsyncTask<String, Integer, Double> pollUsersTask;
	private AsyncTask<String, Integer, Double> pollFeedbacksTask;
	private AsyncTask<String, Integer, Double> pollQuestionsTask;
	private AsyncTask<String, Integer, Double> pollAnswersTask;
	private static ArrayAdapter<String> mFeedbackAdapter;
	private static ArrayAdapter<String> mQuestionAdapter;
	private static ArrayAdapter<String> mAnswerAdapter;
	static PopUpFragment puFragment;
	CustomObject oneObject;
	CustomObject twoObject;
	CustomObject threeObject;
	ARToolkit artoolkit;
	SliderPanel spanel=null;
	SliderImage iv=null;
	GestureDetectorCompat mDetector;
	SensorManager mSensorManager;
	Sensor mRotation;
	TabAdapter mAdapter;
	ViewPager mPager;
	Button button, button2, button3;
	CustomRenderer renderer;
	static Teacher teacher;
	SharedPreferences sp;
	TeacherVisionFragment fragment;
	Notification feedbackNotification;
	NotificationManager mNotificationManager;
	//TeacherVisionConnection tvCon;
	//TeacherVisionService mService;
	private static EditTextPreference courseNamePref, lectureTopicPref;
	private static CheckBoxPreference beepPref, notificationPref;
	private static ListPreference alertThresholdPref, timespanPref, flaretimePref, speedUpPref, slowDownPref, moreExamplesPref, repeatPref, questionPref, answerPref;
	static List<Student> onlineStudentsList;
	static List<Feedback> feedbacksList;
	static List<Question> questionsList;
	static List<Question> ownQuestionsList;
	static List<Answer> answersList;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//DO NOT USE THIS SERVICE YET, DOES NOT WORK
		//Intent intent = new Intent(this, TeacherVisionService.class);
        //startService(intent);
		/*Idea from http://stackoverflow.com/questions/2902640/android-get-the-screen-resolution-pixels-as-integer-values */
		@SuppressWarnings("deprecation")
		int screenWidthPix = getWindowManager().getDefaultDisplay().getWidth();
		@SuppressWarnings("deprecation")
		int screenHeightPix = getWindowManager().getDefaultDisplay().getHeight();
		/*Idea from http://stackoverflow.com/questions/5015094/determine-device-screen-category-small-normal-large-xlarge-using-code*/
		if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
			screenType = 3;
		else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
			screenType = 4;
		else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
			screenType = 1;
		else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL)
			screenType = 2;
		//screen density is investigated here
	    DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;
        if (density==DisplayMetrics.DENSITY_HIGH) {
        	//400 dpi is 600px
        	if(screenType == 1)
        		translationX = 240; //240 / 1.5 = 160
        	else if(screenType == 2)
        		translationX = 315; //315 / 1.5 = 210 in dpi, screen 800 pix wide screen, needs about 1024/400 = 800/x => 312,5, 2.56
        	else if(screenType == 3)
        		translationX = 540; //540 / 1.5 = 360 in dpi
        	else if(screenType == 4)
        		translationX = 600; //600 / 1.5 = 400 in dpi	
        }
        else if (density==DisplayMetrics.DENSITY_MEDIUM) {
        	//400 dpi is 400px
        	if(screenType == 1)
        		translationX = 160; //160 / 1 = 160 in dpi
        	else if(screenType == 2)
        		translationX = 210; //210 / 1 = 210 in dpi
        	else if(screenType == 3)
        		translationX = 360; //360 / 1 = 360 in dpi
        	else if(screenType == 4)
        		translationX = 400; //400 / 1 = 400 in dpi
        }
        else if (density==DisplayMetrics.DENSITY_LOW) {
        	//400 dpi is 300px
        	if(screenType == 1)
        		translationX = 120; //120 / 0.75 = 160 in dpi
        	else if(screenType == 2)
        		translationX = 158; //158 / 0.75 = 210 in dpi
        	else if(screenType == 3)
        		translationX = 270; //270 / 0.75 = 360 in dpi
        	else if(screenType == 4)
        		translationX = 300; //300 / 0.75 = 400 in dpi
        }
        else if(density==DisplayMetrics.DENSITY_XHIGH || density==DisplayMetrics.DENSITY_XXHIGH) {
        	//400 dpi is 800px
        	if(screenType == 1)
        		translationX = 320; //320 / 2 = 160 in dpi
        	else if(screenType == 2)
        		translationX = 420; // 420 / 2 = 210 in dpi
        	else if(screenType == 3)
        		translationX = 720; // 720 / 2 = 360 in dpi
        	else if(screenType == 4)
        		translationX = 800; // 800 / 2 = 400 in dpi      	
        }
        else {
        	//not supported
        }
        int offsetX = ((int)screenWidthPix/20);
        int offsetY = ((int)screenHeightPix/10);
		maxPosOpenX = screenWidthPix;
		//boundary for minimum amount of width on x-axis, where the touch is seen as panel open touch
		minPosOpenX = screenWidthPix-offsetX;
		//boundary for maximum amount of width on x-axis, where the touch is seen as panel close touch
		maxPosCloseX = screenWidthPix-translationX+offsetX;
		//boundary for minimum amount of width on x-axis, where the touch is seen as panel close touch
		minPosCloseX = screenWidthPix-translationX-offsetX;
		//boundary for maximum amount of height on y-axis, where the touch is seen as panel open or close touch
		maxPosY = (screenHeightPix/2)+offsetY;
		//boundary for minimum amount of height on y-axis, where the touch is seen as panel open or close touch
		minPosY = (screenHeightPix/2)-offsetY;
		//instantiation of lists
		onlineStudentsList = new ArrayList<Student>();
		feedbacksList = new ArrayList<Feedback>();
		questionsList = new ArrayList<Question>();
		answersList = new ArrayList<Answer>();
		ownQuestionsList = new ArrayList<Question>();
		//data stored for this user is fetched here, from the previous activity, and stored into the teacher instance
		Intent intent2 = getIntent();
		teacher = (Teacher) intent2.getParcelableExtra("teacherObj");
		ipStr = getResources().getString(R.string.IP);
		usersUrl = "http://"+ipStr+"/mentor/users";
		coursesUrl = "http://"+ipStr+"/mentor/courses";
		//default preferences are fetched, and listener is set to listen for preference changes
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
    	//SharedPreferences.Editor editor = sp.edit();
    	//editor.putString("flaretimePref", "5");
    	//editor.clear();
    	//editor.commit();
        // In case of notifications are added later
        /*String ns = Context.NOTIFICATION_SERVICE;
    	mNotificationManager = (NotificationManager) getSystemService(ns);
    	icon = R.drawable.mlogo2;*/
		//AndAR preview
		renderer = new CustomRenderer();//optional, may be set to null
		super.setNonARRenderer(renderer);//or might be omitted
		try {
			//register an object for each marker type
			artoolkit = super.getArtoolkit();
			oneObject = new CustomObject("onePatt", "onePatt.patt", 80.0, new double[]{0,0}); //name, pattern, markerWidth, markerCenter, customColor
			twoObject = new CustomObject("twoPatt", "twoPatt.patt", 80.0, new double[]{0,0}); //name, pattern, markerWidth, markerCenter
			threeObject = new CustomObject("threePatt", "threePatt.patt", 80.0, new double[]{0,0}); //name, pattern, markerWidth, markerCenter
			artoolkit.registerARObject(oneObject);
			artoolkit.registerARObject(twoObject);
			artoolkit.registerARObject(threeObject);
		} catch (AndARException ex){
			Log.e("AndARException ", ex.getMessage());
		}
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vv = inflater.inflate(R.layout.activity_teacher_vision, null);
		//screen width and height in pixels
		screenWidth = (float)this.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
		screenHeight = (float)this.getApplicationContext().getResources().getDisplayMetrics().heightPixels;
		inflater.inflate(R.layout.fragment_teacher_vision, null);
		//UI elements are added on top of AndAR by calling this addContentView-method instead of setContentView
		super.addContentView(vv, (new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)));
		//create a layout for settings button. add it over video frames
		LinearLayout lil = new LinearLayout(this);
		Button settingsButton = new Button(this);
		settingsButton.setText("Settings");
		settingsButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		settingsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).addToBackStack("settings").commit();
			}
		});
		lil.addView(settingsButton);
		super.addContentView(lil, (new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)));
		//starts the AndAR
		startPreview();
		//mDetector is instantiated here for detecting gestures
		mDetector = new GestureDetectorCompat(this, new SwipeGestureListener());
		//view pager instantiation, also mAdapter instantiated and set as adapter for view pager
		mPager = (ViewPager)findViewById(R.id.pager);
		mAdapter = new TabAdapter(getFragmentManager(), mPager);
		mPager.setAdapter(mAdapter);
		//begin to poll online users
		pollUsersCancelled = false;
		pollUsersTask = new PollUsersTask(this);
		pollUsersTask.execute();
		//begin to poll feedbacks
		pollFeedbacksCancelled = false;
		pollFeedbacksTask = new PollFeedbacksTask(this);
		pollFeedbacksTask.execute();
		//begin to poll questions
		pollQuestionsCancelled = false;
		pollQuestionsTask = new PollQuestionsTask(this);
		pollQuestionsTask.execute();
		//begin to poll answers
		pollAnswersCancelled = false;
		pollAnswersTask = new PollAnswersTask(this);
		pollAnswersTask.execute();
		//initialize tab buttons
		button = (Button)findViewById(R.id.goto_first);
		//first one is not enabled at the beginning, because the user is in first tab at first 
		button.setEnabled(false);
		button2 = (Button)findViewById(R.id.goto_second);
		button3 = (Button)findViewById(R.id.goto_last);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(0);
                button.setEnabled(false);
                button2.setEnabled(true);
                button3.setEnabled(true);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(1);
                button.setEnabled(true);
                button2.setEnabled(false);
                button3.setEnabled(true);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPager.setCurrentItem(2);
                button.setEnabled(true);
                button2.setEnabled(true);
                button3.setEnabled(false);
            }
        });
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	
    @Override
    protected void onStart() {
    	super.onStart();
    	/* Here we bind to the service. This onStart method is also called when we return from another activity back to this activity. */
    	//Intent intent = new Intent(this, TeacherVisionService.class);
    	//bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
    	/* Here we unbind from the service. This onStop method is also called when this activity goes to the background,
    	 * when another Activity comes to the foreground. */
    	/*listenerCreated = false;
    	if(bound) {
    		unbindService(mServiceConnection);
    		bound = false;
    	}*/
    	super.onStop();
    }			

	/**
	 * Handle "Personal" radio button clicked
	 */
	public void onPersonalRbClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch(view.getId()) {
			case R.id.puFeedback:
				if(checked) {
					personalChosen = 1;
				}
				break;
			case R.id.puQuestion:
				if(checked) {
					personalChosen = 2;
				}
				break;
			case R.id.puAnswer:
				if(checked) {
					personalChosen = 3;
				}
				break;
		}		
	}	
	
	/**
	 * This method is used in the case when 3D-object on marker was clicked, and the teacher presses send-button from the dialog
	 */
	public void sendPersonalClicked(View v) {
		Iterator<Student> iter = onlineStudentsList.iterator();
		int userId = 0;
		while(iter.hasNext()) {			
			Student storedStudent = iter.next();
			if(storedStudent.getMarkerNumber() == chosenMarker) {
				//we want to send feedback especially to this student
				userId = storedStudent.getUserId();
			}
		}
		if(userId != 0) {
			if(!customTextEdit.getText().toString().trim().equals("") && customTextEdit.getText().toString().length() 
					>= MIN_LENGTH && customTextEdit.getText().toString().length() <= MAX_LENGTH) {
				message = customTextEdit.getText().toString();
			}
			//send the feedback, delegated_to this userId
			if(personalChosen == 1)
				new PostFeedbackToStudentTask(this, userId).execute(); 
			else if(personalChosen == 2)
				new PostQuestionToStudentTask(this, userId).execute();
			else if(personalChosen == 3)
				new PostAnswerToStudentTask(this, userId).execute();
			if(personalChosen != 0) {
				if(puFragment != null) {
					if(puFragment.isAdded() == true || puFragment.isVisible())
						puFragment.dismiss();
				}
			}
		}
		else {
			Toast.makeText(getApplicationContext(), noRegStudent, Toast.LENGTH_SHORT).show();
		}
		chosenMarker = 0;
	}
	
	@Override
	protected void onDestroy() {
		pollUsersCancelled = true;
		pollFeedbacksCancelled = true;
		pollQuestionsCancelled = true;
		pollAnswersCancelled = true;
		new ExitUserTask(this).execute();
		new PutCourseOfflineTask(this).execute();
		new PutLectureOfflineTask(this).execute();
    	/*if(tvCon != null) {
    		tvCon.disconnect();
    		tvCon = null;
    	}
    	if(bound) {
    		unbindService(mServiceConnection);
    		bound = false;
    	}
    	if(mService != null) {
    		mService.stopSelf();
    		mService = null;
    	}*/
		super.onDestroy();
		this.finish();
	}
	
	/**
	 * Checks if the network connection is OK
	 */
	public static boolean networkConnected(Context context) {
		ConnectivityManager connMan = (ConnectivityManager)context.getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isConnectedOrConnecting())
			return true;
		else
			return false;
	}
	
	/**
	 * This method tries to estimate the offset (amount of space given for 3D-object from it's center)
	 * by using screensize-type and screen density as a base for decision
	 */
	public int getTouchOffset(int farAwayCoeff) {
		int offset = 100;
		if(screenType == 4 && farAwayCoeff == 0)
			offset = 250;
		else if(screenType == 4 && farAwayCoeff == 1)
			offset = 150;
		else if(screenType == 4 && farAwayCoeff == 2)
			offset = 100;
		else if(screenType == 4 && farAwayCoeff == 3)
			offset = 80;
		else if(screenType == 4 && farAwayCoeff == 4)
			offset = 50;
		if(screenType == 3 && farAwayCoeff == 0)
			offset = 180;
		else if(screenType == 3 && farAwayCoeff == 1)
			offset = 130;
		else if(screenType == 3 && farAwayCoeff == 2)
			offset = 90;
		else if(screenType == 3 && farAwayCoeff == 3)
			offset = 50;
		else if(screenType == 3 && farAwayCoeff == 4)
			offset = 30;
		if(screenType == 2 && farAwayCoeff == 0)
			offset = 160;
		else if(screenType == 2 && farAwayCoeff == 1)
			offset = 120;
		else if(screenType == 2 && farAwayCoeff == 2)
			offset = 80;
		else if(screenType == 2 && farAwayCoeff == 3)
			offset = 40;
		else if(screenType == 2 && farAwayCoeff == 4)
			offset = 20;
		if(screenType == 1 && farAwayCoeff == 0)
			offset = 120;
		else if(screenType == 1 && farAwayCoeff == 1)
			offset = 90;
		else if(screenType == 1 && farAwayCoeff == 2)
			offset = 50;
		else if(screenType == 1 && farAwayCoeff == 3)
			offset = 20;
		else if(screenType == 1 && farAwayCoeff == 4)
			offset = 10;
		return offset;
	}
	
	/**
	 * This method is used to recognize touch events on 3D-objects (not for touches on panel's arrow)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);
		double x = (double) event.getX();
		double y = (double) event.getY();
		//when the user lifted a finger up, after the touch
		if(event.getAction() == MotionEvent.ACTION_UP) {
			int touchMarker = 0;
			if(oneObject.isVisible()) {
				double[] transMat = oneObject.getTransMatrix();
				String objName = oneObject.getPatternName();
				double xTranslat = transMat[3]; //about -100 to +100
				double yTranslat = transMat[7]; //about -100 to +100
				double zTranslat = transMat[11]; // about 100 to 500
				float halfWidthX = renderer.bounds[0]; //half of screen width in matrix form, actually the top left x corner of screen
				float halfHeightY = renderer.bounds[3]; //half of screen height in matrix form, actually the bottom right y corner of screen
				/* the formula used in here, is probably more easy to understand using this form
				float bxTrans = (float) xTranslat + halfWidthX;
				float bxCoef = (float) (screenWidth/(2*halfWidthX));
				float bxPos = bxCoef * bxTrans;*/
				xTranslat = xTranslat + halfWidthX;
				yTranslat = yTranslat + halfHeightY;
				float xx = (((float)xTranslat) * screenWidth) / (2*halfWidthX);
				float yy = (((float)yTranslat) * screenHeight) / (2*halfHeightY);
				int howFarAway = 1; //0 is nearest, 1 is close, 2 is quite close, 3 is middle, 4 is quite far, 5 is far
				if(zTranslat <= 100)
					howFarAway = 0;
				else if(zTranslat > 100 && zTranslat <= 200)
					howFarAway = 1;
				else if(zTranslat > 200 && zTranslat <= 300)
					howFarAway = 2;
				else if(zTranslat > 300 && zTranslat <= 400)
					howFarAway = 3;
				else if(zTranslat > 400 && zTranslat <= 500)
					howFarAway = 4;
				int touchOffset = getTouchOffset(howFarAway);
				if(xx > (x-touchOffset) && xx < (x+touchOffset) && yy > (y-touchOffset) && yy < (y+touchOffset)) {
					Log.i(TAG, "Object 1 touched!");
					if(objName.equals("onePatt.patt")) {
						touchMarker = 1;
					}
				}
			}
			if(twoObject.isVisible()) {
				double[] transMat = twoObject.getTransMatrix();
				String objName = twoObject.getPatternName();
				double xTranslat = transMat[3]; //about -100 to +100
				double yTranslat = transMat[7]; //about -100 to +100
				double zTranslat = transMat[11]; // about 100 to 500
				float halfWidthX = renderer.bounds[0];
				float halfHeightY = renderer.bounds[3];
				xTranslat = xTranslat + halfWidthX;
				yTranslat = yTranslat + halfHeightY;
				float xx = (((float)xTranslat) * screenWidth) / (2*halfWidthX);
				float yy = (((float)yTranslat) * screenHeight) / (2*halfHeightY);
				int howFarAway = 1; //0 is nearest, 1 is close, 2 is quite close, 3 is middle, 4 is quite far, 5 is far
				if(zTranslat <= 100)
					howFarAway = 0;
				else if(zTranslat > 100 && zTranslat <= 200)
					howFarAway = 1;
				else if(zTranslat > 200 && zTranslat <= 300)
					howFarAway = 2;
				else if(zTranslat > 300 && zTranslat <= 400)
					howFarAway = 3;
				else if(zTranslat > 400 && zTranslat <= 500)
					howFarAway = 4;
				int touchOffset = getTouchOffset(howFarAway);			
				if(xx > (x-touchOffset) && xx < (x+touchOffset) && yy > (y-touchOffset) && yy < (y+touchOffset)) {
					Log.i(TAG, "Object 2 touched!");
					if(objName.equals("twoPatt.patt")) {
						touchMarker = 2;
					}
				}
			}
			if(threeObject.isVisible()) {
				double[] transMat = threeObject.getTransMatrix();
				String objName = threeObject.getPatternName();
				double xTranslat = transMat[3]; //about -100 to +100
				double yTranslat = transMat[7]; //about -100 to +100
				double zTranslat = transMat[11]; // about 100 to 500
				float halfWidthX = renderer.bounds[0];
				float halfHeightY = renderer.bounds[3];
				xTranslat = xTranslat + halfWidthX;
				yTranslat = yTranslat + halfHeightY;
				float xx = (((float)xTranslat) * screenWidth) / (2*halfWidthX);
				float yy = (((float)yTranslat) * screenHeight) / (2*halfHeightY);
				int howFarAway = 1; //0 is nearest, 1 is close, 2 is quite close, 3 is middle, 4 is quite far, 5 is far
				if(zTranslat <= 100)
					howFarAway = 0;
				else if(zTranslat > 100 && zTranslat <= 200)
					howFarAway = 1;
				else if(zTranslat > 200 && zTranslat <= 300)
					howFarAway = 2;
				else if(zTranslat > 300 && zTranslat <= 400)
					howFarAway = 3;
				else if(zTranslat > 400 && zTranslat <= 500)
					howFarAway = 4;
				int touchOffset = getTouchOffset(howFarAway);			
				if(xx > (x-touchOffset) && xx < (x+touchOffset) && yy > (y-touchOffset) && yy < (y+touchOffset)) {
					Log.i(TAG, "Object 3 touched!");
					if(objName.equals("threePatt.patt")) {
						touchMarker = 3;
					}
				}
			}
			if(touchMarker != 0) {
				//show the popUp only if student with some marker number registered to this lecture, else show toast
				Iterator<Student> iter = onlineStudentsList.iterator();
				int markerNumber = 0;
				while(iter.hasNext()) {
					Student storedStudent = iter.next();
					if(storedStudent.getMarkerNumber() == touchMarker) {
						markerNumber = storedStudent.getMarkerNumber();
					}
				}
				if(markerNumber != 0) {
					//this marker number touched, found from the list of registered students, so continue
					chosenMarker = touchMarker;
					boolean spanelWasNull = false;
					boolean spanelOpen = false;
					if(spanel == null)
						spanelWasNull = true;
					else
						spanelOpen = spanel.getPanelOpen();
					//show the panel if it's not open
					if(spanelWasNull == true || spanelOpen == false) {
						if(puFragment == null) {
							puFragment = new PopUpFragment();
							if(puFragment.isAdded() == false)
								puFragment.show(getFragmentManager(), "popUp");
						}
						else {
							if(puFragment.isVisible() == false)
								puFragment.show(getFragmentManager(), "popUp");
						}
					}
					else {
						Toast.makeText(getApplicationContext(), sliderPanelOpen, Toast.LENGTH_SHORT).show();
						
					}
				}
				else {
					Toast.makeText(getApplicationContext(), noRegStudent, Toast.LENGTH_SHORT).show();
				}
				//reset the touched marker here to 0
				touchMarker = 0;
			}
		}
		return super.onTouchEvent(event);
	}
	
	/**
	 * Inform the user about exceptions that occurred in background threads.
	 * This exception is rather severe and can not be recovered from.
	 * Inform the user and shut down the application.
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("AndAR EXCEPTION", ex.getMessage());
		//if threads, setRunning(false)
		finish();
	}

    /* Here we receive events when user changes preference values. We take changed preferences
     * to variables, so this way we can use the freshly changed preference value without
     * restarting the whole application */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    	if(key.contentEquals("courseNamePref")) {
    		String courseName = sharedPreferences.getString("courseNamePref", "");
    		courseNamePref.setSummary("Value is "+courseName);
    		editor.putString(key, courseName);
    	}
    	else if(key.contentEquals("lectureTopicPref")) {
    		String lectureTopic = sharedPreferences.getString("lectureTopicPref", "");
    		lectureTopicPref.setSummary("Value is "+lectureTopic);
    		editor.putString(key, lectureTopic);
    	}
    	else if(key.contentEquals("beep")) {
    		beepOn = sharedPreferences.getBoolean(key, Boolean.parseBoolean("true"));
    		if(beepOn)
    			beepPref.setSummary(beepPref.getSummaryOn());
    		else
    			beepPref.setSummary(beepPref.getSummaryOff());
    		editor.putBoolean(key, beepOn);
    	}
    	else if(key.contentEquals("notification")) {
    		notificationOn = sharedPreferences.getBoolean(key, Boolean.parseBoolean("true"));
    		if(notificationOn)
    			notificationPref.setSummary(notificationPref.getSummaryOn());
    		else
    			notificationPref.setSummary(notificationPref.getSummaryOff());
    		editor.putBoolean(key, notificationOn);
    	}
    	else if(key.contentEquals("alertThresholdPref")) {
    		alertThreshold = Integer.parseInt(sharedPreferences.getString(key, "1"));
    		alertThresholdPref.setSummary("Value is "+sharedPreferences.getString(key, "1"));
    		//editor.putInt(key, alertThreshold);
    		editor.putString(key, sharedPreferences.getString(key, "1"));
    	}
    	else if(key.contentEquals("timespanPref")) {
    		timeSpan = Integer.parseInt(sharedPreferences.getString(key, "1"));
    		timespanPref.setSummary("Value is "+sharedPreferences.getString(key, "1"));
    		//editor.putInt(key, timeSpan);
    		editor.putString(key, sharedPreferences.getString(key, "1"));
    	}
    	else if(key.contentEquals("flaretimePref")) {
    		int xflareTime = Integer.parseInt(sharedPreferences.getString(key, "5"));
    		flaretimePref.setSummary("Value is "+sharedPreferences.getString(key, "5"));
    		oneObject.setFlareTime(xflareTime*1000);
    		twoObject.setFlareTime(xflareTime*1000);
    		threeObject.setFlareTime(xflareTime*1000);
    		//editor.putInt(key, xflareTime);
    		editor.putString(key, sharedPreferences.getString(key, "5"));
    	}
    	else if(key.contentEquals("speedUpPref")) {
    		speedUpColor = Integer.parseInt(sharedPreferences.getString(key, "1"));
    		speedUpPref.setSummary("Value is "+sharedPreferences.getString(key, "1"));
    		oneObject.setSpeedUpColor(speedUpColor);
    		twoObject.setSpeedUpColor(speedUpColor);
    		threeObject.setSpeedUpColor(speedUpColor);
    		//editor.putInt(key, speedUpColor);
    		editor.putString(key, sharedPreferences.getString(key, "1"));
    	}
    	else if(key.contentEquals("slowDownPref")) {
    		slowDownColor = Integer.parseInt(sharedPreferences.getString(key, "3"));
    		slowDownPref.setSummary("Value is "+sharedPreferences.getString(key, "3"));
    		oneObject.setSlowDownColor(slowDownColor);
    		twoObject.setSlowDownColor(slowDownColor);
    		threeObject.setSlowDownColor(slowDownColor);
    		//editor.putInt(key, slowDownColor);
    		editor.putString(key, sharedPreferences.getString(key, "3"));
    	}
    	else if(key.contentEquals("moreExamplesPref")) {
    		moreExamplesColor = Integer.parseInt(sharedPreferences.getString(key, "2"));
    		moreExamplesPref.setSummary("Value is "+sharedPreferences.getString(key, "2"));
    		oneObject.setMoreExamplesColor(moreExamplesColor);
    		twoObject.setMoreExamplesColor(moreExamplesColor);
    		threeObject.setMoreExamplesColor(moreExamplesColor);
    		//editor.putInt(key, moreExamplesColor);
    		editor.putString(key, sharedPreferences.getString(key, "2"));
    	}
    	else if(key.contentEquals("repeatPref")) {
    		repeatColor = Integer.parseInt(sharedPreferences.getString(key, "4"));
    		repeatPref.setSummary("Value is "+sharedPreferences.getString(key, "4"));
    		oneObject.setRepeatColor(repeatColor);
    		twoObject.setRepeatColor(repeatColor);
    		threeObject.setRepeatColor(repeatColor);
    		//editor.putInt(key, repeatColor);
    		editor.putString(key, sharedPreferences.getString(key, "4"));
    	}
    	else if(key.contentEquals("questionPref")) {
    		questionColor = Integer.parseInt(sharedPreferences.getString(key, "5"));
    		questionPref.setSummary("Value is "+sharedPreferences.getString(key, "5"));
    		oneObject.setQuestionColor(questionColor);
    		twoObject.setQuestionColor(questionColor);
    		threeObject.setQuestionColor(questionColor);
    		//editor.putInt(key, questionColor);
    		editor.putString(key, sharedPreferences.getString(key, "5"));
    	}
    	else if(key.contentEquals("answerPref")) {
    		answerColor = Integer.parseInt(sharedPreferences.getString(key, "6"));
    		answerPref.setSummary("Value is "+sharedPreferences.getString(key, "6"));
    		oneObject.setAnswerColor(answerColor);
    		twoObject.setAnswerColor(answerColor);
    		threeObject.setAnswerColor(answerColor);
    		//editor.putInt(key, answerColor);
    		editor.putString(key, sharedPreferences.getString(key, "6"));
    	}
    	//editor.clear();
    	editor.commit();
    }
	
    /**
     * This class is used for monitoring gestures, especially swipe gestures. Also touches on panel's arrow are monitored
     */
	class SwipeGestureListener extends SimpleOnGestureListener {
		//if user's finger is down, tapping the screen. this onDown method is used for inspecting panel open and close touches, not 3D-object touches
		@Override
		public boolean onDown(MotionEvent event) {		
			if(event.getY() > minPosY && event.getY() < maxPosY) {
				//touch was within accepted boundaries for closing the panel, so close position is satisfied
				if(event.getX() > minPosCloseX && event.getX() < maxPosCloseX) {
					closePosSat = true;
					openPosSat = false;
				}
				//touch was within accepted boundaries for opening the panel, so open position is satisfied
				if(event.getX() > minPosOpenX && event.getX() < maxPosOpenX) {
					openPosSat = true;
					closePosSat = false;
				}
			}
			else {
				closePosSat = false;
				openPosSat = false;
			}
			return true;
		}
		//if the user is making a swipe gesture
		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			float distX = event2.getX() - event1.getX();
			float distY = event2.getY() - event1.getY();
			//if there was more movement on x-axis than on y-axis
			if(Math.abs(distX) > Math.abs(distY)) {
				//if the movement on x-axis was more than one pixels to right and if the velocity on x-axis was larger than one, direction for panel close was satisfied
				if(distX > 1 && Math.abs(velocityX) > 1) {
					closeDirSat = true;
					openDirSat = false;
				}
				//if the movement on x-axis was more than one pixels to left, and if the velocity on x-axis was larger than one, direction for panel open was satisfied
				if(distX < 1 && Math.abs(velocityX) > 1 ) {
					openDirSat = true;
					closeDirSat = false;
				}
			}
			//the gesture was recognized as panel close, because both close position and direction was satisfied
			if(closePosSat == true && closeDirSat == true) {
				try {
					if(spanel == null) {
						spanel = (SliderPanel) findViewById(R.id.sliderPanel);
						spanel.setTransX(translationX);
					}
					if(iv == null) {
						iv = (SliderImage) findViewById(R.id.sliderBtn); 
						iv.setTransX(translationX);
					}
					spanel.deactivate();
					iv.deactivate();
					closeDirSat = false;
					closePosSat = false;
				}
				catch(Exception ex) {
					Log.e(TAG, "Error happened when trying to close the panel " +ex.toString());
				}				
			}
			//the gesture was recognized as panel open, because both open position and direction was satisfied
			else if(openPosSat == true && openDirSat == true) {
				try {
					if(spanel == null) {
						spanel = (SliderPanel) findViewById(R.id.sliderPanel);
						spanel.setTransX(translationX);
					}
					if(iv == null) {
						iv = (SliderImage) findViewById(R.id.sliderBtn);
						iv.setTransX(translationX);
					}
					spanel.activate();
					iv.activate();
					openDirSat = false;
					openPosSat = false;
				}
				catch(Exception ex) {
					Log.e(TAG,"Error happened when trying to open the panel " +ex.toString());
				}				
			}
			return true;
		}
	}
	
	/**
	 * This pop-up dialog is shown, when the user answers a question from the list, or when the user clicks on 3D-object and wants to send a personal message
	 */
	public static class PopUpFragment extends DialogFragment {
		@Override
		public void onDestroyView() {
			chosenMarker = 0;
			super.onDestroyView();
		}
		@Override
		public void onCancel(DialogInterface dialog) {
			chosenMarker = 0;
			super.onCancel(dialog);
		}
		@Override
		public void onDismiss(DialogInterface dialog) {
			chosenMarker = 0;
			super.onDismiss(dialog);
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View popUpView = inflater.inflate(R.layout.fragment_popup, null);
			RadioButton feedbackBtn = (RadioButton) popUpView.findViewById(R.id.puFeedback);
			RadioButton questionBtn = (RadioButton) popUpView.findViewById(R.id.puQuestion);
			//if user wants to answer a question, do not show feedback and question possibilities, else show all possibilities
			if(listItemAnswer == true) {
				feedbackBtn.setVisibility(View.INVISIBLE);
				questionBtn.setVisibility(View.INVISIBLE);
				listItemAnswer = false;
			}
			else {
				feedbackBtn.setVisibility(View.VISIBLE);
				questionBtn.setVisibility(View.VISIBLE);				
			}
			customTextEdit = (EditText) popUpView.findViewById(R.id.customTextEdit);
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
			customTextEdit.setFilters(new InputFilter[]{filter});
			builder.setView(popUpView);
			builder.setTitle(R.string.popUpTitle)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						chosenMarker = 0;
					}
				});
			return builder.create();
		}
	}
	
	/**
	 * This fragment is used to create list views within the tab views of the panel
	 */
	public static class TeacherVisionFragment extends ListFragment {
		int mNum;
		String[] feedbackTexts = null;
		String[] questionTexts = null;
		String[] answerTexts = null;
		static TeacherVisionFragment newInstance(int num) {
			TeacherVisionFragment tvf = new TeacherVisionFragment();
			Bundle args = new Bundle();
			args.putInt("num",num);
			tvf.setArguments(args);
			return tvf;
		}
		//this method is used to update the list, when new message has arrived
		public void update() {
			int fsize = feedbacksList.size();
			int qsize = questionsList.size();
			int asize = answersList.size();
			String[] nFeedbackTexts = new String[fsize];
			String[] nQuestionTexts = new String[qsize];
			String[] nAnswerTexts = new String[asize];
			int fc = 0;
			int qc = 0;
			int ac = 0;
			Iterator<Feedback> iter = feedbacksList.iterator();
			while(iter.hasNext()) {
				Feedback storedFeedback = iter.next();
				if(storedFeedback.getAnonymous() == 1)
					nFeedbackTexts[fc] = "anonymous: " + storedFeedback.getMessage();
				else
					nFeedbackTexts[fc] = storedFeedback.getUsername() + ": " + storedFeedback.getMessage();
				fc++;
			}
			Iterator<Question> iter2 = questionsList.iterator();
			while(iter2.hasNext()) {
				Question storedQuestion = iter2.next();
				if(storedQuestion.getAnonymous() == 1)
					nQuestionTexts[qc] = "anonymous: " + storedQuestion.getContent();
				else
					nQuestionTexts[qc] = storedQuestion.getUsername() + ": " + storedQuestion.getContent();
				qc++;
			}
			Iterator<Answer> iter3 = answersList.iterator();
			while(iter3.hasNext()) {
				Answer storedAnswer = iter3.next();
				if(storedAnswer.getQuestionContent() == null)
					storedAnswer.setQuestionContent("");
				if(storedAnswer.getAnonymous() == 1)
					nAnswerTexts[ac] = "anonymous: " + storedAnswer.getContent() + " to question " + storedAnswer.getQuestionContent();
				else
					nAnswerTexts[ac] = storedAnswer.getUsername() + ": " + storedAnswer.getContent() + " to question " + storedAnswer.getQuestionContent();
				ac++;
			}
			mFeedbackAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,nFeedbackTexts);
			mQuestionAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,nQuestionTexts);
			mAnswerAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,nAnswerTexts);	
			if(mNum == 0) {
				setListAdapter(mFeedbackAdapter);
				mFeedbackAdapter.notifyDataSetChanged();
			}
			else if(mNum == 1) {
				setListAdapter(mQuestionAdapter);
				mQuestionAdapter.notifyDataSetChanged();
			}
			else if(mNum == 2) {
				setListAdapter(mAnswerAdapter);
				mAnswerAdapter.notifyDataSetChanged();
			}
		}
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			int fsize = feedbacksList.size();
			int qsize = questionsList.size();
			int asize = answersList.size();
			feedbackTexts = new String[fsize];
			questionTexts = new String[qsize];
			answerTexts = new String[asize];
			int fc = 0;
			int qc = 0;
			int ac = 0;
			Iterator<Feedback> iter = feedbacksList.iterator();
			while(iter.hasNext()) {
				Feedback storedFeedback = iter.next();
				if(storedFeedback.getAnonymous() == 1)
					feedbackTexts[fc] = "anonymous: " + storedFeedback.getMessage();
				else
					feedbackTexts[fc] = storedFeedback.getUsername() + ": " + storedFeedback.getMessage();
				fc++;
			}
			Iterator<Question> iter2 = questionsList.iterator();
			while(iter2.hasNext()) {
				Question storedQuestion = iter2.next();
				if(storedQuestion.getAnonymous() == 1)
					questionTexts[qc] = "anonymous: " + storedQuestion.getContent();
				else
					questionTexts[qc] = storedQuestion.getUsername() + ": " + storedQuestion.getContent();
				qc++;
			}
			Iterator<Answer> iter3 = answersList.iterator();
			
			while(iter3.hasNext()) {
				Answer storedAnswer = iter3.next();
				if(storedAnswer.getQuestionContent() == null)
					storedAnswer.setQuestionContent("");
				if(storedAnswer.getAnonymous() == 1)
					answerTexts[ac] = "anonymous: " + storedAnswer.getContent() + " to question " + storedAnswer.getQuestionContent();
				else
					answerTexts[ac] = storedAnswer.getUsername() + ": " + storedAnswer.getContent() + " to question " + storedAnswer.getQuestionContent();
				ac++;
			}
			mFeedbackAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,feedbackTexts);
			mQuestionAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,questionTexts);
			mAnswerAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,answerTexts);
			mNum = getArguments() != null ? getArguments().getInt("num") : 1;
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_teacher_vision, container, false);
			LinearLayout bottomSide = (LinearLayout)view.findViewById(R.id.tv_bottomside);
			TextView tv1 = new TextView(bottomSide.getContext());
			tv1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			tv1.setTextColor(Color.parseColor("#FFFFFF"));
			tv1.setTextSize(12);
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
			if(mNum == 0) {
				tv1.setText(R.string.feedbackAll);
				feedbackAllEdit = new EditText(bottomSide.getContext());
				feedbackAllEdit.setTextColor(Color.parseColor("#FFFFFF")); //if does not work, try: getResources().getColor(android.R.color.white);
				feedbackAllEdit.setHint(getResources().getString(R.string.all_hint));
				feedbackAllEdit.setFilters(new InputFilter[]{filter});
				Button btn = new Button(bottomSide.getContext());
				btn.setText(R.string.send);
				btn.setTextColor(Color.parseColor("#FFFFFF"));
				btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				btn.setId(250);
				//btn.setOnClickListener(sendFeedbackListener);
				btn.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		            	//check first
		    			if(!feedbackAllEdit.getText().toString().trim().equals("") && feedbackAllEdit.getText().toString().length() 
		    					>= MIN_LENGTH && feedbackAllEdit.getText().toString().length() <= MAX_LENGTH) {
		    				feedbackAllContent = feedbackAllEdit.getText().toString();
		    			}
		    			if(!onlineStudentsList.isEmpty())
		    				new PostFeedbackToAllTask(getActivity().getApplicationContext(), onlineStudentsList.get(0).getUserId()).execute();
		            }
		        });
				bottomSide.addView(btn);
				bottomSide.addView(feedbackAllEdit);
				bottomSide.addView(tv1);
			}
			else if(mNum == 1) {
				tv1.setText(R.string.questionAll);
				questionAllEdit = new EditText(bottomSide.getContext());
				questionAllEdit.setTextColor(Color.WHITE);
				questionAllEdit.setHint(getResources().getString(R.string.all_hint));
				questionAllEdit.setFilters(new InputFilter[]{filter});
				Button btn = new Button(bottomSide.getContext());
				btn.setText(R.string.send);
				btn.setTextColor(Color.WHITE);
				btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				btn.setId(251);
				btn.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		            	//check first
		    			if(!questionAllEdit.getText().toString().trim().equals("") && questionAllEdit.getText().toString().length() 
		    					>= MIN_LENGTH && questionAllEdit.getText().toString().length() <= MAX_LENGTH) {
		    				questionAllContent = questionAllEdit.getText().toString();
		    			}
		    			if(!onlineStudentsList.isEmpty())
		    				new PostQuestionToAllTask(getActivity().getApplicationContext(), onlineStudentsList.get(0).getUserId()).execute();
		            }
		        });
				bottomSide.addView(btn);
				bottomSide.addView(questionAllEdit);
				bottomSide.addView(tv1);
			}
			//maybe later add answerAll in here
			return view;
		}
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			if(mNum == 0)
				setListAdapter(mFeedbackAdapter);
			else if(mNum == 1) {
				setListAdapter(mQuestionAdapter);
			}
			else if(mNum == 2)
				setListAdapter(mAnswerAdapter);
		}
		@Override
		public void onListItemClick(ListView lw, View v, int position, long id) {
			Log.i(TAG,"Item num: " +id +" clicked");
			if(mNum == 0) {
				//do nothing
			}
			else if(mNum == 1) {
				Question question = questionsList.get(position); 
				int userId = question.getUserId();
				listItemQuid = question.getQuestionId();
				questionContent = question.getContent();
				int markerNum = 0;
				Iterator<Student> iter = onlineStudentsList.iterator();
				while(iter.hasNext()) {
					Student student = iter.next();
					if(student.getUserId() == userId) {
						markerNum = student.getMarkerNumber();
					}
				}
				if(markerNum != 0) {
					listItemAnswer = true;
					chosenMarker = markerNum;
					if(puFragment == null) {
						puFragment = new PopUpFragment();
						if(puFragment.isAdded() == false)
							puFragment.show(getFragmentManager(), "popUp"); 
					}
					else {
						if(puFragment.isVisible() == false)
							puFragment.show(getFragmentManager(), "popUp");
					}
				}
			}
			else if(mNum == 3) {
				//do nothing
			}
		}
	}

	/**
	 * This fragment is used for preferences. At this creation phase, the preferences are populated and summaries are set
	 */
	public static class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			courseNamePref = (EditTextPreference)findPreference("courseNamePref");
			lectureTopicPref = (EditTextPreference)findPreference("lectureTopicPref");
			beepPref = (CheckBoxPreference)findPreference("beep");
			notificationPref = (CheckBoxPreference)findPreference("notification");
			alertThresholdPref = (ListPreference)findPreference("alertThresholdPref");
			timespanPref = (ListPreference)findPreference("timespanPref");
			flaretimePref = (ListPreference)findPreference("flaretimePref");
			speedUpPref = (ListPreference)findPreference("speedUpPref");
			slowDownPref = (ListPreference)findPreference("slowDownPref");
			moreExamplesPref = (ListPreference)findPreference("moreExamplesPref");
			repeatPref = (ListPreference)findPreference("repeatPref");
			questionPref = (ListPreference)findPreference("questionPref");
			answerPref = (ListPreference)findPreference("answerPref");
			if(teacher.getCourseNameDefaulted() == 1 && teacher.getCourseName() != null) {
				courseNamePref.setSummary("Value is "+teacher.getCourseName());
			}
			else
				courseNamePref.setSummary("Add course name here");
			if(teacher.getLectureTopicDefaulted() == 1 && teacher.getLectureTopic() != null) {
				lectureTopicPref.setSummary("Value is "+teacher.getLectureTopic());
			}
			else
				lectureTopicPref.setSummary("Add lecture topic here");
			if(beepPref.isChecked())
				beepPref.setSummary(beepPref.getSummaryOn());
			else if(beepPref.isChecked() == false)
				beepPref.setSummary(beepPref.getSummaryOff());
			if(notificationPref.isChecked())
				notificationPref.setSummary(notificationPref.getSummaryOn());
			else if(notificationPref.isChecked() == false)
				notificationPref.setSummary(notificationPref.getSummaryOff());
			alertThresholdPref.setSummary("Value is "+alertThresholdPref.getEntry().toString());
			timespanPref.setSummary("Value is "+timespanPref.getEntry().toString());
			flaretimePref.setSummary("Value is "+flaretimePref.getEntry().toString());
			speedUpPref.setSummary("Value is "+speedUpPref.getEntry().toString());
			slowDownPref.setSummary("Value is "+slowDownPref.getEntry().toString());
			moreExamplesPref.setSummary("Value is "+moreExamplesPref.getEntry().toString());
			repeatPref.setSummary("Value is "+repeatPref.getEntry().toString());
			questionPref.setSummary("Value is "+questionPref.getEntry().toString());
			answerPref.setSummary("Value is "+answerPref.getEntry().toString());
		}
		/*Idea of changing preferences background color: http://stackoverflow.com/questions/16970209/preferencefragment-background-color*/
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
			return view;
		}
	}
	
	/**
	 * This tab adapter is used to handle events happening in tab views
	 */
	public class TabAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
		public TabAdapter(FragmentManager fm, ViewPager vp) {
			super(fm);
			vp.setOnPageChangeListener(this);
		}
		@Override
		public void onPageScrollStateChanged(int state) {
			//do nothing
		}
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			//do nothing
		}
		@Override
		public void onPageSelected(int position) {
			if(position == 0) {
				button.setEnabled(false);
				button2.setEnabled(true);
				button3.setEnabled(true);
			}
			else if(position == 1) {
				button.setEnabled(true);
				button2.setEnabled(false);
				button3.setEnabled(true);
			}
			else if(position == 2) {
				button.setEnabled(true);
				button2.setEnabled(true);
				button3.setEnabled(false);
			}
		}
		@Override
		public Fragment getItem(int position) {
			return TeacherVisionFragment.newInstance(position);
		}
		@Override
		public int getCount() {
			return NUM_ITEMS;
		}
	}

	/* ServiceConnection, implemented here as an anonymous class. This onServiceConnected method
	 * is called when we call bindService and onServiceDisconnected is called when we call
	 * unbindService. We fetch the service to the mService variable with this binder mechanism and use it to get connection
	 * to the tvCon object (of the TeacherVisionConnection class), which we set TeacherVisionActivity to listen.  */
	/*private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			TeacherVisionServiceBinder readerBinder = (TeacherVisionServiceBinder)service;
			mService = readerBinder.getService();
			tvCon = mService.getConnection(getApplicationContext());
			tvCon.setListener(TeacherVisionActivity.this);
			listenerCreated = true;
			bound = true;
			Log.i(TAG, "Now we are bound");
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};*/
	
    /* Here we receive the questionReceived signal */
    /*public void onQuestionReceived(int questionId) {
    	runOnUiThread(updateQuestionRunnable);
    }*/
    
	/* Update question received to the UI. We filtered this value already
	 * at TeacherVisionConnection receiving loop  */
	/*public Runnable updateQuestionRunnable = new Runnable() {
		public void run() {	
			//use locks for long lasting UI operations
			//edt.setText(Integer.toString(freqNow));
			Log.i(TAG, "At updateQuestionRunnable");
		}
	};*/	
	
    /* Here we receive the feedbackReceived signal */
    /*public void onFeedbackReceived(int feedbackId) {
    	runOnUiThread(updateFeedbackRunnable);
    }*/
    
	/* Update feedback received to the UI. We filtered this value already
	 * at TeacherVisionConnection receiving loop  */
	/*public Runnable updateFeedbackRunnable = new Runnable() {
		public void run() {	
			//use locks for long lasting UI operations
			//edt.setText(Integer.toString(freqNow));
			Log.e(TAG, "At updateFeedbackRunnable");
		}
	};*/
	
    /* Here we receive the answerReceived signal */
    /*public void onAnswerReceived(int answerId) {
    	runOnUiThread(updateAnswerRunnable);
    }*/
    
	/* Update answer received to the UI. We filtered this value already
	 * at TeacherVisionConnection receiving loop  */
	/*public Runnable updateAnswerRunnable = new Runnable() {
		public void run() {	
			//use locks for long lasting UI operations
			//edt.setText(Integer.toString(freqNow));
			Log.i(TAG, "At updateAnswerRunnable");
		}
	};*/
	
    /* Here we receive the registerReceived signal */
    /*public void onRegisterReceived(int registerId) {
    	runOnUiThread(updateRegisterRunnable);
    }*/
    
	/* Update register received to the UI. We filtered this value already
	 * at TeacherVisionConnection receiving loop  */
	/*public Runnable updateRegisterRunnable = new Runnable() {
		public void run() {	
			//use locks for long lasting UI operations
			//edt.setText(Integer.toString(freqNow));
			button.setText("UID");
			Log.i(TAG, "At updateRegisterRunnable");
		}
	};	*/	
	
	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task posts answer to the student
	 *  */		
	private class PostAnswerToStudentTask extends AsyncTask<String, Integer, Double> {
		protected int creationCode = 0;
		protected int mUid;
		private Context mContext;
		public PostAnswerToStudentTask(Context context, int userId) {
			this.mContext = context;
			this.mUid = userId;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("content", message); 
						jsonObj.put("user_id", teacher.getUserId());
						jsonObj.put("username", teacher.getUserName());
						jsonObj.put("question_msg", questionContent);
						jsonObj.put("delegated_to", mUid);
						if(listItemAnswer == true) {
							jsonObj.put("question_id", listItemQuid);
							creationCode = 3;
						}
						else
							jsonObj.put("question_id", 0); 
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/answers");
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
						Log.i(TAG, "ANSWER CREATION SUCCEEDED");
						creationCode = 1;
						if(creationCode == 3)
							creationCode = 6;
					}
					else {
						Log.e(TAG, "ANSWER CREATION FAILED "+httpCon.getResponseCode());
						creationCode = 2;
						if(creationCode == 3)
							creationCode = 5;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST ANSWER "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST ANSWER "+e);
				}				
			}
			else
				Log.e(TAG, "NETWORK CONNECTION ERROR");
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			//progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(creationCode == 1 || creationCode == 6) {
				Toast.makeText(mContext, "Answer sent succesfully to the student", Toast.LENGTH_SHORT).show();
				if(creationCode == 6)
					listItemAnswer = false;
			}
			else {
				Log.e(TAG, "ANSWER POST FAILED");
			}
		}
	}	

	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task posts question to the student
	 *  */		
	private class PostQuestionToStudentTask extends AsyncTask<String, Integer, Double> {
		protected int creationCode = 0;
		protected int mUid;
		private Context mContext;
		public PostQuestionToStudentTask(Context context, int userId) {
			mContext = context;
			this.mUid = userId;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("content", message); 
						jsonObj.put("user_id", teacher.getUserId());
						jsonObj.put("username", teacher.getUserName());
						jsonObj.put("delegated_to", mUid);
						jsonObj.put("answered", 0);
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception: "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/questions");
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
						Log.i(TAG, "QUESTION CREATION SUCCEEDED");
						creationCode = 1;
					}
					else {
						Log.e(TAG, "QUESTION CREATION FAILED "+httpCon.getResponseCode());
						creationCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST QUESTION "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST QUESTION "+e);
				}				
			}
			else
				Log.e(TAG,"NETWORK CONNECTION ERROR");
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			//progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(creationCode == 1) {
				Question ownQuestion = new Question();
				ownQuestion.setContent(message);
				ownQuestion.setUserId(teacher.getUserId());
				ownQuestion.setUsername(teacher.getUserName());
				ownQuestion.setDelegatedTo(mUid);
				//ownQuestion.setQuestionId(questionId);
				ownQuestionsList.add(ownQuestion);
				Toast.makeText(mContext, "Question sent succesfully to the student", Toast.LENGTH_SHORT).show();
			}
			else {
				Log.e(TAG, "QUESTION POST FAILED");
			}
		}
	}
	
	/**
	 * This task is used to send one or more HTTP POST requests to the server.
	 * The task posts question to each student found from the onlineStudentsList
	 *  */		
	private static class PostQuestionToAllTask extends AsyncTask<String, Integer, Double> {
		protected int creationCode = 0;
		protected int mUid;
		private Context mContext;
		Iterator<Student> iter = onlineStudentsList.iterator();
		public PostQuestionToAllTask(Context context, int userId) {
			mContext = context;
			this.mUid = userId;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			//OutputStream os = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("content", questionAllContent); 
						jsonObj.put("user_id", teacher.getUserId());
						jsonObj.put("username", teacher.getUserName());
						jsonObj.put("delegated_to", mUid);
						jsonObj.put("answered", 0);
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/questions");
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
						Log.i(TAG, "QUESTION CREATION SUCCEEDED");
						creationCode = 1;
					}
					else {
						Log.e(TAG, "QUESTION CREATION FAILED "+httpCon.getResponseCode());
						creationCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST QUESTION "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST QUESTION "+e);
				}				
			}
			else
				Log.e(TAG, "NETWORK CONNECTION ERROR");
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			//progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(creationCode == 1) {
				Log.i(TAG, "QUESTION POST SUCCEEDED");
				Toast.makeText(mContext, "Question sent succesfully to the students", Toast.LENGTH_SHORT).show();
				Question ownQuestion = new Question();
				ownQuestion.setContent(questionAllContent);
				ownQuestion.setUserId(teacher.getUserId());
				ownQuestion.setUsername(teacher.getUserName());
				ownQuestion.setDelegatedTo(mUid);
				//ownQuestion.setQuestionId(questionId);
				ownQuestionsList.add(ownQuestion);
				//send same feedback to next user in the onlineStudentsList
				if(iter.hasNext()) {
					Student onlineStudent = iter.next();
					if(onlineStudent != onlineStudentsList.get(0)) {
						int uid = onlineStudent.getUserId();
						new PostQuestionToAllTask(mContext, uid).execute();
					}
				}
				else {
					//do something
				}
			}
			else {
				Log.e(TAG, "QUESTION POST FAILED");
			}
		}
	}

	/**
	 * This task is used to send one or more HTTP POST requests to the server.
	 * The task posts feedback to each student found from the onlineStudentsList
	 *  */		
	private static class PostFeedbackToAllTask extends AsyncTask<String, Integer, Double> {
		protected int creationCode = 0;
		Iterator<Student> iter = onlineStudentsList.iterator();
		protected int mUid;
		Context mContext;
		public PostFeedbackToAllTask(Context context, int uid) {
			this.mContext = context;
			this.mUid = uid;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("message", feedbackAllContent); 
						jsonObj.put("type", 2); //type 1 means course feedback, type 2 lecture feedback
						jsonObj.put("sender_id", teacher.getUserId());
						jsonObj.put("username", teacher.getUserName());
						jsonObj.put("immediate", 0); //0 is for non-immediate feedback, 5 for "quiet down", 6 for "focus"
						jsonObj.put("delegated_to", mUid);
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/feedbacks");
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
						Log.i(TAG, "FEEDBACK CREATION SUCCEEDED");
						creationCode = 1;
					}
					else {
						Log.e(TAG, "FEEDBACK CREATION FAILED "+httpCon.getResponseCode());
						creationCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST FEEDBACK "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST FEEDBACK "+e);
				}				
			}
			else
				Log.e(TAG, "NETWORK CONNECTION ERROR");
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			//progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(creationCode == 1) {
				Log.i(TAG, "FEEDBACK POST SUCCEEDED");
				Toast.makeText(mContext, "Feedback sent succesfully to the students", Toast.LENGTH_SHORT).show();
				//send same feedback to next user in the onlineStudentsList
				if(iter.hasNext()) {
					Student onlineStudent = iter.next();
					if(onlineStudent != onlineStudentsList.get(0)) {
						int uid = onlineStudent.getUserId();
						new PostFeedbackToAllTask(mContext, uid).execute();
					}
				}
				else {
					//do something
				}
			}
			else {
				Log.e(TAG, "FEEDBACK POST FAILED");
			}
		}
	}	
	
	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task posts feedback to the student
	 *  */		
	private class PostFeedbackToStudentTask extends AsyncTask<String, Integer, Double> {
		protected int creationCode = 0;
		protected int mUid;
		private Context mContext;
		public PostFeedbackToStudentTask(Context context, int userId) {
			this.mContext = context;
			this.mUid = userId;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("message", message); 
						jsonObj.put("type", 2); //type 1 means course feedback, type 2 lecture feedback
						jsonObj.put("sender_id", teacher.getUserId());
						jsonObj.put("username", teacher.getUserName());
						jsonObj.put("immediate", 0); //0 is for non-immediate feedback, 5 for "quiet down", 6 for "focus"
						jsonObj.put("delegated_to", mUid);
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/feedbacks");
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
						Log.i(TAG, "FEEDBACK CREATION SUCCEEDED");
						creationCode = 1;
					}
					else {
						Log.e(TAG, "FEEDBACK CREATION FAILED "+httpCon.getResponseCode());
						creationCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST FEEDBACK "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST FEEDBACK "+e);
				}				
			}
			else
				Log.e(TAG, "NETWORK CONNECTION ERROR");
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			//progressBar.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Double result) {
			if(creationCode == 1) {
				Toast.makeText(mContext, "Feedback sent succesfully to the student", Toast.LENGTH_SHORT).show();
			}
			else {
				Log.e(TAG, "FEEDBACK POST FAILED");
			}
		}
	}
	
	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task puts the teacher offline
	 *  */		
	private class ExitUserTask extends AsyncTask<String, Integer, Double> {
		protected int exitUserCode = 0;
		private Context mContext;
		public ExitUserTask(Context context) {
			mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("password", teacher.getTeacherPwd());
						jsonObj.put("name", teacher.getTeacherName());
						jsonObj.put("email", "something@something.com");
						jsonObj.put("number", "123");
						jsonObj.put("online", 0);
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
						exitUserCode = 1;
					}
					else {
						Log.e(TAG, "USER EDIT FAILED "+httpCon.getResponseCode());
						exitUserCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT USER EDIT "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT USER EDIT "+e);
				}				
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");	
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(exitUserCode == 1) {
				//handle ok
			}
			else {
				//handle failure
			}
		}
	}

	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task puts the course offline
	 *  */		
	private class PutCourseOfflineTask extends AsyncTask<String, Integer, Double> {
		protected int courseOfflineCode = 0;
		private Context mContext;
		public PutCourseOfflineTask(Context context) {
			mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("online", 0);
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
						Log.i(TAG, "COURSE OFFLINE SUCCEEDED");
						courseOfflineCode = 1;
					}
					else {
						Log.e(TAG, "COURSE OFFLINE FAILED "+httpCon.getResponseCode());
						courseOfflineCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT COURSE OFFLINE"+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT COURSE OFFLINE "+e);
				}				
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");	
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(courseOfflineCode == 1) {
				//handle ok
			}
			else {
				//handle failure
			}
		}
	}

	/**
	 * This task is used to send HTTP GET requests to the server.
	 * The task polls answers, sent by students, by calling itself all over again as long as allowed to
	 *  */	
	private class PollAnswersTask extends AsyncTask<String, Integer, Double> {
		protected int getAnswersCode = 0;
		String results = "";
		private Context mContext;
		public PollAnswersTask(Context context) {
			mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected(mContext)) {
				try {
					URL urli = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/answers");
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
						Log.i(TAG, "ANSWERS GET SUCCEEDED");
						getAnswersCode = 1;
					}
					else {
						Log.e(TAG, "ANSWERS GET FAILED");
						getAnswersCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET ANSWERS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET ANSWERS "+e);
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at poll answers "+e);
						}
					}
				}
				try {
					Thread.sleep(POLLSPAN);
				}
				catch(InterruptedException e) {
					return null;
				}
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(getAnswersCode == 1) {
				//store all answers into the list
				String [] answersArr = null;
				if(results.contains("}")) {
					answersArr = results.split("[}]");
				}
				if(answersArr != null) {
					for(int i=0; i < answersArr.length; i++) {
						if(answersArr[i].contains("content")) {
							String contentStr = "";
							String username = "";
							int userId = 0;
							int delegatedTo = 0;
							String questionContent = "";
							int anonymous = 0;
							String [] contentParts = answersArr[i].split("content");
							if(contentParts.length > 1) {	
								String [] contentStopParts = contentParts[1].split(",");
								int contentStop = (contentStopParts[0].length());
								contentStr = contentParts[1].substring(4, contentStop-1);
							}
							String [] userIdParts = answersArr[i].split("user_id");
							if(userIdParts.length > 1) {
								String [] uidStopParts = userIdParts[1].split(",");
								int uidStop = (uidStopParts[0].length());
								String uidText = userIdParts[1].substring(3, uidStop);
								if(!uidText.equals("null")) {
									userId = Integer.parseInt(uidText);
								}
							}
							String [] delegatedToParts = answersArr[i].split("delegated_to");
							if(delegatedToParts.length > 1) {
								String [] delegatedStopParts = delegatedToParts[1].split(",");
								int delegatedStop = (delegatedStopParts[0].length());
								String delegatedText = delegatedToParts[1].substring(3, delegatedStop);
								if(!delegatedText.equals("null")) {
									delegatedTo = Integer.parseInt(delegatedText);
								}
							}
							String [] questionContentParts = answersArr[i].split("question_msg");
							if(questionContentParts.length > 1) {	
								String [] questionContentStopParts = questionContentParts[1].split(",");
								int questionContentStop = (questionContentStopParts[0].length());
								questionContent = questionContentParts[1].substring(4, questionContentStop-1);
							}
							String [] anonymousParts = answersArr[i].split("anonymous");
							if(anonymousParts.length > 1) {
								String [] anStopParts = anonymousParts[1].split(",");
								int anStop = (anStopParts[0].length());
								String anText = anonymousParts[1].substring(3, anStop);
								if(!anText.equals("null")) {
									anonymous = Integer.parseInt(anText);
								}
							}
							String [] usernameParts = answersArr[i].split("username");
							if(usernameParts.length > 1) {	
								String [] usernameStopParts = usernameParts[1].split(",");
								int usernameStop = (usernameStopParts[0].length());
								username = usernameParts[1].substring(4, usernameStop-1);
							}
							//iterate through the onlineStudentsList that this is student's answer, not teacher's
							//at the same time store marker num of sender
							Iterator<Student> iter = onlineStudentsList.iterator();
							boolean studentFound = false;
							int sendersMarkerNum = 0;
							while(iter.hasNext()) {
								Student storedStudent = iter.next();
								if(storedStudent.getUserId() == userId) {
									studentFound = true;
									sendersMarkerNum = storedStudent.getMarkerNumber();
								}
							}						
							//iterate through the answersList that this answer does not already exist there
							Iterator<Answer> iter2 = answersList.iterator();
							boolean answerFound = false;
							while(iter2.hasNext()) {
								Answer storedAnswer = iter2.next();
								if(storedAnswer.getContent().equals(contentStr))
									answerFound = true;
							}
							//is if delegatedTo == teacher.getUserId() needed at all, because these are the course and lecture of THIS user
							if(answerFound == false && studentFound == true) {
								//only new answers make it to the list and toast
								Answer answer = new Answer();
								answer.setContent(contentStr);
								answer.setUserId(userId);
								answer.setDelegatedTo(delegatedTo);
								//answer.setQuestionId(questionId);
								answer.setUsername(username);
								answer.setAnonymous(anonymous);
								Iterator<Question> iter4 = ownQuestionsList.iterator();
								while(iter4.hasNext()) {
									Question question = iter4.next();
									//if(teacher.getUserId() == delegatedTo && questionContent.equals(question.getContent()))
									//answer is most likely already dedicated to this user because we are here
									if(questionContent.equals(question.getContent()))
										answer.setQuestionContent(question.getContent());
								}
								answersList.add(answer);
								if(sendersMarkerNum != 0) {
									if(sendersMarkerNum == 1)
										oneObject.setIndicator(6);
									else if(sendersMarkerNum == 2)
										twoObject.setIndicator(6);
									else if(sendersMarkerNum == 3)
										threeObject.setIndicator(6);
								}
								if(anonymous == 1)
									Toast.makeText(getApplicationContext(), "Answer "+contentStr+" received from the user: anonymous", Toast.LENGTH_SHORT).show();
								else
									Toast.makeText(getApplicationContext(), "Answer "+contentStr+" received from the user: "+username, Toast.LENGTH_SHORT).show();
								if(beepOn == true) {
									/* http://stackoverflow.com/questions/4441334/how-to-play-an-android-notification-sound */
									try {
										Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
										Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
										r.play();
									}
									catch(Exception e) {
										Log.e(TAG, "EXCEPTION AT PLAY SOUND WHEN RECEIVING ANSWER");
									}
								}
								/* Idea of updating list fragment within viewpager: http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager */
								TeacherVisionFragment fragment = (TeacherVisionFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":2");
								if(fragment != null) {
									fragment.update();
								}
							}
						}
					}
				}
				if(pollAnswersCancelled == false) {
					pollAnswersTask = new PollAnswersTask(getApplicationContext());
					pollAnswersTask.execute();
				}
			}
		}
	}	

	/**
	 * This task is used to send HTTP GET requests to the server.
	 * The task polls questions, sent by students, by calling itself all over again as long as allowed to
	 *  */	
	private class PollQuestionsTask extends AsyncTask<String, Integer, Double> {
		protected int getQuestionsCode = 0;
		String results = "";
		private Context mContext;
		public PollQuestionsTask(Context context) {
			mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected(mContext)) {
				try {
					URL urli = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/questions");
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
						Log.i(TAG, "QUESTIONS GET SUCCEEDED");
						getQuestionsCode = 1;
					}
					else {
						Log.e(TAG, "QUESTIONS GET FAILED");
						getQuestionsCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET USERS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET USERS "+e);
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at poll questions "+e);
						}
					}
				}
				try {
					Thread.sleep(POLLSPAN);
				}
				catch(InterruptedException e) {
					return null;
				}
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(getQuestionsCode == 1) {
				//store all questions into the list
				String [] questionsArr = null;
				if(results.contains("}")) {
					questionsArr = results.split("[}]");
				}
				if(questionsArr != null) {
					for(int i=0; i < questionsArr.length; i++) {
						if(questionsArr[i].contains("content")) {
							String contentStr = "";
							String username = "";
							int userId = 0;
							int delegatedTo = 0;
							int answered = 0;
							int anonymous = 0;
							int questionId = 0;
							String [] contentParts = questionsArr[i].split("content");
							if(contentParts.length > 1) {	
								String [] contentStopParts = contentParts[1].split(",");
								int contentStop = (contentStopParts[0].length());
								contentStr = contentParts[1].substring(4, contentStop-1);
							}
							String [] userIdParts = questionsArr[i].split("user_id");
							if(userIdParts.length > 1) {
								String [] uidStopParts = userIdParts[1].split(",");
								int uidStop = (uidStopParts[0].length());
								String uidText = userIdParts[1].substring(3, uidStop);
								if(!uidText.equals("null")) {
									userId = Integer.parseInt(uidText);
								}
							}
							String [] questionIdParts = questionsArr[i].split("question_id");
							if(questionIdParts.length > 1) {
								String [] quidStopParts = questionIdParts[1].split(",");
								int quidStop = (quidStopParts[0].length());
								String quidText = questionIdParts[1].substring(3, quidStop);
								if(!quidText.equals("null")) {
									questionId = Integer.parseInt(quidText);
								}
							}
							String [] delegatedToParts = questionsArr[i].split("delegated_to");
							if(delegatedToParts.length > 1) {
								String [] delegatedStopParts = delegatedToParts[1].split(",");
								int delegatedStop = (delegatedStopParts[0].length());
								String delegatedText = delegatedToParts[1].substring(3, delegatedStop);
								if(!delegatedText.equals("null")) {
									delegatedTo = Integer.parseInt(delegatedText);
								}
							}
							String [] answeredParts = questionsArr[i].split("answered");
							if(answeredParts.length > 1) {
								String ansText = answeredParts[1].substring(3, 4);
								if(!ansText.equals("null")) {
									answered = Integer.parseInt(ansText);
								}
							}
							String [] anonymousParts = questionsArr[i].split("anonymous");
							if(anonymousParts.length > 1) {
								String [] anStopParts = anonymousParts[1].split(",");
								int anStop = (anStopParts[0].length());
								String anText = anonymousParts[1].substring(3, anStop);
								if(!anText.equals("null")) {
									anonymous = Integer.parseInt(anText);
								}
							}
							String [] usernameParts = questionsArr[i].split("username");
							if(usernameParts.length > 1) {	
								String [] usernameStopParts = usernameParts[1].split(",");
								int usernameStop = (usernameStopParts[0].length());
								username = usernameParts[1].substring(4, usernameStop-1);
							}
							
							//iterate through onlineStudentsList and find this specific student who sent answer
							Iterator<Student> iter2 = onlineStudentsList.iterator();
							int sendersMarkerNum = 0;
							boolean studentFound = false;
							while(iter2.hasNext()) {
								Student storedStudent = iter2.next();
								if(storedStudent.getUserId() == userId) {
									studentFound = true;
									sendersMarkerNum = storedStudent.getMarkerNumber();
								}
							}
							//iterate through the questionsList that this question does not already exist there
							Iterator<Question> iter = questionsList.iterator();
							boolean questionFound = false;
							while(iter.hasNext()) {
								Question storedQuestion = iter.next();
								if(storedQuestion.getContent().equals(contentStr))
									questionFound = true;
							}
							if(questionFound == false && studentFound == true) {
								//only new questions make it to the list and toast
								Question question = new Question();
								question.setContent(contentStr);
								question.setUserId(userId);
								question.setDelegatedTo(delegatedTo);
								question.setAnswered(answered);
								question.setUsername(username);
								question.setAnonymous(anonymous);
								question.setQuestionId(questionId);
								questionsList.add(question);
								if(sendersMarkerNum != 0) {
									if(sendersMarkerNum == 1)
										oneObject.setIndicator(5);
									else if(sendersMarkerNum == 2)
										twoObject.setIndicator(5);
									else if(sendersMarkerNum == 3)
										threeObject.setIndicator(5);
								}
								if(anonymous == 1)
									Toast.makeText(getApplicationContext(), "Question "+contentStr+" received from the user: anonymous", Toast.LENGTH_SHORT).show();
								else
									Toast.makeText(getApplicationContext(), "Question "+contentStr+" received from the user: "+username, Toast.LENGTH_SHORT).show();
								if(beepOn == true) {
							    	/* http://stackoverflow.com/questions/4441334/how-to-play-an-android-notification-sound */
									try {
										Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
										Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
										r.play();
									}
									catch(Exception e) {
										Log.e(TAG, "EXCEPTION AT PLAY SOUND WHEN RECEIVING QUESTION");
									}
								}
								/* Idea of updating list fragment within viewpager: http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager */
								TeacherVisionFragment fragment = (TeacherVisionFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":1");
								if(fragment != null) {
									fragment.update();
								}
							}
						}
					}
				}
				if(pollQuestionsCancelled == false) {
					pollQuestionsTask = new PollQuestionsTask(getApplicationContext());
					pollQuestionsTask.execute();
				}
			}
		}
	}	

	/**
	 * This task is used to send HTTP GET requests to the server.
	 * The task polls feedbacks, sent by students, by calling itself all over again as long as allowed to
	 *  */		
	private class PollFeedbacksTask extends AsyncTask<String, Integer, Double> {
		protected int getFeedbacksCode = 0;
		String results = "";
		private Context mContext;
		public PollFeedbacksTask(Context context) {
			mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected(mContext)) {
				try {
					URL urli = new URL(coursesUrl+"/"+teacher.getCourseId()+"/lectures/"+teacher.getLectureId()+"/feedbacks");
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
						Log.i(TAG, "FEEDBACKS GET SUCCEEDED");
						getFeedbacksCode = 1;
					}
					else {
						Log.e(TAG, "FEEDBACKS GET FAILED");
						getFeedbacksCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET USERS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET USERS "+e);
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at poll feedbacks "+e);
						}
					}
				}
				try {
					Thread.sleep(POLLSPAN);
				}
				catch(InterruptedException e) {
					return null;
				}
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(getFeedbacksCode == 1) {
				//store all feedbacks into the list
				String [] feedbacksArr = null;
				if(results.contains("}")) {
					feedbacksArr = results.split("[}]");
				}
				if(feedbacksArr != null) {
					for(int i=0; i < feedbacksArr.length; i++) {
						if(feedbacksArr[i].contains("message")) {
							String messageStr = "";
							String username = "";
							//String datetimeStr = ""; 
							int senderId = 0;
							int feedbackId = 0;
							int immediate = 0;
							int anonymous = 0;
							String [] messageParts = feedbacksArr[i].split("message");
							if(messageParts.length > 1) {	
								String [] messageStopParts = messageParts[1].split(",");
								int messageStop = (messageStopParts[0].length());
								messageStr = messageParts[1].substring(4, messageStop-1);
							}
							String [] senderIdParts = feedbacksArr[i].split("sender_id");
							if(senderIdParts.length > 1) {
								String [] senderStopParts = senderIdParts[1].split(",");
								int senderStop = (senderStopParts[0].length());
								String senderText = senderIdParts[1].substring(3, senderStop);
								if(!senderText.equals("null")) {
									senderId = Integer.parseInt(senderText);
								}
							}
							String [] feedbackIdParts = feedbacksArr[i].split("feedback_id");
							if(feedbackIdParts.length > 1) {
								String [] fidStopParts = feedbackIdParts[1].split(",");
								int fidStop = (fidStopParts[0].length());
								String fidText = feedbackIdParts[1].substring(3, fidStop);
								if(!fidText.equals("null")) {
									feedbackId = Integer.parseInt(fidText);
								}
							}
							String [] immediateParts = feedbacksArr[i].split("immediate");
							if(immediateParts.length > 1) {
								String [] imStopParts = immediateParts[1].split(",");
								int imStop = (imStopParts[0].length());
								String imText = immediateParts[1].substring(3, imStop);
								if(!imText.equals("null")) {
									immediate = Integer.parseInt(imText);
								}
							}
							String [] anonymousParts = feedbacksArr[i].split("anonymous");
							if(anonymousParts.length > 1) {
								String [] anStopParts = anonymousParts[1].split(",");
								int anStop = (anStopParts[0].length());
								String anText = anonymousParts[1].substring(3, anStop);
								if(!anText.equals("null")) {
									anonymous = Integer.parseInt(anText);
								}
							}
							String [] usernameParts = feedbacksArr[i].split("username");
							if(usernameParts.length > 1) {	
								String [] usernameStopParts = usernameParts[1].split(",");
								int usernameStop = (usernameStopParts[0].length());
								username = usernameParts[1].substring(4, usernameStop-1);
							}
							/* alert threshold and alert timespan could be fetched like this
							Date date;
							String [] datetimeParts = feedbacksArr[i].split("datetime");
							if(datetimeParts.length > 1) {	
								String [] datetimeStopParts = datetimeParts[1].split(",");
								int datetimeStop = (datetimeStopParts[0].length());
								//should have the form "2014-07-23 08:30:50.940000"
								datetimeStr = datetimeParts[1].substring(4, datetimeStop-1);
								String[] dateParts = datetimeStr.split("-");
								int year = 0, month = 0, day = 0, hours = 0, minutes = 0, seconds = 0;
								if(dateParts.length > 1) {
									year = Integer.parseInt(dateParts[0]);
									month = Integer.parseInt(dateParts[1]);
									day = Integer.parseInt(dateParts[2]);
								}
								String[] timeParts = datetimeStr.split(":");
								if(timeParts.length > 1) {
									hours = Integer.parseInt(timeParts[0]);
									minutes = Integer.parseInt(timeParts[1]);
									seconds = Integer.parseInt(timeParts[2]);
								}
								//System.currentTimeMillis();
								String input = ""+hours+":"+minutes+":"+seconds+" "+day+" "+month+" "+year;
								try {
									date = new SimpleDateFormat("hh:mm:ss dd MM yyyy", Locale.ENGLISH).parse(input);		
								}
								catch(ParseException ex) {
									Log.e(TAG, "GET FEEDBACKS TV PARSE EXCEPTION "+ex);
								}
							}*/
							//iterate through onlineStudentsList and find this specific student who sent feedback
							Iterator<Student> iter2 = onlineStudentsList.iterator();
							int sendersMarkerNum = 0;
							boolean studentFound = false;
							//int userCounter = 0;
							while(iter2.hasNext()) {
								Student storedStudent = iter2.next();
								if(storedStudent.getUserId() == senderId) {
									studentFound = true;
									sendersMarkerNum = storedStudent.getMarkerNumber();
									//userCounter++;
								}
							}

							//iterate through the feedbacksList that this feedback does not already exist there
							Iterator<Feedback> iter = feedbacksList.iterator();
							//boolean showAccepted = false;
							boolean feedbackFound = false;
							//int [] senderIds;
							//int acceptCounter = 0;
							while(iter.hasNext()) {
								Feedback storedFeedback = iter.next();
								/*Date sfDate = storedFeedback.getDate();
								long diff = date.getTime() - sfDate.getTime();
								String sfMessage = storedFeedback.getMessage();
								if(sfMessage.equals(messageStr) && diff < timespan) {
									senderIds[acceptCounter] = storedFeedback.getFeedbackId();
									for(i = 0; i < senderIds.length; i++) {
										if(senderIds[i] != storedFeedback.getFeedbackId())
											acceptCounter++;
									}
								}*/
								if(storedFeedback.getFeedbackId() == feedbackId)
									feedbackFound = true;
							} 
							//if(acceptCounter >= Math.round(alertThreshold * userCounter))
							//	showAccepted = true;
							if(feedbackFound == false && studentFound == true) {
								//only new feedbacks make it to the list and toast
								Feedback feedback = new Feedback();
								//feedback.setDate(date);
								feedback.setMessage(messageStr);
								feedback.setSenderId(senderId);
								feedback.setImmediate(immediate);
								feedback.setUsername(username);
								feedback.setAnonymous(anonymous);
								feedback.setFeedbackId(feedbackId);
								feedbacksList.add(feedback);
								//if(showAccepted == true) {
								if(sendersMarkerNum != 0) {
									if(sendersMarkerNum == 1) {
										if(immediate == 1) //speed up
											oneObject.setIndicator(1);
										else if(immediate == 2) //more examples
											oneObject.setIndicator(2);
										else if(immediate == 3) //slow down
											oneObject.setIndicator(3);
										else if(immediate == 4) //repeat
											oneObject.setIndicator(4);
									}
									else if(sendersMarkerNum == 2) {
										if(immediate == 1) //speed up
											twoObject.setIndicator(1);
										else if(immediate == 2) //more examples
											twoObject.setIndicator(2);
										else if(immediate == 3) //slow down
											twoObject.setIndicator(3);
										else if(immediate == 4) //repeat
											twoObject.setIndicator(4);
									}
									else if(sendersMarkerNum == 3) {
										if(immediate == 1) //speed up
											threeObject.setIndicator(1);
										else if(immediate == 2) //more examples
											threeObject.setIndicator(2);
										else if(immediate == 3) //slow down
											threeObject.setIndicator(3);
										else if(immediate == 4) //repeat
											threeObject.setIndicator(4);
									}
								}
								//}
								if(anonymous == 1)
									Toast.makeText(getApplicationContext(), "Feedback "+messageStr+" received from the user: anonymous", Toast.LENGTH_SHORT).show();
								else
									Toast.makeText(getApplicationContext(), "Feedback "+messageStr+" received from the user: "+username, Toast.LENGTH_SHORT).show();
								/* Idea of updating list fragment within viewpager: http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager */
								TeacherVisionFragment fragment = (TeacherVisionFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0");
								if(fragment != null) {
									//if(fragment.getView() != null) {
										fragment.update();
									//}
								}
								//notifications could be used like this
						    	//feedbackNotification = new Notification(icon, tickerText, when);    	
						    	//String contentText = senderId+" sent feedback "+messageStr;
						    	//Intent notificationIntent = new Intent(getApplicationContext(), CwpForAndroidActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						    	//PendingIntent contentIntent = PendingIntent.getActivity(actContext, 0, notificationIntent, 0);
						    	//feedbackNotification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
								if(beepOn == true) {
									/* http://stackoverflow.com/questions/4441334/how-to-play-an-android-notification-sound */
									try {
										Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
										Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
										r.play();
									}
									catch(Exception e) {
										Log.e(TAG, "EXCEPTION AT PLAY SOUND WHEN RECEIVING FEEDBACK");
									}
								}
						    	/*Notification.Builder mBuilder = new Notification.Builder(getApplicationContext())
						    		.setSmallIcon(icon)
						    		.setContentTitle(contentTitle)
						    		.setContentText(contentText)
						    		.setSound(soundUri);*/
						        //mNotificationManager.notify(1, mBuilder.build());
								/*if(fragment == null)
									fragment = new TeacherVisionFragment();
								fragment.update();*/
							}
						}
					}
				}
				if(pollFeedbacksCancelled == false) {
					pollFeedbacksTask = new PollFeedbacksTask(getApplicationContext());
					pollFeedbacksTask.execute();
				}
			}
		}
	}	
	
	/**
	 * This task is used to send HTTP GET requests to the server.
	 * The task polls online students, registered for this lecture, by calling itself all over again as long as allowed to
	 *  */	
	private class PollUsersTask extends AsyncTask<String, Integer, Double> {
		protected int getUsersCode = 0;
		String results = "";
		private Context mContext;
		public PollUsersTask(Context context) {
			mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected(mContext)) {
				try {
					URL urli = new URL(usersUrl);
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
						getUsersCode = 1;
					}
					else {
						Log.e(TAG, "USERS GET FAILED");
						getUsersCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET USERS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET USERS "+e);
				}
				finally {
					if(reader != null) {
						try {
							reader.close();
						}
						catch(IOException e) {
							Log.e(TAG, "IOException at poll users ");
						}
					}
				}
				try {
					Thread.sleep(POLLSPAN);
				}
				catch(InterruptedException e) {
					Log.e(TAG, "THREAD INTERRUPTED AT USERS POLL");
				}
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(getUsersCode == 1) {
				//links could be handled like this
				//results = results.replace("\"link\": {", "");
				//results = results.replace("\"self\"},", "");
				String [] usersArr = null;
				if(results.contains("}")) {
					usersArr = results.split("[}]");
				}
				if(usersArr != null) {
					for(int i=0; i < usersArr.length; i++) {
						if(usersArr[i].contains("online")) {
							String [] userOnlineParts = usersArr[i].split("online");
							String userOnline = userOnlineParts[1].substring(3, 4);
							int userOn = 0;
							if(!userOnline.equals("n"))
								userOn = Integer.parseInt(userOnline);
							if(userOn == 1) {
								String [] regToLectureParts = usersArr[i].split("reg_to_lecture");
								if(regToLectureParts.length > 1) {
									String [] regStopParts = regToLectureParts[1].split(",");
									int regStop = (regStopParts[0].length());
									String regLecStr = regToLectureParts[1].substring(3, regStop);
									int lectureId = 0;
									if(!regLecStr.contains("null")) {
										lectureId = Integer.parseInt(regLecStr);
									}
									if(lectureId == teacher.getLectureId()) {
										//student is registered to this lecture, show this in UI in form of toast
										//get username, marker_number, mobile_ip, user_id and store into the onlineStudentsList
										String userName = "";
										int markerNum = 0;
										String mobileIp = "";
										int userId = 0;
										String lectureTopicChosen = "";
										String [] userNameParts = usersArr[i].split("username");
										if(userNameParts.length > 1) {
											String [] unameStopParts = userNameParts[1].split(",");
											int unameStop = (unameStopParts[0].length());
											userName = userNameParts[1].substring(4, unameStop-1);
										}
										String [] markerNumParts = usersArr[i].split("marker_number");
										if(markerNumParts.length > 1) {
											String [] markerStopParts = markerNumParts[1].split(",");
											int markerStop = (markerStopParts[0].length());
											String markerText = markerNumParts[1].substring(3, markerStop);
											if(!markerText.contains("null")) {
												markerNum = Integer.parseInt(markerText);
											}
										}
										String [] mobileIpParts = usersArr[i].split("mobile_ip");
										if(mobileIpParts.length > 1) {
											String [] ipStopParts = mobileIpParts[1].split(",");
											int ipStop = (ipStopParts[0].length());
											mobileIp = mobileIpParts[1].substring(4, ipStop-1);
										}
										String [] userIdParts = usersArr[i].split("user_id");
										if(userIdParts.length > 1) {
											String [] uidStopParts = userIdParts[1].split(",");
											int uidStop = (uidStopParts[0].length());
											String uidText = userIdParts[1].substring(3, uidStop);
											if(!uidText.equals("null")) {
												userId = Integer.parseInt(uidText);
											}
										}
										String [] lectureTopicChosenParts = usersArr[i].split("lec_top_chosen");
										if(lectureTopicChosenParts.length > 1) {
											String [] ltcStopParts = lectureTopicChosenParts[1].split(",");
											int ltcStop = (ltcStopParts[0].length());
											lectureTopicChosen = lectureTopicChosenParts[1].substring(4, ltcStop-1);
										}
										//iterate through the onlineStudentsList that this student does not already exist there
										Iterator<Student> iter = onlineStudentsList.iterator();
										boolean studentFound = false;
										while(iter.hasNext()) {
											Student onlineStudent = iter.next();
											if(onlineStudent.getUserName().equals(userName))
												studentFound = true;
										}
										if(studentFound == false) {
											//only new users make it to the list and toast
											Student student = new Student();
											student.setUserName(userName);
											student.setMarkerNumber(markerNum);
											student.setLectureTopicChosen(lectureTopicChosen);
											student.setDeviceIp(mobileIp);
											student.setUserId(userId);
											onlineStudentsList.add(student);
											Toast.makeText(getApplicationContext(), "Student "+userName+" registered to this lecture with marker number "+markerNum, Toast.LENGTH_SHORT).show();
											if(beepOn == true) {
										    	/* http://stackoverflow.com/questions/4441334/how-to-play-an-android-notification-sound */
												try {
													Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
													Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
													r.play();
												}
												catch(Exception e) {
													Log.e(TAG, "EXCEPTION AT PLAY SOUND WHEN POLLING STUDENTS");
												}
											}
										}
									}
								}
							}
							else if(userOn == 0) {
								//user offline, iterate through list that is the certain user stored in it. if it is, then remove it
								String userName = "";
								String [] userNameParts = usersArr[i].split("username");
								if(userNameParts.length > 1) {
									String [] unameStopParts = userNameParts[1].split(",");
									int unameStop = (unameStopParts[0].length());
									userName = userNameParts[1].substring(4, unameStop-1);
								}								
								Iterator<Student> iter = onlineStudentsList.iterator();
								int location = 0;
								while(iter.hasNext()) {
									Student onlineStudent = iter.next();
									if(onlineStudent.getUserName().equals(userName))
										onlineStudentsList.remove(location); //TODO: make sure that this really removes the student
									location++;
								}	
							}
						}
					}
				}
				if(pollUsersCancelled == false) {
					pollUsersTask = new PollUsersTask(getApplicationContext());
					pollUsersTask.execute();
				}
			}
		}
	}	

	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task puts lecture offline
	 *  */	
	private class PutLectureOfflineTask extends AsyncTask<String, Integer, Double> {
		protected int lectureOfflineCode = 0;
		private Context mContext;
		public PutLectureOfflineTask(Context context) {
			mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("online", 0);
						jsonObj.put("lecture_id", teacher.getLectureId());
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
						Log.i(TAG, "LECTURE OFFLINE SUCCEEDED");
						lectureOfflineCode = 1;
					}
					else {
						Log.e(TAG, "LECTURE OFFLINE FAILED "+httpCon.getResponseCode());
						lectureOfflineCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT COURSE OFFLINE"+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT COURSE OFFLINE "+e);
				}				
			}
			else {
				Log.e(TAG, "NETWORK CONNECTION ERROR");	
			}
			return null;
		}
		protected void onPostExecute(Double result) {
			if(lectureOfflineCode == 1) {
				//handle ok
			}
			else {
				//handle failure
			}
		}
	}
}


