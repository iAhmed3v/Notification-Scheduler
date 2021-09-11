package com.ahmed3v.notificationscheduler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.SwitchCompat;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 0;

    private Button scheduledJob, cancelJob;
    private RadioGroup networkOptions;
    private AppCompatSeekBar seekBar;
    private TextView seekBarProgress;

    private JobScheduler mScheduler;

    // Switches for setting job options.
    private SwitchCompat mDeviceIdleSwitch;
    private SwitchCompat mDeviceChargingSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);

        seekBarProgress = findViewById(R.id.seekBar_progress);

        scheduledJob = findViewById(R.id.schedule_job);
        cancelJob = findViewById(R.id.cancel_job);

        mDeviceIdleSwitch = findViewById(R.id.idle_switch);
        mDeviceChargingSwitch = findViewById(R.id.charging_switch);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar , int progress , boolean fromUser) {

                if(progress > 0){

                    seekBarProgress.setText(progress + " s");

                }else {

                    seekBarProgress.setText(R.string.seekBar_progress_text);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        scheduledJob.setOnClickListener(v -> {

            int seekBarInteger = seekBar.getProgress();
            boolean seekBarSet = seekBarInteger > 0;

            mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

            networkOptions = findViewById(R.id.network_options);

            int selectedNetworkID = networkOptions.getCheckedRadioButtonId();

            int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;

            switch(selectedNetworkID) {

                case R.id.no_network:
                    selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                    break;

                case R.id.any_network:
                    selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                    break;

                case R.id.wifi_network:
                    selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                    break;
            }

            ComponentName serviceName = new ComponentName(getPackageName(), NotificationJobService.class.getName());

            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName);

            if(seekBarSet) {

                builder.setOverrideDeadline(seekBarInteger * 1000);
            }

            builder.setRequiredNetworkType(selectedNetworkOption);

            builder.setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked());
            builder.setRequiresCharging(mDeviceChargingSwitch.isChecked());

            boolean constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE)
                    || mDeviceIdleSwitch.isChecked()
                    || mDeviceChargingSwitch.isChecked()
                    || seekBarSet;


            if(constraintSet) {

                JobInfo myJobInfo = builder.build();

                mScheduler.schedule(myJobInfo);

                Toast.makeText(this , "Job Scheduled, job will run when " +
                        "the constraints are met.", Toast.LENGTH_SHORT).show();
            }else {

                Toast.makeText(this , "Please set at least one constraint" , Toast.LENGTH_SHORT).show();
            }

        });


        cancelJob.setOnClickListener(v -> {

            if(mScheduler != null){

                mScheduler.cancelAll();

                mScheduler = null;

                Toast.makeText(this , "Job cancelled" , Toast.LENGTH_SHORT).show();
            }
        });
    }
}