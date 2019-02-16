package edu.carleton.comp4601;

import Jama.Matrix;

public class PageRank {
	
	private static Matrix getProbMatrix(Matrix P) {
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
		double alpha = 0.5;
		P.timesEquals(1-alpha);
		P.plusEquals(new Matrix(N, N, alpha/N));
		return P;
	}
	
	public static Matrix computePageRank(Matrix A) {
		System.out.println("adjacency matrix:");
		A.print(5, 3);
		Matrix P = getProbMatrix(A.copy()); // Work on this matrix
		System.out.println("probability matrix:");
		P.print(5, 3);
		
		double tolerance = 1.0e-3;
		int maxIter = 1000;
		
		double err = Double.MAX_VALUE;
		System.out.println("computing PageRank values:");
		double[] x0 = new double[P.getColumnDimension()];
		x0[0] = 1.;
		Matrix x = new Matrix(x0, P.getColumnDimension()).transpose();
		x.print(5,  3);
		int i;
		for (i = 0; i < maxIter && err > tolerance; i++) {
			Matrix xPrev = x.copy();
			x = x.times(P);
			x.timesEquals(1./x.normInf());
			err = Math.abs(x.minus(xPrev).normInf());
		}
		System.out.printf("converged after %d iterations:", i);
		x.print(5, 3);
		return x;
	}
	
	public static void main(String[] args) {
		double[][] array = {{0, 1, 0},{1, 0, 1},{0, 1, 0}};
		Matrix A = new Matrix(array);
		computePageRank(A);
	}
}
