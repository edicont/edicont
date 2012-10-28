package com.edicont.persistor.query;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Conjunction implements Restriction {
	
	private List<Restriction> restrictions = new ArrayList<Restriction>();
	
	public Conjunction add(Restriction restriction) {
		this.restrictions.add(restriction);
		return this;
	}
	
	public static Conjunction create() {
		return new Conjunction();
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

}
