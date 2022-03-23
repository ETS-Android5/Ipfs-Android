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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import ipfs.gomobile.android.IPFS;
import ipfs.gomobile.example.MainActivity;

public final class FetchFile extends AsyncTask<Void, Void, String> {
    private static final String TAG = "FetchIPFSFile";

    private final WeakReference<MainActivity> activityRef;
    private boolean backgroundError;
    private String fileName;
    private String cid;

    public FetchFile(MainActivity activity, String cid) {
        activityRef = new WeakReference<>(activity);
        this.cid = cid;
    }

    @Override
    protected void onPreExecute() {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        activity.displayStatusProgress("Fetching apk on IPFS");
    }

    @Override
    protected String doInBackground(Void... v) {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) {
            cancel(true);
            return null;
        }

        fileName = activity.getCacheDir() + "/" + cid + ".apk";

        IPFS ipfs = activity.getIpfs();

        Log.e(TAG, "Ipfs fetch start: " + fileName);
        try {
            byte[] fetchedData = ipfs.newRequest("cat")
                    .withArgument(cid)
                    .send();
            Log.e(TAG, "Ipfs fetch length: " + fetchedData.length);
            OutputStream out = new FileOutputStream(fileName);
            InputStream is = new ByteArrayInputStream(fetchedData);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = is.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            is.close();
            out.close();
            return "success";
        } catch (Exception err) {
            backgroundError = true;
            return MainActivity.exceptionToString(err);
        }
    }

    protected void onPostExecute(String result) {
        MainActivity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        if (backgroundError) {
            activity.displayStatusError("fail", result);
            Log.e(TAG, "Ipfs image fetch error: " + result);
        } else {
            File apk = new File(fileName);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", apk);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
            }
            activity.displayStatusSuccess(null);
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("TAG", fileName);
        }
    }

}
