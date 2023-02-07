package com.liulishuo.okdownload.core.exception;

import java.io.IOException;

public class NetworkPolicyException extends IOException {
    public NetworkPolicyException() {
        super("Only allows downloading this task on the wifi network type!");
    }
}
