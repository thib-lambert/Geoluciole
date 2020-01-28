package com.univlr.geoluciole;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.univlr.geoluciole.model.UserPreferences;

import java.util.Date;

public class RGPDConsentementGPSActivity extends AppCompatActivity {

    private CheckBox consentementCheckbox;
    private Button validate_button;
    private Button refused_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgpd_consentement_gps);

        this.consentementCheckbox = findViewById(R.id.rgpd_first_content_consentement_checkbox);
        this.refused_button = findViewById(R.id.rgpd_gps_consent_refused);
        this.validate_button = findViewById(R.id.rgpd_first_validate_button);

        this.consentementCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean consent = consentementCheckbox.isChecked();
                if (consent) {
                    validate_button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    refused_button.setBackgroundColor(getResources().getColor(R.color.colorDisabled));
                } else {
                    validate_button.setBackgroundColor(getResources().getColor(R.color.colorDisabled));
                    refused_button.setBackgroundColor(getResources().getColor(R.color.colorRefused));
                }
            }
        });

        this.validate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean consent = consentementCheckbox.isChecked();
                if (!consent) {
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), RGPDConsentementFormActivity.class);

                // sauvegarde des préférences
                UserPreferences u = UserPreferences.getInstance(RGPDConsentementGPSActivity.this);
                u.setGpsConsent(true);
                u.setDateConsentementGPS(new Date().getTime());
                UserPreferences.storeInstance(RGPDConsentementGPSActivity.this, u);

                startActivity(intent);
                finish();
            }
        });

        this.refused_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean consent = consentementCheckbox.isChecked();
                if (consent) {
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("refused_consent", true);
                UserPreferences u = UserPreferences.getInstance(RGPDConsentementGPSActivity.this);
                u.setConsent(true);
                u.setGpsConsent(false);
                UserPreferences.storeInstance(RGPDConsentementGPSActivity.this, u);

                startActivity(intent);
                finish();
            }
        });
    }
}
