# -*- coding: utf8 -*-
"""
Description: This programm processes the already dependency parsed input from the Database and clusters it
@author: Dagmar Gromann
"""

import pymysql.cursors
import numpy as np

DBhost = "DBhost"
DBname = "DBname"
DBuser ="DBuser"
DBpassword = "DBpassword"

'''
Method to load data and basis to create raw frequency affinity matrix based on verb-preposition pairs
'''
def loadPrepDB(db):
    nounDict = {}
    connection = pymysql.connect(DBhost,DBuser,DBpassword,DBname)
    query = """SELECT verb, prep, noun FROM """+db
    with connection.cursor() as cursor:
        cursor.execute(query)
        connection.commit()
    for verb, prep, noun in cursor:
        if  verb+"-"+prep not in nounDict.keys():
            nounDict[verb+"-"+prep] = {noun : 1}
        else: 
            relations = nounDict[verb+"-"+prep]
            if noun in relations.keys():
                relations[noun] += 1
            else:
                relations[noun] = 1
            nounDict[verb+"-"+prep] = relations
    print("Done loading from DB!", len(nounDict))
    return nounDict


#Iterate over lines(texts) in the file and present each text with a n-dimensional vector according to provided list words
#If the k-th vector has a value i on j-th place it means the word words[j] appears i times in this k-th text
#Also exports the ground trouth labels 
def get_M_fromDB():
    nounDict = loadPrepDB("europarl_en_talmy")
    #nounDict = loadPrepDB("europarl_sv_prep")
    #nounDict = loadPrepDB("europarl_de_prep")
    
    reducedMatr = {}
    n = len(nounDict)
    M = []
    label_names = []
    labels = []
    relations = []
    for key, value in nounDict.items():
        for k, v in value.items(): 
            if v > 10 and key not in reducedMatr.keys():
                reducedMatr[key] = {k: v}
            if v > 10 and key in reducedMatr.keys():
                rels = reducedMatr[key]
                rels[k] = v
                reducedMatr[key] = rels
    
    for key, value in reducedMatr.items():
        for k1, v1 in value.items():
            if k1 not in relations:
                relations.append(k1)
    
    for key, value in reducedMatr.items():
        vector = np.zeros(len(relations))
        label = key
        if label in label_names:
            labels.append(label_names.index(label))
        else: 
            label_names.append(label)
            labels.append(label_names.index(label))
        for k1, v1 in value.items():
            vector[relations.index(k1)] = v1
        M.append(vector)

    M = np.array(M)
    labels = np.array(labels)
    print("Done loading into matrix!")
    return M, labels, label_names, relations, reducedMatr
