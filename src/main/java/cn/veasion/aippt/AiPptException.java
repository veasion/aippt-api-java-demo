package cn.veasion.aippt;

/**
 * AiPptException
 *
 * @author luozhuowei
 * @date 2024/7/12
 */
public class AiPptException extends RuntimeException {

    public AiPptException(String message) {
        super(message);
    }

    public AiPptException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
