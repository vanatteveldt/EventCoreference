DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib

RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

java -Xmx2000m -cp "$LIB/EventCoreference-v3.0-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.GetSemFromNafStream --non-entities --project wikinews --all --ili "$RESOURCES/ili.ttl.gz" --perspective --eurovoc-en "$RESOURCES//mapping_eurovoc_skos.csv.gz" --source-frames "$RESOURCES/source.txt"
