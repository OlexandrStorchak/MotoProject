package com.example.alex.motoproject.event;

public class AlertEvent {
    public final int alertType;
    public final boolean show;

    public AlertEvent(int alertType, boolean show) {
        this.alertType = alertType;
        this.show = show;
    }
}
