package me.taromati.chzzklib.exception;

import lombok.Getter;

@Getter
public class ChzzkException extends RuntimeException {

    private final String code;
    private final String message;

    public ChzzkException(ExceptionCode code) {
        super(code.getMessage());

        this.message = code.getMessage();
        this.code = code.getCode();
    }

    public ChzzkException(String code, String message) {
        super(message);

        this.message = message;
        this.code = code;
    }

}