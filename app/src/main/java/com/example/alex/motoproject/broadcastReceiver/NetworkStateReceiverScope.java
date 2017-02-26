package com.example.alex.motoproject.broadcastReceiver;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface NetworkStateReceiverScope {
}
