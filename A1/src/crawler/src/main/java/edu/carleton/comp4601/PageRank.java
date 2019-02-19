package edu.carleton.comp4601;

import Jama.Matrix;

public class PageRank {
	
	// Create transition matrix from adjacency matrix
	private static Matrix getProbMatrix(Matrix P) {
		// Alpha: likelihood of web surfer jumping to a random location (URL bar)
		final double ALPHA = 0.15;
		int N = P.getColumnDimension();
		double[][] Parray = P.transpose().getArray();
		for (double[] row:Parray) {
			double sum = 0;
			// Get the number of 1's in the row
			for (int i = 0; i < N; i++) {
				sum += row[i];
			}
			// Case 1: No 1's in row
			if (sum == 0) {
				for (int i = 0; i < N; i++) {
					row[i] = 1./N;
				}
			// Otherwise divide each 1 by number of 1's in the row
			} else {
				for (int i = 0; i < N; i++) {
					row[i] /= sum;
				}
			}
		}
		P = new Matrix(Parray);
		P.timesEquals(1-ALPHA);
		P.plusEquals(new Matrix(N, N, ALPHA/N));
		return P;
	}
	
	// Takes an adjacency matrix of directed links
	// Computes and prints page ranks
	public static Matrix computePageRank(Matrix A) {
//		A.print(5, 3);
		// Work on this probability or "transition" matrix
		Matrix P = getProbMatrix(A.copy());
//		P.print(5, 3);
		// Initial probabilities: [1., 0., 0., ... , 0.]
		double[] x0 = new double[P.getColumnDimension()];
		x0[0] = 1.;
		// Work on this vector
		Matrix x = new Matrix(x0, P.getColumnDimension()).transpose();
		// Loop until convergence
		int i;
		double err = Double.MAX_VALUE;
		double tolerance = 1.0e-5;
		int maxIter = 1000;
		for (i = 0; i < maxIter && err > tolerance; i++) {
			Matrix xPrev = x.copy();
			x = x.times(P);
			// Normalize
			x.timesEquals(1./x.normInf());
			err = Math.abs(x.minus(xPrev).normInf());
		}
		System.out.printf("PageRank converged after %d iterations.", i);
		x.print(5, 3);
		return x;
	}
	
	public static void main(String[] args) {
		// Test case
		double[][] array = {{0, 1, 0},{1, 0, 1},{0, 1, 0}};
		Matrix A = new Matrix(array);
		computePageRank(A);
	}
}
