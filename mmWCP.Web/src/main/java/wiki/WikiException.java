package wiki;

public class WikiException extends RuntimeException {

	public WikiException() {
		super();
	}

	public WikiException(String message) {
		super(message);
	}

	public WikiException(String message, Throwable cause) {
		super(message, cause);
	}

	public WikiException(Throwable cause) {
		super(cause);
	}

}
