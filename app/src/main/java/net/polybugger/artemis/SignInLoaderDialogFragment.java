package net.polybugger.artemis;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class SignInLoaderDialogFragment extends AppCompatDialogFragment {

    public static final String TAG = "net.polybugger.artemis.sign_in_loader_dialog_fragment";

    public SignInLoaderDialogFragment() { }

    public static SignInLoaderDialogFragment newInstance() {
        SignInLoaderDialogFragment df = new SignInLoaderDialogFragment();
        df.setCancelable(false);
        return df;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        if(window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return inflater.inflate(R.layout.fragment_sign_in_loader_dialog, container, false);
    }
}
