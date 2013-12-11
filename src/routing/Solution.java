/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

/**
 *
 * @author mars
 */
import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;
import javax.swing.Spring;

public class Solution {
    /* Head ends here */

    static boolean debug = false;

    static void displayPathtoPrincess(int n, String[] grid) {
        int grid_len = grid.length;

        char[][] cgrid = new char[n][n];
        int mid_c = (int) Math.ceil(cgrid.length / 2);
        for (int i = 0; i < cgrid.length; i++) {
            for (int j = 0; j < cgrid.length; j++) {
                if (i == mid_c && j == mid_c) {
                    cgrid[i][j] = 'm';
                    System.out.printf("%c", cgrid[i][j]);

                } else if ((j == n - 1) && (i == n - 1)) {
                    cgrid[i][j] = 'p';
                    System.out.printf("%c", cgrid[i][j]);
                    if (debug) {
                        System.out.printf("runs: %d, val=%s\n", i, grid[i]);
                    }
                } else {
                    cgrid[i][j] = '-';
                    System.out.printf("%c", cgrid[i][j]);
                    if (debug) {
                        System.out.printf("runs: %d, val=%s\n", i, grid[i]);
                    }
                }


            }
//              if (debug) {
            System.out.printf("\n");
//                }
        }
        System.out.printf("======= end of grid =============\n");
        /**
         * I am at grid[grid_len/2][grid_len/2]
         */
        //get to the one corner
        int mid = (int) Math.ceil(cgrid.length / 2);
        //go to down first
        for (int i = mid; i < cgrid.length; i++) {
            
            cgrid[i - 1][mid] = '-';
            System.out.printf("DOWN\n");
            cgrid[i][mid] = 'm';
            if (true) {
                System.out.printf("runs: %d, val=%c\n", i, cgrid[i][mid]);
            }
        }

//        go down
        for (int j = mid; j < cgrid.length; j++) {
            if (j < cgrid.length - 1) {
                if (debug) {
                    System.out.printf("nopt yet...\n");
                }
                //go right
                cgrid[cgrid.length - 1][j - 1] = '-';
                cgrid[cgrid.length - 1][j] = 'm';
                System.out.printf("RIGHT\n");
            } else {
                //check to see if p is here
                if (cgrid[cgrid.length - 1][j] != 'p') {
                    //go up and check corners
                } else {
                    System.out.printf("done\n");
                }
            }
        }
//        System.out.printf("LEFT\n");
//        System.out.printf("RIGHT\n");
//        System.out.printf("UP\n");
//        System.out.printf("DOWN\n");


    }
    /* Tail starts here */

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int m;
        m = in.nextInt();
        String grid[] = new String[m];
        for (int i = 0; i < m; i++) {
            grid[i] = "";// in.next();
        }

        displayPathtoPrincess(m, grid);


    }
}