import mpi.MPI;

public class Main {
    private static final int rowsA = 4;
    private static final int colsA = 4;
    private static final int colsB = 4;
    private static final int MASTER = 0;
    private static final int FROM_MASTER = 1;
    private static final int TO_MASTER = 2;
    public static void main(String[] args) {
        double[][] a = new double[rowsA][colsA];
        double[][] b = new double[colsA][colsB];
        double[][] c = new double[rowsA][colsB];
        MPI.Init(args);
        int numTasks = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        if (numTasks < 2) {
            System.out.println("Need at least two MPI tasks. Quitting...\n");
            MPI.Finalize();
            System.exit(1);

        }
        int numWorkers = numTasks - 1;
        int[] offset = {0};
        int[] rows = {0};
        if(rank == MASTER) {
           System.out.println("Started with " + numTasks + " tasks");
           a = InputMautix(rowsA,colsA,1);

            b = InputMautix(colsA,colsB,1);


            int rowsPerThread = rowsA / numWorkers;
            int extra = rowsA % numWorkers;
            var start = System.currentTimeMillis();
            for(int dest = 1; dest <= numWorkers; dest++) {
                rows[0] = (dest <= extra) ? rowsPerThread + 1 : rowsPerThread;
                System.out.println("Sending " + rows[0] + " rows to task " + dest + " offset="+offset[0]);

                MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(a, offset[0], rows[0], MPI.OBJECT, dest, FROM_MASTER);
                MPI.COMM_WORLD.Send(b, 0, colsA, MPI.OBJECT, dest, FROM_MASTER);

                offset[0] = offset[0] + rows[0];
            }
            System.out.println("Counclusion: ");
            for(int source = 1; source <= numWorkers; source++) {
                MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, source, TO_MASTER);
                MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, source, TO_MASTER);
                MPI.COMM_WORLD.Recv(c, offset[0], rows[0], MPI.OBJECT, source, TO_MASTER);
            }
            var end = System.currentTimeMillis();
            var dur = end - start;
            System.out.println("End with time: " + dur + " ms");
            for(int i = 0; i < rowsA; i++) {
                for (int j = 0; j < colsB; j++) {
                    System.out.print(c[i][j] +" ");
                }
                System.out.print('\n');
            }
        } else {
            MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(a, 0, rows[0], MPI.OBJECT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(b, 0, colsA, MPI.OBJECT, MASTER, FROM_MASTER);


            for (int k = 0; k < colsB; k++) {
                for (int i = 0; i < rows[0]; i++) {
                    for (int j = 0; j < colsA; j++) {
                        c[i][k] += a[i][j] * b[j][k];
                    }
                }
            }

            MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, MASTER, TO_MASTER);
            MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, MASTER, TO_MASTER);
            MPI.COMM_WORLD.Send(c, 0, rows[0], MPI.OBJECT, MASTER, TO_MASTER);
        }

        MPI.Finalize();
    }
    public static double[][] InputMautix(int rows,int cols,double value)
    {
        double[][] result = new double[rows][cols];
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = value;
            }
        }
        return result;
    }
}