package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;
import external.YelpAPI;

// singleton pattern
public class MySQLConnection {
	private static MySQLConnection instance;
	
	public static MySQLConnection getInstance(){
		if (instance == null) {
			instance = new MySQLConnection();
		}
		return instance;
	}
	
	// java.sql.Connection
	private Connection conn = null;
	private MySQLConnection(){
		try {
			// forcing the class representing the MySQL driver to load and initialize
			// the newInstance() call is a work around for some broken Java implementations
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
	
	public void setFavoriteItems(String userId, List<String> itemIds) {
		String query = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for (String itemId : itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		String query = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for (String itemId : itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> itemIds = new HashSet<String>();
		String query = "SELECT item_id FROM history WHERE user_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				itemIds.add(rs.getString("item_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return itemIds;
	}
	
	public Set<Item> getFavoriteItems(String userId){
		Set<Item> items = new HashSet<Item>();
		String query = "SELECT * FROM items, history WHERE items.item_id = history.item_id AND history.user_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, userId);			
			ResultSet rs = statement.executeQuery();
			ItemBuilder builder = new ItemBuilder();
			while (rs.next()) {
				builder.setItemId(rs.getString("item_id"));
				builder.setName(rs.getString("name"));
				builder.setCity(rs.getString("city"));
				builder.setState(rs.getString("state"));
				builder.setCountry(rs.getString("country"));
				builder.setZipcode(rs.getString("zipcode"));
				builder.setRating(rs.getDouble("rating"));
				builder.setAddress(rs.getString("address"));
				builder.setLatitude(rs.getDouble("latitude"));
				builder.setLongitude(rs.getDouble("longitude"));
				builder.setDescription(rs.getString("description"));
				builder.setSnippet(rs.getString("snippet"));
				builder.setSnippetUrl(rs.getString("snippet_url"));
				builder.setImageUrl(rs.getString("image_url"));
				builder.setUrl(rs.getString("url"));
				Set<String> categories = getCategories(rs.getString("item_id"));
				builder.setCategories(categories);
				items.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return items;
	}
	
	public Set<String> getCategories(String itemId){
		Set<String> categories = new HashSet<String>();
		String query = "SELECT category FROM categories WHERE item_id = ?";
		
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		return categories;
	}
	
	public List<Item> searchItems(String userId, double lat, double lon, String term) {
		//connect to external API
		YelpAPI api = new YelpAPI();
		List<Item> items = api.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		
		return items;
	}
	
	public void saveItem(Item item){
		try {
			// First, insert into items table
			String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getCity());
			statement.setString(4, item.getState());
			statement.setString(5, item.getCountry());
			statement.setString(6, item.getZipcode());
			statement.setDouble(7, item.getRating());
			statement.setString(8, item.getAddress());
			statement.setDouble(9, item.getLatitude());
			statement.setDouble(10, item.getLongitude());
			statement.setString(11, item.getDescription());
			statement.setString(12, item.getSnippet());
			statement.setString(13, item.getSnippetUrl());
			statement.setString(14, item.getImageUrl());
			statement.setString(15, item.getUrl());
			statement.execute();

			// Second, update categories table for each category.
			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			for (String category : item.getCategories()) {
				statement = conn.prepareStatement(sql);
				statement.setString(1, item.getItemId());
				statement.setString(2, category);
				statement.execute();
			}
		}  catch (SQLException e) {
			e.printStackTrace();
		}	
	}
}
