package com.liulishuo.okdownload.core.exception;

import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import java.io.IOException;

public class ResumeFailedException extends IOException {
    private final ResumeFailedCause resumeFailedCause;

    public ResumeFailedException(ResumeFailedCause cause) {
        super("Resume failed because of " + cause);
        this.resumeFailedCause = cause;
    }

    public ResumeFailedCause getResumeFailedCause() {
        return this.resumeFailedCause;
    }
}
