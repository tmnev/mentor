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
import android.support.v13.app.FragmentPagerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.text.Spanned;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class StudentVisionActivity extends Activity {
	private static final String TAG = "StudentVision";
	protected static final int CONNTIME = 30000;
	protected static final int POLLSPAN = 4000;
	private static final String allowed = new String("0123456789abcdefghijklmnopqrstuvwxyzåäöABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ ?");
	private static final int NUM_ITEMS = 3;
	private static final int MIN_LENGTH = 2;
	private static final int MAX_LENGTH = 30;
	private static EditText svCustomTextEdit;
	private static EditText questionEdit;
	private static EditText answerEdit;
	private static String coursesUrl;
	private static String message = "";
	private static String svListItemQcontent = "";
	private static String questionContent = "";
	protected static String answerContent = "";
	private String usersUrl;
	private String ipStr;
	private boolean pollTeacherFeedbacksCancelled = false;
	private boolean pollTeacherQuestionsCancelled = false;
	private boolean pollTeacherAnswersCancelled = false;
	private static boolean svListItemAnswer = false;
	private static int immediate = 0;
	private static int svListItemQuid = 0;
	private static int teacherId = 0;
	private static int anonymousChosen = 0;
	private static int qAnonymousChosen = 0;
	private static int aAnonymousChosen = 0;
	static List<Feedback> teacherFeedbacksList;
	static List<Question> teacherQuestionsList;
	static List<Answer> teacherAnswersList;
	static List<Question> svOwnQuestionsList;
	private AsyncTask<String, Integer, Double> pollTeacherFeedbacksTask;
	private AsyncTask<String, Integer, Double> pollTeacherQuestionsTask;
	private AsyncTask<String, Integer, Double> pollTeacherAnswersTask;
	private static ArrayAdapter<String> mTeacherFeedbackAdapter;
	private static ArrayAdapter<String> mTeacherQuestionAdapter;
	private static ArrayAdapter<String> mTeacherAnswerAdapter;
	static SvPopUpFragment svPuFragment;
	static Student student;
	SvTabAdapter mAdapter;
	ViewPager mPager;
	Button svBtn, svBtn2, svBtn3;
	StudentVisionFragment fragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_vision);
		//get student object from previous activity
		Intent intent = getIntent();
		student = (Student) intent.getParcelableExtra("studentObj");
		ipStr = getResources().getString(R.string.IP);
		usersUrl = "http://"+ipStr+"/mentor/users";
		coursesUrl = "http://"+ipStr+"/mentor/courses";
		//instantiate/initialize the lists
		teacherFeedbacksList = new ArrayList<Feedback>();
		teacherQuestionsList = new ArrayList<Question>();
		teacherAnswersList = new ArrayList<Answer>();
		svOwnQuestionsList = new ArrayList<Question>();
		//instantiate view pager and set adapter for it 
		mPager = (ViewPager)findViewById(R.id.sv_pager);
		mAdapter = new SvTabAdapter(getFragmentManager(), mPager);
		mPager.setAdapter(mAdapter);
		//instantiate the tab buttons, set the first button clicks disabled at first, because it's chosen by default
		svBtn = (Button)findViewById(R.id.sv_goto_first);
		svBtn.setEnabled(false);
		svBtn2 = (Button)findViewById(R.id.sv_goto_second);
		svBtn3 = (Button)findViewById(R.id.sv_goto_last);
        svBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                svBtn.setEnabled(false);
                svBtn2.setEnabled(true);
                svBtn3.setEnabled(true);
                mPager.setCurrentItem(0);
            }
        });
        svBtn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {            
                svBtn.setEnabled(true);
                svBtn2.setEnabled(false);
                svBtn3.setEnabled(true);    
                mPager.setCurrentItem(1);
            }
        });
        svBtn3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                svBtn.setEnabled(true);
                svBtn2.setEnabled(true);
                svBtn3.setEnabled(false);
                mPager.setCurrentItem(2);
            }
        });

		//begin to poll feedbacks
		pollTeacherFeedbacksCancelled = false;
		pollTeacherFeedbacksTask = new PollTeacherFeedbacksTask(getApplicationContext());
		pollTeacherFeedbacksTask.execute();
		//begin to poll questions
		pollTeacherQuestionsCancelled = false;
		pollTeacherQuestionsTask = new PollTeacherQuestionsTask(getApplicationContext());
		pollTeacherQuestionsTask.execute();
		//begin to poll answers
		pollTeacherAnswersCancelled = false;
		pollTeacherAnswersTask = new PollTeacherAnswersTask(getApplicationContext());
		pollTeacherAnswersTask.execute();
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
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }		
	
	@Override
	protected void onDestroy() {
		pollTeacherFeedbacksCancelled = true;
		pollTeacherQuestionsCancelled = true;
		pollTeacherAnswersCancelled = true;
		//put this student account offline
		new ExitStudentTask(getApplicationContext()).execute();
		super.onDestroy();
		this.finish();
	}	
	
	/**
	 * This method is used in the case when student answers individual question, and then presses send-button from the dialog
	 */
	public void svSendPersonalClicked(View v) {
		if(teacherId != 0) {
			if(!svCustomTextEdit.getText().toString().trim().equals("") && svCustomTextEdit.getText().toString().length() 
					>= MIN_LENGTH && svCustomTextEdit.getText().toString().length() <= MAX_LENGTH) {
				message = svCustomTextEdit.getText().toString();
			}
			//send the answer, delegated_to this userId
			new PostAnswerTask(this).execute();
		}
		//dismiss the dialog
		if(svPuFragment != null) {
			if(svPuFragment.isAdded() == true || svPuFragment.isVisible())
				svPuFragment.dismiss();
		}
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
	 * Handle anonymous checkbox clicks
	 */
	static OnClickListener anListener = new OnClickListener() {
		public void onClick(View view) {
			boolean checked = ((CheckBox) view).isChecked();
			switch(view.getId()) {
				case 238:
					if(checked)
						anonymousChosen = 1;
					else
						anonymousChosen = 0;
					break;
				case 241:
					if(checked)
						qAnonymousChosen = 1;
					else
						qAnonymousChosen = 0;
				case 243:
					if(checked)
						aAnonymousChosen = 1;
					else
						aAnonymousChosen = 0;
			}			
		}
	};
	
	/**
	 * Handle radio button clicked, which dictates the feedback type
	 */
	static OnClickListener rbListener = new OnClickListener() {
		public void onClick(View view) {
			boolean checked = ((RadioButton) view).isChecked();
			switch(view.getId()) {
				case 234:
					if(checked) {
						//more examples
						immediate = 2;
						message = "More examples";
					}
					break;
				case 235:
					if(checked) {
						//slow down
						immediate = 3;
						message = "Slow down";
					}
					break;
				case 236:
					if(checked) {
						//speed up
						immediate = 1;
						message = "Speed up";
					}
					break;
				case 237:
					if(checked) {
						//repeat
						immediate = 4;
						message = "Repeat";
					}
			}			
		}
	};

	/**
	 * This window is popped-up when student wants to answer certain question
	 */
	public static class SvPopUpFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View popUpView = inflater.inflate(R.layout.fragment_svpopup, null);
			svCustomTextEdit = (EditText) popUpView.findViewById(R.id.svCustomTextEdit);
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
			svCustomTextEdit.setFilters(new InputFilter[]{filter});
			builder.setView(popUpView);
			builder.setTitle(R.string.popUpTitle)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//do something
					}
				});
			return builder.create();
		}
	}

	/**
	 * This fragment is used for list views, populated on the right side of tab views
	 */
	public static class StudentVisionFragment extends ListFragment {
		int mNum;
		String[] feedbackTexts = null;
		String[] questionTexts = null;
		String[] answerTexts = null;
		static StudentVisionFragment newInstance(int num) {
			StudentVisionFragment svf = new StudentVisionFragment();
			Bundle args = new Bundle();
			args.putInt("num",num);
			svf.setArguments(args);
			return svf;
		}
		public void update() {
			int fsize = teacherFeedbacksList.size();
			int qsize = teacherQuestionsList.size();
			int asize = teacherAnswersList.size();
			String[] nFeedbackTexts = new String[fsize];
			String[] nQuestionTexts = new String[qsize];
			String[] nAnswerTexts = new String[asize];
			int fc = 0;
			int qc = 0;
			int ac = 0;
			Iterator<Feedback> iter = teacherFeedbacksList.iterator();
			while(iter.hasNext()) {
				Feedback storedFeedback = iter.next();
				nFeedbackTexts[fc] = storedFeedback.getUsername() + ": " + storedFeedback.getMessage();
				fc++;
			}
			Iterator<Question> iter2 = teacherQuestionsList.iterator();
			while(iter2.hasNext()) {
				Question storedQuestion = iter2.next();
				nQuestionTexts[qc] = storedQuestion.getUsername() + ": " + storedQuestion.getContent();
				qc++;
			}
			Iterator<Answer> iter3 = teacherAnswersList.iterator();
			while(iter3.hasNext()) {
				Answer storedAnswer = iter3.next();
				if(storedAnswer.getQuestionContent() == null)
					storedAnswer.setQuestionContent("");
				nAnswerTexts[ac] = storedAnswer.getUsername() + ": " + storedAnswer.getContent()  + " to question " + storedAnswer.getQuestionContent();
				ac++;
			}
			mTeacherFeedbackAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,nFeedbackTexts);
			mTeacherQuestionAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,nQuestionTexts);
			mTeacherAnswerAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,nAnswerTexts); 
			if(mNum == 0) {
				setListAdapter(mTeacherFeedbackAdapter);
				mTeacherFeedbackAdapter.notifyDataSetChanged();
			}
			else if(mNum == 1) {
				setListAdapter(mTeacherQuestionAdapter);
				mTeacherQuestionAdapter.notifyDataSetChanged();
			}
			else if(mNum == 2) {
				setListAdapter(mTeacherAnswerAdapter);
				mTeacherAnswerAdapter.notifyDataSetChanged();
			}
		}
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			int fsize = teacherFeedbacksList.size();
			int qsize = teacherQuestionsList.size();
			int asize = teacherAnswersList.size();
			feedbackTexts = new String[fsize];
			questionTexts = new String[qsize];
			answerTexts = new String[asize];
			int fc = 0;
			int qc = 0;
			int ac = 0;
			Iterator<Feedback> iter = teacherFeedbacksList.iterator();
			while(iter.hasNext()) {
				Feedback storedFeedback = iter.next();
				feedbackTexts[fc] = storedFeedback.getUsername() + ": " + storedFeedback.getMessage();
				fc++;
			}
			Iterator<Question> iter2 = teacherQuestionsList.iterator();
			while(iter2.hasNext()) {
				Question storedQuestion = iter2.next();
				questionTexts[qc] = storedQuestion.getUsername() + ": " + storedQuestion.getContent();
				qc++;
			}
			Iterator<Answer> iter3 = teacherAnswersList.iterator();
			while(iter3.hasNext()) {
				Answer storedAnswer = iter3.next();
				if(storedAnswer.getQuestionContent() == null)
					storedAnswer.setQuestionContent("");
				answerTexts[ac] = storedAnswer.getUsername() + ": " + storedAnswer.getContent()  + " to question " + storedAnswer.getQuestionContent();;
				ac++;
			}
			mTeacherFeedbackAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,feedbackTexts);
			mTeacherQuestionAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,questionTexts);
			mTeacherAnswerAdapter = new ArrayAdapter<String>(getActivity(),R.layout.tv_list_item,android.R.id.empty,answerTexts);
			mNum = getArguments() != null ? getArguments().getInt("num") : 1;
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_student_vision, container, false);
			LinearLayout leftSide = (LinearLayout)view.findViewById(R.id.sv_leftside);
			TextView tv1 = new TextView(leftSide.getContext());
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
				LinearLayout linl = new LinearLayout(leftSide.getContext());
				linl.setOrientation(LinearLayout.HORIZONTAL);
				LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				linl.setLayoutParams(lp);
				RadioGroup rg = new RadioGroup(leftSide.getContext());
				rg.setOrientation(RadioGroup.VERTICAL);
				rg.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				RadioButton rb1 = new RadioButton(leftSide.getContext());
				rb1.setText(R.string.more_examples);
				rb1.setTextColor(Color.parseColor("#FFFFFF"));
				rb1.setTextSize(10);
				rb1.setId(234);
				rb1.setOnClickListener(rbListener);
				rg.addView(rb1);
				RadioButton rb2 = new RadioButton(leftSide.getContext());
				rb2.setText(R.string.slow_down);
				rb2.setTextColor(Color.parseColor("#FFFFFF"));
				rb2.setTextSize(10);
				rb2.setId(235);
				rb2.setOnClickListener(rbListener);
				rg.addView(rb2);
				RadioButton rb3 = new RadioButton(leftSide.getContext());
				rb3.setText(R.string.speed_up);
				rb3.setTextColor(Color.parseColor("#FFFFFF"));
				rb3.setTextSize(10);
				rb3.setId(236);
				rb3.setOnClickListener(rbListener);
				rg.addView(rb3);
				leftSide.addView(rg);
				RadioButton rb4 = new RadioButton(leftSide.getContext());
				rb4.setText(R.string.repeat);
				rb4.setTextColor(Color.parseColor("#FFFFFF"));
				rb4.setTextSize(10);
				rb4.setId(237);
				rb4.setOnClickListener(rbListener);
				rg.addView(rb4);
				CheckBox cb = new CheckBox(leftSide.getContext());
				cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				cb.setText(R.string.anonymous);
				cb.setOnClickListener(anListener);
				cb.setTextColor(Color.parseColor("#FFFFFF"));
				cb.setTextSize(10);
				cb.setId(238);
				linl.addView(cb);
				Button btn = new Button(leftSide.getContext());
				btn.setText(R.string.send);
				btn.setTextColor(Color.WHITE);
				btn.setTextSize(10);
				btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				btn.setId(239);
				btn.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		            	//get lecture and is it online
		            	//if the lecture is online
		            	new PostFeedbackTask(getActivity().getApplicationContext()).execute();
		            }
		        });
				linl.addView(btn);
				leftSide.addView(linl);
			}
			else if(mNum == 1) {
				tv1.setText(R.string.question);
				questionEdit = new EditText(leftSide.getContext());
				questionEdit.setFilters(new InputFilter[]{filter});
				questionEdit.setTextSize(12);
				questionEdit.setTextColor(Color.parseColor("#FFFFFF"));
				Button btn = new Button(leftSide.getContext());
				btn.setText(R.string.send);
				btn.setTextColor(Color.parseColor("#FFFFFF"));
				btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				btn.setId(240);
				btn.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		            	//check first
		    			if(!questionEdit.getText().toString().trim().equals("") && questionEdit.getText().toString().length() 
		    					>= MIN_LENGTH && questionEdit.getText().toString().length() <= MAX_LENGTH) {
		    				questionContent = questionEdit.getText().toString();
		    			}
		            	new PostQuestionTask(getActivity().getApplicationContext()).execute();
		            }
		        });
				CheckBox cb = new CheckBox(leftSide.getContext());
				cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				cb.setText(R.string.anonymous);
				cb.setTextSize(10);
				cb.setOnClickListener(anListener);
				cb.setTextColor(Color.parseColor("#FFFFFF"));
				cb.setId(241);
				leftSide.addView(tv1);
				leftSide.addView(cb);
				leftSide.addView(questionEdit);
				leftSide.addView(btn);
			}
			else if(mNum == 2) {
				tv1.setText(R.string.answer);
				answerEdit = new EditText(leftSide.getContext());
				answerEdit.setFilters(new InputFilter[]{filter});
				answerEdit.setTextSize(12);
				answerEdit.setTextColor(Color.parseColor("#FFFFFF"));
				Button btn = new Button(leftSide.getContext());
				btn.setText(R.string.send);
				btn.setTextColor(Color.parseColor("#FFFFFF"));
				btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				btn.setId(242);
				btn.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		            	//check first
		    			if(!answerEdit.getText().toString().trim().equals("") && answerEdit.getText().toString().length() 
		    					>= MIN_LENGTH && answerEdit.getText().toString().length() <= MAX_LENGTH) {
		    				answerContent = answerEdit.getText().toString();
		    			}
		            	new PostAnswerTask(getActivity().getApplicationContext()).execute();
		            }
		        });
				CheckBox cb = new CheckBox(leftSide.getContext());
				cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				cb.setText(R.string.anonymous);
				cb.setTextSize(10);
				cb.setOnClickListener(anListener);
				cb.setTextColor(Color.parseColor("#FFFFFF"));
				cb.setId(243);
				leftSide.addView(tv1);
				leftSide.addView(cb);
				leftSide.addView(answerEdit);
				leftSide.addView(btn);
			}
			return view;
		}
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			if(mNum == 0) {
				setListAdapter(mTeacherFeedbackAdapter);	
			}
			else if(mNum == 1) {
				setListAdapter(mTeacherQuestionAdapter);
			}
			else if(mNum == 2) {
				setListAdapter(mTeacherAnswerAdapter);
			}
		}
		@Override
		public void onListItemClick(ListView lv, View v, int position, long id) {
			Log.i(TAG, "Item num " +id +" clicked");
			if(mNum == 0) {
				//do nothing
			}
			else if(mNum == 1) {
				Question question = teacherQuestionsList.get(position); 
				svListItemQuid = question.getQuestionId();
				svListItemQcontent = question.getContent();
				teacherId = question.getUserId();
				if(svPuFragment == null) {
					svPuFragment = new SvPopUpFragment();
					if(svPuFragment.isAdded() == false)
						svPuFragment.show(getFragmentManager(), "svPopUp"); 
				}
				else {
					if(svPuFragment.isVisible() == false)
						svPuFragment.show(getFragmentManager(), "svPopUp");
				}				
			}
			else if(mNum == 3) {
				//do nothing
			}
		}
	};
	
	/**
	 * This tab adapter is used to handle events happening in tab views
	 */
	public class SvTabAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
		public SvTabAdapter(FragmentManager fm, ViewPager vp) {
			super(fm);
			vp.setOnPageChangeListener(this);
		}
		@Override
		public void onPageScrollStateChanged(int state) {
			//do nothing
		}
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			// do nothing
		}
		
		@Override
		public void onPageSelected(int position) {
			if(position == 0) {
				svBtn.setEnabled(false);
				svBtn2.setEnabled(true);
				svBtn3.setEnabled(true);
			}
			else if(position == 1) {
				svBtn.setEnabled(true);
				svBtn2.setEnabled(false);
				svBtn3.setEnabled(true);
			}
			else if(position == 2) {
				svBtn.setEnabled(true);
				svBtn2.setEnabled(true);
				svBtn3.setEnabled(false);
			}			
		}
		@Override
		public Fragment getItem(int position) {
			return StudentVisionFragment.newInstance(position);
		}
		@Override
		public int getCount() {
			return NUM_ITEMS;
		}
	};
	
	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task posts answer to the teacher
	 *  */	
	private static class PostAnswerTask extends AsyncTask<String, Integer, Double> {
		protected int postAnswerCode = 0;
		private Context mContext;
		public PostAnswerTask(Context context) {
			mContext = context;
		}
		@Override
		protected void onPreExecute() {
			//do something
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
						jsonObj.put("delegated_to", teacherId); 
						jsonObj.put("user_id", student.getUserId());
						jsonObj.put("username", student.getUserName());
						jsonObj.put("question_msg", svListItemQcontent);
						jsonObj.put("anonymous", aAnonymousChosen);
						if(svListItemAnswer == true) {
							jsonObj.put("question_id", svListItemQuid);
							svListItemAnswer = false;
						}
						else
							jsonObj.put("question_id", 0);  
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+student.getCourseIdChosen()+"/lectures/"+student.getLectureIdChosen()+"/answers");
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
						Log.i(TAG, "POST ANSWER SUCCEEDED");
						postAnswerCode = 1;
					}
					else {
						Log.e(TAG, "POST ANSWER FAILED "+httpCon.getResponseCode());
						postAnswerCode = 3;
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
			if(postAnswerCode == 1) {
				Toast.makeText(mContext, "Answer sent succesfully to the teacher", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(mContext, "Answer send failed", Toast.LENGTH_SHORT).show();		
			}
		}
	}	

	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task posts question to the teacher
	 *  */	
	private static class PostQuestionTask extends AsyncTask<String, Integer, Double> {
		protected int postQuestionCode = 0;
		private Context mContext;
		public PostQuestionTask(Context context) {
			mContext = context;
		}
		@Override
		protected void onPreExecute() {
			//progressBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedWriter writer = null;
			if(networkConnected(mContext)) {
				try {
					String jsonStr = "";
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("content", questionContent); 
						jsonObj.put("delegated_to", 0); 
						jsonObj.put("user_id", student.getUserId());
						jsonObj.put("username", student.getUserName());
						jsonObj.put("anonymous", qAnonymousChosen);
						jsonObj.put("answered", 0); 
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+student.getCourseIdChosen()+"/lectures/"+student.getLectureIdChosen()+"/questions");
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
						Log.i(TAG, "POST QUESTION SUCCEEDED");
						postQuestionCode = 1;
					}
					else {
						Log.e(TAG, "POST QUESTION FAILED "+httpCon.getResponseCode());
						postQuestionCode = 3;
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
			if(postQuestionCode == 1) {
				Question ownQuestion = new Question();
				ownQuestion.setContent(questionContent);
				ownQuestion.setUserId(student.getUserId());
				ownQuestion.setUsername(student.getUserName());
				//ownQuestion.setDelegatedTo(teacherUid);
				//ownQuestion.setQuestionId(questionId);
				svOwnQuestionsList.add(ownQuestion);
				Toast.makeText(mContext, "Question sent succesfully to the teacher", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(mContext, "Question send failed", Toast.LENGTH_SHORT).show();		
			}
		}
	}	

	/**
	 * This task is used to send HTTP POST request to the server.
	 * The task posts feedback to the teacher
	 *  */	
	private static class PostFeedbackTask extends AsyncTask<String, Integer, Double> {
		protected int postFeedbackCode = 0;
		private Context mContext;
		public PostFeedbackTask(Context context) {
			mContext = context;
		}
		@Override
		protected void onPreExecute() {
			//progressBar.setVisibility(View.VISIBLE);
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
						jsonObj.put("sender_id", student.getUserId());
						jsonObj.put("username", student.getUserName());
						jsonObj.put("anonymous", anonymousChosen);
						jsonObj.put("immediate", immediate); //0 is for non-immediate feedback, 1 for "speed up", 2 for "more examples", 3 for "slow down", 4 for "repeat"
					}
					catch(Exception ex) {
						Log.e(TAG, "JSON Exception "+ex.getStackTrace());
					}
					jsonStr = jsonObj.toString();
					URL url = new URL(coursesUrl+"/"+student.getCourseIdChosen()+"/lectures/"+student.getLectureIdChosen()+"/feedbacks");
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
						Log.i(TAG, "POST FEEDBACK SUCCEEDED");
						postFeedbackCode = 1;
					}
					else {
						Log.e(TAG, "POST FEEDBACK FAILED "+httpCon.getResponseCode());
						postFeedbackCode = 3;
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
			if(postFeedbackCode == 1) {
				Toast.makeText(mContext, "Feedback sent succesfully to the teacher", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(mContext, "Feedback send failed", Toast.LENGTH_SHORT).show();			
			}
		}
	}	

	/**
	 * This task is used to send HTTP GET request to the server.
	 * The task polls answers, sent by the teacher, by calling itself all over again as long as allowed to
	 *  */	
	private class PollTeacherAnswersTask extends AsyncTask<String, Integer, Double> {
		protected int getAnswersCode = 0;
		String results = "";
		Context mContext;
		public PollTeacherAnswersTask(Context context) {
			this.mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected(mContext)) {
				try {
					URL urli = new URL(coursesUrl+"/"+student.getCourseIdChosen()+"/lectures/"+student.getLectureIdChosen()+"/answers");
					HttpURLConnection httpCon = (HttpURLConnection)urli.openConnection();
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestMethod("GET");
					httpCon.setReadTimeout(CONNTIME);
					httpCon.setConnectTimeout(CONNTIME);
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "ANSWERS GET SUCCEEDED");
						getAnswersCode = 1;
					}
					else {
						Log.e(TAG, "ANSWERS GET FAILED");
						getAnswersCode = 2;
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
							String [] usernameParts = answersArr[i].split("username");
							if(usernameParts.length > 1) {	
								String [] usernameStopParts = usernameParts[1].split(",");
								int usernameStop = (usernameStopParts[0].length());
								username = usernameParts[1].substring(4, usernameStop-1);
							}
							//only note answers that are delegated to this user
							if(delegatedTo == student.getUserId()) {
								//iterate through the teacherAnswersList that this answer does not already exist there
								Iterator<Answer> iter = teacherAnswersList.iterator();
								boolean answerFound = false;
								while(iter.hasNext()) {
									Answer storedAnswer = iter.next();
									if(storedAnswer.getContent().equals(contentStr))
										answerFound = true;
								}
								if(answerFound == false) {
									//only new answers make it to the list and toast
									Answer answer = new Answer();
									answer.setContent(contentStr);
									answer.setUserId(userId);
									answer.setDelegatedTo(delegatedTo);
									//answer.setQuestionId(questionId);
									answer.setUsername(username);
									Iterator<Question> iter4 = svOwnQuestionsList.iterator();
									while(iter4.hasNext()) {
										Question question = iter4.next();
										//answer is already dedicated to this user because we are here
										if(questionContent.equals(question.getContent()))
											answer.setQuestionContent(question.getContent());
									}
									teacherAnswersList.add(answer);
									Toast.makeText(getApplicationContext(), "Answer "+contentStr+" received from the user "+username, Toast.LENGTH_SHORT).show();
									/* Idea of updating list fragment within viewpager: http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager */
									StudentVisionFragment fragment = (StudentVisionFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.sv_pager+":2");
									if(fragment != null) {
										fragment.update();
									}
								}
							}
						}
					}
				}
				if(pollTeacherAnswersCancelled == false) {
					pollTeacherAnswersTask = new PollTeacherAnswersTask(mContext);
					pollTeacherAnswersTask.execute();
				}
			}
		}
	}	

	/**
	 * This task is used to send HTTP GET request to the server.
	 * The task polls questions, sent by the teacher, by calling itself all over again as long as allowed to
	 *  */
	private class PollTeacherQuestionsTask extends AsyncTask<String, Integer, Double> {
		protected int getQuestionsCode = 0;
		String results = "";
		Context mContext;
		public PollTeacherQuestionsTask(Context context) {
			this.mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected(mContext)) {
				try {
					URL urli = new URL(coursesUrl+"/"+student.getCourseIdChosen()+"/lectures/"+student.getLectureIdChosen()+"/questions");
					HttpURLConnection httpCon = (HttpURLConnection)urli.openConnection();
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestMethod("GET");
					httpCon.setReadTimeout(CONNTIME);
					httpCon.setConnectTimeout(CONNTIME);
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "QUESTIONS GET SUCCEEDED");
						getQuestionsCode = 1;
					}
					else {
						Log.e(TAG, "QUESTIONS GET FAILED");
						getQuestionsCode = 2;
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
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET QUESTIONS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET QUESTIONS "+e);
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
							String [] usernameParts = questionsArr[i].split("username");
							if(usernameParts.length > 1) {	
								String [] usernameStopParts = usernameParts[1].split(",");
								int usernameStop = (usernameStopParts[0].length());
								username = usernameParts[1].substring(4, usernameStop-1);
							}
							//note only the questions that are delegated to this student
							if(delegatedTo == student.getUserId()) {
								//iterate through the teacherQuestionsList that this question does not already exist there
								Iterator<Question> iter = teacherQuestionsList.iterator();
								boolean questionFound = false;
								while(iter.hasNext()) {
									Question storedQuestion = iter.next();
									if(storedQuestion.getContent().equals(contentStr))
										questionFound = true;
								}
								if(questionFound == false) {
									//only new questions make it to the list and toast
									Question question = new Question();
									question.setContent(contentStr);
									question.setUserId(userId);
									question.setDelegatedTo(delegatedTo);
									question.setAnswered(answered);
									question.setUsername(username);
									question.setQuestionId(questionId);
									teacherQuestionsList.add(question);
									Toast.makeText(getApplicationContext(), "Question "+contentStr+" received from the user "+username, Toast.LENGTH_SHORT).show();
									/* Idea of updating list fragment within viewpager: http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager */
									StudentVisionFragment fragment = (StudentVisionFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.sv_pager+":1");
									if(fragment != null) {
										fragment.update();
									}
								}
							}
						}
					}
				}
				if(pollTeacherQuestionsCancelled == false) {
					pollTeacherQuestionsTask = new PollTeacherQuestionsTask(mContext);
					pollTeacherQuestionsTask.execute();
				}
			}
		}
	}	
	
	/**
	 * This task is used to send HTTP GET request to the server.
	 * The task polls feedbacks, sent by the teacher, by calling itself all over again as long as allowed to
	 *  */
	private class PollTeacherFeedbacksTask extends AsyncTask<String, Integer, Double> {
		protected int getFeedbacksCode = 0;
		String results = "";
		Context mContext;
		public PollTeacherFeedbacksTask(Context context) {
			this.mContext = context;
		}
		@Override
		protected Double doInBackground(String... params) {
			BufferedReader reader = null;
			if(networkConnected(mContext)) {
				try {
					URL urli = new URL(coursesUrl+"/"+student.getCourseIdChosen()+"/lectures/"+student.getLectureIdChosen()+"/feedbacks");
					HttpURLConnection.setFollowRedirects(false);
					HttpURLConnection httpCon = (HttpURLConnection)urli.openConnection();
					httpCon.setDoInput(true);
					httpCon.setRequestProperty("Accept","application/json");
					httpCon.setRequestMethod("GET");
					httpCon.setReadTimeout(CONNTIME);
					httpCon.setConnectTimeout(CONNTIME);
					if(httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
						Log.i(TAG, "FEEDBACKS GET SUCCEEDED");
						getFeedbacksCode = 1;
					}
					else {
						Log.e(TAG, "FEEDBACKS GET FAILED");
						getFeedbacksCode = 2;
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
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT GET FEEDBACKS "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT GET FEEDBACKS "+e);
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
							int senderId = 0;
							int delegatedTo = 0;
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
							String [] delegatedToParts = feedbacksArr[i].split("delegated_to");
							if(delegatedToParts.length > 1) {
								String [] delegatedStopParts = delegatedToParts[1].split(",");
								int delegatedStop = (delegatedStopParts[0].length());
								String delegatedText = delegatedToParts[1].substring(3, delegatedStop);
								if(!delegatedText.equals("null")) {
									delegatedTo = Integer.parseInt(delegatedText);
								}
							}
							String [] usernameParts = feedbacksArr[i].split("username");
							if(usernameParts.length > 1) {	
								String [] usernameStopParts = usernameParts[1].split(",");
								int usernameStop = (usernameStopParts[0].length());
								username = usernameParts[1].substring(4, usernameStop-1);
							}
							//note only feedbacks which are delegated to this student
							if(delegatedTo == student.getUserId()) {
								//iterate through the feedbacksList that this feedback does not already exist there
								Iterator<Feedback> iter = teacherFeedbacksList.iterator();
								boolean feedbackFound = false;
								while(iter.hasNext()) {
									Feedback storedFeedback = iter.next();
									if(storedFeedback.getMessage().equals(messageStr))
										feedbackFound = true;
								}
								if(feedbackFound == false) {
									//only new feedbacks make it to the list and toast
									Feedback feedback = new Feedback();
									feedback.setMessage(messageStr);
									feedback.setSenderId(senderId);
									feedback.setUsername(username);
									teacherFeedbacksList.add(feedback);
									Toast.makeText(getApplicationContext(), "Feedback "+messageStr+" received from the user "+username, Toast.LENGTH_SHORT).show();
									/* Idea of updating list fragment within viewpager: http://stackoverflow.com/questions/7379165/update-data-in-listfragment-as-part-of-viewpager */
									StudentVisionFragment fragment = (StudentVisionFragment)getFragmentManager().findFragmentByTag("android:switcher:"+R.id.sv_pager+":0");
									if(fragment != null) {
										fragment.update();
									}
								}
							}
						}
					}
				}
				if(pollTeacherFeedbacksCancelled == false) {
					pollTeacherFeedbacksTask = new PollTeacherFeedbacksTask(mContext);
					pollTeacherFeedbacksTask.execute();
				}
			}
		}
	}		

	/**
	 * This task is used to send HTTP PUT request to the server.
	 * The task puts this student-typed user offline
	 *  */
	private class ExitStudentTask extends AsyncTask<String, Integer, Double> {
		protected int exitUserCode = 0;
		private Context mContext;
		public ExitStudentTask(Context context) {
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
						jsonObj.put("password", student.getStudentPwd());
						jsonObj.put("name", student.getStudentName());
						jsonObj.put("email", "something@something.com");
						jsonObj.put("number", "123");
						jsonObj.put("online", 0);
						jsonObj.put("mobile_user", "1");
						jsonObj.put("mobile_ip", student.getDeviceIp());
						jsonObj.put("marker_number", "0");
						jsonObj.put("reg_to_course", "0");
						jsonObj.put("reg_to_lecture", "0");
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
						exitUserCode = 1;
					}
					else {
						Log.e(TAG, "USER EDIT FAILED "+httpCon.getResponseCode());
						exitUserCode = 2;
					}
				}
				catch(UnsupportedEncodingException e) {
					Log.e(TAG, "UNSUPPORTED ENCODING EXCEPTION AT POST COURSE "+e);
				}
				catch(IOException e) {
					Log.e(TAG, "IO EXCEPTION AT POST COURSE "+e);
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
}
