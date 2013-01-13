package net.h31ix.anticheat.util;

public class CheckResult {
    
    public enum Result {
        PASSED,FAILED;
    }
    
    private Result result;
    private String message;
    
    public CheckResult(Result result, String message) {
        this(result);
        this.message = message;
    }
    
    public CheckResult(Result result) {
        this.result = result;
    }   
    
    public boolean failed() {
        return result == Result.FAILED;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Result getResult() {
        return result;
    }
    
}