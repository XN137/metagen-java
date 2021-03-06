package io.virtdata.testing;

import ch.qos.logback.core.status.NopStatusListener;
import com.google.auto.service.AutoService;
import io.virtdata.api.DataMapperLibrary;
import io.virtdata.core.FunctionalDataMappingLibrary;
import io.virtdata.testing.functions.ARandomPOJO;

import java.util.ArrayList;
import java.util.List;

@AutoService(DataMapperLibrary.class)
public class TestableLibrary extends FunctionalDataMappingLibrary {
    NopStatusListener nop = new NopStatusListener();

    @Override
    public String getLibraryName() {
        return "fortesting";
    }

    @Override
    public List<Package> getSearchPackages() {
        return new ArrayList<Package>() {
            {
                add(ARandomPOJO.class.getPackage());
            }
        };
    }
}
