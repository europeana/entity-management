# This script creates a mongo user, the entity database and the Spring Batch
# JobRepository
mongo -- "$MONGO_INITDB_DATABASE" <<EOF
    var rootUser = '$MONGO_INITDB_ROOT_USERNAME';
    var rootPassword = '$MONGO_INITDB_ROOT_PASSWORD';

    db.auth(rootUser, rootPassword);


db.getSiblingDB('$EM_APP_DB').createCollection('temp');
db.getSiblingDB('$EM_BATCH_DB').createCollection('temp');

EOF