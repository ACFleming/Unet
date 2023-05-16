// print "Hello World!\n"
// def x = [4,5,6]
// print x.max()
// import java.lang.Math;

 
// def n = new ArrayList<Integer>()
// n.clear()
// println n.size()
// println Math.sqrt(9)


// print distance([1,3,1],[3,4,3])

import java.text.SimpleDateFormat
def date = new Date()
def sdf = new SimpleDateFormat("HH-mm-ss")
def s =  sdf.format(date)
print s.getClass()
// println date.ge


def x = 0.1000
def y = String.format("%06.4f", x)

print y

print "${0..<(8/2)}"