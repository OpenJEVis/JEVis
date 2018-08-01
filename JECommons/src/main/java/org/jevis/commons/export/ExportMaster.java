package org.jevis.commons.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

import java.io.File;
import java.util.*;

public class ExportMaster {


    Map<String,MetaObject> metaObjects = new HashMap<>();
    Map<String,JsonRelationship> relationships = new HashMap<>();
    HashSet<String> structure = new HashSet<>();
    List<Integer> root=new ArrayList<>();



    List<Integer> backlist = Arrays.asList(new Integer[]{100,101,102,103,104,105});

    public void ExportMaster(File outputFile){

    }

    private String getKey(JsonRelationship rel){
        return rel.getFrom()+":"+rel.getTo();//rel.getType()+
    }

    public List<JsonObject> buildJson(List<JEVisObject> objs){
        List<JsonObject> result = new ArrayList<>();
        for(JEVisObject obj:objs){
            try {
                JsonObject jsonO =JsonFactory.buildObject(obj, false);
                jsonO.setAttributes(new ArrayList<>());
                for(JEVisAttribute att:obj.getAttributes()){
                    JsonAttribute jsonA= JsonFactory.buildAttribute(att);
                    jsonO.getAttributes().add(jsonA);

                    JEVisSample lastValue = att.getLatestSample();
                    if(lastValue !=null){
                        jsonA.setLatestValue(JsonFactory.buildSample(lastValue,att.getPrimitiveType()));
                    }
                }
                result.addAll(buildJson(obj.getChildren()));
                result.add(jsonO);

                List<JsonRelationship> jsonRels = JsonFactory.buildRelationship(obj.getRelationships());
                for(JsonRelationship rel:jsonRels){
                    if(rel.getType()==JEVisConstants.ObjectRelationship.PARENT){
                        structure.add(getKey(rel));
                    }
//                    if(!backlist.contains(rel.getType())){
////                        relationships.put(getKey(rel),rel);
//                        structure.add(getKey(rel));
//                    }

                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    public void setObjects(List<JsonObject> objs ){

        for(JsonObject obj:objs){
            MetaObject mo=new MetaObject(obj);
            metaObjects.put(mo.getKey(),mo);
        }

        validateObjects();


//        for(Map.Entry<String,MetaObject> entry:metaObjects.entrySet()){
////            validateObject(entry.getValue());
//        }


    }

    public void export(File outputfile){
        //zip Files...

        for(String key:structure){
            System.out.println("Rel: "+key);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for(Map.Entry<String,MetaObject> entry:metaObjects.entrySet()){
            String json = gson.toJson(entry.getValue());
            System.out.println(json);
        }

    }


    public boolean validateObjects(){
        System.out.println("Validate");
        for(String key:structure){
//            System.out.println("- key: "+key);
            String[] keys = key.split("s:");
            if(metaObjects.containsKey(keys[0])
                    && metaObjects.containsKey(keys[1])){
//                System.out.println("OK both keys exist");
            }else{
                System.out.println("NOK a key is missing for: "+key);
            }
        }

//        for(Map.Entry<String,MetaObject> entry:metaObjects.entrySet()){
//            if(metaObjects.containsKey(String.valueOf(entry.getValue().getObject().getParent()))){
//                System.out.println("Key: "+entry.getKey()+"  -> Valid Parent");
//            }else{
//                System.out.println("Key: "+entry.getKey()+"  -> NOT Valid Parent");
//            }
//        }

        //check if this obj has an valid parent
        return true;
    }


}
