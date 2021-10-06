#!/bin/sh

rm ./jarcontents/*.class
javac -classpath './libs/*' ./src/org/sebson/jdbc/GA/*.java ./src/org/sebson/SQL/*.java -d ./jarcontents

rm ./build/GA-jdbc.jar

for i in `ls ./libs` ; do unzip -o ./libs/$i -d ./jarcontents ; done

cd ./jarcontents

jar cvfe ../build/GA-jdbc.jar org.sebson.jdbc.GA.GADriver ./*