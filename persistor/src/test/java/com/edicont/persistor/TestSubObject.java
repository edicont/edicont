package com.edicont.persistor;

public class TestSubObject {
	
	private String value1;
	private int value2;
	private TestSubObject singleSub;
	
	public String getValue1() {
		return value1;
	}
	public void setValue1(String value1) {
		this.value1 = value1;
	}
	public int getValue2() {
		return value2;
	}
	public void setValue2(int value2) {
		this.value2 = value2;
	}
	public TestSubObject getSingleSub() {
		return singleSub;
	}
	public void setSingleSub(TestSubObject singleSub) {
		this.singleSub = singleSub;
	}
	
	@Override
	public String toString() {
		return "TestSubObject [value1=" + value1 + ", value2=" + value2
				+ ", singleSub=" + singleSub + "]";
	}
	

}
