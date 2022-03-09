package ipfs.gomobile.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private IPFS ipfs;

    private TextView ipfsTitle;
    private ProgressBar ipfsStartingProgress;
    private TextView ipfsResult;

    private TextView peerCounter;

    private TextView onlineTitle;
    private TextView offlineTitle;
    private Button xkcdButton;
    private Button shareButton;
    private Button fetchButton;
    private TextView ipfsStatus;
    private ProgressBar ipfsProgress;
    private TextView ipfsError;
    private EditText cidEditText;

    private PeerCounter peerCounterUpdater;

    void setIpfs(IPFS ipfs) {
        this.ipfs = ipfs;
    }

    IPFS getIpfs() {
        return ipfs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipfsTitle = findViewById(R.id.ipfsTitle);
        ipfsStartingProgress = findViewById(R.id.ipfsStartingProgress);
        ipfsResult = findViewById(R.id.ipfsResult);

        peerCounter = findViewById(R.id.peerCounter);
        cidEditText = findViewById(R.id.edit_cid);
        onlineTitle = findViewById(R.id.onlineTitle);
        offlineTitle = findViewById(R.id.offlineTitle);
        xkcdButton = findViewById(R.id.xkcdButton);
        shareButton = findViewById(R.id.shareButton);
        fetchButton = findViewById(R.id.fetchButton);
        ipfsStatus = findViewById(R.id.ipfsStatus);
        ipfsProgress = findViewById(R.id.ipfsProgress);
        ipfsError = findViewById(R.id.ipfsError);

        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            new StartIPFS(this).execute();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        final MainActivity activity = this;


        xkcdButton.setOnClickListener(v -> new FetchRandomXKCD(activity).execute());

        shareButton.setOnClickListener(v -> {
            if (true) {
                try {
                    String file = copyAssetsTestImage();
                    new ShareFile(activity, Uri.fromFile(new File(file))).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        });

        fetchButton.setOnClickListener(v -> new FetchFile(MainActivity.this, cidEditText.getText().toString()).execute());
    }

    private String copyAssetsTestImage() throws IOException {
        File file = new File(getCacheDir() + "nft.png");
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(file.getAbsolutePath());
        myInput = this.getAssets().open("nft.png");
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] strPerm,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, strPerm, grantResults);

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //new StartIPFS(this).execute();
        } else {
            Toast.makeText(this, R.string.ble_permissions_denied,
                    Toast.LENGTH_LONG).show();
        }
        new StartIPFS(this).execute();
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

    void displayPeerIDError(String error) {
        ipfsTitle.setTextColor(Color.RED);
        ipfsResult.setTextColor(Color.RED);

        ipfsTitle.setText(getString(R.string.titlePeerIDErr));
        ipfsResult.setText(error);
        ipfsStartingProgress.setVisibility(View.INVISIBLE);
    }

    void displayPeerIDResult(String peerID) {
        ipfsTitle.setText(getString(R.string.titlePeerID));
        ipfsResult.setText(peerID);
        ipfsStartingProgress.setVisibility(View.INVISIBLE);

        updatePeerCount(0);
        peerCounter.setVisibility(View.VISIBLE);
        onlineTitle.setVisibility(View.VISIBLE);
        offlineTitle.setVisibility(View.VISIBLE);
        xkcdButton.setVisibility(View.VISIBLE);
        shareButton.setVisibility(View.VISIBLE);
        fetchButton.setVisibility(View.VISIBLE);
        cidEditText.setVisibility(View.VISIBLE);

        peerCounterUpdater = new PeerCounter(this, 10000);
        peerCounterUpdater.start();
    }

    void updatePeerCount(int count) {
        peerCounter.setText(getString(R.string.titlePeerCon, count));
    }

    void displayStatusProgress(String text) {
        ipfsStatus.setTextColor(ipfsTitle.getCurrentTextColor());
        ipfsStatus.setText(text);
        ipfsStatus.setVisibility(View.VISIBLE);
        ipfsError.setVisibility(View.INVISIBLE);
        ipfsProgress.setVisibility(View.VISIBLE);

        xkcdButton.setAlpha(0.5f);
        xkcdButton.setClickable(false);
        shareButton.setAlpha(0.5f);
        shareButton.setClickable(false);
        fetchButton.setAlpha(0.5f);
        fetchButton.setClickable(false);
        cidEditText.setVisibility(View.INVISIBLE);
    }

    void displayStatusSuccess(String cid) {
        ipfsStatus.setVisibility(View.INVISIBLE);
        ipfsProgress.setVisibility(View.INVISIBLE);

        cidEditText.setText(cid);
        xkcdButton.setAlpha(1);
        xkcdButton.setClickable(true);
        shareButton.setAlpha(1);
        shareButton.setClickable(true);
        fetchButton.setAlpha(1);
        fetchButton.setClickable(true);
        cidEditText.setVisibility(View.VISIBLE);
    }

    void displayStatusError(String title, String error) {
        ipfsStatus.setTextColor(Color.RED);
        ipfsStatus.setText(title);

        ipfsProgress.setVisibility(View.INVISIBLE);
        ipfsError.setVisibility(View.VISIBLE);
        ipfsError.setText(error);

        xkcdButton.setAlpha(1);
        xkcdButton.setClickable(true);
        shareButton.setAlpha(1);
        shareButton.setClickable(true);
        fetchButton.setAlpha(1);
        fetchButton.setClickable(true);
        cidEditText.setVisibility(View.VISIBLE);
    }

    static String exceptionToString(Exception error) {
        String string = error.getMessage();

        if (error.getCause() != null) {
            string += ": " + error.getCause().getMessage();
        }

        return string;
    }

}
