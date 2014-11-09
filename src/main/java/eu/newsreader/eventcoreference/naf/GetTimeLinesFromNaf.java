package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.timeline.EntityTimeLine;
import eu.newsreader.eventcoreference.util.EventTypes;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.TimeLanguage;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by piek on 10/22/14.
 */
public class GetTimeLinesFromNaf {
    /**
     * TimeLine:
     Steve Jobs
     1        2004                  18315-7-fighting    18355-4-fighting
     2        2005-06-05      1664-2-keynote
     3        2011-01            18315-7-leave
     4        2011-08-24      18315-2-step_down
     */
    static final public String ID_SEPARATOR = "#";
    static final public String URI_SEPARATOR = "_";
    static HashMap<String, EntityTimeLine> entitiesTimeLineHashMap;
    static HashMap<String, EntityTimeLine> entityTimeLineHashMap;
    static Vector<String> communicationVector = null;
    static Vector<String> grammaticalVector = null;
    static Vector<String> contextualVector = null;

    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        String pathToNafFile = "";
        //pathToNafFile = "/Users/piek/Desktop/NWR/timeline/naf_file_raw_out/4678-Apple_releases_program.xml.naf.coref";
        // pathToNafFile = "/Users/piek/Desktop/NWR/timeline/1514-trialNWR20.naf";
        // pathToNafFile = "/Users/piek/Desktop/NWR/timeline/1514-trialPiekCoref.naf";
        // pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/possession-test.naf";
        // pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/example/razni11-01.event-coref.naf";
        // pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/fifa.naf";
        String pathToFolder = "";
        // pathToFolder = "/Users/piek/Desktop/NWR/NWR-Annotation/corpus_NAF_output/corpus_gm_chrysler_ford";
        pathToFolder = "/Users/piek/Desktop/NWR/timeline/naf_file_raw_out-2";
        String query = "apple";
        String extension = ".naf";
        String eventType = "CONTEXTUAL";
        String project = "apple";
        String comFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/communication.txt";
        String contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/contextual.txt";
        String grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/grammatical.txt";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-file") && args.length>(i+1)) {
                pathToNafFile = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                project = args[i+1];
            }
            else if (arg.equals("--folder") && args.length>(i+1)) {
                pathToFolder = args[i+1];
            }
            else if (arg.equals("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
            else if (arg.equals("--query") && args.length>(i+1)) {
                query = args[i+1];
            }
            else if (arg.equals("--event-type") && args.length>(i+1)) {
                eventType = args[i+1];
            }
            else if (arg.equals("--communication-frames") && args.length>(i+1)) {
                comFrameFile = args[i+1];
            }
            else if (arg.equals("--grammatical-frames") && args.length>(i+1)) {
                grammaticalFrameFile = args[i+1];
            }
            else if (arg.equals("--contextual-frames") && args.length>(i+1)) {
                contextualFrameFile = args[i+1];
            }
        }

        //// read resources
        communicationVector = Util.ReadFileToStringVector(comFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);

        if (!pathToNafFile.isEmpty()) {
            KafSaxParser kafSaxParser = new KafSaxParser();
            kafSaxParser.parseFile(pathToNafFile);
            String timeLines = processNafFile(new File(pathToNafFile), project, kafSaxParser);
            try {
                OutputStream fos = new FileOutputStream(pathToNafFile + ".tml");
                fos.write(timeLines.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (!pathToFolder.isEmpty()) {
           entityTimeLineHashMap = new HashMap<String, EntityTimeLine>();
           entitiesTimeLineHashMap = new HashMap<String, EntityTimeLine>();
           KafSaxParser kafSaxParser = new KafSaxParser();
           ArrayList<File> files= Util.makeFlatFileList(new File (pathToFolder), extension);
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
               // System.out.println("file.getName() = " + file.getName());
                kafSaxParser.parseFile(file);
                String timeLines = processNafFile(file, project, kafSaxParser);
                try {
                    OutputStream fos = new FileOutputStream(file + ".tml");
                    fos.write(timeLines.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                processNafFileFromFolder(file.getName(), project, kafSaxParser, eventType);
               // System.out.println("entityTimeLineHashMap = " + entityTimeLineHashMap.size());
                Set keySet = entityTimeLineHashMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.toLowerCase().indexOf(query.toLowerCase())!=-1) {
                        EntityTimeLine entityTimeLine = entityTimeLineHashMap.get(key);
                        if (entitiesTimeLineHashMap.containsKey(query)) {
                            EntityTimeLine timeLine = entitiesTimeLineHashMap.get(query);
                            timeLine.addTimeLine(entityTimeLine);
                            entitiesTimeLineHashMap.put(query, timeLine);
                        }
                        else {
                            EntityTimeLine timeLine = new EntityTimeLine();
                            timeLine.setEntityId(query);
                            timeLine.addTimeLine(entityTimeLine);
                            entitiesTimeLineHashMap.put(query, timeLine);
                        }
                    }
                }
                entityTimeLineHashMap = new HashMap<String, EntityTimeLine>();
            }
            try {
                OutputStream fos = new FileOutputStream(pathToFolder+"/" + query+"-"+eventType+".html");
                Set keySet = entitiesTimeLineHashMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.toLowerCase().indexOf(query.toLowerCase())!=-1) {

                        EntityTimeLine entityTimeLine = entitiesTimeLineHashMap.get(key);
                        String timeLine = entityTimeLine.toString();
                        fos.write(timeLine.getBytes());
                    }
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
       // System.out.println(timeLines);

    }

    static String getEventTypeString (SemEvent semEvent) {
        String eventTypes = "";
        for (int k = 0; k < semEvent.getConcepts().size(); k++) {
            KafSense kafSense = semEvent.getConcepts().get(k);
            if (kafSense.getResource().equalsIgnoreCase("framenet")) {
                //eventTypes += "fn:"+kafSense.getSensecode()+";";
                if (communicationVector!=null && communicationVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventTypes+="fn:communication@"+kafSense.getSensecode()+";";
                }
                else if (grammaticalVector!=null && grammaticalVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventTypes+="fn:grammatical@"+kafSense.getSensecode()+";";
                }
                if (contextualVector!=null && contextualVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventTypes+="fn:contextual@"+kafSense.getSensecode()+";";
                }
            }
            else if (kafSense.getResource().equalsIgnoreCase("eso")) {
                eventTypes += "eso:"+kafSense.getSensecode()+";";
            }
            else if (kafSense.getResource().equalsIgnoreCase("eventtype")) {
                //  eventTypes += "nwr:"+kafSense.getSensecode()+";";
            }
            else if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
                eventTypes += "wn:"+kafSense.getSensecode()+";";
            }
            else {
            }
        }
        return eventTypes;
    }

    static public String processNafFile (File file, String project, KafSaxParser kafSaxParser) {
        String timeLine = "";
        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        String baseUrl = "";
        if (!kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
            if (!baseUrl.toLowerCase().startsWith("http")) {
                baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
            }
        }
        else {
            baseUrl = ResourcesUri.nwrdata + project + "/" + file.getName() + ID_SEPARATOR;
        }
        GetSemFromNafFile.processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        SemTime docSemTime = GetSemFromNafFile.processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
        GetSemFromNafFile.processNafFileForEventInstances(baseUrl, kafSaxParser, semEvents);
        processNafFileForRelations(baseUrl, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations);
        try {
            OutputStream fos = new FileOutputStream(file.getAbsolutePath()+".trg");
            JenaSerialization.serializeJena(fos, semEvents, semActors, semPlaces, semTimes, semRelations, null, null);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semObject = semActors.get(i);
            timeLine += semObject.getId()+"\n";
            timeLine += "\t";
            for (int j = 0; j < semObject.getNafMentions().size(); j++) {
                NafMention nafMention = semObject.getNafMentions().get(j);
                timeLine += nafMention.getTermsIds().toString()+"["+nafMention.getPhraseFromMention(kafSaxParser)+"];";
            }
            timeLine += "\n\tTIMELINE\n";
            ArrayList<String> coveredEvents = new ArrayList<String>();
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getId())) {
                    /// we have an event involving the object
                    if (RoleLabels.hasPRIMEPARTICIPANT(semRelation.getPredicates()) || RoleLabels.hasSECONDPARTICIPANT(semRelation.getPredicates())) {
                        String eventId = semRelation.getSubject();

                        SemEvent semEvent = null;
                        for (int l = 0; l < semEvents.size(); l++) {
                            if (semEvents.get(l).getId().equals(eventId)) {
                                semEvent = (SemEvent) semEvents.get(l);
                                break;
                            }
                        }
                        if ((semEvent!=null) && (!coveredEvents.contains(semEvent.getId()))) {
                            coveredEvents.add(semEvent.getId());
                            /// This is the event involving this actor
                            /// Now get the time
                            //System.out.println("semEvent.getTopPhraseAsLabel() = " + semEvent.getTopPhraseAsLabel());
                            ArrayList<String> coveredTimes = new ArrayList<String>();
                            for (int m = 0; m < semRelations.size(); m++) {
                                SemRelation relation = semRelations.get(m);
                                if (relation.getSubject().equals(eventId))  {
                                    if (relation.getPredicates().contains("hasSemTime")) {
                                        String timeId = relation.getObject();
                                        for (int n = 0; n < semTimes.size(); n++) {
                                            SemTime semTime = (SemTime) semTimes.get(n);
                                            if (semTime.getId().equals(timeId)) {
                                                String timeString = semTime.getOwlTime().toString();
                                                if (!coveredTimes.contains(timeString)) {
                                                    coveredTimes.add(timeString);
                                                    timeLine += "\t" + timeString;
                                                    String eventTypes = getEventTypeString(semEvent);
                                                    for (int o = 0; o < semEvent.getNafMentions().size(); o++) {
                                                        NafMention nafMention = semEvent.getNafMentions().get(o);
                                                        String sentenceId = "";
                                                        for (int p = 0; p < nafMention.getTokensIds().size(); p++) {
                                                            String tokenId = nafMention.getTokensIds().get(p);
                                                            KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                                                            sentenceId += kafWordForm.getSent();
                                                        }
                                                      //  timeLine += "\t" + file.getName() + "-" + sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                                                        timeLine += "\t" + sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                                                    }
                                                    timeLine +="\tTYPES:"+eventTypes+ "\n";
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (coveredTimes.size()==0) {
                                timeLine += "\tNOTIMEX";
                                String eventTypes = getEventTypeString(semEvent);
                                for (int o = 0; o < semEvent.getNafMentions().size(); o++) {
                                    NafMention nafMention = semEvent.getNafMentions().get(o);
                                    String sentenceId = "";
                                    for (int p = 0; p < nafMention.getTokensIds().size(); p++) {
                                        String tokenId = nafMention.getTokensIds().get(p);
                                        KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                                        sentenceId += kafWordForm.getSent();
                                    }
                                   // timeLine += "\t" + file.getName() + "-" + sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                                    timeLine += "\t"  + sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                                }
                                timeLine +="\tTYPES:"+eventTypes+ "\n";
                            }
                        }
                        else {
                          //  System.out.println("NO EVENTS");
                        }
                    }
                }
            }
            timeLine += "\n";
        }
        return timeLine;
    }

    static public void processNafFileFromFolder (String fileName, String project, KafSaxParser kafSaxParser, String eventType) {
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();

        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        String baseUrl = "";
        if (!kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
            if (!baseUrl.toLowerCase().startsWith("http")) {
                baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
            }
        }
        else {
           // baseUrl = ResourcesUri.nwrdata + project + "/" + fileName + ID_SEPARATOR;
            baseUrl = fileName;
        }
        GetSemFromNafFile.processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        SemTime docSemTime = GetSemFromNafFile.processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
        GetSemFromNafFile.processNafFileForEventInstances(baseUrl, kafSaxParser, semEvents);
        processNafFileForRelations(baseUrl, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations);
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semObject = semActors.get(i);
            ArrayList<String> coveredEvents = new ArrayList<String>();
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getId())) {
                    /// we have an event involving the object
                    if (RoleLabels.hasPRIMEPARTICIPANT(semRelation.getPredicates()) || RoleLabels.hasSECONDPARTICIPANT(semRelation.getPredicates())) {
                        String eventId = semRelation.getSubject();

                        SemEvent semEvent = null;
                        for (int l = 0; l < semEvents.size(); l++) {
                            if (semEvents.get(l).getId().equals(eventId)) {
                                semEvent = (SemEvent) semEvents.get(l);
                                break;
                            }
                        }
                        if (semEvent.getPhrase().length()<=3) {
                            semEvent = null;
                            continue;
                        }
                        boolean EVENTTYPE = false;
                        for (int k = 0; k < semEvent.getConcepts().size(); k++) {
                            KafSense kafSense = semEvent.getConcepts().get(k);
                            if (eventType.equalsIgnoreCase("contextual")) {
                                if (EventTypes.isCONTEXTUAL(kafSense.getSensecode())) {
                                    EVENTTYPE = true;
                                    break;
                                }
                            }
                            if (eventType.equalsIgnoreCase("communication")) {
                                if (EventTypes.isCOMMUNICATION(kafSense.getSensecode())) {
                                    EVENTTYPE = true;
                                    break;
                                }
                            }
                            if (eventType.equalsIgnoreCase("grammatical")) {
                                if (EventTypes.isGRAMMATICAL(kafSense.getSensecode())) {
                                    EVENTTYPE = true;
                                    break;
                                }
                            }
                        }
                        if (!EVENTTYPE) {
                            semEvent = null;
                            continue;
                        }
                        if ((semEvent!=null) && (!coveredEvents.contains(semEvent.getId()))) {
                            coveredEvents.add(semEvent.getId());
                            /// This is the event involving this actor
                            /// Now get the time
                            //System.out.println("semEvent.getTopPhraseAsLabel() = " + semEvent.getTopPhraseAsLabel());
                            ArrayList<String> coveredTimes = new ArrayList<String>();
                            for (int m = 0; m < semRelations.size(); m++) {
                                SemRelation relation = semRelations.get(m);
                                if (relation.getSubject().equals(eventId))  {
                                    if (relation.getPredicates().contains("hasSemTime")) {
                                        String timeId = relation.getObject();
                                        for (int n = 0; n < semTimes.size(); n++) {
                                            SemTime semTime = (SemTime) semTimes.get(n);
                                            if (semTime.getId().equals(timeId)) {
                                                String timeString = semTime.getOwlTime().toString();
                                                if (!coveredTimes.contains(timeString)) {
                                                    coveredTimes.add(timeString);
                                                    if (entityTimeLineHashMap.containsKey(semObject.getURI())) {
                                                       EntityTimeLine entityTimeLine = entityTimeLineHashMap.get(semObject.getId());
                                                       entityTimeLine.addEntityNafMentions(semObject.getNafMentions());
                                                       entityTimeLine.addTimeEventNafMentions(timeString, semEvent.getNafMentions());
                                                       entityTimeLineHashMap.put(semObject.getId(), entityTimeLine);
                                                    }
                                                    else {
                                                        EntityTimeLine entityTimeLine = new EntityTimeLine();
                                                        entityTimeLine.setEntityId(semObject.getURI());
                                                        entityTimeLine.addEntityNafMentions(semObject.getNafMentions());
                                                        entityTimeLine.addTimeEventNafMentions(timeString, semEvent.getNafMentions());
                                                        entityTimeLineHashMap.put(semObject.getURI(), entityTimeLine);
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (coveredTimes.size()==0) {
                                if (entityTimeLineHashMap.containsKey(semObject.getURI())) {
                                    EntityTimeLine entityTimeLine = entityTimeLineHashMap.get(semObject.getId());
                                    entityTimeLine.addEntityNafMentions(semObject.getNafMentions());
                                    entityTimeLine.addTimeEventNafMentions(docSemTime.getOwlTime().toString(), semEvent.getNafMentions());
                                    entityTimeLineHashMap.put(semObject.getURI(), entityTimeLine);
                                }
                                else {
                                    EntityTimeLine entityTimeLine = new EntityTimeLine();
                                    entityTimeLine.setEntityId(semObject.getId());
                                    entityTimeLine.addEntityNafMentions(semObject.getNafMentions());
                                    entityTimeLine.addTimeEventNafMentions(docSemTime.getOwlTime().toString(), semEvent.getNafMentions());
                                    entityTimeLineHashMap.put(semObject.getURI(), entityTimeLine);
                                }
                            }
                        }
                        else {
                          //  System.out.println("NO EVENTS");
                        }
                    }
                }
            }
        }
    }

    /**
     * Main function to get the SEM relations.
     * @param baseUrl
     * @param kafSaxParser
     * @param semEvents
     * @param semActors
     * @param semPlaces
     * @param semTimes
     * @param semRelations
     */
    static void processNafFileForRelations (String baseUrl, KafSaxParser kafSaxParser,
                                            ArrayList<SemObject> semEvents,
                                            ArrayList<SemObject> semActors,
                                            ArrayList<SemObject> semPlaces,
                                            ArrayList<SemObject> semTimes,
                                            ArrayList<SemRelation> semRelations
    ) {


        /*
          We create mappings between the SemTime objects and the events. SemTime objects come either from the TimeEx layer
          or from the SRL layer. If they come from the Timex layer we have no information on how they relate to the event.
          We therefore check if they occur in the same sentence.
          Other options:
          - same + preceding sentence
          - same + preceding + following sentence
          - make a difference for SRL and Timex
         */
        int docTimeRelationCount = 0;
        int timexRelationCount = 0;
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            //// separate check for time that can be and should be derived from the timex layer....
            for (int l = 0; l < semTimes.size(); l++) {
                SemObject semTime = semTimes.get(l);
                //System.out.println("semTime.toString() = " + semTime.toString());
                // if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semTime)) {
                //if (Util.sameSentence(kafSaxParser, semTime, semEvent)) {
                boolean HASTIME = Util.rangemin2plus1Sentence(kafSaxParser, semTime, semEvent);
                if (!HASTIME) {
                   // HASTIME = Util.rangemin5Sentence(kafSaxParser, semTime, semEvent);
                }
                if (HASTIME) {
                    /// create sem relations
                    timexRelationCount++;
                    SemRelation semRelation = new SemRelation();
                    String relationInstanceId = baseUrl+"timeRelation_"+timexRelationCount;
                    semRelation.setId(relationInstanceId);

                    ArrayList<String> termsIds = new ArrayList<String>();
                    for (int j = 0; j < semEvent.getNafMentions().size(); j++) {
                        NafMention nafMention = semEvent.getNafMentions().get(j);
                        termsIds.addAll(nafMention.getTermsIds());
                    }
                    for (int j = 0; j < semTime.getNafMentions().size(); j++) {
                        NafMention nafMention = semTime.getNafMentions().get(j);
                        termsIds.addAll(nafMention.getTermsIds());
                    }

                    NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                    semRelation.addMention(mention);
                    semRelation.addPredicate("hasSemTime");
                    semRelation.setSubject(semEvent.getId());
                    semRelation.setObject(semTime.getId());
                    semRelations.add(semRelation);
                    // System.out.println("semRelation = " + semRelation.getSubject());
                    // System.out.println("semRelation.getObject() = " + semRelation.getObject());
                }
            }
        }


        // NEXT WE RELATE ACTORS AND PLACES TO EVENTS
        ///
        // THIS IS NOT EASY DUE TO THE COMPLEX OVERLAP IN SPAN ACROSS ENTITIES AND ROLES IN SRL
        // IF THE SEMOBJECT IS BASED ON AN PARTICIPANT (SRL ROLE) IT IS OK
        // IF IT IS BASED ON AN ENTITY OR COREFSET WE NEED TO BE CAREFUL
        // THERE HAS TO BE SUFFICIENT OVERLAP OF THE CONTENT WORDS
        // THERE CAN STILL BE DIFFERENCES DUE TO THE FACT THAT MENTIONS ARE ANALYSED DIFFERENTLY
        // THE EXTENT CAN BE A WHOLE PHRASE OR JUST TH EHEAD OF A PHRASE DEPENDING ON THE MODULE
        //
        // WE DEFINED A FUNCTION THAT CHECKS THE OVERLAP WITH ALL THE SEMOBJECTS AND RETURNS THE ONE WITH THE HIGHEST OVERLAP
        // GIVEN A THRESHOLD FOR A MINIMUM OVERLAP. FOR THE OVERLAP WE CAN CALCULATE ALL THE SPAN ELEMENTS OR THE CONTENT WORDS.
        /*
            - iterate over de SRL layers
            - represent predicates and participants
            - check if they overlap with semObjects
            - if so use the instanceId
            - if not create a new instanceId
         */
        // DONE BUT STILL NEEDS TO BE TESTED

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent =  kafSaxParser.getKafEventArrayList().get(i);
            //// we need to get the corresponding semEvent first
            // check the SemEvents
            String semEventId = "";
            for (int j = 0; j < semEvents.size(); j++) {
                SemObject semEvent = semEvents.get(j);
                // if (matchAtLeastASingleSpan(kafEvent.getSpanIds(), semEvent)) {
                if (Util.matchAllOfAnyMentionSpans(kafEvent.getSpanIds(), semEvent)) {
                    semEventId = semEvent.getId();
                    break;
                }
            }
            if (semEventId.isEmpty()) {
                //// this is an event without SRL representation, which is not allowed
                // SHOULD NEVER OCCUR
            }
            else {
                for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                    KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                    // CERTAIN ROLES ARE NOT PROCESSED AND CAN BE SKIPPED
                    if (!RoleLabels.validRole(kafParticipant.getRole())) {
                        continue;
                    }
                    ArrayList<SemObject> semObjects = Util.getAllMatchingObject(kafSaxParser, kafParticipant, semActors);
                    for (int l = 0; l < semObjects.size(); l++) {
                        SemObject semObject = semObjects.get(l);
                        if (semObject!=null) {
                            SemRelation semRelation = new SemRelation();
                            String relationInstanceId = baseUrl + kafEvent.getId() + "," + kafParticipant.getId();
                            semRelation.setId(relationInstanceId);
/*
                            if (kafParticipant.getId().equals("rl130")) {
                                System.out.println(semObject.getId());
                            }
*/
                            ArrayList<String> termsIds = kafEvent.getSpanIds();
                            for (int j = 0; j < kafParticipant.getSpanIds().size(); j++) {
                                String s = kafParticipant.getSpanIds().get(j);
                                termsIds.add(s);
                            }
                            NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                            semRelation.addMention(mention);
                            semRelation.addPredicate("hasSemActor");
                            //// check the source and prefix accordingly
                            semRelation.addPredicate(kafParticipant.getRole());
                            for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                                KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                                semRelation.addPredicate(kafSense.getResource() + ":" + kafSense.getSensecode());
                            }
                            semRelation.setSubject(semEventId);
                            semRelation.setObject(semObject.getId());
                            semRelations.add(semRelation);
                        }
                    }
                    semObjects = Util.getAllMatchingObject(kafSaxParser, kafParticipant, semPlaces);
                    for (int l = 0; l < semObjects.size(); l++) {
                        SemObject semObject = semObjects.get(l);
                        if (semObject!=null) {
                            SemRelation semRelation = new SemRelation();
                            String relationInstanceId = baseUrl + kafEvent.getId() + "," + kafParticipant.getId();
                            semRelation.setId(relationInstanceId);

                            ArrayList<String> termsIds = kafEvent.getSpanIds();
                            for (int j = 0; j < kafParticipant.getSpanIds().size(); j++) {
                                String s = kafParticipant.getSpanIds().get(j);
                                termsIds.add(s);
                            }
                            NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                            semRelation.addMention(mention);
                            semRelation.addPredicate("hasSemPlace");
                            semRelation.addPredicate(kafParticipant.getRole());
                            for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                                KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                                semRelation.addPredicate(kafSense.getResource() + ":" + kafSense.getSensecode());
                            }
                            semRelation.setSubject(semEventId);
                            semRelation.setObject(semObject.getId());
                            semRelations.add(semRelation);
                        }

                    }
                }
            }
        }
    }



}
