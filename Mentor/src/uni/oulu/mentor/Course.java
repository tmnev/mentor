package uni.oulu.mentor;

import java.util.ArrayList;
import java.util.List;

public class Course {
	int courseId;
	String courseName;
	List<Lecture> lectureList = new ArrayList<Lecture>();
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public int getCourseId() {
		return this.courseId;
	}
	public String getCourseName() {
		return this.courseName;
	}
	public void appendLecture(Lecture lecture) {
		this.lectureList.add(lecture);
	}
	public List<Lecture> getLectures() {
		return this.lectureList;
	}
}
