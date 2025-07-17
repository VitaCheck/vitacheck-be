package com.vitacheck.global.apiPayload;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.vitacheck.global.apiPayload.code.BaseCode;
import com.vitacheck.global.apiPayload.code.BaseErrorCode;
import com.vitacheck.global.apiPayload.code.GeneralSuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class CustomResponse<T> {

    @JsonProperty("isSuccess") // isSuccess라는 변수라는 것을 명시하는 Annotation
    private boolean isSuccess;

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("result")
    private final T result;

    // 200 OK
    public static <T> CustomResponse<T> ok(T result) {
        return CustomResponse.onSuccess(GeneralSuccessCode.OK, result);
    }

    // 201 CREATED
    public static <T> CustomResponse<T> created(T result) {
        return CustomResponse.onSuccess(GeneralSuccessCode.CREATED, result);
    }

    // 상태 코드 받아 응답하는 메서드
    public static <T> CustomResponse<T> onSuccess(BaseCode code, T result) {
        return new CustomResponse<>(
                true,
                code.getCode(),
                code.getMessage(),
                result
        );
    }

    //실패 응답 생성 메서드 (데이터 포함)
    public static <T> CustomResponse<T> onFailure(BaseErrorCode code, T result) {
        return new CustomResponse<>(
                false,
                code.getCode(),
                code.getMessage(),
                result
        );
    }

    //실패 응답 생성 메서드 (데이터 없음)
    public static <T> CustomResponse<T> onFailure(BaseErrorCode code) {
        return new CustomResponse<>(
                false,
                code.getCode(),
                code.getMessage(),
                null
        );
    }
}