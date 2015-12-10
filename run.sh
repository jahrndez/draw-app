#!/bin/bash
cd src/
rm -f server/*.class
javac server/Dispatch.java
java server/Dispatch
