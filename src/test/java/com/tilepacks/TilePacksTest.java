package com.tilepacks;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import net.runelite.http.api.RuneLiteAPI;
import org.junit.Before;

import javax.inject.Inject;

abstract class TilePacksTest {
    @Inject
    protected Gson gson;

    @Before
    public void injectDependencies() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Gson.class).toInstance(RuneLiteAPI.GSON);
            }
        }).injectMembers(this);
    }
}
