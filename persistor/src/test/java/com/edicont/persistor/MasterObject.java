package com.edicont.persistor;

import com.edicont.persistor.annotations.Reference;
import com.edicont.persistor.annotations.Uuid;

public class MasterObject {
	
	@Uuid
	private String id;
	private String name;
	@Reference
	private TestObject testObject;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestObject getTestObject() {
		return testObject;
	}

	public void setTestObject(TestObject testObject) {
		this.testObject = testObject;
	}

	@Override
	public String toString() {
		return "MasterObject [id=" + id + ", name=" + name + ", testObject="
				+ testObject + "]";
	}

	
}
