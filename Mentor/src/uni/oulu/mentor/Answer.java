package uni.oulu.mentor;

public class Answer {
	String content;
	String username;
	int userId;
	int delegatedTo;
	int questionId;
	int anonymous;
	String questionContent;
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
	protected void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	protected void setAnonymous(int anonymous) {
		this.anonymous = anonymous;
	}
	protected void setQuestionContent(String questionContent) {
		this.questionContent = questionContent;
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
	protected int getQuestionId() {
		return this.questionId;
	}
	protected int getAnonymous() {
		return this.anonymous;
	}
	protected String getQuestionContent() {
		return this.questionContent;
	}
}
