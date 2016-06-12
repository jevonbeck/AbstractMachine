package org.ricts.abstractmachine.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import org.ricts.abstractmachine.R;

/**
 * Created by Jevon on 05/06/2016.
 */
public class FilenameDialogFragment extends DialogFragment {
    private FileSaver mFileSaver;

    public interface FileSaver {
        void saveFile(String filename);
    }

    public FilenameDialogFragment(){
        // Required empty public constructor
    }

    public static FilenameDialogFragment newInstance(FileSaver saver){
        FilenameDialogFragment fragment = new FilenameDialogFragment();
        fragment.init(saver);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Context context = getContext();

        // TODO: scope text input
        final EditText mainView = new EditText(context);
        mainView.setHint(R.string.filename_hint);
        // android:inputType = "textPersonName"
        mainView.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        /** Actually create the dialog **/
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(mainView)
                // Add action buttons
                .setPositiveButton(R.string.save_button_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String filename = mainView.getText().toString();
                        if(isFilenameValid(filename)){
                            mFileSaver.saveFile(filename);
                        }
                        else {
                            Toast.makeText(context, context.getString(R.string.invalid_filename_message),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.negative_button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FilenameDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public void init(FileSaver saver){
        mFileSaver = saver;
    }

    private boolean isFilenameValid(String filename){
        return !filename.equals("") && !filename.matches("[/\\\\\\?%\\*:\\|<>\\s]");
    }
}
