package net.polybugger.artemis;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SignInMethodFragment extends Fragment {

    public static final String TAG = "net.polybugger.artemis.sign_in_method_fragment";

    public static SignInMethodFragment newInstance() {
        SignInMethodFragment f = new SignInMethodFragment();
        //Bundle args = new Bundle();
        //args.putSerializable(PAST_CURRENT_ARG, pastCurrentEnum);
        //f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity) {
            /*
            try {
                mListener = (Listener) context;
            }
            catch(ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement " + Listener.class.toString());
            }
            */
        }
    }

    @Override
    public void onDetach() {
        // mListener = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
        Bundle args = getArguments();
        mPastCurrent = (PastCurrentEnum) args.getSerializable(PAST_CURRENT_ARG);
        switch(mPastCurrent) {
            case PAST:
                getActivity().setTitle(R.string.past_classes);
                break;
            case CURRENT:
                getActivity().setTitle(R.string.current_classes);
                break;
        }
        */

        View view = inflater.inflate(R.layout.fragment_sign_in_method, container, false);
        return view;
    }
}
