import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Description :implement the slopeOne algorithm 
 * 
 * @author fang 2015.05.03
 * 
 */

public class SlopeOne {
	private static String SPLIT = "\t";
	// the following two map is ready for train data.
	private Map<String, Map<String, Double>> userItemRate = null;
	private Map<String, Map<String, Double>> itemUserRate = null;

	private Map<String, Map<String, Double>> userItemRateTest = null;
	private Map<String, Map<String, Double>> itemUserRateTest = null;

	private static double mae = 0.0;

	public static void main(String[] args) {
		String pathTrain = "F:\\Recommendation\\dataSet\\MovieLens\\ml-100k\\ml-100k\\u1.base";
		String pathTest = "F:\\Recommendation\\dataSet\\MovieLens\\ml-100k\\ml-100k\\u1.test";

		SlopeOne so = new SlopeOne();
		so.loadData(pathTrain, pathTest);
		so.slopeOne();
		System.out.println("mae=" + mae);
	}

	/**
	 * Description : for each record of the test Data, compute the prediction
	 * 
	 */
	public void slopeOne() {
		Set<String> users = userItemRateTest.keySet();
		mae = 0.0;
		int k = 0;
		for (String u : users) {
			for (Entry<String, Double> entry : userItemRateTest.get(u)
					.entrySet()) {
				double res = slopeOneCore(u, entry.getKey());
				mae += Math.abs(res - entry.getValue());
				k++;
			}
		}
		mae = mae / k;
	}

	/**
	 * Description :compute a prediction for the user-->item
	 * 
	 */
	public double slopeOneCore(String user, String item) {
		Map<String, Double> itemRate = userItemRate.get(user);
		// the user has any ratings , return!
		if (itemRate == null || itemRate.size() == 0) {
			return -1;
		}
		// if the user has already rated the item
		if (itemRate.containsKey(item)) {
			return itemRate.get(item);
		}

		double sum = 0.0;
		for (Entry<String, Double> entry : itemRate.entrySet()) {
			String itemJ = entry.getKey();
			sum += getDev(item, itemJ);

		}
		sum = sum / itemRate.size() + getAverarge(itemRate);
		return sum;

	}

	/**
	 * Description :get the deviliation between the itemI and itemJ
	 * 
	 */
	public double getDev(String itemI, String itemJ) {
		Set<String> commonUser = getCommonUser(itemI, itemJ);
		if (commonUser == null || commonUser.size() == 0) {
			return 0;
		}
		double sum = 0;
		for (String u : commonUser) {
			sum += userItemRate.get(u).get(itemI)
					- userItemRate.get(u).get(itemJ);
		}
		sum = sum / commonUser.size();
		return sum;
	}

	/**
	 * Description : get the user set who both rated the itemI and itemJ
	 * 
	 */
	public Set<String> getCommonUser(String itemI, String itemJ) {

		Map<String, Double> userRate1 = itemUserRate.get(itemI);
		if (userRate1 == null || userRate1.size() == 0) {
			return null;
		}
		Set<String> userI = userRate1.keySet();

		Map<String, Double> userRate2 = itemUserRate.get(itemJ);
		if (userRate2 == null || userRate2.size() == 0) {
			return null;
		}

		Set<String> userJ = userRate2.keySet();
		userI.retainAll(userJ);

		return userI;
	}

	/**
	 * Description : get the average of the user's rating
	 * 
	 */
	public double getAverarge(Map<String, Double> itemRating) {
		double sum = 0.0;
		int k = 0;
		for (Entry<String, Double> entry : itemRating.entrySet()) {
			sum += entry.getValue();
			k++;
		}
		if (k == 0)
			return 0.0;
		return sum / k;
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
