Unfortunately I did not have time to implement task 3 and the optimisations

Task 2 logic:
The logic for extracting join conditions is as follows;
The decideContitionType function in SelectStatement class, line 85, decides whether a condition involves one or more
tables and adds the conditions to the relevant list of conditions. This gives us a list of select conditions and a
seperate list of join conditions. I form these lists for each table and then call join operator with the two lists

Apologies for the short description but I have 30 minutes left to submit, The code is well commented
the logic can be found at
SelectStatement line 85 to 290
