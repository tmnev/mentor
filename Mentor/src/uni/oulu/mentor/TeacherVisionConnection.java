package uni.oulu.mentor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

@SuppressWarnings("all")
public class TeacherVisionConnection {
	private static final int REGISTER_VAL = -4;
	private static final int ANSWER_VAL = -3;
	private static final int QUESTION_VAL = -2;
	private static final int FEEDBACK_VAL = -1;
	private static final int MAX_VAL = 2147483647;
	private static final int MIN_VAL = 1;
	private static final int TV_ID = 1;
	private static final int mPort = 5000;
	private static CharSequence feedbackContentTitle = "Mentor feedback message received";
	private static CharSequence questionContentTitle = "Mentor question message received";
	private static CharSequence answerContentTitle = "Mentor answer message received";
	private static CharSequence registerContentTitle = "New user registered to the lecture";
	private boolean mNotificate = false;
	private CharSequence contentText;
	private boolean threadStarted = false;
	private int icon;
	private String mHost;
	private long when = 0;
	volatile boolean running = true;
	volatile boolean listenerAvailable = false;
	private int readVal;
	private int feedbackId = 0;
	private int questionId = 0;
	private int answerId = 0;
	private int registerId = 0;
	protected String ipStr;
	private ServerSocket serverSocket;
	Context mContext;
	Socket mSocket = null;
	DataInputStream dis;
	DataOutputStream dos;
	TeacherVisionListener tvListener;
	NotificationManager mNotificationManager;
	Notification feedbackNotification, questionNotification, answerNotification, registerNotification;
	private static CharSequence tickerText = "Incoming Mentor Message";
	Thread thread;
	public TeacherVisionConnection(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		icon = R.drawable.mlogo2;
		ipStr = mContext.getResources().getString(R.string.IP);
		mHost = "http://"+ipStr; //THIS SHOULD BE STUDENTS IP, NOT OURS
		listenerAvailable = true;
        /* Here we create our working thread as an anonymous (inner) class and override the run
         * method within it. In this run method we create a new socket and open data input stream. 
         * We also start receiveMessage loop to listen for incoming messages from the server here. */
		thread = new Thread() {
			@Override
			public void run() {
				try {
					//mSocket = new Socket(mHost, mPort);
					mSocket = null;
					serverSocket = new ServerSocket(mPort);
					mSocket = serverSocket.accept();
					if(dis == null) {
						dis = new DataInputStream(mSocket.getInputStream());
					}
					if(dos == null) {
						dos = new DataOutputStream(mSocket.getOutputStream());
					}
					try {
						
						receiveMessage();
					}
					catch(IOException ex) {
						Log.e("AT TVCONNECTION ", "Error at receiveMessage(): " +ex);
					}
				}
                catch(UnknownHostException e) {
                	Log.e("AT TVCONNECTION ", "Socket creation failed because of unknown host" +e);
                }
                catch(IOException e) {
                	Log.e("AT TVCONNECTION ", "Socket creation failed because of IOException " +e);
                }     
			}
		};
	}
	
	/* This loop listens to opened socket for incoming messages. First it reads 4 byte signed integer from the socket.
	 * If listener was not available (activity is at background), we don't send messages to the activity (and show in UI).
	 * Instead we show a notification that message was received from the server. */
	public void receiveMessage() throws IOException  {
		while(running) {
			synchronized (this) {
				try {
					if(dis != null) {
						readVal = dis.readInt();
						if(readVal == FEEDBACK_VAL) {
							if(listenerAvailable == true) {
								//write reply, indicating OK
								dos.writeInt(1024);
								//read feedback id from stream
								feedbackId = dis.readUnsignedShort();
								if(feedbackId >= MIN_VAL && feedbackId <= MAX_VAL)
									tvListener.onFeedbackReceived(feedbackId);
								Log.e("AT RECEIVE MESSAGE ", "Got feedback event from the server " +questionId);
							}
							else {
								/* Here we show a notification that a feedback message is received when the activity is at background 
								 * When we press the notification message, we restore the old activity instead of creating new one.
								 * This is a good way to save memory and resources, and makes it easier for us to close only one
								 * activity*/
						    	when = System.currentTimeMillis();
						    	feedbackNotification = new Notification(icon, tickerText, when);    	
						    	contentText = mHost +" sent feedback with id " +Integer.toString(feedbackId);
						    	/* Idea of using this setFlags method and these flags (to return into currently running activity instead of creating new one) 
						    	 * is from http://stackoverflow.com/questions/5247902/intent-to-resume-a-previously-paused-activity-called-from-a-notification */
						    	Intent notificationIntent = new Intent(mContext, TeacherVisionActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						    	PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
						    	feedbackNotification.setLatestEventInfo(mContext, feedbackContentTitle, contentText, contentIntent);
						        mNotificationManager.notify(TV_ID, feedbackNotification);
						        mNotificate = true;
							}
						}
						else if(readVal == QUESTION_VAL) {
							if(listenerAvailable == true) {
								//write reply, indicating OK
								dos.writeInt(1024);
								//read question id from stream
								questionId = dis.readUnsignedShort();
								if(questionId >= MIN_VAL && questionId <= MAX_VAL)
									tvListener.onQuestionReceived(questionId);
								Log.e("AT RECEIVE MESSAGE ", "Got question event from the server " +questionId);
							}
							else {
								/* Here we show a notification that a feedback message is received when the activity is at background 
								 * When we press the notification message, we restore the old activity instead of creating new one.
								 * This is a good way to save memory and resources, and makes it easier for us to close only one
								 * activity*/
						    	when = System.currentTimeMillis();
						    	feedbackNotification = new Notification(icon, tickerText, when);    	
						    	contentText = mHost +" sent question with id " +Integer.toString(questionId);
						    	/* Idea of using this setFlags method and these flags (to return into currently running activity instead of creating new one) 
						    	 * is from http://stackoverflow.com/questions/5247902/intent-to-resume-a-previously-paused-activity-called-from-a-notification */
						    	Intent notificationIntent = new Intent(mContext, TeacherVisionActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						    	PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
						    	feedbackNotification.setLatestEventInfo(mContext, questionContentTitle, contentText, contentIntent);
						        mNotificationManager.notify(TV_ID, feedbackNotification);
						        mNotificate = true;
							}
						}
						else if(readVal == ANSWER_VAL) {
							if(listenerAvailable == true) {
								//write reply, indicating OK
								dos.writeInt(1024);
								//read answer id from stream
								answerId = dis.readUnsignedShort();
								if(answerId >= MIN_VAL && answerId <= MAX_VAL)
									tvListener.onAnswerReceived(answerId);
								Log.e("AT RECEIVE MESSAGE ", "Got answer event from the server " +answerId);
							}
							else {
								/* Here we show a notification that a feedback message is received when the activity is at background 
								 * When we press the notification message, we restore the old activity instead of creating new one.
								 * This is a good way to save memory and resources, and makes it easier for us to close only one
								 * activity*/
						    	when = System.currentTimeMillis();
						    	feedbackNotification = new Notification(icon, tickerText, when);    	
						    	contentText = mHost +" sent answer with id " +Integer.toString(feedbackId);
						    	/* Idea of using this setFlags method and these flags (to return into currently running activity instead of creating new one) 
						    	 * is from http://stackoverflow.com/questions/5247902/intent-to-resume-a-previously-paused-activity-called-from-a-notification */
						    	Intent notificationIntent = new Intent(mContext, TeacherVisionActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						    	PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
						    	feedbackNotification.setLatestEventInfo(mContext, answerContentTitle, contentText, contentIntent);
						        mNotificationManager.notify(TV_ID, feedbackNotification);
						        mNotificate = true;
							}
						}
						else if(readVal == REGISTER_VAL) {
							if(listenerAvailable == true) {
								//write reply, indicating OK
								dos.writeInt(1024);
								//read user id from stream
								registerId = dis.readUnsignedShort();
								if(registerId >= MIN_VAL && answerId <= MAX_VAL)
									tvListener.onAnswerReceived(registerId);
								Log.e("AT RECEIVE MESSAGE ", "Got register event from the server " +registerId);
							}
							else {
								/* Here we show a notification that a feedback message is received when the activity is at background 
								 * When we press the notification message, we restore the old activity instead of creating new one.
								 * This is a good way to save memory and resources, and makes it easier for us to close only one
								 * activity*/
						    	when = System.currentTimeMillis();
						    	registerNotification = new Notification(icon, tickerText, when);    	
						    	contentText = mHost +" sent register with userId " +Integer.toString(registerId);
						    	/* Idea of using this setFlags method and these flags (to return into currently running activity instead of creating new one) 
						    	 * is from http://stackoverflow.com/questions/5247902/intent-to-resume-a-previously-paused-activity-called-from-a-notification */
						    	Intent notificationIntent = new Intent(mContext, TeacherVisionActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						    	PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
						    	registerNotification.setLatestEventInfo(mContext, registerContentTitle, contentText, contentIntent);
						        mNotificationManager.notify(TV_ID, registerNotification);
						        mNotificate = true;
							}
						}
						/*else if(readVal >= MIN_VAL && readVal <= MAX_VAL) {

						}*/
						else {
							Log.e("AT RECEIVE MESSAGE ", "Got message from the server");
							//got other kind of message
						}
					}
				}
				catch(IOException e) {
					Log.i("TV CONNECTION ", "Message reading failed with IOException " +e);
					running = false;
					disconnect();
					break;
				}
			}
		}
	}
	
	public void disconnect() {
		try {
			running = false;
			mNotificate = false;
			if(serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
			if(mSocket != null) {
				mSocket.close();
				mSocket = null;
			}
			if(dis != null) {
				dis.close();
				dis = null;
			}
			if(dos != null) {
				dos.close();
				dos = null;
			}
			thread.interrupt();
		}
		catch(Exception e) {
			Log.i("AT TV CONNECTION ", "Exception in disconnecting " +e);
		}
	}
	
	/* Here we store the listener we receive as a parameter to the global variable tvListener.
	 * With this tvListener variable we can call TeacherVisionService's abstract methods, which are
	 * overridden and and implemented in TeacherVisionActivity, which implements TeacherVisionListener */
	public void setListener(TeacherVisionListener listener) {
		tvListener = listener;
		if(threadStarted == false) {
			thread.start();
			threadStarted = true;
		}
	}
}
