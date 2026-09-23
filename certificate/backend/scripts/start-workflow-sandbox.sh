#!/bin/sh
# Run only the test-source sandbox. Never start the production application here.
set -eu
workflow_source=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
workflow_frontend=${1:-"$workflow_source/../react-master"}
if [ ! -f "$workflow_frontend/dist/index.html" ]; then
  echo 'Please build react-master first; pass its absolute directory as argument 1.' >&2
  exit 1
fi
workflow_stage=$(mktemp -d /tmp/yishun-workflow-sandbox.XXXXXX)
echo "Isolated local build: $workflow_stage"
rsync -a --exclude='._*' "$workflow_source/src" "$workflow_source/.mvn" "$workflow_source/pom.xml" "$workflow_source/mvnw" "$workflow_stage/"
cd "$workflow_stage"
sh ./mvnw -q test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/workflow-classpath.txt
workflow_classpath=$(tr -d '\n' < target/workflow-classpath.txt)
exec java -Dfile.encoding=UTF-8 -cp "target/test-classes:target/classes:$workflow_classpath" com.kiss.yishun.workflow.WorkflowSandbox "$workflow_frontend/dist"
