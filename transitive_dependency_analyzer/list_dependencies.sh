#!/bin/bash
mvn dependency:list | grep "^\[INFO\]   " | awk '{print $2}' | sort | uniq 
