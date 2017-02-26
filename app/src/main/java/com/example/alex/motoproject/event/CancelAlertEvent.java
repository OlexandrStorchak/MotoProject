package com.example.alex.motoproject.event;

public class CancelAlertEvent {
    public final int alertType;

    public CancelAlertEvent(int alertType) {
        this.alertType = alertType;
    }
}
