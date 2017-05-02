Mobile Application - Lab 10

INFORMATION FOR THE GRADER:

Each time the StocksAdapter goes to update the ListView, it accesses a MySQLWorkbench database that I have hosted on AWS (through a PHP web service API that is also stored on AWS), so it knows what stock symbols the user has saved in order to populate the list. 

Because the service is required to update the price information every minute or so, at that minute point that the update happens, a Java class will access the stocks API based on what stock symbols are stored in the user's list, and populate an array in the Java class with those updated prices. It will then use that array to update the prices to their corresponding stock symbol in the database, so that when the ListView builds itself again, it is accessing the database which has the most updated prices.

The charts are always as up-to-date as possible because they alone are accessing the internet each time the image loads. 

There is currently no delete button, so to actually get rid of stocks from the list, I have to remove them from the database myself. I will clear the database upon submitting the lab.

Dropbox link to the PHP "web service" file: https://www.dropbox.com/sh/icp178vvjr94du9/AABw2nqmyWaqr5ZQEtwoSixya?dl=0
