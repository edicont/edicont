package com.edicont.persistor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.edicont.persistor.model.ClassDescription;
import com.edicont.persistor.model.FieldDescription;
import com.edicont.persistor.query.Query;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Then engine and entry-point to the edicont persistence engine.
 * 
 * @author bernhard.dangl
 * @version 0.1
 */
public class EdicontServer {
	
	private static Log log = LogFactory.getLog(EdicontServer.class);
	
	private DB db;
	
	private Map<String, ClassDescription> classDescriptions = new HashMap<String, ClassDescription>();

	/**
	 * returns the ClassDescription of the given class.
	 * if not available the classDescription is created and cached for later use.
	 * @param clazz
	 * @return
	 */
	public synchronized ClassDescription getClassDescription(Class<?> clazz) {
		log.debug("Calling for ClassDescription for ["+clazz.getName()+"] ...");
		ClassDescription descr = this.classDescriptions.get(clazz.getName());
		if(descr==null) {
			log.debug("ClassDesription not in cache.");
			descr = new ClassDescription(clazz);
			descr.ensureIndexes(db);
			classDescriptions.put(descr.getClassName(), descr);
		} else {
			log.debug("ClassDescription from cache.");
		}
		return descr;
	}
	
	/**
	 * returns the MongoDB Database this EdicontServer is connected to.
	 * @return
	 */
	public DB getDB() {
		return db;
	}

	/**
	 * sets the MongoDB Database this EdicontServer should connect to. 
	 * @param db
	 */
	public void setDB(DB db) {
		this.db = db;
	}
	
	/**
	 * returns the value of the uuid field of the given object (if available, otherwise <code>null</code>)
	 * @param object the object to read from
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public String getUuid(Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		FieldDescription descr = getClassDescription(object.getClass()).getUuidField();
		if(descr==null) {
			return null;
		} else {
			return (String)descr.getFieldValue(object);
		}
	}
	
	/**
	 * returns the mongodb collection this class has to be stored.
	 * @param clazz
	 * @return
	 */
	public DBCollection getCollectionOfClass(Class<?> clazz) {
		return db.getCollection(getClassDescription(clazz).getEntityName());
	}
	
	/**
	 * Returns all stored objects of the given class
	 * @param ofClass
	 * @return
	 * @throws Exception
	 */
	public List<Object> get(Class<?> ofClass) throws Exception {
		
		log.debug("Start get(["+ofClass.getName()+"]) ...");
		
		ClassDescription descr = getClassDescription(ofClass);
		BasicDBObject query = new BasicDBObject();
		query.put("_class", descr.getClassName());
		
		log.debug("mongodb query created: "+query);
		
		DBCursor cursor = db.getCollection(descr.getEntityName()).find(query);
		
		log.debug("mongodb cursor created.");
		
		List<Object> erg = new ArrayList<Object>();
		
		log.debug("start iterating over the cursor ...");
		
		while(cursor.hasNext()) {
			DBObject dbObject = cursor.next();
			erg.add(descr.toObject(this, dbObject));
		}
		
		log.debug("ready iterating over the cursor.");
		
		return erg;
	}
	
	/**
	 * returns the object with the given uuid from the collection specified by the desired class.
	 * @param ofClass class that specifies the collection
	 * @param uuid id of the object
	 * @return
	 * @throws Exception
	 */
	public Object get(Class<?> ofClass, String uuid) throws Exception {
		
		log.debug("start get(["+ofClass.getName()+"], ["+uuid+"]) ...");
		
		ClassDescription descr = getClassDescription(ofClass);
		
		DBObject dbObject = getCollectionOfClass(ofClass).findOne(new BasicDBObject("_id", new ObjectId(uuid)));
		
		log.debug("found "+dbObject);
		
		return descr.toObject(this, dbObject);
		
	}
	
	/**
	 * returns all stored Objects filtered by the query.
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public List<Object> get(Query query) throws Exception {
		
		log.debug("start get(["+query+"]) ...");
		
		ClassDescription descr = getClassDescription(query.getClazz());
		
		DBCursor cursor = query.execute(this);
		
		log.debug("mongodb cursor created.");
		
		List<Object> erg = new ArrayList<Object>();
		
		log.debug("start iterating over cursor ...");
		
		while(cursor.hasNext()) {
			DBObject dbObject = cursor.next();
			erg.add(descr.toObject(this, dbObject));
		}
		
		log.debug("ready iterating over cursor.");
		
		return erg;
		
	}
	
	/**
	 * stores the object to the mongodb
	 * @param object
	 */
	public void save(Object object) {
		
		log.debug("start save(["+object+"]) ...");
		
		ClassDescription descr = getClassDescription(object.getClass());
		
		DBObject dbObject = descr.toMongoObject(this, object);
		
		log.debug("mongodb object created.");
		
		db.getCollection(descr.getEntityName()).save(dbObject);
		
		log.debug("object stored successfully.");
		
		try {
			descr.setUuidOfObject(object, dbObject);
			log.debug("uuid set successfully.");
		} catch (Exception e) {
			log.warn("could not set uuid", e);
			throw new IllegalStateException("Error setting uuid:", e);
		}
	}
	
	/**
	 * remove all objects matching the query from the persistence store.
	 * @param query
	 * @return
	 */
	public int remove(Query query) {
		
		log.debug("start remove(["+query+"]");
		
		return query.remove(this);
		
		
	}
	
	
}
