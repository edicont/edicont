package com.edicont.persistor.model;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.edicont.persistor.EdicontServer;
import com.edicont.persistor.annotations.Entity;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * This class stores meta-information about a given class.
 * 
 * @author bernhard.dangl
 * @version 0.1
 */
public class ClassDescription {
	
	private Log log = LogFactory.getLog(ClassDescription.class);
	
	/**
	 * the class name
	 */
	private String className;
	
	/**
	 * the name of the collection this class should be stored to.
	 */
	private String entityName;
	
	/**
	 * the class that is described by this object
	 */
	private Class<?> type;
	
	/**
	 * The description of the fields of the class.
	 */
	private Map<String, FieldDescription> fieldDescriptions = null;
	
	/**
	 * holds the names of the reference fields.
	 */
	private List<String> referenceFields = new ArrayList<String>();
	
	/**
	 * returns a list of all reference fields.
	 * @return
	 */
	public List<String> getReferenceFields() {
		List<String> erg = new ArrayList<String>();
		erg.addAll(this.referenceFields);
		return erg;
	}
	
	/**
	 * Creates the description of the given class as a new instance.
	 * @param clazz the class to be described
	 */
	public ClassDescription(Class<?> clazz) {
		
		log.debug("Create new ClassDescription for ["+clazz.getName()+"] ...");
		
		this.className = clazz.getName();
		this.type = clazz;
		
		Entity entity = (Entity)clazz.getAnnotation(Entity.class);
		if(entity!=null) {
			entityName = entity.value();
			log.debug("Entity annotation found: "+entityName);
		} else {
			entityName = className.substring(className.lastIndexOf('.')+1);
		}
		
		fieldDescriptions = computeFieldDescriptions(clazz);
		
		for(Iterator<FieldDescription> it = fieldDescriptions.values().iterator(); it.hasNext();) {
			FieldDescription descr = it.next();
			if(descr.isByReference()) {
				referenceFields.add(descr.getName());
			}
		}
		
		log.debug("Ready creating ClassDescription.");
		
	}
	
	/**
	 * Computes the FieldDescriptions for the given Class
	 * 
	 * @param clazz the Class to analyze
	 * @return a map of descriptions
	 */
	protected Map<String, FieldDescription> computeFieldDescriptions(Class<?> clazz) {
		
		log.debug("compute FieldDescriptions for class ["+clazz.getName()+"] ...");
		
		Class<?> superClass = clazz.getSuperclass();
		
		Map<String, FieldDescription> erg = null;
		
		if(superClass==null) {
			erg = new HashMap<String, FieldDescription>();
		} else {
			erg = computeFieldDescriptions(superClass);
		}
		
		Field[] fields = clazz.getDeclaredFields();
		
		log.debug("iterate over the declared fields of class ["+clazz.getName()+"] ...");
		
		for(int i=0; i<fields.length; i++) {
			Field field = fields[i];
			
			FieldDescription descr = new FieldDescription(field);
			
			erg.put(field.getName(), descr);
			
			if(descr.isUuid()) {
				erg.put("$$uuid", descr);
			}
		}
		
		log.debug("iterate over the declared fields of class ["+clazz.getName()+"] - done.");
		
		return erg;
	}

	/**
	 * returns the name of the class.
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * returns the name of the collection this class is stored to.
	 * @return
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * returns the description of the Uuid-Field (if there is one)
	 * @return
	 */
	public FieldDescription getUuidField() {
		return this.fieldDescriptions.get("$$uuid");
	}
	
	/**
	 * creates the DBObject representing the values of the given object in mongodb
	 * @param server the EdicontServer
	 * @param object the object to transform
	 * @return
	 */
	public DBObject toMongoObject(EdicontServer server, Object object) {
		
		log.debug("start converting ["+object+"] to DBObject ...");
		
		if(type.isArray()) {
			
			log.debug("it is an array");
			
			BasicDBList erg = new BasicDBList();
			
			int arrayLength = Array.getLength(object);
			
			log.debug("start iterating over the array ...");
			for(int i=0; i<arrayLength; i++) {
				log.debug("convert ("+(i+1)+"/"+arrayLength+") ...");
				erg.add(server.getClassDescription(type.getComponentType()).toMongoObject(server, Array.get(object, i)));
			}
			log.debug("done iterating over the array.");
			
			return erg;
			
		} else if(object instanceof List<?>) {
			
			log.debug("it is a List");
			
			BasicDBList erg = new BasicDBList();
			
			List<?> list = (List<?>)object;
			
			log.debug("start iterating over the List ...");
			for(int i=0; i<list.size(); i++) {
				log.debug("convert ("+(i+1)+"/"+list.size()+") ...");
				erg.add(server.getClassDescription(list.get(i).getClass()).toMongoObject(server, list.get(i)));
			}
			log.debug("done iterating over the List.");
			
			return erg;
			
		} else {
			
			log.debug("no array, no List");
			
			DBObject erg = new BasicDBObject();
			
			erg.put("_class", this.getClassName());
			
			log.debug("start iterating over fielddescriptions ...");
			for(Iterator<FieldDescription> iterator = fieldDescriptions.values().iterator(); iterator.hasNext();) {
				FieldDescription descr = iterator.next();
				
				log.debug("field: ["+descr.getName()+"]");
				
				if(!descr.isTransient()) {
					
					Object value = null;
					try {
						value = descr.propertyValueToDBValue(server, object);
					} catch (Exception e) {
						log.warn("Exception converting property value to mongodb value.", e);
					} 
					
					if(descr.isUuid() && value!=null) {
						erg.put("_id", new ObjectId((String)value));
					} else {
						erg.put(descr.getMongoFieldName(), value);
					}	
					
					log.debug("property added.");
					
					
				} else {
					log.debug("is transient: skip.");
				}
				
			}
			
			return erg;
			
		}
		
		
	}

	/**
	 * converts the given mongodb DBObject to the original object
	 * @param server the EdicontServer
	 * @param dbObject the DBObject to take the values from
	 * @return the converted object.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	public Object toObject(EdicontServer server, DBObject dbObject) throws InstantiationException, IllegalAccessException, ClassNotFoundException, Exception {
		
		if(dbObject==null) return null;
		
		log.debug("start to convert ["+dbObject+"] ...");
		
		if(type.isArray()) {
			
			log.debug("is an array.");
			
			BasicDBList dbList = (BasicDBList)dbObject;
			Object object = Array.newInstance(type.getComponentType(), dbList.size());
			
			log.debug("iterate over the array ...");
			for(int i=0; i<dbList.size(); i++) {
				log.debug("setting ("+(i+1)+"/"+dbList.size()+") ...");
				Array.set(object, i, server.getClassDescription(type.getComponentType()).toObject(server, (DBObject) dbList.get(i)));
			}
			log.debug("done iterate over the array.");
			
			return object;
			
		} else {
			Object object = (this.className.equals("java.util.List") ? Class.forName("java.util.ArrayList"): Class.forName(this.className)).newInstance();
			
			log.debug("iterate over field descriptions ...");
			for(Iterator<FieldDescription> iter = fieldDescriptions.values().iterator(); iter.hasNext();) {
				FieldDescription descr = iter.next();
				
				log.debug("field: ["+descr.getName()+"] ...");
				if(!descr.isTransient()) {
					descr.setObjectValueFromDBValue(server, object, dbObject);
					log.debug("value set.");
				} else {
					log.debug("is transient: skip.");
				}
			}
			log.debug("done iterate over field descriptions.");
			
			this.setUuidOfObject(object, dbObject);
			
			return object;	
		}
		
	}

	/**
	 * sets the uuid-property of the given object
	 * @param object the object to set the value on
	 * @param toTakeFrom the DBObject to take the id from.
	 * @return <code>true</code> if the uuid was set successfully.
	 * @throws Exception
	 */
	public boolean setUuidOfObject(Object object, DBObject toTakeFrom) throws Exception {
		
		log.debug("start setting uuid ...");
		
		FieldDescription uuid = this.getUuidField();
		
		log.debug("uuid field: "+ (uuid!=null ? uuid.getName() : null));
		
		if(uuid==null) return false;
		
		String id = toTakeFrom.get("_id").toString();
		
		PropertyUtils.setProperty(object, uuid.getName(), id);
		
		log.debug("uuid set to: ["+id+"]");
		
		return true;
	}

	/**
	 * takes care to have the required fields indexed in mongodb
	 * @param db
	 */
	public void ensureIndexes(DB db) {
		
		log.debug("start ensureIndexes ...");
		
		DBCollection coll = db.getCollection(this.getEntityName());
		
		log.debug("iterate over the field descriptions ...");
		for(Iterator<FieldDescription> iter = fieldDescriptions.values().iterator(); iter.hasNext();) {
			FieldDescription descr = iter.next();
			
			log.debug("field: "+descr.getName());
			
			if(!descr.isUuid() && descr.isIndexed()) {
				log.debug("should be indexed.");
				coll.ensureIndex(new BasicDBObject(descr.getMongoFieldName(), 1));
				log.debug("index set.");
			} else {
				log.debug("not indexed.");
			}
		}
		log.debug("done iterate over the field descriptions.");
	}
	
	@Override
	public String toString() {
		return "ClassDescription [className=" + className + ", entityName="
				+ entityName + ", fieldDescriptions=" + fieldDescriptions
				+ "]";
	}

	

}
