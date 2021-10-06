#!/bin/sh

rm ./jarcontents/*.class
javac -classpath './libs/*' ./src/org/sebson/jdbc/GA/*.java ./src/org/sebson/SQL/*.java -d ./jarcontents

rm ./build/GA-jdbc.jar

for i in `ls ./libs` ; do unzip -o ./libs/$i -d ./jarcontents ; done

cd ./jarcontents

jar cvfe ../build/GA-jdbc.jar org.sebson.jdbc.GA.GADriver ./*

# Run it:
#   java -jar ./build/GA-jdbc.jar KEY_FILE_LOCATION=dev-mg-950381754252.json "select ga:city, ga:pageTitle, ga:dateHourMinute, ga:country, ga:sessions from 'your_google_view_id' where date between 10DaysAgo and today"