#!/bin/bash
mvn dependency:list | grep "^\[INFO\]   " | awk '{print $2}' | grep -v "org.apache" | grep -v "test" | grep -v "provided" | grep -v "runtime" | grep -v ":system" |  sort | uniq 
