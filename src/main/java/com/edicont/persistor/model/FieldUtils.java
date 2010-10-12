package com.edicont.persistor.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.edicont.persistor.EdicontServer;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

/**
 * Provides some useful methods to handle mongodb field specific tasks
 * 
 * @author bernhard.dangl
 * @version 0.1
 */
public class FieldUtils {
	
	private static Log log = LogFactory.getLog(FieldUtils.class);
	
	/**
	 * Array of classes supported by mongo driver out of the box
	 */
	public static final Class<?>[] MONGO_SUPPORTED_CLASSES = {
		String.class,
		Integer.class,
		int.class,
		Boolean.class,
		boolean.class,
		Float.class,
		float.class,
		Double.class,
		double.class,
		Short.class,
		short.class,
		Date.class,
		Pattern.class,
		byte[].class,
		String[].class,
		Integer[].class,
		int[].class,
		Boolean[].class,
		boolean[].class,
		Float[].class,
		float[].class,
		Double[].class,
		double[].class,
		Short[].class,
		short[].class,
		Date[].class,
		Pattern[].class
	};

	/**
	 * is this class supported by the mongo driver out of the box?
	 * @param type the class to check
	 * @return <code>true</code> if supported by mongo driver out of the box.
	 */
	public static boolean isMongoSupported(Object object) {
		if(object==null) return true;
		
		Class<?> type = object.getClass();
		log.debug("isMongoSupported(): type="+type+"; isList="+(object instanceof List));
		
		if((object instanceof List)) {
			List<?> list = (List<?>)object;
			for(int i=0; i<list.size(); i++) {
				if(!isMongoSupported(list.get(i))) {
					return false;
				}
			}
			return true;
		} else {
			for(int i=0; i<MONGO_SUPPORTED_CLASSES.length; i++) {
				if(MONGO_SUPPORTED_CLASSES[i].equals(type)) {
					return true;
				}
			}
			return false;
		}
		
	}
	/**
	 * returns an array of type <code>componentType</code> represented by <code>dbList</code>
	 * @param componentType
	 * @param dbList
	 * @return
	 * @throws Exception 
	 * @throws ClassNotFoundException 
	 * @throws IllegalArgumentException 
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	@SuppressWarnings("unchecked")
	public static Object createListFromDBList(EdicontServer server, Class<?> type, BasicDBList dbList) throws ArrayIndexOutOfBoundsException, IllegalArgumentException, ClassNotFoundException, Exception {
		
		log.debug("start reating List from mongo List ...");
		log.debug("dbList: "+dbList);
		
		if(dbList==null) return null;
		
		if(type.isArray()) {
			
			log.debug("is an array.");
			Object erg = Array.newInstance(type.getComponentType(), dbList.size());
			
			log.debug("iterate over the items ...");
			for(int i=0; i<dbList.size(); i++) {
				Object value = dbList.get(i);
				log.debug("("+(i+1)+"/"+dbList.size()+"): value: "+value);
				if(isMongoSupported(value)) {
					log.debug("is a native mongo type. store it as it is.");
					Array.set(erg, i, value);
				} else {
					log.debug("is not a native mongo type. convert it to target object");
					Array.set(erg, i, server.getClassDescription(type.getComponentType()).toObject(server, (DBObject) value));
				}
			}
			log.debug("done iterate over the items.");
			
			return erg;
			
		} else {
			
			log.debug("is a List");
			
			@SuppressWarnings("rawtypes")
			List erg = (List) (type.isInterface() ? new ArrayList() : type.newInstance());
			
			log.debug("iterate over the items ...");
			for(int i=0; i<dbList.size(); i++) {
				Object value = dbList.get(i);
				log.debug("("+(i+1)+"/"+dbList.size()+"): value: "+value);
				if(isMongoSupported(value)) {
					log.debug("is a native mongo type. store it as it is.");
					erg.add(value);
				} else {
					DBObject dbObject = (DBObject) value;
					Class<?> subClass = Class.forName((String)dbObject.get("_class"));
					log.debug("is not a native mongo type. convert it to target object of class ["+subClass.getName()+"]");
					erg.add(server.getClassDescription(subClass).toObject(server, (DBObject) value));
				}
			}
			log.debug("done iterate over the items.");
			
			return erg;
			
		}
		
	}
	
	
	
}
