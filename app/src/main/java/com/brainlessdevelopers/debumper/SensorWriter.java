package com.brainlessdevelopers.debumper;

import java.io.File;

/**
 * Created by Fran on 3/9/2017.
 */
public class SensorWriter {
    private File outputFolder;
    private File accelerometerLog;

    public SensorWriter(File outputFolder, File accelerometerLog){
        this.outputFolder = outputFolder;
        this.accelerometerLog = accelerometerLog;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public File getAccelerometerLog() {
        return accelerometerLog;
    }

    public void setAccelerometerLog(File accelerometerLog) {
        this.accelerometerLog = accelerometerLog;
    }

}
