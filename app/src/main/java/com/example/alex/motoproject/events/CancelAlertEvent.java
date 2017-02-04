package com.example.alex.motoproject.events;

public class CancelAlertEvent {
    public final int alertType;

    public CancelAlertEvent(int alertType) {
        this.alertType = alertType;
    }
}
