package com.example.markerdemo;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileOperations {
    private static final String TAG = "FileOperations";
    FileOutputStream fos;
    FileInputStream fis;
    private static final String fileName = "datafile.txt";

    public void writeToFile(String data) {
        Log.d(TAG, "writeToFile: Writing to file" + data);

        //File file =  new File(directory, fileName);
        try {
            fos = new FileOutputStream(fileName, true);
            fos.write(data.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public String readFromFile() {
        Log.d(TAG, "readFromFile: Starting read from file");

        StringBuffer sbuffer = new StringBuffer();
        try {
            //fis = openFileInput(fileName);

            int i;
            while ((i = fis.read()) != -1) {
                sbuffer.append((char) i);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "readFromFile: Read from file " + sbuffer.toString());

        return sbuffer.toString();
    }

}
