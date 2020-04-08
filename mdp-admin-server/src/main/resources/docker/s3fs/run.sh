#!/bin/bash

echo "${AWS_ACCESS_KEY_ID}:${AWS_SECRET_ACCESS_KEY}" > /etc/passwd-s3fs
chmod 0400 /etc/passwd-s3fs

extract_s3_bucket() {
    S3_SOURCE=$S3_BUCKET
    (echo "$S3_SOURCE" | grep -Eq  ^s3:// ) && S3_SOURCE=$(echo "${S3_SOURCE/s3:\/\//}")

    S3_BUCKET=$(echo $S3_SOURCE | cut -d "/" -f1)
    SUB_PATH=$(echo "${S3_SOURCE/#$S3_BUCKET/}")
}

extract_s3_bucket


mkdir -p ${MNT_POINT}

MOUNT_POINT_BASEDIR=$(dirname ${MOUNT_POINT})
test $MOUNT_POINT_BASEDIR
mkdir -p ${MOUNT_POINT_BASEDIR}
mkdir -p ${MOUNT_POINT}

/usr/local/bin/s3fs $S3_BUCKET $MNT_POINT -f -o url=${S3_ENDPOINT},allow_other,use_path_request_style,use_cache=/tmp,max_stat_cache_size=1000,stat_cache_expire=900,retries=5,connect_timeout=10${S3_EXTRAVARS} &

if [ ! -d "${MNT_POINT}${SUB_PATH}" ];
then
    echo 'wait for mount';
    sleep 3;
fi;

echo 'Mount s3 path '${S3_BUCKET}${SUB_PATH}' to '${MOUNT_POINT}
cp -r ${MNT_POINT}${SUB_PATH} ${MOUNT_POINT}