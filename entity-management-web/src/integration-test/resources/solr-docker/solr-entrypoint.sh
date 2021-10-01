#!/bin/bash
set -e

# Create new configset based off default one
cp -r /opt/solr/server/solr/configsets/_default/ /opt/solr/server/solr/configsets/"$EM_INDEXING_CORE"/

# Overwrite Files copied to /opt/em-conf in Dockerfile
mv /opt/em-conf/* /opt/solr/server/solr/configsets/"$EM_INDEXING_CORE"/conf/

# Set access rights for entity-management configset
chown -R solr:solr /opt/solr/server/solr/configsets/"$EM_INDEXING_CORE"

precreate-core "$EM_INDEXING_CORE" /opt/solr/server/solr/configsets/"$EM_INDEXING_CORE"

# Set access rights for entity-management core data
chown -R solr:solr /opt/solr/server/solr/mycores/"$EM_INDEXING_CORE"/

## drop access to solr and run cmd
exec gosu solr "$@"


