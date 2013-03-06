#!bin/bash

javac UdpSobs.java
java -classpath ".:sqlite-jdbc-3.7.2.jar" UdpSobs -s 2278
