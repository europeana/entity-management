# This script creates a mongo user, the entity database and the Spring Batch
# JobRepository
mongo -- "$MONGO_INITDB_DATABASE" <<EOF
#    if [[ -z ${MONGO_INITDB_ROOT_USERNAME:-} || -z ${MONGO_INITDB_ROOT_PASSWORD:-} ]]; then
#        echo "Mongo admin user or password missing. Cannot create initial user and databases"
#        exit
#    fi

    var rootUser = '$MONGO_INITDB_ROOT_USERNAME';
    var rootPassword = '$MONGO_INITDB_ROOT_PASSWORD';


    db.auth(rootUser, rootPassword);

#if [[ -n ${EM_DB_USER:-} && -n ${EM_DB_PASSWORD:-} ]]; then

    var user = '$EM_DB_USER';
    var passwd = '$EM_DB_PASSWORD';
    db.createUser({
        user: 'api_user',
        pwd: 'password',
        roles: [
            { role: 'readWrite', db: '$EM_APP_DB' },
            { role: 'readWrite', db: '$EM_BATCH_DB' }
            ],
    });
#fi


db.getSiblingDB('$EM_APP_DB').createCollection('temp');
db.getSiblingDB('$EM_BATCH_DB').createCollection('temp');

EOF