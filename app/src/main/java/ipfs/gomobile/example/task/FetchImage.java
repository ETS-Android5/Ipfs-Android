package ipfs.gomobile.example.task;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.season.myapplication.BuildConfig;
import com.season.myapplication.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import ipfs.gomobile.android.IPFS;
import ipfs.gomobile.example.DisplayImageActivity;
import ipfs.gomobile.example.MainActivity;

public final class FetchImage extends AsyncTask<Void, Void, String> {
    private static final String TAG = "FetchIPFSFile";

    private final WeakReference<MainActivity> activityRef;
    private boolean backgroundError;
    private byte[] fetchedData;
    private String cid;

    public FetchImage(MainActivity activity, String cid) {
        activityRef = new WeakReference<>(activity);
        this.cid = cid;
    }

    @Override
    protected void onPreExecute() {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        activity.displayStatusProgress("Fetching image on IPFS");
    }

    @Override
    protected String doInBackground(Void... v) {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) {
            cancel(true);
            return null;
        }

        IPFS ipfs = activity.getIpfs();

        try {
            fetchedData = ipfs.newRequest("cat")
                    .withArgument(cid)
                    .send();
            return "success";
        } catch (Exception err) {
            backgroundError = true;
            return MainActivity.exceptionToString(err);
        }
    }

    protected void onPostExecute(String result) {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        //copyFile(fileName, fileSD);
        if (backgroundError) {
            activity.displayStatusError("fail", result);
            Log.e(TAG, "Ipfs image fetch error: " + result);
        } else {
            activity.displayStatusSuccess(null);

            // Put directly data through this way because of size limit with Intend
            DisplayImageActivity.fetchedData = fetchedData;

            Intent intent = new Intent(activity, DisplayImageActivity.class);
            intent.putExtra("Title", result);
            activity.startActivity(intent);
        }
    }


}
