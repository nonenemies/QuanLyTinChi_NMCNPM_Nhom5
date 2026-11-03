package Services;

/** Database failure exposed to application callers without leaking JDBC details to the UI. */
public final class ServiceException extends RuntimeException {
    public ServiceException(String userMessage) {
        super(userMessage);
    }

    public ServiceException(String userMessage, Throwable cause) {
        super(userMessage, cause);
    }

    public static ServiceException wrap(String userMessage, RuntimeException cause) {
        return cause instanceof ServiceException serviceException
                ? serviceException
                : new ServiceException(userMessage, cause);
    }
}
