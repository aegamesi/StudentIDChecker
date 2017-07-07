package com.aegamesi.studentidchecker.util;

import com.aegamesi.studentidchecker.models.RoomAssignmentCondition;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Collection;

import io.realm.RealmList;
import io.realm.RealmModel;

public class RealmListJsonAdapter {
    @FromJson
	public RealmList<RoomAssignmentCondition> fromJson(Collection<RoomAssignmentCondition> collection) {
        RealmList<RoomAssignmentCondition> realmList = new RealmList<>();
        realmList.addAll(collection);
        return realmList;
    }

    @ToJson
    public Collection<RoomAssignmentCondition> toJson(RealmList<RoomAssignmentCondition> realmList) {
        return realmList;
    }
}