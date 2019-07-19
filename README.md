# hadoop_project

detail see the screenshot


1. generate 10G test file 

/bin/hadoop jar  /home/hadoop/hadoop_file_system/hadoop_project/hadoop-2.6.5-src/hadoop-mapreduce-project/csc550/target/ITU_CSC_550_2019_Summer-2.6.5.jar  gen  10000000 CSC550_10G


time: 33s to generate 


2. sort 

/bin/hadoop jar  /home/hadoop/hadoop_file_system/hadoop_project/hadoop-2.6.5-src/hadoop-mapreduce-project/csc550/target/ITU_CSC_550_2019_Summer-2.6.5.jar  sort CSC550_10G CSC500_Sort_Result


time:2minus 24s 


3. validation


./bin/hadoop jar  /home/hadoop/hadoop_file_system/hadoop_project/hadoop-2.6.5-src/hadoop-mapreduce-project/csc550/target/ITU_CSC_550_2019_Summer-2.6.5.jar validate  CSC500_Sort_Result CSC500_Result_Validation

time: 22 to validate
 
