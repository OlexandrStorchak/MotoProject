package com.example.alex.motoproject.retainFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class RetainFragment extends Fragment {
    public Object data;

    // Find/Create in FragmentManager
    public static RetainFragment findOrCreate(FragmentManager fm, String tag) {
        RetainFragment retainFragment = (RetainFragment) fm.findFragmentByTag(tag);

        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            fm.beginTransaction()
                    .add(retainFragment, tag)
                    .commitAllowingStateLoss();
        }

        return retainFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keeps this Fragment alive during configuration changes
        setRetainInstance(true);
    }

    // Remove from FragmentManager
    public void remove(FragmentManager fm) {
        if (!fm.isDestroyed()) {
            fm.beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
            data = null;
        }
    }
}