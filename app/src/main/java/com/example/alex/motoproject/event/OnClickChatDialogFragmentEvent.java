package com.example.alex.motoproject.event;

public class OnClickChatDialogFragmentEvent {

    private int mDistanceLimit;

    public OnClickChatDialogFragmentEvent(int distanceLimit) {
        mDistanceLimit = distanceLimit;
    }

    public int getDistanceLimit() {
        return mDistanceLimit;
    }
}
