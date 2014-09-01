package uni.oulu.mentor;

public class Question {
	String content;
	String username;
	int userId;
	int delegatedTo;
	int answered;
	int anonymous;
	int questionId;
	protected void setContent(String content) {
		this.content = content;
	}
	protected void setUsername(String username) {
		this.username = username;
	}
	protected void setUserId(int userId) {
		this.userId = userId;
	}
	protected void setDelegatedTo(int delegatedTo) {
		this.delegatedTo = delegatedTo;
	}
	protected void setAnswered(int answered) {
		this.answered = answered;
	}
	protected void setAnonymous(int anonymous) {
		this.anonymous = anonymous;
	}
	protected void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	protected String getContent() {
		return this.content;
	}
	protected String getUsername() {
		return this.username;
	}
	protected int getUserId() {
		return this.userId;
	}
	protected int getDelegatedTo() {
		return this.delegatedTo;
	}
	protected int getAnswered() {
		return this.answered;
	}
	protected int getAnonymous() {
		return this.anonymous;
	}
	protected int getQuestionId() {
		return this.questionId;
	}
}
