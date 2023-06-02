import java.util.Random;

public class MatrixMultiplication {
    public static void main(String[] args) {
        int rowsA = 1000;
        int colsA = 1000;
        int colsB = 1000;

        double a[][] = createMatrix(rowsA,colsA,1);
        double b[][] = createMatrix(rowsA,colsB,1);
        double c[][] = new double[rowsA][colsB];


        var start = System.currentTimeMillis();
         c = multiplyMatrices(a, b);
        var end = System.currentTimeMillis();
        var dur = end - start;
        System.out.println("time: " + dur);
        //printMatrix(c);

    }


        public static double[][] multiplyMatrices( double[][] a,double[][] b){
            int rowsA = a.length;
            int colsA = a[0].length;
            int colsB = b[0].length;

            double[][] c = new double[rowsA][colsB];

            for (int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < colsA; k++) {
                        c[i][j] += a[i][k] * b[k][j];
                    }
                }
            }

            return c;
        }
    public static double[][] createMatrix(int rows, int cols, int value) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = value;
            }
        }
        return matrix;
    }
    public static void printMatrix(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}