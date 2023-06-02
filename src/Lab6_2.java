import mpi.MPI;

public class Lab6_2 {
    private static final int rowsA = 1000;
    private static final int colsA = 1000;
    private static final int colsB = 1000;
    private static final int MASTER = 0;
    public static void main(String[] args) {
        double[][] a = new double[rowsA][colsA];
        double[][] b = new double[colsA][colsB];
        double[][] c = new double[rowsA][colsB];
        MPI.Init(args);
        int numTasks = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        if (numTasks < 2) {
            System.out.println("Need at least two MPI tasks. Quitting...\n");
            MPI.COMM_WORLD.Abort(1);
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
            var startTime = System.currentTimeMillis();

            for(int dest = 1; dest <= numWorkers; dest++) {
                rows[0] = (dest <= extra) ? rowsPerThread + 1 : rowsPerThread;
                System.out.println("Sending " + rows[0] + " rows to task " + dest + " offset="+offset[0]);

                MPI.COMM_WORLD.Isend(offset, 0, 1, MPI.INT, dest, 0);
                MPI.COMM_WORLD.Isend(rows, 0, 1, MPI.INT, dest, 1);
                MPI.COMM_WORLD.Isend(a, offset[0], rows[0], MPI.OBJECT, dest, 2);
                MPI.COMM_WORLD.Isend(b, 0, colsA, MPI.OBJECT, dest, 3);

                offset[0] = offset[0] + rows[0];
            }
            System.out.println("Counclusion: ");
            for(int source = 1; source <= numWorkers; source++) {
                var offsetRequest = MPI.COMM_WORLD.Irecv(offset, 0, 1, MPI.INT, source, 4);
                var rowsRequest = MPI.COMM_WORLD.Irecv(rows, 0, 1, MPI.INT, source, 5);
                offsetRequest.Wait();
                rowsRequest.Wait();
                var matrixRequest = MPI.COMM_WORLD.Irecv(c, offset[0], rows[0], MPI.OBJECT, source, 6);
                matrixRequest.Wait();
            }
            var endTime = System.currentTimeMillis();

//            for(int i = 0; i < rowsA; i++) {
//                for (int j = 0; j < colsB; j++) {
//                    System.out.print(c[i][j] +" ");
//                }
//                System.out.print('\n');
//            }

            var dur = endTime-startTime;
            System.out.println("End with time: " + dur + " ms");
        } else {
            var offsetRequest = MPI.COMM_WORLD.Irecv(offset, 0, 1, MPI.INT, MASTER, 0);
            var rowsRequest = MPI.COMM_WORLD.Irecv(rows, 0, 1, MPI.INT, MASTER, 1);
            offsetRequest.Wait();
            rowsRequest.Wait();
            MPI.COMM_WORLD.Isend(offset, 0, 1, MPI.INT, MASTER, 4);
            MPI.COMM_WORLD.Isend(rows, 0, 1, MPI.INT, MASTER, 5);

            var aRequest = MPI.COMM_WORLD.Irecv(a, 0, rows[0], MPI.OBJECT, MASTER, 2);
            var bRequest = MPI.COMM_WORLD.Irecv(b, 0, colsA, MPI.OBJECT, MASTER, 3);
            aRequest.Wait();
            bRequest.Wait();
            for (int k = 0; k < colsB; k++) {
                for (int i = 0; i < rows[0]; i++) {
                    for (int j = 0; j < colsA; j++) {
                        c[i][k] += a[i][j] * b[j][k];
                    }
                }
            }


            MPI.COMM_WORLD.Isend(c, 0, rows[0], MPI.OBJECT, MASTER, 6);
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