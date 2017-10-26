package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.mysql.MySQLConnection;
import entity.Item;

public class Recommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		MySQLConnection conn = MySQLConnection.getInstance();
		
		// find all visited restaurant-ids for input userId and location
		Set<String> favoriteItems = conn.getFavoriteItemIds(userId);
		
		// get all the categories for the visited restaurants
		Set<String> allCategories = new HashSet<String>();
		for (String item : favoriteItems) {
			allCategories.addAll(conn.getCategories(item));
		}
		allCategories.remove("Undefined");
		
		if (allCategories.isEmpty()) {
			allCategories.add("");
		}
		
		// get all the restaurants based on the visited categories
		Set<Item> recommendedItems = new HashSet<Item>();
		for (String category : allCategories) {
			List<Item> items = conn.searchItems(userId, lat, lon, category);
			recommendedItems.addAll(items);
		}
		
		// filter restaurants that this input user has visited
		List<Item> filteredItems = new ArrayList<Item>();
		for (Item item : recommendedItems) {
			if (!favoriteItems.contains(item.getItemId())) {
				filteredItems.add(item);
			}
		}
		
		// TODO:deDup
		
		// rank filteredItems based on distance
		Collections.sort(filteredItems, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				double distance1 = getDistance(item1.getLatitude(), item1.getLongitude(), lat, lon);
				double distance2 = getDistance(item2.getLatitude(), item2.getLongitude(), lat, lon);
				return Double.compare(distance1, distance2);
			}
		});
		
		return filteredItems;
	}

	// Source : http://andrew.hedges.name/experiments/haversine/
	private static double getDistance(double lat1, double lon1, double lat2, double lon2){
		double dlon = lon2 - lon1;
	    double dlat = lat2 - lat1;
	    double a = Math.sin(dlat / 2 / 180 * Math.PI) * Math.sin(dlat / 2 / 180 * Math.PI)
	       + Math.cos(lat1 / 180 * Math.PI) * Math.cos(lat2 / 180 * Math.PI)
	       * Math.sin(dlon / 2 / 180 * Math.PI) * Math.sin(dlon / 2 / 180 * Math.PI);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    // Radius of earth in miles.
	    double R = 3961;
	    return R * c;
	}
}
