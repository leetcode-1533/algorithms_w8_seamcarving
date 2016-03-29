import java.awt.Color;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Stopwatch;
import edu.princeton.cs.algs4.ResizingArrayStack;

public class SeamCarver {
    private Color[][] pic;
    private int height;
    private int width;
    private double[][] P_energy, Inv_energy;
    
    private void initEnergy() {
        for(int i = 0; i < height; i++) { // Iterator for rows
            for(int j = 0; j < width; j++) { // Iterator for Columns
                if(j == 0 || j == width - 1  || i == 0 || i == height - 1)
                    P_energy[i][j] = 1000;
                else
                    P_energy[i][j] = Math.sqrt(centerDiff(j, i));
            }
        }
    }
        
    private int centerDiff(int col, int row) {
        Color above = pic[row - 1][col];
        Color below = pic[row + 1][col];      
        int bdiff = above.getBlue() - below.getBlue();
        int rdiff = above.getRed() - below.getRed();
        int gdiff = above.getGreen() - below.getGreen();
        int vertDiff = bdiff * bdiff + rdiff * rdiff + gdiff * gdiff;

        above = pic[row][col - 1];
        below = pic[row][col + 1];    
        bdiff = above.getBlue() - below.getBlue();
        rdiff = above.getRed() - below.getRed();
        gdiff = above.getGreen() - below.getGreen();
        int horiDiff = bdiff * bdiff + rdiff * rdiff + gdiff * gdiff;
        
        return vertDiff + horiDiff;        
    }
    
    public SeamCarver(Picture picture) {
        height = picture.height();
        width = picture.width();
        pic = new Color[height][width];
        
        P_energy = new double[height][width];
        Inv_energy = new double[width][height];

        initEnergy();
        
        for(int col = 0; col < picture.width(); col++) {
            for(int row = 0; row < picture.height(); row++) {
                pic[row][col] = picture.get(col, row);
            }
        }
        
        for(int i = 0; i < P_energy.length; i++) {
            for(int j = 0; j < P_energy[0].length; j++) {
                Inv_energy[j][i] = P_energy[i][j];              
            }       
        }   
    }
    
    private void Vert_Modified() {
        int height = P_energy.length;
        int width = P_energy[0].length;
        
        if(P_energy.length != Inv_energy[0].length) {
            Inv_energy = new double[width][height];

            for(int i = 0; i < P_energy.length; i++) {
                for(int j = 0; j < P_energy[0].length; j++) {
                    Inv_energy[j][i] = P_energy[i][j];              
                }       
            }          
        }
        
    }
    
    private void Hori_Modified() {
        int height = Inv_energy.length;
        int width = Inv_energy[0].length;
        
        if(P_energy.length != Inv_energy[0].length) {
            P_energy = new double[width][height];

            for(int i = 0; i < Inv_energy.length; i++) {
                for(int j = 0; j < Inv_energy[0].length; j++) {
                    P_energy[j][i] = Inv_energy[i][j];              
                }       
            }          
        }
    }
    
    public int[] findVerticalSeam() {
        Hori_Modified();
        return findVerticalSeam(P_energy);
    }
    
    public int[] findHorizontalSeam() {
        Vert_Modified(); //lazy copy
        return findVerticalSeam(Inv_energy);
    }
    
    public int[] findVerticalSeam(double[][] energy) {  
        int height = energy.length;
        int width = energy[0].length;
        
        double[][] dist = new double[height][width];
        int[][] verticTo = new int[height][width];
        
        for(int i = 1; i < height; i++ ) {
            for(int j = 0; j < width; j++) {
                dist[i][j] = -1;
            }
        }
        
        for(int j = 0; j < width; j++) {
            dist[0][j] = energy[0][j];
        }
        
        for(int j = 0; j < width; j++) {
            verticTo[0][j] = 0;
        }
        
        vertSP(energy, dist, verticTo);
        
        ResizingArrayStack<Integer> rev_route = new ResizingArrayStack<Integer>();
        int endPoint = minIndex(dist[height - 1]);
        rev_route.push(endPoint);

        int next = verticTo[height - 1][endPoint];

        for(int i = height - 2; i >= 0; i--) {
            rev_route.push(next);
            next = verticTo[i][next];
        }
        
        int[] route = new int[height];
        for(int i = 0; i < height; i++) 
            route[i] = rev_route.pop();
         
        return route;         
    }
    
    private int minIndex(double[] array) {
        double min = array[0];
        int loc = 0;
        
        for(int i = 1; i < array.length; i++) {
            if(array[i] < min) {
                loc = i;
                min = array[i];            
            }        
        }
        return loc;
    }
    
    private void vertSP(double[][] energy, double[][] dist, int[][] verticTo) {
        for(int i = 0; i < energy.length - 1; i++) { // for next row
            for(int j = 0; j < energy[0].length; j++) {
                int[] curPoint = new int[] {i, j};
                int[] nextPs = adj(j, energy);
                for(int nP : nextPs) {
                    refresh(curPoint, new int[] {i + 1, nP}, energy, dist, verticTo);
                }
            }
        }
        
    }
    
    private int[] adj(int x, double[][] energy) {
        // for point at column x, give it adjacency neighbors at next row
        // Constrained by width columns<-> width, 
        if(x == 0) {
            return new int[] {0, 1};
        } else if(x == energy[0].length - 1) {
            return new int[] {energy[0].length - 2, energy[0].length - 1};
        } else {
            return new int[] { x - 1, x, x + 1};
        }      
    }
    
    private void refresh(int[] vFrom, int[] vTo, double[][] energy, double[][] dist, int[][] verticTo) {
        // vFrom[0] for row, vFrom[1] for column
        if(dist[vFrom[0]][vFrom[1]] + energy[vTo[0]][vTo[1]] < dist[vTo[0]][vTo[1]] || dist[vTo[0]][vTo[1]] < 0) {
            verticTo[vTo[0]][vTo[1]] = vFrom[1];  // record the above row column
            dist[vTo[0]][vTo[1]] = dist[vFrom[0]][vFrom[1]] + energy[vTo[0]][vTo[1]];
        }
    }
    
    public void removeVerticalSeam(int[] seam, double[][] energy, Color[][] pict) {
        for(int i = 0; i < energy.length; i++) {
            double[] temp = new double[energy[0].length - 1];
            for(int j = 0; j < energy[0].length - 1; j++) {
                if(j < seam[i])
                    temp[j] = energy[i][j];
                else if(j > seam[i])
                    temp[j] = energy[i][j - 1];
            }
            energy[i] = temp;
        } 
        
        for(int i = 0; i < pict.length; i++) {
            Color[] temp = new Color[pict[0].length - 1];
            for(int j = 0; j < pict[0].length - 1; j++) {
                if(j < seam[i])
                    temp[j] = pict[i][j];
                else if(j > seam[i])
                    temp[j] = pict[i][j - 1];
            }
            pict[i] = temp;
        } 
    }
    
    public void removeVerticalSeam(int[] seam) {
        width--;

        removeVerticalSeam(seam, P_energy, pic);
    }
    
    public void removeHorizontalSeam(int[] seam) {
        height--;
        
        Color[][] Inv_pic = new Color[pic[0].length][pic.length];
        for(int i = 0; i < pic.length; i++) {
            for(int j = 0; j < pic[0].length; j++) {
                Inv_pic[j][i] = pic[i][j];
            }
        }
        
        removeVerticalSeam(seam, Inv_energy, Inv_pic);
        
        for(int i = 0; i < pic.length; i++) {
            Color[] temp = new Color[pic[0].length];
            for(int j = 0; j < pic[0].length; j++) {
                temp[j] = Inv_pic[j][i];
            }
            pic[i] = temp;
        }
    }
    
    public double energy(int x, int y) {
        // at column x, row y
        Hori_Modified();
        return P_energy[y][x]; // energy per row is picture per row
    }
    
    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
    
//    public Picture picture() {
//        return pic;
//    }
    
    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);
        StdOut.printf("image is %d pixels wide by %d pixels high.\n", picture.width(), picture.height());
        
        SeamCarver sc = new SeamCarver(picture);
        
        StdOut.printf("Vroute \n");  
        int[] vroute = sc.findVerticalSeam();
        for(int item : vroute)
            StdOut.printf("%3d ", item);
        
        StdOut.printf("\n Hroute \n");  
        int[] hroute = sc.findHorizontalSeam();
        for(int item : hroute)
            StdOut.printf("%3d ", item);
    }
}
