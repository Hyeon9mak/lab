package wooteco.subway.exception;

import org.springframework.http.HttpStatus;

public class InvalidEmailException extends SubwayException {

    public InvalidEmailException() {
        super("올바르지 않은 이메일 형식입니다.");
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String message() {
        return "INVALID_EMAIL";
    }
}
