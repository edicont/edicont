package com.edicont.persistor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.edicont.persistor.EdicontPersistor;
import com.edicont.persistor.query.Query;
import com.mongodb.DB;
import com.mongodb.Mongo;

import junit.framework.TestCase;

public class EdicontPersistorTest extends TestCase {
	
	private EdicontPersistor server;

	protected void setUp() throws Exception {
		super.setUp();
		Mongo mongo = new Mongo();
		DB db = mongo.getDB("testdb");
		
		server = new EdicontPersistor();
		server.setDB(db);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGet() {
		try {
			
			
			//delete all stored TestObject-Instances
			Query.create(TestObject.class).remove(server);
			
			//request all instances of class TestObject
			List<Object> list = server.get(TestObject.class);
			assertEquals(0, list.size()); //has to be 0 as we have removed all instances first.
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			//create a new Instance
			TestObject object = new TestObject();
			object.setId("4cae151f67b06f0b5cd7ae87");
			object.setFirstname("Hans");
			object.setLastname("Müller");
			object.setSize(156);
			object.setDob(sdf.parse("1998-10-10"));
			object.setStrings(new String[] {"schubidu", "dadada"});
			List<Integer> numbers = new ArrayList<Integer>();
			numbers.add(1);
			numbers.add(2);
			numbers.add(3);
			object.setNumbers(numbers);
			TestSubObject sub = new TestSubObject();
			sub.setValue1("Wert1");
			sub.setValue2(1111);
			sub.setSingleSub(new TestSubObject());
			object.setTestSub(sub);
			TestSubObject sub1 = new TestSubObject();
			sub1.setValue1("Wert2");
			sub1.setValue2(1);
			TestSubObject sub2 = new TestSubObject();
			sub2.setValue1("Wert3");
			sub2.setValue2(2);
			object.setTestSubArray(new TestSubObject[] {sub1, sub2});
			
			List<TestSubObject> tList = new ArrayList<TestSubObject>();
			tList.add(sub1);
			tList.add(sub2);
			tList.add(sub);
			object.setTestSubList(tList);
			
			//save this object
			server.save(object);
			
			
			//read the id of this object
			String uuid = object.getId();
			
			
			//read all Instances of class TestObject.
			list = server.get(TestObject.class);
			assertEquals(1, list.size()); //should be one now
			
			//fetch the TestObject instance with a given id.
			TestObject t = (TestObject) server.get(TestObject.class, uuid);
			assertNotNull(t);
			
			assertEquals("Hans", t.getFirstname());
			assertEquals("Müller", t.getLastname());
			assertEquals(156, t.getSize());
			assertEquals(sdf.parse("1998-10-10"), t.getDob());
			assertTrue(Arrays.deepEquals(new String[] {"schubidu", "dadada"}, t.getStrings()));
			List<Integer> n = t.getNumbers();
			assertEquals(3, n.size());
			assertEquals(1, (int)n.get(0));
			assertEquals(2, (int)n.get(1));
			assertEquals(3, (int)n.get(2));
			TestSubObject tsub = t.getTestSub();
			assertEquals("Wert1", tsub.getValue1());
			assertEquals(1111, tsub.getValue2());
			assertNotNull(tsub.getSingleSub());
			assertNull(tsub.getSingleSub().getSingleSub());
			assertNull(tsub.getSingleSub().getValue1());
			assertEquals(0, tsub.getSingleSub().getValue2());
			
			
			//create and execute a query on the TestObject
			List<Object> l = server.get(
					Query.create(TestObject.class)
						.add(
							Query.compare("lastname", "Müller", Query.EQ)
						)
						.add(
							Query.compare("firstname", "B", Query.GT)
						)
						.add(
							Query.compare("size", 176, Query.LTE)
						)
						.add(
							Query.compare("testSub.value1", "Wert1", Query.EQ)
						)
					);
			
			assertEquals(1, l.size());
			assertEquals(uuid, ((TestObject)l.get(0)).getId());
			
			
			//remove all master classes
			Query.create(MasterObject.class).remove(server);
			
			//create a master class
			MasterObject master = new MasterObject();
			master.setName("MASTER");
			//set the TestObject. Will be stored as a relation.
			master.setTestObject((TestObject) l.get(0));
			
			//persist it
			server.save(master);
			
			assertNotNull(master.getId());
			
			//fetch the master again
			MasterObject m1 = (MasterObject)server.get(MasterObject.class, master.getId());
			
			assertNotNull(m1);
			assertEquals("MASTER", m1.getName());
			assertNotNull(m1.getTestObject());
			assertEquals("Hans", m1.getTestObject().getFirstname());
			
			System.out.println(m1);
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception thrown.");
			
		}
		
	}

	

}
