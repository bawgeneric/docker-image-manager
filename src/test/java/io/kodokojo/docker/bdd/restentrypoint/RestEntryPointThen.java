package io.kodokojo.docker.bdd.restentrypoint;

/*
 * #%L
 * docker-image-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tngtech.jgiven.Stage;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;

public class RestEntryPointThen<SELF extends RestEntryPointThen<?>> extends Stage<SELF> {



    protected ClientRestEntryPoint provideClientRestEntryPoint(String baseUrl) {
        Gson gson = new GsonBuilder().create();
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(baseUrl).setConverter(new GsonConverter(gson)).build();
        return restAdapter.create(ClientRestEntryPoint.class);
    }

    private interface ClientRestEntryPoint {

        @GET("/api")
        String apiVersion();

    }

}

