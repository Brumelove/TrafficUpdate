package com.mauritues.brume.bmtraffic.serviceproviders;


import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mauritues.brume.bmtraffic.SessionVariables;
import com.mauritues.brume.bmtraffic.model.LocationRecording;

import javax.annotation.Nullable;

public class LocationRecordingServiceProvider {
    private FirebaseFirestore store;

    public LocationRecordingServiceProvider(FirebaseFirestore firestore) {
        if (store == null)
            store = firestore;
    }

    public void recordTraffic(LocationRecording locationRecording) {
        System.out.println("about to write");
        CollectionReference recordings = store.collection("recordings");
        recordings.add(locationRecording);
        retrieveAllRecordings();
    }

    public void retrieveAllRecordings() {


        /*DataRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        Log.e("Data", collectData(dataSnapshot.getChildren()).toString());
                        SessionVariables.storedRecordings = collectData(dataSnapshot.getChildren());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });*/
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            store.collection("recordings")
                    .whereEqualTo("userId", FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getUid()
                    ).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    // Handle errors
                    if (e != null) {
                        Log.w("Data Store", "onEvent:error", e);
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        SessionVariables.storedRecordings = queryDocumentSnapshots.toObjects(LocationRecording.class);
                        Log.e("Data", SessionVariables.storedRecordings.toString());
                    }
                }
            });
        }
    }


    /*private List<LocationRecording> collectData(Iterable<DataSnapshot>  data){
        ArrayList<LocationRecording> recordings = new ArrayList<>();

        //iterate through each record
        for (DataSnapshot dsp : data){
            recordings.add((LocationRecording)dsp.getValue());
        }

        return recordings;
    }*/
}

