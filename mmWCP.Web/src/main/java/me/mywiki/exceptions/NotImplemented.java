package me.mywiki.exceptions;

public class NotImplemented extends UnsupportedOperationException {
    
    public NotImplemented() {
        super("Operation not implemented yet");
    }

    private static final long serialVersionUID = 1L;

}
