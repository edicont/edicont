package com.edicont.persistor.query;

import com.mongodb.DBObject;

public abstract interface Restriction {
	
	abstract DBObject toDBObject();

}
