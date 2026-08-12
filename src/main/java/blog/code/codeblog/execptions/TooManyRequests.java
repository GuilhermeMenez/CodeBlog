package blog.code.codeblog.execptions;

public class TooManyRequests extends RuntimeException {
    public TooManyRequests(String message) {
        super(message);
    }
}
