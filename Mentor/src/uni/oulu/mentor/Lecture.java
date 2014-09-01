package uni.oulu.mentor;

public class Lecture {
	int lectureId;
	int courseId;
	int number;
	int is_exercise;
	String topic;
	public void setLectureId(int lectureId) {
		this.lectureId = lectureId;
	}
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public void setIsExercise(int is_exercise) {
		this.is_exercise = is_exercise;
	}
	public void setLectureTopic(String topic) {
		this.topic = topic;
	}
	public int getLectureId() {
		return this.lectureId;
	}
	public int getCourseId() {
		return this.courseId;
	}
	public int getNumber() {
		return this.number;
	}
	public int getIsExercise() {
		return this.is_exercise;
	}
	public String getLectureTopic() {
		return this.topic;
	}
}
