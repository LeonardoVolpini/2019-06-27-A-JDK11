package it.polito.tdp.crimes.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.crimes.model.Adiacenza;
import it.polito.tdp.crimes.model.Event;


public class EventsDao {
	
	public List<Event> listAllEvents(){
		String sql = "SELECT * FROM events" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Event> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(new Event(res.getLong("incident_id"),
							res.getInt("offense_code"),
							res.getInt("offense_code_extension"), 
							res.getString("offense_type_id"), 
							res.getString("offense_category_id"),
							res.getTimestamp("reported_date").toLocalDateTime(),
							res.getString("incident_address"),
							res.getDouble("geo_lon"),
							res.getDouble("geo_lat"),
							res.getInt("district_id"),
							res.getInt("precinct_id"), 
							res.getString("neighborhood_id"),
							res.getInt("is_crime"),
							res.getInt("is_traffic")));
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<Integer> getAllYears(){
		String sql="SELECT DISTINCT YEAR(e.reported_date) AS anno "
				+ "FROM `events` e "
				+ "ORDER BY YEAR(e.reported_date)";
		List<Integer> list = new ArrayList<>() ;
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			ResultSet res = st.executeQuery() ;
			while(res.next()) {
				list.add(res.getInt("anno"));
			}
			conn.close();
			return list ;
		}catch (SQLException e) {
			throw new RuntimeException("Errore DB",e);
		}
	}

	public List<String> getAllCategoriesTypes(){
		String sql="SELECT DISTINCT e.offense_category_id AS c "
				+ "FROM `events` e "
				+ "ORDER BY e.offense_category_id";
		List<String> list = new ArrayList<>() ;
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			ResultSet res = st.executeQuery() ;
			while(res.next()) {
				list.add(res.getString("c"));
			}
			conn.close();
			return list ;
		}catch (SQLException e) {
			throw new RuntimeException("Errore DB",e);
		}
	}

	public List<String> getVertici(String categoria, int anno) {
		String sql="SELECT DISTINCT e.offense_type_id AS t "
				+ "FROM `events` e "
				+ "WHERE e.offense_category_id=? AND YEAR(e.reported_date)=?";
		List<String> list = new ArrayList<>() ;
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setString(1, categoria);
			st.setInt(2, anno);
			ResultSet res = st.executeQuery() ;
			while(res.next()) {
				list.add(res.getString("t"));
			}
			conn.close();
			return list ;
		}catch (SQLException e) {
			throw new RuntimeException("Errore DB",e);
		}
	}
	
	public List<Adiacenza> getAdiacenze(String categoria, int anno){
		String sql="SELECT e1.offense_type_id AS t1, e2.offense_type_id AS t2, COUNT(DISTINCT e1.district_id) AS peso "
				+ "FROM `events` e1, `events` e2 "
				+ "WHERE e1.offense_type_id > e2.offense_type_id "
				+ "		AND e1.offense_category_id=? AND e2.offense_category_id = e1.offense_category_id "
				+ "		AND YEAR(e1.reported_date)=? AND YEAR(e2.reported_date) = YEAR(e1.reported_date) "
				+ "GROUP BY e1.offense_type_id, e2.offense_type_id";
		List<Adiacenza> list = new ArrayList<>() ;
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setString(1, categoria);
			st.setInt(2, anno);
			ResultSet res = st.executeQuery() ;
			while(res.next()) {
				Adiacenza a = new Adiacenza(res.getString("t1"),res.getString("t2"),res.getInt("peso"));
				list.add(a);
			}
			conn.close();
			return list ;
		}catch (SQLException e) {
			throw new RuntimeException("Errore DB",e);
		}
	}
}
