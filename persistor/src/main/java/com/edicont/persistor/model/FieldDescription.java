package com.edicont.persistor.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.edicont.persistor.EdicontPersistor;
import com.edicont.persistor.annotations.Indexed;
import com.edicont.persistor.annotations.Reference;
import com.edicont.persistor.annotations.Transient;
import com.edicont.persistor.annotations.Uuid;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

/**
 * Generates and stores the meta information of a field.
 * 
 * @author bernhard.dangl
 * @version 0.1
 */
public class FieldDescription {
	
	private static Log log = LogFactory.getLog(FieldDescription.class);

	/**
	 * name of the field
	 */
	private String name;
	
	/**
	 * type of the field
	 */
	private Class<?> type;
	
	/**
	 * should mongodb have an index on this field?
	 */
	private boolean indexed = false;
	
	/**
	 * name of the field in the mongodb
	 */
	private String mongoFieldName;
	
	/**
	 * is this the uuid field?
	 */
	private boolean uuid = false;
	
	/**
	 * should this field be stored at all? If <code>true</code> it won't be stored.
	 */
	private boolean isTransient = false;
	
	/**
	 * should the value be stored as a reference?
	 */
	private boolean byReference = false;
	

	/**
	 * creates a new instance and analyzes the given field.
	 * @param field the field to analyze
	 */
	public FieldDescription(Field field) {
		
		log.debug("create description for field ["+field.getName()+"]");
		name = field.getName();
		type = field.getType();
		
		Indexed ind = (Indexed)field.getAnnotation(Indexed.class);
		if(ind!=null) {
			indexed = true;
			log.debug("- is indexed.");
		}
		
		com.edicont.persistor.annotations.Field f = (com.edicont.persistor.annotations.Field)field.getAnnotation(com.edicont.persistor.annotations.Field.class);
		if(f!=null) {
			mongoFieldName = f.value();
		} else {
			mongoFieldName = name;
		}
		log.debug("- mongoFieldName: "+mongoFieldName);
		
		Uuid uuid = (Uuid)field.getAnnotation(Uuid.class);
		if(uuid!=null) {
			this.uuid = true;
			log.debug("- is uuid.");
		}
		
		Transient trans = (Transient) field.getAnnotation(Transient.class);
		if(trans!=null) {
			this.isTransient = true;
			log.debug("- is transient");
		} else {
			this.isTransient = false;
		}
		
		Reference ref = (Reference) field.getAnnotation(Reference.class);
		this.byReference = (ref!=null);
		
		log.debug("- byReference: "+byReference);
		
		log.debug("done creating field description");
	}

	/**
	 * returns the name of the field.
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * returns the type of the field.
	 * @return
	 */
	public Class<?> getType() {
		return type;
	}
	/**
	 * returns <code>true</code> if the field should be indexed in mongodb.
	 * @return
	 */
	public boolean isIndexed() {
		return indexed;
	}
	/**
	 * returns the name of the corresponding mongodb field.
	 * @return
	 */
	public String getMongoFieldName() {
		return mongoFieldName;
	}
	
	/**
	 * returns <code>true</code> if this field is the uuid field.
	 * @return
	 */
	public boolean isUuid() {
		return uuid;
	}
	/**
	 * returns <code>true</code> if this field shouldn't be stored to mongodb.
	 * @return
	 */
	public boolean isTransient() {
		return isTransient;
	}
	
	
	
	public boolean isByReference() {
		return byReference;
	}

	/**
	 * returns the value of this field in the given object.
	 * @param object the object to read from
	 * @return the value of the object
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public Object getFieldValue(Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return PropertyUtils.getProperty(object, getName());
	}
	
	/**
	 * returns the value of field of the given object in a form
	 * to be storable to mongodb 
	 * @param server the EdicontServer instance
	 * @param object the object the field should be read from
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public Object propertyValueToDBValue(EdicontPersistor server, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		log.debug("start converting of property value to mongodb value ...");
		
		Object value = PropertyUtils.getProperty(object, this.getName());
		
		log.debug("value to convert: "+value);
		
		if(!FieldUtils.isMongoSupported(value)) {
			
			log.debug("not a native mongo type. Create a new DBObject for it.");
			
			if(this.isByReference()) {
				
				log.debug("store by reference.");
				
				server.save(value);
				
				String uuid = server.getUuid(value);
				if(uuid==null) {
					throw new IllegalStateException("No reference possible to a class of type ["+object.getClass().getName()+"]. No uuid specified.");
				}
				
				return new DBRef(server.getDB(), server.getCollectionOfClass(value.getClass()).getName(), new ObjectId(uuid));
				
			} else {
				ClassDescription classDesc = server.getClassDescription(value.getClass());
				DBObject dbObject = classDesc.toMongoObject(server, value);
				
				log.debug("converted to: "+dbObject);
				
				return dbObject;
			}
			
			
		} else {
			
			log.debug("is a native mongo type. can be returned as is is.");
			
			return value;
			
		}
	}
	
	/**
	 * sets the field value of the given object by reading and converting
	 * the value from the given DBObject.
	 * @param object target to set the property
	 * @param dbObject source to read the property
	 * @throws Exception 
	 * @throws ClassNotFoundException 
	 */
	public void setObjectValueFromDBValue(EdicontPersistor server, Object object, DBObject dbObject) throws ClassNotFoundException, Exception {
		
		log.debug("start converting dbObject to target object ...");
		
		log.debug("field: "+this.getName());
		
		Object value = dbObject.get(this.getMongoFieldName());
		
		log.debug("value to set: "+value);
		
		if(value==null) {
			log.debug("is null. set it");
			PropertyUtils.setProperty(object, this.getName(), value);
		} else {
			
			log.debug("value is of class: "+value.getClass());
			
			if(this.type.isArray() || value instanceof BasicDBList) {
				log.debug("it's a list or an array.");
				BasicDBList dbList = (BasicDBList)value;
				PropertyUtils.setProperty(object, this.getName(), FieldUtils.createListFromDBList(server, type, dbList));
			} else {
				if(value instanceof DBObject) {
					
					log.debug("it's a dbObject. convert it.");
									
					ClassDescription classDesc = server.getClassDescription(this.type);
					value = classDesc.toObject(server, (DBObject)value);
					
					log.debug("converted to: "+value);
					
				} else if(value instanceof DBRef) {
					
					log.debug("it's a DBRef so get the reference");
					
					value = ((DBRef)value).fetch();
					
					log.debug("Referenced object loaded. "+value);
					
					ClassDescription classDesc = server.getClassDescription(this.type);
					value = classDesc.toObject(server, (DBObject) value);
					
					log.debug("converted to: "+value);
				}
				PropertyUtils.setProperty(object, this.getName(), value);
			}
		}
		
	}

	@Override
	public String toString() {
		return "FieldDescription [name=" + name + ", type=" + type
				+ ", indexed=" + indexed + ", mongoFieldName=" + mongoFieldName
				+ ", uuid=" + uuid + ", isTransient=" + isTransient
				+ ", byReference=" + byReference + "]";
	}
	
	
}
