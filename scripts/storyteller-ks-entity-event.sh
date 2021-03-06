DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
DEMO="$( cd $ROOT && cd .. && pwd)"/Storyteller/UncertaintyVisualization/app/data/
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

MERGE=$1
TIME=$2
ONT=$3
ONTGRAN=$4
ACTOR=$5
INTERSECT=$6
TOPIC=$7
CLIMAX=$8
ENTITY=$9
EVENT=${10}

echo "TIME GRANULARITY = $TIME. Options Y=year, M=month, W=week, D=day, N=time disabled"
echo "ACTION ONTOLOGY = $ONT. Options fn, eso, ili, any or N=ontology disabled"
echo "ACTION SIMILARITY THRESHOLD = $ONTGRAN."
echo "ACTOR COUNT = $ACTOR"
echo "ACTOR INTERSECT = $INTERSECT"
echo "TOPIC INTERSECT = $TOPIC"
echo "CLIMAX THRESHOLD = $CLIMAX"
echo "Entity = $ENTITY"
echo "Event = $EVENT"

java -Xmx4000m -cp $LIB/EventCoreference-v3.0-jar-with-dependencies.jar eu.newsreader.eventcoreference.storyline.TrigToJsonStoryPerspectives --climax-level $CLIMAX  --ks-limit 500 --story-limit 500 $MERGE --time $TIME --action-ont $ONT --action-sim $ONTGRAN --actor-cnt $ACTOR --actor-intersect $INTERSECT --topic-level $TOPIC --ft "../../data/poll.data"  --blacklist "../../data/blacklist.txt" --eurovoc "$RESOURCES/mapping_eurovoc_skos.csv.gz" --eurovoc-blacklist "../../data/eurovoc-blacklist.txt" --project wikinews  --service https://knowledgestore2.fbk.eu  --ks "nwr/wikinews-new" --ks-user wikinews --ks-pass wikinews --entity $ENTITY --event $EVENT

# Command line example
#./storyteller-ks-entity.sh --merge W ili 5 5 2 50 50 "Cameron" "eso:Buying" 
 
cp "$ROOT/scripts/contextual.timeline.json" "$DEMO/contextual.timeline.json"

