package uni.oulu.mentor;

import android.os.Parcel;
import android.os.Parcelable;

public class Student implements Parcelable {
	private int markerNumber;
	private String studentName;
	private int lectureIdChosen;
	private int courseIdChosen;
	private String lectureTopicChosen;
	private String deviceIp;
	private int userId;
	private String pwd;
	private String userName;
	protected int getMarkerNumber() {
		return this.markerNumber;
	}
	protected String getStudentName() {
		return this.studentName;
	}
	protected String getLectureTopicChosen() {
		return this.lectureTopicChosen;
	}
	protected String getDeviceIp() {
		return this.deviceIp;
	}
	protected int getLectureIdChosen() {
		return this.lectureIdChosen;
	}
	protected int getCourseIdChosen() {
		return this.courseIdChosen;
	}
	protected int getUserId() {
		return this.userId;
	}
	protected String getStudentPwd() {
		return this.pwd;
	}
	protected String getUserName() {
		return this.userName;
	}
	protected void setMarkerNumber(int markerNumber) {
		this.markerNumber = markerNumber;
	}
	protected void setStudentName(String studentName) {
		this.studentName = studentName;
	}
	protected void setLectureTopicChosen(String lectureTopicChosen) {
		this.lectureTopicChosen = lectureTopicChosen;
	}
	protected void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}
	protected void setLectureIdChosen(int lectureIdChosen) {
		this.lectureIdChosen = lectureIdChosen;
	}
	protected void setCourseIdChosen(int courseIdChosen) {
		this.courseIdChosen = courseIdChosen;
	}
	protected void setUserId(int userId) {
		this.userId = userId;
	}
	protected void setStudentPwd(String pwd) {
		this.pwd = pwd;
	}
	protected void setUserName(String userName) {
		this.userName = userName;
	}
	//needed for parcelable class
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(markerNumber);
		out.writeString(studentName);
		out.writeInt(lectureIdChosen);
		out.writeInt(courseIdChosen);
		out.writeString(lectureTopicChosen);
		out.writeString(deviceIp);
		out.writeInt(userId);
		out.writeString(pwd);
		out.writeString(userName);
	}
	public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() {
		public Student createFromParcel(Parcel in) {
			return new Student(in);
		}
		public Student[] newArray(int size) {
			return new Student[size];
		}
	};
	private Student(Parcel in) {
		markerNumber = in.readInt();
		studentName = in.readString();
		lectureIdChosen = in.readInt();
		courseIdChosen = in.readInt();
		lectureTopicChosen = in.readString();
		deviceIp = in.readString();
		userId = in.readInt();
		pwd = in.readString();
		userName = in.readString();
	}
	//basic constructor
	protected Student() {}
}
