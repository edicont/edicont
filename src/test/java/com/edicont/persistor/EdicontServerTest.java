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

public class EdicontServerTest extends TestCase {
	
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
			
			
			
			Query.create().setClazz(TestObject.class).remove(server);
			
			List<Object> list = server.get(TestObject.class);
			assertEquals(0, list.size());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			TestObject object = new TestObject();
			object.setId("4cae151f67b06f0b5cd7ae87");
			object.setFirstname("Jakob");
			object.setLastname("Dangl");
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
			
			server.save(object);
			
			String uuid = object.getId();
			
			list = server.get(TestObject.class);
			assertEquals(1, list.size());
			
			
			TestObject t = (TestObject) server.get(TestObject.class, uuid);
			assertNotNull(t);
			
			assertEquals("Jakob", t.getFirstname());
			assertEquals("Dangl", t.getLastname());
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
			
			
			List<Object> l = server.get(
					Query.create()
						.setClazz(TestObject.class)
						.add(
							Query.compare("lastname", "Dangl", Query.EQ)
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
			
			
			
			Query.create().setClazz(MasterObject.class).remove(server);
			
			MasterObject master = new MasterObject();
			master.setName("MASTER");
			master.setTestObject((TestObject) l.get(0));
			
			server.save(master);
			
			assertNotNull(master.getId());
			
			MasterObject m1 = (MasterObject)server.get(MasterObject.class, master.getId());
			
			assertNotNull(m1);
			assertEquals("MASTER", m1.getName());
			assertNotNull(m1.getTestObject());
			assertEquals("Jakob", m1.getTestObject().getFirstname());
			
			System.out.println(m1);
			
		} catch(Exception e) {
			e.printStackTrace();
			fail("Exception thrown.");
			
		}
		
	}

	

}
