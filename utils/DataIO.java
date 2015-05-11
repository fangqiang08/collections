/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Description :
 *
 * @author fang
 * 2015年5月11日
 * 
 */
public class DataIO {
	
	private static String SPLIT = "\t";
	
	public void writeCollectionToFile(String filePath, Collection<String> collection){
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),"utf-8"));
			for(String s: collection){
				bw.write(s);
				bw.newLine();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Description : load a line in a file as a list<Double>
	 * 
	 * 2015年5月11日
	 */
	public void loadAsList(String path, List<List<Double>> list){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path),"utf-8"));
			String s;
			while((s=br.readLine())!=null){
				String str[]=s.split(SPLIT);
				List<Double> tempList=new LinkedList<>();
				for(String e: str){
					tempList.add(Double.parseDouble(e));
				}
				list.add(tempList);
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	/**
	 * Description : load the data as <user, <item, Double>> and  <item, <user, Double>>
	 * 
	 */
	public void loadFileUserItemRate_ItemUserRate(String path,
			Map<String, Map<String, Double>> userItemRate,
			Map<String, Map<String, Double>> itemUserRate) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
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
