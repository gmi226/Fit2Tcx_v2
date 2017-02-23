package com.garmin.mwfit;

import com.garmin.fit.*;
import com.garmin.mwfit.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.util.ArrayList;

public class Fit2TcxV2 {

    private final static String SPACE_ACTIVITIES = "  ";                            //space*2
    private final static String SPACE_ACTIVITY = "    ";                            //space*4
    private final static String SPACE_LAP = "      ";                               //space*6
    private final static String SPACE_LAP_CONTENT = "          ";                   //space*10
    private final static String SPACE_TRACK = "        ";                           //space*8
    private final static String SPACE_TRACK_POINT = "          ";                   //space*10
    private final static String SPACE_TRACK_POINT_CONTENT = "            ";         //space*12
    private final static String SPACE_TRACK_POINT_CONTENT_DETAIL = "              ";//space*14

    static ArrayList<String> arrayXml = null;

    static String tempFirstTrackTime = "";
    static String tempLapStartTime = "";

    static String[] mArgs;

    static FitActivity fitActivity = null;
    static FitLap fitLap = null;
    static FitTrack fitTrack = null;
    static FitTrackPoint fitTrackPoint = null;
    static FitProduct fitProduct = null;

    public static void main( String[] args ) {

        if (fitActivity == null) {

            fitActivity = new FitActivity();
        }

        mArgs = args;
        Decode decode = new Decode();
        //decode.skipHeader();        // Use on streams with no header and footer (stream contains FIT defn and data messages only)
        //decode.incompleteStream();  // This suppresses exceptions with unexpected eof (also incorrect crc)
        MesgBroadcaster mesgBroadcaster = new MesgBroadcaster( decode );
        Listener listener = new Listener();
        FileInputStream in;

        System.out.printf( "FIT Decode Example Application - Protocol %d.%d Profile %.2f %s\n", Fit.PROTOCOL_VERSION_MAJOR, Fit.PROTOCOL_VERSION_MINOR, Fit.PROFILE_VERSION / 100.0, Fit.PROFILE_TYPE );

        if ( args.length != 1 ) {
            System.out.println( "Usage: java -jar DecodeExample.jar <filename>" );
            return;
        }

        try {
            in = new FileInputStream( args[0] );
        } catch ( java.io.IOException e ) {
            throw new RuntimeException( "Error opening file " + args[0] + " [1]" );
        }

        try {
            if ( !decode.checkFileIntegrity( (InputStream) in ) ) {
                throw new RuntimeException( "FIT file integrity failed." );
            }
        } catch ( RuntimeException e ) {
            System.err.print( "Exception Checking File Integrity: " );
            System.err.println( e.getMessage() );
            System.err.println( "Trying to continue..." );
        } finally {
            try {
                in.close();
            } catch ( java.io.IOException e ) {
                throw new RuntimeException( e );
            }
        }

        try {
            in = new FileInputStream( args[0] );
        } catch ( java.io.IOException e ) {
            throw new RuntimeException( "Error opening file " + args[0] + " [2]" );
        }

        mesgBroadcaster.addListener( (SportMesgListener) listener );
        mesgBroadcaster.addListener( (LapMesgListener) listener );
        mesgBroadcaster.addListener( (RecordMesgListener) listener );
        mesgBroadcaster.addListener( (SessionMesgListener) listener );
        mesgBroadcaster.addListener( (FileIdMesgListener) listener );

        try {
            decode.read( in, mesgBroadcaster, mesgBroadcaster );
        } catch ( FitRuntimeException e ) {
            // If a file with 0 data size in it's header  has been encountered,
            // attempt to keep processing the file
            if ( decode.getInvalidFileDataSize() ) {
                decode.nextFile();
                decode.read( in, mesgBroadcaster, mesgBroadcaster );
            } else {
                System.err.print( "Exception decoding file: " );
                System.err.println( e.getMessage() );

                try {
                    in.close();
                } catch ( java.io.IOException f ) {
                    throw new RuntimeException( f );
                }

                return;
            }
        }

        try {
            in.close();
        } catch ( java.io.IOException e ) {
            throw new RuntimeException( e );
        }

        System.out.println( "Decoded FIT file " + args[0] + "." );

        prepareXml();
        makeTcxFile();

        cleanFit();
    }

    private static void cleanFit() {

        fitActivity = null;
        fitLap = null;
        fitTrack = null;
        fitTrackPoint = null;
        arrayXml = null;
        tempFirstTrackTime = "";
        tempLapStartTime = "";
    }

    private static class Listener implements
            SportMesgListener,
            LapMesgListener,
            RecordMesgListener,
            SessionMesgListener,
            FileIdMesgListener {

        @Override
        public void onMesg( FileIdMesg mesg ) {

            if (fitProduct == null) {

                fitProduct = new FitProduct();
            }

            if ( mesg.getProduct() != null ) {

                fitProduct.setName(GarminProduct.getStringFromValue(mesg.getProduct()));
            }

            if ( mesg.getSerialNumber() != null ) {

                fitProduct.setUnitId(mesg.getSerialNumber().toString());
            }

            if ( mesg.getNumber() != null ) {

                fitProduct.setProductId(mesg.getNumber().toString());
            }
        }

        @Override
        public void onMesg( RecordMesg mesg ) {

            if (fitTrack == null) {

                fitTrack = new FitTrack();
            }

            if (fitTrackPoint == null) {

                fitTrackPoint = new FitTrackPoint();
            }

            parseMesg( mesg, RecordMesg.TimestampFieldNum );
            parseMesg( mesg, RecordMesg.PositionLatFieldNum );
            parseMesg( mesg, RecordMesg.PositionLongFieldNum );
            parseMesg( mesg, RecordMesg.AltitudeFieldNum );
            parseMesg( mesg, RecordMesg.DistanceFieldNum );
            parseMesg( mesg, RecordMesg.HeartRateFieldNum );
            parseMesg( mesg, RecordMesg.CadenceFieldNum );
            parseMesg( mesg, RecordMesg.SpeedFieldNum );

            addTrackPoint();
        }

        @Override
        public void onMesg(LapMesg mesg) {

            if (fitLap == null) {

                fitLap = new FitLap();
            }

            parseMesg( mesg, LapMesg.StartTimeFieldNum);
            parseMesg( mesg, LapMesg.TotalTimerTimeFieldNum);
            parseMesg( mesg, LapMesg.TotalDistanceFieldNum);
            parseMesg( mesg, LapMesg.MaxSpeedFieldNum);
            parseMesg( mesg, LapMesg.TotalCaloriesFieldNum);
            parseMesg( mesg, LapMesg.AvgHeartRateFieldNum);
            parseMesg( mesg, LapMesg.MaxHeartRateFieldNum);
            parseMesg( mesg, LapMesg.IntensityFieldNum);
            parseMesg( mesg, LapMesg.AvgCadenceFieldNum);
            parseMesg( mesg, LapMesg.LapTriggerFieldNum);

            addLap();
        }

        @Override
        public void onMesg(SportMesg mesg) {

            fitActivity.setSport(Sport.parseSportToXml(mesg.getSport()));
        }

        @Override
        public void onMesg(SessionMesg mesg) {

            fitActivity.setSport(Sport.parseSportToXml(mesg.getSport()));
        }

        private void parseMesg(Mesg mesg, int fieldNum) {

            Iterable<FieldBase> fields = mesg.getOverrideField( (short) fieldNum );
            Field profileField = Factory.createField( mesg.getNum(), fieldNum );

            if ( profileField == null ) {
                return;
            }

            for ( FieldBase field : fields ) {

                if ( field instanceof Field ) {

                    String value = field.getValue().toString();
                    switch (profileField.getName()) {
                        //Lap
                        case Fit.TAG_LAP_START_TIME:

                            if (tempLapStartTime == "") {

                                tempLapStartTime = tempFirstTrackTime;
                            }

                            fitLap.setStartTime(tempLapStartTime);
                            tempLapStartTime = mesg.getTimeStampNew();
                            break;
                        case Fit.TAG_LAP_TOTAL_TIME:

                            fitLap.setTotalTimeSeconds(value);
                            break;
                        case Fit.TAG_LAP_CALORIES:

                            fitLap.setCalories(value);
                            break;
                        case Fit.TAG_LAP_MAX_SPEED:

                            fitLap.setMaximumHeartRateBpm(value);
                            break;
                        case Fit.TAG_LAP_AVG_HEART:

                            fitLap.setAverageHeartRateBpm(value);
                            break;
                        case Fit.TAG_LAP_MAX_HEART:

                            fitLap.setMaximumHeartRateBpm(value);
                            break;
                        case Fit.TAG_LAP_INTENSITY:

                            fitLap.setIntensity(value);
                            break;
                        case Fit.TAG_LAP_TRIGGER:

                            fitLap.setTriggerMethod(LapTrigger.parseTriggerToXml(((LapMesg) mesg).getLapTrigger()));
                            break;

                        //TrackPoint
                        case Fit.TAG_RECORD_TIME_STAMP:

                            if (tempFirstTrackTime == "") {

                                tempFirstTrackTime = mesg.getTimeStampNew();
                            }
                            fitTrackPoint.setTime(mesg.getTimeStampNew());
                            break;
                        case Fit.TAG_LATITUDE:

                            fitTrackPoint.setLatitudeDegrees(mesg.getDegrees(field.getValue()));
                            break;
                        case Fit.TAG_LONGITUDE:

                            fitTrackPoint.setLongitudeDegrees(mesg.getDegrees(field.getValue()));
                            break;
                        case Fit.TAG_RECORD_SPEED:

                            fitTrackPoint.setSpeed(value);
                            break;
                        case Fit.TAG_ALTITUDE:

                            fitTrackPoint.setAltitudeMeters(value);
                            break;
                        case Fit.TAG_RECORD_HEART:

                            fitTrackPoint.setHeartRateBpm(value);
                            break;

                        //Common
                        case Fit.TAG_DISTANCE:
                            if (mesg instanceof LapMesg) {

                                if (Double.parseDouble(value) == 0) {

                                    value = fitTrack.getFitTrackPoints().get(fitTrack.getFitTrackPoints().size()-1).getDistanceMeters();
                                }
                                fitLap.setDistanceMeters(value);
                            }else {

                                fitTrackPoint.setDistanceMeters(value);
                            }
                            break;
                        case Fit.TAG_CADENCE:
                            if (mesg instanceof LapMesg) {

                                fitLap.setCadence(value);
                            }else {

                                fitTrackPoint.setCadence(value);
                            }
                            break;
                        default:
                            break;
                    }
                }else {

                    String value = field.getValue().toString();
                    switch (profileField.getName()) {
                        case Fit.TAG_RECORD_SPEED:

                            fitTrackPoint.setSpeed(value);
                            break;
                        case Fit.TAG_DISTANCE:
                            if (mesg instanceof RecordMesg) {

                                fitTrackPoint.setDistanceMeters(value);
                            }
                            break;
                        case Fit.TAG_CADENCE:
                            if (mesg instanceof RecordMesg) {

                                fitTrackPoint.setCadence(value);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private static void addLap() {

        if (fitLap != null) {

            if (fitTrack != null) {

                fitLap.setFitTrack(fitTrack);
            }
            fitActivity.addLap(fitLap);
        }
        fitTrack = null;
        fitLap = null;
    }

    private static void addTrackPoint() {

        if (fitTrack == null) {

            fitTrack = new FitTrack();
        }
        if (fitTrackPoint != null) {

            fitTrack.addFitTrackPoint(fitTrackPoint);
        }
        fitTrackPoint = null;
    }

    private static void addIntoXml(String data) {

        if (arrayXml == null) {

            arrayXml = new ArrayList<String>();
        }
        arrayXml.add(data+"\n");
    }

    private static void prepareXml() {

        addIntoXml(Fit.TAG_XML_HEADER);
        addIntoXml("<" + Fit.TAG_TRAINING_CENTER + "\n" + Fit.TAG_TRAINING_CENTER_XMLNS + ">");
        //space*2
        addIntoXml(SPACE_ACTIVITIES+"<" + Fit.TAG_ACTIVITIES + ">");

        if (fitActivity != null) {

            String sportMode = fitActivity.getSport();

            addIntoXml(SPACE_ACTIVITY + "<" + Fit.TAG_ACTIVITY + " " + Fit.TAG_SPORT + "=\"" + sportMode + "\">");
            addIntoXml(SPACE_LAP+"<" + Fit.TAG_ID + ">" + tempFirstTrackTime + "</" + Fit.TAG_ID + ">");

            ArrayList<FitLap> laps = fitActivity.getFitLaps();
            if (laps != null) {

                String lapStartTime;
                String lapTotalTimeSeconds;
                String lapDistanceMeters;
                String lapCalories;
                String lapAverageHeartRateBpm;
                String lapMaximumHeartRateBpm;
                String lapIntensity;
                String lapCadence;
                String lapTriggerMethod;
                FitTrack lapFitTrack;

                for (FitLap lap : laps) {

                    lapStartTime = lap.getStartTime();
                    lapTotalTimeSeconds = lap.getTotalTimeSeconds();
                    lapDistanceMeters = lap.getDistanceMeters();
                    lapCalories = lap.getCalories();
                    lapAverageHeartRateBpm = lap.getAverageHeartRateBpm();
                    lapMaximumHeartRateBpm = lap.getMaximumHeartRateBpm();
                    lapIntensity = lap.getIntensity();
                    lapCadence = lap.getCadence();
                    lapTriggerMethod = lap.getTriggerMethod();
                    lapFitTrack = lap.getFitTrack();

                    if (lapStartTime != null && lapStartTime != ""
                            && lapTotalTimeSeconds != null && lapTotalTimeSeconds != ""
                            && lapDistanceMeters != null && lapDistanceMeters != "") {

                        addIntoXml(SPACE_LAP+"<" + Fit.TAG_LAP + " " + Fit.TAG_LAP_START_TIME + "=\"" + lapStartTime + "\">");
                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_LAP_TOTAL_TIME + ">" + lapTotalTimeSeconds + "</" + Fit.TAG_LAP_TOTAL_TIME + ">");
                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_DISTANCE + ">" + lapDistanceMeters + "</" + Fit.TAG_DISTANCE + ">");
                    }else {

                        //if lap startTime, lapTotalTimeSeconds, lapDistanceMeters has issue, give up lap
                        continue;
                    }

                    if (lapCalories != null && lapCalories != "") {

                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_LAP_CALORIES + ">" + lapCalories + "</" + Fit.TAG_LAP_CALORIES + ">");
                    }

                    if (lapAverageHeartRateBpm != null && lapAverageHeartRateBpm != "") {

                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_LAP_AVG_HEART + ">");
                        addIntoXml(SPACE_LAP_CONTENT + "<" + Fit.TAG_VALUE + ">" + lapAverageHeartRateBpm + "</" + Fit.TAG_VALUE + ">");
                        addIntoXml(SPACE_TRACK + "</" + Fit.TAG_LAP_AVG_HEART + ">");
                    }

                    if (lapMaximumHeartRateBpm != null && lapMaximumHeartRateBpm != "") {

                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_LAP_MAX_HEART + ">");
                        addIntoXml(SPACE_LAP_CONTENT + "<" + Fit.TAG_VALUE + ">" + lapMaximumHeartRateBpm + "</" + Fit.TAG_VALUE + ">");
                        addIntoXml(SPACE_TRACK + "</" + Fit.TAG_LAP_MAX_HEART + ">");
                    }

                    if (lapIntensity != null && lapIntensity != "") {

                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_LAP_INTENSITY + ">" + lapIntensity + "</" + Fit.TAG_LAP_INTENSITY + ">");
                    }

                    if (lapCadence != null && lapCadence != "") {

                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_CADENCE + ">" + lapCadence + "</" + Fit.TAG_CADENCE + ">");
                    }

                    if (lapTriggerMethod != null && lapTriggerMethod != "") {

                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_LAP_TRIGGER + ">" + lapTriggerMethod + "</" + Fit.TAG_LAP_TRIGGER + ">");
                    }

                    if (lapFitTrack != null) {

                        addIntoXml(SPACE_TRACK + "<" + Fit.TAG_TRACK + ">");
                        ArrayList<FitTrackPoint> trackPoints = lapFitTrack.getFitTrackPoints();
                        if (trackPoints != null) {

                            String trackPointTime;
                            String trackPointLatitudeDegrees;
                            String trackPointLongitudeDegrees;
                            String trackPointAltitudeMeters;
                            String trackPointDistanceMeters;
                            String trackPointHeartRateBpm;
                            String trackPointCadence;
                            String trackPointSpeed;

                            String preDistance = null;

                            for(FitTrackPoint trackPoint : trackPoints) {

                                trackPointTime = trackPoint.getTime();
                                trackPointLatitudeDegrees = trackPoint.getLatitudeDegrees();
                                trackPointLongitudeDegrees = trackPoint.getLongitudeDegrees();
                                trackPointAltitudeMeters = trackPoint.getAltitudeMeters();
                                trackPointDistanceMeters = trackPoint.getDistanceMeters();
                                trackPointHeartRateBpm = trackPoint.getHeartRateBpm();
                                trackPointCadence = trackPoint.getCadence();
                                trackPointSpeed = trackPoint.getSpeed();

                                if (sportMode.equals("Swimming")) {

                                    if (trackPointDistanceMeters == null
                                            || trackPointDistanceMeters.equals("")) {

                                        continue;
                                    }
                                    if (preDistance != null
                                            && preDistance.equals(trackPointDistanceMeters)) {

                                        continue;
                                    }
                                    preDistance = trackPointDistanceMeters;
                                }

                                addIntoXml(SPACE_TRACK_POINT + "<" + Fit.TAG_TRACK_POINT + ">");
                                if (trackPointTime != null && trackPointTime != "") {

                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "<" + Fit.TAG_RECORD_TIME_STAMP + ">" + trackPointTime + "</" + Fit.TAG_RECORD_TIME_STAMP + ">");
                                }

                                if (trackPointLatitudeDegrees != null && trackPointLatitudeDegrees != ""
                                        && trackPointLongitudeDegrees != null && trackPointLongitudeDegrees != "") {

                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "<" + Fit.TAG_POSITION + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT_DETAIL + "<" + Fit.TAG_LATITUDE + ">" + trackPointLatitudeDegrees + "</" + Fit.TAG_LATITUDE + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT_DETAIL + "<" + Fit.TAG_LONGITUDE + ">" + trackPointLongitudeDegrees + "</" + Fit.TAG_LONGITUDE + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "</" + Fit.TAG_POSITION + ">");
                                }

                                if (trackPointAltitudeMeters != null && trackPointAltitudeMeters != "") {

                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "<" + Fit.TAG_ALTITUDE + ">" + trackPointAltitudeMeters + "</" + Fit.TAG_ALTITUDE + ">");
                                }

                                if (trackPointDistanceMeters != null && trackPointDistanceMeters != "") {

                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "<" + Fit.TAG_DISTANCE + ">" + trackPointDistanceMeters + "</" + Fit.TAG_DISTANCE + ">");
                                }

                                if (trackPointHeartRateBpm != null && trackPointHeartRateBpm != "") {

                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "<" + Fit.TAG_RECORD_HEART + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT_DETAIL + "<" + Fit.TAG_VALUE + ">" + trackPointHeartRateBpm + "</" + Fit.TAG_VALUE + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "</" + Fit.TAG_RECORD_HEART + ">");
                                }

                                if (trackPointCadence != null && trackPointCadence != "") {

                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "<" + Fit.TAG_CADENCE + ">" + trackPointCadence + "</" + Fit.TAG_CADENCE + ">");
                                }

                                if (trackPointSpeed != null && trackPointSpeed != "") {

                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "<" + Fit.TAG_EXTENSIONS + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT_DETAIL + "<" + Fit.TAG_TPX + Fit.TAG_TPX_XMLNS + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT_DETAIL + "  <" + Fit.TAG_RECORD_SPEED + ">" + trackPointSpeed + "</" + Fit.TAG_RECORD_SPEED + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT_DETAIL + "</" + Fit.TAG_TPX + ">");
                                    addIntoXml(SPACE_TRACK_POINT_CONTENT + "</" + Fit.TAG_EXTENSIONS + ">");
                                }

                                addIntoXml(SPACE_TRACK_POINT + "</" + Fit.TAG_TRACK_POINT + ">");
                            }
                        }
                        addIntoXml(SPACE_TRACK + "</" + Fit.TAG_TRACK + ">");
                    }
                    addIntoXml(SPACE_LAP + "</" + Fit.TAG_LAP + ">");
                }
            }

            if (fitProduct != null) {

                addIntoXml(SPACE_LAP + "<" + Fit.TAG_CREATOR + Fit.TAG_CREATOR_HEADER + ">");
                String name = fitProduct.getName();
                String unitId = fitProduct.getUnitId();
                String productId = fitProduct.getProductId();
                String versionMajor = fitProduct.getVersionMajor();
                String versionMinor = fitProduct.getVersionMinor();
                if (name != null && name != "") {

                    addIntoXml(SPACE_TRACK + "<" + Fit.TAG_CREATOR_NAME + ">" + name + "</" + Fit.TAG_CREATOR_NAME + ">");
                }
                if (unitId != null && unitId != "") {

                    addIntoXml(SPACE_TRACK + "<" + Fit.TAG_CREATOR_UNIT_ID + ">" + unitId + "</" + Fit.TAG_CREATOR_UNIT_ID + ">");
                }
                if (productId != null && productId != "") {

                    addIntoXml(SPACE_TRACK + "<" + Fit.TAG_CREATOR_PRODUCT_ID + ">" + productId + "</" + Fit.TAG_CREATOR_PRODUCT_ID + ">");
                }
                if (versionMajor != null || versionMinor != null) {

                    addIntoXml(SPACE_TRACK + "<" + Fit.TAG_CREATOR_VERSION + ">");
                    if (versionMajor != null && versionMajor != "") {

                        addIntoXml(SPACE_TRACK_POINT + "<" + Fit.TAG_CREATOR_VERSION_MAJOR + ">" + versionMajor + "</" + Fit.TAG_CREATOR_VERSION_MAJOR + ">");
                    }
                    if (versionMinor != null && versionMinor != "") {

                        addIntoXml(SPACE_TRACK_POINT + "<" + Fit.TAG_CREATOR_VERSION_MINOR + ">" + versionMinor + "</" + Fit.TAG_CREATOR_VERSION_MINOR + ">");
                    }
                    addIntoXml(SPACE_TRACK + "</" + Fit.TAG_CREATOR_VERSION + ">");
                }
                addIntoXml(SPACE_LAP + "</" + Fit.TAG_CREATOR + ">");
            }
            addIntoXml(SPACE_ACTIVITY + "</" + Fit.TAG_ACTIVITY + ">");
        }
        addIntoXml(SPACE_ACTIVITIES + "</" + Fit.TAG_ACTIVITIES + ">");
        addIntoXml("</" + Fit.TAG_TRAINING_CENTER + ">");
    }

    private static void makeTcxFile() {

        try {

            String fileName = mArgs[0].split("\\.")[0]+".tcx";
            File file = new File(fileName);

            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter streamWriter = new OutputStreamWriter(fOut, "UTF-8");

            for (String data : arrayXml) {

                streamWriter.append(data);
            }
            streamWriter.flush();
            streamWriter.close();
            fOut.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}