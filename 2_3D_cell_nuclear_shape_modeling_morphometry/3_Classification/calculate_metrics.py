#!/usr/bin/env python
# coding: utf-8
#
# merges the results of classification of all cells and reports image-level output label,
#  cell-level accuracy and average probability
# requires Python with NumPy
#
# 1st parameter - path to list file with paths to all cells classification result files
# 2nd parameter - path to output file with results
#
# author: Alexandr Kalinin, 2017

from __future__ import division

import argparse

from numpy import genfromtxt


def main(res_file_lst, output_file):
    TRUE_LABEL, FALSE_LABEL = "ss", "prolif"
    result_files = genfromtxt(res_file_lst, delimiter="\n", dtype="str")
    results = []
    for f in result_files:
        res = genfromtxt(f, delimiter=",", dtype=(str, float))
        results.append(res)
    print(str(results))
    acc = sum(x[0] == TRUE_LABEL for x in results) / len(results)
    label = TRUE_LABEL if acc >= 0.5 else FALSE_LABEL
    avg_prob = sum(float(x[1]) for x in results if x[0] == label) / (acc * len(results))
    f = open(output_file, "w")
    f.write(
        "Image level prediction: %s, Accuracy: %0.2f, Avg. probability: %0.2f\n\nProbabilities per cell:\n"
        % (label, acc, avg_prob)
    )
    for res in results:
        f.write("Label: %s, prob: %0.2f\n" % (res[0], float(res[1])))
    f.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Collecting results of cell type classification"
    )
    parser.add_argument("results", metavar="M", nargs=1, help="classification results")
    parser.add_argument("-o", metavar="O", nargs=1, help="output")
    args = parser.parse_args()
    print(args)
    main(args.results[0], args.o[0])
