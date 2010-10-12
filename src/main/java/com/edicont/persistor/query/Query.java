package com.edicont.persistor.query;

import java.util.ArrayList;
import java.util.List;


import com.edicont.persistor.EdicontPersistor;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class Query implements Restriction, ComparationConstants {
	
	private Class<?> clazz;
	private BasicDBObject order = new BasicDBObject();
	private List<Restriction> restrictions = new ArrayList<Restriction>();
	private int limit = 0;
	private int skip  = 0;
	
	public Class<?> getClazz() {
		return clazz;
	}

	public Query setClazz(Class<?> clazz) {
		this.clazz = clazz;
		return this;
	}
	
	public Query limit(int limit) {
		this.limit = limit;
		return this;
	}
	
	public Query skip(int skip) {
		this.skip = skip;
		return this;
	}
	
	public Query addOrder(String key, boolean asc) {
		order.append(key, asc ? 1 : -1);
		return this;
	}
	
	public Query add(Restriction restriction) {
		this.restrictions.add(restriction);
		return this;
	}
	
	public static Query create() {
		return new Query();
	}
	
	public static Conjunction and() {
		return Conjunction.create();
	}
	public static Disjunction or() {
		return Disjunction.create();
	}
	public static Comparation compare() {
		return Comparation.create();
	}
	public static Comparation compare(String field, Object value, String comparator) {
		return Comparation.create(field, value, comparator);
	}

	public DBObject toDBObject() {
		BasicDBObject result = new BasicDBObject();
		for(int i=0; i<restrictions.size(); i++) {
			DBObject dbObject = restrictions.get(i).toDBObject();
			String key = dbObject.keySet().iterator().next();
			result.append(key, dbObject.get(key));
		}
		return result;
	}
	
	public DBCursor execute(EdicontPersistor server) {
		DBObject dbQuery = this.toDBObject();
		
		DBCursor cursor = server.getCollectionOfClass(getClazz()).find(dbQuery);
		if(!this.order.isEmpty()) {
			cursor = cursor.sort(this.order);
		}
		if(this.skip>0) {
			cursor = cursor.skip(skip);
		}
		if(this.limit>0) {
			cursor = cursor.limit(limit);
		}
		
		return cursor;
	}
	
	public int remove(EdicontPersistor server) {
		DBObject dbQuery = this.toDBObject();
		
		WriteResult result = server.getCollectionOfClass(getClazz()).remove(dbQuery);
		
		return result.getN();
	}
	

}
