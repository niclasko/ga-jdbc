#!/bin/sh

rm ./jarcontents/*.class
javac -classpath './libs/*' ./src/org/sebson/jdbc/GA/*.java ./src/org/sebson/SQL/*.java -d ./jarcontents

rm ./build/GA-jdbc.jar

for i in `ls ./libs` ; do unzip -o ./libs/$i -d ./jarcontents ; done

cd ./jarcontents

jar cvfe ../build/GA-jdbc.jar org.sebson.jdbc.GA.GADriver ./*

# Run it:
#   java -jar ./build/GA-jdbc.jar KEY_FILE_LOCATION=dev-mg-950381754252.json "select ga:city, ga:pageTitle, ga:dateHourMinute, ga:country, ga:sessions from '154354697'"
# ADD GA View user: ga-exporter@dev-mg.iam.gserviceaccount.com
# IAM: https://iam.datahub-prod.mestergruppen.cloud/swagger-ui/#/