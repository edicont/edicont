package com.edicont.persistor.query;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Comparation implements Restriction,ComparationConstants {
	
	
	private String field = null;
	private Object value = null;
	private String comparator = EQ;
	
	public String getField() {
		return field;
	}
	public Comparation setField(String field) {
		this.field = field;
		return this;
	}

	public Object getValue() {
		return value;
	}
	public Comparation setValue(Object value) {
		this.value = value;
		return this;
	}
	public String getComparator() {
		return comparator;
	}
	public Comparation setComparator(String comparator) {
		this.comparator = comparator;
		return this;
	}

	public static Comparation create() {
		return new Comparation();
	}
	public static Comparation create(String field, Object value, String comparator) {
		return create().setField(field).setValue(value).setComparator(comparator);
	}


	public DBObject toDBObject() {
		BasicDBObject restriction = new BasicDBObject();
		if(EQ.equals(comparator)) {
			restriction.put(field, value);
		} else {
			restriction.put(field, new BasicDBObject(comparator, value));
		}
		return restriction;
	}

}
