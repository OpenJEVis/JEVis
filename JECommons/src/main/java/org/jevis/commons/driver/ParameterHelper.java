package org.jevis.commons.driver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.gson.GsonBuilder;
import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParameterHelper {

    private final DateTime lastReadout;
    private final DateTime currentTime;

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ParameterHelper.class);

    public ParameterHelper(DateTime lastReadout, DateTime currentTime) {
        this.lastReadout = lastReadout;
        this.currentTime = currentTime;
    }

    public String getNewPath(String path, JEVisSample parameterSample) {


        Optional<JEVisFile> parameterFile = getParameterFile(parameterSample);
        if (!parameterFile.isPresent()) return path;
        Map<VarFiller.Variable, VarFiller.VarFunction> parameterMap = getParamterMap(parameterFile.get());
        VarFiller varFiller = new VarFiller(path, parameterMap);
        return varFiller.getFilledURIString();

    }


    private Map<VarFiller.Variable, VarFiller.VarFunction> getParamterMap(JEVisFile jeVisFile) {


        String json = new String(jeVisFile.getBytes(), StandardCharsets.UTF_8);

        Gson gson = GsonBuilder.createDefaultBuilder().create();
        Type listType = new TypeToken<ArrayList<Parameter>>() {
        }.getType();
        List<Parameter> Parameters = gson.fromJson(json, listType);
        for (Parameter Parameter : Parameters) {
            Parameter.setLastTS(lastReadout);
            Parameter.setCurrentTS(currentTime);
        }

        Map<VarFiller.Variable, VarFiller.VarFunction> map = Parameters.stream().collect(Collectors.toMap(choice -> choice.getVariable(), choice -> choice));

        return map;


    }


    private Optional<JEVisFile> getParameterFile(JEVisSample jeVisSample) {
        try {
            if (jeVisSample == null) return Optional.empty();
            return Optional.of(jeVisSample.getValueAsFile());
        } catch (Exception e) {
            logger.error(e);
        }
        return Optional.empty();
    }

}
