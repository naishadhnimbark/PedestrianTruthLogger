package com.example.lab_bngnssvv.pedestriantruthlogger;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    /*********Defining the Button and toggle button instances*********/
    private static Button button_spr,button_cts,button_through_point;
    ToggleButton toggleButton_stop_start;
    /****************************************************************/

    /*************************Global variables*****************************/
    private static boolean isExternal = false;
    boolean flag_permission = false,isSwitchOn = false,isThroughPoint = false;
    public String fileName;
    private int STORAGE_PERMISSION_CODE = 23;   //to make sure which API version is running on the physical device to make sure the need of runtime permissions
    private File file;
    GPSTracker gps; //object of class GPSTracker for location services
    double latitude,longitude;
    /***********************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_cts = (Button) findViewById(R.id.button_cts);
        button_spr = (Button) findViewById(R.id.button_spr);
        toggleButton_stop_start = (ToggleButton) findViewById(R.id.toggleButton_stop_start);
        button_through_point = (Button)findViewById(R.id.button_through_point);

        /******Disabling all other buttons*****/
        button_cts.setEnabled(false);
        toggleButton_stop_start.setEnabled(false);
        button_through_point.setEnabled(false);
        /**************************************/

        onButtonClickListener_spt();
        onButtonClickListener_cts();
        onButtonClickListener_through_point();
        onToggleButtonClickListener_stop_start();
    }

    /*****The function maintains the Start Pedestrian Route Button****
     * The button will create the File in internal or external storage
     * and also checks for the related permissions
     */
    private void onButtonClickListener_spt() {

        button_spr.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isReadStorageAllowed()){ //this will make sure the permissions are enabled or not
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Create new File ?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Handle Yes
                                            button_cts.setEnabled(true);
                                            button_through_point.setEnabled(true);
                                            toggleButton_stop_start.setEnabled(true);
                                            //switch_stop_start.setEnabled(true);

                                            boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                                            if (isSDPresent) {
                                                boolean bool;
                                                String path = Environment.getExternalStorageDirectory() + "/truth_logger";
                                                //Toast.makeText(getApplicationContext(), "Confirm file creation.", Toast.LENGTH_LONG).show();
                                                File dFolder = new File(path);
                                                dFolder.mkdirs();
                                                try {
                                                    bool = dFolder.createNewFile(); //Making sure that the destination folder is created successfully
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                fileName = new java.text.SimpleDateFormat("hh:mm:ss").format(new Date());   //assigning the filename according to the format given
                                                file = new File(path, fileName + ".txt");
                                                try {
                                                    bool = file.createNewFile();    //Making sure that the destination file is created successfully
                                                    if (bool) {
                                                        //Toast.makeText(getApplicationContext(), "File creation successful.", Toast.LENGTH_SHORT).show();
                                                        isExternal = true;
                                                    } else {

                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                //Toast.makeText(getApplicationContext(), "NO external storage.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Handle No
                                            //Toast.makeText(getApplicationContext(), "File creation Failed!!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .create();
                            builder.setTitle("Confirm File Creation");
                            builder.show();
                            button_spr.setEnabled(false);   //no need of file creation again once it is done.
                        }
                        else{
                            //Toast.makeText(MainActivity.this, "You dont have permissions, please allow!", Toast.LENGTH_SHORT).show();
                            requestStoragePermission(); //this will request the necessary permission for storage access, location and internet if not given to application
                        }
                    }
                }
        );

    }

    /*****The function maintains the Turn button*****
     * the button captures the location and UTC time stamp and saves it in
     * file created by the start pedestrian route button
     */
    private void onButtonClickListener_cts() {

        button_cts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getApplicationContext(), "entering Save function..", Toast.LENGTH_SHORT).show();
                        StringBuilder timeStamp_sb = new StringBuilder(100);
                        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        final String utcTime = sdf.format(new Date());
                        timeStamp_sb.append(utcTime);
                        timeStamp_sb.append(System.getProperty("line.separator"));  //to make sure that every time stamp comes in new line
                        String timeStamp = timeStamp_sb.toString();
                        Save(file, timeStamp);
                        //Toast.makeText(getApplicationContext(), "Finished..", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /*****The function maintains the Through Point button*****
     * the button captures the current location of the device
     * and storas in the file created by start pedestrian route
     */
    private void onButtonClickListener_through_point(){
        button_through_point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isThroughPoint = true;
                StringBuilder timeStamp_sb = new StringBuilder(10);
                timeStamp_sb.append(System.getProperty("line.separator"));  //to make sure that every time stamp comes in new line
                String timeStamp = timeStamp_sb.toString();
                Save(file,timeStamp);
                isThroughPoint = false;
            }
        });
    }

    /*****The function maintains the Stop start toggle button*****
     * the button captures the location and UTC time stamp associated with
     * Stop (Toggle or) click and Strat (Toggle or) click respectively
     */
    private void onToggleButtonClickListener_stop_start() {
        toggleButton_stop_start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                button_cts.setEnabled(false);
                button_through_point.setEnabled(false);
                if(isChecked){
                    StringBuilder timeStamp_sb = new StringBuilder(100);
                    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    final String utcTime = sdf.format(new Date());
                    timeStamp_sb.append(utcTime);
                    String timeStamp = timeStamp_sb.toString();
                    Save(file, timeStamp);
                }
                else{
                    isSwitchOn = true;
                    StringBuilder timeStamp_sb = new StringBuilder(100);
                    timeStamp_sb.append(",");
                    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    final String utcTime = sdf.format(new Date());
                    timeStamp_sb.append(utcTime);
                    timeStamp_sb.append(System.getProperty("line.separator"));
                    String timeStamp = timeStamp_sb.toString();
                    Save(file, timeStamp);
                    button_cts.setEnabled(true);
                    button_through_point.setEnabled(true);
                    isSwitchOn = false;
                }
            }
        });
    }

    /*
     * The function Saves the received data at file location
     * specified by "file"
     * @param file
     * @param data
     */
    public void Save(File file, String data) {
        if (!isExternal) {  //need to check the storage is external of internal
            try {
                File directory = new File("/storage/emulated/0/truth_logger");
                if (!directory.exists()) {
                    directory.mkdir();
                }

                File myFile = new File(directory, "myFile.txt");
                boolean bool = myFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(myFile);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fos);

                if(!isSwitchOn){
                    gps = new GPSTracker(MainActivity.this);
                    if (gps.canGetLocation) {
                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        //Toast.makeText(getApplicationContext(),"Lat: "+latitude+"\nLong: "+longitude,Toast.LENGTH_LONG).show();
                    } else {
                        gps.showSettingsAlert();
                    }
                    outputWriter.write("W,");
                    outputWriter.append(String.valueOf(latitude));
                    outputWriter.append(",");
                    outputWriter.append(String.valueOf(longitude));
                    if(!isThroughPoint)
                        outputWriter.append(",");
                }
                outputWriter.append(data);
                outputWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fos);
                if(!isSwitchOn){
                    gps = new GPSTracker(MainActivity.this);
                    if (gps.canGetLocation) {
                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        //Toast.makeText(getApplicationContext(),"Lat: "+latitude+"\nLong: "+longitude,Toast.LENGTH_LONG).show();
                    } else {
                        gps.showSettingsAlert();
                    }
                    outputWriter.write("W,");
                    outputWriter.append(String.valueOf(latitude));
                    outputWriter.append(",");
                    outputWriter.append(String.valueOf(longitude));
                    if(!isThroughPoint)
                        outputWriter.append(",");
                }
                outputWriter.append(data);
                outputWriter.close();
                outputWriter.append(data);
                outputWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /*
     *The function make sure that all the permission to access the storage,location and internet is enabled or not.
     * @return
     */
    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result_read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int result_write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //int result_internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

        //If permission is granted returning true we dont have get the permission for internet
        flag_permission = result_read == PackageManager.PERMISSION_GRANTED && result_write == PackageManager.PERMISSION_GRANTED ;//&& result_internet == PackageManager.PERMISSION_GRANTED;

        //If permission is not granted returning false
        return flag_permission;
    }

    /*
     * The function will request the permissions if teh permissions are not enabled
     */
    private void requestStoragePermission(){

        //if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
        //If the user has denied the permission previously your code will come to this block
        //Here you can explain why you need this permission
        //Explain here why you need this permission
        //}

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, STORAGE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, STORAGE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, STORAGE_PERMISSION_CODE);
    }

    /*
     * This method will be called when the user will tap on allow or deny
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                Toast.makeText(this,"Permission granted now you can read the storage",Toast.LENGTH_LONG).show();
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }
}
