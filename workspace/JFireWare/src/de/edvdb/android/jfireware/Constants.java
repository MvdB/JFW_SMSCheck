package de.edvdb.android.jfireware;

import com.google.android.gms.maps.model.LatLng;

public class Constants {
//	public static final String ID_ADRESSE = "de.edvdb.android.adresse";
//	public static final String ID_SCHLAGWORT = "de.edvdb.android.schlagwort";
//	public static final String ID_MESSAGE = "de.edvdb.android.message";

	public static final String START_PATTERN = "Einsatzort:";
	public static final String END_PATTERN = "Schlagwort:";
	
	public static final String DB_TABLE_NAME = "jfwtable";
	public static final String DB_CREATE_TABLE = "create table jfwtable(address varchar(200), date varchar(40), body varchar(200))";
	public static final String DB_SELECT_LATEST = "select * from jfwtable where date = (select max(date) from jfwtable)";
	
	
	public static final String LINE_SEP = System.getProperty("line.separator");
	
	static final LatLng BASE = new LatLng(48.1963681, 11.8070446);
	public static final int MAP_ZOOM = 12;
}
