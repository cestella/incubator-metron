#!/bin/bash
TLD=$(pwd)
pushd $1
$TLD/transitive_dependency_analyzer/list_dependencies.sh | python $TLD/build_utils/generate_license.py $TLD/dependencies_with_url.csv $TLD/LICENSE
popd
