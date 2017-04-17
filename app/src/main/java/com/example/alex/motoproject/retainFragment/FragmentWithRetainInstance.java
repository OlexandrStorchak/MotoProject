package com.example.alex.motoproject.retainFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

public abstract class FragmentWithRetainInstance extends Fragment {
    // Keeps track if this Fragment is being destroy by System or User
    protected boolean mDestroyedBySystem;
    private RetainFragment mRetainFragment;

    public FragmentWithRetainInstance() {

    }

    public abstract String getDataTag();

    // Convenience method to get data.
    public Object getRetainData() {
        return mRetainFragment.data;
    }

    // Convenience method to set data.
    public void setRetainData(Object data) {
        mRetainFragment.data = data;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Find or Create a RetainFragment to hold the component
        mRetainFragment = RetainFragment.findOrCreate(getFragManager(), getDataTag());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset this variable
        mDestroyedBySystem = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDestroyedBySystem = true;
    }

    @Override
    public void onDestroy() {
        if (mDestroyedBySystem) {
            onDestroyBySystem();
        } else {
            onDestroyByUser();
        }
        super.onDestroy();
    }

    // Activity destroyed By User. Perform cleanup of retain fragment.
    public void onDestroyByUser() {
        mRetainFragment.remove(getFragManager());
        mRetainFragment = null;
    }

    // Activity destroyed by System. Subclasses can override this if needed.
    public void onDestroyBySystem() {
    }

    public FragmentManager getFragManager() {
        return getActivity().getSupportFragmentManager();
    }
}
