package com.craftmend.openaudiomc.generic.rd.routes;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.rd.http.HttpResponse;
import com.craftmend.openaudiomc.generic.rd.http.Route;
import com.openaudiofabric.OpenAudioFabric;

import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

public class DefaultRoute extends Route {

    @Override
    public HttpResponse onRequest(NanoHTTPD.IHTTPSession session) {
        Map<String, Object> r = new HashMap<>();
        r.put("version", OpenAudioFabric.getInstance().getPluginVersion());
        r.put("info", "This server is running OpenAudioMc by Mindgamesnl. https://openaudiomc.net/");
        return HttpResponse.json(OpenAudioMc.getGson().toJson(r));
    }

}
