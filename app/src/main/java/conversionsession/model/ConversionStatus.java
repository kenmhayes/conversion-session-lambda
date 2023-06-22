package conversionsession.model;

public enum ConversionStatus {
    NOT_STARTED,
    STARTED,
    COMPLETED,
    FAILED_NO_RETRY,
    FAILED_CAN_RETRY
}
