import Pycluster
if __name__ == "__main__":
	with open('all.txt') as file:
		array2d = [[int(digit) for digit in line.split()] for line in file]
	f1=open('kmeans_output.txt', 'w+')
	
	for iterationNo in range(0, 1000):	
		labels, error, nfound = Pycluster.kcluster(array2d, 10, transpose=0, npass=100)
		for i in range(0, labels.size):
			print >> f1, labels[i]
		print >> f1, 'X'



