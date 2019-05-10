package com.mauritues.brume.bmtraffic;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mauritues.brume.bmtraffic.model.LocationRecording;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SessionVariables {

    public static HashMap<String, LatLng> Locations = new HashMap<>();
    public static String MAP_VIEW_KEY;

    public static List<LocationRecording>  storedRecordings = new ArrayList<>();

    public static String  PhoneVerificationId = "";
    public static PhoneAuthProvider.ForceResendingToken PhoneResendToken;

    public static PhoneAuthCredential PhoneCredential;

    public static FirebaseUser LoggedUser;

}
