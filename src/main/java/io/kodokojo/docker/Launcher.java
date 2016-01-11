package io.kodokojo.docker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.kodokojo.docker.config.StandardModule;
import io.kodokojo.docker.service.source.RestEntryPoint;

public class Launcher {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new StandardModule());
        RestEntryPoint entryPoint = injector.getInstance(RestEntryPoint.class);
        entryPoint.start();
    }

}
