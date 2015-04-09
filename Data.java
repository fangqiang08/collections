
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Data {
	
	public void divideTrainTest(int ratingNum, int trainNum, String fileName,
			String fileTrain, String fileTest) {
		int a[];
		a = GenerateRandomNum(ratingNum, trainNum);
		int b[] = new int[trainNum];
		System.arraycopy(a, 0, b, 0, trainNum);
		Arrays.sort(b);
		a = null;

		BufferedReader br = null;
		BufferedWriter bw1 = null;
		BufferedWriter bw2 = null;
		try {
			br = new BufferedReader(new FileReader(new File(fileName)));
			bw1 = new BufferedWriter(new FileWriter(new File(fileTrain)));
			bw2 = new BufferedWriter(new FileWriter(new File(fileTest)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String s;
		int count = 0;
		int k = 0;
		
		int testCount=0;
		int trainCount=0;
		
		try {
			while ((s = br.readLine()) != null) {
				if (count == b[k]) {
					bw1.write(s);
					bw1.newLine();
					k++;
					trainCount++;

				} else {
					bw2.write(s);
					bw2.newLine();
					testCount++;
				}
				count++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bw1.flush();
				bw1.close();
				bw2.flush();
				bw2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("trainCount="+trainCount);
		System.out.println("testCount="+testCount);
	}

	//   get the random number which is in [0,n]   获取0到n之间k个随机数
	public int[] GenerateRandomNum(int n, int k) {
		int i, t;
		int[] a = new int[n];
		for (i = 0; i < n; i++) {
			a[i] = i;
		}
		for (i = 0; i < k; i++) {
			Random rd = new Random();
			//  generate a num which is in[i,n)         生成一个[i,n)之间的随机数
			// t=i+rd.nextInt(n-i-1);
			t = i + rd.nextInt(n - i);
			swap(a, i, t);
		}

		return a;
	}

	public static void swap(int[] a, int i, int j) {
		int temp;
		temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}

}
