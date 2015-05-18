/**
 * 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Description :the implementation of the paper "A new collaborative filtering approach for increasing the aggregate diversity of recommender systems"
 *
 * @author fang
 * 2015年5月5日
 * 
 */
public class NewCollaborativeFiltering {
	
	private static String SPLIT = "\t";
	private static int ratingNum=0;
	
	private static int TopN=10;
	
	// the following two map is ready for train data.
	private Map<String, Map<String, Double>> userItemRate=null;
	private Map<String, Map<String, Double>> itemUserRate=null;
	
	// the following two map is ready for test data.
	private Map<String, Map<String, Double>> userItemRateTest = null;
	private Map<String, Map<String, Double>> itemUserRateTest = null;
	
	//for each item, find it's  k most significant co-occurrences or by using a threshold
	private Map<String, Set<String>> itemItem=new HashMap();
	
	
	//storage the item vector   <item, vector>
	private Map<String, List<Integer>> itemItemList=new HashMap<>();
	
	
	
	public static void main(String[] args) {
		NewCollaborativeFiltering ncf=new NewCollaborativeFiltering();
		
		String pathTrain = "F:\\Recommendation\\coding\\My\\recordWithRate2_itemMaped_train";
		String pathTest = "F:\\Recommendation\\coding\\My\\finalTestData";

		ncf.loadData(pathTrain, pathTest);
		
		ncf.findCooccurringItemPair();
		
		System.out.println("ncf.itemItem.size():"+ncf.itemItem.size());
		ncf.getItemVector();
		
		System.out.println("ncf.itemItemList.size():"+ncf.itemItemList.size());
		
		System.out.println("mae:"+ncf.predict());
	}
	
	
	public double predict(){
		Set<String> users = userItemRateTest.keySet();
		double mae = 0.0;
		int k = 0;
		int diversity=0;
		for (String u : users) {
			TreeMap<Double, String> resSet=new TreeMap<Double, String>(new Comparator<Double>() {
				public int compare(Double d1, Double d2){
					return d2.compareTo(d1);
				}
			});
			for (Entry<String, Double> entry : userItemRateTest.get(u)
					.entrySet()) {
				double res = getPrediction(u, entry.getKey());
				if(res==-1)
					continue;
//				System.out.println("res,,="+res);
//				System.out.println("entry.getValue(),,="+entry.getValue());
				mae += Math.abs(res - entry.getValue());
//				System.out.println("mae,,="+mae);
				k++;
				
				resSet.put(res, entry.getKey());
			}
			int count=0;
			for(Entry<Double, String> entry: resSet.entrySet()){
				if(count++>TopN){
					break;
				}
				if(userItemRateTest.get(u).get(entry.getValue())>=3){
					diversity++;
				}
			}
		}
//		System.out.println("mae,,="+mae);
//		System.out.println("k,,="+k);
		mae = mae / k;
//		System.out.println("mae,="+mae);
		System.out.println("diversity:"+diversity);
		return mae;
	}
	
	
	
	/**
	 * Description :predict the user for the item. 
	 * 
	 */
	public double getPrediction(String user, String item){
		Map<String, Double> itemRate=userItemRate.get(user);
		if(itemRate==null||itemRate.size()==0){
			return -1;
		}
		if(itemRate.containsKey(item)){
			return itemRate.get(item);
		}
		double sum=0.0, sum1=0.0;
		for(Entry<String ,Double> entry: itemRate.entrySet()){
			double sim=getSimilarities(item, entry.getKey());
			if(sim==-1){
				return -1;
			}
			sum+=sim*entry.getValue();
			sum1+=Math.abs(sim);
            System.out.println("sum:"+sum);
            System.out.println("sum1:"+sum1);
		}
		if(sum1==0){
			return -1;
		}
		return sum/sum1;
	}
	
	/**
	 * Description :get the similarity of the two items.
	 * 
	 */
	public double getSimilarities(String itemI, String itemJ){
		List itemIList=itemItemList.get(itemI);
		List itemJList=itemItemList.get(itemJ);
		
		if(itemIList==null||itemJList==null){
			return -1;
		}
		
		double t1=VectorMultiplyVector(itemIList, itemIList);
		double t2=VectorMultiplyVector(itemJList, itemJList);
		
		if(t1==0||t2==0){
			return -1;
		}
		
		double sum=0;
		sum=VectorMultiplyVector(itemIList, itemJList)/(Math.sqrt(VectorMultiplyVector(itemIList, itemIList))*Math.sqrt(VectorMultiplyVector(itemJList, itemJList)));
		return sum;
	}
	
	public double VectorMultiplyVector(List<Integer> list1, List<Integer> list2){
		double sum=0;
		for(int i=0;i<list1.size();i++){
			sum+=list1.get(i)*list2.get(i);
		}
		return sum;
	}
	
	
	/**
	 * Description :get the vector to respesent it.
	 * 
	 */
	public void getItemVector(){
		Set<String> items=itemUserRate.keySet();
		List<String> itemList=new LinkedList<String>(items);
		Collections.sort(itemList);
		
		for(Entry<String, Set<String>> entry: itemItem.entrySet()){
			
			List<Integer> oneItemList=new ArrayList(30);
			
			for(String i:itemList){
				if(entry.getValue().contains(i)){
					oneItemList.add(1);
				}else{
					oneItemList.add(0);
				}
			}
			
			itemItemList.put(entry.getKey(), oneItemList);
		}
	}
	
	     /**
	     * Description : We define two items to be co-occurrences if they are con-tained in at least one user profile in which they are rated
similarly.
		 * 
		 */
	public void findCooccurringItemPair(){
		for(Entry<String, Map<String, Double>> entry: userItemRate.entrySet()){
			Map<String, Double> itemRate=entry.getValue();
			String itemArray[]=itemRate.keySet().toArray(new String[0]);
					//toArray();
			for(int i=0;i<itemArray.length-1;i++){
				
				if(itemItem.containsKey(itemArray[i])){
					continue;
				}
				
				Map<Double, String> itemValue=new TreeMap(new Comparator<Double>() {
					public int compare(Double d1, Double d2){
						return d2.compareTo(d1);
					}
				});
				for(int j=i+1;j<itemArray.length;j++){
					if(Math.abs(itemRate.get(itemArray[i])-itemRate.get(itemArray[j]))<=1){
						double significanceValue=getSignificanceValue(itemArray[i], itemArray[j]);
						itemValue.put(significanceValue, itemArray[j]);
					}
				}
				
				int k=0;
				Set<String> userSet=new HashSet();
				for(Entry<Double, String> entry1: itemValue.entrySet()){
					if(++k<=TopN){
						userSet.add(entry1.getValue());
					}else{
						break;
					}
				}
				itemItem.put(itemArray[i], userSet);
			}
		}
	}
	
	
	/**
	 * Description : get  significance value for the two item
	 * 
	 */
	public double getSignificanceValue(String itemI, String itemJ){
		int k11=0,k12=0,k21=0,k22=0;
		double coor=0.0;
		for(Entry<String, Map<String, Double>> entry: userItemRate.entrySet()){
			Set<String> itemSet=entry.getValue().keySet();
			if(itemSet.contains(itemI)&&itemSet.contains(itemJ)){
				k11++;
			}else if(itemSet.contains(itemI)&&!itemSet.contains(itemJ)){
				k12++;
			}else if(!itemSet.contains(itemI)&&itemSet.contains(itemJ)){
				k21++;
			}else{
				k22++;
			}
		}
		int r1=k11+k12;
		int r2=k21+k22;
		int c1=k11+k21;
		int c2=k12+k22;
		coor=Math.pow((Math.abs(k11*k22-k12*k21)-ratingNum*0.5),2)/(r1*r2*c1*c2);
		return coor;
		
	}
	
	
	/**
	 * Description :load the train data and the test data
	 * 
	 */
	public void loadData(String pathTrain, String pathTest) {
		userItemRate = new HashMap<>();
		itemUserRate = new HashMap<>();
		loadFile(pathTrain, userItemRate, itemUserRate);
		System.out.println(userItemRate.size() + ":" + itemUserRate.size());
		
		userItemRateTest = new HashMap<>();
		itemUserRateTest = new HashMap<>();
		loadFile(pathTest, userItemRateTest, itemUserRateTest);
		System.out.println(userItemRateTest.size() + ":"
				+ itemUserRateTest.size());
	}

	public void loadFile(String path,
			Map<String, Map<String, Double>> userItemRate,
			Map<String, Map<String, Double>> itemUserRate) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					path), "utf-8"));
			String s;
			while ((s = br.readLine()) != null) {
				String str[] = s.split(SPLIT);
				double rate = Double.parseDouble(str[2]);
				// construct the <user, <item, rate>>
				Map<String, Double> itemRate;
				if (userItemRate.containsKey(str[0])) {
					itemRate = userItemRate.get(str[0]);
				} else {
					itemRate = new HashMap<String, Double>();
				}
				itemRate.put(str[1], rate);
				userItemRate.put(str[0], itemRate);

				// construct the <item, <user, rate>>
				Map<String, Double> userRate;
				if (itemUserRate.containsKey(str[1])) {
					userRate = itemUserRate.get(str[1]);
				} else {
					userRate = new HashMap<String, Double>();
				}
				userRate.put(str[0], rate);
				itemUserRate.put(str[1], userRate);
				ratingNum++;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	}

}
