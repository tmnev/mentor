package uni.oulu.mentor;

public interface TeacherVisionListener {
	public void onFeedbackReceived(int feedbackId);
	public void onAnswerReceived(int answerId);
	public void onQuestionReceived(int questionId);
	public void onRegisterReceived(int registerId);
}
