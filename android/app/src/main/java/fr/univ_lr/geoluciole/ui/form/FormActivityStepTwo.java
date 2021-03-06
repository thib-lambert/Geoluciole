/*
 * Copyright (c) 2020, Martin Allusse and Alexandre Baret and Jessy Barritault and Florian
 * Bertonnier and Lisa Fougeron and François Gréau and Thibaud Lambert and Antoine
 * Orgerit and Laurent Rayez
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *  Neither the name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fr.univ_lr.geoluciole.ui.form;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.jaredrummler.android.device.DeviceName;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;

import java.util.Calendar;
import java.util.Date;

import fr.univ_lr.geoluciole.R;
import fr.univ_lr.geoluciole.model.UserPreferences;
import fr.univ_lr.geoluciole.model.form.FormModel;
import fr.univ_lr.geoluciole.utils.Time;

public class FormActivityStepTwo extends AppCompatActivity {
    private static final String TAG = FormActivityStepTwo.class.getSimpleName();

    private static final String STEP_ANONYMOUS = "1/3";
    private static final String FORM = "Form";

    // variables dates et heures
    @Order(1)
    @NotEmpty(messageResId = R.string.form_err_required)
    private EditText txtDateArrivee;
    @Order(2)
    @NotEmpty(messageResId = R.string.form_err_required)
    private EditText txtDateDepart;

    // variables boutons dates et heures
    private Button btnDatePickerArrivee;
    private Button btnDatePickerDepart;

    // variables boutons precedent et suivant
    private Button btnPrevious;
    private Button btnContinue;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;

    private FormModel form;
    private Date dateDepart;
    private Date dateArrive;
    private Time timeDepart;
    private Time timeArrive;

    // validation
    private ValidationFormListener validatorListener;
    private Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_activity_step_two);
        // init éléments du form
        initUI();
        // set le form si defini
        formSetter();
        // desactive les keyboards des inputs
        disableKeyboardOnInputClick();
        // init listeners inputs
        initListenersInput();
        // init listeners boutons
        initListenersButtons();
        // init validation
        initValidatorListener();
    }

    /**
     * Méthode pour initialiser les éléments UI
     */
    private void initUI() {
        // title
        // variable title
        TextView title = findViewById(R.id.form_title);
        // step
        // variable step
        TextView step = findViewById(R.id.form_step);
        if (!UserPreferences.getInstance(FormActivityStepTwo.this).isAccountConsent()) {
            title.setText(R.string.form_title_anonym);
            step.setText(STEP_ANONYMOUS);
        }
        // date et heure arrivée boutons
        this.btnDatePickerArrivee = findViewById(R.id.btn_in_date);
        // date arrivée input
        this.txtDateArrivee = findViewById(R.id.in_date);
        // date et heure de départ boutons
        this.btnDatePickerDepart = findViewById(R.id.btn_out_date);
        // date départ input
        this.txtDateDepart = findViewById(R.id.out_date);
        // bouton précédent
        this.btnPrevious = findViewById(R.id.btn_prev);
        // cacher le bouton precedent si pas de consentement
        if (!UserPreferences.getInstance(FormActivityStepTwo.this).isAccountConsent()) {
            this.btnPrevious.setVisibility(View.INVISIBLE);
        }
        // bouton suivant
        this.btnContinue = findViewById(R.id.btn_next);
    }

    /**
     * Méthode permettant de gérer le formulaire
     */
    private void formSetter() {
        form = (FormModel) getIntent().getSerializableExtra(FORM);
        if (form == null) {
            UserPreferences userPref = UserPreferences.getInstance(FormActivityStepTwo.this);
            form = new FormModel(userPref.getId());
        } else {
            this.dateArrive = form.getDateIn();
            this.timeArrive = form.getTimeIn();
            this.dateDepart = form.getDateOut();
            this.timeDepart = form.getTimeOut();
            if (this.dateDepart != null && dateArrive != null) {
                txtDateArrivee.setText(FormModel.datetimeToString(this.dateArrive, this.timeArrive));
                txtDateDepart.setText(FormModel.datetimeToString(this.dateDepart, this.timeDepart));
            }
            Log.i(TAG, "formSetter, récupération du form : " + form);
        }

        // récupération des infos du device
        DeviceName.with(FormActivityStepTwo.this).request(new DeviceName.Callback() {
            @Override
            public void onFinished(DeviceName.DeviceInfo info, Exception error) {
                String name = info.marketName;
                String model = info.model;
                form.setDevice(name + "|" + model);
            }
        });
    }

    /**
     * Méthode pour désactiver l'affichage des keyboard au clic sur les input
     */
    private void disableKeyboardOnInputClick() {
        this.txtDateArrivee.setInputType(EditorInfo.TYPE_NULL);
        this.txtDateDepart.setInputType(EditorInfo.TYPE_NULL);
    }

    /**
     * Méthode pour ajouter les listeners de DatePicker et Timepicker sur les inputs
     */
    private void initListenersInput() {
        this.txtDateArrivee.setOnClickListener(getAndSetTextDate(txtDateArrivee, false));
        this.txtDateDepart.setOnClickListener(getAndSetTextDate(txtDateDepart, true));
    }

    /**
     * Méthode pour ajouter les listeners
     */
    private void initListenersButtons() {
        // boutons listeners
        this.btnDatePickerArrivee.setOnClickListener(getAndSetTextDate(txtDateArrivee, false));
        this.btnDatePickerDepart.setOnClickListener(getAndSetTextDate(txtDateDepart, true));
        // bouton précédent
        this.btnPrevious.setOnClickListener(previousView());
        // bouton suivant
        this.btnContinue.setOnClickListener(getDateTimeData());
        Log.i(TAG, "initListenersButtons initialisés");

    }

    /**
     * Méthode initialisant le validateur
     */
    private void initValidatorListener() {
        validator = new Validator(FormActivityStepTwo.this);
        validatorListener = new ValidationFormListener(FormActivityStepTwo.this, FormActivityStepThree.class, form);
        validator.setValidationListener(validatorListener);
        TextWatcherListener textWatcherListener = new TextWatcherListener(this.validator);
        txtDateArrivee.addTextChangedListener(textWatcherListener);
        txtDateDepart.addTextChangedListener(textWatcherListener);
    }

    private void saveToForm() {
        form.setVersion(Build.VERSION.RELEASE);

        // depart
        if (dateDepart != null && timeDepart != null) {
            form.setDateOut(this.dateDepart);
            form.setTimeOut(this.timeDepart);
        }
        // arrive
        if (dateArrive != null && timeArrive != null) {
            form.setTimeIn(this.timeArrive);
            form.setDateIn(this.dateArrive);
        }
        Log.i(TAG, "saveToForm");
    }

    /**
     * Méthode récupérant les données dates et heures d'arrivée et départ de La Rochelle
     * de l'utilisateur, met à jour l'objet formulaire avec les données
     *
     * @return View.OnClickListener
     */
    private View.OnClickListener getDateTimeData() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToForm();
                // validation
                validatorListener.setRedirect(true);
                validator.validate();
                validatorListener.setRedirect(false);

            }

        };
    }

    /**
     * Listener pour le bouton précédent, retour sur la vue précédente
     *
     * @return View.OnClickListener
     */
    private View.OnClickListener previousView() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        };
    }

    /**
     * Méthode permettant de revenir sur la première vue
     */
    private void back() {
        if (UserPreferences.getInstance(FormActivityStepTwo.this).isAccountConsent()) {
            saveToForm();
            Intent intent = new Intent(getApplicationContext(),
                    FormActivityStepOne.class);
            intent.putExtra(FORM, form);
            startActivity(intent);
            overridePendingTransition(R.transition.trans_right_in, R.transition.trans_right_out);
            finish();
            Log.i(TAG, "back, première vue");

        } else {
            super.onBackPressed();
            Log.i(TAG, "back, quitte l'application");

        }
    }

    /**
     * Redéfinition de la méthode onBackPressed
     * afin de revenir sur le premier écran du formulaire si consentement
     * sinon réduit l'application
     */
    @Override
    public void onBackPressed() {
        back();
    }

    /**
     * Méthode récupérant les dates d'arrivée et départ
     * de l'utilisateur à La Rochelle
     *
     * @param text EditText à setter
     * @return View.OnClickListener
     */
    private View.OnClickListener getAndSetTextDate(final EditText text, final boolean depart) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(FormActivityStepTwo.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c.set(year, monthOfYear, dayOfMonth);
                        if (depart) {
                            dateDepart = c.getTime();
                        } else {
                            dateArrive = c.getTime();
                        }
                        openTimer(depart);
                    }
                }, mYear, mMonth, mDay);
                setBound(datePickerDialog.getDatePicker(), depart);
                datePickerDialog.show();
            }
        };
    }

    private void openTimer(final boolean depart) {
        int mMinute;
        int hours;
        final Calendar c = Calendar.getInstance();
        mMinute = c.get(Calendar.MINUTE);
        hours = c.get(Calendar.HOUR_OF_DAY);
        TimePickerDialog timePickerDialog = new TimePickerDialog(FormActivityStepTwo.this, 0, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (!depart) {
                    timeArrive = new Time(hourOfDay, minute);
                    txtDateArrivee.setText(FormModel.datetimeToString(dateArrive, timeArrive));
                } else {
                    timeDepart = new Time(hourOfDay, minute);
                    txtDateDepart.setText(FormModel.datetimeToString(dateDepart, timeDepart));
                }
            }
        }, hours, mMinute, true);
        timePickerDialog.show();
    }

    /**
     * Validation for datepicker
     *
     * @param datePicker datepicker
     * @param depart     is date for depart
     */
    private void setBound(DatePicker datePicker, boolean depart) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));

        if (depart && dateArrive != null) {
            cal.setTime(this.dateArrive);
            datePicker.setMinDate(cal.getTimeInMillis());
        } else if (!depart && dateDepart != null) {
            cal.setTime(this.dateDepart);
            datePicker.setMaxDate(cal.getTimeInMillis());
        }
    }
}
