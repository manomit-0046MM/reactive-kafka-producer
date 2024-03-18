package com.bsolz.reactivekafkaproducer.utils;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

public class ConvertJsonToPojo {
    private static final Gson gson = new GsonBuilder().create();

    public static <T> T getFromJson(String json, Class<T> tClass) {
        return gson.fromJson(json, tClass);
    }
    public static <T> String toJson(T tClass) {
        return gson.toJson(tClass);
    }
}
