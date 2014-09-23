package net.rdrei.android.dirchooser.sample;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.rdrei.android.dirchooser.DirectoryChooserFragment;


public class DirChooserFragment extends Fragment implements DirectoryChooserFragment.OnFragmentInteractionListener {

    private TextView mDirectoryTextView;
    private DirectoryChooserFragment mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = DirectoryChooserFragment.newInstance("DialogSample", null);
        mDialog.setTargetFragment(this, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        mDirectoryTextView = (TextView) view.findViewById(R.id.tvFragmentText);

        view.findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.show(getFragmentManager(), null);
            }
        });

        return view;
    }


    @Override
    public void onSelectDirectory(@NonNull String path) {
        mDirectoryTextView.setText(path);
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
    }
}
