package uni.oulu.mentor;

import android.os.Parcel;
import android.os.Parcelable;

public class Teacher implements Parcelable {
	private String courseName;
	private String teacherName;
	private int lectureNumber;
	private int alertThreshold;
	private int timeInterval;
	private String deviceIp;
	private String pwd;
	public int tvisitorChosen;
	private int userId;
	private int courseId;
	private String userName;
	private int lectureId;
	private String lectureTopic;
	private int courseNameDefaulted;
	private int lectureTopicDefaulted;
	//getters and setters
	protected int getUserId() {
		return this.userId;
	}
	protected int getCourseId() {
		return this.courseId;
	}
	protected String getCourseName() {
		return this.courseName;
	}
	protected String getTeacherName() {
		return this.teacherName;
	}
	protected String getTeacherPwd() {
		return this.pwd;
	}
	protected int getLectureName() {
		return this.lectureNumber;
	}
	protected int getAlertThreshold() {
		return this.alertThreshold;
	}
	protected int getTimeInterval() {
		return this.timeInterval;
	}
	protected String getDeviceIp() {
		return this.deviceIp;
	}
	protected int getTvisitorChosen() {
		return this.tvisitorChosen;
	}
	protected String getUserName() {
		return this.userName;
	}
	protected int getLectureId() {
		return this.lectureId;
	}
	protected String getLectureTopic() {
		return this.lectureTopic;
	}
	protected int getCourseNameDefaulted() {
		return this.courseNameDefaulted;
	}
	protected int getLectureTopicDefaulted() {
		return this.lectureTopicDefaulted;
	}
	protected void setUserId(int userId) {
		this.userId = userId;
	}
	protected void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	protected void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	protected void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}
	protected void setTeacherPwd(String pwd) {
		this.pwd = pwd;
	}
	protected void setLectureNumber(int lectureNumber) {
		this.lectureNumber = lectureNumber;
	}
	protected void setAlertThreshold(int alertThreshold) {
		this.alertThreshold = alertThreshold;
	}
	protected void setTimeInterval(int timeInterval) {
		this.timeInterval = timeInterval;
	}
	protected void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}
	protected void setTvisitorChosen(int tvisitorChosen) {
		this.tvisitorChosen = tvisitorChosen;
	}
	protected void setUserName(String userName) {
		this.userName = userName;
	}
	protected void setLectureId(int lectureId) {
		this.lectureId = lectureId;
	}
	protected void setLectureTopic(String lectureTopic) {
		this.lectureTopic = lectureTopic;
	}
	protected void setCourseNameDefaulted(int courseNameDefaulted) {
		this.courseNameDefaulted = courseNameDefaulted;
	}
	protected void setLectureTopicDefaulted(int lectureTopicDefaulted) {
		this.lectureTopicDefaulted = lectureTopicDefaulted;
	}
	//needed for parcelable class
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(courseName);
		out.writeString(teacherName);
		out.writeInt(lectureNumber);
		out.writeInt(alertThreshold);
		out.writeInt(timeInterval);
		out.writeString(deviceIp);
		out.writeString(pwd);
		out.writeInt(tvisitorChosen);
		out.writeInt(userId);
		out.writeInt(courseId);
		out.writeString(userName);
		out.writeInt(lectureId);
		out.writeString(lectureTopic);
		out.writeInt(courseNameDefaulted);
		out.writeInt(lectureTopicDefaulted);
	}
	public static final Parcelable.Creator<Teacher> CREATOR = new Parcelable.Creator<Teacher>() {
		public Teacher createFromParcel(Parcel in) {
			return new Teacher(in);
		}
		public Teacher[] newArray(int size) {
			return new Teacher[size];
		}
	};
	private Teacher(Parcel in) {
		courseName = in.readString();
		teacherName = in.readString();
		lectureNumber = in.readInt();
		alertThreshold = in.readInt();
		timeInterval = in.readInt();
		deviceIp = in.readString();
		pwd = in.readString();
		tvisitorChosen = in.readInt();
		userId = in.readInt();
		courseId = in.readInt();
		userName = in.readString();
		lectureId = in.readInt();
		lectureTopic = in.readString();
		courseNameDefaulted = in.readInt();
		lectureTopicDefaulted = in.readInt();
	}
	//basic constructor
	protected Teacher() {}
};

