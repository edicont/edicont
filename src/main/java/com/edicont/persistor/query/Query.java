package com.edicont.persistor.query;

import java.util.ArrayList;
import java.util.List;

import com.edicont.persistor.EdicontPersistor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
/**
 * represents a set of search restrictions to the mongodb
 * 
 * @author bernhard.dangl
 * @version 0.1
 */
public class Query implements Restriction, ComparationConstants {
	
	private Class<?> clazz;
	private BasicDBObject order = new BasicDBObject();
	private List<Restriction> restrictions = new ArrayList<Restriction>();
	private int limit = 0;
	private int skip  = 0;
	
	/**
	 * creates a new Query instance
	 * is a shortcut for new <code>Query(Class<?> clazz)</code>
	 * @param clazz the class we want to look for.
	 * @return a new query instance
	 */
	public static Query create(Class<?> clazz) {
		return new Query(clazz);
	}
	
	/**
	 * creates a new Query instance
	 * @param clazz the class we want to look for instances of.
	 */
	public Query(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	/**
	 * instances of which class should be searched for?
	 * @return the class
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * sets the class restriction of the query.
	 * @param clazz
	 * @return
	 */
	public Query setClazz(Class<?> clazz) {
		this.clazz = clazz;
		return this;
	}
	
	/**
	 * sets the maximum number of returned objects.
	 * @param limit the maximum number of returned objects.
	 * @return
	 */
	public Query limit(int limit) {
		this.limit = limit;
		return this;
	}
	
	/**
	 * how many objects should be skipped before the first 
	 * object in the result.
	 * @param skip number of skipped objects.
	 * @return
	 */
	public Query skip(int skip) {
		this.skip = skip;
		return this;
	}
	
	/**
	 * adds a sort order to the query
	 * @param key the name of the field we want to sort for.
	 * @param asc in ascending order (<code>true</code>)
	 * @return
	 * @see 
	 */
	public Query addOrder(String key, boolean asc) {
		order.append(key, asc ? 1 : -1);
		return this;
	}
	
	/**
	 * add a restriction. all restrictions in the root level of a query
	 * are connected via conjunction.
	 * @param restriction restriction to add.
	 * @return
	 */
	public Query add(Restriction restriction) {
		this.restrictions.add(restriction);
		return this;
	}
	/**
	 * adds a conjunction to the query.
	 * shorthand for add(new Conjunction())
	 * @return returns the conjunction.
	 */
	public static Conjunction and() {
		return Conjunction.create();
	}
	/**
	 * adds a disjunction to the query.
	 * shorthand for add(new Disjunction());
	 * @return the conjunction
	 */
	public static Disjunction or() {
		return Disjunction.create();
	}
	
	/**
	 * creates a Comparation instance.
	 * @return
	 */
	public static Comparation compare() {
		return Comparation.create();
	}
	/**
	 * creates a Comparation instance with attributes
	 * @param field the name of the field
	 * @param value the value to search for
	 * @param comparator the comparator
	 * @return
	 * @see ComparationConstants
	 */
	public static Comparation compare(String field, Object value, String comparator) {
		return Comparation.create(field, value, comparator);
	}

	/**
	 * converts this query to the representation in mongodb related objects
	 * @return
	 */
	public DBObject toDBObject() {
		BasicDBObject result = new BasicDBObject();
		for(int i=0; i<restrictions.size(); i++) {
			DBObject dbObject = restrictions.get(i).toDBObject();
			String key = dbObject.keySet().iterator().next();
			result.append(key, dbObject.get(key));
		}
		return result;
	}
	
	/**
	 * executes the query and returns a DBCursor as a result.
	 * @param server the EdicontPersistor to use to connect to mongodb
	 * @return a DBCursor of the result
	 */
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
	
	/**
	 * uses this query to remove all matching objects
	 * @param server the EdicontPersistor to use.
	 * @return the number of removed objects.
	 */
	public int remove(EdicontPersistor server) {
		DBObject dbQuery = this.toDBObject();
		
		WriteResult result = server.getCollectionOfClass(getClazz()).remove(dbQuery);
		
		return result.getN();
	}
	

}
