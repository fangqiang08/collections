package com.fangqiang.myutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class RSVD {
	
	private static String SPLIT="\t"; 
	
	private static String TrainDataFile="F:\\Recommendation\\coding\\My\\recordWithRate2_itemMaped_train";
	
	private static String TestDataFile="F:\\Recommendation\\coding\\My\\recordWithRate2_itemMaped_test";

	private static String lofFile="F:\\Recommendation\\coding\\My\\RSVDlog.txt";
	
	private static int ratingNum=89280;
	private static int trainNum=62496;
	
	private static int userNum = 9861;
//			943;
	private static int itemNum = 12606;
//			1682;

	public static int[] factorNums;

	private static int loopNum = 500;

	List<String> modelTrain;
	List<String> modelTest;

	// the user-factor martix
	private static double[][] u;
	// the item-factor martix
	private static double[][] v;

	// private static double regularization;
	private static double learnRate1;
	private static double learnRate2;

	private static double[] regularization;

	// 保存每次训练集迭代时的MAE
	private static double[] trainMAEArray = new double[loopNum];
	private static double[] trainRMSEArray = new double[loopNum];

	// 保存每次测试集迭代时的MAE
	private static double[] testMAEArray = new double[loopNum];
	private static double[] testRMSEArray = new double[loopNum];
	
	private static HashMap<Integer, Integer> itemIdMap;

	static {
		
		factorNums = new int[] { 50 };
		regularization = new double[] {0.03};
	}

	public static void main(String[] args) {
//		System.out.println();
		Long time1=System.currentTimeMillis();
		/*Data data=new Data();
		itemIdMap=data.mapItemId(moviesOriginFile, movieMapFile15);*/
	//	data.divideTrainTest(ratingNum, trainNum, originFile, TrainDataFile, TestDataFile);
	//	System.out.println("划分数据完毕！！！");
		
		RSVD rsvd = new RSVD();
		learnRate1 = 0.005;
		learnRate2 = 0.005;
		for (int i = 0; i < factorNums.length; i++) {
			for (int j = 0; j < regularization.length; j++) {
				rsvd.myRSVD(factorNums[i],regularization[j], regularization[j], learnRate1, learnRate2);
			}
		}
		
		Long time2=System.currentTimeMillis();
		System.out.println("time:"+((time2-time1)/1000)+"s");

	}

	public void inital(int factorNum) {
		Random r = new Random();
		u = new double[userNum][factorNum];
		for (int i = 0; i < userNum; i++) {
			for (int j = 0; j < factorNum; j++) {
				u[i][j] = r.nextDouble() * 0.001;
			}
		}

		v = new double[itemNum][factorNum];
		for (int i = 0; i < itemNum; i++) {
			for (int j = 0; j < factorNum; j++) {
				v[i][j] = r.nextDouble() * 0.001;
			}
		}
		modelTrain = readData(TrainDataFile);
		modelTest = readData(TestDataFile);
		System.out.println("inital结束");
	}

	public void myRSVD(int factorNum, double regularization1, double regularization2,
			 double learnRate1, double learnRate2) {

		inital(factorNum);

		for (int k = 0; k < loopNum; k++) {
			for (String s : modelTrain) {
				String[] str = s.split(SPLIT);
				int userId = Integer.parseInt(str[0]);
				int itemId = Integer.parseInt(str[1]);
//				int itemMapId=itemIdMap.get(itemId);
				double rate = Double.parseDouble(str[2]);

				HashMap<String, String> map;
				
				double preRate = getDotProduct(u[userId - 1], v[itemId-1],
						factorNum);
				double e = rate - preRate;
				double[] oldU = Arrays.copyOf(u[userId - 1], factorNum);

				double[] temp1 = VectorAddVector(
						u[userId - 1],
						NumMulVector(
								learnRate1,
								VectorMinusVector(
										NumMulVector(e, v[itemId-1],
												factorNum),
										NumMulVector(regularization1,
												u[userId - 1], factorNum),
										factorNum), factorNum), factorNum);
				System.arraycopy(temp1, 0, u[userId - 1], 0, factorNum);

				double[] temp2 = VectorAddVector(
						v[itemId-1],
						NumMulVector(
								learnRate2,
								VectorMinusVector(
										NumMulVector(e, oldU, factorNum),
										NumMulVector(regularization2,
												v[itemId-1], factorNum),
										factorNum), factorNum), factorNum);
				System.arraycopy(temp2, 0, v[itemId-1], 0, factorNum);

			}
			// 计算每次的MAE和RMSE
			double MAE = 0.0;
			double RMSE = 0.0;
			int count = 0;
			for (String s : modelTrain) {
				String[] str = s.split(SPLIT);
				int userId = Integer.parseInt(str[0]);
				int itemId = Integer.parseInt(str[1])-1;
				double rate = Double.parseDouble(str[2]);
				
//				int itemMapId=itemIdMap.get(itemId-1);
				double prerate = getDotProduct(u[userId - 1], v[itemId],
						factorNum);
				MAE += Math.abs(rate - prerate);
				RMSE += Math.pow(rate - prerate, 2);
				count++;
			}
			MAE = MAE / count;
			RMSE = Math.sqrt(RMSE / count);

			trainMAEArray[k] = MAE;
			trainRMSEArray[k] = RMSE;

			predict(k, factorNum);

		}
		save(lofFile, factorNum, regularization1, regularization2);
		modelTrain = null;
		modelTest=null;
	}

	// 将集合中保存的MAE和RMSE写入到文件中
	public void save(String fileName, int factorNum, double regularization1,
			double regularization2) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fileName, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			for (int i = 0; i < loopNum; i++) {
				String dMAE = Double.toString(trainMAEArray[i]);
				String dRMSE = Double.toString(trainRMSEArray[i]);
				bw.write(dMAE+","+dRMSE+","+regularization1+","+factorNum+"TRAIN");
				bw.newLine();
			}
			
			for (int i = 0; i < loopNum; i++) {
				String dMAE = Double.toString(testMAEArray[i]);
				String dRMSE = Double.toString(testRMSEArray[i]);
				bw.write(dMAE+","+dRMSE+","+regularization1+","+factorNum+"TEST");
				bw.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 测试集上测试 MAE和RMSE
	public void predict(int k, int factorNum) {

		double MAE = 0.0;
		double RMSE = 0.0;
		int count = 0;

		for (String s : modelTest) {
			String[] str = s.split(SPLIT);
			int userId = Integer.parseInt(str[0]);
			int itemId = Integer.parseInt(str[1])-1;
			double rate = Double.parseDouble(str[2]);

//			int itemMapId=itemIdMap.get(itemId);
			double prerate = getDotProduct(u[userId - 1], v[itemId],
					factorNum);
			MAE += Math.abs(rate - prerate);
			RMSE += Math.pow(rate - prerate, 2);
			count++;
		}
		MAE = MAE / count;
		RMSE = Math.sqrt(RMSE / count);

		testMAEArray[k] = MAE;
		testRMSEArray[k] = RMSE;

	}

	// 两个向量相加
	//add two vector
	public double[] VectorAddVector(double[] a, double[] b, int factorNum) {
		double c[] = new double[factorNum];
		for (int i = 0; i < factorNum; i++) {
			c[i] = a[i] + b[i];
		}
		return c;
	}

	// 两个向量相减
	//subtract two vectors
	public double[] VectorMinusVector(double[] a, double[] b, int factorNum) {
		double c[] = new double[factorNum];
		for (int i = 0; i < factorNum; i++) {
			c[i] = a[i] - b[i];
		}
		return c;
	}

	// 数字与向量相乘
	//make a multiplication between a number and a vector
	public double[] NumMulVector(double a, double[] b, int factorNum) {
		double c[] = new double[factorNum];
		for (int i = 0; i < factorNum; i++) {
			c[i] = b[i] * a;
		}
		return c;
	}

	// 向量点积
	private static double getDotProduct(double[] p, double[] q, int factorNum) {
		double sum = 0;
		for (int i = 0; i < factorNum; i++) {
			sum += p[i] * q[i];
		}
		return sum;
	}

	public void initial(int factorNum) {
		Random r = new Random();

		u = new double[userNum][factorNum];
		for (int i = 0; i < userNum; i++) {
			for (int j = 0; j < factorNum; j++) {
				u[i][j] = r.nextDouble() * 0.001;
			}
		}

		v = new double[itemNum][factorNum];
		for (int i = 0; i < itemNum; i++) {
			for (int j = 0; j < factorNum; j++) {
				v[i][j] = r.nextDouble() * 0.001;
			}
		}

	}

	// read the training Data file
	public List<String> readData(String fileName) {
		File fileIn = new File(fileName);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String s;
		String[] str;
		int userId;
		int itemId;
		double rate;
		int counter = 0;

		List<String> model = new LinkedList<>();

		try {
			while ((s = br.readLine()) != null) {
				str = s.split(SPLIT);
				userId = Integer.parseInt(str[0]);
				itemId = Integer.parseInt(str[1]);
				rate = Double.parseDouble(str[2]);

				counter++;

				model.add(s);

			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * userNum=userIds.size(); itemNum=itemIds.size(); userIds=null;
		 * itemIds=null;
		 */

		System.out.println("读入数据完毕！读入数据" + model.size() + "条");
		System.out.println("counter="+counter);
		return model;
	}

}
