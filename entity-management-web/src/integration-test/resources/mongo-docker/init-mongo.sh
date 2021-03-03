# This script creates a mongo user, the entity database and the Spring Batch
# JobRepository
mongo -- "$MONGO_INITDB_DATABASE" <<EOF
    var rootUser = '$MONGO_INITDB_ROOT_USERNAME';
    var rootPassword = '$MONGO_INITDB_ROOT_PASSWORD';


    db.auth(rootUser, rootPassword);

    db.createUser({
        user: '$EM_DB_USER',
        pwd: '$EM_DB_PASSWORD',
        roles: [
            { role: 'readWrite', db: '$EM_APP_DB' },
            { role: 'readWrite', db: '$EM_BATCH_DB' }
            ],
    });


db.getSiblingDB('$EM_APP_DB').createCollection('temp');
db.getSiblingDB('$EM_BATCH_DB').createCollection('temp');

EOF