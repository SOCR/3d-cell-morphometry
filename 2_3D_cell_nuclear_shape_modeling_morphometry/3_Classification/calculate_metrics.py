#!/usr/bin/env python
# coding: utf-8
#
# Merge classification results for all cells and report image-level label,
# accuracy, and average probability. Requires Python with NumPy.
#
# 1st parameter - path to list file with paths to all cells classification result files
# 2nd parameter - path to output file with results
#
# author: Alexandr Kalinin, 2017

from __future__ import division

import argparse
from typing import List, Tuple

from numpy import genfromtxt


def main(res_file_lst, output_file):
    TRUE_LABEL, FALSE_LABEL = "ss", "prolif"
    result_files = genfromtxt(res_file_lst, delimiter="\n", dtype=str)

    def _parse_result_file(path: str) -> Tuple[str, float]:
        label, prob = genfromtxt(path, delimiter=",", dtype=str)
        return label, float(prob)

    results: List[Tuple[str, float]] = [_parse_result_file(f) for f in result_files]

    acc = sum(r[0] == TRUE_LABEL for r in results) / float(len(results))
    label = TRUE_LABEL if acc >= 0.5 else FALSE_LABEL

    probs = [r[1] for r in results if r[0] == label]
    avg_prob = sum(probs) / len(probs) if probs else 0.0

    with open(output_file, "w", encoding="utf-8") as fh:
        fh.write(
            "Image level prediction: %s, Accuracy: %0.2f, Avg. probability: %0.2f\n\n"
            "Probabilities per cell:\n" % (label, acc, avg_prob)
        )
        for res in results:
            fh.write("Label: %s, prob: %0.2f\n" % (res[0], res[1]))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Collecting results of cell type classification"
    )
    parser.add_argument("results", metavar="M", nargs=1, help="classification results")
    parser.add_argument("-o", metavar="O", nargs=1, help="output")
    args = parser.parse_args()
    print(args)
    main(args.results[0], args.o[0])
