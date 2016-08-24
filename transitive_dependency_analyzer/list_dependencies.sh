#!/bin/bash
mvn dependency:list | grep "^\[INFO\]   " | awk '{print $2}' | grep -v "org.apache" | grep -v "test" | sort | uniq 
