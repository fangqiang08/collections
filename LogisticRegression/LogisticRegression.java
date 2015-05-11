package LogisticRegression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import utils.DataIO;

/**
 * Description :algorithm to implement the Logistic Regression
 *
 * @author fang
 * 2015.05.11
 * 
 */
public class LogisticRegression {
	
	private final static int FACTOR=4;
	
	private int iteratorNum=30;
	
	private static double thetaJ[]=new double[FACTOR+1];
	private double thetaJ_Temp[]=new double[FACTOR+1];
	
	private double alpha=0.0001;						//the learn rate
	private double lamaba=0.0001;						//the penalty factor
	
	private List<List<Double>> trainList;
	private List<List<Double>> testList;
	
	private static List<String> OutputResult;
	
	private int trainNum=3;
	
	private DataIO dataIO;
	
	public static void main(String[] args) {
		String OutputResultFile="F:\\Recommendation\\coding\\output.txt";
		String pathTrain="F:\\Recommendation\\coding\\train_file.txt";
		String pathTest="F:\\Recommendation\\coding\\predict_file.txt";
		
		
		LogisticRegression lr=new LogisticRegression();
		lr.initData();
		lr.loadData(pathTrain, pathTest);
		lr.logisticRegression();
		lr.predict();
		lr.printResult(OutputResultFile, OutputResult);
	}

	/**
	 * Description :load the train data to  trainList
	 *  and load the test data to testList.
	 */
	public void loadData(String pathTrain, String pathTest){
		dataIO.loadAsList(pathTrain, trainList);
		System.out.println("trainList.size():"+trainList.size());
		dataIO.loadAsList(pathTest, testList);
	}
	
	public void initData(){
		Random r=new Random();
		for(int i=0;i<=FACTOR;i++){
			thetaJ[i]=r.nextDouble()*0.001;
//			thetaJ_Temp[i]=r.nextInt(1)*0.001;
		}
		
		/*Arrays.fill(thetaJ, 0);
		Arrays.fill(thetaJ_Temp, 0);*/
		
		trainList=new ArrayList<List<Double>>();
		testList=new ArrayList<List<Double>>();
		OutputResult=new ArrayList<String>();
		dataIO=new DataIO();
	}
	
	public void logisticRegression(){
		for(int in=0;in<iteratorNum;in++){
			singleDev();
			
			/*for(int k=0;k<FACTOR;k++){
				thetaJ[k]=thetaJ_Temp[k];
			}*/
		}
	}
	
	/**
	 * Description :predict the test data using the model. 
	 * 
	 */
	public void predict(){
		for(int i=0;i<testList.size();i++){
			int res=getHypothesis(i, testList)>0.5?1:0;
			
			StringBuilder sb=new StringBuilder();
			for(int k=0;k<FACTOR;k++){
				sb.append(testList.get(i).get(k)).append("\t");
			}
			sb.append(res);
			OutputResult.add(sb.toString());
		}
	}
	
	public double sigmoidFunc(double x){
		return 1/(1+Math.pow(Math.E, -1*x));
	}
	
	/**
	 * Description : sum[h(xi)-y(xi)xji]
	 * 
	 */
	public void singleDev(){
		// gcalculate thetaJ_Temp[0] individually
		thetaJ_Temp[0]=0;
		for(int i=0;i<trainNum;i++){
			thetaJ_Temp[0]+=(getHypothesis(i,trainList)-trainList.get(i).get(FACTOR))*trainList.get(i).get(0);
		}
		thetaJ_Temp[0]=thetaJ[0]-alpha*(1/trainNum)*thetaJ_Temp[0];
		
		for(int j=1;j<=FACTOR;j++){
			thetaJ_Temp[j]=0;
			for(int i=0;i<trainNum;i++){
				thetaJ_Temp[j]+=(getHypothesis(i,trainList)-trainList.get(i).get(FACTOR))*trainList.get(i).get(j);
			}
			thetaJ_Temp[j]=thetaJ[j]*(1-alpha*lamaba/trainNum)-alpha*(1/trainNum)*thetaJ_Temp[j];
		}
		
		for(int ii=0;ii<=FACTOR;ii++){
			System.out.print(thetaJ_Temp[ii]+"-");
			System.out.print(thetaJ[ii]+"||");
		}
		
		System.arraycopy(thetaJ_Temp, 0, thetaJ, 0, FACTOR+1);
	}
	
	
	/**
	 * Description : get the getHypothesis value about the ith record.
	 * 
	 */
	public double getHypothesis(int i,List<List<Double>> list){
		double ret=thetaJ[0];
		for(int k=1;k<=FACTOR;k++){
			ret+=thetaJ[k]*list.get(i).get(k-1);
		}
		
		System.out.println(ret+":"+sigmoidFunc(ret));
		return sigmoidFunc(ret);
	}
	
	/**
	 * Description :print the result to file 
	 * 
	 */
	public void printResult(String OutputResultFile, List<String> list){
		dataIO.writeCollectionToFile(OutputResultFile, OutputResult);
	}
	
	
}
