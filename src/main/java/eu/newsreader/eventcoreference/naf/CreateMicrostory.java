package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafEventRelation;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.input.FrameNetReader;
import eu.newsreader.eventcoreference.objects.NafMention;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;
import eu.newsreader.eventcoreference.objects.SemTime;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by piek on 2/4/15.
 */
public class CreateMicrostory {

    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/scale-test.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/reasoning/increase-example/57VV-5311-F111-G0HJ.xml_7684191f264a9e21af56de7ec51cf2d5.naf.coref";
        //String pathToNafFile = "/Users/piek/newsreader-deliverables/papers/maplex/47P9-DCM0-0092-K267.xml";
        //String pathToNafFile = "/Users/piek/Desktop/MapLex/47T0-YSP0-018S-20DV.xml";
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/47T0-B4V0-01D6-Y3WM.xml";
        // String pathToNafFile = "/Code/vu/newsreader/EventCoreference/example/naf_and_trig/5C37-HGT1-JBJ4-2472.xml_fb5a69273e6b8028fa2b9796eb62483b.naf";
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/47KD-4MN0-009F-S2JG.xml";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/47R9-0JG0-015B-31P6.xml";
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/4PG2-TTJ0-TXVX-P0FV.xml";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/47KD-4MN0-009F-S2JG.xml";
        String pathToNafFile = "/Users/piek/Desktop/NWR/Cross-lingual/test.srl.lexicalunits.pm.fn.ecoref.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NEDRerankedTest/51Y9-WY41-DYVC-J27G_reranked.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/possession-test.naf";
        //String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/example/razni11-01.event-coref.naf";
        //String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/fifa.naf";
        String project = "cars";

        boolean DOCTIME = true;
        boolean CONTEXTTIME = true;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-file") && args.length>(i+1)) {
                pathToNafFile = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                project = args[i+1];
            }

            else if (arg.equals("--no-doc-time")) {
                DOCTIME = false;
            }
            else if (arg.equals("--no-context-time")) {
                CONTEXTTIME = false;
            }
        }
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        boolean ADDITIONALROLES = true;
        GetSemFromNaf.processNafFile(project, kafSaxParser, semEvents, semActors, semTimes, semRelations, ADDITIONALROLES, DOCTIME, CONTEXTTIME);
        try {
            // System.out.println("semEvents = " + semEvents.size());
            String pathToTrigFile = pathToNafFile+".trig";
            OutputStream fos = new FileOutputStream(pathToTrigFile);
            JenaSerialization.serializeJena(fos,
                    semEvents, semActors, semTimes, semRelations, null, false, true);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static ArrayList<SemObject> getMicroEvents (Integer sentenceRange, ArrayList<SemObject> semEvents) {
        ArrayList<SemObject> microEvents = new ArrayList<SemObject>();
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent =  semEvents.get(i);
            for (int j = 0; j < semEvent.getNafMentions().size(); j++) {
                NafMention nafMention = semEvent.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    try {
                        Integer sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            if (!Util.hasObjectUri(microEvents, semEvent.getURI())) {
                                microEvents.add(semEvent);
                            }
                        }
                    } catch (NumberFormatException e) {
                        //  e.printStackTrace();
                    }
                }
            }
        }
        return microEvents;
    }

    static ArrayList<SemObject> getMicroActors (Integer sentenceRange, ArrayList<SemObject> semActors) {
        ArrayList<SemObject> microEvents = new ArrayList<SemObject>();
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor =  semActors.get(i);
            for (int j = 0; j < semActor.getNafMentions().size(); j++) {
                NafMention nafMention = semActor.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    try {
                        Integer sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            microEvents.add(semActor);
                        }
                    } catch (NumberFormatException e) {
                        //  e.printStackTrace();
                    }
                }
            }
        }
        return microEvents;
    }
     static ArrayList<SemObject> getMicroTimes (Integer sentenceRange, ArrayList<SemObject> semTimes) {
        ArrayList<SemObject> microTimes = new ArrayList<SemObject>();
         for (int i = 0; i < semTimes.size(); i++) {
             SemObject semTime =  semTimes.get(i);
             for (int j = 0; j < semTime.getNafMentions().size(); j++) {
                 NafMention nafMention = semTime.getNafMentions().get(j);
                 String sentenceId = nafMention.getSentence();
                 if (!sentenceId.isEmpty()) {
                     try {
                         Integer sentenceInteger = Integer.parseInt(sentenceId);
                         if (sentenceInteger <= sentenceRange) {
                             microTimes.add(semTime);
                         }
                     } catch (NumberFormatException e) {
                         //   e.printStackTrace();
                     }
                 }
             }
         }
        return microTimes;
    }

     static ArrayList<SemRelation> getMicroRelations (Integer sentenceRange, ArrayList<SemRelation> semRelations) {
        ArrayList<SemRelation> microRelations = new ArrayList<SemRelation>();
         for (int i = 0; i < semRelations.size(); i++) {
             SemRelation semRelation =  semRelations.get(i);
            // System.out.println("semRelation.getId() = " + semRelation.getId());
             for (int j = 0; j < semRelation.getNafMentions().size(); j++) {
                 NafMention nafMention = semRelation.getNafMentions().get(j);
                 String sentenceId = nafMention.getSentence();
                 if (!sentenceId.isEmpty()) {
                     Integer sentenceInteger = null;
                     try {
                         sentenceInteger = Integer.parseInt(sentenceId);
                         if (sentenceInteger <= sentenceRange) {
                             microRelations.add(semRelation);
                         }
                     } catch (NumberFormatException e) {
                         // e.printStackTrace();
                     }
                 }
             }
         }
        /*for (int i = 0; i < microRelations.size(); i++) {
             SemRelation microRelation = microRelations.get(i);
             System.out.println("microRelation.getId() = " + microRelation.getId());
         }*/
        return microRelations;
    }

    /**
     * Obtain events and participants through FN relations
     * @param semEvents
     * @param microSemEvents
     * @param frameNetReader
     */
    static void addEventsThroughFrameNetBridging (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  FrameNetReader frameNetReader
    ) {
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    if (frameNetReader.frameNetConnected(microEvent, event) ||
                            frameNetReader.frameNetConnected(event, microEvent)) {
                        if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                            microSemEvents.add(event);
                        }
                    }
                }
            }
        }
    }

    static ArrayList<SemObject> getEventsThroughFrameNetBridging (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  FrameNetReader frameNetReader
    ) { ArrayList<SemObject> fnRelatedEvents = new ArrayList<SemObject>();
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    if (frameNetReader.frameNetConnected(microEvent, event) ||
                            frameNetReader.frameNetConnected(event, microEvent)) {
                        if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                            !Util.hasObjectUri(fnRelatedEvents, event.getURI())) {
                            fnRelatedEvents.add(event);
                        }
                    }
                }
            }
        }
        return fnRelatedEvents;
    }


    public static ArrayList<JSONObject> getEventsThroughTopicBridging(ArrayList<JSONObject> events,
                                                                        JSONObject event,
                                                                      int topicThreshold) {
        ArrayList<JSONObject> topicEvents = new ArrayList<JSONObject>();
        boolean DEBUG = false;
        if (DEBUG) System.out.println("bridging");
        try {
            JSONArray topics = null;
            try {
                topics = (JSONArray) event.get("topics");
            } catch (JSONException e) {
              if (DEBUG)  e.printStackTrace();
            }
            if (topics!=null) {
                if (DEBUG) {
                    System.out.println("topics = " + topics.length());
                    System.out.println("topics.toString() = " + topics.toString());
                }
                for (int i = 0; i < events.size(); i++) {
                    JSONObject oEvent = events.get(i);
                    if (!oEvent.equals(event)) {
                        JSONArray oTopics = null;
                        try {
                            oTopics = (JSONArray) oEvent.get("topics");
                        } catch (JSONException e) {
                            if (DEBUG)   e.printStackTrace();
                        }
                        if (oTopics!=null) {
                            if (DEBUG) {
                                System.out.println("otopics = " + oTopics.length());
                                System.out.println("oTopics.toString() = " + oTopics.toString());
                            }
                            int sharedTopics = 0;
                            for (int j = 0; j < topics.length(); j++) {
                                String topic = (String) topics.get(j);
                                for (int k = 0; k < oTopics.length(); k++) {
                                    String oTopic = (String) oTopics.get(k);
                                    if (topic.equals(oTopic)) {
                                        sharedTopics++;
                                    }
                                }
                            }
                            int prop1 = sharedTopics*100/topics.length();
                            int prop2 = sharedTopics*100/oTopics.length();
                            if (DEBUG) {
                                if (sharedTopics > 0) {
                                    System.out.println("sharedTopics = " + sharedTopics);
                                    System.out.println("prop1 = " + prop1);
                                    System.out.println("prop2 = " + prop2);
                                }
                            }

                            if (prop1>=topicThreshold && prop2>= topicThreshold) {
                               // System.out.println("topicThreshold = " + topicThreshold);
                                if (!topicEvents.contains(oEvent)) {
/*
                                    System.out.println("prop1 = " + prop1);
                                    System.out.println("prop2 = " + prop2);
                                    System.out.println("oEvent = " + oEvent);
*/
                                    topicEvents.add(oEvent);
                                    //System.out.println("topicEvents = " + topicEvents.size());
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return topicEvents;
    }

    public static ArrayList<JSONObject> getEventsThroughFrameNetBridging(ArrayList<JSONObject> events,
                                                                        JSONObject event,
                                                                         FrameNetReader frameNetReader)
            throws JSONException {
        ArrayList<JSONObject> fnRelatedEvents = new ArrayList<JSONObject>();
        JSONArray superFrames = (JSONArray) event.get("fnsuperframes");
        if (superFrames!=null) {
            for (int j = 0; j < superFrames.length(); j++) {
                String frame = (String) superFrames.get(j);
                for (int i = 0; i < events.size(); i++) {
                    JSONObject oEvent = events.get(i);
                    if (!oEvent.equals(event)) {
                        JSONArray oSuperFrames = (JSONArray) oEvent.get("fnsuperframes");
                        for (int k = 0; k < oSuperFrames.length(); k++) {
                            String oFrame = (String) oSuperFrames.get(k);
                            if (frame.equals(oFrame)) {
                                if (!fnRelatedEvents.contains(oEvent)) {
                                    fnRelatedEvents.add(oEvent);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return fnRelatedEvents;
    }

    public static ArrayList<JSONObject> getEventsThroughEsoBridging(ArrayList<JSONObject> events,
                                                                        JSONObject event,
                                                                         FrameNetReader frameNetReader)
            throws JSONException {
        ArrayList<JSONObject> esoRelatedEvents = new ArrayList<JSONObject>();
        JSONArray superFrames = (JSONArray) event.get("esosuperclasses");
        if (superFrames!=null) {
            for (int j = 0; j < superFrames.length(); j++) {
                String frame = (String) superFrames.get(j);
                for (int i = 0; i < events.size(); i++) {
                    JSONObject oEvent = events.get(i);
                    if (!oEvent.equals(event)) {
                        JSONArray oSuperFrames = (JSONArray) oEvent.get("esosuperclasses");
                        for (int k = 0; k < oSuperFrames.length(); k++) {
                            String oFrame = (String) oSuperFrames.get(k);
                            if (frame.equals(oFrame)) {
                                if (!esoRelatedEvents.contains(oEvent)) {
                                    esoRelatedEvents.add(oEvent);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return esoRelatedEvents;
    }


    /**
     * Obtain events and participants through NAF event relations
     * @param semEvents
     * @param microSemEvents
     * @param kafSaxParser
     */
    static void addEventsThroughNafEventRelations (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  KafSaxParser kafSaxParser
    ) {
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            ArrayList<String> microPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    ArrayList<String> eventPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
                    for (int j = 0; j < kafSaxParser.kafTlinks.size(); j++) {
                        KafEventRelation kafEventRelation = kafSaxParser.kafTlinks.get(j);
                        if (microPredicateIds.contains(kafEventRelation.getFrom()) ||
                                eventPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                                microSemEvents.add(event);
                            }
                            break;
                        }
                        if (eventPredicateIds.contains(kafEventRelation.getFrom()) ||
                                microPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                                microSemEvents.add(event);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    static ArrayList<SemObject> getEventsThroughNafEventRelations (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  KafSaxParser kafSaxParser
    ) {
        ArrayList<SemObject> relatedEvents = new ArrayList<SemObject>();
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            ArrayList<String> microPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    ArrayList<String> eventPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
                    for (int j = 0; j < kafSaxParser.kafTlinks.size(); j++) {
                        KafEventRelation kafEventRelation = kafSaxParser.kafTlinks.get(j);
                        if (microPredicateIds.contains(kafEventRelation.getFrom()) &&
                                eventPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                                    !Util.hasObjectUri(relatedEvents, event.getURI())) {
                                relatedEvents.add(event);
                            }
                            break;
                        }
                        if (eventPredicateIds.contains(kafEventRelation.getFrom()) &&
                                microPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                                !Util.hasObjectUri(relatedEvents, event.getURI())) {
                                relatedEvents.add(event);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return relatedEvents;
    }


    /**
     * Obtain events and participants through bridging relations
     * @param semEvents
     * @param microSemEvents
     * @param microSemActors
     * @param semRelations
     */
    static void addEventsThroughCoparticipation(ArrayList<SemObject> semEvents,
                                                ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        for (int i = 0; i < microSemActors.size(); i++) {
            SemObject semObject = microSemActors.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getURI())) {
                    for (int k = 0; k < semEvents.size(); k++) {
                        SemObject event = semEvents.get(k);
                        if (event.getURI().equals(semRelation.getSubject())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                                microSemEvents.add(event);
                            }
                        }
                    }
                }
            }
        }
    }

    static ArrayList<SemObject> getEventsThroughCoparticipation(ArrayList<SemObject> semEvents,
                                                ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        ArrayList<SemObject> coparticipationEvents = new ArrayList<SemObject>();
        for (int i = 0; i < microSemActors.size(); i++) {
            SemObject semObject = microSemActors.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getURI())) {
                    for (int k = 0; k < semEvents.size(); k++) {
                        SemObject event = semEvents.get(k);
                        if (event.getURI().equals(semRelation.getSubject())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                                !Util.hasObjectUri(coparticipationEvents, event.getURI())) {
                                coparticipationEvents.add(event);
                            }
                        }
                    }
                }
            }
        }
        return coparticipationEvents;
    }


    public static ArrayList<JSONObject> getEventsThroughCoparticipation(ArrayList<JSONObject> events,
                                                                 JSONObject event, int intersection)
            throws JSONException {
        ArrayList<JSONObject> coPartipantEvents = new ArrayList<JSONObject>();
        JSONObject actorObject = event.getJSONObject("actors");
        Iterator keys = actorObject.sortedKeys();
        while (keys.hasNext()) {
            String key = keys.next().toString(); //role
            if (key.toLowerCase().startsWith("pb/")
                    || key.toLowerCase().startsWith("fn/")
                    || key.toLowerCase().startsWith("eso/")
                    ) {
                JSONArray actors = actorObject.getJSONArray(key);
                for (int i = 0; i < events.size(); i++) {
                    JSONObject oEvent = events.get(i);
                    if (!oEvent.get("instance").toString().equals(event.get("instance").toString())) {
                        ///skip event itself
                        JSONObject oActorObject = oEvent.getJSONObject("actors");
                        Iterator oKeys = oActorObject.sortedKeys();
                        int cnt = 0;
                        while (oKeys.hasNext()) {
                            String oKey = oKeys.next().toString();
                            if (oKey.toLowerCase().startsWith("pb/")
                                    || oKey.toLowerCase().startsWith("fn/")
                                    || oKey.toLowerCase().startsWith("eso/")
                                    ) {
                                JSONArray oActors = oActorObject.getJSONArray(oKey);
                              //  System.out.println("oActors.length() = " + oActors.length());
                               // System.out.println("oActors.toString() = " + oActors.toString());
                                if (countIntersectingDBpActor(actors,oActors)>=intersection) {
                                    cnt++;
                                    if (!coPartipantEvents.contains(oEvent)) {
                                        coPartipantEvents.add(oEvent);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return coPartipantEvents;
    }

    public static ArrayList<JSONObject> getEventsThroughCoparticipation(String entityFilter, ArrayList<JSONObject> events,
                                                                 JSONObject event)
            throws JSONException {
        ArrayList<JSONObject> coPartipantEvents = new ArrayList<JSONObject>();
        JSONObject actorObject = event.getJSONObject("actors");
        Iterator keys = actorObject.sortedKeys();
        while (keys.hasNext()) {
            String key = keys.next().toString(); //role
          //  System.out.println("key = " + key);
            if (key.toLowerCase().startsWith("pb/")
                    || key.toLowerCase().startsWith("fn/")
                    || key.toLowerCase().startsWith("eso/")) {
                JSONArray actors = actorObject.getJSONArray(key);
                // System.out.println("actors.toString() = " + actors.toString());
                for (int i = 0; i < events.size(); i++) {
                    JSONObject oEvent = events.get(i);
                    if (!oEvent.get("instance").toString().equals(event.get("instance").toString())) {
                        ///skip event itself
                        JSONObject oActorObject = oEvent.getJSONObject("actors");
                        Iterator oKeys = oActorObject.sortedKeys();
                        while (oKeys.hasNext()) {
                            String oKey = oKeys.next().toString();
                            if (oKey.toLowerCase().startsWith("pb/")
                                    || oKey.toLowerCase().startsWith("fn/")
                                    || oKey.toLowerCase().startsWith("eso/")) {
                                JSONArray oActors = oActorObject.getJSONArray(oKey);
                                // System.out.println("oActors.toString() = " + oActors.toString());
                                if (intersectingActor(entityFilter, actors, oActors)) {
                                    if (!coPartipantEvents.contains(oEvent)) {
                                         coPartipantEvents.add(oEvent);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return coPartipantEvents;
    }

    public static ArrayList<JSONObject> getEventsThroughCoparticipation(ArrayList<JSONObject> events,
                                                                        ArrayList<JSONObject> storyEvents)
                throws JSONException {
            ArrayList<JSONObject> coPartipantEvents = new ArrayList<JSONObject>();
            for (int j = 0; j < events.size(); j++) {
                JSONObject jsonObject = events.get(j);
                JSONObject actorObject = jsonObject.getJSONObject("actors");
                Iterator keys = actorObject.sortedKeys();
                while (keys.hasNext()) {
                    String key = keys.next().toString(); //role
                    //  System.out.println("key = " + key);
                    if (key.equalsIgnoreCase("pb/A0")
                            || key.equalsIgnoreCase("pb/A1")
                            || key.equalsIgnoreCase("pb/A2")
                            || key.equalsIgnoreCase("pb/A3")
                            || key.equalsIgnoreCase("pb/A4")
                            || key.toLowerCase().startsWith("fn/")
                            || key.toLowerCase().startsWith("eso/")) {
                        JSONArray actors = actorObject.getJSONArray(key);
                        // System.out.println("actors.toString() = " + actors.toString());
                        for (int i = 0; i < storyEvents.size(); i++) {
                            JSONObject oEvent = storyEvents.get(i);
                            if (!oEvent.get("instance").toString().equals(jsonObject.get("instance").toString())) {
                                ///skip event itself
                                JSONObject oActorObject = oEvent.getJSONObject("actors");
                                Iterator oKeys = oActorObject.sortedKeys();
                                while (oKeys.hasNext()) {
                                    String oKey = oKeys.next().toString();
                                    if (oKey.equalsIgnoreCase("pb/A0")
                                            || oKey.equalsIgnoreCase("pb/A1")
                                            || oKey.equalsIgnoreCase("pb/A2")
                                            || oKey.equalsIgnoreCase("pb/A3")
                                            || oKey.equalsIgnoreCase("pb/A4")
                                            || oKey.toLowerCase().startsWith("fn/")
                                            || oKey.toLowerCase().startsWith("eso/")) {
                                        JSONArray oActors = oActorObject.getJSONArray(oKey);
                                        // System.out.println("oActors.toString() = " + oActors.toString());
                                        if (intersectingDBpActor(actors, oActors)) {
                                            if (!coPartipantEvents.contains(oEvent)) {
                                                coPartipantEvents.add(oEvent);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return coPartipantEvents;
        }

    static boolean intersectingActor (JSONArray actors, JSONArray oActors) throws JSONException {
        for (int j = 0; j < actors.length(); j++) {
            String actor = actors.getString(j);
            for (int k = 0; k < oActors.length(); k++) {
                String oActor = oActors.getString(k);
                if (actor.equals(oActor)) {
                    return true;
                }
            }
        }
        return false;
    }

    static int countIntersectingDBpActor (JSONArray actors, JSONArray oActors) throws JSONException {
        int cnt = 0;
        for (int j = 0; j < actors.length(); j++) {
            String actor = actors.getString(j);
            //System.out.println("actor = " + actor);
            for (int k = 0; k < oActors.length(); k++) {
                String oActor = oActors.getString(k);
                if (actor.equals(oActor)) {
                    cnt++;
                }
            }
/*
            if (actor.indexOf("dbpedia")>-1 || actor.indexOf("dbp:")>-1 || actor.indexOf("en:")>-1) {
                for (int k = 0; k < oActors.length(); k++) {
                    String oActor = oActors.getString(k);
                    if (oActor.indexOf("dbpedia")>-1 || actor.indexOf("dbp:")>-1 || actor.indexOf("en:")>-1) {
                        if (actor.equals(oActor)) {
                            cnt++;
                        }
                    }
                }
            }
*/
        }
        return cnt;
    }

    static boolean intersectingDBpActor (JSONArray actors, JSONArray oActors) throws JSONException {
        for (int j = 0; j < actors.length(); j++) {
            String actor = actors.getString(j);
            //System.out.println("actor = " + actor);
            if (actor.indexOf("dbpedia")>-1 || actor.indexOf("dbp:")>-1 || actor.indexOf("en:")>-1) {
                for (int k = 0; k < oActors.length(); k++) {
                    String oActor = oActors.getString(k);
                    if (oActor.indexOf("dbpedia")>-1 || actor.indexOf("dbp:")>-1 || actor.indexOf("en:")>-1) {
                        if (actor.equals(oActor)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean intersectingActor (String entityFilter, JSONArray actors, JSONArray oActors) throws JSONException {
        for (int j = 0; j < actors.length(); j++) {
            String actor = actors.getString(j);
            for (int k = 0; k < oActors.length(); k++) {
                String oActor = oActors.getString(k);
                if (actor.equals(oActor)) {
                    if (actor.indexOf(entityFilter)==-1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Obtain participants through bridging relations
     * @param microSemEvents
     * @param semActors
     * @param microSemActors
     * @param semRelations
     */
    static void addActorsThroughCoparticipation(ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> semActors,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject semEvent = microSemEvents.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getSubject().equals(semEvent.getURI())) {
                    for (int k = 0; k < semActors.size(); k++) {
                        SemObject actor = semActors.get(k);
                        if (actor.getURI().equals(semRelation.getObject())) {
                            if (!Util.hasObjectUri(microSemActors, actor.getURI())) {
                                microSemActors.add(actor);
                            }
                                //Util.addObject(microSemActors, actor);
                            }
                        }
                    }
                }
            }
    }

    static ArrayList<SemObject> getActorsThroughCoparticipation(ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> semActors,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        ArrayList<SemObject> coparticipantActors = new ArrayList<SemObject>();
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject semEvent = microSemEvents.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getSubject().equals(semEvent.getURI())) {
                    for (int k = 0; k < semActors.size(); k++) {
                        SemObject actor = semActors.get(k);
                        if (actor.getURI().equals(semRelation.getObject())) {
                            if (!Util.hasObjectUri(microSemActors, actor.getURI()) &&
                                !Util.hasObjectUri(coparticipantActors, actor.getURI())) {
                                coparticipantActors.add(actor);
                            }
                        }
                    }
                }
            }
        }
        return coparticipantActors;
    }


}
