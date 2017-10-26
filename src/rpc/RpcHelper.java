package rpc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	// Parses a JSONObject from http request
	public static JSONObject readJsonObject(HttpServletRequest request){
		StringBuilder sb = new StringBuilder();
		String line = null;
		
		try{
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return new JSONObject(sb.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	// Writes a JSONObject to http response
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj){
		try {
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.println(obj);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Writes a JSONArray to http response
	public static void writeJsonArray(HttpServletResponse response, JSONArray array){
		try {
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.println(array);
			out.flush();
			out.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
