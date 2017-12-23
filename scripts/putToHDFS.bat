call python generate_data.py 10000
call hadoop fs -mkdir /input 
call hadoop fs -put infile.txt /input/infile1.txt 
call hadoop fs -ls /input 
