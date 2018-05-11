#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

BIGTOP_DEFAULTS_DIR=${BIGTOP_DEFAULTS_DIR-/etc/default}
[ -n "${BIGTOP_DEFAULTS_DIR}" -a -r ${BIGTOP_DEFAULTS_DIR}/hbase ] && . ${BIGTOP_DEFAULTS_DIR}/hbase

# Autodetect JAVA_HOME if not defined
if [ -e /usr/libexec/bigtop-detect-javahome ]; then
  . /usr/libexec/bigtop-detect-javahome
elif [ -e /usr/lib/bigtop-utils/bigtop-detect-javahome ]; then
  . /usr/lib/bigtop-utils/bigtop-detect-javahome
fi

export METRON_VERSION=${project.version}
export METRON_HOME=/usr/metron/$METRON_VERSION
export CLASSNAME="org.apache.metron.semhasher.cli.CLI"
export PROJ_JAR=${project.artifactId}-$METRON_VERSION.jar
export NUM_EXECUTORS=${SEMHASH_EXECUTORS-1}
export EXECUTOR_MEMORY=${SEMHASH_EXECUTOR_MEMORY-2G}
spark-submit --conf spark.executor.extraClassPath=$METRON_HOME/lib/$PROJ_JAR --driver-class-path $METRON_HOME/lib/$PROJ_JAR --jars $METRON_HOME/lib/$PROJ_JAR --class org.apache.metron.semhasher.cli.CLI --master yarn --deploy-mode client --executor-memory 2G --num-executors 1 $METRON_HOME/lib/$PROJ_JAR "$@"
