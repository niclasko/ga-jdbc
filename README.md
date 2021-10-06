# README #

JDBC driver for Google Analytics

Author: Niclas Kjäll-Ohlsson (niclasko@gmail.com)

### What is this repository for? ###

* Use simple SQL to fetch data from Google Analytics.

### Build ###

* ./buildjar.sh
* Self-contained JAR-file can then be found in build-directory

### Setup ###

In order to use the ga-jdbc driver a "OAuth 2.0 Client ID", with accompanying key file, needs to be created at https://console.cloud.google.com/apis/credentials?project=your-google-cloud-project. It needs to have permissions to access the Google Reporting API.

For detailed steps on how to create a new "OAuth 2.0 Client ID", please follow the steps here: https://developers.google.com/analytics/devguides/reporting/core/v4/quickstart/installed-java. After creating the "OAuth 2.0 Client ID" you can download it from the list of "OAuth 2.0 Client ID's" and use it for the ga-jdbc driver.

### Usage ###

In your Java code provide the following information:

* Driver class name: org.sebson.jdbc.GA.GADriver
* Connection string: jdbc:GA://?KEY_FILE_LOCATION=FULL_PATH_TO_KEY_FILE_FROM_ABOVE
* User/Pass: not applicable, but provide anything, e.g. dummy/dummy

### Code Example ###

The following is an example of Python code using the ga-jdbc driver to query Google Analytics and create a Pandas data frame from the query results. The query below selects from a Google Analytics view (GA View). The "OAuth 2.0 Client ID" user from above needs to be given permission to read from the GA View in order for this to work. To give access to the "OAuth 2.0 Client ID" user, please follow the below steps:

1. Login to https://analytics.google.com and go to Admin-console
2. For the web site in question and for the Google Analytics view in question click "View settings" and give "Read & Analyse"
 permission to your Google Analytics system user or regular user which is associated with the "OAuth 2.0 Client ID" user from above.
   
NOTE:
For information about which fields can be queried in Google Analytics, please refer to: https://ga-dev-tools.web.app/dimensions-metrics-explorer/

NOTE on SQL:
The where-clause needs to contain a date filter and only one filter on the form date between [date_modifier] and [date_modifier]. Change [date_modifier] to either today or nDaysAgo (where n is a number)
```
import jaydebeapi
import pandas as pd

conn = jaydebeapi.connect("org.sebson.jdbc.GA.GADriver",
                           "jdbc:GA://?KEY_FILE_LOCATION=./path_to_key_file/YOUR_KEY_FILE.json",
                           ["dummy", "dummypassword"],
                           "./path_to_ga_jdbc_driver/GA-jdbc.jar")

google_analytics_view_id = "your_google_view_id"

query = f"select ga:city, ga:month, ga:year, ga:country, ga:latitude, ga:longitude,
        ga:sessions, ga:users, ga:newUsers from '{google_analytics_view_id}'
        where date between 10DaysAgo and today"

stmt = self.ga_conn.jconn.createStatement()
rs = stmt.executeQuery(query)
rsmd = rs.getMetaData()
column_names = [rsmd.getColumnName(i+1) for i in range(rsmd.getColumnCount())]
records = []
while(rs.next()):
    records.append(
        [rs.getObject(i+1) for i in range(rsmd.getColumnCount())]
    )
df = pd.DataFrame(records, columns=column_names))
```