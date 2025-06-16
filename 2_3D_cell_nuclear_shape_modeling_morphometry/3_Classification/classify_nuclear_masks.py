#!/usr/bin/env python
# coding: utf-8
#
# classifies a cell using morphometric measures
# requires Python with NumPy and pickled scikit-learn classifier
#
# 1st parameter - path to input morphometry measures
# 2nd parameter - path to directory with pickled scikit-learn classifier
# 5th parameter - path to output file with output label and probability
#
# author: Alexandr Kalinin, 2017

import argparse
import pickle

from numpy import genfromtxt


def main(morphometry_file, classifier_file, output_file):
    # measures = []
    # for morphometry_file in morphometry_files:
    morphometry = genfromtxt(morphometry_file, delimiter="\t")
    measures = morphometry[1:, 1:]
    # measures.append(morphometry[1:, 1:][0])
    # print(str(measures))
    clf_models = pickle.load(open(classifier_file, "rb"))
    # sc = clf_models['scaler']
    # nr = clf_models['normalizer']
    lb = clf_models["label_binarizer"]
    clf = clf_models["classifier"]
    # measures_scaled = nr.transform(sc.transform(measures))
    probs = clf.predict_proba(measures)
    preds = lb.inverse_transform(probs, threshold=0.5)
    f = open(output_file, "w")
    f.write("%s,%0.2f\n" % (str(preds[0]), max(probs[0][0], probs[0][1])))
    f.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Nuclear morphometry-based cell type classification"
    )
    parser.add_argument("morphometry", metavar="M", nargs=1, help="morphometry")
    parser.add_argument("-clf", metavar="C", nargs=1, help="classifier")
    parser.add_argument("-o", metavar="O", nargs=1, help="output")
    args = parser.parse_args()
    print(args)
    main(args.morphometry[0], args.clf[0], args.o[0])
