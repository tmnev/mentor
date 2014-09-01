package uni.oulu.mentor;

public class Feedback {
	String message;
	String username;
	int senderId;
	int delegatedTo;
	int immediate;
	int anonymous;
	int feedbackId;
	protected void setMessage(String message) {
		this.message = message;
	}
	protected void setUsername(String username) {
		this.username = username;
	}
	protected void setSenderId(int senderId) {
		this.senderId = senderId;
	}
	protected void setDelegatedTo(int delegatedTo) {
		this.delegatedTo = delegatedTo;
	}
	protected void setImmediate(int immediate) {
		this.immediate = immediate;
	}
	protected void setAnonymous(int anonymous) {
		this.anonymous = anonymous;
	}
	protected void setFeedbackId(int feedbackId) {
		this.feedbackId = feedbackId;
	}
	protected String getMessage() {
		return this.message;
	}
	protected String getUsername() {
		return this.username;
	}
	protected int getSenderId() {
		return this.senderId;
	}
	protected int getDelegatedTo() {
		return this.delegatedTo;
	}
	protected int getImmediate() {
		return this.immediate;
	}
	protected int getAnonymous() {
		return this.anonymous;
	}
	protected int getFeedbackId() {
		return this.feedbackId;
	}
}
