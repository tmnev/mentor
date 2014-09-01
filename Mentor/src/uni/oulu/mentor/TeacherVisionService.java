package uni.oulu.mentor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class TeacherVisionService extends Service {
	private final IBinder binder = new TeacherVisionServiceBinder();
	private TeacherVisionConnection tvConnection;
	TeacherVisionActivity activity;
	public class TeacherVisionServiceBinder extends Binder {
		TeacherVisionService getService() {
			return TeacherVisionService.this;
		}
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show();
		return START_STICKY;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		tvConnection.listenerAvailable = false;
		return true;
	}
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	/* Here we make a new instance of TeacherVisionConnection and call it's constructor with context as it's parameter.
	 * We also set listenerAvailable variable to true here and return instance of connection to the caller */
	public TeacherVisionConnection getConnection(Context context) {
		if(tvConnection == null) {
		tvConnection = new TeacherVisionConnection(context);
		
		}
		tvConnection.listenerAvailable = true;
		return tvConnection;
	}
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show();
		Log.e("TV ", "Service stopping");
		tvConnection.disconnect();
		tvConnection = null;
		super.onDestroy();
	}
}
