#!/bin/bash
#
# Use this shell script to compile (if necessary) your code and then execute it. Below is an example of what might be found in this file if your program was written in Python
#
#python ./src/find_political_donors.py ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt

# go to src directory
cd src;

# compile the java files, make sure you are using java 8
javac *.java;
java FindDonors ./input/itcont.txt ./output/medianvals_by_zip.txt ./output/medianvals_by_date.txt;

cd ..;
# print part of output
echo "+++++++++++++++++++++++++";
# 10 rows of cal_by_zip
echo "medianvals_by_zip.txt";
echo "-------------------------";
cat output/medianvals_by_zip.txt | head -10;

echo "-------------------------";

# 10 rows of cal_by_date
echo "medianvals_by_date.txt";
echo "-------------------------";
cat output/medianvals_by_date.txt | head -10;

echo "+++++++++++++++++++++++++";