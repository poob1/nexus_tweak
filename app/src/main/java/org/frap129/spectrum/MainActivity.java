package org.frap129.spectrum;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.util.List;
import java.util.Objects;

import eu.chainfire.libsuperuser.Shell;

import static org.frap129.spectrum.Utils.KPM;
import static org.frap129.spectrum.Utils.checkSupport;
import static org.frap129.spectrum.Utils.cpuScalingGovernorPath;
import static org.frap129.spectrum.Utils.finalGov;
import static org.frap129.spectrum.Utils.getCustomDesc;
import static org.frap129.spectrum.Utils.kernelProp;
import static org.frap129.spectrum.Utils.kpmFinal;
import static org.frap129.spectrum.Utils.kpmPath;
import static org.frap129.spectrum.Utils.kpmPropPath;
import static org.frap129.spectrum.Utils.listToString;
import static org.frap129.spectrum.Utils.notTunedGov;
import static org.frap129.spectrum.Utils.profileProp;
import static org.frap129.spectrum.Utils.setProfile;
import static org.frap129.spectrum.Utils.deviceProp;
import static org.frap129.spectrum.Utils.androProp;
import static org.frap129.spectrum.Utils.socProp;

public class MainActivity extends AppCompatActivity {

    private CardView oldCard;
    private List<String> suResult = null;
    private List<String> suResult1 = null;
    private List<String> suResult2 = null;
    private List<String> suResult3 = null;
    private int notaneasteregg = 0;
    private static final int PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Define existing CardViews
        final CardView card0 = (CardView) findViewById(R.id.card0);
        final CardView card1 = (CardView) findViewById(R.id.card1);
        final CardView card2 = (CardView) findViewById(R.id.card2);
        final CardView card3 = (CardView) findViewById(R.id.card3);
        final int balColor = ContextCompat.getColor(this, R.color.colorAutomatic);
        final int perColor = ContextCompat.getColor(this, R.color.colorBalance);
        final int batColor = ContextCompat.getColor(this, R.color.colorBattery);
        final int gamColor = ContextCompat.getColor(this, R.color.colorGaming);

        // Check for Spectrum Support
        if (!checkSupport(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.no_spectrum_support_dialog_title))
                    .setMessage(getString(R.string.no_spectrum_support_dialog_message))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .show();
            return;
        }

        // Ensure root access
        if (!Utils.checkSU()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.no_root_detected_dialog_title))
                    .setMessage(getString(R.string.no_root_detected_dialog_message))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }


        String disabledProfiles = Utils.disabledProfiles();
        String[] profilesToDisable = disabledProfiles.split(",");
        for (String profile : profilesToDisable){
            switch (profile) {
                case "Automatic":
                    card0.setVisibility(View.GONE);
                    break;
                case "balance":
                    card1.setVisibility(View.GONE);
                    break;
                case "battery":
                    card2.setVisibility(View.GONE);
                    break;
                case "gaming":
                    card3.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

        // Get profile descriptions
        getDesc();

        // Highlight current profile
        initSelected();

        // Set system property on click
        card0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            cardClick(card0, 0, balColor);
                if (notaneasteregg == 1) {
                    notaneasteregg++;
                } else {
                    notaneasteregg = 0;
                }
            }
        });

        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardClick(card1, 1, perColor);
                if (notaneasteregg == 3) {
                    Intent intent = new Intent(MainActivity.this, ProfileLoaderActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    notaneasteregg = 0;
                }
            }
        });

        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardClick(card2, 2, batColor);
                if (notaneasteregg == 2) {
                    notaneasteregg++;
                } else {
                    notaneasteregg = 0;
                }
            }
        });

        card3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardClick(card3, 3, gamColor);
                notaneasteregg = 1;
            }
        });
        
        CardView textup = (CardView)findViewById(R.id.textup);
        textup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://t.me/NexusKernel";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }

    // Method that detects the selected profile on launch
    private void initSelected() {
        SharedPreferences profile = this.getSharedPreferences("profile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = profile.edit();

        if(KPM) {
            suResult = Shell.SU.run(String.format("cat %s", kpmPath));
        } else {
            suResult = Shell.SU.run(String.format("getprop %s", profileProp));
        }

        if (suResult != null) {
            String result = listToString(suResult);

            if(result.contains("-1")) {
                // Default KPM value, just in case
            } else if (result.contains("0")) {
                CardView card0 = (CardView) findViewById(R.id.card0);
                int balColor = ContextCompat.getColor(this, R.color.colorAutomatic);
                card0.setCardBackgroundColor(balColor);
                oldCard = card0;
                editor.putString("profile", "automatic");
                editor.apply();
            } else if (result.contains("1")) {
                CardView card1 = (CardView) findViewById(R.id.card1);
                int perColor = ContextCompat.getColor(this, R.color.colorBalance);
                card1.setCardBackgroundColor(perColor);
                oldCard = card1;
                editor.putString("profile", "balance");
                editor.apply();
            } else if (result.contains("2")) {
                CardView card2 = (CardView) findViewById(R.id.card2);
                int batColor = ContextCompat.getColor(this, R.color.colorBattery);
                card2.setCardBackgroundColor(batColor);
                oldCard = card2;
                editor.putString("profile", "battery");
                editor.apply();
            } else if (result.contains("3")) {
                CardView card3 = (CardView) findViewById(R.id.card3);
                int gamColor = ContextCompat.getColor(this, R.color.colorGaming);
                card3.setCardBackgroundColor(gamColor);
                oldCard = card3;
                editor.putString("profile", "gaming");
                editor.apply();
            } else {
                editor.putString("profile", "custom");
                editor.apply();
            }
        }
    }

    // Method that reads and sets profile descriptions
    private void getDesc() {
        TextView desc0 = (TextView) findViewById(R.id.desc0);
        TextView desc1 = (TextView) findViewById(R.id.desc1);
        TextView desc2 = (TextView) findViewById(R.id.desc2);
        TextView desc3 = (TextView) findViewById(R.id.desc3);
        TextView desc5 = (TextView) findViewById(R.id.desc5);
        TextView desc6 = (TextView) findViewById(R.id.desc6);
        TextView desc7 = (TextView) findViewById(R.id.desc7);
        String balDesc;
        String kernel;

        if(KPM){
            suResult = Shell.SU.run(String.format("cat %s", kpmPropPath));
        } else {
            suResult = Shell.SU.run(String.format("getprop %s", kernelProp));
        }
        kernel = listToString(suResult);
        if (kernel.isEmpty())
            return;
        balDesc = desc0.getText().toString();
        balDesc = balDesc.replaceAll("\\bElectron\\b", kernel);
        desc0.setText(balDesc);
        
        suResult1 = Shell.SU.run(String.format("getprop %s", deviceProp));
        suResult2 = Shell.SU.run(String.format("getprop %s", androProp));
        suResult3 = Shell.SU.run(String.format("getprop %s", socProp));
        
        String kernel1 = listToString(suResult1);
        if (kernel1.isEmpty())
            return;
        String device = desc5.getText().toString();
        device = device.replaceAll("\\bdevi\\b", kernel1);
        desc5.setText(device);

        String kernel2 = listToString(suResult2);
        if (kernel2.isEmpty())
            return;
        String andro = desc6.getText().toString();
        andro = andro.replaceAll("\\bandro\\b", kernel2);
        desc6.setText(andro);
        
        String kernel3 = listToString(suResult3);
        if (kernel3.isEmpty())
            return;
        String soc = desc7.getText().toString();
        soc = soc.replaceAll("\\bsoc\\b", kernel3);
        desc7.setText(soc);

        if (Utils.supportsCustomDesc()){
            if(!Objects.equals(getCustomDesc("automatic"), "fail")) desc0.setText(getCustomDesc("automatic"));
            if(!Objects.equals(getCustomDesc("balance"), "fail")) desc1.setText(getCustomDesc("balance"));
            if(!Objects.equals(getCustomDesc("battery"), "fail")) desc2.setText(getCustomDesc("battery"));
            if(!Objects.equals(getCustomDesc("gaming"), "fail")) desc3.setText(getCustomDesc("gaming"));
        }
    }

    // Method that completes card onClick tasks
    private void cardClick(CardView card, int prof, int color) {
        if (oldCard != card) {
            ColorStateList ogColor = card.getCardBackgroundColor();
            card.setCardBackgroundColor(color);
            if (oldCard != null)
                oldCard.setCardBackgroundColor(ogColor);
            setProfile(prof);
            if (KPM) {
                Shell.SU.run(String.format("echo %s > %s", notTunedGov, cpuScalingGovernorPath));
                finalGov = listToString(Shell.SU.run(String.format("cat %s", kpmFinal)));
                Shell.SU.run(String.format("echo %s > %s", finalGov, cpuScalingGovernorPath));
            }
            oldCard = card;
            SharedPreferences profile = this.getSharedPreferences("profile", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = profile.edit();
            editor.putString("profile", String.valueOf(prof));
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences first = this.getSharedPreferences("firstFind", Context.MODE_PRIVATE);
        if (!first.getBoolean("firstFind", true)) {
            getMenuInflater().inflate(R.menu.nav, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.custom_profile:
                Intent i = new Intent(this, ProfileLoaderActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        if(Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
                            if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                this.recreate();
                            } else {
                                Toast.makeText(this, R.string.custom_descriptions_fail_text, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
    }

}

