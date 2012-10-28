package com.edicont.persistor.query;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Disjunction implements Restriction{

	private List<Restriction> restrictions = new ArrayList<Restriction>();
	
	public Disjunction add(Restriction restriction) {
		
		this.restrictions.add(restriction);
		
		return this;
	}
	
	public static Disjunction create() {
		return new Disjunction();
	}

	public DBObject toDBObject() {
		DBObject[] dbObjects = new DBObject[restrictions.size()];
		for(int i=0; i<restrictions.size(); i++) {
			dbObjects[i] = restrictions.get(i).toDBObject();
		}
		return new BasicDBObject("$or", dbObjects);
	}
	
}
