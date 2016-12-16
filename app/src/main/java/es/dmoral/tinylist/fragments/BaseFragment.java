package es.dmoral.tinylist.fragments;

import android.support.v4.app.Fragment;

/**
 * Created by grender on 16/12/16.
 */

public abstract class BaseFragment extends Fragment {
    abstract void setupViews();

    public abstract void redrawItems();
}
