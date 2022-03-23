package ipfs.gomobile.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.season.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ipfs.gomobile.android.IPFS;
import ipfs.gomobile.example.task.FetchFile;
import ipfs.gomobile.example.task.FetchImage;
import ipfs.gomobile.example.task.FetchRandomXKCD;
import ipfs.gomobile.example.task.ShareFile;
import ipfs.gomobile.example.task.StartIPFS;

public class MainActivity extends AppCompatActivity {
    private IPFS ipfs;

    private TextView ipfsTitle;
    private ProgressBar ipfsStartingProgress;
    private TextView ipfsResult;

    private TextView peerCounter;

    private Button xkcdButton;
    private TextView ipfsStatus;
    private ProgressBar ipfsProgress;
    private TextView ipfsError;
    private EditText cidEditText;
    private View startContainer;
    private Button startButton;

    private PeerCounter peerCounterUpdater;

    public void setIpfs(IPFS ipfs) {
        this.ipfs = ipfs;
    }

    public IPFS getIpfs() {
        return ipfs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startContainer = findViewById(R.id.startVertical);
        startButton = findViewById(R.id.startButton);
        ipfsTitle = findViewById(R.id.ipfsTitle);
        ipfsStartingProgress = findViewById(R.id.ipfsStartingProgress);
        ipfsResult = findViewById(R.id.ipfsResult);

        peerCounter = findViewById(R.id.peerCounter);
        cidEditText = findViewById(R.id.edit_cid);
        xkcdButton = findViewById(R.id.xkcdButton);
        ipfsStatus = findViewById(R.id.ipfsStatus);
        ipfsProgress = findViewById(R.id.ipfsProgress);
        ipfsError = findViewById(R.id.ipfsError);

        resetStatus();

        final MainActivity activity = this;


        xkcdButton.setOnClickListener(v -> {
            new FetchRandomXKCD(activity).execute();
        });

        findViewById(R.id.sharePicButton).setOnClickListener(v -> {
            try {
                String file = copyAssetsTestImage(((EditText) findViewById(R.id.edit_assets)).getText().toString());
                new ShareFile(activity, Uri.fromFile(new File(file))).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.fetchApkButton).setOnClickListener(v -> {
            new FetchFile(MainActivity.this, cidEditText.getText().toString()).execute();
        });

        findViewById(R.id.fetchPicButton).setOnClickListener(v -> {
            new FetchImage(MainActivity.this, cidEditText.getText().toString()).execute();
        });

        startButton.setOnClickListener(v -> {
            ipfsTitle.setVisibility(View.VISIBLE);
            ipfsStartingProgress.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.INVISIBLE);

            new StartIPFS(this).execute();

        });
    }


    private void resetStatus() {
        ipfsTitle.setVisibility(View.INVISIBLE);
        ipfsStartingProgress.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.VISIBLE);
        if (ipfs != null && ipfs.isStarted()) {
            startContainer.setVisibility(View.GONE);
        } else {
            startContainer.setVisibility(View.VISIBLE);
        }
    }

    private String copyAssetsTestImage(String name) throws IOException {
        File file = new File(getCacheDir() + name);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(file.getAbsolutePath());
        myInput = this.getAssets().open(name);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        return file.getAbsolutePath();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (peerCounterUpdater != null) {
            peerCounterUpdater.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (peerCounterUpdater != null) {
            peerCounterUpdater.start();
        }
    }

    public void displayPeerIDError(String error) {
        resetStatus();
        ipfsTitle.setTextColor(Color.RED);
        ipfsResult.setTextColor(Color.RED);

        ipfsTitle.setText("Error:");
        ipfsResult.setText(error);
        ipfsStartingProgress.setVisibility(View.INVISIBLE);
    }

    public void displayPeerIDResult(String peerID) {
        resetStatus();
        ipfsTitle.setText("Your Peer ID is:");
        ipfsResult.setText(peerID);
        ipfsStartingProgress.setVisibility(View.INVISIBLE);

        updatePeerCount(0);

        peerCounterUpdater = new PeerCounter(this, 10000);
        peerCounterUpdater.start();
    }

    public void updatePeerCount(int count) {
        peerCounter.setText("Peers connected: " + count);
    }

    public void displayStatusProgress(String text) {
        ipfsStatus.setTextColor(ipfsTitle.getCurrentTextColor());
        ipfsStatus.setText(text);
        ipfsStatus.setVisibility(View.VISIBLE);
        ipfsError.setVisibility(View.INVISIBLE);
        ipfsProgress.setVisibility(View.VISIBLE);

    }

    public void displayStatusSuccess(String cid) {
        ipfsStatus.setVisibility(View.INVISIBLE);
        ipfsProgress.setVisibility(View.INVISIBLE);

        if (!TextUtils.isEmpty(cid)) {
            cidEditText.setText(cid);
        }
    }

    public void displayStatusError(String title, String error) {
        ipfsStatus.setTextColor(Color.RED);
        ipfsStatus.setText(title);

        ipfsProgress.setVisibility(View.INVISIBLE);
        ipfsError.setVisibility(View.VISIBLE);
        ipfsError.setText(error);

    }

    public static String exceptionToString(Exception error) {
        String string = error.getMessage();

        if (error.getCause() != null) {
            string += ": " + error.getCause().getMessage();
        }

        return string;
    }

}
