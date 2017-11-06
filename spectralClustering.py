# -*- coding: utf-8 -*-
"""
Description: Main class of clustering algorithm that directs calls and writes results to output
Created in January 2017
@author: Dagmar Gromann
"""

import numpy as np
import scipy.spatial.distance as dist

import spectral
import similarity as sim
import preprocessing as pp

from scipy.sparse.csgraph import minimum_spanning_tree as mst

#Path and variables
MAINPATH="results/"
LANGUAGE="en"

def printResults(similarity, label_names, labels_train_pred, nounDict, k, algorithm, graph, statistics): 
	results = open(MAINPATH+"/"+LANGUAGE+"_"+similarity+"_"+str(k)+"_"+algorithm+"_"+graph+".txt", "w")
	cluster_results = {}
	for i in range(len(label_names)):
	 	cluster_results[label_names[i]] = labels_train_pred[i]
    	
	v = {}
	for key, value in sorted(cluster_results.items()):
		v.setdefault(value, []).append(key)

	averageClusterSize = 0
	for key, value in v.items():
		results.write(str(key)+": "+str(value)+"\n")
		averageClusterSize += len(value)
		if len(value)>24:
			print(key, "and length", len(value))
		counter = 1
		for it in value:
			results.write(str(counter)+" "+it+": "+str(nounDict[it])+"\n")
			counter += 1
		results.write("\n")

	print("Average cluster size: ", averageClusterSize/k)
	statistics.write(str(k)+" "+algorithm+" "+graph+"\n")
	statistics.write("Average cluster size: "+str(averageClusterSize/k)+"\n")
	statistics.write("\n")

def main():
	statistics = open(MAINPATH+"/"+"statistics_ppmi.txt", "w")
			
	M, labels, label_names, relations, nounDict = pp.get_M_fromDB()
	
	#Choose a method to build your similarity matrix
	#Term Frequency-Inverse Document Frequency
	M_ppmi = sim.get_tf_idf_M(M, "raw", "c", norm_samps=True)
	similarity = "tfidf"
	
	#Jensen Shanon Divergence
	#M_ppmi = sim.JensenShanon(M)
	#similarity = "jsd" 

	#Positive Pointwise Mutual Information
	#M_ppmi = sim.raw2ppmi(M)
	#similarity = "ppmi"

	#Change this value according to expected number of clusters required
	#We tested with 50, 100, 200, 300 based on our dataset
	k = 300
	print("Length features and labels:", len(M_ppmi), len(labels))

	c = spectral.spectral(M_ppmi, labels, sim.cos_s, dist.euclidean)
	#c = spectral.spectral(X, Y, sim.gauss_s, dist.euclidean)
	
	#Fully connceted 
	c.full_graph("cosine")
	print(c.graph)
	for algo in [c.norm_rw_sc, c.norm_sym_sc]:
		kmeans, kmeans_pred = algo(k)
		print("Kmeans pred:", kmeans_pred, len(kmeans_pred))
		labels_train_pred = kmeans.labels_.astype(np.int)
		print(c.clustering)
		printResults(similarity, label_names, labels_train_pred, nounDict, k, c.clustering, c.graph, statistics)

	n = M.shape[0] 
	
	'''cosine and knn mutual / gauss mutual'''
	number = int(2*(n/np.log(n)))
	'''gaus non-mutual'''
	#number = int((n/np.log(n)))
	 
	#K nearest neighbors
	c.kNN_graph(number, "euclidean", False)
	print(c.graph)
	for algo in [c.norm_rw_sc, c.norm_sym_sc]:
		kmeans, kmeans_pred = algo(k)
		print("Kmeans pred:", kmeans_pred, len(kmeans_pred))
		labels_train_pred = kmeans.labels_.astype(np.int)
		print(c.clustering)
		printResults(similarity, label_names, labels_train_pred, nounDict, k, c.clustering, c.graph, statistics)
   	
	
   	#Epsilon
	T = mst(c.W)
	A = T.toarray().astype(float)
	eps = np.min(A[np.nonzero(A)])
	print("eps", eps)
	c.eps_graph(eps)
	print(c.graph)
	for algo in [c.norm_rw_sc, c.norm_sym_sc]:
		kmeans, kmeans_pred = algo(k)
		print("Kmeans pred:", kmeans_pred, len(kmeans_pred))
		labels_train_pred = kmeans.labels_.astype(np.int)
		print(c.clustering)
		printResults(similarity, label_names, labels_train_pred, nounDict, k, c.clustering, c.graph, statistics)
	
	statistics.close()

if __name__ == '__main__':
 	main()